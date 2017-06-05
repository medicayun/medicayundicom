/* $Id: MPPSForwardCmd.java 1119 2004-05-21 09:50:27Z gunterze $
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
package org.dcm4chex.archive.dcm.mppsscp;

import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;

import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Status;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.AAssociateAC;
import org.dcm4che.net.AAssociateRQ;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.Association;
import org.dcm4che.net.AssociationFactory;
import org.dcm4che.net.DcmServiceException;
import org.dcm4che.net.FutureRSP;
import org.dcm4che.net.PDU;
import org.dcm4che.net.PresContext;
import org.dcm4chex.archive.ejb.jdbc.AECmd;
import org.dcm4chex.archive.ejb.jdbc.AEData;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1119 $ $Date: 2004-05-21 17:50:27 +0800 (周五, 21 5月 2004) $
 * @since 10.03.2004
 */
abstract class MPPSForwardCmd {
    private static final String[] NATIVE_TS =
        { UIDs.ExplicitVRLittleEndian, UIDs.ImplicitVRLittleEndian };
    private static final int PC_ID = 1;
    private static AssociationFactory aFact = AssociationFactory.getInstance();
    private static DcmObjectFactory dofFact = DcmObjectFactory.getInstance();

    protected final MPPSScpService service;
    protected final AEData aeData;
    protected final String callingAET;
    protected final String iuid;
    protected final Dataset mpps;

    MPPSForwardCmd(MPPSScpService service, String callingAET, String iuid, Dataset mpps)
        throws DcmServiceException {
        this.service = service;
        this.callingAET = callingAET;
        this.iuid = iuid;
        this.mpps = mpps;
        try {
            this.aeData =
                new AECmd(service.getDataSource(), service.getForwardAET()).execute();
            if (aeData == null) {
                throw new DcmServiceException(
                    Status.ProcessingFailure,
                    "Failed to resolve AET:" + service.getForwardAET());
            }
        } catch (SQLException e) {
            throw new DcmServiceException(Status.ProcessingFailure, e);
        }
    }

    void execute() {
        try {
            Association assoc = aFact.newRequestor(createSocket());
            assoc.setAcTimeout(service.getAcTimeout());
            assoc.setDimseTimeout(service.getDimseTimeout());
            assoc.setSoCloseDelay(service.getSoCloseDelay());
            PDU pdu = assoc.connect(makeAAssociateRQ());
            if (!(pdu instanceof AAssociateAC)) {
                service.getLog().error(
                    "connection to " + aeData + " failed: " + pdu);
                return;
            }
            ActiveAssociation activeAssoc =
                aFact.newActiveAssociation(assoc, null);
            activeAssoc.start();
            if (checkAAssociateAC((AAssociateAC) pdu)) {
                service.logDataset("Forward MPPS to " + aeData + "\n", mpps);
                FutureRSP rsp =
                    activeAssoc.invoke(
                        aFact.newDimse(PC_ID, makeCommand(assoc), mpps));
                Command rspCmd = rsp.get().getCommand();
                if (rspCmd.getStatus() != Status.Success) {
                    service.getLog().warn("" + aeData + " returns " + rspCmd);
                }
            } else {
                service.getLog().error("MPPS rejected by " + aeData);
            }
            try {
                activeAssoc.release(false);
            } catch (Exception e) {
                service.getLog().warn(
                    "release association to " + aeData + " failed:",
                    e);
            }
        } catch (Exception e) {
            service.getLog().error(
                "sending storage commitment result to " + aeData + " failed:",
                e);
        }
    }

    private AAssociateRQ makeAAssociateRQ() {
        AAssociateRQ rq = aFact.newAAssociateRQ();
        rq.setCallingAET(callingAET);
        rq.setCalledAET(aeData.getTitle());
        rq.addPresContext(
            aFact.newPresContext(
                PC_ID,
                UIDs.ModalityPerformedProcedureStep,
                NATIVE_TS));
        return rq;
    }

    private boolean checkAAssociateAC(AAssociateAC ac) {
        return ac.getPresContext(PC_ID).result() == PresContext.ACCEPTANCE;
    }

    private Socket createSocket() throws IOException {
        String[] cipherSuites = aeData.getCipherSuites();
        if (cipherSuites == null || cipherSuites.length == 0) {
            return new Socket(aeData.getHostName(), aeData.getPort());
        } else {
            return service.getSocketFactory(cipherSuites).createSocket(
                    aeData.getHostName(),
                    aeData.getPort());
        }
    }
    
    protected abstract Command makeCommand(Association as);

    static final class NCreate extends MPPSForwardCmd {

        NCreate(MPPSScpService service, String callingAET, String iuid, Dataset ds)
            throws DcmServiceException {
            super(service, callingAET, iuid, ds);
        }

        protected Command makeCommand(Association as) {
            Command cmd = dofFact.newCommand();
            cmd.initNCreateRQ(
                as.nextMsgID(),
                UIDs.ModalityPerformedProcedureStep,
                iuid);
            return cmd;
        }
    }

    static final class NSet extends MPPSForwardCmd {

        NSet(MPPSScpService service, String callingAET, String iuid, Dataset ds)
            throws DcmServiceException {
            super(service, callingAET, iuid, ds);
        }

        protected Command makeCommand(Association as) {
            Command cmd = dofFact.newCommand();
            cmd.initNSetRQ(
                as.nextMsgID(),
                UIDs.ModalityPerformedProcedureStep,
                iuid);
            return cmd;
        }
    }
}
