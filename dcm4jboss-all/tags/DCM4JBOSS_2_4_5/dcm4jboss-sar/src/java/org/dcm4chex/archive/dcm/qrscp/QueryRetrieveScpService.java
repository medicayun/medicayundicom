/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4chex.archive.dcm.qrscp;

import java.io.IOException;
import java.net.Socket;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.management.JMException;
import javax.management.ObjectName;

import org.dcm4che.auditlog.InstancesAction;
import org.dcm4che.auditlog.RemoteNode;
import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Status;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.AcceptorPolicy;
import org.dcm4che.net.DcmServiceException;
import org.dcm4che.net.DcmServiceRegistry;
import org.dcm4che.net.ExtNegotiator;
import org.dcm4cheri.util.StringUtils;
import org.dcm4chex.archive.dcm.AbstractScpService;
import org.dcm4chex.archive.ejb.jdbc.AECmd;
import org.dcm4chex.archive.ejb.jdbc.AEData;
import org.dcm4chex.archive.ejb.jdbc.QueryCmd;
import org.dcm4chex.archive.ejb.jdbc.RetrieveCmd;
import org.dcm4chex.archive.exceptions.UnkownAETException;
import org.dcm4chex.archive.mbean.TLSConfigDelegate;
import org.dcm4chex.archive.util.EJBHomeFactory;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision: 1799 $ $Date: 2005-06-22 20:47:51 +0800 (周三, 22 6月 2005) $
 * @since 31.08.2003
 */
public class QueryRetrieveScpService extends AbstractScpService {

    private static final String NONE = "NONE";
    
    private static String transactionIsolationLevelAsString(int level) {
        switch (level) {
        	case 0:
        	    return "DEFAULT";
        	case Connection.TRANSACTION_READ_UNCOMMITTED:
        	    return "READ_UNCOMMITTED";
        	case Connection.TRANSACTION_READ_COMMITTED:
        	    return "READ_COMMITTED";
        	case Connection.TRANSACTION_REPEATABLE_READ:
        	    return "REPEATABLE_READ";
        	case Connection.TRANSACTION_SERIALIZABLE:
        	    return "SERIALIZABLE";
        }
        throw new IllegalArgumentException("level:" + level);
    }
    
    private static int transactionIsolationLevelOf(String s) {
        String uc = s.trim().toUpperCase();
        if ("READ_UNCOMMITTED".equals(uc))
            return Connection.TRANSACTION_READ_UNCOMMITTED;
        if ("READ_COMMITTED".equals(uc))
            return Connection.TRANSACTION_READ_COMMITTED;
        if ("REPEATABLE_READ".equals(uc))
            return Connection.TRANSACTION_REPEATABLE_READ;
        if ("SERIALIZABLE".equals(uc))
            return Connection.TRANSACTION_SERIALIZABLE;
        return 0;
    }
    
    private String[] sendNoPixelDataToAETs = null;
    
    private String[] ignoreUnsupportedSOPClassFailuresByAETs = null;
    
    private LinkedHashMap requestStgCmtFromAETs = new LinkedHashMap();
    
    private String queryTransactionIsolationLevel;

    private String retrieveTransactionIsolationLevel;
    
    private ObjectName fileSystemMgtName;

    private ObjectName stgCmtScuScpName;
    
    private TLSConfigDelegate tlsConfig = new TLSConfigDelegate(this);
    
    private boolean sendPendingMoveRSP = true;

    private boolean retrieveLastReceived = true;

    private boolean forwardAsMoveOriginator = true;

    private int acTimeout = 5000;

    private int dimseTimeout = 0;

    private int soCloseDelay = 500;

    private int bufferSize = 512;

    private boolean patientRootFind;

    private boolean studyRootFind;

    private boolean patientStudyOnlyFind;

    private boolean tianiPatientRootFind;

    private boolean tianiStudyRootFind;

    private boolean tianiPatientStudyOnlyFind;

    private boolean patientRootMove;

    private boolean studyRootMove;

    private boolean patientStudyOnlyMove;

    private FindScp dicomFindScp = new FindScp(this, true);

    private FindScp tianiFindScp = new FindScp(this, false);
    
    private MoveScp moveScp = new MoveScp(this);
    
    private int maxUIDsPerMoveRQ = 100;
    
    public String getEjbProviderURL() {
        return EJBHomeFactory.getEjbProviderURL();
    }
    
    public void setEjbProviderURL(String ejbProviderURL) {
        EJBHomeFactory.setEjbProviderURL(ejbProviderURL);
    }
    
