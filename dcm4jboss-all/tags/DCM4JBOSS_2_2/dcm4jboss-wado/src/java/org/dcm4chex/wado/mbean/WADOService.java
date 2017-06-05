/*
 * Created on 09.12.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.dcm4chex.wado.mbean;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.log4j.Logger;
import org.dcm4chex.wado.common.WADORequestObject;
import org.dcm4chex.wado.common.WADOResponseObject;
import org.dcm4chex.wado.mbean.cache.WADOCache;
import org.dcm4chex.wado.mbean.cache.WADOCacheImpl;
import org.jboss.system.ServiceMBeanSupport;

/**
 * @author franz.willer
 *
 * The MBean to manage the WADO service.
 * <p>
 * This class use WADOSupport for the WADO methods and WADOCache for caching jpg images.
 */
public class WADOService extends ServiceMBeanSupport  {

	private Logger log = Logger.getLogger( getClass().getName() );
	private WADOCache cache = WADOCacheImpl.getInstance();
	private WADOSupport support = new WADOSupport( this.server );
	
	private static final long GIGA = 1000000000L;
	private static final long MEGA = 1000000L;
	
	public void setCacheRoot( String newRoot ) {
		cache.setCacheRoot( newRoot );
	}
	
	public String getCacheRoot() {
		return cache.getCacheRoot();
	}
	
	public void setMinFreeSpace( int minFree ) {
		cache.setMinFreeSpace( minFree * MEGA );
	}
	
	public int getMinFreeSpace() {
		return (int) (cache.getMinFreeSpace() / MEGA);
	}

	public void setPreferredFreeSpace( int minFree ) {
		cache.setPreferredFreeSpace( minFree * MEGA );
	}
	
	public int getPreferredFreeSpace() {
		return (int) (cache.getPreferredFreeSpace() / MEGA);
	}
	
	/**
	 * @return Returns the clientRedirect.
	 */
	public boolean isClientRedirect() {
		return cache.isClientRedirect();
	}
	/**
	 * @param clientRedirect The clientRedirect to set.
	 */
	public void setClientRedirect(boolean clientRedirect) {
		cache.setClientRedirect( clientRedirect );
	}
	/**
	 * @return Returns the redirectCaching.
	 */
	public boolean isRedirectCaching() {
		return cache.isRedirectCaching();
	}
	/**
	 * @param redirectCaching The redirectCaching to set.
	 */
	public void setRedirectCaching(boolean redirectCaching) {
		cache.setRedirectCaching( redirectCaching );
	}
	
	/**
	 * Clears the cache.
	 * <p>
	 * Remove all files in the cache.
	 *
	 */
	public void clearCache() {
		cache.clearCache();
	}
	
	/**
	 * Cleans the cache if necessary.
	 *
	 */
	public void cleanCache() {
		cache.cleanCache( false ); //cleans the cache and wait until clean process is done. (not in background)
	}
	
	/**
	 * Returns the available space of the cache drive.
	 * <p>
	 * 
	 * @return
	 */
	public String getFreeSpace() {
		return getSizeWithUnit( cache.getFreeSpace() );
	}
	
	/**
	 * Getter for the name of the FileSystemMgt MBean.
	 * <p>
	 * This bean is used to locate the DICOM file.
	 *  
	 * @return Name of the MBean
	 */
	public String getFileSystemMgtName() {
		return support.getFileSystemMgtName().toString();
	}
	
	/**
	 * Setter for the name of the FileSystemMgt MBean.
	 * <p>
	 * This bean is used to locate the DICOM file.
	 *  
	 * @param Name of the MBean
	 */
	public void setFileSystemMgtName( String name ) {
		try {
			ObjectName on = new ObjectName( name );
			support.setFileSystemMgtName( on );
		} catch (MalformedObjectNameException e) {
		}
	}
	
	/**
	 * Get the requested DICOM object as File packed in a WADOResponseObject.
	 * <p>
	 *  
	 * @param reqVO The request parameters packed in an value object.
	 * 
	 * @return The value object containing the retrieved object or an error.
	 */
	public WADOResponseObject getWADOObject( WADORequestObject reqVO ) {
		return support.getWADOObject( reqVO );
	}

	/**
	 * Returns a String representation with trailing (G|M|K)B.
	 * <p>
	 * Rounds the value to a more readable form.
	 * <p>
	 * Use 1000 instead of 1024 for one KB.
	 * 
	 * @param size the size in bytes.
	 * 
	 * @return The size with unit. e.g. 2GB
	 */
	private String getSizeWithUnit(long size) {
		if ( size >= GIGA ){
				return (size/GIGA)+" GB";
		} else if ( size > MEGA ) {
			if ( (size % MEGA) == 0 ) {
				return (size/MEGA)+" MB";
			} else {
				return (size/1000L)+" KB";
			}
		} else {
			return (size/1000L)+" KB";
		}
	}

}
