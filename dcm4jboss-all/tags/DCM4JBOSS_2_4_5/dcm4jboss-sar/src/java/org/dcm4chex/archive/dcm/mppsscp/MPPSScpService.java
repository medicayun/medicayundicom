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
 * @version $Revision: 1765 $ $Date: 2005-06-02 07:43:50 +0800 (周四, 02 6月 2005) $
 * @since 10.03.2004
 */
public class MPPSScpService extends AbstractScpService {

    public static final String EVENT_TYPE = "org.dcm4chex.archive.dcm.mppsscp";

    public static final NotificationFilter NOTIF_FILTER = new NotificationFilter() {

		private static final long serialVersionUID = 3688507684001493298L;

		public boolean isNotificationEnabled(Notification notif) {
            return EVENT_TYPE.equals(notif.getType());
        }
    };

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

    protected void updatePresContexts(AcceptorPolicy policy, boolean enable) {
        policy.putPresContext(UIDs.ModalityPerformedProcedureStep,
                enable ? getTransferSyntaxUIDs() : null);
    }

    void sendMPPSNotification(Dataset ds) {
        long eventID = super.getNextNotificationSequenceNumber();
        Notification notif = new Notification(EVENT_TYPE, this, eventID);
        notif.setUserData(ds);
        super.sendNotification(notif);
    }

}
