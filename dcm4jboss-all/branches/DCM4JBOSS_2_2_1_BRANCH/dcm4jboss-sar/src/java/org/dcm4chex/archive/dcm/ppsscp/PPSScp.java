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

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision: 1295 $ $Date: 2004-12-14 01:53:01 +0800 (周二, 14 12月 2004) $
 */
class PPSScp extends DcmServiceBase {

    private final PPSScpService service;

    public PPSScp(PPSScpService service) {
        this.service = service;
    }

    protected Dataset doNCreate(ActiveAssociation assoc, Dimse rq,
            Command rspCmd) throws IOException, DcmServiceException {
        final Command cmd = rq.getCommand();
        final Dataset mpps = rq.getDataset();
        final String iuid = rspCmd.getAffectedSOPInstanceUID();
        service.logDataset("Creating PPS:\n", mpps);
        mpps.putUI(Tags.SOPInstanceUID, iuid);
        return null;
    }

    protected Dataset doNSet(ActiveAssociation assoc, Dimse rq, Command rspCmd)
            throws IOException, DcmServiceException {
        final Command cmd = rq.getCommand();
        final Dataset mpps = rq.getDataset();
        final String iuid = cmd.getRequestedSOPInstanceUID();
        service.logDataset("Set PPS:\n", mpps);
        mpps.putUI(Tags.SOPInstanceUID, iuid);
        return null;
    }
}
