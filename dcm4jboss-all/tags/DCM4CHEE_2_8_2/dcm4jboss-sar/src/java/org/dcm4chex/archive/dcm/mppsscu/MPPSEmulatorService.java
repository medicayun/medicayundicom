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

package org.dcm4chex.archive.dcm.mppsscu;

import java.util.List;

import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;
import org.dcm4cheri.util.StringUtils;
import org.dcm4chex.archive.config.RetryIntervalls;
import org.dcm4chex.archive.ejb.interfaces.MPPSEmulator;
import org.dcm4chex.archive.ejb.interfaces.MPPSEmulatorHome;
import org.dcm4chex.archive.mbean.TimerSupport;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.dcm4chex.archive.util.HomeFactoryException;
import org.jboss.system.ServiceMBeanSupport;

/**
 * @author gunter.zeilinger@tiani.com
 * @version Revision $Date: 2006-05-05 21:23:21 +0800 (周五, 05 5月 2006) $
 * @since 26.02.2005
 */

public class MPPSEmulatorService extends ServiceMBeanSupport implements
        NotificationListener {

    private static final String IN_PROGRESS = "IN PROGRESS";

    private static final String COMPLETED = "COMPLETED";

    private static final int[] MPPS_CREATE_TAGS = { Tags.SpecificCharacterSet,
            Tags.SOPInstanceUID, Tags.Modality, Tags.ProcedureCodeSeq,
            Tags.RefPatientSeq, Tags.PatientName, Tags.PatientID,
            Tags.IssuerOfPatientID, Tags.PatientBirthDate, Tags.PatientSex,
            Tags.PerformedStationAET, Tags.PerformedStationName,
            Tags.PPSStartDate, Tags.PPSStartTime, Tags.PPSEndDate,
            Tags.PPSEndTime, Tags.PPSStatus, Tags.PPSID,
            Tags.PPSDescription, Tags.PerformedProcedureTypeDescription,
            Tags.PerformedProtocolCodeSeq, Tags.ScheduledStepAttributesSeq, };

    private static final int[] MPPS_SET_TAGS = { Tags.SpecificCharacterSet,
            Tags.SOPInstanceUID, Tags.PPSEndDate, Tags.PPSEndTime,
            Tags.PPSStatus, Tags.PerformedSeriesSeq, };
    
    private static final int[] MWL_TAGS = {
            Tags.AccessionNumber, Tags.RefStudySeq, Tags.StudyInstanceUID,
            Tags.RequestedProcedureDescription, Tags.RequestedProcedureID, 
            Tags.PlacerOrderNumber, Tags.FillerOrderNumber         
    };

    private static final int[] SPS_TAGS = {
        Tags.SPSDescription, Tags.ScheduledProtocolCodeSeq, Tags.SPSID        
    };

    private static final int[] PAT_TAGS = {
        Tags.PatientName, Tags.PatientID, Tags.IssuerOfPatientID,
        Tags.PatientBirthDate, Tags.PatientSex
	};

    private final TimerSupport timer = new TimerSupport(this);
    
    private long pollInterval = 0L;

    private Integer schedulerID;

    private String calledAET;

    private String[] stationAETs = {};

    private long[] delay = {};

    private ObjectName mppsScuServiceName;

    private ObjectName mwlScuServiceName;

    private ObjectName hl7SendServiceName;

    public final ObjectName getMppsScuServiceName() {
        return mppsScuServiceName;
    }

    public final void setMppsScuServiceName(ObjectName mppsScuServiceName) {
        this.mppsScuServiceName = mppsScuServiceName;
    }

    public final ObjectName getMwlScuServiceName() {
        return mwlScuServiceName;
    }

    public final void setMwlScuServiceName(ObjectName mwlScuServiceName) {
        this.mwlScuServiceName = mwlScuServiceName;
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
    
    public final String getCalledAET() {
        return calledAET;
    }

    public final void setCalledAET(String calledAET) {
        this.calledAET = calledAET;
    }
    
    public final String getStationAETsWithDelay() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < stationAETs.length; i++) {
            sb.append(stationAETs[i]);
            sb.append(':');
            sb.append(RetryIntervalls.formatInterval(delay[i]));
            sb.append(':');
        }
        sb.setLength(sb.length()-1);
        return sb.toString();
    }
    
    public final void setStationAETsWithDelay(String aetDelays) {
        String[] tokens = StringUtils.split(aetDelays, ':');
        if ((tokens.length & 1) != 0) {
            throw new IllegalArgumentException("Missing delay item: "
                    + aetDelays);
        }
        String[] newAETs = new String[tokens.length / 2];
        long[] newDelay = new long[newAETs.length];
        for (int i = 0, j = 0; i < newAETs.length; i++) {
            newAETs[i] = tokens[j++];
            newDelay[i] = RetryIntervalls.parseInterval(tokens[j++]);
        }
        this.stationAETs = newAETs;
        this.delay = newDelay;
    }
    
    public final String getPollInterval() {
        return RetryIntervalls.formatIntervalZeroAsNever(pollInterval);
    }

    public void setPollInterval(String interval) {
        this.pollInterval = RetryIntervalls
                .parseIntervalOrNever(interval);
        if (getState() == STARTED) {
            timer.stopScheduler("CheckSeriesWithoutMPPS", schedulerID, this);
            schedulerID = timer.startScheduler("CheckSeriesWithoutMPPS",
            		pollInterval, this);
        }
    }

    public void handleNotification(Notification notification, Object handback) {
        try {
            emulateMPPS();
        } catch (Exception e) {
            log.error("emulateMPPS failed:", e);
        }
    }

    public int emulateMPPS() throws Exception {
        log.info("Check for received series without MPPS");
        if (stationAETs.length == 0)
            return 0;
        int num = 0;
        MPPSEmulator mppsEmulator = getMPPSEmulatorHome().create();
        try {
            for (int i = 0; i < stationAETs.length; ++i) {
                Dataset[] mpps = mppsEmulator.generateMPPS(stationAETs[i],
                        delay[i]);
                for (int j = 0; j < mpps.length; ++j) {
                    Dataset ssa = mpps[j].getItem(Tags.ScheduledStepAttributesSeq);
                    String suid = ssa.getString(Tags.StudyInstanceUID);
                    log.info("Emulate MPPS for Study:" + suid + " of Patient:"
                    		+ mpps[j].getString(Tags.PatientName)
                    		+ " received from Station:" + stationAETs[i]);
                	fillType2Attrs(mpps[j], MPPS_CREATE_TAGS);
                    if (addMWLAttrs(mpps[j]) && createMPPS(mpps[j])
                            && updateMPPS(mpps[j]))
                        ++num;
                }
            }
        } finally {
            try {
                mppsEmulator.remove();
            } catch (Exception ignore) {
            }
        }
        return num;
    }

    private boolean addMWLAttrs(Dataset mpps) {
        Dataset filter = getPreparedFilter();
        Dataset ssa = mpps.getItem(Tags.ScheduledStepAttributesSeq);
        final String suid = ssa.getString(Tags.StudyInstanceUID);
        filter.putUI(Tags.StudyInstanceUID, suid);
        List mwlEntries = findMWLEntries(filter);
        if (mwlEntries == null)
            return false;
        if (mwlEntries.isEmpty()) {
        	log.debug("No MWL entry for Study - " + suid + " ! Try with AccessionNumber and PatientID!");
        	filter.putUI(Tags.StudyInstanceUID, (String)null);
        	filter.putSH(Tags.AccessionNumber, ssa.getString(Tags.AccessionNumber));
        	filter.putLO(Tags.PatientID, mpps.getString( Tags.PatientID ) );
        	mwlEntries = findMWLEntries(filter);
            if (mwlEntries == null)
                return false;
            if (mwlEntries.isEmpty()) {
	            log.info("No matching MWL entry for Study - " + suid);
	            fillType2Attrs(ssa, MWL_TAGS);
	            fillType2Attrs(ssa, SPS_TAGS);
	            return true;
            }
        }
        DcmElement ssaSq = mpps.putSQ(Tags.ScheduledStepAttributesSeq);
        String mppsPatID = mpps.getString(Tags.PatientID);
        for (int i = 0, n = mwlEntries.size(); i < n; ++i) {
            ssa = ssaSq.addNewItem();
            Dataset mwlItem = (Dataset) mwlEntries.get(i);
            ssa.putAll(mwlItem.subSet(MWL_TAGS));
            Dataset sps = mwlItem.getItem(Tags.SPSSeq);
            if (sps != null) {
                ssa.putAll(sps.subSet(SPS_TAGS)); 
            }
            if ( ! mwlItem.getString(Tags.PatientID).equals(mppsPatID) ) {
            	log.debug("MWL and MPPS patient different! Merge "+mppsPatID+" with (dominant) "+mwlItem.getString(Tags.PatientID));
            	sendHL7Merge(mwlItem.subSet(PAT_TAGS),mpps.subSet(PAT_TAGS));
            }
        }
        return true;
    }

	/**
	 * @return
	 */
	private Dataset getPreparedFilter() {
		Dataset filter = DcmObjectFactory.getInstance().newDataset();
        for (int i = 0; i < MWL_TAGS.length; i++) {
            filter.putXX(MWL_TAGS[i]);
        }
        for (int i = 0; i < PAT_TAGS.length; i++) {//we need pat atrributes for merge (if necessary)!
            filter.putXX(PAT_TAGS[i]);
        }
        filter.putSQ(Tags.SPSSeq);
		return filter;
	}

	private void fillType2Attrs(Dataset ds, int[] tags) {
		for (int i = 0; i < tags.length; i++) {
			if (ds.vm(tags[i]) == -1)
				ds.putXX(tags[i]);
		}		
	}

	private boolean createMPPS(Dataset mpps) {
        mpps.putCS(Tags.PPSStatus, IN_PROGRESS);
        return sendMPPS(true, mpps.subSet(MPPS_CREATE_TAGS), calledAET);
    }

    private boolean updateMPPS(Dataset mpps) {
        mpps.putCS(Tags.PPSStatus, COMPLETED);
        return sendMPPS(false, mpps.subSet(MPPS_SET_TAGS), calledAET);
    }

    private MPPSEmulatorHome getMPPSEmulatorHome() throws HomeFactoryException {
        return (MPPSEmulatorHome) EJBHomeFactory.getFactory().lookup(
                MPPSEmulatorHome.class, MPPSEmulatorHome.JNDI_NAME);
    }

    private boolean sendMPPS(boolean create, Dataset mpps, String destination) {
        try {
            server.invoke(mppsScuServiceName,
                    "sendMPPS", new Object[] { Boolean.valueOf(create), mpps, destination },
                    new String[] { boolean.class.getName(), Dataset.class.getName(),
                            String.class.getName() });
            return true;
        } catch (Exception x) {
            log.error("Exception occured in sendMPPS;", x);
        }
        return false;
    }

    public List findMWLEntries(Dataset ds) {
        try {
            return (List) server.invoke(mwlScuServiceName, "findMWLEntries",
                    new Object[] { ds },
                    new String[] { Dataset.class.getName() });
        } catch (Exception x) {
            log.error("Exception occured in findMWLEntries: " + x.getMessage(),
                    x);
        }
        return null;
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
            log.error("Failed to send HL7 patient merge message:", e);
            log.error(dsDominant);
        }
	}
    

    protected void startService() throws Exception {
        timer.init();
        schedulerID = timer.startScheduler("CheckSeriesWithoutMPPS",
        		pollInterval, this);
    }

    protected void stopService() throws Exception {
        timer.stopScheduler("CheckSeriesWithoutMPPS", schedulerID, this);
        super.stopService();
    }
}
