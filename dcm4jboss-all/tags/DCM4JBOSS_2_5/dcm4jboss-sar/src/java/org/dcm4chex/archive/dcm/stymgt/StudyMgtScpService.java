package org.dcm4chex.archive.dcm.stymgt;

import javax.management.Notification;
import javax.management.NotificationFilter;

import org.dcm4che.data.Dataset;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.AcceptorPolicy;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.Association;
import org.dcm4che.net.DcmServiceRegistry;
import org.dcm4chex.archive.dcm.AbstractScpService;
import org.dcm4chex.archive.util.EJBHomeFactory;

public class StudyMgtScpService extends AbstractScpService {

    public static final String EVENT_TYPE = "org.dcm4chex.archive.dcm.stymgt";

    public static final NotificationFilter NOTIF_FILTER = new NotificationFilter() {

		private static final long serialVersionUID = 3257281448414097465L;

		public boolean isNotificationEnabled(Notification notif) {
            return EVENT_TYPE.equals(notif.getType());
        }
    };

    private StudyMgtScp stymgtScp = new StudyMgtScp(this);

	/**
	 * @return Returns the ignoreDeleteFailed.
	 */
	public boolean isIgnoreDeleteFailed() {
		return stymgtScp.isIgnoreDeleteFailed();
	}
	/**
	 * @param ignoreDeleteFailed The ignoreDeleteFailed to set.
	 */
	public void setIgnoreDeleteFailed(boolean ignoreDeleteFailed) {
		stymgtScp.setIgnoreDeleteFailed( ignoreDeleteFailed );
	}

    public String getEjbProviderURL() {
        return EJBHomeFactory.getEjbProviderURL();
    }

    public void setEjbProviderURL(String ejbProviderURL) {
        EJBHomeFactory.setEjbProviderURL(ejbProviderURL);
    }
	
	protected void bindDcmServices(DcmServiceRegistry services) {
        services.bind(UIDs.TianiStudyManagement, stymgtScp);
	}

	protected void unbindDcmServices(DcmServiceRegistry services) {
		services.unbind(UIDs.TianiStudyManagement);		
	}

	protected void updatePresContexts(AcceptorPolicy policy, boolean enable) {
        policy.putPresContext(UIDs.TianiStudyManagement,
                enable ? getTransferSyntaxUIDs() : null);
    }

    void sendStudyMgtNotification(ActiveAssociation assoc, int cmdField,
			int actionTypeID, String iuid, Dataset ds) {
		Association a = assoc.getAssociation();
		long eventID = super.getNextNotificationSequenceNumber();
		Notification notif = new Notification(EVENT_TYPE, this, eventID);
		notif.setUserData(new StudyMgtOrder(a.getCallingAET(), a
				.getCalledAET(), cmdField, actionTypeID, iuid, ds));
		super.sendNotification(notif);
	}

}
