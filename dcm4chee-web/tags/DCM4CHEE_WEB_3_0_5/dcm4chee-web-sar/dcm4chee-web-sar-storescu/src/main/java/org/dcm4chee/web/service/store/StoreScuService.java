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
package org.dcm4chee.web.service.store;

import java.io.IOException;
import java.security.GeneralSecurityException;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import org.dcm4che2.data.UIDDictionary;
import org.dcm4che2.net.Association;
import org.dcm4che2.net.DataWriterAdapter;
import org.dcm4che2.net.DimseRSPHandler;
import org.dcm4che2.net.NoPresentationContextException;
import org.dcm4che2.net.TransferCapability;
import org.dcm4che2.util.StringUtils;
import org.dcm4chee.web.service.common.AbstractScheduledScuService;
import org.dcm4chee.web.service.common.DicomActionOrder;

/**
 * @author bernhard.ableitinger@gmail.com
 * @version $Revision$ $Date$
 * @since Jan 10, 2012
 */
public class StoreScuService extends AbstractScheduledScuService {

    private String[] calledAETs;
    
    public StoreScuService() {
        super();
    }

    public String getCalledAETs() {
        return calledAETs == null ? NONE : StringUtils.join(calledAETs, '\\');
    }

    public void setCalledAETs(String calledAET) {
        this.calledAETs = NONE.equals(calledAET) ? null : StringUtils.split(calledAET, '\\');
    }
    
    public void schedule(DicomObject dataset) throws Exception {
        if (calledAETs != null) {
            for ( String aet : calledAETs) {
                schedule(new DicomActionOrder(aet, dataset, "Store Dataset"));
            }
        }
        else
        {
            LOG.warn("Failed to schedule dataset(s) for storage! No or invalid calledAETs configured...");
        }
    }
    
    public boolean store(DicomObject dataset)
    {
    	return store(null, dataset);
    }
    
    public boolean store(String cuid, DicomObject dataset)
    {
        boolean suc = true;
        if (calledAETs != null) {
            for ( String aet : calledAETs) {
               try
               {
                   if (store(aet, cuid, dataset)!=0)
                   {
                       suc = false;
                   }
               }
               catch (Exception e)
               {
                   suc = false;
               }
            }
        }
        else
        {
            LOG.warn("Failed to schedule dataset(s) for storage! No or invalid calledAETs configured...");
        }
        return suc;
    }
    
    @Override
    public void process(DicomActionOrder order) throws Exception {
        this.store(order.getDestAET(), order.getDicomObject());
    }
    
    private int store(String aet, String cuid, DicomObject dataset) throws IOException, InterruptedException, GeneralSecurityException {
        if (cuid==null) {
        	cuid = dataset.getString(Tag.SOPClassUID);
        }

        setTransferCapability(new TransferCapability[]{
                new TransferCapability(cuid,NATIVE_LE_TS, TransferCapability.SCU)});
        Association assoc = open(aet);
        TransferCapability tc = assoc.getTransferCapabilityAsSCU(cuid);
        if ( tc == null ) {
            throw new NoPresentationContextException(UIDDictionary.getDictionary().prompt(UID.KeyObjectSelectionDocumentStorage));
        }
        String tsuid = tc.getTransferSyntax()[0];
        LOG.debug("Send C-STORE request to {}:\n{}", aet, dataset);
        RspHandler rspHandler = new RspHandler();
        assoc.cstore(cuid, dataset.getString(Tag.SOPInstanceUID), priority, 
                new DataWriterAdapter(dataset), tsuid, rspHandler);
        assoc.waitForDimseRSP();
        try {
            assoc.release(true);
        } catch (InterruptedException t) {
            LOG.error("Failed to release association! aet:"+aet, t);
        }
        return rspHandler.getStatus();
    }


    private class RspHandler extends DimseRSPHandler {
        private int status;

        public int getStatus() {
            return status;
        }

        @Override
        public void onDimseRSP(Association as, DicomObject cmd,
                DicomObject data) {
            int status = cmd.getInt(Tag.Status);
            switch (status) {
            case 0:
            {
                LOG.debug("Dataset stored at {}",as.getCalledAET());
                break;
            }
            case 0xA700:
            {
                LOG.error("Failed to store dataset - Out Of Resources! (status: {}H, calledAET:{}, comment:{})", 
                        new Object[] {StringUtils.shortToHex(status), as.getCalledAET(), cmd.getString(Tag.ErrorComment)});
                break;
            }
            case 0xA900:
            {
                LOG.error("Failed to store dataset - Dataset doesn't match SOP class! (status: {}H, calledAET:{}, comment:{})", 
                        new Object[] {StringUtils.shortToHex(status), as.getCalledAET(), cmd.getString(Tag.ErrorComment)});
                break;
            }
            case 0xC000:
            {
                LOG.error("Failed to store dataset - Can't understand! (status: {}H, calledAET:{}, comment:{})", 
                        new Object[] {StringUtils.shortToHex(status), as.getCalledAET(), cmd.getString(Tag.ErrorComment)});
                break;
            }
            case 0xB000:
            case 0xB006:
            case 0xB007:
                LOG.warn("Dataset stored at {} with status {}H",as.getCalledAET(), StringUtils.shortToHex(status));
                break;
            default:
                LOG.error("Failed to store dataset! (status: {}H, calledAET:{}, comment:{})", 
                        new Object[] {StringUtils.shortToHex(status), as.getCalledAET(), cmd.getString(Tag.ErrorComment)});
            }
        }
    }

}

