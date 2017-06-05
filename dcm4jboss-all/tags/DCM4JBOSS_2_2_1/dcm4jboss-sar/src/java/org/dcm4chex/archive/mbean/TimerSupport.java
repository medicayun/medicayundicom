/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.mbean;

import java.util.Date;

import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.timer.TimerNotification;

import org.jboss.system.ServiceMBeanSupport;

/**
 * @author gunter.zeilinger@tiani.com
 * @version Revision $Date: 2005-02-07 09:02:03 +0800 (周一, 07 2月 2005) $
 * @since 06.02.2005
 */

public class TimerSupport extends ServiceMBeanSupport {
    public static String DEFAULT_TIMER_NAME = "jboss:service=Timer";

    private ObjectName mTimer;

    private static class NotificationFilter implements
            javax.management.NotificationFilter {

        private Integer mId;

        /**
         * Create a Filter.
         * @param id the Scheduler id
         */
        public NotificationFilter(Integer pId) {
            mId = pId;
        }

        /**
         * Determine if the notification should be sent to this Scheduler
         */
        public boolean isNotificationEnabled(Notification notification) {
            if (notification instanceof TimerNotification) {
                TimerNotification lTimerNotification = (TimerNotification) notification;
                return lTimerNotification.getNotificationID().equals(mId);
            }
            return false;
        }
    }

    protected void startService() throws Exception {
        // Create Timer MBean if need be

        mTimer = new ObjectName(DEFAULT_TIMER_NAME);

        if (!getServer().isRegistered(mTimer)) {
            getServer().createMBean("javax.management.timer.Timer", mTimer);
        }
        if (!((Boolean) getServer().getAttribute(mTimer, "Active"))
                .booleanValue()) {
            // Now start the Timer
            getServer().invoke(mTimer, "start", new Object[] {},
                    new String[] {});
        }
    }

    protected Integer startScheduler(long period, NotificationListener listener) {
        if (period <= 0L) return null;
        try {
            Date now = new Date(System.currentTimeMillis() + 1000);
            Integer id = (Integer) getServer().invoke(
                    mTimer,
                    "addNotification",
                    new Object[] { "Schedule", "Scheduler Notification", null,
                            now, new Long(period) },
                    new String[] { String.class.getName(),
                            String.class.getName(), Object.class.getName(),
                            Date.class.getName(), Long.TYPE.getName() });
            getServer().addNotificationListener(mTimer, listener,
                    new TimerSupport.NotificationFilter(id), null);
            return id;
        } catch (Exception e) {
            log.error("operation failed", e);
        }
        return null;
    }

    protected void stopScheduler(Integer id, NotificationListener listener) {
        if (id == null) return;
        try {
            getServer().removeNotificationListener(mTimer, listener);
            getServer().invoke(mTimer, "removeNotification",
                    new Object[] { id },
                    new String[] { Integer.class.getName() });
        } catch (Exception e) {
            log.error("operation failed", e);
        }
    }
}
