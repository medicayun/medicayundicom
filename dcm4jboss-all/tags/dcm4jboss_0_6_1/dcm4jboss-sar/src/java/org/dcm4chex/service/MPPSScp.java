/* $Id: MPPSScp.java 1036 2004-03-10 19:28:43Z gunterze $
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
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.Association;
import org.dcm4che.net.DcmServiceBase;
import org.dcm4che.net.DcmServiceException;
import org.dcm4che.net.Dimse;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision: 1036 $ $Date: 2004-03-11 03:28:43 +0800 (周四, 11 3月 2004) $
 */
class MPPSScp extends DcmServiceBase {
    private final MPPSScpService service;

    public MPPSScp(MPPSScpService service) {
        this.service = service;
    }

    protected Dataset doNCreate(
        ActiveAssociation assoc,
        Dimse rq,
        Command rspCmd)
        throws IOException, DcmServiceException {
        Command cmd = rq.getCommand();
        Dataset mpps = rq.getDataset();
        service.logDataset("Create MPPS:\n", mpps);
        if (service.isForward()) {
            Association a = assoc.getAssociation();
            a.putProperty(
                "MPPSForwardCmd-" + cmd.getMessageID(),
                new MPPSForwardCmd.NCreate(
                    service,
                    rspCmd.getAffectedSOPInstanceUID(),
                    mpps));
        }
        return null;
    }

    protected Dataset doNSet(ActiveAssociation assoc, Dimse rq, Command rspCmd)
        throws IOException, DcmServiceException {
        Command cmd = rq.getCommand();
        Dataset mpps = rq.getDataset();
        service.logDataset("Set MPPS:\n", mpps);
        if (service.isForward()) {
            Association a = assoc.getAssociation();
            a.putProperty(
                "MPPSForwardCmd-" + cmd.getMessageID(),
                new MPPSForwardCmd.NSet(
                    service,
                    cmd.getRequestedSOPInstanceUID(),
                    mpps));
        }
        return null;
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
