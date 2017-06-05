/* $Id: MoveForwardCmd.java 1091 2004-04-24 13:42:49Z gunterze $
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
package org.dcm4chex.service;

import java.io.IOException;
import java.sql.SQLException;

import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.AAssociateAC;
import org.dcm4che.net.AAssociateRQ;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.Association;
import org.dcm4che.net.AssociationFactory;
import org.dcm4che.net.Dimse;
import org.dcm4che.net.DimseListener;
import org.dcm4che.net.PDU;
import org.dcm4chex.archive.ejb.jdbc.AEData;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1091 $ $Date: 2004-04-24 21:42:49 +0800 (周六, 24 4月 2004) $
 * @since 20.04.2004
 */
class MoveForwardCmd {

    private static final AssociationFactory af = AssociationFactory
            .getInstance();

    private static final DcmObjectFactory of = DcmObjectFactory.getInstance();

    private static final String IMAGE = "IMAGE";

    private static final int MSGID = 1;

    private static final int PCID = 1;

    private static final String[] NATIVE_TS = { UIDs.ExplicitVRLittleEndian,
            UIDs.ImplicitVRLittleEndian};

    private final QueryRetrieveScpService service;

    private final AEData aeData;

    private final String callingAET;

    private final String destAET;

    private final int priority;

    private final String[] iuids;

    private ActiveAssociation aa;
    
    public MoveForwardCmd(QueryRetrieveScpService service, String callingAET,
            String retrieveAET, int priority, String destAET, String[] iuids)
            throws SQLException, UnkownAETException {
        this.service = service;
        this.callingAET = callingAET;
        this.aeData = service.queryAEData(retrieveAET);
        this.destAET = destAET;
        this.iuids = iuids;
        this.priority = priority;
    }

    public void execute(DimseListener moveRspListener)
            throws InterruptedException, IOException {
        Association a = af.newRequestor(service.createSocket(aeData));
        a.setAcTimeout(service.getAcTimeout());
        AAssociateRQ rq = af.newAAssociateRQ();
        rq.setCalledAET(aeData.getTitle());
        rq.setCallingAET(callingAET);
        rq.addPresContext(af.newPresContext(PCID,
                UIDs.StudyRootQueryRetrieveInformationModelMOVE, NATIVE_TS));
        PDU ac = a.connect(rq);
        if (!(ac instanceof AAssociateAC)) { throw new IOException(
                "Association not accepted by " + aeData + ":\n" + ac); }
        ActiveAssociation aa = af.newActiveAssociation(a, null);
        aa.start();
        try {
            if (a.getAcceptedTransferSyntaxUID(PCID) == null) { throw new IOException(
                    "Study Root IM MOVE Service not supported by " + aeData); }
            Command cmd = of.newCommand();
            cmd.initCMoveRQ(MSGID,
                    UIDs.StudyRootQueryRetrieveInformationModelMOVE, priority,
                    destAET);
            Dataset ds = of.newDataset();
            ds.putCS(Tags.QueryRetrieveLevel, IMAGE);
            ds.putUI(Tags.SOPInstanceUID, iuids);
            Dimse cmoverq = af.newDimse(PCID, cmd, ds);
            aa.invoke(cmoverq, moveRspListener);
        } finally {
            try {
                aa.release(true);
                // workaround to ensure that the final MOVE-RSP of forwarded
                // MOVE-RQ is processed before the final MOVE-RSP is sent
                // to the primary Move Originator
                Thread.sleep(10); 
            } catch (Exception ignore) {
            }
        }
    }

    public void cancel() throws IOException {
        if (aa == null)
            return;
        Command cmd = of.newCommand();
        cmd.initCCancelRQ(MSGID);
        Dimse ccancelrq = af.newDimse(PCID, cmd);
        aa.getAssociation().write(ccancelrq);
    }
}