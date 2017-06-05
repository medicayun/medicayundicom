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
 * @version $Revision: 4135 $ $Date: 2004-06-30 23:08:06 +0800 (周三, 30 6月 2004) $
 * @since 25.06.2004
 *
 */
public class MediaCreationRequest implements Serializable {

    private final File requestFile;

    private String priority = Priority.LOW;

    private int numberOfCopies = 1;

    private File filesetDir;

    private File isoImageFile;
    
    private String medium;

    private String filesetID;

    private String volsetID;

    private int volsetSeqno = 1;

    private int volsetSize = 1;

    public MediaCreationRequest(File requestFile) {
        this.requestFile = requestFile;
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
        this.filesetID = filesetID;
    }

    public final File getIsoImageFile() {
        return isoImageFile;
    }

    public final void setIsoImageFile(File isoImageFile) {
        this.isoImageFile = isoImageFile;
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
        this.volsetID = volsetID;
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
        return "MediaCreationRequest[rquid=" + requestFile.getName()
                + ", fsuid=" + (filesetDir == null ? "" : filesetDir.getName())
                + ", fsid=" + (filesetID == null ? "" : filesetID) + ", seqNo="
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
        if (isoImageFile != null && isoImageFile.exists())
                retval = FileUtils.delete(isoImageFile, log);
        if (filesetDir != null && filesetDir.exists())
                retval = FileUtils.delete(filesetDir, log) && retval;
        return retval;
    }
}
