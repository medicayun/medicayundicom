/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.cdw.mbean;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.FileMetaInfo;
import org.dcm4che.dict.Tags;
import org.dcm4che.media.DirBuilderFactory;
import org.dcm4che.media.DirRecord;
import org.dcm4che.media.DirWriter;
import org.dcm4che.util.UIDGenerator;
import org.dcm4cheri.util.StringUtils;
import org.dcm4chex.cdw.common.Executer;
import org.dcm4chex.cdw.common.ExecutionStatusInfo;
import org.dcm4chex.cdw.common.FileUtils;
import org.dcm4chex.cdw.common.Flag;
import org.dcm4chex.cdw.common.MediaCreationException;
import org.dcm4chex.cdw.common.MediaCreationRequest;
import org.dcm4chex.cdw.common.SpoolDirDelegate;
import org.jboss.logging.Logger;

/**
 * @author gunter.zeilinter@tiani.com
 * @version $Revision: 4142 $ $Date: 2004-07-09 05:40:43 +0800 (周五, 09 7月 2004) $
 * @since 29.06.2004
 *
 */
class FilesetBuilder {

    private static final char[] HEX_DIGIT = { '0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    private static final UIDGenerator uidgen = UIDGenerator.getInstance();

    private static final DirBuilderFactory dbf = DirBuilderFactory
            .getInstance();

    private final MediaComposerService service;

    private final SpoolDirDelegate spoolDir;

    private final DirRecordFactory recFact;

    private final MediaCreationRequest rq;

    private final Dataset attrs;

    private final Logger log;

    private final boolean preserveInstances;

    private final File mergeDir;

    private static String toHex(int val) {
        char[] ch8 = new char[8];
        for (int i = 8; --i >= 0; val >>= 4)
            ch8[i] = HEX_DIGIT[val & 0xf];

        return String.valueOf(ch8);
    }

    private static String[] makeFileIDs(String pid, String suid, String seruid,
            String iuid) {
        return new String[] { "DICOM", toHex(pid.hashCode()),
                toHex(suid.hashCode()), toHex(seruid.hashCode()),
                toHex(iuid.hashCode()),};
    }

    public FilesetBuilder(MediaComposerService service,
            MediaCreationRequest rq, Dataset attrs) {
        this.service = service;
        this.spoolDir = service.getSpoolDir();
        this.recFact = service.getDirRecordFactory();
        this.log = service.getLog();
        this.rq = rq;
        this.attrs = attrs;
        this.preserveInstances = Flag.isYes(attrs
                .getString(Tags.PreserveCompositeInstancesAfterMediaCreation));
        this.mergeDir = Flag.isYes(attrs
                .getString(Tags.IncludeDisplayApplication)) ? service
                .getMergeDirViewer() : service.getMergeDir();
    }

    public void build() throws MediaCreationException {
        try {
            File rootDir = mkRootDir(spoolDir);
            rq.setFilesetDir(rootDir);
            File ddFile = new File(rootDir, "DICOMDIR");
            HashMap patRecs = new HashMap();
            HashMap styRecs = new HashMap();
            HashMap serRecs = new HashMap();
            File readmeFile = new File(rootDir, service
                    .getFileSetDescriptorFile());
            DirWriter dirWriter = dbf.newDirWriter(ddFile, rootDir.getName(),
                    rq.getFilesetID(), readmeFile, service
                            .getCharsetOfFileSetDescriptorFile(), null);
            try {
                DcmElement refSOPs = attrs.get(Tags.RefSOPSeq);
                for (int i = 0, n = refSOPs.vm(); i < n; ++i) {
                    addFile(rootDir, refSOPs.getItem(i), dirWriter, patRecs,
                            styRecs, serRecs);
                }

            } finally {
                dirWriter.close();
            }
            File[] files = mergeDir.listFiles();
            for (int i = 0; i < files.length; ++i)
                makeSymLink(files[i], new File(rootDir, files[i].getName()));
        } catch (IOException e) {
            throw new MediaCreationException(ExecutionStatusInfo.PROC_FAILURE,
                    e);
        }
    }

    private void addFile(File rootDir, Dataset item, DirWriter dirWriter,
            HashMap patRecs, HashMap styRecs, HashMap serRecs)
            throws IOException {
        String iuid = item.getString(Tags.RefSOPInstanceUID);
        File src = spoolDir.getInstanceFile(iuid);
        Dataset ds = FileUtils.readDataset(src, log);
        FileMetaInfo fmi = ds.getFileMetaInfo();
        String tsuid = fmi.getTransferSyntaxUID();
        String cuid = fmi.getMediaStorageSOPClassUID();
        String pid = ds.getString(Tags.PatientID);
        String suid = ds.getString(Tags.StudyInstanceUID);
        String seruid = ds.getString(Tags.SeriesInstanceUID);
        String[] fileIDs = makeFileIDs(pid, suid, seruid, iuid);
        DirRecord serRec = (DirRecord) serRecs.get(seruid);
        if (serRec == null) {
            DirRecord styRec = (DirRecord) styRecs.get(suid);
            if (styRec == null) {
                DirRecord patRec = (DirRecord) patRecs.get(pid);
                if (patRec == null) {
                    patRec = dirWriter.add(null, "PATIENT", recFact.makeRecord(
                            DirRecord.PATIENT, ds));
                    patRecs.put(pid, patRec);
                }
                styRec = dirWriter.add(patRec, "STUDY", recFact.makeRecord(
                        DirRecord.STUDY, ds));
                styRecs.put(suid, styRec);
            }
            serRec = dirWriter.add(styRec, "SERIES", recFact.makeRecord(
                    DirRecord.SERIES, ds));
            serRecs.put(seruid, serRec);
        }
        String recType = DirBuilderFactory.getRecordType(cuid);
        dirWriter.add(serRec, recType, recFact.makeRecord(recType, ds),
                fileIDs, cuid, iuid, tsuid);

        File dest = new File(rootDir, StringUtils.toString(fileIDs,
                File.separatorChar));
        File parent = dest.getParentFile();
        if (!parent.exists() && !parent.mkdirs())
                throw new IOException("Failed to mkdirs " + parent);
        if (preserveInstances)
            makeSymLink(src, dest);
        else
            move(src, dest);
    }

    private File mkRootDir(SpoolDirDelegate spoolDir) throws IOException {
        String uid = attrs.getString(Tags.StorageMediaFileSetUID);
        if (uid == null) uid = uidgen.createUID();

        File d;
        while ((d = spoolDir.getMediaFilesetRootDir(uid)).exists()) {
            log.warn("Duplicate file set UID:" + uid + " - generate new uid");
            uid = uidgen.createUID();
        }
        if (!d.mkdir()) throw new IOException("Failed to mkdir " + d);
        return d;
    }

    private void move(File src, File dst) throws IOException {
        if (log.isDebugEnabled()) log.debug("mv " + src + " " + dst);
        if (!src.renameTo(dst))
                throw new IOException("mv " + src + " " + dst + " failed!");
    }

    private void makeSymLink(File src, File dst) throws IOException {
        String[] cmd = new String[] { "ln", "-s", src.getAbsolutePath(),
                dst.getAbsolutePath()};
        try {
            if (new Executer(log, cmd, null, null).waitFor() == 0) return;
        } catch (InterruptedException e) {
        }
        throw new IOException(StringUtils.toString(cmd, ' ') + " failed!");
    }

}
