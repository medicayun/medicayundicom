/* $Id: MPPSScp.java 1079 2004-04-05 11:26:02Z umbertoc $
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
import org.dcm4che.data.DcmElement;
import org.dcm4che.dict.Status;
import org.dcm4che.dict.Tags;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.Association;
import org.dcm4che.net.DcmServiceBase;
import org.dcm4che.net.DcmServiceException;
import org.dcm4che.net.Dimse;
import org.dcm4chex.archive.ejb.interfaces.MPPSManager;
import org.dcm4chex.archive.ejb.interfaces.MPPSManagerHome;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.dcm4chex.archive.util.HomeFactoryException;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision: 1079 $ $Date: 2004-04-05 19:26:02 +0800 (周一, 05 4月 2004) $
 */
class MPPSScp extends DcmServiceBase {

    private static final String IN_PROGRESS = "IN PROGRESS";

    private static final String COMPLETED = "COMPLETED";

    private static final String DISCONTINUED = "DISCONTINUED";

    private static final int[] TYPE1_NCREATE_ATTR = {
            Tags.ScheduledStepAttributesSeq, // Tags.PPSID,
            Tags.PerformedStationAET, Tags.PPSStartDate, Tags.PPSStartTime,
            Tags.PPSStatus, Tags.Modality};

    private static final int[] ONLY_NCREATE_ATTR = {
            Tags.ScheduledStepAttributesSeq, Tags.PatientName, Tags.PatientID,
            Tags.PatientBirthDate, Tags.PatientSex, Tags.PPSID,
            Tags.PerformedStationAET, Tags.PerformedStationName,
            Tags.PerformedLocation, Tags.PPSStartDate, Tags.PPSStartTime,
            Tags.Modality, Tags.StudyID};

    private static final int[] TYPE1_FINAL_ATTR = { Tags.PPSEndDate,
            Tags.PPSEndTime, Tags.PerformedSeriesSeq};

    private final MPPSScpService service;

    public MPPSScp(MPPSScpService service) {
        this.service = service;
    }

    private MPPSManagerHome getMPPSManagerHome() throws HomeFactoryException {
        return (MPPSManagerHome) EJBHomeFactory.getFactory().lookup(
                MPPSManagerHome.class, MPPSManagerHome.JNDI_NAME);
    }

    protected Dataset doNCreate(ActiveAssociation assoc, Dimse rq,
            Command rspCmd) throws IOException, DcmServiceException {
        final Command cmd = rq.getCommand();
        final Dataset mpps = rq.getDataset();
        final String iuid = rspCmd.getAffectedSOPInstanceUID();
        service.logDataset("Creating MPPS:\n", mpps);
        checkCreateAttributs(mpps);
        createMPPS(iuid, mpps);
        if (service.isForward()) {
            Association a = assoc.getAssociation();
            a.putProperty("MPPSForwardCmd-" + cmd.getMessageID(),
                    new MPPSForwardCmd.NCreate(service, iuid, mpps));
        }
        return null;
    }

    private void createMPPS(String iuid, Dataset mpps)
            throws DcmServiceException {
        try {
            MPPSManager mgr = getMPPSManagerHome().create();
            try {
                mgr.createMPPS(iuid, mpps);
            } finally {
                try {
                    mgr.remove();
                } catch (Exception ignore) {
                }
            }
        } catch (DcmServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new DcmServiceException(Status.ProcessingFailure, e);
        }
    }

    private void updateMPPS(String iuid, Dataset mpps)
            throws DcmServiceException {
        try {
            MPPSManager mgr = getMPPSManagerHome().create();
            try {
                mgr.updateMPPS(iuid, mpps);
            } finally {
                try {
                    mgr.remove();
                } catch (Exception ignore) {
                }
            }
        } catch (DcmServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new DcmServiceException(Status.ProcessingFailure, e);
        }
    }

    protected Dataset doNSet(ActiveAssociation assoc, Dimse rq, Command rspCmd)
            throws IOException, DcmServiceException {
        final Command cmd = rq.getCommand();
        final Dataset mpps = rq.getDataset();
        final String iuid = cmd.getRequestedSOPInstanceUID();
        service.logDataset("Set MPPS:\n", mpps);
        checkSetAttributs(mpps);
        updateMPPS(iuid, mpps);
        if (service.isForward()) {
            Association a = assoc.getAssociation();
            a.putProperty("MPPSForwardCmd-" + cmd.getMessageID(),
                    new MPPSForwardCmd.NSet(service, iuid, mpps));
        }
        return null;
    }

    private void checkCreateAttributs(Dataset mpps) throws DcmServiceException {
        for (int i = 0; i < TYPE1_NCREATE_ATTR.length; ++i) {
            if (mpps.vm(TYPE1_NCREATE_ATTR[i]) <= 0) { throw new DcmServiceException(
                    Status.MissingAttributeValue, "Missing Type 1 Attribute "
                            + Tags.toString(TYPE1_NCREATE_ATTR[i])); }
        }
        DcmElement ssaSq = mpps.get(Tags.ScheduledStepAttributesSeq);
        for (int i = 0, n = ssaSq.vm(); i < n; ++i) {
            if (ssaSq.getItem(i).vm(Tags.StudyInstanceUID) <= 0) { throw new DcmServiceException(
                    Status.MissingAttributeValue,
                    "Missing Study Instance UID in Scheduled Step Attributes Seq."); }
        }
        if (!IN_PROGRESS.equals(mpps.getString(Tags.PPSStatus))) { throw new DcmServiceException(
                Status.InvalidAttributeValue); }
    }

    private void checkSetAttributs(Dataset mpps) throws DcmServiceException {
        for (int i = 0; i < ONLY_NCREATE_ATTR.length; ++i) {
            if (mpps.vm(ONLY_NCREATE_ATTR[i]) >= 0) { throw new DcmServiceException(
                    Status.ProcessingFailure, "Cannot update attribute "
                            + Tags.toString(ONLY_NCREATE_ATTR[i])); }
        }
        final String status = mpps.getString(Tags.PPSStatus);
        if (status == null || status.equals(IN_PROGRESS)) return;
        if (!status.equals(COMPLETED) && !status.equals(DISCONTINUED)) { throw new DcmServiceException(
                Status.InvalidAttributeValue, "Invalid MPPS Status: " + status); }
        for (int i = 0; i < TYPE1_FINAL_ATTR.length; ++i) {
            if (mpps.vm(TYPE1_FINAL_ATTR[i]) <= 0) { throw new DcmServiceException(
                    Status.MissingAttributeValue, "Missing Type 1 Attribute "
                            + Tags.toString(TYPE1_FINAL_ATTR[i])); }
        }
    }

    protected void doAfterRsp(ActiveAssociation assoc, Dimse rsp) {
        Association a = assoc.getAssociation();
        Command cmd = rsp.getCommand();
        String key = "MPPSForwardCmd-" + cmd.getMessageIDToBeingRespondedTo();
        MPPSForwardCmd forwardCmd = (MPPSForwardCmd) a.getProperty(key);
        if (forwardCmd != null) {
            forwardCmd.execute();
        }
    }
}
