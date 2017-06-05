package org.dcm4chex.archive.ejb.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import EDU.oswego.cs.dl.util.concurrent.WaitFreeQueue;

import javax.ejb.FinderException;

import org.apache.log4j.Logger;
import org.dcm4chex.archive.ejb.interfaces.FileSystemLocal;
import org.dcm4chex.archive.ejb.interfaces.FileSystemLocalHome;
import org.dcm4chex.archive.ejb.interfaces.SeriesLocal;
import org.dcm4chex.archive.ejb.interfaces.SeriesLocalHome;
import org.dcm4chex.archive.ejb.interfaces.StudyLocal;
import org.dcm4chex.archive.ejb.interfaces.StudyLocalHome;

/**
 * The utility class is performing tuning class for EJB 2.1 when we turn on Option D cache (ontainer-cache-conf), 
 * where Entity object
 * will be cached if findByPrimaryKey is used. Since sometimes we don't know the primary key yet, but we do know other
 * unique identifier, such as Instance UID, therefore, we can map the other unique identifier to primary key. 
 * As a result, we'll turn all subsequent findByXXXXX calls to findByPrimaryKey thus improve the performance.
 * <p>
 * Primary Key never changes, so we can cache it. Internally, we have a scavenger thread that checks the cache
 * to see of the cache size reaches the maximum or not, if so, remove oldest ones. This is only for StudyPK 
 * and SeriesPK caching.
 * <p>
 * @author fang.yang@agfa.com
 * @version $Id: EntityPkCache.java 2525 2006-06-12 10:05:43Z javawilli $
 * @since May 18, 2006
 *
 */
public class EntityPkCache {

	private static Logger log = Logger.getLogger(EntityPkCache.class);

	private static final EntityPkCache instance = new EntityPkCache();
	
	/**
	 * Key: directory path of file system, unique identifier
	 * Value: Primary Key, Long
	 */
	private Map pksFS = new HashMap();

	/**
	 * Key: Study Instance UID
	 * Value: Primary Key, Long
	 */
	private Map pksStudy = new HashMap();
	
	/**
	 * Key: Series Instance UID
	 * Value: Primary Key, Long
	 */
	private Map pksSeries = new HashMap();
	
	/**
	 * Keep track FIFO of StudyIuid
	 */
	private WaitFreeQueue qStudy = new WaitFreeQueue();
	
	/**
	 * Keep track FIFO of SeriesIuid
	 */
	private WaitFreeQueue qSeries = new WaitFreeQueue();
	
	private long scavengerInterval = 60000; 	// 1 minute
	private int maxStudyIuidCacheSize = 168; 	// lucky number
	private int maxSeriesIuidCacheSize = 168; 	// lucky number
	
	private Timer timer = null;
	
	private EntityPkCache()
	{
		// We start it in case it's being used standalone
		start();
	}
	
	public void start()
	{
		// Fire up cache scavenger
		timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				scavenge();
			}			
		}, scavengerInterval, scavengerInterval);		
		log.info("EntityPkCache scavenger started. Interval: "+scavengerInterval+" milliseconds");	
	}
	
	public boolean isScavengerRunning()
	{
		return timer != null;
	}
	
	public void stop()
	{
		if(timer != null)
		{
			timer.cancel();
			timer = null;
		}
	}
	
	public static EntityPkCache getInstance()
	{
		return instance;
	}
	
	public static FileSystemLocal findByDirectoryPath(FileSystemLocalHome fileSystemHome, String dirPath) throws FinderException
	{
		return instance._findByDirectoryPath(fileSystemHome, dirPath);
	}
	
	protected FileSystemLocal _findByDirectoryPath(FileSystemLocalHome fileSystemHome, String dirPath) throws FinderException
	{
		synchronized(pksFS)
		{
			if(!pksFS.containsKey(dirPath))
			{
				Long pk = fileSystemHome.findByDirectoryPath(dirPath).getPk();
				pksFS.put(dirPath, pk);
			}
		}
		
		return fileSystemHome.findByPrimaryKey((Long)pksFS.get(dirPath)); 
	}
	
	public static StudyLocal findByStudyIuid(StudyLocalHome studyHome, String studyIuid) throws FinderException
	{
		return instance._findByStudyIuid(studyHome, studyIuid);
	}
	
	protected StudyLocal _findByStudyIuid(StudyLocalHome studyHome, String studyIuid) throws FinderException
	{
		synchronized(pksStudy)
		{
			if(!pksStudy.containsKey(studyIuid))
			{
				Long pk = studyHome.findByStudyIuid(studyIuid).getPk();
				pksStudy.put(studyIuid, pk);
				try {
					qStudy.offer(studyIuid,100);
				} catch (InterruptedException e) {
					log.warn("Study Pk caching failed! Ignored!",e);
				}
			}
		}
		
		return studyHome.findByPrimaryKey((Long)pksStudy.get(studyIuid)); 
	}
	
	public static SeriesLocal findBySeriesIuid(SeriesLocalHome seriesHome, String seriesIuid) throws FinderException
	{
		return instance._findBySeriesIuid(seriesHome, seriesIuid);
	}
	
	protected SeriesLocal _findBySeriesIuid(SeriesLocalHome seriesHome, String seriesIuid) throws FinderException
	{
		synchronized(pksSeries)
		{
			if(!pksSeries.containsKey(seriesIuid))
			{
				Long pk = seriesHome.findBySeriesIuid(seriesIuid).getPk();
				pksSeries.put(seriesIuid, pk);
				try {
					qSeries.offer(seriesIuid,100);
				} catch (InterruptedException e) {
					log.warn("Series Pk caching failed! Ignored!", e);
				}
			}
		}
		
		return seriesHome.findByPrimaryKey((Long)pksSeries.get(seriesIuid)); 
	}
	
	private void scavenge()
	{
		synchronized(pksStudy)
		{
			doScavenge(pksStudy, qStudy, maxStudyIuidCacheSize);
		}
		
		synchronized(pksSeries)
		{
			doScavenge(pksSeries, qSeries, maxSeriesIuidCacheSize);
		}
	}
	
	private void doScavenge(Map pks, WaitFreeQueue q, int maxCacheSize)
	{
		for(int i = pks.size(); i > maxCacheSize; i--)
		{
			String iuid = null;
			try {
				iuid = (String) q.poll(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(iuid != null)
				pks.remove(iuid);
			else
				break;
		}
	}

	public int getMaxSeriesIuidCacheSize() {
		return maxSeriesIuidCacheSize;
	}

	public void setMaxSeriesIuidCacheSize(int maxSeriesIuidCacheSize) {
		this.maxSeriesIuidCacheSize = maxSeriesIuidCacheSize;
	}

	public int getMaxStudyIuidCacheSize() {
		return maxStudyIuidCacheSize;
	}

	public void setMaxStudyIuidCacheSize(int maxStudyIuidCacheSize) {
		this.maxStudyIuidCacheSize = maxStudyIuidCacheSize;
	}

	public long getScavengerInterval() {
		return scavengerInterval;
	}

	public void setScavengerInterval(long scavengerInterval) {
		if(this.scavengerInterval != scavengerInterval)
		{
			this.scavengerInterval = scavengerInterval;
			stop();
			start();
		}
	}
}