    public final ObjectName getTLSConfigName() {
        return tlsConfig.getTLSConfigName();
    }

    public final void setTLSConfigName(ObjectName tlsConfigName) {
        tlsConfig.setTLSConfigName(tlsConfigName);
    }

    public final ObjectName getStgCmtScuScpName() {
        return stgCmtScuScpName;
    }
    
    public final void setStgCmtScuScpName(ObjectName stgCmtScuScpName) {
        this.stgCmtScuScpName = stgCmtScuScpName;
    }
    
    public final String getQueryTransactionIsolationLevel() {
        return transactionIsolationLevelAsString(QueryCmd.transactionIsolationLevel);
    }
    
    public final void setQueryTransactionIsolationLevel(String level) {
        QueryCmd.transactionIsolationLevel = transactionIsolationLevelOf(level);
    }
    
    public final String getRetrieveTransactionIsolationLevel() {
        return transactionIsolationLevelAsString(RetrieveCmd.transactionIsolationLevel);
    }
    
    public final void setRetrieveTransactionIsolationLevel(String level) {
        RetrieveCmd.transactionIsolationLevel = transactionIsolationLevelOf(level);
    }

    public final ObjectName getFileSystemMgtName() {
        return fileSystemMgtName;
    }

    public final void setFileSystemMgtName(ObjectName fileSystemMgtName) {
        this.fileSystemMgtName = fileSystemMgtName;
    }

    public final boolean isAcceptPatientRootFind() {
        return patientRootFind;
    }

    public final void setAcceptPatientRootFind(boolean enable) {
        this.patientRootFind = enable;
        enableService();
    }

    public final boolean isAcceptTianiPatientRootFind() {
        return tianiPatientRootFind;
    }

    public final void setAcceptTianiPatientRootFind(boolean enable) {
        this.tianiPatientRootFind = enable;
        enableService();
    }

    public final boolean isAcceptPatientRootMove() {
        return patientRootMove;
    }

    public final void setAcceptPatientRootMove(boolean enable) {
        this.patientRootMove = enable;
        enableService();
    }

    public final boolean isAcceptPatientStudyOnlyFind() {
        return patientStudyOnlyFind;
    }

    public final void setAcceptPatientStudyOnlyFind(boolean enable) {
        this.patientStudyOnlyFind = enable;
        enableService();
    }

    public final boolean isAcceptTianiPatientStudyOnlyFind() {
        return tianiPatientStudyOnlyFind;
    }

    public final void setAcceptTianiPatientStudyOnlyFind(boolean enable) {
        this.tianiPatientStudyOnlyFind = enable;
        enableService();
    }

    public final boolean isAcceptPatientStudyOnlyMove() {
        return patientStudyOnlyMove;
    }

    public final void setAcceptPatientStudyOnlyMove(boolean patientStudyOnlyMove) {
        this.patientStudyOnlyMove = patientStudyOnlyMove;
        enableService();
    }

    public final boolean isAcceptStudyRootFind() {
        return studyRootFind;
    }

    public final void setAcceptStudyRootFind(boolean enable) {
        this.studyRootFind = enable;
        enableService();
    }

    public final boolean isAcceptTianiStudyRootFind() {
        return tianiStudyRootFind;
    }

    public final void setAcceptTianiStudyRootFind(boolean enable) {
        this.tianiStudyRootFind = enable;
        enableService();
    }

    public final boolean isAcceptStudyRootMove() {
        return studyRootMove;
    }

