/* $Id: QueryRetrieveScpService.java 1132 2004-06-14 14:34:51Z gunterze $
 * Copyright (c) 2002,2003 by TIANI MEDGRAPH AG
 *
 * This file is part of dcm4che.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.dcm4chex.archive.dcm.qrscp;

import java.sql.SQLException;
import java.util.ArrayList;

import org.dcm4che.dict.UIDs;
import org.dcm4che.net.AcceptorPolicy;
import org.dcm4che.net.DcmServiceRegistry;
import org.dcm4che.net.ExtNegotiator;
import org.dcm4chex.archive.dcm.AbstractScpService;
import org.dcm4chex.archive.ejb.jdbc.AECmd;
import org.dcm4chex.archive.ejb.jdbc.AEData;
import org.dcm4chex.archive.exceptions.UnkownAETException;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision: 1132 $ $Date: 2004-06-14 22:34:51 +0800 (周一, 14 6月 2004) $
 * @since 31.08.2003
 */
public class QueryRetrieveScpService extends AbstractScpService {

    private String[] retrieveAETs = {};

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

    public final String[] getRetrieveAETs() {
        return retrieveAETs;
    }

    public final void setRetrieveAETs(String[] retrieveAETs) {
        this.retrieveAETs = retrieveAETs;
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
}
