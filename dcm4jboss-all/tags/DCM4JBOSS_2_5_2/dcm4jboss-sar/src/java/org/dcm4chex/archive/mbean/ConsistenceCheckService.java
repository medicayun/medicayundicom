/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.mbean;

import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.Calendar;

import javax.ejb.FinderException;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.apache.log4j.Logger;
import org.dcm4chex.archive.config.RetryIntervalls;
import org.dcm4chex.archive.ejb.interfaces.ConsistencyCheck;
import org.dcm4chex.archive.ejb.interfaces.ConsistencyCheckHome;
import org.dcm4chex.archive.util.EJBHomeFactory;

/**
 * @author franz.willer@gwi-ag.com
 * @version $Revision: 1694 $ $Date: 2005-04-14 23:16:19 +0800 (周四, 14 4月 2005) $
 * @since 35.03.2005
 *
 */
public class ConsistenceCheckService extends TimerSupport {

    private long taskInterval = 0L;
	private long minStudyAge;
	private long maxStudyAge;
	private long maxCheckedBefore;

    private int disabledStartHour;
    private int disabledEndHour;
    private int limitNumberOfStudiesPerTask;

    private Integer listenerID;

    private ObjectName fileSystemMgtName;
    private static final Logger log = Logger.getLogger(ConsistenceCheckService.class);

    private final NotificationListener consistentCheckListener = new NotificationListener() {
        public void handleNotification(Notification notif, Object handback) {
            Calendar cal = Calendar.getInstance();
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            if (isDisabled(hour)) {
                if (log.isDebugEnabled())
                    log.debug("ConsistentCheck ignored in time between "
                            + disabledStartHour + " and " + disabledEndHour
                            + " !");
            } else {
                try {
                	check();
                } catch (Exception e) {
                    log.error("Consistant check failed!", e);
                }
            }
        }
    };

    public final String getTaskInterval() {
        String s = RetryIntervalls.formatIntervalZeroAsNever(taskInterval);
        return (disabledEndHour == -1) ? s : s + "!" + disabledStartHour + "-"
                + disabledEndHour;
    }

    public void setTaskInterval(String interval) {
        long oldInterval = taskInterval;
        int pos = interval.indexOf('!');
        if (pos == -1) {
            taskInterval = RetryIntervalls.parseIntervalOrNever(interval);
            disabledEndHour = -1;
        } else {
            taskInterval = RetryIntervalls.parseIntervalOrNever(interval
                    .substring(0, pos));
            int pos1 = interval.indexOf('-', pos);
            disabledStartHour = Integer.parseInt(interval.substring(pos + 1,
                    pos1));
            disabledEndHour = Integer.parseInt(interval.substring(pos1 + 1));
        }
        if (getState() == STARTED && oldInterval != taskInterval) {
            stopScheduler("CheckStudyConsistency", listenerID,
            		consistentCheckListener);
            listenerID = startScheduler("CheckStudyConsistency", taskInterval,
            		consistentCheckListener);
        }
    }


    public int getLimitNumberOfStudiesPerTask() {
        return limitNumberOfStudiesPerTask;
    }

    public void setLimitNumberOfStudiesPerTask(int limit) {
        this.limitNumberOfStudiesPerTask = limit;
    }

	/**
	 * Getter for minStudyAge. 
	 * <p>
	 * This value is used to ensure that consistent check is not performed 
	 * on studies that are not completed (store is not completed).
	 * 
	 * @return Returns ##w (in weeks), ##d (in days), ##h (in hours).
	 */
	public String getMinStudyAge() {
		return RetryIntervalls.formatInterval(minStudyAge);
	}
	
	/**
	 * Setter for minStudyAge. 
	 * <p>
	 * This value is used to ensure that consistent check is not performed 
	 * on studies that are not completed (store is not completed).
	 * 
	 * @param age ##w (in weeks), ##d (in days), ##h (in hours).
	 */
	public void setMinStudyAge(String age) {
		this.minStudyAge = RetryIntervalls.parseInterval(age);
	}
	
    /**
	 * Getter for maxStudyAge. 
	 * <p>
	 * This value is used to limit consistent check to 'newer' studies.
     * 
     * @return ##w (in weeks), ##d (in days), ##h (in hours).
     */
    public String getMaxStudyAge() {
        return RetryIntervalls.formatInterval(maxStudyAge);
    }
    
    /**
	 * Setter for maxStudyAge. 
	 * <p>
	 * This value is used to limit consistent check to 'newer' studies.
     *  
     * @param maxStudyAge The maxStudyAge to set.
     */
    public void setMaxStudyAge(String age) {
        this.maxStudyAge = RetryIntervalls.parseInterval(age);
    }

    /**
	 * Getter for maxStudyAge. 
	 * <p>
	 * This value is used to limit consistent check to 'newer' studies.
     * 
     * @return ##w (in weeks), ##d (in days), ##h (in hours).
     */
    public String getMaxCheckedBefore() {
        return RetryIntervalls.formatInterval(maxCheckedBefore);
    }
    
    /**
	 * Setter for maxStudyAge. 
	 * <p>
	 * This value is used to limit consistent check to 'newer' studies.
     *  
     * @param maxStudyAge The maxStudyAge to set.
     */
    public void setMaxCheckedBefore(String maxCheckedBefore) {
        this.maxCheckedBefore = RetryIntervalls.parseInterval(maxCheckedBefore);
    }
    
    public String check() throws RemoteException, FinderException {
    	int updated = 0;
    	long l = System.currentTimeMillis();
    	Timestamp createdBefore = new Timestamp( l - this.minStudyAge );
    	Timestamp createdAfter = new Timestamp( l - this.maxStudyAge );
    	Timestamp checkedBefore = new Timestamp( l - this.maxCheckedBefore );
    	ConsistencyCheck checker = newConsistencyCheck();
    	if ( log.isDebugEnabled() ) log.debug("call findStudiesToCheck: createdAfter:"+createdAfter+" createdBefore:"+createdBefore+" checkedBefore:"+checkedBefore);
    	int[] studyPks = checker.findStudiesToCheck( createdAfter, createdBefore, checkedBefore, this.limitNumberOfStudiesPerTask );
    	for ( int i = 0, len = studyPks.length ; i < len ; i++) {
    		if ( checker.updateStudy( studyPks[i]) ) {
    			updated++;
    		}
    	}
    	return updated + " of "+ studyPks.length + " studies updated!";
    }
    
    private boolean isDisabled(int hour) {
        if (disabledEndHour == -1) return false;
        boolean sameday = disabledStartHour <= disabledEndHour;
        boolean inside = hour >= disabledStartHour && hour < disabledEndHour; 
        return sameday ? inside : !inside;
    }

    protected void startService() throws Exception {
        super.startService();
        listenerID = startScheduler("CheckStudyConsistency", taskInterval,
        		consistentCheckListener);
    }

    protected void stopService() throws Exception {
        stopScheduler("CheckStudyConsistency", listenerID,
        		consistentCheckListener);
        super.stopService();
    }

    private ConsistencyCheck newConsistencyCheck() {
        try {
        	ConsistencyCheckHome home = (ConsistencyCheckHome) EJBHomeFactory
                    .getFactory().lookup(ConsistencyCheckHome.class,
                    		ConsistencyCheckHome.JNDI_NAME);
            return home.create();
        } catch (Exception e) {
            throw new RuntimeException("Failed to access ConsistencyCheck EJB:",
                    e);
        }
    }

}