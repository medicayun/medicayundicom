/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4chex.archive.dcm.storescp;

import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ejb.CreateException;

import org.dcm4che.auditlog.AuditLoggerFactory;
import org.dcm4che.auditlog.InstancesAction;
import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmDecodeParam;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmEncodeParam;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.DcmParser;
import org.dcm4che.data.DcmParserFactory;
import org.dcm4che.dict.Status;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.VRs;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.Association;
import org.dcm4che.net.AssociationListener;
import org.dcm4che.net.DcmServiceBase;
import org.dcm4che.net.DcmServiceException;
import org.dcm4che.net.Dimse;
import org.dcm4che.net.PDU;
import org.dcm4cheri.util.StringUtils;
import org.dcm4chex.archive.codec.CompressCmd;
import org.dcm4chex.archive.common.PrivateTags;
import org.dcm4chex.archive.config.CompressionRules;
import org.dcm4chex.archive.ejb.interfaces.FileDTO;
import org.dcm4chex.archive.ejb.interfaces.FileSystemMgt;
import org.dcm4chex.archive.ejb.interfaces.FileSystemMgtHome;
import org.dcm4chex.archive.ejb.interfaces.Storage;
import org.dcm4chex.archive.ejb.interfaces.StorageHome;
import org.dcm4chex.archive.ejb.jdbc.QueryFilesCmd;
import org.dcm4chex.archive.mbean.FileSystemInfo;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.dcm4chex.archive.util.FileUtils;
import org.dcm4chex.archive.util.HomeFactoryException;
import org.jboss.logging.Logger;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision: 1738 $
 * @since 03.08.2003
 */
public class StoreScp extends DcmServiceBase implements AssociationListener {

    private static final int[] TYPE1_ATTR = { Tags.StudyInstanceUID,
            Tags.SeriesInstanceUID, Tags.SOPInstanceUID, Tags.SOPClassUID, };

    private final StoreScpService service;

    private final Logger log;

    private int bufferSize = 512;

    private int updateDatabaseMaxRetries = 2;

    private int maxCountUpdateDatabaseRetries = 0;

    private boolean storeDuplicateIfDiffMD5 = true;

    private boolean storeDuplicateIfDiffHost = true;

    private CompressionRules compressionRules = new CompressionRules("");

    private HashSet coerceWarnCallingAETs = new HashSet();

    private long updateDatabaseRetryInterval = 0L;

    private long outOfResourcesThreshold = 30000000L;

    public StoreScp(StoreScpService service) {
        this.service = service;
        this.log = service.getLog();
    }

    public final String getCoerceWarnCallingAETs() {
        if (coerceWarnCallingAETs.isEmpty())
            return "NONE";
        StringBuffer sb = new StringBuffer();
        Iterator it = coerceWarnCallingAETs.iterator();
        sb.append(it.next());
        while (it.hasNext())
            sb.append(',').append(it.next());
        return sb.toString();
    }

    public final void setCoerceWarnCallingAETs(String aets) {
        coerceWarnCallingAETs.clear();
        if ("NONE".equals(aets))
            return;
        coerceWarnCallingAETs.addAll(Arrays.asList(StringUtils
                .split(aets, '\\')));
    }

    public final boolean isStoreDuplicateIfDiffHost() {
        return storeDuplicateIfDiffHost;
    }

    public final void setStoreDuplicateIfDiffHost(boolean storeDuplicate) {
        this.storeDuplicateIfDiffHost = storeDuplicate;
    }

    public final boolean isStoreDuplicateIfDiffMD5() {
        return storeDuplicateIfDiffMD5;
    }

    public final void setStoreDuplicateIfDiffMD5(boolean storeDuplicate) {
        this.storeDuplicateIfDiffMD5 = storeDuplicate;
    }

    public final CompressionRules getCompressionRules() {
        return compressionRules;
    }

    public final void setCompressionRules(CompressionRules compressionRules) {
        this.compressionRules = compressionRules;
    }

    public final int getBufferSize() {
        return bufferSize;
    }

    public final void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public final int getUpdateDatabaseMaxRetries() {
        return updateDatabaseMaxRetries;
    }

    public final void setUpdateDatabaseMaxRetries(int updateDatabaseMaxRetries) {
        this.updateDatabaseMaxRetries = updateDatabaseMaxRetries;
    }

