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

package org.dcm4chex.archive.dcm.movescu;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.jms.JMSException;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.dict.Tags;
import org.dcm4cheri.util.StringUtils;
import org.dcm4chex.archive.config.DicomPriority;
import org.dcm4chex.archive.config.ForwardingRules;
import org.dcm4chex.archive.dcm.storescp.StoreScpService;
import org.dcm4chex.archive.notif.IANNotificationVO;
import org.dcm4chex.archive.util.JMSDelegate;
import org.jboss.system.ServiceMBeanSupport;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 2010 $ $Date: 2005-10-07 03:55:27 +0800 (周五, 07 10月 2005) $
 * @since 27.08.2004
 *
 */
public class ForwardService extends ServiceMBeanSupport implements
        NotificationListener {

    private static final String NONE = "NONE";
    private static final String[] EMPTY = {};

    private ObjectName storeScpServiceName;
    private ObjectName editContentServiceName;

    private int maxSOPInstanceUIDsPerMoveRQ = 100;
    
    private String[] forwardModifiedToAETs = {};

    private int forwardPriority = 0;

    private ForwardingRules forwardingRules = new ForwardingRules("");

    public final String getForwardingRules() {
        return forwardingRules.toString();
    }

    public final void setForwardingRules(String forwardingRules) {
        this.forwardingRules = new ForwardingRules(forwardingRules);
    }

    public final String getForwardPriority() {
        return DicomPriority.toString(forwardPriority);
    }

    public final void setForwardPriority(String forwardPriority) {
        this.forwardPriority = DicomPriority.toCode(forwardPriority);
    }

    public final ObjectName getStoreScpServiceName() {
        return storeScpServiceName;
    }

    public final void setStoreScpServiceName(ObjectName storeScpServiceName) {
        this.storeScpServiceName = storeScpServiceName;
    }

	public ObjectName getEditContentServiceName() {
		return editContentServiceName;
	}
    
	public void setEditContentServiceName(ObjectName editContentServiceName) {
		this.editContentServiceName = editContentServiceName;
	}
	
	public String getForwardModifiedToAETs() {
		return forwardModifiedToAETs.length == 0 ? NONE
                : StringUtils.toString( forwardModifiedToAETs, ',');
	}
	
	public void setForwardModifiedToAETs( String s ) {
        forwardModifiedToAETs = NONE.equals(s) ? EMPTY
                : StringUtils.split( s, ',');
	}

    public final int getMaxSOPInstanceUIDsPerMoveRQ() {
        return maxSOPInstanceUIDsPerMoveRQ;
    }
    
    public final void setMaxSOPInstanceUIDsPerMoveRQ(int max) {
        this.maxSOPInstanceUIDsPerMoveRQ = max;
    }
    
    protected void startService() throws Exception {
        server.addNotificationListener(storeScpServiceName,
                this,
                StoreScpService.NOTIF_FILTER,
                null);
        server.addNotificationListener(editContentServiceName,
        		contentEditNotificationListener,
                null,
                "contentEdit");
    }

    protected void stopService() throws Exception {
        server.removeNotificationListener(storeScpServiceName,
        		contentEditNotificationListener,
                StoreScpService.NOTIF_FILTER,
                null);
        server.removeNotificationListener(editContentServiceName,
                this,
                null,
                "contentEdit");
    }

    public void handleNotification(Notification notif, Object handback) {
    	IANNotificationVO ianVO = (IANNotificationVO) notif.getUserData();
        Map ians = ianVO.getIANs();
        if (ians != null) {
	        Map param = new HashMap();
			param.put("calling", new String[]{ianVO.getCallingAET()});
			param.put("called", new String[]{ianVO.getCalledAET()});
            String[] destAETs = forwardingRules
                    .getForwardDestinationsFor(param);
            if (destAETs.length != 0) forward(destAETs, ians);
        }
    }

    private void forward(String[] destAETs, Map ians) {
        String[] studyIUIDs = new String[1];
        String[] seriesIUIDs = new String[1];
        ArrayList sopIUIDs = new ArrayList();
        Dataset refSOP;
        String retrAET = null;
        for (Iterator it = ians.values().iterator(); it.hasNext();) {
            Dataset ian = (Dataset) it.next();
            studyIUIDs[0] = ian.getString(Tags.StudyInstanceUID);
            DcmElement refSeriesSeq = ian.get(Tags.RefSeriesSeq);
            for (int i = 0, n = refSeriesSeq.vm(); i < n; ++i) {
                Dataset refSeries = refSeriesSeq.getItem(i);
                seriesIUIDs[0] = refSeries.getString(Tags.SeriesInstanceUID);
                DcmElement refSOPSeq = refSeries.get(Tags.RefSOPSeq);
                for (int j = 0, m = refSOPSeq.vm(); j < m; ++j) {
                    refSOP = refSOPSeq.getItem(j);
                    retrAET = refSOP.getString(Tags.RetrieveAET);
                    sopIUIDs.add(refSOP.getString(Tags.RefSOPInstanceUID));
                    if (sopIUIDs.size() >= maxSOPInstanceUIDsPerMoveRQ) {
                        scheduleMoveOrders(studyIUIDs, seriesIUIDs, sopIUIDs, destAETs, retrAET);
                    }
                }
                scheduleMoveOrders(studyIUIDs, seriesIUIDs, sopIUIDs, destAETs, retrAET);
            }
        }
    }

    private void scheduleMoveOrders(String[] studyIUIDs, String[] seriesIUIDs, 
            ArrayList sopIUIDs, String[] destAETs, String retrAET) {
        if (sopIUIDs.isEmpty())
            return;
        String[] iuids = (String[]) sopIUIDs
                .toArray(new String[sopIUIDs.size()]);
        sopIUIDs.clear();
        for (int k = 0; k < destAETs.length; ++k) {
            final String destAET = ForwardingRules.toAET(destAETs[k]);
            final long scheduledTime = ForwardingRules
                .toScheduledTime(destAETs[k]);
            MoveOrder order = new MoveOrder(retrAET, destAET, forwardPriority, null,
                    studyIUIDs, seriesIUIDs, iuids);
            log.info("Scheduling " + order
                    + (scheduledTime > 0L ? (" for " + new Date(
                            scheduledTime)) : " now"));
            try {
                JMSDelegate.queue(MoveOrder.QUEUE, order, JMSDelegate
                        .toJMSPriority(forwardPriority), scheduledTime);
            } catch (JMSException e) {
                log.error("Failed to schedule " + order, e);
            }
        }
    }

    public final NotificationListener contentEditNotificationListener = new NotificationListener() {

		/**
		 * @see javax.management.NotificationListener#handleNotification(javax.management.Notification, java.lang.Object)
		 */
		public void handleNotification(Notification notif, Object ctx) {
			if ( forwardModifiedToAETs == null || forwardModifiedToAETs.length < 1 ) return;
			Object o = notif.getUserData();
			if ( o == null ) return;
			if ( log.isDebugEnabled() ) {
				log.debug("ContentEditNotification:"); 
				log.debug( o );
			}
			Map map = null;
			if ( o instanceof Dataset) {
				map = new HashMap();
				map.put("ds", o );
			} else if ( o instanceof Map ) {
				map = (Map) o;
			} else {
				log.error("Ignored! ContentEditNotification with wrong userObject type! "+o.getClass().getName());
				return;
			}
			forward( forwardModifiedToAETs, map);
		}
    	
    };

}