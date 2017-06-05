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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.ejb.CreateException;
import javax.ejb.FinderException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.ReflectionException;
import javax.xml.transform.Templates;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmObjectFactory;
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
import org.dcm4chex.archive.util.XSLTUtils;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision: 3218 $ $Date: 2007-03-19 20:51:16 +0800 (周一, 19 3月 2007) $
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

    //should be the same as in StoreSCP.
    private static final String MWL2STORE_XSL = "mwl-cfindrsp2cstorerq.xsl";
    
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
    
    /**
     * Link MPPS entries to local available MWL entries.
     * 
     * @param spsIDs
     * @param mppsIUIDs
     * @return
     * @throws CreateException
     * @throws HomeFactoryException
     * @throws RemoteException
     * @throws DcmServiceException
     */
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
	               	try {
	            	    Dataset coerceWL = getCoercionDS((Dataset) map.get("mwlAttrs"));
	            	    if ( log.isDebugEnabled() ) {
	            	        log.debug("MWL Attributes:");
	            	        log.debug(map.get("mwlAttrs"));
	            	        log.debug("Series Attributes from worklist:");
		            	    log.debug(coerceWL);
	            	    }
	            	    if ( coerceWL != null ) {
	            	        log.info("Coerce MWL attributes to series/study after manual MWL-MPPS linking!");
	            	        Collection seriesDS = mgr.getSeriesAndStudyDS(mppsIUIDs[j]);
	            	        Dataset series;
	            	        Dataset coerce = DcmObjectFactory.getInstance().newDataset();
	            	        for ( Iterator iter = seriesDS.iterator() ; iter.hasNext() ; ) {
	            	            coerce.putAll(coerceWL);
	            	            series = (Dataset) iter.next();
	            	            series.remove(Tags.RequestAttributesSeq);
		            	        coerceAttributes(series,coerce);
		            	        log.debug("Update series "+series.getString(Tags.SeriesInstanceUID)+" with worklist attributes!");
	            	        }
	            	        Dataset dsN = mgr.updateSeriesAndStudy(seriesDS);
                            if ( dsN != null ) {
                                log.debug("IAN Dataset of coerced study:");
                                log.debug(dsN);
                                map.put("StudyMgtDS",dsN);
                            }
	            	    }
	            	} catch ( Exception x ) {
	            	    log.error("Cant coerce MWL attributes to series)",x);
	            	}
	                sendMPPSNotification((Dataset) map.get("mppsAttrs"), MPPSScpService.EVENT_TYPE_MPPS_LINKED);
	           	}
        	}
         }
        if ( spsIDs.length > 1 ) {
            log.warn("MWL-MPPS linking use multible worklist entries! Series are updated only for the first worlist item!");
        }
      
   		if ( dominant != null ) {
       		Dataset[] priorPats = (Dataset[])mapPrior.values().toArray(new Dataset[mapPrior.size()]);
       		map.put("dominant", dominant );
       		map.put("priorPats", priorPats);
        }
/*_*/        
       	return map;
    }

    /**
     * Link MPPS entries to MWL entries from external Modality Worklist.
     * 
     * @param spsAttrs  Array of Datasets
     * @param mppsIUIDs
     * @return
     * @throws CreateException
     * @throws HomeFactoryException
     * @throws RemoteException
     * @throws DcmServiceException
     * @throws FinderException 
     */
    public Map linkMppsToMwl(Dataset[] spsAttrs, String[] mppsIUIDs) throws CreateException, HomeFactoryException, RemoteException, DcmServiceException, FinderException {
        MPPSManager mgr = getMPPSManagerHome().create();
        Map map = null;
        Dataset dominant = null, prior;
        Map mapPrior = new HashMap();
        for ( int i = spsAttrs.length - 1; i >=0 ; i--) {
            for ( int j = 0 ; j < mppsIUIDs.length ; j++ ) {
                map = mgr.linkMppsToMwl(spsAttrs[i], mppsIUIDs[j]);
                if ( map.containsKey("mwlPat")) { //need patient merge!
                    if (dominant == null ) {
                        dominant = (Dataset)map.get("mwlPat");
                    }
                    prior = (Dataset) map.get("mppsPat");
                    mapPrior.put(prior.getString(PrivateTags.PatientPk), prior);
                }
                logMppsLinkRecord(map, spsAttrs[i].getString(Tags.SPSID), mppsIUIDs[j]);
                if ( i == 0 ) {
                    try {
                        Dataset coerceWL = getCoercionDS((Dataset) map.get("mwlAttrs"));
                        if ( log.isDebugEnabled() ) {
                            log.debug("MWL Attributes:");
                            log.debug(map.get("mwlAttrs"));
                            log.debug("Series Attributes from worklist:");
                            log.debug(coerceWL);
                        }
                        if ( coerceWL != null ) {
                            log.info("Coerce MWL attributes to series/study after manual MWL-MPPS linking!");
                            Collection seriesDS = mgr.getSeriesAndStudyDS(mppsIUIDs[j]);
                            Dataset series;
                            Dataset coerce = DcmObjectFactory.getInstance().newDataset();
                            for ( Iterator iter = seriesDS.iterator() ; iter.hasNext() ; ) {
                                coerce.putAll(coerceWL);
                                series = (Dataset) iter.next();
                                series.remove(Tags.RequestAttributesSeq);
                                coerceAttributes(series,coerce);
                                log.debug("Update series "+series.getString(Tags.SeriesInstanceUID)+" with worklist attributes!");
                            }
                            Dataset dsN = mgr.updateSeriesAndStudy(seriesDS);
                            if ( dsN != null ) {
                                log.debug("IAN Dataset of coerced study:");
                                log.debug(dsN);
                                map.put("StudyMgtDS",dsN);
                            }
                        }
                    } catch ( Exception x ) {
                        log.error("Cant coerce MWL attributes to series)",x);
                    }
                    sendMPPSNotification((Dataset) map.get("mppsAttrs"), MPPSScpService.EVENT_TYPE_MPPS_LINKED);
                }
            }
         }
        if ( spsAttrs.length > 1 ) {
            log.warn("MWL-MPPS linking use multible worklist entries! Series are updated only for the first worlist item!");
        }
      
        if ( dominant != null ) {
            Dataset[] priorPats = (Dataset[])mapPrior.values().toArray(new Dataset[mapPrior.size()]);
            map.put("dominant", dominant );
            map.put("priorPats", priorPats);
        }
/*_*/        
        return map;
    }
    
    private Dataset getCoercionDS(Dataset ds) throws InstanceNotFoundException, MBeanException, ReflectionException {
        if ( ds == null ) return null;        
        Dataset sps = ds.getItem(Tags.SPSSeq);
        String aet = sps != null ? sps.getString(Tags.ScheduledStationAET) : null;
        Templates tmpl = this.getCoercionTemplatesFor(aet, MWL2STORE_XSL);
        if (tmpl == null) {
            log.warn("Coercion template "+MWL2STORE_XSL+" not found! Can not store MWL attributes to series!");
            return null;
        }
        Dataset out = DcmObjectFactory.getInstance().newDataset();
        try {
            XSLTUtils.xslt(ds, tmpl, out);
        } catch (Exception e) {
            log.error("Attribute coercion failed:", e);
            return null;
        }
        return out;
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
        if (!auditLogger.isAuditLogIHEYr4()) {
            return;
        }
    	Dataset mppsAttrs = (Dataset) map.get("mppsAttrs");
    	Dataset mwlAttrs = (Dataset) map.get("mwlAttrs");
        try {
            server.invoke(auditLogger.getAuditLoggerName(),
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