    public final int getMaxCountUpdateDatabaseRetries() {
        return maxCountUpdateDatabaseRetries;
    }

    public final void setMaxCountUpdateDatabaseRetries(int count) {
        this.maxCountUpdateDatabaseRetries = count;
    }

    public final long getUpdateDatabaseRetryInterval() {
        return updateDatabaseRetryInterval;
    }

    public final void setUpdateDatabaseRetryInterval(long interval) {
        this.updateDatabaseRetryInterval = interval;
    }

    public final long getOutOfResourcesThreshold() {
        return outOfResourcesThreshold;
    }

    public final void setOutOfResourcesThreshold(long threshold) {
        this.outOfResourcesThreshold = threshold;
    }

    protected void doCStore(ActiveAssociation activeAssoc, Dimse rq,
            Command rspCmd) throws IOException, DcmServiceException {
        Command rqCmd = rq.getCommand();
        String iuid = rqCmd.getAffectedSOPInstanceUID();
        String cuid = rqCmd.getAffectedSOPClassUID();
        InputStream in = rq.getDataAsStream();
        Association assoc = activeAssoc.getAssociation();
        File file = null;
        try {
            List duplicates = new QueryFilesCmd(iuid).execute();
            if (!(duplicates.isEmpty() || storeDuplicateIfDiffMD5 || storeDuplicateIfDiffHost
                    && !containsLocal(duplicates))) {
                log.info("Received Instance[uid=" + iuid
                        + "] already exists - ignored");
                return;
            }

            DcmDecodeParam decParam = DcmDecodeParam.valueOf(rq
                    .getTransferSyntaxUID());
            Dataset ds = objFact.newDataset();
            DcmParser parser = DcmParserFactory.getInstance().newDcmParser(in);
            parser.setDcmHandler(ds.getDcmHandler());
            parser.parseDataset(decParam, Tags.PixelData);
            service.logDataset("Dataset:\n", ds);
            checkDataset(rqCmd, ds);

            FileSystemInfo fsInfo = service.selectStorageFileSystem();
            if (fsInfo.getAvailable() < outOfResourcesThreshold)
                throw new DcmServiceException(Status.OutOfResources);
            File baseDir = fsInfo.getDirectory();
            file = makeFile(baseDir, ds);
            String compressTSUID = parser.getReadTag() == Tags.PixelData
                    && parser.getReadLength() != -1 ? compressionRules
                    .getTransferSyntaxFor(assoc, ds) : null;
            ds.setFileMetaInfo(objFact.newFileMetaInfo(rqCmd
                    .getAffectedSOPClassUID(), rqCmd
                    .getAffectedSOPInstanceUID(),
                    compressTSUID != null ? compressTSUID : rq
                            .getTransferSyntaxUID()));

            byte[] md5sum = storeToFile(parser, ds, file);
            if (ignoreDuplicate(duplicates, md5sum)) {
                log.info("Received Instance[uid=" + iuid
                        + "] already exists - ignored");
                deleteFailedStorage(file);
                return;
            }

            final int baseDirPathLength = baseDir.getPath().length();
            final String filePath = file.getPath().substring(
                    baseDirPathLength + 1).replace(File.separatorChar, '/');
            ds.setPrivateCreatorID(PrivateTags.CreatorID);
            ds.putAE(PrivateTags.CallingAET, assoc.getCallingAET());
            ds.putAE(PrivateTags.CalledAET, assoc.getCalledAET());
            ds.putAE(Tags.RetrieveAET, fsInfo.getRetrieveAET());
            Dataset coercedElements = updateDB(ds, fsInfo, filePath, file, 
            		md5sum);
            if (coercedElements.isEmpty()
                    || !coerceWarnCallingAETs.contains(assoc.getCallingAET())) {
                rspCmd.putUS(Tags.Status, Status.Success);
            } else {
                int[] coercedTags = new int[coercedElements.size()];
                Iterator it = coercedElements.iterator();
                for (int i = 0; i < coercedTags.length; i++) {
                    coercedTags[i] = ((DcmElement) it.next()).tag();
                }
                rspCmd.putAT(Tags.OffendingElement, coercedTags);
                rspCmd.putUS(Tags.Status, Status.CoercionOfDataElements);
            }
            ds.putAll(coercedElements);
            updateIANInfo(assoc, ds, fsInfo.getRetrieveAET());
            updateInstancesStored(assoc, ds);
            updateStudyInfos(assoc, ds, fsInfo.getPath());
        } catch (DcmServiceException e) {
            log.warn(e.getMessage(), e);
            deleteFailedStorage(file);
            throw e;
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            deleteFailedStorage(file);
            throw new DcmServiceException(Status.ProcessingFailure, e);
        }
    }

