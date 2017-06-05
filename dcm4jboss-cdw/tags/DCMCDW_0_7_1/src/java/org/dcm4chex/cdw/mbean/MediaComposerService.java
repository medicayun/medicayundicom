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
import java.util.Iterator;
import java.util.List;

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
 * @version $Revision: 4167 $ $Date: 2004-08-16 07:15:37 +0800 (周一, 16 8月 2004) $
 * @since 25.06.2004
 *
 */
public class MediaComposerService extends ServiceMBeanSupport {

    private static final long BLOCK_SIZE = 2048L;

    private static final long MB = 1000000L;

    private static final long GB = 1000000000L;

    private static final long MIN_MEDIA_SIZE = 1000000L;

    private SpoolDirDelegate spoolDir = new SpoolDirDelegate(this);

    private DirRecordFactory dirRecordFactory = new DirRecordFactory(
            "resource:dicomdir-records.xml");

    private LabelCreator labelCreator = new LabelCreator();

    private final File xmlFile;

    private final File mergeDir;

    private final File mergeDirViewer;

    private final File mergeDirWeb;

    private long mediaCapacity = 700 * MB;

    private String fileSetDescriptorFile;

    private String charsetOfFileSetDescriptorFile = "ISO_IR 100";

    private boolean keepSpoolFiles = false;

    private boolean createIcon = true;

    private int iconWidth = 64;

    private int iconHeight = 64;

    private int jpegWidth = 512;

    private int jpegHeight = 512;

    private boolean makeIsoImage = true;
    
    private boolean logXml = false;

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

    final File getMergeDir() {
        return mergeDir;
    }

    final File getMergeDirWeb() {
        return mergeDirWeb;
    }

    final File getMergeDirViewer() {
        return mergeDirViewer;
    }

    final DirRecordFactory getDirRecordFactory() {
        return dirRecordFactory;
    }

    final ImageReader getImageReader() {
        return imageReader;
    }

    static String formatSize(long size) {
        if (size < GB)
            return "" + ((float) size / MB) + "MB";
        else
            return "" + ((float) size / GB) + "GB";
    }

    private long parseSize(String s) {
        long u;
        if (s.endsWith("GB"))
            u = GB;
        else if (s.endsWith("MB"))
            u = MB;
        else
            throw new IllegalArgumentException(s);
        try {
            return (long) (Float.parseFloat(s.substring(0, s.length() - 2)) * u);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(s);
        }
    }

    public final String getMediaCapacity() {
        return formatSize(mediaCapacity);
    }

    public final void setMediaCapacity(String mediaCapacity) {
        setMediaCapacity(parseSize(mediaCapacity));
    }

    private void setMediaCapacity(long mediaCapacity) {
        if (mediaCapacity < MIN_MEDIA_SIZE)
                throw new IllegalArgumentException("mediaCapacity:"
                        + mediaCapacity);
        this.mediaCapacity = mediaCapacity;
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
            if (!((c >= 'A' && c <= 'Z')
                    || (c >= '0' && c <= '9')
                    || c == ' '
                    || c == '_')) {
                        throw new IllegalArgumentException("Illegal CS:" + s);
                    } 
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

    public final boolean isCreateIcon() {
        return createIcon;
    }

    public final void setCreateIcon(boolean includeIcon) {
        this.createIcon = includeIcon;
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

    public String getCreateLabel() {
        return labelCreator.getCreateLabel();
    }

    public void setCreateLabel(String format) {
        labelCreator.setCreateLabel(format);
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
            log.info("Start processing " + rq);
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
                if (builder.isWeb()) dom.createWeb(rq);
                final long fsSize = sizeOf(rq.getFilesetDir());
                if (fsSize > mediaCapacity) {
                    if (!Flag.isYes(attrs.getString(Tags.AllowMediaSplitting))) { 
                        throw new MediaCreationException(
                            ExecutionStatusInfo.SET_OVERSIZED,
                            "File-set size: " + formatSize(fsSize)
                                    + " exceeds Media Capacity: "
                                    + formatSize(mediaCapacity)); }
                    final long sizeOfNonDicomContent = fsSize
                            - builder.sizeOfDicomContent();
                    if (sizeOfNonDicomContent > mediaCapacity) {
                        throw new MediaCreationException(
                            ExecutionStatusInfo.SET_OVERSIZED,
                            "Size of Non-DICOM Content: "
                                    + formatSize(sizeOfNonDicomContent)
                                    + " exceeds Media Capacity: "
                                    + formatSize(mediaCapacity)); }
                    List rqList = builder.splitMedia(mediaCapacity
                            - sizeOfNonDicomContent, mediaCapacity
                            - rq.getDicomDirFile().length(), dom);
                    if (logXml) dom.toXML(xmlFile);
                    for (int i = 0, n = rqList.size(); i < n; ++i) {
                        MediaCreationRequest rq1 =(MediaCreationRequest) rqList.get(i);
                        dom.createIndex(rq1);
                        labelCreator.createLabel(rq1, dom);
                        if (!forward(rq1))
                                return;
                    }
                } else {
                    if (logXml) dom.toXML(xmlFile);
                    dom.createIndex(rq);
                    labelCreator.createLabel(rq, dom);
                    if (!forward(rq)) return;
                }
                cleanup = false;
            } catch (MediaCreationException e) {
                if (rq.isCanceled()) {
                    log.info("" + rq + " was canceled");
                    return;
                }
                log.error("Failed to process " + rq, e);
                attrs.putCS(Tags.ExecutionStatus, ExecutionStatus.FAILURE);
                attrs.putCS(Tags.ExecutionStatusInfo, e.getStatusInfo());
                rq.writeAttributes(attrs, log);
            }
        } catch (IOException e) {
            // error already logged
        } finally {
            if (cleanup && !keepSpoolFiles) {
                if (attrs != null) spoolDir.deleteRefInstances(attrs);
                rq.cleanFiles(log);
            }
        }
    }

    private boolean forward(MediaCreationRequest rq)
            throws IOException, MediaCreationException {
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
            JMSDelegate.getInstance(
                    makeIsoImage ? "MakeIsoImage" : "MediaWriter").queue(log,
                    rq);
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

}
