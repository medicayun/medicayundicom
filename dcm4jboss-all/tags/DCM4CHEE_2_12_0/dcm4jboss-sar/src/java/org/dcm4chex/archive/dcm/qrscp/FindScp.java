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

package org.dcm4chex.archive.dcm.qrscp;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.management.ObjectName;

import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.dict.Status;
import org.dcm4che.dict.Tags;
import org.dcm4che.net.AAssociateAC;
import org.dcm4che.net.AAssociateRQ;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.Association;
import org.dcm4che.net.AssociationListener;
import org.dcm4che.net.DcmServiceBase;
import org.dcm4che.net.DcmServiceException;
import org.dcm4che.net.Dimse;
import org.dcm4che.net.DimseListener;
import org.dcm4che.net.PDU;
import org.dcm4chex.archive.ejb.jdbc.QueryCmd;
import org.dcm4chex.archive.perf.PerfCounterEnum;
import org.dcm4chex.archive.perf.PerfMonDelegate;
import org.dcm4chex.archive.perf.PerfPropertyEnum;
import org.jboss.logging.Logger;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision: 4958 $ $Date: 2007-09-06 19:47:56 +0800 (周四, 06 9月 2007) $
 * @since 31.08.2003
 */
public class FindScp extends DcmServiceBase implements AssociationListener {
    private static final int PID = 0;
    private static final int ISSUER = 1;
    private static final String QUERY_XSL = "cfindrq.xsl";
    private static final String RESULT_XSL = "cfindrsp.xsl";
    private static final String QUERY_XML = "-cfindrq.xml";
    private static final String RESULT_XML = "-cfindrsp.xml";

    protected final QueryRetrieveScpService service;
    
    private final boolean filterResult;

	protected final Logger log;
	
    private PerfMonDelegate perfMon;

    public FindScp(QueryRetrieveScpService service, boolean filterResult) {
        this.service = service;
		this.log = service.getLog();
        this.filterResult = filterResult;
        perfMon = new PerfMonDelegate(this.service);
    }
 
    public final ObjectName getPerfMonServiceName() {
		return perfMon.getPerfMonServiceName();
	}

	public final void setPerfMonServiceName(ObjectName perfMonServiceName) {
		perfMon.setPerfMonServiceName(perfMonServiceName);
	}

    protected MultiDimseRsp doCFind(ActiveAssociation assoc, Dimse rq,
            Command rspCmd) throws IOException, DcmServiceException {
        Association as = assoc.getAssociation();
        String callingAET = as.getCallingAET();
        try {
        	perfMon.start(assoc, rq, PerfCounterEnum.C_FIND_SCP_QUERY_DB);
        	
            Dataset rqData = rq.getDataset();
        	perfMon.setProperty(assoc, rq, PerfPropertyEnum.REQ_DIMSE, rq);

            if(log.isDebugEnabled()) {
            	log.debug("Identifier:\n");
            	log.debug(rqData);
            }
            
            Association a = assoc.getAssociation();
            service.logDIMSE(a, QUERY_XML, rqData);
            service.logDicomQuery(a, rq.getCommand().getAffectedSOPClassUID(),
                    rqData);
            Dataset coerce = 
                service.getCoercionAttributesFor(a, QUERY_XSL, rqData);
            if (coerce != null) {
                service.coerceAttributes(rqData, coerce);
            }
            service.supplementIssuerOfPatientID(rqData, callingAET);
            if (!isUniversalMatching(rqData.getString(Tags.PatientID))
                    && service.isPixQueryCallingAET(a.getCallingAET())) {
                pixQuery(rqData);
            }
            MultiDimseRsp rsp = newMultiCFindRsp(rqData);
            perfMon.stop(assoc, rq, PerfCounterEnum.C_FIND_SCP_QUERY_DB);
            return rsp;
        } catch (Exception e) {
            log.error("Query DB failed:", e);
            throw new DcmServiceException(Status.UnableToProcess, e);
        }
    }

    private boolean isUniversalMatching(String key) {
        if (key == null) {
            return true;
        }
        char[] a = key.toCharArray();
        for (int i = 0; i < a.length; i++) {
            if (a[i] != '*') {
                return false;
            }          
        }
        return true;
    }

    private boolean isWildCardMatching(String key) {
        return key.indexOf('*') != -1 || key.indexOf('?') != -1;
    }

    private void pixQuery(Dataset rqData) throws DcmServiceException {
        ArrayList result = new ArrayList();
        boolean updateRQ = pixQuery(rqData, result);
        DcmElement opidsq = rqData.get(Tags.OtherPatientIDSeq);
        if (opidsq != null) {
            for (int i = 0, n = opidsq.countItems(); i < n; i++) {
                if (pixQuery(opidsq.getItem(i), result)) {
                    updateRQ = true;
                }
            }
        }
        if (updateRQ) {
            Iterator iter = result.iterator();
            opidsq = rqData.putSQ(Tags.OtherPatientIDSeq);
            if (iter.hasNext()) {
                setPID(rqData, (String[]) iter.next());
                while (iter.hasNext()) {
                    setPID(opidsq.addNewItem(), (String[]) iter.next());
                }
            }
        }
    }
   
    private void setPID(Dataset rqData, String[] pid) {
        rqData.putLO(Tags.PatientID, pid[PID]);
        rqData.putLO(Tags.IssuerOfPatientID, pid[ISSUER]);       
    }

