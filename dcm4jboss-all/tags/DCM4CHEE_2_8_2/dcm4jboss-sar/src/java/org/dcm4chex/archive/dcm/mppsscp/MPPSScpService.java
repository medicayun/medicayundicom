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
import java.util.Map;

import javax.ejb.CreateException;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.ObjectName;

import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.AcceptorPolicy;
import org.dcm4che.net.DcmServiceException;
import org.dcm4che.net.DcmServiceRegistry;
import org.dcm4chex.archive.dcm.AbstractScpService;
import org.dcm4chex.archive.ejb.interfaces.MPPSManager;
import org.dcm4chex.archive.ejb.interfaces.MPPSManagerHome;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.dcm4chex.archive.util.HomeFactoryException;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision: 2362 $ $Date: 2006-03-24 01:23:19 +0800 (周五, 24 3月 2006) $
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

    private ObjectName hl7SendServiceName;
    
    private MPPSScp mppsScp = new MPPSScp(this);

    public String getEjbProviderURL() {
        return EJBHomeFactory.getEjbProviderURL();
    }        

    public void setEjbProviderURL(String ejbProviderURL) {
        EJBHomeFactory.setEjbProviderURL(ejbProviderURL);
    }

	/**
	 * @return Returns the hl7SendServiceName.
	 */
	public ObjectName getHl7SendServiceName() {
		return hl7SendServiceName;
	}
	/**
	 * @param hl7SendServiceName The hl7SendServiceName to set.
	 */
	public void setHl7SendServiceName(ObjectName hl7SendServiceName) {
		this.hl7SendServiceName = hl7SendServiceName;
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
    
    public Map linkMppsToMwl(String spsID, String mppsIUID, boolean sendNotif) throws CreateException, HomeFactoryException, RemoteException, DcmServiceException {
        MPPSManager mgr = getMPPSManagerHome().create();
       	Map map = mgr.linkMppsToMwl(spsID, mppsIUID);
       	logMppsLinkRecord(map, spsID, mppsIUID);
        Boolean userAction = (Boolean) map.get("userAction");
       	if ( userAction != null && userAction.booleanValue() ) {
       		log.warn("User action is needed! MPPS patient has more than one studies and should be merged!");
       	} else {
       		if ( map.get("mwlPat") != null ) {
       			Dataset dominant = (Dataset)map.get("mwlPat");
       			Dataset prior = (Dataset)map.get("mppsPat");
       			logPatientMerge(dominant, prior);
       			sendHL7Merge(dominant, prior);
       		}
           	Dataset mpps = (Dataset) map.get("mppsAttrs");
           	if ( sendNotif ) {
                sendMPPSNotification(mpps, MPPSScpService.EVENT_TYPE_MPPS_LINKED);
           	}
        }
       	return map;
    }
    
	void sendHL7Merge(Dataset dsDominant, Dataset priorPat) {
        try {
            server.invoke(this.hl7SendServiceName,
                    "sendHL7PatientMerge",
                    new Object[] {  dsDominant, 
            					new Dataset[] { priorPat }, 
            					"LOCAL^LOCAL",
								"LOCAL^LOCAL",
								Boolean.FALSE },
                    new String[] { Dataset.class.getName(),
        					   Dataset[].class.getName(),
							   String.class.getName(),
							   String.class.getName(),
							   boolean.class.getName() });
        } catch (Exception e) {
            log.error("Failed to send HL7 patient merge message:", e);log.error(dsDominant);
        }
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

    
    public void logPatientMerge(Dataset dominant, Dataset prior) {
        logPatientRecord(
                "Modify",
                dominant.getString(Tags.PatientID),
                dominant.getString(Tags.PatientName),
                makeMergeDesc(prior));
        logPatientRecord(
                "Delete",
                prior.getString(Tags.PatientID),
                prior.getString(Tags.PatientName),
                makeMergeDesc(dominant));
    	
    }
    public String makeMergeDesc(Dataset ds) {
        return "Merged with [" + ds.getString(Tags.PatientID) +
        	"]" +  ds.getString(Tags.PatientName);
    }
	
    public void logPatientRecord(String action,
            String patid, String patname, String desc) {
        try {
            server.invoke(auditLogName,
                    "logPatientRecord",
                    new Object[] { action, patid, patname, desc},
                    new String[] { String.class.getName(),
                            String.class.getName(), String.class.getName(),
                            String.class.getName()});
        } catch (Exception e) {
            log.warn("Failed to log patientRecord:", e);
        }
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
