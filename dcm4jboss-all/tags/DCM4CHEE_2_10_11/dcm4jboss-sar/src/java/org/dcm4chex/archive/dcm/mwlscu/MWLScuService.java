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

package org.dcm4chex.archive.dcm.mwlscu;

import java.io.IOException;
import java.net.Socket;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import javax.ejb.CreateException;
import javax.management.ObjectName;

import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Status;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.AAssociateAC;
import org.dcm4che.net.AAssociateRQ;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.Association;
import org.dcm4che.net.AssociationFactory;
import org.dcm4che.net.Dimse;
import org.dcm4che.net.FutureRSP;
import org.dcm4che.net.PDU;
import org.dcm4chex.archive.config.DicomPriority;
import org.dcm4chex.archive.ejb.interfaces.MWLManager;
import org.dcm4chex.archive.ejb.interfaces.MWLManagerHome;
import org.dcm4chex.archive.ejb.jdbc.AECmd;
import org.dcm4chex.archive.ejb.jdbc.AEData;
import org.dcm4chex.archive.ejb.jdbc.MWLQueryCmd;
import org.dcm4chex.archive.mbean.TLSConfigDelegate;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.dcm4chex.archive.util.HomeFactoryException;
import org.jboss.system.ServiceMBeanSupport;

/**
 * @author franz.willer
 * @version $Revision: 3086 $ $Date: 2007-01-23 00:42:10 +0800 (周二, 23 1月 2007) $ MBean to configure
 *          and service modality worklist managment issues.
 *          <p>
 * 
 */
public class MWLScuService extends ServiceMBeanSupport {

    /** Holds the calling AET. */
    private String callingAET;

    /** Holds the AET of modality worklist service. */
    private String calledAET;

    /** Holds Association timeout in ms. */
    private int acTimeout;

    /** Holds DICOM message timeout in ms. */
    private int dimseTimeout;

    /** Holds max PDU length in bytes. */
    private int maxPDUlen = 16352;

    /** Holds socket close delay in ms. */
    private int soCloseDelay;

    /** DICOM priority. Used for move and media creation action. */
    private int priority = 0;

    private static final String[] NATIVE_TS = { UIDs.ExplicitVRLittleEndian,
            UIDs.ImplicitVRLittleEndian };

    private TLSConfigDelegate tlsConfig = new TLSConfigDelegate(this);

    private MWLManager mwlManager;

    public final ObjectName getTLSConfigName() {
        return tlsConfig.getTLSConfigName();
    }

    public final void setTLSConfigName(ObjectName tlsConfigName) {
        tlsConfig.setTLSConfigName(tlsConfigName);
    }

    public final int getReceiveBufferSize() {
        return tlsConfig.getReceiveBufferSize();
    }

    public final void setReceiveBufferSize(int size) {
        tlsConfig.setReceiveBufferSize(size);
    }

    public final int getSendBufferSize() {
        return tlsConfig.getSendBufferSize();
    }

    public final void setSendBufferSize(int size) {
        tlsConfig.setSendBufferSize(size);
    }

    public final boolean isTcpNoDelay() {
        return tlsConfig.isTcpNoDelay();
    }

    public final void setTcpNoDelay(boolean on) {
        tlsConfig.setTcpNoDelay(on);
    }

    /**
     * Returns the calling AET defined in this MBean.
     * 
     * @return The calling AET.
     */
    public final String getCallingAET() {
        return callingAET;
    }

    /**
     * Set the calling AET.
     * 
     * @param aet
     *            The calling AET to set.
     */
    public final void setCallingAET(String aet) {
        callingAET = aet;
    }

    /**
     * Returns the AET that holds the work list (Modality Work List SCP).
     * 
     * @return The retrieve AET.
     */
    public final String getCalledAET() {
        return calledAET;
    }

    /**
     * Set the retrieve AET.
     * 
     * @param aet
     *            The retrieve AET to set.
     */
    public final void setCalledAET(String aet) {
        calledAET = aet;
    }

    public final boolean isLocal() {
        return "LOCAL".equalsIgnoreCase(calledAET);
    }