    private boolean pixQuery(Dataset rqData, ArrayList result)
            throws DcmServiceException {
        String pid = rqData.getString(Tags.PatientID);
        if (isUniversalMatching(pid)) {
            return false;
        }
        String issuer = rqData.getString(Tags.IssuerOfPatientID);       
        if (!service.isPixQueryIssuer(issuer)
                || isWildCardMatching(pid) && !service.isPixQueryLocal()) {
            addNewPidAndIssuerTo(new String[] { pid, issuer }, result);
            return false;
        }
        List l = service.queryCorrespondingPIDs(pid, issuer);
        if (l.isEmpty() && !service.isPixQueryLocal()) {
            addNewPidAndIssuerTo(new String[] { pid, issuer }, result);
            return false;            
        }
        for (Iterator iter = l.iterator(); iter.hasNext();) {
            addNewPidAndIssuerTo((String[]) iter.next(), result);            
        }
        return true;
    }
    
    private boolean addNewPidAndIssuerTo(String[] pidAndIssuer, List result) {
        for (Iterator iter = result.iterator(); iter.hasNext();) {
            String[] e = (String[]) iter.next();
            if (pidAndIssuer[PID].equals(e[PID])
                    && pidAndIssuer[ISSUER].equals(e[ISSUER])) {
                return false;
            }
        }
        result.add(pidAndIssuer);
        return true;
    }

    protected MultiDimseRsp newMultiCFindRsp(Dataset rqData) throws SQLException {
        QueryCmd queryCmd = QueryCmd.create(rqData, filterResult,
                service.isNoMatchForNoValue() );
        queryCmd.execute();
		return new MultiCFindRsp(queryCmd);
	}

	protected Dataset getDataset(QueryCmd queryCmd) throws SQLException, 
    		DcmServiceException {
		return queryCmd.getDataset();
	}
    
    protected void doMultiRsp(ActiveAssociation assoc, Dimse rq, Command rspCmd,
	        MultiDimseRsp mdr)
	    throws IOException, DcmServiceException {
	        try {
	            assoc.addCancelListener(rspCmd.getMessageIDToBeingRespondedTo(),
	                mdr.getCancelListener());
	            
            	do {
                	perfMon.start(assoc, rq, PerfCounterEnum.C_FIND_SCP_RESP_OUT);

	                Dataset rspData = mdr.next(assoc, rq, rspCmd);
	                Dimse rsp = fact.newDimse(rq.pcid(), rspCmd, rspData);
	                assoc.getAssociation().write(rsp);
	                
                	perfMon.setProperty(assoc, rq, PerfPropertyEnum.RSP_DATASET, rspData);
                	perfMon.stop(assoc, rq, PerfCounterEnum.C_FIND_SCP_RESP_OUT);
	                
	                doAfterRsp(assoc, rsp);
	            } while (rspCmd.isPending());
	        } finally {
	            mdr.release();
	        }
   	}

	protected class MultiCFindRsp implements MultiDimseRsp {

        private final QueryCmd queryCmd;

        private boolean canceled = false;
        
        private int pendingStatus = Status.Pending;
        
        public MultiCFindRsp(QueryCmd queryCmd) {
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
                throws DcmServiceException {
            if (canceled) {
                rspCmd.putUS(Tags.Status, Status.Cancel);
                return null;
            }
            try {
                Association a = assoc.getAssociation();
                queryCmd.setCoercePatientIds( service.isCoerceRequestPatientIdsAET(a.getCallingAET()) );
                if (!queryCmd.next()) {
                    rspCmd.putUS(Tags.Status, Status.Success);
                    return null;
                }
                rspCmd.putUS(Tags.Status, pendingStatus);
                Dataset data = getDataset(queryCmd);				
                log.debug("Identifier:\n");
                log.debug(data);
                service.logDIMSE(a , RESULT_XML, data);
                Dataset coerce = 
                    service.getCoercionAttributesFor(a, RESULT_XSL, data);
                if (coerce != null) {
                    service.coerceAttributes(data, coerce);
                }
                return data;
            } catch (DcmServiceException e) {
            	throw e;
            } catch (SQLException e) {
                log.error("Retrieve DB record failed:", e);
                throw new DcmServiceException(Status.UnableToProcess, e);
            } catch (Exception e) {
                log.error("Corrupted DB record:", e);
                throw new DcmServiceException(Status.UnableToProcess, e);
            }
        }

        public void release() {
            queryCmd.close();
        }
    }

	public void write(Association src, PDU pdu) {
    	if (pdu instanceof AAssociateAC)
    		perfMon.assocEstEnd(src, Command.C_FIND_RQ);
	}

	public void received(Association src, PDU pdu) {
    	if(pdu instanceof AAssociateRQ)
    		perfMon.assocEstStart(src, Command.C_FIND_RQ);
	}

	public void write(Association src, Dimse dimse) {
	}

	public void received(Association src, Dimse dimse) {
	}

	public void error(Association src, IOException ioe) {
	}

	public void closing(Association assoc) {
    	if(assoc.getAAssociateAC() != null)
    		perfMon.assocRelStart(assoc, Command.C_FIND_RQ);
	}

	public void closed(Association assoc) {
    	if(assoc.getAAssociateAC() != null)
    		perfMon.assocRelEnd(assoc, Command.C_FIND_RQ);
	}
}
