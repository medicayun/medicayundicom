/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.dcm.gpwlscp;

import java.io.IOException;

import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.dict.Status;
import org.dcm4che.dict.Tags;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.DcmServiceBase;
import org.dcm4che.net.DcmServiceException;
import org.dcm4che.net.Dimse;
import org.dcm4chex.archive.ejb.interfaces.GPWLManager;
import org.dcm4chex.archive.ejb.interfaces.GPWLManagerHome;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.dcm4chex.archive.util.HomeFactoryException;
import org.jboss.logging.Logger;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1793 $ $Date: 2005-06-22 15:47:41 +0800 (周三, 22 6月 2005) $
 * @since 04.04.2005
 *
 */

class GPSPSScp extends DcmServiceBase {
	
    private static final int[] TYPE1_NACTION_ATTR = {
        Tags.TransactionUID, Tags.GPSPSStatus};
	private static final int REQUEST_GPSPS_STATUS_MODIFICATION = 1;
	private final GPWLScpService service;
	private final Logger log;

    public GPSPSScp(GPWLScpService service) {
        this.service = service;
        this.log = service.getLog();
    }

	protected Dataset doNAction(ActiveAssociation assoc, Dimse rq,
			Command rspCmd) throws IOException, DcmServiceException {
        Command rqCmd = rq.getCommand();
        Dataset actionInfo = rq.getDataset();
        log.debug("N-Action Information:\n");
		log.debug(actionInfo);

        final String iuid = rqCmd.getAffectedSOPInstanceUID();
        final int actionID = rqCmd.getInt(Tags.ActionTypeID, -1);
        if (actionID != REQUEST_GPSPS_STATUS_MODIFICATION) 
        	throw new DcmServiceException(Status.NoSuchActionType, "actionID:"
                    + actionID);
        if (actionInfo.vm(Tags.TransactionUID) <= 0)
            throw new DcmServiceException(Status.MissingAttributeValue,
                    "Missing Transaction UID (0008,1195)");
        if (actionInfo.vm(Tags.GPSPSStatus) <= 0)
            throw new DcmServiceException(Status.MissingAttributeValue,
                    "Missing GPSPS Status (0040,4001)");
        DcmElement src = actionInfo.get(Tags.ActualHumanPerformersSeq);
        if (src != null) {
	    	Dataset item, code;
	    	for (int i = 0, n = src.vm(); i < n; ++i) {
	    		item = src.getItem(i);
	    		code = item.getItem(Tags.HumanPerformerCodeSeq);
	    		if (code == null) {
	    			log.warn("Missing >Human Performer Code Seq (0040,4009)");
	    		} else if (code.vm(Tags.CodeValue) <= 0
	    					|| code.vm(Tags.CodingSchemeDesignator) <= 0) {
	    			log.warn("Invalid Item in >Human Performer Code Seq (0040,4009)");    			
	    		}
	      	}
        }
        modifyStatus(iuid, actionInfo);
		return null;
	}

	private void modifyStatus(String iuid, Dataset actionInfo)
			throws DcmServiceException {
        try {
            GPWLManager mgr = getGPWLManagerHome().create();
            try {
                mgr.modifyStatus(iuid, actionInfo);
            } finally {
                try {
                    mgr.remove();
                } catch (Exception ignore) {
                }
            }
        } catch (DcmServiceException e) {
        	log.error("Exception during status update:", e);
            throw e;
        } catch (Exception e) {
        	log.error("Exception during status update:", e);
            throw new DcmServiceException(Status.ProcessingFailure, e);
        }
	}

	private GPWLManagerHome getGPWLManagerHome() throws HomeFactoryException {
        return (GPWLManagerHome) EJBHomeFactory.getFactory().lookup(
                GPWLManagerHome.class, GPWLManagerHome.JNDI_NAME);
    }
}
