/* $Id: FindScp.java 1864 2005-07-19 15:45:39Z gunterze $
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

package org.dcm4chex.archive.dcm.qrscp;

import java.io.IOException;
import java.sql.SQLException;

import org.dcm4che.auditlog.AuditLoggerFactory;
import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Status;
import org.dcm4che.dict.Tags;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.Association;
import org.dcm4che.net.DcmServiceBase;
import org.dcm4che.net.DcmServiceException;
import org.dcm4che.net.Dimse;
import org.dcm4che.net.DimseListener;
import org.dcm4chex.archive.ejb.jdbc.QueryCmd;
import org.jboss.logging.Logger;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision: 1864 $ $Date: 2005-07-19 23:45:39 +0800 (周二, 19 7月 2005) $
 * @since 31.08.2003
 */
public class FindScp extends DcmServiceBase {

    private final AuditLoggerFactory alf = AuditLoggerFactory.getInstance();

    private final QueryRetrieveScpService service;
    
    private final boolean filterResult;

    private final boolean blockResults;
	
	private Logger log;

    public FindScp(QueryRetrieveScpService service, boolean filterResult,
			boolean blockResults) {
        this.service = service;
		this.log = service.getLog();
        this.filterResult = filterResult;
		this.blockResults = blockResults;
    }

    protected MultiDimseRsp doCFind(ActiveAssociation assoc, Dimse rq,
            Command rspCmd) throws IOException, DcmServiceException {
        final QueryCmd queryCmd;
        try {
            Dataset rqData = rq.getDataset();
			log.debug("Identifier:\n");
			log.debug(rqData);
            logDicomQuery(assoc.getAssociation(), rq.getCommand(), rqData);
            queryCmd = QueryCmd.create(rqData, filterResult);
            queryCmd.execute();
        } catch (Exception e) {
            log.error("Query DB failed:", e);
            throw new DcmServiceException(Status.ProcessingFailure, e);
        }
        return new MultiCFindRsp(queryCmd);
    }

    void logDicomQuery(Association assoc, Command cmd, Dataset keys) {
        service.logDicomQuery(keys, alf.newRemoteNode(assoc.getSocket(), assoc
                .getCallingAET()), cmd.getAffectedSOPClassUID());
    }

    private class MultiCFindRsp implements MultiDimseRsp {

        private final QueryCmd queryCmd;

        private boolean canceled = false;
        
        public MultiCFindRsp(QueryCmd queryCmd) {
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
                throws DcmServiceException {
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
                Dataset data = queryCmd.getDataset();
				if (blockResults) {
					Dataset parent = DcmObjectFactory.getInstance().newDataset();
					DcmElement sq = parent.putSQ(Tags.DirectoryRecordSeq);
					sq.addItem(data);
					for (int i = 1, n = service.getMaxBlockedFindRSP();
							i <= n && queryCmd.next(); ++i) {
						sq.addItem(queryCmd.getDataset());
					}
					data = parent;
				}				
				log.debug("Identifier:\n");
				log.debug(data);
                return data;
            } catch (SQLException e) {
                log.error("Retrieve DB record failed:", e);
                throw new DcmServiceException(Status.ProcessingFailure, e);
            } catch (Exception e) {
                log.error("Corrupted DB record:", e);
                throw new DcmServiceException(Status.ProcessingFailure, e);
            }
        }

        public void release() {
            queryCmd.close();
        }
    }
}
