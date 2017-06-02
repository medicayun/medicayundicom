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

package org.dcm4chex.webview;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import org.dcm4che2.data.UIDDictionary;
import org.dcm4che2.data.VR;
import org.dcm4che2.net.Association;
import org.dcm4che2.net.CommandUtils;
import org.dcm4che2.net.Device;
import org.dcm4che2.net.DimseRSP;
import org.dcm4che2.net.Executor;
import org.dcm4che2.net.ExtQueryTransferCapability;
import org.dcm4che2.net.NetworkApplicationEntity;
import org.dcm4che2.net.NetworkConnection;
import org.dcm4che2.net.NewThreadExecutor;
import org.dcm4che2.net.NoPresentationContextException;
import org.dcm4che2.net.TransferCapability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author franz.willer@agfa.com
 * @version $Revision$ $Date$
 * @since 04.10.2006
 */
public class UIDQuery {

    private static final String[] NATIVE_LE_TS = {
        UID.ImplicitVRLittleEndian,
        UID.ExplicitVRLittleEndian  };
 
    private static final String[] FIND_CUID = {
        UID.StudyRootQueryRetrieveInformationModelFIND,
        UID.PatientRootQueryRetrieveInformationModelFIND, };
    
    private Executor executor = new NewThreadExecutor("WebViewLauncher");

    private NetworkApplicationEntity remoteAE = new NetworkApplicationEntity();

    private NetworkConnection remoteConn = new NetworkConnection();

    private Device device = new Device("WebViewLauncher");

    private NetworkApplicationEntity ae = new NetworkApplicationEntity();

    private NetworkConnection conn = new NetworkConnection();

    private Association assoc;
    
    static Logger log = LoggerFactory.getLogger(UIDQuery.class);
    
    public UIDQuery(String calling, String called, String host, int port) {
        remoteAE.setInstalled(true);
        remoteAE.setAssociationAcceptor(true);
        remoteAE.setNetworkConnection(new NetworkConnection[] { remoteConn });
        device.setNetworkApplicationEntity(ae);
        device.setNetworkConnection(conn);
        ae.setNetworkConnection(conn);
        ae.setAssociationInitiator(true);

        ae.setAETitle(calling);
        remoteAE.setAETitle(called);
        remoteConn.setHostname(host);
        remoteConn.setPort(port);

    }

    /**
     * Query for given attributes.
     * <p>
     * The returned InstanceContainer sort the result by study and series.
     * 
     * @param keys DICOM object with query attributes.
     * 
     * @return InstanceContainer with splitted (study and series) result.
     */
    public InstanceContainer query(DicomObject keys) { 
        keys.putString(Tag.QueryRetrieveLevel, VR.CS, "IMAGE");
        if ( ! keys.contains(Tag.StudyInstanceUID)) keys.putNull(Tag.StudyInstanceUID, null);
        if ( ! keys.contains(Tag.SeriesInstanceUID)) keys.putNull(Tag.SeriesInstanceUID, null);
        if ( ! keys.contains(Tag.SOPClassUID)) keys.putNull(Tag.SOPClassUID, null);
        if ( ! keys.contains(Tag.SOPInstanceUID)) keys.putNull(Tag.SOPInstanceUID, null);
        
        configureTransferCapability();
        InstanceContainer l;
        try {
            assoc = ae.connect(remoteAE, executor);
            log.debug("assoc:"+assoc);
            l = doQuery(keys);
            log.debug("query:"+l);
        } catch (Exception e) {
            log.error("Query failed:",e);
            l = new InstanceContainer();
        }
        return l;
    }


    private void configureTransferCapability() {
        TransferCapability[] tc = new TransferCapability[2];
        tc[0] = new ExtQueryTransferCapability(FIND_CUID[0],
                NATIVE_LE_TS, TransferCapability.SCU);
        tc[1] = new ExtQueryTransferCapability(FIND_CUID[1],
                NATIVE_LE_TS, TransferCapability.SCU);
        ae.setTransferCapability(tc);
    }

    private InstanceContainer doQuery(DicomObject keys) throws IOException, InterruptedException {
        TransferCapability tc = selectFindTransferCapability();
        String cuid = tc.getSopClass();
        String tsuid = selectTransferSyntax(tc);
        if ( log.isDebugEnabled()) {
            log.debug("Send Query Request using "
                + UIDDictionary.getDictionary().prompt(cuid) + ":");
            log.debug(keys.toString());
        }
        DimseRSP rsp = assoc.cfind(cuid, 0, keys, tsuid, Integer.MAX_VALUE);
        InstanceContainer result = new InstanceContainer();
        int count = 0;
        while (rsp.next()) {
            DicomObject cmd = rsp.getCommand();
            if (CommandUtils.isPending(cmd)) {
                DicomObject data = rsp.getDataset();
                result.add(data);
                if ( log.isDebugEnabled()) {
                    log.debug("\nReceived Query Response #" 
                        + ++count + ":");
                    log.debug(data.toString());
                }
            }
        }
        return result;
    }

    private TransferCapability selectFindTransferCapability()
            throws NoPresentationContextException {
        TransferCapability tc;
        if ((tc = selectTransferCapability(FIND_CUID)) != null)
            return tc;
        throw new NoPresentationContextException(UIDDictionary.getDictionary()
                .prompt(FIND_CUID[0])
                + " not supported by" + remoteAE.getAETitle());
    }

    private TransferCapability selectTransferCapability(String[] cuid) {
        TransferCapability tc;
        for (int i = 0; i < cuid.length; i++) {
            tc = assoc.getTransferCapabilityAsSCU(cuid[i]);
            if (tc != null)
                return tc;
        }
        return null;
    }
   
    private String selectTransferSyntax(TransferCapability tc) {
        String[] tcuids = tc.getTransferSyntax();
        if (Arrays.asList(tcuids).indexOf(UID.DeflatedExplicitVRLittleEndian) != -1)
            return UID.DeflatedExplicitVRLittleEndian;
        return tcuids[0];
    }

}