    private boolean containsLocal(List duplicates) {
        for (int i = 0, n = duplicates.size(); i < n; ++i) {
            FileDTO dto = (FileDTO) duplicates.get(i);
            if (service.isLocalFileSystem(dto.getDirectoryPath()))
                return true;
        }
        return false;
    }

    private boolean ignoreDuplicate(List duplicates, byte[] md5sum) {
        for (int i = 0, n = duplicates.size(); i < n; ++i) {
            FileDTO dto = (FileDTO) duplicates.get(i);
            if (storeDuplicateIfDiffMD5
                    && !Arrays.equals(md5sum, dto.getFileMd5()))
                continue;
            if (storeDuplicateIfDiffHost
                    && !service.isLocalFileSystem(dto.getDirectoryPath()))
                continue;
            return true;
        }
        return false;
    }

    private void deleteFailedStorage(File file) {
        if (file == null)
            return;
        log.info("M-DELETE file:" + file);
        file.delete();
        // purge empty series and study directory
        File seriesDir = file.getParentFile();
        if (seriesDir.delete())
            seriesDir.getParentFile().delete();
    }

    private Dataset updateDB(Dataset ds, FileSystemInfo fsInfo, String filePath,
    		File file, byte[] md5)
            throws DcmServiceException, CreateException, HomeFactoryException,
            IOException {
        Storage storage = getStorageHome().create();
        try {
            int retry = 0;
            for (;;) {
                try {
                    synchronized (this) {
                        return storage.store(ds, fsInfo.getPath(), filePath,
                                (int) file.length(), md5);
                    }
                } catch (Exception e) {
                    ++retry;
                    if (retry > updateDatabaseMaxRetries) {
                        service.getLog().error(
                                "failed to update DB with entries for received "
                                        + file, e);
                        throw new DcmServiceException(Status.ProcessingFailure,
                                e);
                    }
                    maxCountUpdateDatabaseRetries = Math.max(retry,
                            maxCountUpdateDatabaseRetries);
                    service.getLog().warn(
                            "failed to update DB with entries for received "
                                    + file + " - retry", e);
                    try {
                        Thread.sleep(updateDatabaseRetryInterval);
                    } catch (InterruptedException e1) {
                        log.warn("update Database Retry Interval interrupted:",
                                e1);
                    }
                }
            }
        } finally {
            try {
                storage.remove();
            } catch (Exception ignore) {
            }
        }
    }

    private File makeFile(File basedir, Dataset ds) throws IOException {
        Calendar today = Calendar.getInstance();
        File dir = new File(basedir, String.valueOf(today.get(Calendar.YEAR))
                + File.separator + toDec(today.get(Calendar.MONTH) + 1)
                + File.separator + toDec(today.get(Calendar.DAY_OF_MONTH))
                + File.separator
                + toHex(ds.getString(Tags.StudyInstanceUID).hashCode())
                + File.separator
                + toHex(ds.getString(Tags.SeriesInstanceUID).hashCode()));
        if (!dir.exists()) {
            dir.mkdirs();
        }
		return FileUtils.createNewFile(dir, ds.getString(Tags.SOPInstanceUID).hashCode());
    }

    private StorageHome getStorageHome() throws HomeFactoryException {
        return (StorageHome) EJBHomeFactory.getFactory().lookup(
                StorageHome.class, StorageHome.JNDI_NAME);
    }

    private FileSystemMgtHome getFileSystemMgtHome()
            throws HomeFactoryException {
        return (FileSystemMgtHome) EJBHomeFactory.getFactory().lookup(
                FileSystemMgtHome.class, FileSystemMgtHome.JNDI_NAME);
    }