    public final void setAcceptStudyRootMove(boolean studyRootMove) {
        this.studyRootMove = studyRootMove;
        enableService();
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

    public final boolean isSendPendingMoveRSP() {
        return sendPendingMoveRSP;
    }

    public final void setSendPendingMoveRSP(boolean sendPendingMoveRSP) {
        this.sendPendingMoveRSP = sendPendingMoveRSP;
    }

    public final boolean isRetrieveLastReceived() {
        return retrieveLastReceived;
    }

    public final void setRetrieveLastReceived(boolean retrieveLastReceived) {
        this.retrieveLastReceived = retrieveLastReceived;
    }

    public final boolean isForwardAsMoveOriginator() {
        return forwardAsMoveOriginator;
    }

    public final void setForwardAsMoveOriginator(boolean forwardAsMoveOriginator) {
        this.forwardAsMoveOriginator = forwardAsMoveOriginator;
    }
    
    public final String getSendNoPixelDataToAETs() {
        return sendNoPixelDataToAETs == null ? NONE
                : StringUtils.toString(callingAETs, '\\');
    }

    public final void setSendNoPixelDataToAETs(String aets) {
        this.sendNoPixelDataToAETs = NONE.equalsIgnoreCase(aets) ? null 
                : StringUtils.split(aets, '\\');
    }
    
    public final String getIgnoreUnsupportedSOPClassFailuresByAETs() {
        return ignoreUnsupportedSOPClassFailuresByAETs == null ? NONE
            : StringUtils.toString(ignoreUnsupportedSOPClassFailuresByAETs, '\\');
    }
    
    public final void setIgnoreUnsupportedSOPClassFailuresByAETs(String aets) {
        this.ignoreUnsupportedSOPClassFailuresByAETs = NONE.equalsIgnoreCase(aets) ? null 
                : StringUtils.split(aets, '\\');
    }
    
    public final String getRequestStgCmtFromAETs() {
        if (requestStgCmtFromAETs.isEmpty()) return NONE;        
        StringBuffer sb = new StringBuffer();
        Iterator it = requestStgCmtFromAETs.entrySet().iterator();
        while (it.hasNext()) {
            final Map.Entry entry = (Entry) it.next();
            final String key = (String) entry.getKey();
            final String value = (String) entry.getValue();
            sb.append(key);
            if (!key.equals(value))
                sb.append(':').append(value);
            sb.append('\\');
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    public final void setRequestStgCmtFromAETs(String aets) {
        requestStgCmtFromAETs.clear();
        if (aets != null && aets.length() > 0 
                && !aets.equalsIgnoreCase(NONE)) {
            String[] a = StringUtils.split(aets, '\\');
            String s;
            int c;
            for (int i = 0; i < a.length; i++) {
                s = a[i];
                c = s.indexOf(':');
                if (c == -1)
                    requestStgCmtFromAETs.put(s, s);
                else if (c > 0 && c < s.length() -1)
                    requestStgCmtFromAETs.put(s.substring(0, c), s.substring(c+1));
            }
        }
    }
    
    public final int getBufferSize() {
        return bufferSize;
    }

    public final void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

	public final int getMaxUIDsPerMoveRQ() {
		return maxUIDsPerMoveRQ;
	}
	
	public final void setMaxUIDsPerMoveRQ(int max) {
		this.maxUIDsPerMoveRQ = max;
	}
	
	protected void bindDcmServices(DcmServiceRegistry services) {
        services.bind(UIDs.PatientRootQueryRetrieveInformationModelFIND,
                dicomFindScp);
        services.bind(UIDs.StudyRootQueryRetrieveInformationModelFIND,
                dicomFindScp);
        services.bind(UIDs.PatientStudyOnlyQueryRetrieveInformationModelFIND,
                dicomFindScp);

        services.bind(UIDs.TianiPatientRootQueryRetrieveInformationModelFIND,
                tianiFindScp);
        services.bind(UIDs.TianiStudyRootQueryRetrieveInformationModelFIND,
                tianiFindScp);
        services.bind(UIDs.TianiPatientStudyOnlyQueryRetrieveInformationModelFIND,
                tianiFindScp);
        
        services.bind(UIDs.PatientRootQueryRetrieveInformationModelMOVE,
                moveScp);
        services.bind(UIDs.StudyRootQueryRetrieveInformationModelMOVE, moveScp);
        services.bind(UIDs.PatientStudyOnlyQueryRetrieveInformationModelMOVE,
                moveScp);
    }

    protected void unbindDcmServices(DcmServiceRegistry services) {
        services.unbind(UIDs.PatientRootQueryRetrieveInformationModelFIND);
        services.unbind(UIDs.StudyRootQueryRetrieveInformationModelFIND);
        services.unbind(UIDs.PatientStudyOnlyQueryRetrieveInformationModelFIND);

        services.unbind(UIDs.TianiPatientRootQueryRetrieveInformationModelFIND);
        services.unbind(UIDs.TianiStudyRootQueryRetrieveInformationModelFIND);
        services.unbind(UIDs.TianiPatientStudyOnlyQueryRetrieveInformationModelFIND);

        services.unbind(UIDs.PatientRootQueryRetrieveInformationModelFIND);
        services.unbind(UIDs.StudyRootQueryRetrieveInformationModelFIND);
        services.unbind(UIDs.PatientStudyOnlyQueryRetrieveInformationModelFIND);
    }

    private static final ExtNegotiator ECHO_EXT_NEG = new ExtNegotiator() {
        public byte[] negotiate(byte[] offered) {
            return offered;
        }
    };

    protected void updatePC(AcceptorPolicy policy, String cuid, boolean enable) {
        policy.putPresContext(cuid, enable ? getTransferSyntaxUIDs() : null);
        policy.putExtNegPolicy(cuid, enable ? ECHO_EXT_NEG : null);
    }
    
    protected void updatePresContexts(AcceptorPolicy policy, boolean enable) {
        updatePC(policy, UIDs.PatientRootQueryRetrieveInformationModelFIND,
                enable && patientRootFind);
        updatePC(policy, UIDs.StudyRootQueryRetrieveInformationModelFIND,
                enable && studyRootFind);
        updatePC(policy, UIDs.PatientStudyOnlyQueryRetrieveInformationModelFIND,
                enable && patientStudyOnlyFind);
        updatePC(policy, UIDs.TianiPatientRootQueryRetrieveInformationModelFIND,
                enable && tianiPatientRootFind);
        updatePC(policy, UIDs.TianiStudyRootQueryRetrieveInformationModelFIND,
                enable && tianiStudyRootFind);
        updatePC(policy, UIDs.TianiPatientStudyOnlyQueryRetrieveInformationModelFIND,
                enable && tianiPatientStudyOnlyFind);
        updatePC(policy, UIDs.PatientRootQueryRetrieveInformationModelMOVE,
                enable && patientRootMove);
        updatePC(policy, UIDs.StudyRootQueryRetrieveInformationModelMOVE,
                enable && studyRootMove);
        updatePC(policy, UIDs.PatientStudyOnlyQueryRetrieveInformationModelMOVE,
                enable && patientStudyOnlyMove);
    }

    public AEData queryAEData(String aet) throws SQLException,
            UnkownAETException {
        AEData aeData = new AECmd(aet).getAEData();
        if (aeData == null) { throw new UnkownAETException(aet); }
        return aeData;
    }

    boolean isLocalFileSystem(String dirpath) throws DcmServiceException {
        try {
            Boolean b = (Boolean) server.invoke(fileSystemMgtName,
                    "isLocalFileSystem",
                    new Object[] { dirpath},
                    new String[] { String.class.getName()});
            return b.booleanValue();
        } catch (Exception e) {
            throw new DcmServiceException(Status.ProcessingFailure, e);
        }
    }

    boolean isWithoutPixelData(String moveDest) {
        return sendNoPixelDataToAETs != null
            && Arrays.asList(sendNoPixelDataToAETs).contains(moveDest);
    }
    
    boolean isIgnoreUnsupportedSOPClassFailures(String moveDest) {
        return ignoreUnsupportedSOPClassFailuresByAETs != null
            && Arrays.asList(ignoreUnsupportedSOPClassFailuresByAETs).contains(moveDest);
    }
    
    String getStgCmtAET(String moveDest) {
        return (String) requestStgCmtFromAETs.get(moveDest);
    }    

    void logInstancesSent(RemoteNode node, InstancesAction action) {
        if (auditLogName == null) return;
        try {
            server.invoke(auditLogName,
                    "logInstancesSent",
                    new Object[] { node, action},
                    new String[] { RemoteNode.class.getName(),
                    	InstancesAction.class.getName()});
        } catch (Exception e) {
            log.warn("Audit Log failed:", e);
        }
    }

    void logDicomQuery(Dataset keys, RemoteNode node, String cuid) {
        if (auditLogName == null) return;
        try {
            server.invoke(auditLogName,
                    "logDicomQuery",
                    new Object[] { keys, node, cuid},
                    new String[] { Dataset.class.getName(), 
                    	RemoteNode.class.getName(), String.class.getName()});
        } catch (Exception e) {
            log.warn("Audit Log failed:", e);
        }
    }

    Socket createSocket(AEData aeData) throws IOException {
        return tlsConfig.createSocket(aeData);
    }

    public void queueStgCmtOrder(String calling, String called,
            Dataset actionInfo) {
        try {
            server.invoke(stgCmtScuScpName, "queueStgCmtOrder", new Object[] {
                    calling, called, actionInfo, Boolean.FALSE }, new String[] {
                    String.class.getName(), String.class.getName(),
                    Dataset.class.getName(), boolean.class.getName() });
        } catch (JMException e) {
            log.error("Failed to queue Storage C0mmitment Request", e);
        }
    }
}