    /**
     * Returns the Association timeout in ms.
     * 
     * @return Returns the acTimeout.
     */
    public final int getAcTimeout() {
        return acTimeout;
    }

    /**
     * Set the association timeout.
     * 
     * @param acTimeout
     *            The acTimeout in ms.
     */
    public final void setAcTimeout(int acTimeout) {
        this.acTimeout = acTimeout;
    }

    /**
     * Returns the DICOM message timeout in ms.
     * 
     * @return Returns the dimseTimeout.
     */
    public final int getDimseTimeout() {
        return dimseTimeout;
    }

    /**
     * Set the DICOM message timeout.
     * 
     * @param dimseTimeout
     *            The dimseTimeout in ms.
     */
    public final void setDimseTimeout(int dimseTimeout) {
        this.dimseTimeout = dimseTimeout;
    }

    /**
     * Returns the socket close delay in ms.
     * 
     * @return Returns the soCloseDelay.
     */
    public final int getSoCloseDelay() {
        return soCloseDelay;
    }

    /**
     * Set the socket close delay.
     * 
     * @param delay
     *            Socket close delay in ms.
     */
    public final void setSoCloseDelay(int delay) {
        soCloseDelay = delay;
    }

    /**
     * returns the max PDU length in bytes.
     * 
     * @return Returns the maxPDUlen.
     */
    public final int getMaxPDUlen() {
        return maxPDUlen;
    }

    /**
     * Set the max PDU length.
     * 
     * @param maxPDUlen
     *            The maxPDUlen in bytes.
     */
    public final void setMaxPDUlen(int maxPDUlen) {
        this.maxPDUlen = maxPDUlen;
    }

    /**
     * Returns the DICOM priority as int value.
     * <p>
     * This value is used for CFIND. 0..MED, 1..HIGH, 2..LOW
     * 
     * @return Returns the priority.
     */
    public final String getPriority() {
        return DicomPriority.toString(priority);
    }

    /**
     * Set the DICOM priority.
     * 
     * @param priority
     *            The priority to set.
     */
    public final void setPriority(String priority) {
        this.priority = DicomPriority.toCode(priority);
    }

    /**
     * 
     */
    protected void startService() throws Exception {
        super.startService();
    }

    /**
     * 
     */
    protected void stopService() throws Exception {
        super.stopService();
    }

    public boolean deleteMWLEntry(String spsID) {
        try {
            lookupMWLManager().removeWorklistItem(spsID);
            log.info("MWL entry with id " + spsID + " removed!");
            return true;
        } catch (Exception x) {
            log.error("Can't delete MWLEntry with id:" + spsID, x);
            return false;
        }
    }

    /**
     * Get a list of work list entries.
     */
    public int findMWLEntries(Dataset searchDS, List result) {
        log.debug("Query MWL SCP: " + calledAET + " with keys:");
        log.debug(searchDS);
        if (isLocal()) {
            return findMWLEntriesLocal(searchDS, result);
        } else {
            return findMWLEntriesFromAET(searchDS, result);
        }
    }

    /**
     * @param searchDS
     * @return
     */
    public int findMWLEntriesLocal(Dataset searchDS, List result) {
        MWLQueryCmd queryCmd = null;
        try {
            queryCmd = new MWLQueryCmd(searchDS);
            queryCmd.execute();
            while (queryCmd.next()) {
                Dataset rsp = queryCmd.getDataset();
                logResponse(rsp);
                result.add(rsp);
            }
        } catch (SQLException x) {
            log.error("Exception in findMWLEntriesLocal! ", x);
        }
        if (queryCmd != null)
            queryCmd.close();
        return queryCmd.isMatchNotSupported() ? 0xff01 : 0xff00;
    }

    private void logResponse(Dataset rsp) {
        if (log.isDebugEnabled()) {
            log.debug("Received matching MWL item from " + calledAET + " :");
            log.debug(rsp);
        }
    }

