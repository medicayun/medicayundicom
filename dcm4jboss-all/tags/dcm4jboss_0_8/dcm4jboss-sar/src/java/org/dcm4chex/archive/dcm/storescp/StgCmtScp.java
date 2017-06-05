/* $Id: StgCmtScp.java 1119 2004-05-21 09:50:27Z gunterze $
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
package org.dcm4chex.archive.dcm.storescp;

import java.io.IOException;

import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Status;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.Association;
import org.dcm4che.net.DcmServiceBase;
import org.dcm4che.net.DcmServiceException;
import org.dcm4che.net.Dimse;

/**
 * 
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1119 $ $Date: 2004-05-21 17:50:27 +0800 (周五, 21 5月 2004) $
 * @since 01.03.2004
 */
class StgCmtScp extends DcmServiceBase {
    
    private final StoreScpService service;

    /**
     * @param service
     */
    public StgCmtScp(StoreScpService service) {
        this.service = service;
    }

    /* (non-Javadoc)
     * @see org.dcm4che.net.DcmServiceBase#doNAction(org.dcm4che.net.ActiveAssociation, org.dcm4che.net.Dimse, org.dcm4che.data.Command)
     */
    protected Dataset doNAction(
        ActiveAssociation assoc,
        Dimse rq,
        Command rspCmd)
        throws IOException, DcmServiceException {
        Command cmd = rq.getCommand();
        Dataset data = rq.getDataset();
        service.logDataset("StgCmt Request:\n", data);
        if (!UIDs
            .StorageCommitmentPushModelSOPInstance
            .equals(cmd.getRequestedSOPInstanceUID())) {
            throw new DcmServiceException(Status.NoSuchObjectInstance);
        }
        if (cmd.getInt(Tags.ActionTypeID, -1) != 1) {
            throw new DcmServiceException(Status.NoSuchActionType);
        }
        Association a = assoc.getAssociation();
        a.putProperty("StgCmtCmd-" + cmd.getMessageID(), new StgCmtCmd(service, a, data));
        return null;
    }

    private void check(Command cmd)
        throws DcmServiceException {
    }


    protected void doAfterRsp(ActiveAssociation assoc, Dimse rsp) {
        Association a = assoc.getAssociation();
        Command cmd = rsp.getCommand();
        String key = "StgCmtCmd-" + cmd.getMessageIDToBeingRespondedTo();
        final StgCmtCmd stgCmtCmd = (StgCmtCmd) a.getProperty(key);
        if (stgCmtCmd != null) {
        	new Thread(new Runnable() {

				public void run() {
					stgCmtCmd.execute();
				}}).start();
        }
    }

}
