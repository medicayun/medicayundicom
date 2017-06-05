/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4chex.archive.dcm;

import javax.management.ObjectName;

import org.dcm4che.data.Dataset;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.AcceptorPolicy;
import org.dcm4che.net.AssociationFactory;
import org.dcm4che.net.DcmServiceRegistry;
import org.dcm4che.net.PDataTF;
import org.dcm4che.server.DcmHandler;
import org.dcm4cheri.util.StringUtils;
import org.jboss.system.ServiceMBeanSupport;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision: 1645 $ $Date: 2005-03-27 00:28:54 +0800 (周日, 27 3月 2005) $
 * @since 31.08.2003
 */
public abstract class AbstractScpService extends ServiceMBeanSupport {

    protected static final String ANY = "ANY";
    protected static final String NONE = "NONE";

    protected static final String[] ONLY_DEFAULT_TS = { UIDs.ImplicitVRLittleEndian,};

    protected static final String[] NATIVE_LE_TS = { UIDs.ExplicitVRLittleEndian,
            UIDs.ImplicitVRLittleEndian,};

    protected ObjectName dcmServerName;

    protected ObjectName auditLogName;
    
    protected DcmHandler dcmHandler;

    protected String[] calledAETs;

    protected String[] callingAETs;
    
    protected boolean acceptExplicitVRLE = true;
    
    protected int maxPDULength = PDataTF.DEF_MAX_PDU_LENGTH;
        
    public final ObjectName getDcmServerName() {
        return dcmServerName;
    }

    public final void setDcmServerName(ObjectName dcmServerName) {
        this.dcmServerName = dcmServerName;
    }

    public final ObjectName getAuditLoggerName() {
        return auditLogName;
    }

    public final void setAuditLoggerName(ObjectName auditLogName) {
        this.auditLogName = auditLogName;
    }

    public final String getCalledAETs() {
        return StringUtils.toString(calledAETs, '\\');
    }
    
    public final void setCalledAETs(String calledAETs) {
        disableService();
        this.calledAETs = StringUtils.split(calledAETs, '\\');
        enableService();
    }

	public final int getMaxPDULength() {
		return maxPDULength;
	}
	
	public final void setMaxPDULength(int maxPDULength) {
		this.maxPDULength = maxPDULength;
		enableService();
	}
	
    protected void enableService() {
        if (dcmHandler == null) return;
        AcceptorPolicy policy = dcmHandler.getAcceptorPolicy();
        for (int i = 0; i < calledAETs.length; ++i) {
            AcceptorPolicy policy1 = policy.getPolicyForCalledAET(calledAETs[i]);
            if (policy1 == null) {
                policy1 = AssociationFactory.getInstance().newAcceptorPolicy();
                policy1.setCallingAETs(callingAETs);
                policy.putPolicyForCalledAET(calledAETs[i], policy1);                
            } else {
                if (policy1.getCallingAETs().length > 0) {
                    if (callingAETs == null) {
                        policy1.setCallingAETs(null);
                    } else {
                        for (int j = 0; j < callingAETs.length; j++) {
                            policy1.addCallingAET(callingAETs[j]);
                        }
                    }
                }
            }
            policy1.setMaxPDULength(maxPDULength);
            updatePresContexts(policy1, true);
        }
    }

    private void disableService() {
        if (dcmHandler == null) return;
        AcceptorPolicy policy = dcmHandler.getAcceptorPolicy();
        for (int i = 0; i < calledAETs.length; ++i) {
            AcceptorPolicy policy1 = policy.getPolicyForCalledAET(calledAETs[i]);
            if (policy1 != null) {
                updatePresContexts(policy1, false);
                if (policy1.listPresContext().isEmpty()) {
                    policy.putPolicyForCalledAET(calledAETs[i], null);
                }
            }
        }
    }

    public final String getCallingAETs() {
        return callingAETs != null ? StringUtils.toString(callingAETs, '\\') : ANY;
    }

    public final void setCallingAETs(String callingAETs) {
        this.callingAETs = ANY.equalsIgnoreCase(callingAETs) ? null 
                : StringUtils.split(callingAETs, '\\');
        enableService();
    }
    
    public final boolean isAcceptExplicitVRLE() {
        return acceptExplicitVRLE;
    }

    public final void setAcceptExplicitVRLE(boolean acceptExplicitVRLE) {
        this.acceptExplicitVRLE = acceptExplicitVRLE;
        enableService();
    }
    
    protected void startService() throws Exception {
        dcmHandler = (DcmHandler) server.invoke(dcmServerName, "getDcmHandler",
                null, null);
        bindDcmServices(dcmHandler.getDcmServiceRegistry());
        enableService();
    }

    protected void stopService() throws Exception {
        disableService();
        unbindDcmServices(dcmHandler.getDcmServiceRegistry());
        dcmHandler = null;
    }

    protected abstract void bindDcmServices(DcmServiceRegistry services);

    protected abstract void unbindDcmServices(DcmServiceRegistry services);

    protected abstract void updatePresContexts(AcceptorPolicy policy, 
            boolean enable);
    
    protected String[] getTransferSyntaxUIDs() {
        return acceptExplicitVRLE ? NATIVE_LE_TS : ONLY_DEFAULT_TS;
    }
    
    public void logDataset(String prompt, Dataset ds) {
        if (!log.isDebugEnabled()) { return; }
        log.debug(prompt);
        log.debug( ds );
    }    
}
