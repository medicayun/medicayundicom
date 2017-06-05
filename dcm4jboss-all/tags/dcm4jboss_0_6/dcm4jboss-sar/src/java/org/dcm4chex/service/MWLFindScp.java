/* $Id: MWLFindScp.java 968 2004-02-11 22:14:42Z gunterze $
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
import java.sql.SQLException;

import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Status;
import org.dcm4che.dict.Tags;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.Association;
import org.dcm4che.net.DcmServiceBase;
import org.dcm4che.net.DcmServiceException;
import org.dcm4che.net.Dimse;
import org.dcm4che.net.DimseListener;
import org.dcm4chex.archive.ejb.jdbc.MWLQueryCmd;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision: 968 $ $Date: 2004-02-12 06:14:42 +0800 (周四, 12 2月 2004) $
 */
public class MWLFindScp extends DcmServiceBase {
    private final MWLFindScpService service;

    public MWLFindScp(MWLFindScpService service) {
        this.service = service;
    }

    protected MultiDimseRsp doCFind(
        ActiveAssociation assoc,
        Dimse rq,
        Command rspCmd)
        throws IOException, DcmServiceException {
        final MWLQueryCmd queryCmd;
        try {
            Dataset rqData = rq.getDataset();
            service.logDataset("Identifier:\n", rqData);
            queryCmd = new MWLQueryCmd(service.getDS(), rqData);
            queryCmd.execute();
        } catch (Exception e) {
            service.getLog().error("Query DB failed:", e);
            throw new DcmServiceException(Status.ProcessingFailure, e);
        }
        return new MultiCFindRsp(queryCmd);
    }

    private class MultiCFindRsp implements MultiDimseRsp {
        private final MWLQueryCmd queryCmd;
        private boolean canceled = false;

        public MultiCFindRsp(MWLQueryCmd queryCmd) {
            this.queryCmd = queryCmd;
        }

        public DimseListener getCancelListener() {
            return new DimseListener() {
                public void dimseReceived(Association assoc, Dimse dimse) {
                    canceled = true;
                }
            };
        }

        public Dataset next(ActiveAssociation assoc, Dimse rq, Command rspCmd)
            throws DcmServiceException
        {
            if (canceled) {
                rspCmd.putUS(Tags.Status, Status.Cancel);
                return null;                
            }
            try {
                if (!queryCmd.next()) {
                    rspCmd.putUS(Tags.Status, Status.Success);
                    return null;
                }
                rspCmd.putUS(Tags.Status, Status.Pending);
                Dataset rspData = queryCmd.getDataset();
                service.logDataset("Identifier:",rspData);
                return rspData;
            } catch (SQLException e) {
                service.getLog().error("Retrieve DB record failed:", e);
                throw new DcmServiceException(Status.ProcessingFailure, e);                
            } catch (Exception e) {
                service.getLog().error("Corrupted DB record:", e);
                throw new DcmServiceException(Status.ProcessingFailure, e);                
            }                        
        }

        public void release() {
            queryCmd.close();
        }
    }
}
