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

package org.dcm4chex.archive.dcm.mppsscp;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import javax.ejb.CreateException;
import javax.ejb.FinderException;
import javax.management.Notification;
import javax.management.NotificationFilter;

import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.AcceptorPolicy;
import org.dcm4che.net.DcmServiceException;
import org.dcm4che.net.DcmServiceRegistry;
import org.dcm4chex.archive.common.PrivateTags;
import org.dcm4chex.archive.dcm.AbstractScpService;
import org.dcm4chex.archive.ejb.interfaces.MPPSManager;
import org.dcm4chex.archive.ejb.interfaces.MPPSManagerHome;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.dcm4chex.archive.util.HomeFactoryException;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision: 2658 $ $Date: 2006-07-28 22:53:38 +0800 (周五, 28 7月 2006) $
 * @since 10.03.2004
 */
public class MPPSScpService extends AbstractScpService {

    public static final String EVENT_TYPE_MPPS_RECEIVED = "org.dcm4chex.archive.dcm.mppsscp";
    public static final String EVENT_TYPE_MPPS_LINKED = "org.dcm4chex.archive.dcm.mppsscp#linked";
    
    public static final NotificationFilter NOTIF_FILTER = new NotificationFilter() {

		private static final long serialVersionUID = 3688507684001493298L;

		public boolean isNotificationEnabled(Notification notif) {
            return EVENT_TYPE_MPPS_RECEIVED.equals(notif.getType());
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
                enable ? valuesToStringArray(tsuidMap) : null);
    }
    
    void sendMPPSNotification(Dataset ds, String eventType) {
        long eventID = super.getNextNotificationSequenceNumber();
        Notification notif = new Notification(eventType, this, eventID);
        notif.setUserData(ds);
        super.sendNotification(notif);
    }
    
    public Map linkMppsToMwl(String[] spsIDs, String[] mppsIUIDs) throws CreateException, HomeFactoryException, RemoteException, DcmServiceException {
        MPPSManager mgr = getMPPSManagerHome().create();
        Map map = null;
        Dataset dominant = null, prior;
        Map mapPrior = new HashMap();
        for ( int i = spsIDs.length - 1; i >=0 ; i--) {
        	for ( int j = 0 ; j < mppsIUIDs.length ; j++ ) {
		       	map = mgr.linkMppsToMwl(spsIDs[i], mppsIUIDs[j]);
		       	if ( map.containsKey("mwlPat")) { //need patient merge!
		       		if (dominant == null ) {
		       			dominant = (Dataset)map.get("mwlPat");
		       		}
		       		prior = (Dataset) map.get("mppsPat");
		       		mapPrior.put(prior.getString(PrivateTags.PatientPk), prior);
		       	}
		       	logMppsLinkRecord(map, spsIDs[i], mppsIUIDs[j]);
	           	if ( i == 0 ) {
	                sendMPPSNotification((Dataset) map.get("mppsAttrs"), MPPSScpService.EVENT_TYPE_MPPS_LINKED);
	           	}
        	}
        }
      
   		if ( dominant != null ) {
   			map.clear();
       		Dataset[] priorPats = (Dataset[])mapPrior.values().toArray(new Dataset[mapPrior.size()]);
       		map.put("dominant", dominant );
       		map.put("priorPats", priorPats);
        }
/*_*/        
       	return map;
    }
    
    public void unlinkMpps(String mppsIUID) throws RemoteException, CreateException, HomeFactoryException, FinderException {
        MPPSManager mgr = getMPPSManagerHome().create();
    	mgr.unlinkMpps(mppsIUID);
    }
    
	/**
	 * Deletes MPPS entries specified by an array of MPPS IUIDs.
	 * <p>
	 * 
	 * @param iuids  The List of Instance UIDs of the MPPS Entries to delete.
	 * @return
	 * @throws HomeFactoryException
	 * @throws CreateException
	 * @throws RemoteException
	 */
	public boolean deleteMPPSEntries(String[] iuids) throws RemoteException, CreateException, HomeFactoryException {
        MPPSManager mgr = getMPPSManagerHome().create();
        mgr.deleteMPPSEntries(iuids);
		return false;
	}

    public void logMppsLinkRecord(Map map, String spsID, String mppsIUID ) {
    	Dataset mppsAttrs = (Dataset) map.get("mppsAttrs");
    	Dataset mwlAttrs = (Dataset) map.get("mwlAttrs");
        try {
            server.invoke(auditLogName,
                    "logProcedureRecord",
                    new Object[] { "Modify", 
            		mppsAttrs.getString(Tags.PatientID), 
            		mppsAttrs.getString(Tags.PatientName),
            		mwlAttrs.getString(Tags.PlacerOrderNumber), 
            		mwlAttrs.getString(Tags.FillerOrderNumber), 
            		mppsAttrs.getItem(Tags.ScheduledStepAttributesSeq).getString(Tags.StudyInstanceUID), 
					mwlAttrs.getString(Tags.AccessionNumber),
					"MPPS "+mppsIUID+" linked with MWL entry "+spsID},
                    new String[] { String.class.getName(),
                            String.class.getName(), String.class.getName(),
                            String.class.getName(), String.class.getName(),
                            String.class.getName(), String.class.getName(),
                            String.class.getName()});
        } catch (Exception e) {
            log.warn("Failed to log procedureRecord:", e);
        }
    }

    
    private MPPSManagerHome getMPPSManagerHome() throws HomeFactoryException {
        return (MPPSManagerHome) EJBHomeFactory.getFactory().lookup(
                MPPSManagerHome.class, MPPSManagerHome.JNDI_NAME);
    }
    
}
