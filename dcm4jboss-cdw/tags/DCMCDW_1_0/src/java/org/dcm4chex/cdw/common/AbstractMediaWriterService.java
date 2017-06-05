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
import org.dcm4che.dict.Tags;
import org.jboss.system.ServiceMBeanSupport;

/**
 * @author gunter.zeilinter@tiani.com
 * @version $Revision: 4212 $ $Date: 2004-10-31 09:18:08 +0800 (周日, 31 10月 2004) $
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
    
    protected void startService() throws Exception {
        JMSDelegate.startListening(serviceName.getKeyProperty("name"), listener);
    }

    protected void stopService() throws Exception {
        JMSDelegate.stopListening(serviceName.getKeyProperty("name"));
    }

    protected void process(MediaCreationRequest rq) {
        boolean cleanup = true;
        try {
            log.info("Start Creating Media for " + rq);
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
            rq.updateStatus(ExecutionStatus.CREATING, ExecutionStatusInfo.NORMAL, log);
            try {
                cleanup = handle(rq, attrs);
            } catch (MediaCreationException e) {
                log.error("Failed to process " + rq, e);
                rq.updateStatus(ExecutionStatus.FAILURE, e.getStatusInfo(), log);
            }
        } catch (IOException e) {
            // error already logged
        } finally {
            if (cleanup && !keepSpoolFiles) rq.cleanFiles(spoolDir);
        }

    }

    /**
     * @param mcrq Media Creation Request
     * @param attrs Attributes of Media Creation Request
     * @return signals, if File Set and ISO image shall/can be deleted after
     *  this method returns: <code>true</code> = delete, <code>false</code> = keep
     * @throws MediaCreationException if the Media Creation fails.
     * @throws IOException if an i/o error accesing the Media Creation Request occurs.
     */
    protected abstract boolean handle(MediaCreationRequest mcrq, Dataset attrs)
            throws MediaCreationException, IOException;
}
