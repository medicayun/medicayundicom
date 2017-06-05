/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4chex.archive.dcm.hpscp;

import java.io.IOException;
import java.net.Socket;

import javax.management.ObjectName;

import org.dcm4che.dict.UIDs;
import org.dcm4che.net.AcceptorPolicy;
import org.dcm4che.net.DcmServiceRegistry;
import org.dcm4chex.archive.dcm.AbstractScpService;
import org.dcm4chex.archive.ejb.jdbc.AEData;
import org.dcm4chex.archive.mbean.TLSConfigDelegate;
import org.dcm4chex.archive.util.EJBHomeFactory;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1929 $ $Date: 2005-08-30 22:54:54 +0800 (周二, 30 8月 2005) $
 * @since Aug 17, 2005
 */
public class HPScpService extends AbstractScpService {

    private final HPStoreScp hpStoreScp = new HPStoreScp(this);
    private final HPFindScp hpFindScp = new HPFindScp(this);
    private final HPMoveScp hpMoveScp = new HPMoveScp(this);

    private TLSConfigDelegate tlsConfig = new TLSConfigDelegate(this);
    
    private boolean sendPendingMoveRSP = true;
    private int acTimeout = 5000;
    private int dimseTimeout = 0;
    private int soCloseDelay = 500;
	
    public String getEjbProviderURL() {
        return EJBHomeFactory.getEjbProviderURL();
    }

    public void setEjbProviderURL(String ejbProviderURL) {
        EJBHomeFactory.setEjbProviderURL(ejbProviderURL);
    }

    public final ObjectName getTLSConfigName() {
        return tlsConfig.getTLSConfigName();
    }

    public final void setTLSConfigName(ObjectName tlsConfigName) {
        tlsConfig.setTLSConfigName(tlsConfigName);
    }

	public final int getAcTimeout() {
        return acTimeout;
    }

    public final void setAcTimeout(int acTimeout) {
        this.acTimeout = acTimeout;
    }

    public final int getDimseTimeout() {
        return dimseTimeout;
    }

    public final void setDimseTimeout(int dimseTimeout) {
        this.dimseTimeout = dimseTimeout;
    }

    public final int getSoCloseDelay() {
        return soCloseDelay;
    }

    public final void setSoCloseDelay(int soCloseDelay) {
        this.soCloseDelay = soCloseDelay;
    }

    public final boolean isSendPendingMoveRSP() {
        return sendPendingMoveRSP;
    }

    public final void setSendPendingMoveRSP(boolean sendPendingMoveRSP) {
        this.sendPendingMoveRSP = sendPendingMoveRSP;
    }

	Socket createSocket(AEData aeData) throws IOException {
        return tlsConfig.createSocket(aeData);
    }
	
    protected void bindDcmServices(DcmServiceRegistry services) {
        services.bind(UIDs.HangingProtocolStorage, hpStoreScp);
        services.bind(UIDs.HangingProtocolInformationModelFIND, hpFindScp);
        services.bind(UIDs.HangingProtocolInformationModelMOVE, hpFindScp);
    }

    protected void unbindDcmServices(DcmServiceRegistry services) {
        services.unbind(UIDs.HangingProtocolStorage);
        services.unbind(UIDs.HangingProtocolInformationModelFIND);
        services.unbind(UIDs.HangingProtocolInformationModelMOVE);
    }

    protected void updatePresContexts(AcceptorPolicy policy, boolean enable) {
		String[] tsuids = enable ? getTransferSyntaxUIDs() : null;
        policy.putPresContext(UIDs.HangingProtocolStorage, tsuids);
        policy.putPresContext(UIDs.HangingProtocolInformationModelFIND, tsuids);
        policy.putPresContext(UIDs.HangingProtocolInformationModelMOVE, tsuids);
     }

}
