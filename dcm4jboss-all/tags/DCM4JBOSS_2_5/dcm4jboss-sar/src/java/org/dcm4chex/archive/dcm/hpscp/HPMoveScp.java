/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4chex.archive.dcm.hpscp;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Status;
import org.dcm4che.dict.Tags;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.Association;
import org.dcm4che.net.DcmServiceBase;
import org.dcm4che.net.DcmServiceException;
import org.dcm4che.net.Dimse;
import org.dcm4chex.archive.ejb.jdbc.AECmd;
import org.dcm4chex.archive.ejb.jdbc.AEData;
import org.dcm4chex.archive.ejb.jdbc.HPRetrieveCmd;
import org.jboss.logging.Logger;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1929 $ $Date: 2005-08-30 22:54:54 +0800 (周二, 30 8月 2005) $
 * @since Aug 17, 2005
 */
public class HPMoveScp extends DcmServiceBase {

	private final HPScpService service;

	private final Logger log;

	public HPMoveScp(HPScpService service) {
		this.service = service;
		this.log = service.getLog();
	}

	public void c_move(ActiveAssociation assoc, Dimse rq) throws IOException {
		Command rqCmd = rq.getCommand();
		try {
			Dataset rqData = rq.getDataset();
			log.debug("Identifier:\n");
			log.debug(rqData);
			checkMoveRQ(assoc.getAssociation(), rq.pcid(), rqCmd, rqData);
			String dest = rqCmd.getString(Tags.MoveDestination);
			AEData aeData = queryAEData(dest);
			List hpList = qeuryHPList(rqData);
			new Thread(new HPMoveTask(service, assoc, rq.pcid(), rqCmd, rqData,
					hpList, aeData, dest)).start();
		} catch (DcmServiceException e) {
			Command rspCmd = objFact.newCommand();
			rspCmd.initCMoveRSP(rqCmd.getMessageID(), rqCmd
					.getAffectedSOPClassUID(), e.getStatus());
			e.writeTo(rspCmd);
			Dimse rsp = fact.newDimse(rq.pcid(), rspCmd);
			assoc.getAssociation().write(rsp);
		}
	}

	private List qeuryHPList(Dataset rqData)
			throws DcmServiceException {
		try {
			return new HPRetrieveCmd(rqData).getDatasets();
		} catch (SQLException e) {
			service.getLog().error("Query DB failed:", e);
			throw new DcmServiceException(Status.ProcessingFailure, e);
		}
	}

	private AEData queryAEData(String dest)
			throws DcmServiceException {
		try {
			AEData aeData = new AECmd(dest).getAEData();
			if (aeData == null) {
				throw new DcmServiceException(Status.MoveDestinationUnknown,
						dest);
			}
			return aeData;
		} catch (SQLException e) {
			service.getLog().error("Query DB failed:", e);
			throw new DcmServiceException(Status.ProcessingFailure, e);
		}
	}

	private void checkMoveRQ(Association assoc, int pcid, Command rqCmd,
			Dataset rqData) throws DcmServiceException {
		
		if (rqCmd.vm(Tags.MoveDestination) <= 0) {
			throw new DcmServiceException(Status.UnableToProcess, 
					"Missing Move Destination");
		}

		if (rqData.vm(Tags.SOPInstanceUID) <= 0) {
			throw new DcmServiceException(Status.IdentifierDoesNotMatchSOPClass,
					"Missing SOP Instance UID");
		}
	}

}
