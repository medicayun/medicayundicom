/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.cdw.mbean;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.management.ObjectName;

import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import org.dcm4chex.cdw.common.ConfigurationException;
import org.dcm4chex.cdw.common.Executer;
import org.dcm4chex.cdw.common.ExecutionStatus;
import org.dcm4chex.cdw.common.ExecutionStatusInfo;
import org.dcm4chex.cdw.common.FileUtils;
import org.dcm4chex.cdw.common.Flag;
import org.dcm4chex.cdw.common.JMSDelegate;
import org.dcm4chex.cdw.common.MediaCreationException;
import org.dcm4chex.cdw.common.MediaCreationRequest;
import org.dcm4chex.cdw.common.SpoolDirDelegate;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.system.server.ServerConfig;
import org.jboss.system.server.ServerConfigLocator;

/**
 * @author gunter.zeilinter@tiani.com
 * @version $Revision: 4189 $ $Date: 2004-09-26 04:36:31 +0800 (周日, 26 9月 2004) $
 * @since 25.06.2004
 *
 */
public class MediaComposerService extends ServiceMBeanSupport {

    private static final long MIN_MEDIA_CAPACITY = 1000000L;

    private static final int MIN_BUFFER_SIZE = 512;

    private static final long BLOCK_SIZE = 2048L;

    private SpoolDirDelegate spoolDir = new SpoolDirDelegate(this);

    private DirRecordFactory dirRecordFactory = new DirRecordFactory(
            "resource:dicomdir-records.xml");

    private LabelCreator labelCreator = new LabelCreator();

    private final File xmlFile;

    private final File mergeDir;

    private final File mergeDirViewer;

    private final File mergeDirWeb;

    private long mediaCapacity = 700 * FileUtils.MEGA;

    private int bufferSize = 512;

    private String fileSetDescriptorFile;

    private String charsetOfFileSetDescriptorFile = "ISO_IR 100";

    private boolean keepSpoolFiles = false;

    //    private boolean createIcon = true;

    private int iconWidth = 64;

    private int iconHeight = 64;

    private int jpegWidth = 512;

    private int jpegHeight = 512;

    private boolean includeDisplayApplicationOnAllMedia = true;

    private boolean makeIsoImage = true;

    private boolean logXml = false;

    private Set valuesForIncludeWeb = new HashSet();

    private Set valuesForIncludeMd5Sums = new HashSet();

    private Set valuesForCreateIcons = new HashSet();

    private final ImageReader imageReader;

    private final MessageListener listener = new MessageListener() {

        public void onMessage(Message msg) {
            ObjectMessage objmsg = (ObjectMessage) msg;
            try {
                MediaComposerService.this.process((MediaCreationRequest) objmsg
                        .getObject());
            } catch (Throwable e) {
                log.error(e.getMessage(), e);
            }
        }

    };

    public MediaComposerService() {
        ServerConfig config = ServerConfigLocator.locate();
        File homedir = config.getServerHomeDir();
        xmlFile = new File(homedir, "log" + File.separatorChar + "dicomdir.xml");
        File datadir = config.getServerDataDir();
        checkExists(mergeDir = new File(datadir, "mergedir"));
        checkExists(mergeDirViewer = new File(datadir, "mergedir-viewer"));
        checkExists(mergeDirWeb = new File(datadir, "mergedir-web"));
        Iterator it = ImageIO.getImageReadersByFormatName("DICOM");
        if (!it.hasNext())
                throw new ConfigurationException("DICOM Image Reader not found");
        imageReader = (ImageReader) it.next();
    }

    private void checkExists(File file) {
        if (!file.exists())
                throw new ConfigurationException("missing " + file);
    }

    public final boolean isIncludeDisplayApplicationOnAllMedia() {
        return includeDisplayApplicationOnAllMedia;
    }

    public final void setIncludeDisplayApplicationOnAllMedia(
            boolean includeDisplayApplicationOnAllMedia) {
        this.includeDisplayApplicationOnAllMedia = includeDisplayApplicationOnAllMedia;
    }

