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

package org.dcm4chex.archive.dcm.mwlscp;

import java.io.IOException;
import java.sql.SQLException;

import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Status;
import org.dcm4che.dict.Tags;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.Association;
import org.dcm4che.net.DcmServiceBase;
import org.dcm4che.net.DcmServiceException;
import org.dcm4che.net.Dimse;
import org.dcm4che.net.DimseListener;
import org.dcm4chex.archive.ejb.jdbc.MWLQueryCmd;
import org.jboss.logging.Logger;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision: 2766 $ $Date: 2006-09-19 17:17:23 +0800 (周二, 19 9月 2006) $
 */
public class MWLFindScp extends DcmServiceBase {
    private static final String QUERY_XSL = "mwl-cfindrq.xsl";
    private static final String RESULT_XSL = "mwl-cfindrsp.xsl";
    private static final String QUERY_XML = "-mwl-cfindrq.xml";
    private static final String RESULT_XML = "-mwl-cfindrsp.xml";
    
    private final MWLFindScpService service;
	private final Logger log;

    public static int transactionIsolationLevel = 0;

    protected static final DcmObjectFactory dof = DcmObjectFactory.getInstance();
	
	public MWLFindScp(MWLFindScpService service) {
        this.service = service;
		this.log = service.getLog();
    }
	
	protected MultiDimseRsp doCFind(ActiveAssociation assoc, Dimse rq,
            Command rspCmd) throws IOException, DcmServiceException {
        Association a = assoc.getAssociation();
        Dataset rqData = rq.getDataset();
        log.debug("Identifier:\n");
        log.debug(rqData);
        service.logDIMSE(a, QUERY_XML, rqData);
        Dataset coerce = service.getCoercionAttributesFor(a, QUERY_XSL, rqData);
        if (coerce != null) {
            service.coerceAttributes(rqData, coerce);
        }

        MWLQueryCmd queryCmd;
        try {
            queryCmd = new MWLQueryCmd(rqData);
            queryCmd.execute();
        } catch (Exception e) {
            log.error("Query DB failed:", e);
            throw new DcmServiceException(Status.ProcessingFailure, e);
        }
        return new MultiCFindRsp(queryCmd);
    }

	private class MultiCFindRsp implements MultiDimseRsp {
        private final MWLQueryCmd queryCmd;
        private boolean canceled = false;
        private int pendingStatus = Status.Pending;

        public MultiCFindRsp(MWLQueryCmd queryCmd) {
            this.queryCmd = queryCmd;
            if ( queryCmd.isMatchNotSupported() ) { 
                pendingStatus = 0xff01;
            } else if ( service.isCheckMatchingKeySupported() && queryCmd.isMatchingKeyNotSupported() ) {
                pendingStatus = 0xff01;
            }
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
        		Association a = assoc.getAssociation();
                rspCmd.putUS(Tags.Status, pendingStatus);
                Dataset rspData = queryCmd.getDataset();
				log.debug("Identifier:\n");
				log.debug(rspData);
                service.logDIMSE(a, RESULT_XML, rspData);
                Dataset coerce = 
                    service.getCoercionAttributesFor(a, RESULT_XSL, rspData);
                if (coerce != null) {
                    service.coerceAttributes(rspData, coerce);
                }
                return rspData;
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
