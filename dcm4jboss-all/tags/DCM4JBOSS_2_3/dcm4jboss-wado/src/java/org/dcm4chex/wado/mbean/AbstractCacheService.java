/*
 * Created on 09.12.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.dcm4chex.wado.mbean;

import javax.management.Notification;
import javax.management.NotificationListener;

import org.apache.log4j.Logger;
import org.dcm4chex.archive.config.RetryIntervalls;
import org.dcm4chex.archive.mbean.TimerSupport;
import org.dcm4chex.archive.util.FileUtils;
import org.dcm4chex.wado.mbean.cache.WADOCache;

/**
 * @author franz.willer
 *
 * The MBean to manage the WADO service.
 * <p>
 * This class use WADOSupport for the WADO methods and WADOCache for caching jpg images.
 */
public abstract class AbstractCacheService extends TimerSupport {

	protected Logger log = Logger.getLogger( getClass().getName() );
	protected WADOCache cache = null;
	
	private static final long MIN_FREE = 1 * FileUtils.MEGA;
	private static final long MIN_PREF_FREE = 2 * FileUtils.MEGA;

    private final NotificationListener freeDiskSpaceListener = 
        new NotificationListener(){
            public void handleNotification(Notification notif, Object handback) {
                cache.freeDiskSpace( true );
            }};
	private long freeDiskSpaceInterval;
	private Integer freeDiskSpaceListenerID;
	
	public WADOCache getCache() {
		return cache;
	}
	
	public void setCacheRoot( String newRoot ) {
		cache.setCacheRoot( newRoot );
	}
	
	public String getCacheRoot() {
		return cache.getCacheRoot();
	}
	
	public void setMinFreeSpace( String minFree ) {
		cache.setMinFreeSpace( FileUtils.parseSize(minFree, MIN_FREE) );
	}
	
	public String getMinFreeSpace() {
		return FileUtils.formatSize(cache.getMinFreeSpace());
	}

	public void setPreferredFreeSpace( String minFree ) {
		cache.setPreferredFreeSpace( FileUtils.parseSize(minFree, MIN_PREF_FREE) );
	}
	
	public String getPreferredFreeSpace() {
		return FileUtils.formatSize(cache.getPreferredFreeSpace());
	}
	
	/**
	 * Clears the cache.
	 * <p>
	 * Remove all files in the cache.
	 *
	 */
	public String clearCache() {
		long before = cache.getFreeSpace();
		cache.clearCache();
		return getDeleteResult( before );
	}
	
	private String getDeleteResult( long before ) {
		long after = cache.getFreeSpace();
		long delta = after - before;
		StringBuffer sb = new StringBuffer();
		log.info("getDeleteResult: before:"+before+" , after:"+after+" , delta:"+delta);
		sb.append(FileUtils.formatSize(delta)).append(" removed!");
		if ( after < cache.getMinFreeSpace() ) {
			sb.append(" WARNING: INSUFFICIANT Free disk space! Should be ").append(this.getMinFreeSpace());
		} else if ( after < cache.getPreferredFreeSpace() ) {
			sb.append(" WARNING: Free disk space is less than preferred free space (").append(this.getPreferredFreeSpace()).append(")");
		}
		return sb.toString();
	}

	/**
	 * Get the timer interval for scheduling freeDiskSpace.
	 * <p>
	 * The return value is either 'NEVER' or &lt;interval&gt;&lt;unit&gt; where interval is a 
	 * natural number and unit one of 'w', 'd', 'h', 'm' or 's' representing week, day, hour,
	 * minute or second.
	 * 
	 * @return A formatted String representing the timer interval.
	 */
    public final String getFreeDiskSpaceInterval() {
        return RetryIntervalls.formatIntervalZeroAsNever(freeDiskSpaceInterval);
    }
    
    /**
	 * Set the timer interval for scheduling freeDiskSpace.
	 * <p>
	 * The parameter value is either 'NEVER' or &lt;interval&gt;&lt;unit&gt; where interval is a 
	 * natural number and unit one of 'w', 'd', 'h', 'm' or 's' representing week, day, hour,
	 * minute or second.
     * 
     * @param interval Timer interval as formatted String.
     */
    public void setFreeDiskSpaceInterval(String interval) {
        freeDiskSpaceInterval = RetryIntervalls.parseIntervalOrNever(interval);
        if (getState() == STARTED) {
            stopScheduler("CheckFreeDiskSpace", freeDiskSpaceListenerID,
            		freeDiskSpaceListener);
            freeDiskSpaceListenerID = startScheduler("CheckFreeDiskSpace",
            		freeDiskSpaceInterval, freeDiskSpaceListener);
        }
    }
	
	/**
	 * Cleans the cache if necessary.
	 *
	 */
	public String freeDiskSpace() {
		long before = cache.getFreeSpace();
		cache.freeDiskSpace( false ); //cleans the cache and wait until clean process is done. (not in background)
		return getDeleteResult( before );
	}
	
	/**
	 * Returns the available space of the cache drive.
	 * <p>
	 * 
	 * @return
	 */
	public String getFreeSpace() {
		return FileUtils.formatSize( cache.getFreeSpace() );
	}
	
	/**
	 * Start scheduler for freeDiskSpace.
	 * <p>
	 * This queue is used to receive media creation request from scheduler or web interface.
	 */
    protected void startService() throws Exception {
        super.startService();
        freeDiskSpaceListenerID = startScheduler("CheckFreeDiskSpace", 
        		freeDiskSpaceInterval, freeDiskSpaceListener);
    }

	/**
	 * Stop scheduler for freeDiskSpace.
	 * 
	 */
    protected void stopService() throws Exception {
        stopScheduler("CheckFreeDiskSpace", freeDiskSpaceListenerID,
        		freeDiskSpaceListener);
        super.stopService();
    }
	

}
