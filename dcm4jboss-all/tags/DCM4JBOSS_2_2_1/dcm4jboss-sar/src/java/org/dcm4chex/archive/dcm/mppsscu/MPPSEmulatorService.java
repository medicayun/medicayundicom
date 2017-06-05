/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
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

/**
 * @author gunter.zeilinger@tiani.com
 * @version Revision $Date: 2005-03-03 00:37:31 +0800 (周四, 03 3月 2005) $
 * @since 26.02.2005
 */

public class MPPSEmulatorService extends TimerSupport implements
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
    
    private long pollInterval = 0L;

    private Integer schedulerID;

    private String calledAET;

    private String[] stationAETs = {};

    private long[] delay = {};

    private ObjectName mppsScuServiceName;

    private ObjectName mwlScuServiceName;

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
            stopScheduler(schedulerID, this);
            schedulerID = startScheduler(pollInterval, this);
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
        Dataset filter = DcmObjectFactory.getInstance().newDataset();
        for (int i = 0; i < MWL_TAGS.length; i++) {
            filter.putXX(MWL_TAGS[i]);
        }
        Dataset ssa = mpps.getItem(Tags.ScheduledStepAttributesSeq);
        final String suid = ssa.getString(Tags.StudyInstanceUID);
        filter.putUI(Tags.StudyInstanceUID, suid);
        filter.putSQ(Tags.SPSSeq);
        List mwlEntries = findMWLEntries(filter);
        if (mwlEntries == null)
            return false;
        if (mwlEntries.isEmpty()) {
            log.info("No matching MWL entry for Study - " + suid);
            fillType2Attrs(ssa, MWL_TAGS);
            fillType2Attrs(ssa, SPS_TAGS);
            return true;
        }
        DcmElement ssaSq = mpps.putSQ(Tags.ScheduledStepAttributesSeq);
        for (int i = 0, n = mwlEntries.size(); i < n; ++i) {
            ssa = ssaSq.addNewItem();
            Dataset mwlItem = (Dataset) mwlEntries.get(i);
            ssa.putAll(mwlItem.subSet(MWL_TAGS));
            Dataset sps = mwlItem.getItem(Tags.SPSSeq);
            if (sps != null) {
                ssa.putAll(sps.subSet(SPS_TAGS)); 
            }
        }
        return true;
    }

	private void fillType2Attrs(Dataset ds, int[] tags) {
		for (int i = 0; i < tags.length; i++) {
			if (ds.vm(tags[i]) == -1)
				ds.putXX(tags[i]);
		}		
	}

	private boolean createMPPS(Dataset mpps) {
        mpps.putCS(Tags.PPSStatus, IN_PROGRESS);
        int status = sendMPPS(mpps.subSet(MPPS_CREATE_TAGS), calledAET);
        if (status != 0) {
            log.error("Failed to create MPPS! Error status:"
                    + Integer.toHexString(status));
            return false;
        }
        return true;
    }

    private boolean updateMPPS(Dataset mpps) {
        mpps.putCS(Tags.PPSStatus, COMPLETED);
        int status = sendMPPS(mpps.subSet(MPPS_SET_TAGS), calledAET);
        if (status != 0) {
            log.error("Failed to update MPPS! Error status:"
                    + Integer.toHexString(status));
            return false;
        }
        return true;
    }

    private MPPSEmulatorHome getMPPSEmulatorHome() throws HomeFactoryException {
        return (MPPSEmulatorHome) EJBHomeFactory.getFactory().lookup(
                MPPSEmulatorHome.class, MPPSEmulatorHome.JNDI_NAME);
    }

    private int sendMPPS(Dataset mpps, String destination) {
        try {
            Integer status = (Integer) server.invoke(mppsScuServiceName,
                    "sendMPPS", new Object[] { mpps, destination },
                    new String[] { Dataset.class.getName(),
                            String.class.getName() });
            return status.intValue();
        } catch (Exception x) {
            log.error("Exception occured in sendMPPS;", x);
        }
        return -1;
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
}