    private int findMWLEntriesFromAET(Dataset searchDS, List result) {
        ActiveAssociation assoc = null;
        int pendingStatus = 0xff00;
        try {
            // get association for mwl find.
            AEData aeData = new AECmd(calledAET).getAEData();
            assoc = openAssoc(tlsConfig.createSocket(aeData),
                    getCFINDAssocReq());
            if (assoc == null) {
                log.error("Couldnt open association to " + aeData);
                return Status.ProcessingFailure;
            }
            Association as = assoc.getAssociation();
            if (as.getAcceptedTransferSyntaxUID(1) == null) {
                log.error(calledAET + " doesnt support CFIND request!", null);
                return Status.ProcessingFailure;
            }
            // send mwl cfind request.
            Command cmd = DcmObjectFactory.getInstance().newCommand();
            cmd.initCFindRQ(1, UIDs.ModalityWorklistInformationModelFIND,
                    priority);
            Dimse mcRQ = AssociationFactory.getInstance().newDimse(1, cmd,
                    searchDS);
            FutureRSP findRsp = assoc.invoke(mcRQ);
            Dimse dimse = findRsp.get();
            List pending = findRsp.listPending();
            if ( pending.size() > 0 ) {
                pendingStatus = ((Dimse) pending.get(0)).getCommand().getStatus();
                Iterator iter = pending.iterator();
                while (iter.hasNext()) {
                    Dataset rsp = ((Dimse) iter.next()).getDataset();
                    logResponse(rsp);
                    result.add(rsp);
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("Received final C-FIND RSP from " + calledAET + " :"
                        + dimse);
                
            }
        } catch (Exception e) {
            log.error("Cant get working list! Reason: unexpected error", e);
            return Status.ProcessingFailure;
        } finally {
            if (assoc != null)
                try {
                    assoc.release(true);
                } catch (Exception e1) {
                    log.error(
                            "Cant release association for CFIND modality working list"
                                    + assoc.getAssociation(), e1);
                }
        }
        return pendingStatus;
    }

    /**
     * Open a DICOM association for given host, port and assocition request.
     * 
     * @param host
     *            Host to create the association.
     * @param port
     *            Port number to create the association.
     * @param assocRQ
     *            The association request object.
     * 
     * @return The Active association object.
     * 
     * @throws IOException
     * @throws GeneralSecurityException
     */
    private ActiveAssociation openAssoc(Socket sock, AAssociateRQ assocRQ)
            throws IOException, GeneralSecurityException {
        AssociationFactory aFact = AssociationFactory.getInstance();
        Association assoc = aFact.newRequestor(sock);
        assoc.setAcTimeout(acTimeout);
        assoc.setDimseTimeout(dimseTimeout);
        assoc.setSoCloseDelay(soCloseDelay);
        PDU assocAC = assoc.connect(assocRQ);
        if (!(assocAC instanceof AAssociateAC)) {
            return null;
        }
        ActiveAssociation retval = aFact.newActiveAssociation(assoc, null);
        retval.start();
        return retval;
    }

    /**
     * Return the association request for modality worklist C-FIND.
     * <p>
     * This association is used for sending modality worklist C-FIND request and action
     * command.
     * 
     * @return Association for media creation.
     */
    private AAssociateRQ getCFINDAssocReq() {
        AssociationFactory aFact = AssociationFactory.getInstance();
        AAssociateRQ assocRQ = aFact.newAAssociateRQ();
        assocRQ.setCalledAET(calledAET);
        assocRQ.setCallingAET(callingAET);
        assocRQ.setMaxPDULength(maxPDUlen);
        assocRQ.addPresContext(aFact.newPresContext(1,
                UIDs.ModalityWorklistInformationModelFIND, NATIVE_TS));
        return assocRQ;
    }

    /**
     * Returns the MWLManager session bean.
     * 
     * @return The MWLManager.
     * 
     * @throws HomeFactoryException
     * @throws RemoteException
     * @throws CreateException
     */
    private MWLManager lookupMWLManager() throws HomeFactoryException,
            RemoteException, CreateException {
        if (mwlManager == null) {
            MWLManagerHome home = (MWLManagerHome) EJBHomeFactory.getFactory()
                    .lookup(MWLManagerHome.class, MWLManagerHome.JNDI_NAME);
            mwlManager = home.create();
        }
        return mwlManager;
    }

}
