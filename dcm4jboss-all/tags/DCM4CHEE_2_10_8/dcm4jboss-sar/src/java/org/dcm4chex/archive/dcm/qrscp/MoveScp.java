/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), available at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * TIANI Medgraph AG.
 * Portions created by the Initial Developer are Copyright (C) 2003-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunter.zeilinger@tiani.com>
 * Franz Willer <franz.willer@gwi-ag.com>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package org.dcm4chex.archive.dcm.qrscp;

import java.io.IOException;
import java.net.InetAddress;
import java.sql.SQLException;

import javax.management.ObjectName;

import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmObject;
import org.dcm4che.dict.Status;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.AAssociateAC;
import org.dcm4che.net.AAssociateRQ;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.Association;
import org.dcm4che.net.AssociationListener;
import org.dcm4che.net.DcmServiceBase;
import org.dcm4che.net.DcmServiceException;
import org.dcm4che.net.Dimse;
import org.dcm4che.net.PDU;
import org.dcm4chex.archive.ejb.jdbc.AEData;
import org.dcm4chex.archive.ejb.jdbc.FileInfo;
import org.dcm4chex.archive.ejb.jdbc.RetrieveCmd;
import org.dcm4chex.archive.exceptions.UnkownAETException;
import org.dcm4chex.archive.perf.PerfCounterEnum;
import org.dcm4chex.archive.perf.PerfMonDelegate;
import org.dcm4chex.archive.perf.PerfPropertyEnum;
import org.jboss.logging.Logger;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision: 2957 $ $Date: 2006-11-16 06:05:19 +0800 (周四, 16 11月 2006) $
 * @since 31.08.2003
 */
public class MoveScp extends DcmServiceBase implements AssociationListener{
    private final QueryRetrieveScpService service;
	private final Logger log;
    private PerfMonDelegate perfMon;

    public MoveScp(QueryRetrieveScpService service) {
        this.service = service;
		this.log = service.getLog();
        perfMon = new PerfMonDelegate(this.service);
    }

    public void c_move(ActiveAssociation assoc, Dimse rq) throws IOException {
        Command rqCmd = rq.getCommand();
        try {            
            Dataset rqData = rq.getDataset();
            if(log.isDebugEnabled()) {
            	log.debug("Identifier:\n");
            	log.debug(rqData);
            }
            
            checkMoveRQ(assoc.getAssociation(), rq.pcid(), rqCmd, rqData);
            String dest = rqCmd.getString(Tags.MoveDestination);
            AEData aeData = null;
            FileInfo[][] fileInfos = null;
            try {
               	perfMon.start(assoc, rq, PerfCounterEnum.C_MOVE_SCP_QUERY_DB);
            	perfMon.setProperty(assoc, rq, PerfPropertyEnum.REQ_DIMSE, rq);

    			InetAddress host = dest.equals( assoc.getAssociation().getCallingAET()) ? assoc.getAssociation().getSocket().getInetAddress() : null;
                aeData = service.queryAEData(dest, host);
                fileInfos = RetrieveCmd.create(rqData).getFileInfos();

                perfMon.setProperty(assoc, rq, PerfPropertyEnum.NUM_OF_RESULTS, String.valueOf(fileInfos.length));
                perfMon.stop(assoc, rq, PerfCounterEnum.C_MOVE_SCP_QUERY_DB);
                
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
            } catch (UnkownAETException e) {
                throw new DcmServiceException(Status.MoveDestinationUnknown, dest);
            } catch (SQLException e) {
                service.getLog().error("Query DB failed:", e);
                throw new DcmServiceException(Status.UnableToProcess, e);
            } catch (Throwable e) {
                service.getLog().error("Unexpected exception:", e);
                throw new DcmServiceException(Status.UnableToProcess, e);
            }
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
    
    public final ObjectName getPerfMonServiceName() {
		return perfMon.getPerfMonServiceName();
	}

	public final void setPerfMonServiceName(ObjectName perfMonServiceName) {
		perfMon.setPerfMonServiceName(perfMonServiceName);
	}
    
    public PerfMonDelegate getPerfMonDelegate() {
    	return perfMon;
    }

    public void write(Association src, PDU pdu) {
    	if (pdu instanceof AAssociateAC)
    		perfMon.assocEstEnd(src, Command.C_MOVE_RQ);
	}

	public void received(Association src, PDU pdu) {
    	if(pdu instanceof AAssociateRQ)
    		perfMon.assocEstStart(src, Command.C_MOVE_RQ);
	}

	public void write(Association src, Dimse dimse) {
	}

	public void received(Association src, Dimse dimse) {
	}

	public void error(Association src, IOException ioe) {
	}

	public void closing(Association assoc) {
    	if(assoc.getAAssociateAC() != null)
    		perfMon.assocRelStart(assoc, Command.C_MOVE_RQ);
	}

	public void closed(Association assoc) {
    	if(assoc.getAAssociateAC() != null)
    		perfMon.assocRelEnd(assoc, Command.C_MOVE_RQ);
	}
}
