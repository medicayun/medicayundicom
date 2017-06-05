/* $Id: MoveScp.java 1004 2004-02-21 22:32:43Z gunterze $
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
import org.dcm4chex.archive.ejb.jdbc.AECmd;
import org.dcm4chex.archive.ejb.jdbc.AEData;
import org.dcm4chex.archive.ejb.jdbc.FileInfo;
import org.dcm4chex.archive.ejb.jdbc.RetrieveCmd;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision: 1004 $ $Date: 2004-02-22 06:32:43 +0800 (周日, 22 2月 2004) $
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
            AEData aeData = queryAEData(dest);
            FileInfo[][] fileInfos = queryFileInfos(rqData);
            new Thread(
                new MoveTask(
                    service,
                    assoc,
                    rq.pcid(),
                    rqCmd,
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

    private FileInfo[][] queryFileInfos(Dataset rqData)
        throws DcmServiceException {
        FileInfo[][] fileInfos = null;
        try {
            RetrieveCmd retrieveCmd =
                RetrieveCmd.create(service.getDS(), rqData);
            fileInfos = retrieveCmd.execute();
        } catch (Exception e) {
            service.getLog().error("Query DB failed:", e);
            throw new DcmServiceException(Status.ProcessingFailure, e);
        }
        return fileInfos;
    }

    private AEData queryAEData(String dest) throws DcmServiceException {
        AEData aeData = null;
        try {
            AECmd aeCmd = new AECmd(service.getDS(), dest);
            aeData = aeCmd.execute();
        } catch (Exception e) {
            service.getLog().error("Query DB failed:", e);
            throw new DcmServiceException(Status.ProcessingFailure, e);
        }
        if (aeData == null) {
            throw new DcmServiceException(Status.MoveDestinationUnknown, dest);
        }
        return aeData;
    }
}