    private byte[] storeToFile(DcmParser parser, Dataset ds, File file)
    		throws Exception {
        log.info("M-WRITE file:" + file);
        MessageDigest md = MessageDigest.getInstance("MD5");
        BufferedOutputStream os = new BufferedOutputStream(
                new FileOutputStream(file));
        DigestOutputStream dos = new DigestOutputStream(os, md);
        try {
            DcmDecodeParam decParam = parser.getDcmDecodeParam();
            String tsuid = ds.getFileMetaInfo().getTransferSyntaxUID();
			DcmEncodeParam encParam = DcmEncodeParam.valueOf(tsuid);
            CompressCmd compressCmd = null;
            if (!decParam.encapsulated && encParam.encapsulated) {
                compressCmd = CompressCmd.createCompressCmd(ds);
                compressCmd.coerceDataset(ds);
            }
            ds.writeFile(dos, encParam);
            if (parser.getReadTag() != Tags.PixelData)
                return md.digest();
            int len = parser.getReadLength();
            InputStream in = parser.getInputStream();
            byte[] buffer = new byte[bufferSize];
            if (encParam.encapsulated) {
                ds.writeHeader(dos, encParam, Tags.PixelData, VRs.OB, -1);
                if (decParam.encapsulated) {
                    parser.parseHeader();
                    while (parser.getReadTag() == Tags.Item) {
                        len = parser.getReadLength();
                        ds.writeHeader(dos, encParam, Tags.Item, VRs.NONE, len);
                        copy(in, dos, len, buffer);
                        parser.parseHeader();
                    }
                } else {
                    int read = compressCmd.compress(decParam.byteOrder, parser
                            .getInputStream(), dos);
                    in.skip(parser.getReadLength() - read);
                }
                ds.writeHeader(dos, encParam, Tags.SeqDelimitationItem,
                        VRs.NONE, 0);
            } else {
                ds.writeHeader(dos, encParam, Tags.PixelData, parser
                        .getReadVR(), len);
                copy(in, dos, len, buffer);
            }
            parser.parseDataset(decParam, -1);
            ds.subSet(Tags.PixelData, -1).writeDataset(dos, encParam);
        } finally {
            try {
                dos.close();
            } catch (IOException ignore) {
            }
        }
        return md.digest();
    }

    private void copy(InputStream in, OutputStream out, int totLen,
            byte[] buffer) throws IOException {
        for (int len, toRead = totLen; toRead > 0; toRead -= len) {
            len = in.read(buffer, 0, Math.min(toRead, buffer.length));
            if (len == -1) {
                throw new EOFException();
            }
            out.write(buffer, 0, len);
        }
    }

    private void checkDataset(Command rqCmd, Dataset ds)
            throws DcmServiceException {
        for (int i = 0; i < TYPE1_ATTR.length; ++i) {
            if (ds.vm(TYPE1_ATTR[i]) <= 0) {
                throw new DcmServiceException(
                        Status.DataSetDoesNotMatchSOPClassError,
                        "Missing Type 1 Attribute "
                                + Tags.toString(TYPE1_ATTR[i]));
            }
        }
        if (!rqCmd.getAffectedSOPInstanceUID().equals(
                ds.getString(Tags.SOPInstanceUID))) {
            throw new DcmServiceException(
                    Status.DataSetDoesNotMatchSOPClassError,
                    "SOP Instance UID in Dataset differs from Affected SOP Instance UID");
        }
        if (!rqCmd.getAffectedSOPClassUID().equals(
                ds.getString(Tags.SOPClassUID))) {
            throw new DcmServiceException(
                    Status.DataSetDoesNotMatchSOPClassError,
                    "SOP Class UID in Dataset differs from Affected SOP Class UID");
        }
    }

    private static char[] HEX_DIGIT = { '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	private static final int[] SCN_TAGS = { Tags.SpecificCharacterSet,
		Tags.PatientID, Tags.PatientName, Tags.StudyID, Tags.StudyInstanceUID };

    private String toHex(int val) {
        char[] ch8 = new char[8];
        for (int i = 8; --i >= 0; val >>= 4) {
            ch8[i] = HEX_DIGIT[val & 0xf];
        }
        return String.valueOf(ch8);
    }

    private String toDec(int val) {
        return String.valueOf(new char[] { HEX_DIGIT[val / 10],
                HEX_DIGIT[val % 10] });
    }

