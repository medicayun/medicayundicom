/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4chex.archive.dcm.mwlscp;

import java.rmi.RemoteException;

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
import org.dcm4chex.archive.ejb.interfaces.MWLManager;
import org.dcm4chex.archive.ejb.interfaces.MWLManagerHome;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.dcm4chex.archive.util.HomeFactoryException;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision: 1204 $ $Date: 2004-11-16 08:09:30 +0800 (周二, 16 11月 2004) $
 * @since 31.01.2004
 */
public class MWLFindScpService extends AbstractScpService
	implements NotificationListener {

    private static final String[] AS_UIDS = { UIDs.ModalityWorklistInformationModelFIND};

    private ObjectName mppsScpServiceName;

    private MWLFindScp mwlFindScp = new MWLFindScp(this);

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

    protected void initPresContexts(AcceptorPolicy policy) {
        addPresContexts(policy, AS_UIDS, getTransferSyntaxUIDs());
    }

    private MWLManagerHome getMWLManagerHome() throws HomeFactoryException {
        return (MWLManagerHome) EJBHomeFactory.getFactory().lookup(
                MWLManagerHome.class, MWLManagerHome.JNDI_NAME);
    }

    public void handleNotification(Notification notif, Object handback) {
        Dataset mpps = (Dataset) notif.getUserData();
        DcmElement sq = mpps.get(Tags.ScheduledStepAttributesSeq);
        if (sq == null) return;
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
                    try {
                        Dataset ds = mgr.removeWorklistItem(spsid);
                        log.info("Removed MWL item - " + spsid);
                    } catch (RemoteException e) {
                        log.error("Failed to remove MWL item - " + spsid, e);
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
