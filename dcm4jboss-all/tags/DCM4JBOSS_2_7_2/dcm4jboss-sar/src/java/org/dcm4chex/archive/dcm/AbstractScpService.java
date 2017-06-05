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

package org.dcm4chex.archive.dcm;

import javax.management.ObjectName;

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
 * @version $Revision: 2068 $ $Date: 2005-11-15 21:01:49 +0800 (周二, 15 11月 2005) $
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
    protected int maxOpsInvoked = 1;
    protected int maxOpsPerformed = 1;
        
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
        return calledAETs == null ? "":StringUtils.toString(calledAETs, '\\');
    }
    
    public final void setCalledAETs(String calledAETs) {
    	if ( getCalledAETs().equals(calledAETs)) return;
        disableService();
        this.calledAETs = StringUtils.split(calledAETs, '\\');
        enableService();
    }

	public final int getMaxPDULength() {
		return maxPDULength;
	}
	
	public final void setMaxPDULength(int maxPDULength) {
		if ( this.maxPDULength == maxPDULength ) return;
		this.maxPDULength = maxPDULength;
		enableService();
	}
	
    public final int getMaxOpsInvoked() {
		return maxOpsInvoked;
	}

	public final void setMaxOpsInvoked(int maxOpsInvoked) {
		if ( this.maxOpsInvoked == maxOpsInvoked ) return;
		this.maxOpsInvoked = maxOpsInvoked;
		enableService();
	}

	public final int getMaxOpsPerformed() {
		return maxOpsPerformed;
	}

	public final void setMaxOpsPerformed(int maxOpsPerformed) {
		if ( this.maxOpsPerformed == maxOpsPerformed ) return;
		this.maxOpsPerformed = maxOpsPerformed;
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
 			policy1.setAsyncOpsWindow(maxOpsInvoked, maxOpsPerformed);
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
    	if ( getCallingAETs().equals(callingAETs)) return;
        this.callingAETs = ANY.equalsIgnoreCase(callingAETs) ? null 
                : StringUtils.split(callingAETs, '\\');
        enableService();
    }
    
    public final boolean isAcceptExplicitVRLE() {
        return acceptExplicitVRLE;
    }

    public final void setAcceptExplicitVRLE(boolean acceptExplicitVRLE) {
        if ( this.acceptExplicitVRLE == acceptExplicitVRLE ) return;
        this.acceptExplicitVRLE = acceptExplicitVRLE;
        enableService();
    }
    
    protected void startService() throws Exception {
        dcmHandler = (DcmHandler) server.invoke(dcmServerName, "dcmHandler",
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
}