    private void mkdir(File dir) {
        if (dir.mkdir()) {
            log.info("M-WRITE dir:" + dir);
        }
    } // Implementation of AssociationListener

    public void write(Association src, PDU pdu) {
    }

    public void received(Association src, PDU pdu) {
    }

    public void write(Association src, Dimse dimse) {
    }

    public void received(Association src, Dimse dimse) {
    }

    public void error(Association src, IOException ioe) {
    }

    public void closing(Association assoc) {
    }

    public void closed(Association assoc) {
        final Map ians = (Map) assoc.getProperty(StoreScpService.IANS_KEY);
        if (ians != null) {
            updateDBStudiesAndSeries(ians);
            updateStudyAccessTime((HashSet) assoc.getProperty("StudyInfos"));
        }
        logInstancesStored(assoc);
        service.sendReleaseNotification(assoc);
    }

    private void updateDBStudiesAndSeries(Map ians) {
        Storage store;
        try {
            store = getStorageHome().create();
        } catch (Exception e) {
            log.fatal("Failed to access Storage EJB");
            return;
        }
        try {
            for (Iterator it = ians.values().iterator(); it.hasNext();) {
                Dataset ian = (Dataset) it.next();
                DcmElement seq = ian.get(Tags.RefSeriesSeq);
                for (int i = 0, n = seq != null ? seq.vm() : 0; i < n; ++i) {
                    Dataset ser = seq.getItem(i);
                    final String seriuid = ser
                            .getString(Tags.SeriesInstanceUID);
                    updateDBSeries(store, seriuid);
                }
                final String suid = ian.getString(Tags.StudyInstanceUID);
                updateDBStudy(store, suid);
            }
        } finally {
            try {
                store.remove();
            } catch (Exception ignore) {
            }
        }
    }

    private void updateDBStudy(Storage store, final String suid) {
        int retry = 0;
        for (;;) {
            try {
                synchronized (this) {
                    store.updateStudy(suid);
                }
                return;
            } catch (Exception e) {
                ++retry;
                if (retry > updateDatabaseMaxRetries) {
                    service.getLog().error(
                            "give up update DB Study Record [iuid=" + suid
                                    + "]:", e);
                    ;
                }
                maxCountUpdateDatabaseRetries = Math.max(retry,
                        maxCountUpdateDatabaseRetries);
                service.getLog().warn(
                        "failed to update DB Study Record [iuid=" + suid
                                + "] - retry:", e);
                try {
                    Thread.sleep(updateDatabaseRetryInterval);
                } catch (InterruptedException e1) {
                    log.warn("update Database Retry Interval interrupted:", e1);
                }
            }
        }
    }

    private void updateDBSeries(Storage store, final String seriuid) {
        int retry = 0;
        for (;;) {
            try {
                synchronized (this) {
                    store.updateSeries(seriuid);
                }
                return;
            } catch (Exception e) {
                ++retry;
                if (retry > updateDatabaseMaxRetries) {
                    service.getLog().error(
                            "give up update DB Series Record [iuid=" + seriuid
                                    + "]:", e);
                    ;
                }
                maxCountUpdateDatabaseRetries = Math.max(retry,
                        maxCountUpdateDatabaseRetries);
                service.getLog().warn(
                        "failed to update DB Series Record [iuid=" + seriuid
                                + "] - retry", e);
                try {
                    Thread.sleep(updateDatabaseRetryInterval);
                } catch (InterruptedException e1) {
                    log.warn("update Database Retry Interval interrupted:", e1);
                }
            }
        }
    }

    private void updateIANInfo(Association assoc, Dataset ds, String retrieveAET) {
        Map ians = (Map) assoc.getProperty(StoreScpService.IANS_KEY);
        if (ians == null) {
            assoc.putProperty(StoreScpService.IANS_KEY, ians = new HashMap());
        }
        Dataset refSOP = getRefSOPSeq(ds, getRefSeriesSeq(ds, ians))
                .addNewItem();
        refSOP.putAE(Tags.RetrieveAET, retrieveAET);
        refSOP.putCS(Tags.InstanceAvailability, "ONLINE");
        refSOP.putUI(Tags.RefSOPClassUID, ds.getString(Tags.SOPClassUID));
        refSOP.putUI(Tags.RefSOPInstanceUID, ds.getString(Tags.SOPInstanceUID));
    }

