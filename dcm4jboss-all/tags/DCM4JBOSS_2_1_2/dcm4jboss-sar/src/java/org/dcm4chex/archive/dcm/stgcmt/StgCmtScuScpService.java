/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.dcm.stgcmt;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.management.JMException;
import javax.management.ObjectName;

import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmParser;
import org.dcm4che.data.FileFormat;
import org.dcm4che.dict.Status;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.AAssociateAC;
import org.dcm4che.net.AAssociateRQ;
import org.dcm4che.net.AcceptorPolicy;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.Association;
import org.dcm4che.net.DcmServiceException;
import org.dcm4che.net.DcmServiceRegistry;
import org.dcm4che.net.Dimse;
import org.dcm4che.net.FutureRSP;
import org.dcm4che.net.PDU;
import org.dcm4che.net.PresContext;
import org.dcm4che.net.RoleSelection;
import org.dcm4che.util.UIDGenerator;
import org.dcm4chex.archive.config.RetryIntervalls;
import org.dcm4chex.archive.dcm.AbstractScpService;
import org.dcm4chex.archive.ejb.interfaces.Storage;
import org.dcm4chex.archive.ejb.interfaces.StorageHome;
import org.dcm4chex.archive.ejb.jdbc.AECmd;
import org.dcm4chex.archive.ejb.jdbc.AEData;
import org.dcm4chex.archive.ejb.jdbc.FileInfo;
import org.dcm4chex.archive.ejb.jdbc.RetrieveCmd;
import org.dcm4chex.archive.exceptions.UnkownAETException;
import org.dcm4chex.archive.mbean.TLSConfigDelegate;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.dcm4chex.archive.util.FileUtils;
import org.dcm4chex.archive.util.JMSDelegate;

/**
 * @author <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 * @version $Revision: 1492 $ $Date: 2005-02-07 09:02:03 +0800 (周一, 07 2月 2005) $
 * @since Jan 5, 2005
 */
