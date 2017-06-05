/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.cdw.mbean;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BandedSampleModel;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.FileMetaInfo;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4che.imageio.plugins.DcmMetadata;
import org.dcm4che.media.DirBuilderFactory;
import org.dcm4che.media.DirReader;
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

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

/**
 * @author gunter.zeilinter@tiani.com
 * @version $Revision: 4166 $ $Date: 2004-07-28 13:48:59 +0800 (周三, 28 7月 2004) $
 * @since 29.06.2004
 *
 */
class FilesetBuilder {

    private static final String DICOM = "DICOM";

    private static final String IHE_PDI = "IHE_PDI";

    private static final char[] HEX_DIGIT = { '0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    private static final UIDGenerator uidgen = UIDGenerator.getInstance();

    private static final DcmObjectFactory dof = DcmObjectFactory.getInstance();

    private static final DirBuilderFactory dbf = DirBuilderFactory
            .getInstance();

    private final MediaComposerService service;

    private final SpoolDirDelegate spoolDir;

    private final DirRecordFactory recFact;

    private final MediaCreationRequest rq;

    private final Dataset attrs;

    private final Logger log;

    private final boolean debug;

    private final ImageReader reader;

    private final boolean preserveInstances;

    private final String web;

    private BufferedImage imageBI;

    private BufferedImage jpegBI;

    private BufferedImage iconBI;

    private byte[] iconPixelData;

    private boolean viewer;

    private final FilesetComponent fsRoot = FilesetComponent
            .makeRootFilesetComponent();

    private final Dataset recFilter = dof.newDataset();

    private static String toHex(int val) {
        char[] ch8 = new char[8];
        for (int i = 8; --i >= 0; val >>= 4)
            ch8[i] = HEX_DIGIT[val & 0xf];

        return String.valueOf(ch8);
    }

    private static String[] makeFileIDs(String pid, String suid, String seruid,
            String iuid) {
        return new String[] { DICOM, toHex(pid.hashCode()),
                toHex(suid.hashCode()), toHex(seruid.hashCode()),
                toHex(iuid.hashCode()),};
    }

    private static final class Pair {

        final DirRecord rec;

        final FilesetComponent comp;

        Pair(DirRecord rec, FilesetComponent comp) {
            this.rec = rec;
            this.comp = comp;
        }
    }

    public FilesetBuilder(MediaComposerService service,
            MediaCreationRequest rq, Dataset attrs) {
        this.service = service;
        this.spoolDir = service.getSpoolDir();
        this.recFact = service.getDirRecordFactory();
        this.log = service.getLog();
        this.debug = log.isDebugEnabled();
        this.reader = service.getImageReader();
        this.rq = rq;
        this.attrs = attrs;
        this.preserveInstances = Flag.isYes(attrs
                .getString(Tags.PreserveCompositeInstancesAfterMediaCreation));
        this.viewer = Flag.isYes(attrs
                .getString(Tags.IncludeDisplayApplication));
        this.web = attrs.getString(Tags.IncludeNonDICOMObjects, "NO");
    }

    final boolean isWeb() {
        return !"NO".equals(web);
    }

    final boolean isViewer() {
        return viewer;
    }

    public void buildFileset() throws MediaCreationException {
        try {
            File rootDir = mkRootDir(spoolDir, false);
            rq.setFilesetDir(rootDir);
            File readmeFile = new File(rootDir, service
                    .getFileSetDescriptorFile());
            DirWriter dirWriter = dbf.newDirWriter(rq.getDicomDirFile(),
                    rootDir.getName(), rq.getFilesetID(), readmeFile, service
                            .getCharsetOfFileSetDescriptorFile(), null);
            HashMap patRecs = new HashMap();
            HashMap styRecs = new HashMap();
            HashMap serRecs = new HashMap();
            try {
                DcmElement refSOPs = attrs.get(Tags.RefSOPSeq);
                for (int i = 0, n = refSOPs.vm(); i < n; ++i) {
                    addFile(rootDir, refSOPs.getItem(i), dirWriter, patRecs,
                            styRecs, serRecs);
                }

            } finally {
                dirWriter.close();
            }
            mergeDir(service.getMergeDir(), rootDir);
            if (isWeb()) {
                (new File(rootDir, IHE_PDI)).mkdir();
                mergeDir(service.getMergeDirWeb(), rootDir);
            }
            if (viewer) mergeDir(service.getMergeDirViewer(), rootDir);
        } catch (IOException e) {
            throw new MediaCreationException(ExecutionStatusInfo.PROC_FAILURE,
                    e);
        }
    }

    public long sizeOfDicomContent() {
        return fsRoot.size();
    }

    private void mergeDir(File src, File dest) throws IOException {
        if (debug) log.debug("merge " + src + " to " + dest);
        File[] files = src.listFiles();
        for (int i = 0; i < files.length; ++i) {
            File link = new File(dest, files[i].getName());
            if (link.isDirectory())
                mergeDir(files[i], link);
            else {
                if (link.isFile()) FileUtils.delete(link, log);
                makeSymLink(files[i], link);
            }
        }
    }

    private void addFile(File rootDir, Dataset item, DirWriter dirWriter,
            HashMap patRecs, HashMap styRecs, HashMap serRecs)
            throws IOException, MediaCreationException {
        String iuid = item.getString(Tags.RefSOPInstanceUID);
        String cuid = item.getString(Tags.RefSOPClassUID);
        File src = spoolDir.getInstanceFile(iuid);
        log.debug("M-READ " + src);
        ImageInputStream in = new FileImageInputStream(src);
        String[] fileIDs;
        try {
            reader.setInput(in);
            DcmMetadata metadata = (DcmMetadata) reader.getStreamMetadata();
            Dataset ds = metadata.getDataset();
            FileMetaInfo fmi = ds.getFileMetaInfo();
            String tsuid = fmi.getTransferSyntaxUID();

            String pid = ds.getString(Tags.PatientID);
            if (pid == null)
                    throw new MediaCreationException(
                            ExecutionStatusInfo.DIR_PROC_ERR,
                            "Missing Patient ID in instance[uid=" + iuid + "]");
            String suid = ds.getString(Tags.StudyInstanceUID);
            String seruid = ds.getString(Tags.SeriesInstanceUID);
            fileIDs = makeFileIDs(pid, suid, seruid, iuid);
            Pair serRec = (Pair) serRecs.get(seruid);
            if (serRec == null) {
                Pair styRec = (Pair) styRecs.get(suid);
                if (styRec == null) {
                    Pair patRec = (Pair) patRecs.get(pid);
                    if (patRec == null) {
                        patRec = new Pair(dirWriter.add(null,
                                DirRecord.PATIENT, recFact.makeRecord(
                                        DirRecord.PATIENT, ds)),
                                FilesetComponent.makePatientFilesetComponent(
                                        ds, toFilePath(fileIDs, 2)));
                        patRecs.put(pid, patRec);
                        fsRoot.addChild(patRec.comp);
                    }
                    styRec = new Pair(dirWriter.add(patRec.rec,
                            DirRecord.STUDY, recFact.makeRecord(
                                    DirRecord.STUDY, ds)), FilesetComponent
                            .makeStudyFilesetComponent(ds, toFilePath(fileIDs,
                                    3)));
                    styRecs.put(suid, styRec);
                    patRec.comp.addChild(styRec.comp);
                }
                serRec = new Pair(dirWriter.add(styRec.rec, DirRecord.SERIES,
                        recFact.makeRecord(DirRecord.SERIES, ds)),
                        FilesetComponent.makeSeriesFilesetComponent(ds,
                                toFilePath(fileIDs, 4)));
                serRecs.put(seruid, serRec);
                styRec.comp.addChild(serRec.comp);
            }
            FilesetComponent comp = FilesetComponent
                    .makeInstanceFilesetComponent(ds, toFilePath(fileIDs, 5));
            comp.incSize(src.length());
            serRec.comp.addChild(comp);
            String recType = DirBuilderFactory.getRecordType(cuid);
            Dataset rec = recFact.makeRecord(recType, ds);
            if (DirRecord.IMAGE.equals(recType)) {
                Dataset iconItem = item.getItem(Tags.IconImageSeq);
                if (iconItem == null && service.isCreateIcon())
                        if (!UIDs.ExplicitVRLittleEndian.equals(tsuid)
                                && !UIDs.ImplicitVRLittleEndian.equals(tsuid)) {
                            log
                                    .warn("Cannot generate icon from compressed image "
                                            + src);
                        } else {
                            try {
                                iconItem = mkIconItem(ds);
                            } catch (Exception e) {
                                log.warn("Failed to generate icon from " + src,
                                        e);
                            }
                        }
                if (iconItem != null)
                        rec.putSQ(Tags.IconImageSeq).addItem(iconItem);
                if (isWeb()) {
                    if (!UIDs.ExplicitVRLittleEndian.equals(tsuid)
                            && !UIDs.ImplicitVRLittleEndian.equals(tsuid)) {
                        log
                                .warn("Cannot generate jpeg from compressed DICOM image "
                                        + src);
                    } else {
                        try {
                            fileIDs[0] = IHE_PDI;
                            File jpegDir = new File(rq.getFilesetDir(),
                                    StringUtils.toString(fileIDs,
                                            File.separatorChar));
                            fileIDs[0] = DICOM;
                            mkJpegs(jpegDir);
                        } catch (Exception e) {
                            log.warn("Failed to generate jpeg from " + src, e);
                        }
                    }
                }
            }
            dirWriter.add(serRec.rec, recType, rec, fileIDs, cuid, iuid, tsuid);
        } finally {
            try {
                in.close();
            } catch (IOException ignore) {
            }
        }

        File dest = new File(rootDir, StringUtils.toString(fileIDs,
                File.separatorChar));
        mkParentDir(dest);
        if (preserveInstances)
            makeSymLink(src, dest);
        else
            move(src, dest);
    }

    private String toFilePath(String[] fileIDs, int n) {
        StringBuffer sb = new StringBuffer(fileIDs[0]);
        for (int i = 1; i < n; ++i)
            sb.append(File.separatorChar).append(fileIDs[i]);
        return sb.toString();
    }

    private void mkJpegs(File dir) throws IOException {
        dir.mkdirs();
        int w0 = reader.getWidth(0);
        int h0 = reader.getHeight(0);
        float ratio = reader.getAspectRatio(0);
        int w = Math.min(w0, service.getJpegWidth());
        int h = Math.min(h0, service.getJpegHeight());
        w = (int) Math.min(w, h * ratio);
        h = (int) Math.min(h, w / ratio);
        if (debug)
                log.debug("create jpegs[w=" + w + ",h=" + h + "] from image[w="
                        + w0 + ",h=" + h0 + "]");
        ImageReadParam param = reader.getDefaultReadParam();
        imageBI = checkReusable(imageBI, w0, h0);
        AffineTransformOp scaleOp = null;
        if (w != w0 || h != h0) {
            jpegBI = checkReusable(jpegBI, w, h);
            AffineTransform scale = AffineTransform.getScaleInstance((double) w
                    / w0, (double) h / h0);
            scaleOp = new AffineTransformOp(scale,
                    AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        }
        BufferedImage bi;
        for (int frame = 0, n = reader.getNumImages(false); frame < n; ++frame) {
            param.setDestination(imageBI);
            bi = imageBI = reader.read(frame, param);
            if (scaleOp != null) {
                // workaround for ImagingLib.filter(this, bi, dst) == null 
                // if bi.getSampleModel() instanceof BandedSampleModel 
                if (imageBI.getSampleModel() instanceof BandedSampleModel) {
                    log.debug("convert RGB plane to RGB pixel");
                    BufferedImage newbi = convertBI(imageBI,
                            BufferedImage.TYPE_INT_RGB);
                    imageBI.flush();
                    imageBI = newbi;
                    jpegBI = null;
                }
                bi = jpegBI = scaleOp.filter(imageBI, jpegBI);
                imageBI.flush();
            }
            File dest = new File(dir, "" + (frame + 1) + ".JPG");
            OutputStream out = new BufferedOutputStream(new FileOutputStream(
                    dest));
            try {
                JPEGImageEncoder enc = JPEGCodec.createJPEGEncoder(out);
                enc.encode(bi);
            } finally {
                out.close();
            }
            bi.flush();
        }
    }

    private BufferedImage convertBI(BufferedImage src, int imageType) {
        BufferedImage dst = new BufferedImage(src.getWidth(), src.getHeight(),
                imageType);
        Graphics2D big = dst.createGraphics();
        try {
            big.drawImage(src, 0, 0, null);
        } finally {
            big.dispose();
        }
        return dst;
    }

    private Dataset mkIconItem(Dataset ds) throws IOException {
        int frame = ds.getInt(Tags.RepresentativeFrameNumber, 1) - 1;
        frame = Math.max(0, frame);
        frame = Math.min(frame, ds.getInt(Tags.NumberOfFrames, 1) - 1);

        int w0 = reader.getWidth(0);
        int h0 = reader.getHeight(0);
        float ratio = reader.getAspectRatio(0);
        int w = Math.min(w0, service.getIconWidth());
        int h = Math.min(h0, service.getIconHeight());
        w = (int) Math.min(w, h * ratio);
        h = (int) Math.min(h, w / ratio);
        int xSubsampling = (w0 - 1) / w + 1;
        int ySubsampling = (h0 - 1) / h + 1;
        w = (w0 - 1) / xSubsampling + 1;
        h = (h0 - 1) / ySubsampling + 1;
        if (debug)
                log.debug("create icon[w=" + w + ",h=" + h + "] from image[w="
                        + w0 + ",h=" + h0 + "]");
        ImageReadParam param = reader.getDefaultReadParam();
        param.setSourceSubsampling(xSubsampling, ySubsampling, 0, 0);
        param.setDestination(checkReusable(iconBI, w, h));
        iconBI = reader.read(frame, param);
        if (w != iconBI.getWidth() || h != iconBI.getHeight()) {
            log.warn("created icon with unexpected dimension[w="
                    + iconBI.getWidth() + ",h=" + iconBI.getHeight()
                    + "] instead [w=" + w + ",h=" + h + "] from image[w=" + w0
                    + ",h=" + h0 + "]");
            w = iconBI.getWidth();
            h = iconBI.getHeight();
        }
        String pmi = ds.getString(Tags.PhotometricInterpretation);
        BufferedImage bi = iconBI;
        if ("RGB".equals(pmi)) {
            bi = convertBI(iconBI, BufferedImage.TYPE_BYTE_INDEXED);
            pmi = "PALETTE COLOR";
        }

        /*
         File dest = new File(ds.getString(Tags.SOPInstanceUID) + ".JPG");
         OutputStream out = new BufferedOutputStream(new FileOutputStream(
         dest));
         try {
         JPEGImageEncoder enc = JPEGCodec.createJPEGEncoder(out);
         enc.encode(bi);
         } finally {
         out.close();
         }
         */

        if (iconPixelData == null || iconPixelData.length != w * h)
                iconPixelData = new byte[w * h];
        Dataset iconItem = dof.newDataset();

        if ("PALETTE COLOR".equals(pmi)) {
            IndexColorModel cm = (IndexColorModel) bi.getColorModel();
            int[] lutDesc = { cm.getMapSize(), 0, 8};
            byte[] r = new byte[lutDesc[0]];
            byte[] g = new byte[lutDesc[0]];
            byte[] b = new byte[lutDesc[0]];
            cm.getReds(r);
            cm.getGreens(g);
            cm.getBlues(b);
            iconItem.putUS(Tags.RedPaletteColorLUTDescriptor, lutDesc);
            iconItem.putUS(Tags.GreenPaletteColorLUTDescriptor, lutDesc);
            iconItem.putUS(Tags.BluePaletteColorLUTDescriptor, lutDesc);
            iconItem.putOW(Tags.RedPaletteColorLUTData, ByteBuffer.wrap(r)
                    .order(ByteOrder.LITTLE_ENDIAN));
            iconItem.putOW(Tags.GreenPaletteColorLUTData, ByteBuffer.wrap(g)
                    .order(ByteOrder.LITTLE_ENDIAN));
            iconItem.putOW(Tags.BluePaletteColorLUTData, ByteBuffer.wrap(b)
                    .order(ByteOrder.LITTLE_ENDIAN));

            Raster raster = bi.getRaster();
            for (int y = 0, i = 0; y < h; ++y)
                for (int x = 0; x < w; ++x, ++i)
                    iconPixelData[i] = (byte) raster.getSample(x, y, 0);
        } else {
            pmi = "MONOCHROME2";
            for (int y = 0, i = 0; y < h; ++y)
                for (int x = 0; x < w; ++x, ++i)
                    iconPixelData[i] = (byte) bi.getRGB(x, y);
        }
        iconItem.putCS(Tags.PhotometricInterpretation, pmi);
        iconItem.putUS(Tags.Rows, h);
        iconItem.putUS(Tags.Columns, w);
        iconItem.putUS(Tags.SamplesPerPixel, 1);
        iconItem.putUS(Tags.BitsAllocated, 8);
        iconItem.putUS(Tags.BitsStored, 8);
        iconItem.putUS(Tags.HighBit, 7);
        iconItem.putOB(Tags.PixelData, iconPixelData);
        return iconItem;
    }

    private BufferedImage checkReusable(BufferedImage bi, int w, int h)
            throws IOException {
        if (bi == null || bi.getWidth() != w || bi.getHeight() != h)
                return null;
        ImageTypeSpecifier t1 = reader.getRawImageType(0);
        ImageTypeSpecifier t2 = ImageTypeSpecifier.createFromRenderedImage(bi);
        if (!t1.equals(t2)) return null;
        if (debug) log.debug("Reuse BufferedImage[w=" + w + ",h=" + h + "]");
        return bi;
    }

    private File mkRootDir(SpoolDirDelegate spoolDir, boolean newuid)
            throws IOException {
        String uid = newuid ? null : attrs
                .getString(Tags.StorageMediaFileSetUID);
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
        if (debug) log.debug("mv " + src + " " + dst);
        if (!src.renameTo(dst))
                throw new IOException("mv " + src + " " + dst + " failed!");
    }

    private void makeSymLink(File src, File dst) throws IOException {
        String[] cmd = new String[] { "ln", "-s", src.getAbsolutePath(),
                dst.getAbsolutePath()};
        try {
            if (new Executer(cmd).waitFor() == 0) return;
        } catch (InterruptedException e) {
        }
        throw new IOException(StringUtils.toString(cmd, ' ') + " failed!");
    }

    public ArrayList splitMedia(long freeSizeFirst, long freeSizeOther,
            DicomDirDOM dom) throws MediaCreationException, IOException {
        File ddFile = rq.getDicomDirFile();
        ArrayList fsList = new ArrayList();
        split(fsRoot, freeSizeFirst, freeSizeOther, fsList);
        rq.setVolsetSize(fsList.size());
        String fsIDPrefix = getFilesetIDPrefix();
        File oldDDFile = new File(rq.getFilesetDir(), "DICOMDIR.old");
        move(ddFile, oldDDFile);
        DirReader dirReader = dbf.newDirReader(oldDDFile);
        ArrayList rqList = new ArrayList();
        try {
            for (int i = 0, n = fsList.size(); i < n; ++i) {
                FilesetComponent comp = (FilesetComponent) fsList.get(i);
                File rootDir = rq.getFilesetDir();
                MediaCreationRequest newrq = rq;
                if (i > 0) {
                    rootDir = mkRootDir(spoolDir, false);
                    moveComponents(comp.childs(), rootDir);
                    mergeDir(service.getMergeDir(), rootDir);
                    newrq = new MediaCreationRequest(rq);
                    newrq.setFilesetDir(rootDir);
                    newrq.setVolsetSeqno(i + 1);
                    setVolsetSeqno(comp.childs(), i + 1, dom);
                }
                rqList.add(newrq);
                if (fsIDPrefix != null)
                        newrq.setFilesetID(fsIDPrefix + (i + 1));
                File newReadmeFile = new File(rootDir, service
                        .getFileSetDescriptorFile());
                DirWriter dirWriter = dbf.newDirWriter(newrq.getDicomDirFile(),
                        rootDir.getName(), newrq.getFilesetID(), newReadmeFile,
                        service.getCharsetOfFileSetDescriptorFile(), null);
                try {
                    copyPatientDirRecords(comp.root().childs(), dirWriter,
                            dirReader);
                } finally {
                    dirWriter.close();
                }
            }
        } finally {
            dirReader.close();
            FileUtils.delete(oldDDFile, log);
        }
        dom.updateSeqNo();
        return rqList;
    }

    private void setVolsetSeqno(List comps, int seqNo, DicomDirDOM dom) {
        for (int i = 0, n = comps.size(); i < n; ++i) {
            FilesetComponent comp = (FilesetComponent) comps.get(i);            
	        switch (comp.level()) {
	        case FilesetComponent.PATIENT:
	            dom.setPatientSeqNo(comp.id(), seqNo);
	            break;
	        case FilesetComponent.STUDY:
	            dom.setStudySeqNo(comp.parent().id(), comp.id(), seqNo);
	            break;
	        case FilesetComponent.SERIES:
	            dom.setSeriesSeqNo(comp.parent().parent().id(), comp.parent().id(),
	                    comp.id(), seqNo);
	            break;
	        case FilesetComponent.INSTANCE:
	            dom.setInstanceSeqNo(comp.parent().parent().parent().id(), comp
	                    .parent().parent().id(), comp.parent().id(), comp.id(),
	                    seqNo);
	            break;
	        }
        }
    }

    private void split(FilesetComponent src, long freeSizeFirst,
            long freeSizeOther, ArrayList fsList) throws MediaCreationException {
        if (debug) log.debug("split " + src);
        List childs = src.childs();
        while (!childs.isEmpty()) {
            FilesetComponent comp = src
                    .takeChilds(fsList.isEmpty() ? freeSizeFirst
                            : freeSizeOther);
            if (comp.isEmpty()) {
                if (src.level() == FilesetComponent.SERIES)
                        throw new MediaCreationException(
                                ExecutionStatusInfo.INST_OVERSIZED, "Instance "
                                        + ((FilesetComponent) childs.get(0))
                                                .id()
                                        + " does not fit on media");
                break;
            }
            fsList.add(comp);
            if (debug)
                    log.debug(" into " + comp + " on Media #" + fsList.size());
        }
        for (int i = 0, n = childs.size(); i < n; ++i)
            split((FilesetComponent) childs.get(i), freeSizeFirst,
                    freeSizeOther, fsList);
    }

    private String getFilesetIDPrefix() {
        if (rq.getFilesetID().length() == 0) return null;
        String prefix = rq.getFilesetID() + '_';
        int maxSuffixLen = String.valueOf(rq.getVolsetSize()).length();
        return prefix
                .substring(0, Math.min(prefix.length(), 16 - maxSuffixLen));
    }

    private void moveComponents(List comps, File newRootDir) throws IOException {
        for (int i = 0, n = comps.size(); i < n; ++i) {
            FilesetComponent comp = (FilesetComponent) comps.get(i);
            move(comp.getFilePath(), newRootDir);
        }
    }

    private void move(String filepath, File newRootDir) throws IOException {
        File src = new File(rq.getFilesetDir(), filepath);
        if (!src.exists()) return;
        File dest = new File(newRootDir, filepath);
        mkParentDir(dest);
        move(src, dest);
        FileUtils.purgeDir(src.getParentFile(), log);
    }

    private boolean mkParentDir(File dest) throws IOException {
        File parent = dest.getParentFile();
        if (parent.exists()) return false;
        if (!parent.mkdirs())
                throw new IOException("Failed to mkdirs " + parent);
        return true;
    }

    private void copyPatientDirRecords(List pats, DirWriter dirWriter,
            DirReader dirReader) throws IOException {
        for (int i = 0, n = pats.size(); i < n; ++i) {
            FilesetComponent pat = (FilesetComponent) pats.get(i);
            recFilter.clear();
            recFilter.putLO(Tags.PatientID, pat.id());
            DirRecord srcPatRec = dirReader.getFirstRecordBy(null, recFilter,
                    false);
            DirRecord dstPatRec = dirWriter.add(null, srcPatRec.getType(),
                    srcPatRec.getDataset());
            copyStudyDirRecords(pat.childs(), dirWriter, srcPatRec, dstPatRec);
        }
    }

    private void copyStudyDirRecords(List stys, DirWriter dirWriter,
            DirRecord srcPatRec, DirRecord dstPatRec) throws IOException {
        for (int i = 0, n = stys.size(); i < n; ++i) {
            FilesetComponent sty = (FilesetComponent) stys.get(i);
            recFilter.clear();
            recFilter.putUI(Tags.StudyInstanceUID, sty.id());
            DirRecord srcRec = srcPatRec
                    .getFirstChildBy(null, recFilter, false);
            DirRecord dstRec = dirWriter.add(dstPatRec, srcRec.getType(),
                    srcRec.getDataset());
            copySeriesDirRecords(sty.childs(), dirWriter, srcRec, dstRec);
        }
    }

    private void copySeriesDirRecords(List sers, DirWriter dirWriter,
            DirRecord srcStyRec, DirRecord dstStyRec) throws IOException {
        for (int i = 0, n = sers.size(); i < n; ++i) {
            FilesetComponent ser = (FilesetComponent) sers.get(i);
            recFilter.clear();
            recFilter.putUI(Tags.SeriesInstanceUID, ser.id());
            DirRecord srcRec = srcStyRec
                    .getFirstChildBy(null, recFilter, false);
            DirRecord dstRec = dirWriter.add(dstStyRec, srcRec.getType(),
                    srcRec.getDataset());
            copyInstanceDirRecords(ser.childs(), dirWriter, srcRec, dstRec);
        }
    }

    private void copyInstanceDirRecords(List insts, DirWriter dirWriter,
            DirRecord srcSerRec, DirRecord dstSerRec) throws IOException {
        for (int i = 0, n = insts.size(); i < n; ++i) {
            FilesetComponent inst = (FilesetComponent) insts.get(i);
            recFilter.clear();
            recFilter.putUI(Tags.SOPInstanceUID, inst.id());
            DirRecord srcRec = srcSerRec
                    .getFirstChildBy(null, recFilter, false);
            Dataset ds = srcRec.getDataset();
            DirRecord dstRec = dirWriter.add(dstSerRec, srcRec.getType(), ds,
                    ds.getStrings(Tags.RefFileID), ds
                            .getString(Tags.RefSOPClassUIDInFile), ds
                            .getString(Tags.RefSOPInstanceUIDInFile), ds
                            .getString(Tags.RefSOPTransferSyntaxUIDInFile));
        }
    }

}
