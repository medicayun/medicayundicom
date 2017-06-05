/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.cdw.common;

import java.io.IOException;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.management.ObjectName;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.dict.Tags;
import org.jboss.system.ServiceMBeanSupport;

/**
 * @author gunter.zeilinter@tiani.com
 * @version $Revision: 4185 $ $Date: 2004-09-23 09:30:26 +0800 (周四, 23 9月 2004) $
 * @since 22.06.2004
 *
 */
public abstract class AbstractMediaWriterService extends ServiceMBeanSupport {

    protected SpoolDirDelegate spoolDir = new SpoolDirDelegate(this);

    protected boolean keepSpoolFiles = false;

    private final MessageListener listener = new MessageListener() {

        public void onMessage(Message msg) {
            ObjectMessage objmsg = (ObjectMessage) msg;
            try {
                AbstractMediaWriterService.this
                        .process((MediaCreationRequest) objmsg.getObject());
            } catch (Throwable e) {
                log.error(e.getMessage(), e);
            }
        }

    };

    public final ObjectName getSpoolDirName() {
        return spoolDir.getSpoolDirName();
    }

    public final void setSpoolDirName(ObjectName spoolDirName) {
        spoolDir.setSpoolDirName(spoolDirName);
    }

    public final boolean isKeepSpoolFiles() {
        return keepSpoolFiles;
    }

    public final void setKeepSpoolFiles(boolean keepSpoolFiles) {
        this.keepSpoolFiles = keepSpoolFiles;
    }
    
    private JMSDelegate getJMS() {
        return JMSDelegate.getInstance(serviceName.getKeyProperty("name"));
    }

    protected void startService() throws Exception {
        getJMS().setMessageListener(listener);
    }

    protected void stopService() throws Exception {
        getJMS().setMessageListener(null);
    }

    protected void process(MediaCreationRequest rq) {
        boolean cleanup = true;
        try {
            log.info("Start processing " + rq);
            if (rq.isCanceled()) {
                log.info("" + rq + " was canceled");
                return;
            }
            Dataset attrs = rq.readAttributes(log);
            String status = attrs.getString(Tags.ExecutionStatus);
            if (ExecutionStatus.FAILURE.equals(status)) {
                log.info("" + rq + " already failed");
                return;
            }
            if (!ExecutionStatus.CREATING.equals(status)) {
                attrs.putCS(Tags.ExecutionStatus, ExecutionStatus.CREATING);
                attrs.putCS(Tags.ExecutionStatusInfo,
                        ExecutionStatusInfo.NORMAL);
                rq.writeAttributes(attrs, log);
            }
            try {
                handle(rq, attrs);
                if (rq.getRemainingCopies() == 0) {
                    log.info("Finished processing " + rq);
	                DcmElement sq = attrs.get(Tags.RefStorageMediaSeq);
	                if (sq == null) sq = attrs.putSQ(Tags.RefStorageMediaSeq);
	                Dataset item = sq.addNewItem();
	                item.putSH(Tags.StorageMediaFileSetID, rq.getFilesetID());
	                item.putUI(Tags.StorageMediaFileSetUID, rq.getFilesetDir()
	                        .getName());
	                if (rq.getVolsetSeqno() == rq.getVolsetSize()) {
	                    attrs.putCS(Tags.ExecutionStatus, ExecutionStatus.DONE);
	                    attrs.putCS(Tags.ExecutionStatusInfo,
	                            ExecutionStatusInfo.NORMAL);
	                }
                } else {
                    cleanup = false;
                }
            } catch (MediaCreationException e) {
                log.error("Failed to process " + rq, e);
                attrs.putCS(Tags.ExecutionStatus, ExecutionStatus.FAILURE);
                attrs.putCS(Tags.ExecutionStatusInfo, e.getStatusInfo());
            }
            if (rq.isCanceled()) {
                log.info("" + rq + " was canceled");
                return;
            }
            rq.writeAttributes(attrs, log);
        } catch (IOException e) {
            // error already logged
        } finally {
            if (cleanup && !keepSpoolFiles) rq.cleanFiles(spoolDir);
        }

    }

    protected abstract void handle(MediaCreationRequest r, Dataset attrs)
            throws MediaCreationException, IOException;
}
