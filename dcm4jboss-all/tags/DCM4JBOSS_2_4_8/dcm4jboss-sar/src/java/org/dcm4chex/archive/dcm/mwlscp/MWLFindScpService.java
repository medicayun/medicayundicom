/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4chex.archive.dcm.mwlscp;

import java.util.Arrays;

import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.AcceptorPolicy;
import org.dcm4che.net.DcmServiceRegistry;
import org.dcm4chex.archive.dcm.AbstractScpService;
import org.dcm4chex.archive.dcm.mppsscp.MPPSScpService;
import org.dcm4chex.archive.ejb.interfaces.MPPSManager;
import org.dcm4chex.archive.ejb.interfaces.MPPSManagerHome;
import org.dcm4chex.archive.ejb.interfaces.MWLManager;
import org.dcm4chex.archive.ejb.interfaces.MWLManagerHome;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.dcm4chex.archive.util.HomeFactoryException;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision: 1691 $ $Date: 2005-04-14 17:09:16 +0800 (周四, 14 4月 2005) $
 * @since 31.01.2004
 */
public class MWLFindScpService extends AbstractScpService
	implements NotificationListener {

    private static final int NO_OP = 0;
    private static final int UPDATE_STATUS = 1;
    private static final int REMOVE_ITEM = 2;
    
    private String[] ON_MPPS_RECEIVED = {
    		"NO_OP", "UPDATE_STATUS", "REMOVE_ITEM"
    };
    
	private ObjectName mppsScpServiceName;
    
    private int onMPPSReceived = NO_OP;

    private MWLFindScp mwlFindScp = new MWLFindScp(this);

	public final String getOnMPPSReceived() {
		return ON_MPPS_RECEIVED[onMPPSReceived];
	}
	
	public final void setOnMPPSReceived(String onMPPSReceived) {
		int tmp = Arrays.asList(ON_MPPS_RECEIVED).indexOf(onMPPSReceived);
		if (tmp == -1) {
			throw new IllegalArgumentException(onMPPSReceived);
		}
		this.onMPPSReceived = tmp;
	}
	
    public String getEjbProviderURL() {
        return EJBHomeFactory.getEjbProviderURL();
    }        

    public void setEjbProviderURL(String ejbProviderURL) {
        EJBHomeFactory.setEjbProviderURL(ejbProviderURL);
    }

    public final ObjectName getMppsScpServiceName() {
        return mppsScpServiceName;
    }
    
    public final void setMppsScpServiceName(ObjectName mppsScpServiceName) {
        this.mppsScpServiceName = mppsScpServiceName;
    }
    
    protected void startService() throws Exception {
        server.addNotificationListener(mppsScpServiceName,
                this,
                MPPSScpService.NOTIF_FILTER,
                null);
        super.startService();
    }

    protected void stopService() throws Exception {
        super.stopService();
        server.removeNotificationListener(mppsScpServiceName,
                this,
                MPPSScpService.NOTIF_FILTER,
                null);
    }
    
    protected void bindDcmServices(DcmServiceRegistry services) {
        services.bind(UIDs.ModalityWorklistInformationModelFIND, mwlFindScp);
    }

    protected void unbindDcmServices(DcmServiceRegistry services) {
        services.unbind(UIDs.ModalityWorklistInformationModelFIND);
    }

    protected void updatePresContexts(AcceptorPolicy policy, boolean enable) {
        policy.putPresContext(UIDs.ModalityWorklistInformationModelFIND,
                enable ? getTransferSyntaxUIDs() : null);
    }

    private MWLManagerHome getMWLManagerHome() throws HomeFactoryException {
        return (MWLManagerHome) EJBHomeFactory.getFactory().lookup(
                MWLManagerHome.class, MWLManagerHome.JNDI_NAME);
    }

	private MPPSManagerHome getMPPSManagerHome() throws HomeFactoryException {
        return (MPPSManagerHome) EJBHomeFactory.getFactory().lookup(
                MPPSManagerHome.class, MPPSManagerHome.JNDI_NAME);
    }
    
	private Dataset getMPPS(String iuid) throws Exception {
		MPPSManager mgr = getMPPSManagerHome().create();
		try {
			return mgr.getMPPS(iuid);
		} finally {
			try {
				mgr.remove();
			} catch (Exception ignore) {
			}
		}
	}
	
    public void handleNotification(Notification notif, Object handback) {
    	if (onMPPSReceived == NO_OP) return; 
        Dataset mpps = (Dataset) notif.getUserData();
		final String iuid = mpps.getString(Tags.SOPInstanceUID);
		final String status = mpps.getString(Tags.PPSStatus);
        DcmElement sq = mpps.get(Tags.ScheduledStepAttributesSeq);
        if (sq == null) {
        	// MPPS N-SET can be ignored for REMOVE_ITEM
        	// or if status == IN PROGRESS
        	if (onMPPSReceived == REMOVE_ITEM || "IN PROCESS".equals(status))
        		return;
        	try {
				mpps = getMPPS(iuid);
				sq = mpps.get(Tags.ScheduledStepAttributesSeq);
			} catch (Exception e) {
				log.error("Failed to load MPPS - " + iuid, e);
				return;
			}
        }
        MWLManager mgr;
        try {
            mgr = getMWLManagerHome().create();
        } catch (Exception e) {
            log.error("Failed to access MWL Manager:", e);
            return;
        }
        try {
            for (int i = 0, n = sq.vm(); i < n; ++i) {
                Dataset item = sq.getItem(i);
                String spsid = item.getString(Tags.SPSID);
                if (spsid != null) {
                	if (onMPPSReceived == REMOVE_ITEM) {
	                    try {
	                        if (mgr.removeWorklistItem(spsid) != null) {
	                        	log.info("Removed MWL item[spsid=" + spsid + "]");
	                        } else {
	                        	log.warn("No such MWL item[spsid=" + spsid + "]");
	                        }
	                    } catch (Exception e) {
	                        log.error("Failed to remove MWL item[spsid="
	                        		+ spsid + "]", e);
	                    }
                	} else { // onMPPSReceived == UPDATE_STATUS
	                    try {
	                        mgr.updateSPSStatus(spsid, status);
	                        log.info("Update MWL item[spsid=" + spsid
	                        		+ ", status=" + status + "]");
	                    } catch (Exception e) {
	                        log.error("Failed to update MWL item[spsid="
	                        		+ spsid + "]", e);
	                    }
                	}
                }
            }
        } finally {
            try {
                mgr.remove();
            } catch (Exception ignore) {
            }
        }
    }
}
