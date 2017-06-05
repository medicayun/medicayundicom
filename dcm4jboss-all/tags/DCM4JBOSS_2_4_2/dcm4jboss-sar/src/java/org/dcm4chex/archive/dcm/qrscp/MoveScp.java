/* $Id: MoveScp.java 1533 2005-02-14 18:36:57Z gunterze $
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

import java.io.IOException;
import java.sql.SQLException;

import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmObject;
import org.dcm4che.dict.Status;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.Association;
import org.dcm4che.net.DcmServiceBase;
import org.dcm4che.net.DcmServiceException;
import org.dcm4che.net.Dimse;
import org.dcm4chex.archive.ejb.jdbc.AEData;
import org.dcm4chex.archive.ejb.jdbc.FileInfo;
import org.dcm4chex.archive.ejb.jdbc.RetrieveCmd;
import org.dcm4chex.archive.exceptions.UnkownAETException;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision: 1533 $ $Date: 2005-02-15 02:36:57 +0800 (周二, 15 2月 2005) $
 * @since 31.08.2003
 */
public class MoveScp extends DcmServiceBase {
    private final QueryRetrieveScpService service;

    public MoveScp(QueryRetrieveScpService service) {
        this.service = service;
    }

    public void c_move(ActiveAssociation assoc, Dimse rq) throws IOException {
        Command rqCmd = rq.getCommand();
        try {
            Dataset rqData = rq.getDataset();
            service.logDataset("Identifier:\n", rqData);
            checkMoveRQ(assoc.getAssociation(), rq.pcid(), rqCmd, rqData);
            String dest = rqCmd.getString(Tags.MoveDestination);
            AEData aeData = null;
            FileInfo[][] fileInfos = null;
            try {
                aeData = service.queryAEData(dest);
                fileInfos = RetrieveCmd.create(rqData).execute();
            } catch (SQLException e) {
                service.getLog().error("Query DB failed:", e);
                throw new DcmServiceException(Status.ProcessingFailure, e);
            } catch (UnkownAETException e) {
                throw new DcmServiceException(Status.MoveDestinationUnknown, dest);
            }
            new Thread(
                new MoveTask(
                    service,
                    assoc,
                    rq.pcid(),
                    rqCmd,
                    rqData,
                    fileInfos,
                    aeData,
                    dest))
                .start();
        } catch (DcmServiceException e) {
            Command rspCmd = objFact.newCommand();
            rspCmd.initCMoveRSP(
                rqCmd.getMessageID(),
                rqCmd.getAffectedSOPClassUID(),
                e.getStatus());
            e.writeTo(rspCmd);
            Dimse rsp = fact.newDimse(rq.pcid(), rspCmd);
            assoc.getAssociation().write(rsp);
        }
    }

    private void checkMoveRQ(
        Association assoc,
        int pcid,
        Command rqCmd,
        Dataset rqData)
        throws DcmServiceException {

        checkAttribute(
            rqCmd,
            Tags.MoveDestination,
            Status.UnableToProcess,
            "Missing Move Destination");
        checkAttribute(
            rqData,
            Tags.QueryRetrieveLevel,
            Status.IdentifierDoesNotMatchSOPClass,
            "Missing Query Retrieve Level");

        final String level = rqData.getString(Tags.QueryRetrieveLevel);
        final String asid =
            assoc.getProposedPresContext(pcid).getAbstractSyntaxUID();
        if ("PATIENT".equals(level)) {
            if (UIDs.StudyRootQueryRetrieveInformationModelMOVE.equals(asid)) {
                throw new DcmServiceException(
                    Status.IdentifierDoesNotMatchSOPClass,
                    "Cannot use Query Retrieve Level PATIENT with Study Root IM");
            }
            checkAttribute(
                rqData,
                Tags.PatientID,
                Status.IdentifierDoesNotMatchSOPClass,
                "Missing Patient ID");
        } else if ("STUDY".equals(level)) {
            checkAttribute(
                rqData,
                Tags.StudyInstanceUID,
                Status.IdentifierDoesNotMatchSOPClass,
                "Missing Study Instance UID");
            
        } else if ("SERIES".equals(level)) {
            if (UIDs.PatientStudyOnlyQueryRetrieveInformationModelMOVE.equals(asid)) {
                throw new DcmServiceException(
                    Status.IdentifierDoesNotMatchSOPClass,
                    "Cannot use Query Retrieve Level SERIES with Patient Study Only IM");
            }
            checkAttribute(
                rqData,
                Tags.SeriesInstanceUID,
                Status.IdentifierDoesNotMatchSOPClass,
                "Missing Series Instance UID");            
        } else if ("IMAGE".equals(level)) {
            if (UIDs.PatientStudyOnlyQueryRetrieveInformationModelMOVE.equals(asid)) {
                throw new DcmServiceException(
                    Status.IdentifierDoesNotMatchSOPClass,
                    "Cannot use Query Retrieve Level SERIES with Patient Study Only IM");
            }
            checkAttribute(
                rqData,
                Tags.SOPInstanceUID,
                Status.IdentifierDoesNotMatchSOPClass,
                "Missing SOP Instance UID");            
        } else {
            throw new DcmServiceException(
                Status.IdentifierDoesNotMatchSOPClass,
                "Invalid Retrieve Level " + level);            
        }
    }

    private void checkAttribute(DcmObject dcm, int tag, int status, String msg)
        throws DcmServiceException {
        if (dcm.vm(tag) <= 0) {
            throw new DcmServiceException(status, msg);
        }
    }
}
