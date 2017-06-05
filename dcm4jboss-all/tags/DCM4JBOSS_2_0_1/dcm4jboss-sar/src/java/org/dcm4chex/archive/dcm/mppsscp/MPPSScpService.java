/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4chex.archive.dcm.mppsscp;

import javax.management.Notification;
import javax.management.NotificationFilter;

import org.dcm4che.data.Dataset;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.AcceptorPolicy;
import org.dcm4che.net.DcmServiceRegistry;
import org.dcm4chex.archive.dcm.AbstractScpService;
import org.dcm4chex.archive.util.EJBHomeFactory;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision: 1204 $ $Date: 2004-11-16 08:09:30 +0800 (周二, 16 11月 2004) $
 * @since 10.03.2004
 */
public class MPPSScpService extends AbstractScpService {

    public static final String EVENT_TYPE = "org.dcm4chex.archive.dcm.mppsscp";

    public static final NotificationFilter NOTIF_FILTER = new NotificationFilter() {

        public boolean isNotificationEnabled(Notification notif) {
            return EVENT_TYPE.equals(notif.getType());
        }
    };

    private static final String[] AS_UIDS = { UIDs.ModalityPerformedProcedureStep};

    private MPPSScp mppsScp = new MPPSScp(this);
    
    public String getEjbProviderURL() {
        return EJBHomeFactory.getEjbProviderURL();
    }        

    public void setEjbProviderURL(String ejbProviderURL) {
        EJBHomeFactory.setEjbProviderURL(ejbProviderURL);
    }

    protected void bindDcmServices(DcmServiceRegistry services) {
        services.bind(UIDs.ModalityPerformedProcedureStep, mppsScp);
    }

    protected void unbindDcmServices(DcmServiceRegistry services) {
        services.unbind(UIDs.ModalityPerformedProcedureStep);
    }

    protected void initPresContexts(AcceptorPolicy policy) {
        addPresContexts(policy, AS_UIDS, getTransferSyntaxUIDs());
    }

    void sendMPPSNotification(Dataset ds) {
        long eventID = super.getNextNotificationSequenceNumber();
        Notification notif = new Notification(EVENT_TYPE, this, eventID);
        notif.setUserData(ds);
        super.sendNotification(notif);
    }

}