    private DcmElement getRefSeriesSeq(Dataset ds, Map ians) {
        final String siud = ds.getString(Tags.StudyInstanceUID);
        Dataset ian = (Dataset) ians.get(siud);
        if (ian != null) {
            return ian.get(Tags.RefSeriesSeq);
        }
        ians.put(siud, ian = DcmObjectFactory.getInstance().newDataset());
        ian.putAll(ds.subSet(SCN_TAGS));
        DcmElement ppsSeq = ian.putSQ(Tags.RefPPSSeq);
        Dataset pps = ds.getItem(Tags.RefPPSSeq);
        if (pps != null) {
            // add IAN Type 2 Attribute
            if (!pps.contains(Tags.PerformedWorkitemCodeSeq))
                pps.putSQ(Tags.PerformedWorkitemCodeSeq);
            ppsSeq.addItem(pps);
        }
        return ian.putSQ(Tags.RefSeriesSeq);
    }

    private DcmElement getRefSOPSeq(Dataset ds, DcmElement seriesSq) {
        final String siud = ds.getString(Tags.SeriesInstanceUID);
        Dataset info;
        for (int i = 0, n = seriesSq.vm(); i < n; ++i) {
            info = seriesSq.getItem(i);
            if (siud.equals(info.getString(Tags.SeriesInstanceUID))) {
                return info.get(Tags.RefSOPSeq);
            }
        }
        info = seriesSq.addNewItem();
        info.putUI(Tags.SeriesInstanceUID, siud);
        return info.putSQ(Tags.RefSOPSeq);
    }

    void updateInstancesStored(Association assoc, Dataset ds) {
        try {
            InstancesAction stored = (InstancesAction) assoc
                    .getProperty("InstancesStored");
            String suid = ds.getString(Tags.StudyInstanceUID);
            if (stored != null
                    && !stored.listStudyInstanceUIDs()[0].equals(suid)) {
                logInstancesStored(assoc);
                stored = null;
            }
            if (stored == null) {
                final AuditLoggerFactory alf = AuditLoggerFactory.getInstance();
                stored = alf.newInstancesAction("Create", suid,
                        alf.newPatient(
                            ds.getString(Tags.PatientID), 
                            ds.getString(Tags.PatientName)));
                stored.setAccessionNumber(ds.getString(Tags.AccessionNumber));
                assoc.putProperty("InstancesStored", stored);
            }
            stored.incNumberOfInstances(1);
            stored.addSOPClassUID(ds.getString(Tags.SOPClassUID));
        } catch (Exception e) {
            log.error("Could not audit log InstancesStored:", e);
        }
    }

    void logInstancesStored(Association assoc) {
        InstancesAction stored = (InstancesAction) assoc
                .getProperty("InstancesStored");
        if (stored != null) {
            service.logInstancesStored(
                    AuditLoggerFactory.getInstance().newRemoteNode(assoc.getSocket(),
                            assoc.getCallingAET()), stored);
        }
        assoc.putProperty("InstancesStored", null);
    }

    private void updateStudyInfos(Association assoc, Dataset ds, String basedir) {
        HashSet studyInfos = (HashSet) assoc.getProperty("StudyInfos");
        if (studyInfos == null) {
            studyInfos = new HashSet();
            assoc.putProperty("StudyInfos", studyInfos);
        }
        String suid = ds.getString(Tags.StudyInstanceUID);
        studyInfos.add(suid + '@' + basedir);
    }

    private void updateStudyAccessTime(HashSet studyInfos) {
        if (studyInfos == null)
            return;
        FileSystemMgt fsMgt;
        try {
            fsMgt = getFileSystemMgtHome().create();
        } catch (Exception e) {
            log.fatal("Failed to access FileSystemMgt EJB");
            return;
        }
        try {
            for (Iterator it = studyInfos.iterator(); it.hasNext();) {
                String studyInfo = (String) it.next();
                int delim = studyInfo.indexOf('@');
                try {
                    fsMgt.touchStudyOnFileSystem(studyInfo.substring(0, delim),
                            studyInfo.substring(delim + 1));
                } catch (Exception e) {
                    log.warn("Failed to update access time for study "
                            + studyInfo, e);
                }
            }
        } finally {
            try {
                fsMgt.remove();
            } catch (Exception ignore) {
            }
        }
    }
}