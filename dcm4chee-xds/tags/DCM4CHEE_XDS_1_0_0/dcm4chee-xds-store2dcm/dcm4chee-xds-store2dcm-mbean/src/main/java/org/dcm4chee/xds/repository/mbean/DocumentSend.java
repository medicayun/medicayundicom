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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import org.dcm4che2.data.UIDDictionary;
import org.dcm4che2.io.DicomOutputStream;
import org.dcm4che2.net.Association;
import org.dcm4che2.net.ConfigurationException;
import org.dcm4che2.net.DimseRSPHandler;
import org.dcm4che2.net.NoPresentationContextException;
import org.dcm4che2.net.PDVOutputStream;
import org.dcm4che2.net.TransferCapability;
import org.dcm4che2.net.UserIdentity;
import org.dcm4che2.net.service.StorageCommitmentService;
import org.dcm4che2.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DocumentSend extends StorageCommitmentService {

    private static final String[] ONLY_IVLE_TS = { 
        UID.ImplicitVRLittleEndian
    };

    private static final String[] IVLE_TS = { 
        UID.ImplicitVRLittleEndian,
        UID.ExplicitVRLittleEndian, 
        UID.ExplicitVRBigEndian,
    };

    private static final String[] EVLE_TS = {
        UID.ExplicitVRLittleEndian,
        UID.ImplicitVRLittleEndian,
        UID.ExplicitVRBigEndian, 
    };

    private HashMap as2ts = new HashMap();
    
    private DocumentSendCfg dcmCfg;

    private Association assoc;

    private int priority = 0;
    
    
    static Logger log = LoggerFactory.getLogger(DocumentSend.class);
    
    
    public DocumentSend(DocumentSendCfg dcmCfg) {
    	this.dcmCfg = dcmCfg;
    }

    public byte[] sendDocument(Store2Dcm store) {

        if (dcmCfg.getUsername() != null) {
            UserIdentity userId;
            if (dcmCfg.getPasscode()!=null) {
                userId = new UserIdentity.UsernamePasscode(dcmCfg.getUsername(),
                		dcmCfg.getPasscode().toCharArray());
            } else {
                userId = new UserIdentity.Username(dcmCfg.getUsername());
            }
            userId.setPositiveResponseRequested(dcmCfg.isUidnegrsp());
            dcmCfg.setUserIdentity(userId);
        }
        long t1 = System.currentTimeMillis();
        addTransferCapability(store.getSOPClassUID(), store.getTransferSyntax());
        configureTransferCapability();
        if ( dcmCfg.isUseTLS() ) {
	        try {
	            dcmCfg.initTLS();
	        } catch (Exception e) {
	            log.error("Failed to initialize TLS context:"+ e.getMessage(), e);
	            return null;
	        }
        }
        long t2 = System.currentTimeMillis();
        t1 = System.currentTimeMillis();
        try {
            open();
        } catch (Exception e) {
            log.error("ERROR: Failed to establish association:"
                    + e.getMessage(), e);
            return null;
        }
        t2 = System.currentTimeMillis();
        log.info("Connected to " + dcmCfg.getCalledAET() + " in " 
                + ((t2 - t1) / 1000F) + "s");

        t1 = System.currentTimeMillis();
        byte[] hash = send(store);
        t2 = System.currentTimeMillis();
        if ( store.isStored() ) {
        	return hash;
        }
        return null;
    }

    private void addTransferCapability(String cuid, String tsuid) {
        log.debug("addTransferCapability cuid:"+cuid+" ts:"+tsuid);
        HashSet ts = (HashSet) as2ts.get(cuid);
        if (ts == null) {
            ts = new HashSet();
            ts.add(UID.ImplicitVRLittleEndian);
            as2ts.put(cuid, ts);
        }
        ts.add(tsuid);
    }

    private void configureTransferCapability() {
        TransferCapability[] tc = new TransferCapability[as2ts.size()+1];
        tc[0] = new TransferCapability(
                    UID.StorageCommitmentPushModelSOPClass,
                    ONLY_IVLE_TS, 
                    TransferCapability.SCU);
        Iterator iter = as2ts.entrySet().iterator();
        for (int i = 1; i < tc.length; i++) {
            Map.Entry e = (Map.Entry) iter.next();
            String cuid = (String) e.getKey();
            HashSet ts = (HashSet) e.getValue();
            tc[i] = new TransferCapability(cuid, 
                    (String[]) ts.toArray(new String[ts.size()]),
                    TransferCapability.SCU);
            String[] tsa = tc[i].getTransferSyntax();
        }
        this.dcmCfg.getAE().setTransferCapability(tc);
    }

    public Association open() throws IOException, ConfigurationException,
            InterruptedException {
        assoc = dcmCfg.connect();
        return assoc;
    }

    public Association getActiveAssociation() {
		return assoc;
	}

	public byte[] send(final Store2Dcm store) {
    	log.info("assoc:"+assoc);
        TransferCapability tc = assoc.getTransferCapabilityAsSCU(store.getSOPClassUID());
        if (tc == null) {
        	log.warn(UIDDictionary.getDictionary().prompt(
                    store.getSOPClassUID())
                    + " not supported by " + dcmCfg.getCalledAET());
            return null;
        }
        String tsuid = selectTransferSyntax(tc.getTransferSyntax(),
                store.getTransferSyntax());
        if (tsuid == null) {
            log.warn(UIDDictionary.getDictionary().prompt(
                    store.getSOPClassUID())
                    + " with "
                    + UIDDictionary.getDictionary().prompt(store.getTransferSyntax())
                    + " not supported by" + dcmCfg.getCalledAET());
            return null;
        }
        DataWriter dw = new DataWriter(store);
        try {
            DimseRSPHandler rspHandler = new DimseRSPHandler() {
                public void onDimseRSP(Association as, DicomObject cmd,
                        DicomObject data) {
                    DocumentSend.this.onDimseRSP(as, cmd, data, store);
                }
            };
            assoc.cstore(store.getSOPClassUID(), store.getSOPInstanceUID(), priority,
                    dw, tsuid, rspHandler);
        } catch (NoPresentationContextException e) {
            log.warn("WARNING: " + e.getMessage()
                    + " - cannot send encapsulated document");
            return null;
        } catch (IOException e) {
            log.error("ERROR: Failed to send encapsulated document:"
                    + e.getMessage(), e);
            return null;
        } catch (InterruptedException e) {
            // should not happen
            e.printStackTrace();
            return null;
        }
        try {
            assoc.waitForDimseRSP();
        } catch (InterruptedException e) {
            // should not happen
            e.printStackTrace();
        }
        return dw.getHash();
    }

    private String selectTransferSyntax(String[] available, String tsuid) {
        if (tsuid.equals(UID.ImplicitVRLittleEndian))
            return selectTransferSyntax(available, IVLE_TS);
        if (tsuid.equals(UID.ExplicitVRLittleEndian))
            return selectTransferSyntax(available, EVLE_TS);
        return tsuid;
    }

    private String selectTransferSyntax(String[] available, String[] tsuids) {
        for (int i = 0; i < tsuids.length; i++)
            for (int j = 0; j < available.length; j++)
                if (available[j].equals(tsuids[i]))
                    return available[j];
        return null;
    }

    public void close() {
        try {
        	if ( assoc != null ) {
	            assoc.release(false);
	            log.info("Released connection to " + dcmCfg.getCalledAET());
        	} else {
        		log.info("Try to close non existing association!");
        	}
        } catch (InterruptedException ignore) {}
    }


    private class DataWriter implements org.dcm4che2.net.DataWriter {

        private Store2Dcm store;
        private byte[] hash;

        public DataWriter(Store2Dcm store) {
            this.store = store;
        }
        
        public byte[] getHash() {
        	return hash;
        }

        public void writeTo(PDVOutputStream out, String tsuid)
                throws IOException {
                try {
                    DicomOutputStream dos = new DicomOutputStream(out);
                    hash = store.encapsulate(dos, false);
                } catch (Exception x) {
                    if ( x instanceof IOException ) throw (IOException)x;
                    throw new IOException("Encapsulate Document in DICOM object failed:"+x.getMessage());
                }
        }

    }

    private void promptErrRSP(String prefix, int status, Store2Dcm store,
            DicomObject cmd) {
        log.warn(prefix + StringUtils.shortToHex(status) + "H for iuid:"
                + store.getSOPInstanceUID() + ", cuid=" + store.getSOPClassUID()
                + ", tsuid=" + store.getTransferSyntax());
        log.warn(cmd.toString());
    }

    private void onDimseRSP(Association as, DicomObject cmd, DicomObject data, Store2Dcm store) {
        int status = cmd.getInt(Tag.Status);
        int msgId = cmd.getInt(Tag.MessageIDBeingRespondedTo);
        store.setRspStatus(status);
    	log.info("Document sent! Received RSP Status:"+status);
        switch (status) {
        case 0: 
        	store.setStored(true);
        	break;
        case 0xB000:
        case 0xB006:
        case 0xB007:
        	store.setStored(true);
        	promptErrRSP("WARNING: Received RSP with Status ", status, store, cmd);
            break;
        default:
            promptErrRSP("ERROR: Received RSP with Status ", status, store, cmd);
        }
    }
}
