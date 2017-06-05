/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4chex.archive.dcm.qrscp;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.management.ObjectName;

import org.dcm4che.dict.Status;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.AcceptorPolicy;
import org.dcm4che.net.DcmServiceException;
import org.dcm4che.net.DcmServiceRegistry;
import org.dcm4che.net.ExtNegotiator;
import org.dcm4chex.archive.dcm.AbstractScpService;
import org.dcm4chex.archive.ejb.jdbc.AECmd;
import org.dcm4chex.archive.ejb.jdbc.AEData;
import org.dcm4chex.archive.ejb.jdbc.QueryCmd;
import org.dcm4chex.archive.ejb.jdbc.RetrieveCmd;
import org.dcm4chex.archive.exceptions.UnkownAETException;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision: 1214 $ $Date: 2004-11-23 19:12:27 +0800 (周二, 23 11月 2004) $
 * @since 31.08.2003
 */
public class QueryRetrieveScpService extends AbstractScpService {

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
    
    private String queryTransactionIsolationLevel;

    private String retrieveTransactionIsolationLevel;
    
    private ObjectName fileSystemMgtName;
    
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

    private boolean patientRootMove;

    private boolean studyRootMove;

    private boolean patientStudyOnlyMove;

    private FindScp findScp = new FindScp(this);

    private MoveScp moveScp = new MoveScp(this);

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

    public final void setAcceptPatientRootFind(boolean patientRootFind) {
        this.patientRootFind = patientRootFind;
        if (getState() == STARTED) {
            updatePolicy(makeAcceptorPolicy());
        }
    }

    public final boolean isAcceptPatientRootMove() {
        return patientRootMove;
    }

    public final void setAcceptPatientRootMove(boolean patientRootMove) {
        this.patientRootMove = patientRootMove;
        updatePolicy();
    }

    public final boolean isAcceptPatientStudyOnlyFind() {
        return patientStudyOnlyFind;
    }

    public final void setAcceptPatientStudyOnlyFind(boolean patientStudyOnlyFind) {
        this.patientStudyOnlyFind = patientStudyOnlyFind;
        updatePolicy();
    }

    public final boolean isAcceptPatientStudyOnlyMove() {
        return patientStudyOnlyMove;
    }

    public final void setAcceptPatientStudyOnlyMove(boolean patientStudyOnlyMove) {
        this.patientStudyOnlyMove = patientStudyOnlyMove;
        updatePolicy();
    }

    public final boolean isAcceptStudyRootFind() {
        return studyRootFind;
    }

    public final void setAcceptStudyRootFind(boolean studyRootFind) {
        this.studyRootFind = studyRootFind;
        updatePolicy();
    }

    public final boolean isAcceptStudyRootMove() {
        return studyRootMove;
    }

    public final void setAcceptStudyRootMove(boolean studyRootMove) {
        this.studyRootMove = studyRootMove;
        updatePolicy();
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

    public final int getBufferSize() {
        return bufferSize;
    }

    public final void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    protected void bindDcmServices(DcmServiceRegistry services) {
        services.bind(UIDs.PatientRootQueryRetrieveInformationModelFIND,
                findScp);
        services.bind(UIDs.StudyRootQueryRetrieveInformationModelFIND, findScp);
        services.bind(UIDs.PatientStudyOnlyQueryRetrieveInformationModelFIND,
                findScp);

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

        services.unbind(UIDs.PatientRootQueryRetrieveInformationModelFIND);
        services.unbind(UIDs.StudyRootQueryRetrieveInformationModelFIND);
        services.unbind(UIDs.PatientStudyOnlyQueryRetrieveInformationModelFIND);
    }

    private String[] getAbstractSyntaxUIDs() {
        ArrayList asuids = new ArrayList(6);
        if (patientRootFind)
                asuids.add(UIDs.PatientRootQueryRetrieveInformationModelFIND);
        if (studyRootFind)
                asuids.add(UIDs.StudyRootQueryRetrieveInformationModelFIND);
        if (patientStudyOnlyFind)
                asuids.add(UIDs.PatientStudyOnlyQueryRetrieveInformationModelFIND);
        if (patientRootMove)
                asuids.add(UIDs.PatientRootQueryRetrieveInformationModelMOVE);
        if (studyRootMove)
                asuids.add(UIDs.StudyRootQueryRetrieveInformationModelMOVE);
        if (patientStudyOnlyMove)
                asuids
                        .add(UIDs.PatientStudyOnlyQueryRetrieveInformationModelMOVE);
        return (String[]) asuids.toArray(new String[asuids.size()]);
    }
    
    private static final ExtNegotiator ECHO_EXT_NEG = new ExtNegotiator() {
        public byte[] negotiate(byte[] offered) {
            return offered;
        }
    };

    protected void initPresContexts(AcceptorPolicy policy) {
        String[] asuids = getAbstractSyntaxUIDs();
        addPresContexts(policy, asuids, getTransferSyntaxUIDs());
        for (int i = 0; i < asuids.length; i++)
            policy.putExtNegPolicy(asuids[i], ECHO_EXT_NEG);
    }

    public AEData queryAEData(String aet) throws SQLException,
            UnkownAETException {
        AEData aeData = new AECmd(getDataSource(), aet).execute();
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
}
