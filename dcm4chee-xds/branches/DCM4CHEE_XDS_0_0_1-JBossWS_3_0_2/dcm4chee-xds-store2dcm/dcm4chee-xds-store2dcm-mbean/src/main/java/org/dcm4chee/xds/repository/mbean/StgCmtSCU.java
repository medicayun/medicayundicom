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
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Gunter Zeilinger, Huetteldorferstr. 24/10, 1150 Vienna/Austria/Europe.
 * Portions created by the Initial Developer are Copyright (C) 2002-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunterze@gmail.com>
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

package org.dcm4chee.xds.repository.mbean;

import java.io.IOException;
import java.util.HashMap;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import org.dcm4che2.data.VR;
import org.dcm4che2.net.Association;
import org.dcm4che2.net.DimseRSP;
import org.dcm4che2.net.Executor;
import org.dcm4che2.net.NetworkConnection;
import org.dcm4che2.net.NewThreadExecutor;
import org.dcm4che2.net.NoPresentationContextException;
import org.dcm4che2.net.service.StorageCommitmentService;
import org.dcm4che2.util.StringUtils;
import org.dcm4che2.util.UIDUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StgCmtSCU extends StorageCommitmentService {


    private static final int STG_CMT_ACTION_TYPE = 1;

    private Executor executor = new NewThreadExecutor("XDS_STORE");
    private DocumentSendCfg dcmCfg;
    private HashMap waitObjects= new HashMap();

	private boolean requestStgCmt;
	private boolean stgCmtSynchronized;
    private long stgCmtTimeout = 10000L;
    
	private DicomObject stgCmtResult;

    private static Logger log = LoggerFactory.getLogger(StgCmtSCU.class);
    
    public StgCmtSCU(DocumentSendCfg dcmCfg) {
    	this.dcmCfg = dcmCfg;
    	dcmCfg.getAE().register(this);
    }

	public boolean isRequestStgCmt() {
		return requestStgCmt;
	}

	public boolean setRequestStgCmt(boolean requestStgCmt) throws IOException {
		boolean changed = this.requestStgCmt != requestStgCmt;
		this.requestStgCmt = requestStgCmt;
		return changed;
	}

	public boolean isStgCmtSynchronized() {
		return stgCmtSynchronized;
	}

	public void setStgCmtSynchronized(boolean stgCmtSynchronized) {
		this.stgCmtSynchronized = stgCmtSynchronized;
	}

    public long getStgCmtTimeout() {
		return stgCmtTimeout;
	}

	public void setStgCmtTimeout(long timeout) {
		this.stgCmtTimeout = timeout;
	}

    
    public synchronized DicomObject waitForStgCmtResult(Store2Dcm store) throws InterruptedException {
    	log.debug("--- WAIT for StgCmt Result ---");
     	synchronized (store) {
     		try {
     			wait(this.stgCmtTimeout);
     	    	log.debug("--- WAIT DONE ---");
     		} catch ( InterruptedException ignore ) {
     		}
    	}
		if ( stgCmtResult != null ) {
 	        DicomElement refSOPSq = stgCmtResult.get(Tag.ReferencedSOPSequence);
 	        String iuid;
 	        for ( int i = 0 ; i < refSOPSq.countItems() ; i++ ) {
 	        	iuid = refSOPSq.getDicomObject(i).getString(Tag.ReferencedSOPInstanceUID);
 	        	log.debug("+++ check iuid of STGCMT RSP:"+iuid+ "=?"+store.getSOPInstanceUID()	);
 	        	if ( iuid.equals(store.getSOPInstanceUID() ) ) {
 	            	log.debug("+++ STGCMT of "+iuid	);
 	        		store.setCommitted(true);
 	        	}
 	        }
 	        if ( !store.isCommitted() ) {
 	        	log.debug("+++ STG CMT not for this IUID! notifyAll +++");
 	        	notifyAll();
 	        	waitForStgCmtResult(store);
 	        }
		} else {
			//TODO: retry
		}
        return stgCmtResult;
    }


    public void startStgCmtListener() throws IOException { 
    	NetworkConnection conn = dcmCfg.getConn();
        if (conn.getServer() == null && conn.isListening()) {
            conn.bind(executor );
            log.info("Start Server listening on port " + conn.getPort());
        }
    }

    public void stopStgCmtListener() {
    	NetworkConnection conn = dcmCfg.getConn();
        if (conn.isListening()) {
            try {
                Thread.sleep(dcmCfg.getShutdownDelay());
            } catch (InterruptedException ignore) {} // Should not happen
            conn.unbind();
        	log.info("Stop Server listening on port " + conn.getPort());
        }
    }
    
    public String requestStgCmt(Association assoc, String cuid, String iuid) {
        long t1 = System.currentTimeMillis();
    	String tuid = sendStgCmtRq(assoc, cuid, iuid);
        long t2 = System.currentTimeMillis();
        log.info("--- Request Storage Commitment from " 
                + dcmCfg.getCalledAET() + " in " + ((t2 - t1) / 1000F) + "s. Transaction UID:"+tuid );
        return tuid;
    }
    private String sendStgCmtRq(Association assoc, String cuid, String iuid) {
        DicomObject actionInfo = new BasicDicomObject();
        String tuid = UIDUtils.createUID();
        actionInfo.putString(Tag.TransactionUID, VR.UI, tuid);
        DicomElement refSOPSq = actionInfo.putSequence(Tag.ReferencedSOPSequence);
        BasicDicomObject refSOP = new BasicDicomObject();
        refSOP.putString(Tag.ReferencedSOPClassUID, VR.UI, cuid);
        refSOP.putString(Tag.ReferencedSOPInstanceUID, VR.UI, iuid);
        refSOPSq.addDicomObject(refSOP);
        try {
        	log.debug("+++ Send N-ACTION for StgCmt +++");
            DimseRSP rsp = assoc.naction(UID.StorageCommitmentPushModelSOPClass,
                UID.StorageCommitmentPushModelSOPInstance, STG_CMT_ACTION_TYPE,
                actionInfo, UID.ImplicitVRLittleEndian);
        	log.debug("+++ receive N-ACTION +++ rsp:"+rsp);
            rsp.next();
            DicomObject cmd = rsp.getCommand();
            int status = cmd.getInt(Tag.Status);
            if (status == 0) {
                return tuid;
            }
            log.warn("WARNING: Storage Commitment request failed with status: "
                    + StringUtils.shortToHex(status) + "H");
            log.warn(cmd.toString());
        } catch (NoPresentationContextException e) {
        	log.warn("WARNING: " + e.getMessage()
                    + " - cannot request Storage Commitment");
        } catch (IOException e) {
            e.printStackTrace();
            log.warn(
                    "ERROR: Failed to send Storage Commitment request: "
                    + e.getMessage());
        } catch (InterruptedException e) {
            // should not happen
            e.printStackTrace();
        }
        return null;
    }
    
    protected synchronized void onNEventReportRSP(Association as, int pcid,
            DicomObject rq, DicomObject info, DicomObject rsp) {
    	log.info("--- RECEIVE N-EVENT REPORT ---");
        stgCmtResult = info;
    	notifyAll();
    }

}