    public final String[] getValuesForIncludeMd5Sums() {
        return (String[]) valuesForIncludeMd5Sums
                .toArray(new String[valuesForIncludeMd5Sums.size()]);
    }

    public final void setValuesForIncludeMd5Sums(String[] values) {
        valuesForIncludeMd5Sums.clear();
        valuesForIncludeMd5Sums.addAll(Arrays.asList(values));
    }

    public final String[] getValuesForIncludeWeb() {
        return (String[]) valuesForIncludeWeb
                .toArray(new String[valuesForIncludeWeb.size()]);
    }

    public final void setValuesForIncludeWeb(String[] values) {
        valuesForIncludeWeb.clear();
        valuesForIncludeWeb.addAll(Arrays.asList(values));
    }

    public final String[] getValuesForCreateIcons() {
        return (String[]) valuesForCreateIcons
                .toArray(new String[valuesForCreateIcons.size()]);
    }

    public final void setValuesForCreateIcons(String[] values) {
        valuesForCreateIcons.clear();
        valuesForCreateIcons.addAll(Arrays.asList(values));
    }

    final DirRecordFactory getDirRecordFactory() {
        return dirRecordFactory;
    }

    final ImageReader getImageReader() {
        return imageReader;
    }

    public final String getMediaCapacity() {
        return FileUtils.formatSize(mediaCapacity);
    }

    public final void setMediaCapacity(String mediaCapacity) {
        this.mediaCapacity = FileUtils.parseSize(mediaCapacity,
                MIN_MEDIA_CAPACITY);
    }

    public final int getBufferSize() {
        return bufferSize;
    }

    public final void setBufferSize(int bufferSize) {
        if (bufferSize < MIN_BUFFER_SIZE)
                throw new IllegalArgumentException("bufferSize:" + bufferSize);
        this.bufferSize = bufferSize;
    }

    public final boolean isKeepSpoolFiles() {
        return keepSpoolFiles;
    }

    public final void setKeepSpoolFiles(boolean keepSpoolFiles) {
        this.keepSpoolFiles = keepSpoolFiles;
    }

    public final String getCharsetOfFileSetDescriptorFile() {
        return charsetOfFileSetDescriptorFile;
    }

    public final void setCharsetOfFileSetDescriptorFile(
            String charsetOfFileSetDescriptorFile) {
        this.charsetOfFileSetDescriptorFile = charsetOfFileSetDescriptorFile;
    }

    public final String getFileSetDescriptorFile() {
        return fileSetDescriptorFile == null ? "NO" : fileSetDescriptorFile;
    }

    public final void setFileSetDescriptorFile(String fname) {
        if ("NO".equals(fname))
            this.fileSetDescriptorFile = null;
        else {
            checkCS(fname);
            checkExists(new File(mergeDir, fname));
            this.fileSetDescriptorFile = fname;
        }
    }

