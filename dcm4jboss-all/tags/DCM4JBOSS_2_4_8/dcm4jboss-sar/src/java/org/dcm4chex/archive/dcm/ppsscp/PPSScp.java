/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4chex.archive.dcm.ppsscp;

import java.io.IOException;

import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.DcmServiceBase;
import org.dcm4che.net.DcmServiceException;
import org.dcm4che.net.Dimse;
import org.jboss.logging.Logger;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision: 1793 $ $Date: 2005-06-22 15:47:41 +0800 (周三, 22 6月 2005) $
 */
class PPSScp extends DcmServiceBase {

    private final PPSScpService service;
	private final Logger log;

    public PPSScp(PPSScpService service) {
        this.service = service;
		this.log = service.getLog();
    }

    protected Dataset doNCreate(ActiveAssociation assoc, Dimse rq,
            Command rspCmd) throws IOException, DcmServiceException {
        final Command cmd = rq.getCommand();
        final Dataset gppps = rq.getDataset();
        final String iuid = rspCmd.getAffectedSOPInstanceUID();
		log.debug("Creating PPS:\n");
		log.debug(gppps);
        gppps.putUI(Tags.SOPInstanceUID, iuid);
        service.sendPPSNotification(gppps);
        return null;
    }

    protected Dataset doNSet(ActiveAssociation assoc, Dimse rq, Command rspCmd)
            throws IOException, DcmServiceException {
        final Command cmd = rq.getCommand();
        final Dataset gppps = rq.getDataset();
        final String iuid = cmd.getRequestedSOPInstanceUID();
		log.debug("Set PPS:\n");
		log.debug(gppps);
        gppps.putUI(Tags.SOPInstanceUID, iuid);
        service.sendPPSNotification(gppps);
        return null;
    }
}
