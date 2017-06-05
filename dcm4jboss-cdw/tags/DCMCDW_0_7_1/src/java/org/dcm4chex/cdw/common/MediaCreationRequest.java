/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.cdw.common;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import org.jboss.logging.Logger;

/**
 * @author gunter.zeilinter@tiani.com
 * @version $Revision: 4167 $ $Date: 2004-08-16 07:15:37 +0800 (周一, 16 8月 2004) $
 * @since 25.06.2004
 *
 */
public class MediaCreationRequest implements Serializable {

    private final File requestFile;

    private String mediaWriterName;

    private String priority = Priority.LOW;

    private int numberOfCopies = 1;

    private File filesetDir;

    private File isoImageFile;

    private File labelFile;
    
    private String medium;

    private String filesetID = "";

    private String volsetID = "";

    private int volsetSeqno = 1;

    private int volsetSize = 1;

    public MediaCreationRequest(File requestFile) {
        this.requestFile = requestFile;
    }

    public MediaCreationRequest(MediaCreationRequest other) {
        this.requestFile = other.requestFile;
        this.mediaWriterName = other.mediaWriterName;
        this.priority = other.priority;
        this.numberOfCopies = other.numberOfCopies;
        this.filesetDir = other.filesetDir;
        this.isoImageFile = other.isoImageFile;
        this.labelFile = other.labelFile;
        this.medium = other.medium;
        this.filesetID = other.filesetID;
        this.volsetID = other.volsetID;
        this.volsetSeqno = other.volsetSeqno;
        this.volsetSize = other.volsetSize;
    }
    
    public final File getDicomDirFile() {
        if (filesetDir == null)
            throw new IllegalStateException("FilesetDir not yet initialized");
        return new File(filesetDir, "DICOMDIR");
    }

    public final String getMediaWriterName() {
        return mediaWriterName;
    }

    public final void setMediaWriterName(String mediaWriterName) {
        this.mediaWriterName = mediaWriterName;
    }

    public final int getNumberOfCopies() {
        return numberOfCopies;
    }

    public final void setNumberOfCopies(int copies) {
        this.numberOfCopies = copies;
    }

    public final String getMedium() {
        return medium;
    }

    public final void setMedium(String medium) {
        this.medium = medium;
    }

    public final String getPriority() {
        return priority;
    }

    public final void setPriority(String priority) {
        this.priority = priority;
    }

    public final String getFilesetID() {
        return filesetID;
    }

    public final void setFilesetID(String filesetID) {
        this.filesetID = filesetID != null ? filesetID : "";
    }

    public final File getIsoImageFile() {
        return isoImageFile;
    }

    public final void setIsoImageFile(File isoImageFile) {
        this.isoImageFile = isoImageFile;
    }

    public final File getLabelFile() {
        return labelFile;
    }

    public final void setLabelFile(File labelFile) {
        this.labelFile = labelFile;
    }

    public final File getFilesetDir() {
        return filesetDir;
    }

    public final void setFilesetDir(File filesetDir) {
        this.filesetDir = filesetDir;
    }

    public final void setRootDir(File filesetDir) {
        this.filesetDir = filesetDir;
    }

    public final String getVolsetID() {
        return volsetID;
    }

    public final void setVolsetID(String volsetID) {
        this.volsetID = volsetID != null ? volsetID : "";
    }

    public final int getVolsetSeqno() {
        return volsetSeqno;
    }

    public final void setVolsetSeqno(int volsetSeqno) {
        this.volsetSeqno = volsetSeqno;
    }

    public final int getVolsetSize() {
        return volsetSize;
    }

    public final void setVolsetSize(int volsetSize) {
        this.volsetSize = volsetSize;
    }

    public final boolean isCanceled() {
        return !requestFile.exists();
    }

    public String toString() {
        return "MediaCreationRequest[mediaWriter=" + mediaWriterName
        		+ ", rquid=" + requestFile.getName() + ", fsuid="
                + (filesetDir == null ? "" : filesetDir.getName()) + ", fsid="
                + (filesetID == null ? "" : filesetID) + ", seqNo="
                + volsetSeqno + ", tot=" + volsetSize + "]";
    }

    public Dataset readAttributes(Logger log) throws IOException {
        return FileUtils.readDataset(requestFile, log);
    }

    public void updateStatus(String status, String info, Logger log)
            throws IOException {
        Dataset attrs = FileUtils.readDataset(requestFile, log);
        attrs.putCS(Tags.ExecutionStatus, status);
        attrs.putCS(Tags.ExecutionStatusInfo, info);
        writeAttributes(attrs, log);
    }

    public void writeAttributes(Dataset attrs, Logger log) throws IOException {
        FileUtils.writeDataset(attrs, requestFile, log);
    }

    public boolean cleanFiles(Logger log) {
        boolean retval = true;
        if (labelFile != null && labelFile.exists())
            retval = FileUtils.delete(labelFile, log);
        if (isoImageFile != null && isoImageFile.exists())
                retval = FileUtils.delete(isoImageFile, log) && retval;
        if (filesetDir != null && filesetDir.exists())
                retval = FileUtils.delete(filesetDir, log) && retval;
        return retval;
    }
}
