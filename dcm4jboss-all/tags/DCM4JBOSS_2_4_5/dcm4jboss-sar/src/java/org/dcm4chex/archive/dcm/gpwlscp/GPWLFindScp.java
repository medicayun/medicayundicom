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
import org.dcm4chex.archive.ejb.jdbc.GPWLQueryCmd;
import org.jboss.logging.Logger;

/**
 * @author gunter.zeilinger@tiani.com
 * @version Revision $Date: 2005-06-22 15:47:41 +0800 (周三, 22 6月 2005) $
 * @since 03.04.2005
 */

class GPWLFindScp extends DcmServiceBase {
    private final GPWLScpService service;
	private final Logger log;

    public GPWLFindScp(GPWLScpService service) {
        this.service = service;
		this.log = service.getLog();
    }

    protected MultiDimseRsp doCFind(
        ActiveAssociation assoc,
        Dimse rq,
        Command rspCmd)
        throws IOException, DcmServiceException {
        final GPWLQueryCmd queryCmd;
        try {
            Dataset rqData = rq.getDataset();
			log.debug("Identifier:\n");
			log.debug(rqData);
            queryCmd = new GPWLQueryCmd(rqData);
            queryCmd.execute();
        } catch (Exception e) {
            service.getLog().error("Query DB failed:", e);
            throw new DcmServiceException(Status.ProcessingFailure, e);
        }
        return new MultiCFindRsp(queryCmd);
    }

    private class MultiCFindRsp implements MultiDimseRsp {
        private final GPWLQueryCmd queryCmd;
        private boolean canceled = false;

        public MultiCFindRsp(GPWLQueryCmd queryCmd) {
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
				log.debug("Identifier:\n");
				log.debug(rspData);
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
