package org.dcm4chex.archive.ejb.util;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

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
 * We use a LinkedHashMap to get a ordered and 'bounded' map.
 * <p>
 * @author fang.yang@agfa.com
 * @version $Id: EntityPkCache.java 2572 2006-06-27 16:09:11Z javawilli $
 * @since May 18, 2006
 *
 */
public class EntityPkCache {

	private static Logger log = Logger.getLogger(EntityPkCache.class);

	private static final EntityPkCache instance = new EntityPkCache();
	
	private int maxCacheSize = 512; 	// lucky number

	/**
	 * Key: directory path of file system, unique identifier
	 * Value: Primary Key, Long
	 */
	private Map pksCache = Collections.synchronizedMap(new LinkedHashMap( maxCacheSize, 0.5f, true ) {
		     protected boolean removeEldestEntry(Map.Entry eldest) {
		        return size() > maxCacheSize;
		     }
		} );

	
	private EntityPkCache(){
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
		synchronized(pksCache)
		{
			if(pksCache.containsKey(dirPath)) {
				try {
					return fileSystemHome.findByPrimaryKey((Long)pksCache.get(dirPath));
				} catch ( FinderException outdated ) {}
			}
			Long pk = fileSystemHome.findByDirectoryPath(dirPath).getPk();
			pksCache.put(dirPath, pk);
		}
		return fileSystemHome.findByPrimaryKey((Long)pksCache.get(dirPath)); 
	}
	
	public static StudyLocal findByStudyIuid(StudyLocalHome studyHome, String studyIuid) throws FinderException
	{
		return instance._findByStudyIuid(studyHome, studyIuid);
	}
	
	protected StudyLocal _findByStudyIuid(StudyLocalHome studyHome, String studyIuid) throws FinderException
	{
		synchronized(pksCache)
		{
			if(pksCache.containsKey(studyIuid)) {
				try {
					return studyHome.findByPrimaryKey((Long)pksCache.get(studyIuid)); 
				} catch ( FinderException outdated ) {}
			}
			Long pk = studyHome.findByStudyIuid(studyIuid).getPk();
			pksCache.put(studyIuid, pk);
		}
		return studyHome.findByPrimaryKey((Long)pksCache.get(studyIuid)); 
	}
	
	public static SeriesLocal findBySeriesIuid(SeriesLocalHome seriesHome, String seriesIuid) throws FinderException
	{
		return instance._findBySeriesIuid(seriesHome, seriesIuid);
	}
	
	protected SeriesLocal _findBySeriesIuid(SeriesLocalHome seriesHome, String seriesIuid) throws FinderException
	{
		synchronized(pksCache)
		{
			if(pksCache.containsKey(seriesIuid)) {
				try {
					return seriesHome.findByPrimaryKey((Long)pksCache.get(seriesIuid)); 
				} catch ( FinderException outdated ) {}
			}
			Long pk = seriesHome.findBySeriesIuid(seriesIuid).getPk();
			pksCache.put(seriesIuid, pk);
		}
		return seriesHome.findByPrimaryKey((Long)pksCache.get(seriesIuid)); 
	}
	
	/**
	 * @return Returns the maxFSCacheSize.
	 */
	public int getMaxCacheSize() {
		return maxCacheSize;
	}
	/**
	 * @param maxFSCacheSize The maxFSCacheSize to set.
	 */
	public void setMaxCacheSize(int maxCacheSize) {
		this.maxCacheSize = maxCacheSize;
	}


}
