/*
 * Created on 09.12.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.dcm4chex.wado.mbean;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.dcm4chex.wado.common.WADORequestObject;
import org.dcm4chex.wado.common.WADOResponseObject;
import org.dcm4chex.wado.mbean.cache.WADOCacheImpl;

/**
 * @author franz.willer 
 *
 * The MBean to manage the WADO service.
 * <p>
 * This class use WADOSupport for the WADO methods and WADOCache for caching jpg images.
 */
public class WADOService extends AbstractCacheService {

	private WADOSupport support = new WADOSupport( this.server );

	public WADOService() {
		cache = WADOCacheImpl.getWADOCache();
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

}