    private static void checkCS(String s) {
        if (s.length() > 16)
                throw new IllegalArgumentException("Illegal CS:" + s);
        char[] a = s.toCharArray();
        for (int i = 0; i < a.length; i++) {
            char c = a[i];
            if (!((c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == ' ' || c == '_')) { throw new IllegalArgumentException(
                    "Illegal CS:" + s); }
        }
    }

    public final int getIconHeight() {
        return iconHeight;
    }

    public final void setIconHeight(int iconHeight) {
        this.iconHeight = iconHeight;
    }

    public final int getIconWidth() {
        return iconWidth;
    }

    public final void setIconWidth(int iconWidth) {
        this.iconWidth = iconWidth;
    }

    public final int getJpegHeight() {
        return jpegHeight;
    }

    public final void setJpegHeight(int jpegHeight) {
        this.jpegHeight = jpegHeight;
    }

    public final int getJpegWidth() {
        return jpegWidth;
    }

    public final void setJpegWidth(int jpegWidth) {
        this.jpegWidth = jpegWidth;
    }

    public String getLabelFileFormat() {
        return labelCreator.getLabelFileFormat();
    }

    public void setLabelFileFormat(String format) {
        labelCreator.setLabelFileFormat(format);
    }

    public final boolean isMakeIsoImage() {
        return makeIsoImage;
    }

    public final void setMakeIsoImage(boolean makeIsoImage) {
        this.makeIsoImage = makeIsoImage;
    }

    public final boolean isLogXml() {
        return logXml;
    }

    public final void setLogXml(boolean logXml) {
        this.logXml = logXml;
    }

    public final ObjectName getSpoolDirName() {
        return spoolDir.getSpoolDirName();
    }

    public final void setSpoolDirName(ObjectName spoolDirName) {
        spoolDir.setSpoolDirName(spoolDirName);
    }

    final SpoolDirDelegate getSpoolDir() {
        return spoolDir;
    }

    protected void startService() throws Exception {
        log.info("initialize " + DicomDirDOM.class.getName());
        JMSDelegate.getInstance("MediaComposer").setMessageListener(listener);
    }

    protected void stopService() throws Exception {
        JMSDelegate.getInstance("MediaComposer").setMessageListener(null);
    }

    protected void process(MediaCreationRequest rq) {
        boolean cleanup = true;
        Dataset attrs = null;
        try {
            log.info("Composing media for " + rq);
            if (rq.isCanceled()) {
                log.info("" + rq + " was canceled");
                return;
            }
            attrs = rq.readAttributes(log);
            attrs.putCS(Tags.ExecutionStatus, ExecutionStatus.PENDING);
            attrs.putCS(Tags.ExecutionStatusInfo, ExecutionStatusInfo.BUILDING);
            rq.writeAttributes(attrs, log);
            try {
                FilesetBuilder builder = new FilesetBuilder(this, rq, attrs);
                builder.buildFileset();
                DicomDirDOM dom = new DicomDirDOM(this, rq, attrs);
                dom.insertModalitiesInStudy();
                File fsDir = rq.getFilesetDir();
                spoolDir.copy(mergeDir, fsDir, builder.getBuffer());
                if (builder.isViewer()) {
                    spoolDir.copy(mergeDirViewer, fsDir, builder.getBuffer());
                }
                if (builder.isWeb()) {
                    dom.createWeb(rq);
                    spoolDir.copy(mergeDirWeb, fsDir, builder.getBuffer());
                }
                final long fsSize = sizeOf(fsDir);
                if (fsSize > mediaCapacity) {
                    List rqList = splitMedia(rq, attrs, builder, dom, fsSize);
                    if (logXml) dom.toXML(xmlFile);
                    for (int i = 0, n = rqList.size(); i < n; ++i) {
                        MediaCreationRequest rq1 = (MediaCreationRequest) rqList
                                .get(i);
                        File fsDir1 = rq1.getFilesetDir();
                        if (i > 0) {
                            spoolDir
                                    .copy(mergeDir, fsDir1, builder.getBuffer());
                            if (includeDisplayApplicationOnAllMedia
                                    && builder.isViewer()) {
                                spoolDir.copy(mergeDirViewer, fsDir1, builder
                                        .getBuffer());
                            }
                        }
                        finishFileset(builder, dom, rq1);
                        if (!forward(rq1)) return;
                    }
                } else {
                    if (logXml) dom.toXML(xmlFile);
                    finishFileset(builder, dom, rq);
                    if (!forward(rq)) return;
                }
                cleanup = false;
            } catch (MediaCreationException e) {
                if (rq.isCanceled()) {
                    log.info("" + rq + " was canceled");
                    return;
                }
                log.error("Failed to compose media for " + rq, e);
                attrs.putCS(Tags.ExecutionStatus, ExecutionStatus.FAILURE);
                attrs.putCS(Tags.ExecutionStatusInfo, e.getStatusInfo());
                rq.writeAttributes(attrs, log);
            }
        } catch (IOException e) {
            // error already logged
        } finally {
            if (cleanup && !keepSpoolFiles) {
                if (attrs != null) spoolDir.deleteRefInstances(attrs);
                rq.cleanFiles(spoolDir);
            }
        }
    }

    private void finishFileset(FilesetBuilder builder, DicomDirDOM dom, MediaCreationRequest rq) throws MediaCreationException {
        dom.createIndex(rq);
        builder.createMd5Sums(rq);
        if (labelCreator.isActive()) {
            File f = spoolDir.getLabelFile(rq.getFilesetUID(), labelCreator.getLabelFileFormat());
            rq.setLabelFile(f);
            labelCreator.createLabel(rq, dom);
        }
    }

    private List splitMedia(MediaCreationRequest rq, Dataset attrs,
            FilesetBuilder builder, DicomDirDOM dom, final long fsSize)
            throws MediaCreationException, IOException {
        if (!Flag.isYES(attrs.getString(Tags.AllowMediaSplitting))) { throw new MediaCreationException(
                ExecutionStatusInfo.SET_OVERSIZED, "File-set size: "
                        + FileUtils.formatSize(fsSize)
                        + " exceeds Media Capacity: "
                        + FileUtils.formatSize(mediaCapacity)); }
        final long sizeOfNonDicomContent = fsSize
                - builder.sizeOfDicomContent();
        if (sizeOfNonDicomContent > mediaCapacity) { throw new MediaCreationException(
                ExecutionStatusInfo.SET_OVERSIZED,
                "Size of Non-DICOM Content: "
                        + FileUtils.formatSize(sizeOfNonDicomContent)
                        + " exceeds Media Capacity: "
                        + FileUtils.formatSize(mediaCapacity)); }
        long freeSizeFirst = mediaCapacity - sizeOfNonDicomContent;
        long freeSizeOther = mediaCapacity - rq.getDicomDirFile().length();
        if (includeDisplayApplicationOnAllMedia && builder.isViewer())
                freeSizeOther -= sizeOf(mergeDirViewer);
        return builder.splitMedia(freeSizeFirst, freeSizeOther, dom);
    }

    private boolean forward(MediaCreationRequest rq) throws IOException,
            MediaCreationException {
        if (rq.isCanceled()) {
            log.info("" + rq + " was canceled");
            return false;
        }
        Dataset attrs = rq.readAttributes(log);
        String status = attrs.getString(Tags.ExecutionStatus);
        if (ExecutionStatus.FAILURE.equals(status)) {
            log.info("" + rq + " already failed");
            return false;
        }
        if (rq.getVolsetSeqno() == 1) {
            attrs.putCS(Tags.ExecutionStatus, ExecutionStatus.PENDING);
            attrs.putCS(Tags.ExecutionStatusInfo,
                    makeIsoImage ? ExecutionStatusInfo.QUEUED_MKISOFS
                            : ExecutionStatusInfo.QUEUED);
            rq.writeAttributes(attrs, log);
        }
        try {
            log.info("Finished composing media for " + rq);
            JMSDelegate.getInstance(makeIsoImage ? "MakeIsoImage"
                    : "MediaWriter").queue(log, rq, 0L);
        } catch (JMSException e) {
            throw new MediaCreationException(ExecutionStatusInfo.PROC_FAILURE,
                    e);
        }
        return true;
    }

    private long sizeOf(File file) throws MediaCreationException {
        String[] cmdarray = { "mkisofs", "-quiet", "-print-size",
                file.getAbsolutePath()};
        try {
            ByteArrayOutputStream stdout = new ByteArrayOutputStream();
            Executer ex = new Executer(cmdarray, stdout, null);
            int exit = ex.waitFor();
            String result = stdout.toString();
            if (log.isDebugEnabled())
                    log.debug("mkisofs -print-size: " + result);
            return Long.parseLong(result.trim()) * BLOCK_SIZE;
        } catch (Exception e) {
            throw new MediaCreationException(ExecutionStatusInfo.PROC_FAILURE,
                    e);
        }
    }

    final boolean includeWeb(String value) {
        return valuesForIncludeWeb.contains(value);
    }

    final boolean includeMd5Sums(String value) {
        return valuesForIncludeMd5Sums.contains(value);
    }

    final boolean createIcons(String value) {
        return valuesForCreateIcons.contains(value);
    }

    final boolean isArchiveHighWater() {
        return spoolDir.isArchiveHighWater();
    }
}