public class StgCmtScuScpService extends AbstractScpService implements
        MessageListener {

    private static final int INVOKE_FAILED_STATUS = -1;

    private ObjectName fileSystemMgtName;
    
    private String queueName = "StgCmtScuScp";

    private TLSConfigDelegate tlsConfig = new TLSConfigDelegate(this);

    private int acTimeout = 5000;

    private int dimseTimeout = 0;

    private int soCloseDelay = 500;

    private RetryIntervalls scuRetryIntervalls = new RetryIntervalls();

    private RetryIntervalls scpRetryIntervalls = new RetryIntervalls();

    private StgCmtScuScp stgCmtScuScp = new StgCmtScuScp(this);

    private long receiveResultInSameAssocTimeout;

    public final String getScuRetryIntervalls() {
        return scuRetryIntervalls.toString();
    }

    public final void setScuRetryIntervalls(String s) {
        this.scuRetryIntervalls = new RetryIntervalls(s);
    }

    public final String getScpRetryIntervalls() {
        return scpRetryIntervalls.toString();
    }

    public final void setScpRetryIntervalls(String s) {
        this.scpRetryIntervalls = new RetryIntervalls(s);
    }

    public final ObjectName getTLSConfigName() {
        return tlsConfig.getTLSConfigName();
    }

    public final void setTLSConfigName(ObjectName tlsConfigName) {
        tlsConfig.setTLSConfigName(tlsConfigName);
    }

    public final String getQueueName() {
        return queueName;
    }
    
    public final void setQueueName(String queueName) {
        this.queueName = queueName;
    }
    
    public String getEjbProviderURL() {
        return EJBHomeFactory.getEjbProviderURL();
    }

    public final ObjectName getFileSystemMgtName() {
        return fileSystemMgtName;
    }

    public final void setFileSystemMgtName(ObjectName fileSystemMgtName) {
        this.fileSystemMgtName = fileSystemMgtName;
    }

    public void setEjbProviderURL(String ejbProviderURL) {
        EJBHomeFactory.setEjbProviderURL(ejbProviderURL);
    }

    public final int getAcTimeout() {
        return acTimeout;
    }

    public final void setAcTimeout(int acTimeout) {
        this.acTimeout = acTimeout;
    }

    public final int getDimseTimeout() {
        return dimseTimeout;
    }

    public final void setDimseTimeout(int dimseTimeout) {
        this.dimseTimeout = dimseTimeout;
    }

    public final int getSoCloseDelay() {
        return soCloseDelay;
    }

    public final void setSoCloseDelay(int soCloseDelay) {
        this.soCloseDelay = soCloseDelay;
    }

    public final long getReceiveResultInSameAssocTimeout() {
        return receiveResultInSameAssocTimeout;
    }
    
    public final void setReceiveResultInSameAssocTimeout(long timeout) {
        if (timeout < 0)
            throw new IllegalArgumentException("timeout: " + timeout);
        this.receiveResultInSameAssocTimeout = timeout;
    }
    
    protected void bindDcmServices(DcmServiceRegistry services) {
        services.bind(UIDs.StorageCommitmentPushModel, stgCmtScuScp);
    }

    protected void unbindDcmServices(DcmServiceRegistry services) {
        services.unbind(UIDs.StorageCommitmentPushModel);
    }

    protected void updatePresContexts(AcceptorPolicy policy, boolean enable) {
        if (enable) {
            policy.putPresContext(UIDs.StorageCommitmentPushModel,
                    getTransferSyntaxUIDs());
            policy.putRoleSelection(UIDs.StorageCommitmentPushModel, true, true);
        } else {
            policy.putPresContext(UIDs.StorageCommitmentPushModel, null);
            policy.removeRoleSelection(UIDs.StorageCommitmentPushModel);            
        }
    }

    Socket createSocket(AEData aeData) throws IOException {
        return tlsConfig.createSocket(aeData);
    }

    boolean isLocalFileSystem(String dirpath) {
        try {
            Boolean b = (Boolean) server.invoke(fileSystemMgtName,
                    "isLocalFileSystem", new Object[] { dirpath },
                    new String[] { String.class.getName() });
            return b.booleanValue();
        } catch (JMException e) {
            throw new RuntimeException("Failed to invoke isLocalFileSystem", e);
        }
    }

    public void queueStgCmtOrder(String calling, String called,
            Dataset actionInfo, boolean scpRole) throws JMSException {
        StgCmtOrder order = new StgCmtOrder(calling, called, actionInfo, scpRole);
        JMSDelegate.queue(queueName, order, 0, 0);        
    }
    
    protected void startService() throws Exception {
        super.startService();
        JMSDelegate.startListening(queueName, this);
    }

    protected void stopService() throws Exception {
        JMSDelegate.stopListening(queueName);
        super.stopService();
    }

    public void onMessage(Message message) {
        ObjectMessage om = (ObjectMessage) message;
        try {
            StgCmtOrder order = (StgCmtOrder) om.getObject();
            log.info("Start processing " + order);
            final int status = order.isScpRole() ? process(order)
                    : invoke(order);
            if (status == 0) {
                log.info("Finished processing " + order);
                return;
            }
            order.setFailureStatus(status);
            final int failureCount = order.getFailureCount() + 1;
            order.setFailureCount(failureCount);
            final RetryIntervalls retryIntervalls = order.isScpRole() 
                    ? scpRetryIntervalls : scuRetryIntervalls;
            final long delay = retryIntervalls.getIntervall(failureCount);
            if (delay == -1L) {
                log.error("Give up to process " + order);
            } else {
                log.warn("Failed to process " + order + ". Scheduling retry.");
                JMSDelegate.queue(queueName, order, 0,
                        System.currentTimeMillis() + delay);
            }
        } catch (JMSException e) {
            log.error("jms error during processing message: " + message, e);
        } catch (Throwable e) {
            log.error("unexpected error during processing message: " + message,
                    e);
        }

    }

    private int invoke(StgCmtOrder order) {
        final String calledAET = order.getCalledAET();
        final String callingAET = order.getCallingAET();
        final Dataset actionInfo = order.getActionInfo();
        actionInfo.putUI(Tags.TransactionUID,
                UIDGenerator.getInstance().createUID());
        try {
            ActiveAssociation aa = openAssociation(calledAET, callingAET, false);
            try {
                Command cmd = dof.newCommand();
                cmd.initNActionRQ(1, UIDs.StorageCommitmentPushModel,
                        UIDs.StorageCommitmentPushModelSOPInstance, 1);
                Dimse rq = asf.newDimse(1, cmd, actionInfo);
                logDataset("Storage Commitment Request:\n", actionInfo);
                FutureRSP rsp = aa.invoke(rq);
                Command rspCmd = rsp.get().getCommand();
                final int status = rspCmd.getStatus();
                if (status != Status.Success) {
                    log.warn("" + calledAET
                            + " returns N-ACTION-RQ with error status: "
                            + rspCmd);
                } else {
                    Thread.sleep(receiveResultInSameAssocTimeout);                  
                }
                return status;
            } finally {
                try {
                    aa.release(false);
                } catch (Exception e) {
                    log.warn(
                            "release association to " + calledAET + " failed:",
                            e);
                }
            }
        } catch (Exception e) {
            log.error("sending storage commitment request to " + calledAET
                    + " failed:", e);
            return INVOKE_FAILED_STATUS;
        }
    }

    private int process(StgCmtOrder order) {
        int failureReason = Status.ProcessingFailure;
        Storage storage = null;
        try {
            StorageHome home = (StorageHome) EJBHomeFactory.getFactory()
                    .lookup(StorageHome.class, StorageHome.JNDI_NAME);
            storage = home.create();
        } catch (Exception e) {
            log.error("Failed to access Storage EJB", e);
        }
        Dataset actionInfo = order.getActionInfo();
        DcmElement refSOPSeq = actionInfo.get(Tags.RefSOPSeq);
        Map fileInfos = null;
        if (storage != null) {
            try {
                FileInfo[][] aa = RetrieveCmd.create(refSOPSeq).execute();
                fileInfos = new HashMap();
                for (int i = 0; i < aa.length; i++) {
                    fileInfos.put(aa[i][0].sopIUID, aa[i]);
                }
            } catch (SQLException e) {
                log.error("Failed to query DB", e);
            }
        }
        Dataset eventInfo = dof.newDataset();
        eventInfo.putUI(Tags.TransactionUID, actionInfo
                .getString(Tags.TransactionUID));
        DcmElement successSOPSeq = eventInfo.putSQ(Tags.RefSOPSeq);
        DcmElement failedSOPSeq = eventInfo.putSQ(Tags.FailedSOPSeq);
        for (int i = 0, n = refSOPSeq.vm(); i < n; ++i) {
            Dataset refSOP = refSOPSeq.getItem(i);
            if (storage != null && fileInfos != null
                    && (failureReason = commit(storage, refSOP, fileInfos)) == Status.Success) {
                successSOPSeq.addItem(refSOP);
            } else {
                refSOP.putUS(Tags.FailureReason, failureReason);
                failedSOPSeq.addItem(refSOP);
            }
        }
        if (failedSOPSeq.isEmpty()) {
            eventInfo.remove(Tags.FailedSOPSeq);
        }
        return sendResult(order.getCalledAET(), order.getCallingAET(),
                eventInfo);
    }

    private int commit(Storage storage, Dataset refSOP, Map fileInfos) {
        final String iuid = refSOP.getString(Tags.RefSOPInstanceUID);
        final String cuid = refSOP.getString(Tags.RefSOPClassUID);
        FileInfo[] fileInfo = (FileInfo[]) fileInfos.get(iuid);
        if (fileInfo == null) {
            log.warn("Failed Storage Commitment of Instance[uid=" + iuid
                    + "]: no such object");
            return Status.NoSuchObjectInstance;
        }
        if (!fileInfo[0].sopCUID.equals(cuid)) {
            log.warn("Failed Storage Commitment of Instance[uid=" + iuid
                    + "]: SOP Class in request[" + cuid
                    + "] does not match SOP Class in stored object["
                    + fileInfo[0].sopCUID + "]");
            return Status.ClassInstanceConflict;
        }
        try {
            LinkedHashSet retrieveAETs = new LinkedHashSet();
            for (int i = 0; i < fileInfo.length; i++) {
                retrieveAETs.add(fileInfo[i].fileRetrieveAET);
                checkFile(fileInfo[i]);
            }
            storage.commit(iuid);
            retrieveAETs.add(fileInfo[0].extRetrieveAET);
            retrieveAETs.remove(null);
            if (!retrieveAETs.isEmpty())
                refSOP.putAE(Tags.RetrieveAET, (String[]) retrieveAETs
                        .toArray(new String[retrieveAETs.size()]));
            return Status.Success;
        } catch (Exception e) {
            log.error("Failed Storage Commitment of Instance[uid="
                    + fileInfo[0].sopIUID + "]:", e);
            return Status.ProcessingFailure;
        }
    }

    private void checkFile(FileInfo info) throws IOException {
        if (info.basedir == null || !isLocalFileSystem(info.basedir))
            return;
        File file = FileUtils.toFile(info.basedir, info.fileID);
        log.info("M-READ file:" + file);
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(
                file));
        DigestInputStream dis = new DigestInputStream(in, md);
        try {
            DcmParser parser = paf.newDcmParser(dis);
            parser.parseDcmFile(FileFormat.DICOM_FILE, Tags.PixelData);
            if (parser.getReadTag() == Tags.PixelData) {
                if (parser.getReadLength() == -1) {
                    while (parser.parseHeader() == Tags.Item) {
                        readOut(parser.getInputStream(), parser.getReadLength());
                    }
                }
                readOut(parser.getInputStream(), parser.getReadLength());
                parser.parseDataset(parser.getDcmDecodeParam(), -1);
            }
        } finally {
            try {
                dis.close();
            } catch (IOException ignore) {
            }
        }
        byte[] md5 = md.digest();
        if (!Arrays.equals(md5, info.getFileMd5())) {
            throw new IOException("MD5 mismatch");
        }
    }

    private void readOut(InputStream in, int len) throws IOException {
        int toRead = len;
        while (toRead-- > 0) {
            if (in.read() < 0) {
                throw new EOFException();
            }
        }
    }

    private ActiveAssociation openAssociation(String calledAET,
            String callingAET, boolean scp) throws IOException, SQLException,
            UnkownAETException {
        AEData ae = new AECmd(calledAET).execute();
        if (ae == null) {
            throw new UnkownAETException(calledAET);
        }
        Association a = asf.newRequestor(createSocket(ae));
        a.setAcTimeout(acTimeout);
        a.setDimseTimeout(dimseTimeout);
        a.setSoCloseDelay(soCloseDelay);
        AAssociateRQ rq = asf.newAAssociateRQ();
        rq.setCalledAET(calledAET);
        rq.setCallingAET(callingAET);
        rq.addPresContext(asf.newPresContext(1,
                UIDs.StorageCommitmentPushModel, NATIVE_LE_TS));
        if (scp)
            rq.addRoleSelection(asf.newRoleSelection(
                    UIDs.StorageCommitmentPushModel, false, true));
        PDU pdu = a.connect(rq);
        if (!(pdu instanceof AAssociateAC)) {
            throw new IOException("Association not accepted by " + calledAET);
        }
        ActiveAssociation aa = asf.newActiveAssociation(a, super.dcmHandler.getDcmServiceRegistry());
        aa.start();
        AAssociateAC ac = (AAssociateAC) pdu;
        if (ac.getPresContext(1).result() == PresContext.ACCEPTANCE) {
            if (!scp)
                return aa;
            RoleSelection rs = ac
                    .getRoleSelection(UIDs.StorageCommitmentPushModel);
            if (rs != null && rs.scp())
                return aa;
        }
        try {
            aa.release(false);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        throw new IOException("Storage Commitment Service rejected by "
                + calledAET);
    }

    private int sendResult(String calledAET, String callingAET,
            Dataset eventInfo) {
        try {
            ActiveAssociation aa = openAssociation(calledAET, callingAET, true);
            try {
                Command cmd = dof.newCommand();
                cmd.initNEventReportRQ(1, UIDs.StorageCommitmentPushModel,
                        UIDs.StorageCommitmentPushModelSOPInstance, eventInfo
                                .contains(Tags.FailedSOPSeq) ? 2 : 1);
                Dimse rq = asf.newDimse(1, cmd, eventInfo);
                logDataset("Storage Commitment Result:\n", eventInfo);
                FutureRSP rsp = aa.invoke(rq);
                Command rspCmd = rsp.get().getCommand();
                final int status = rspCmd.getStatus();
                if (status != Status.Success) {
                    log.warn("" + calledAET
                            + " returns N-EVENT-REPORT with error status:\n"
                            + rspCmd);
                }
                return status;
            } finally {
                try {
                    aa.release(false);
                } catch (Exception e) {
                    log.warn(
                            "release association to " + calledAET + " failed:",
                            e);
                }
            }
        } catch (Exception e) {
            log.error("sending storage commitment result to " + calledAET
                    + " failed:", e);
            return INVOKE_FAILED_STATUS;
        }
    }

    void commited(Dataset stgcmtResult) throws DcmServiceException {
        Storage storage = null;
        try {
            StorageHome home = (StorageHome) EJBHomeFactory.getFactory()
                    .lookup(StorageHome.class, StorageHome.JNDI_NAME);
            storage = home.create();
            storage.commited(stgcmtResult);
        } catch (Exception e) {
            log.error("Failed update External AETs in DB records", e);
            throw new DcmServiceException(Status.ProcessingFailure, e);
        } finally {
            if (storage != null)
                try {
                    storage.remove();
                } catch (Exception ignore) {}
        }
    }
}
