/*
 * Created on 03.12.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.dcm4chex.wado.mbean.cache;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author franz.willer
 *
 * This Interface defines the basic methods of an WADO cache for DICOM images.
 * <p>
 * The images are represented as File objects to allow streaming of the image data.
 */
public interface WADOCache {

	/**
	 * Get an image as BufferedImage from cache.
	 * <p>
	 * This method returns the image from the default path of this cache.<br>
	 * 
	 * @param studyUID		Unique identifier of the study.
	 * @param seriesUID		Unique identifier of the series.
	 * @param instanceUID	Unique identifier of the instance.
	 * 
	 * @return	The image if in cache or null.
	 */
	BufferedImage getImage( String studyUID, String seriesUID, String instanceUID );
	
	/**
	 * Get an image as file from cache.
	 * <p>
	 * This method returns the image from the default path of this cache.<br>
	 * 
	 * @param studyUID		Unique identifier of the study.
	 * @param seriesUID		Unique identifier of the series.
	 * @param instanceUID	Unique identifier of the instance.
	 * 
	 * @return	The File of the image if in cache or null.
	 */
	File getImageFile( String studyUID, String seriesUID, String instanceUID );
	
	/**
	 * Put an image as BufferedImage to the cache.
	 * <p>
	 * Stores the image on the default path of this cache.
	 * 
	 * @param image			The image.
	 * @param studyUID		Unique identifier of the study.
	 * @param seriesUID		Unique identifier of the series.
	 * @param instanceUID	Unique identifier of the instance.
	 * 
	 * @return The File object of the image in this cache.
	 * 
	 * @throws IOException
	 */
	File putImage( BufferedImage image, String studyUID, String seriesUID, String instanceUID ) throws IOException;
	
	/**
	 * Get an image of special size from cache.
	 * <p>
	 * This method use a image size (rows and columns) to search on a special path of this cache.
	 * 
	 * @param studyUID		Unique identifier of the study.
	 * @param seriesUID		Unique identifier of the series.
	 * @param instanceUID	Unique identifier of the instance.
	 * @param rows			Image height in pixel.
	 * @param columns		Image width in pixel.
	 * 
	 * @return				The image if in cache or null.
	 */
	BufferedImage getImage( String studyUID, String seriesUID, String instanceUID, String rows, String columns );

	/**
	 * Get an image of special size from cache.
	 * <p>
	 * This method use a image size (rows and columns) to search on a special path of this cache.
	 * 
	 * @param studyUID		Unique identifier of the study.
	 * @param seriesUID		Unique identifier of the series.
	 * @param instanceUID	Unique identifier of the instance.
	 * @param rows			Image height in pixel.
	 * @param columns		Image width in pixel.
	 * 
	 * @return				The File object of the image if in cache or null.
	 */
	File getImageFile( String studyUID, String seriesUID, String instanceUID, String rows, String columns );

	/**
	 * Put an image of special size to this cache.
	 * <p>
	 * Stores the image on a special path of this cache.
	 * 
	 * @param image			The image (with special size)
	 * @param studyUID		Unique identifier of the study.
	 * @param seriesUID		Unique identifier of the series.
	 * @param instanceUID	Unique identifier of the instance.
	 * @param rows			Image height in pixel.
	 * @param columns		Image width in pixel.
	 * 
	 * @return The File object of the image in this cache.
	 * @throws IOException
     */
	File putImage( BufferedImage image, String studyUID, String seriesUID, String instanceUID, String pixelRows, String pixelColumns ) throws IOException;
	
	/**
	 * Puts a stream to this cache.
	 * 
	 * @param stream		The InputStream to store.
	 * @param studyUID		Unique identifier of the study.
	 * @param seriesUID		Unique identifier of the series.
	 * @param instanceUID	Unique identifier of the instance.
	 * @param rows			Image height in pixel.
	 * @param columns		Image width in pixel.
	 * 
	 * @return	The stored File object.
	 * 
	 * @throws IOException
	 */
	File putStream( InputStream stream, String studyUID, String seriesUID, String instanceUID, String pixelRows, String pixelColumns ) throws IOException; 

	/**
	 * Return the File object to get or store a file for given arguments.
	 * <p>
	 * If the cache object referenced with arguments is'nt in this cache the returned file object
	 * exists() method will result false!
	 * @param studyUID		Unique identifier of the study.
	 * @param seriesUID		Unique identifier of the series.
	 * @param instanceUID	Unique identifier of the instance.
	 * @param key TODO
	 * 
	 * @return File object to get or store a file.
	 */
	File getFileObject( String studyUID, String seriesUID, String instanceUID, String key );
	
	/**
	 * Clears this cache.
	 * <p>
	 * Remove all images in this cache.
	 */
	void clearCache();
	
	/**
	 * Removes old entries to shrink this cache.
	 * 
	 * @param background If true clean process runs in a seperate thread.
	 */
	void freeDiskSpace( boolean background );
	
	/**
	 * Setter for root directory of this cache.
	 * <p>
	 * If a relative path is given, the resulting absolute path is relative to
	 * the servers home dir.
	 * 
	 * @param newRoot The root directory for this cache.
	 */
	void setCacheRoot( String newRoot );
	
	/**
	 * Getter for root directory of this cache.
	 * <p>
	 * This method returns always the same String used in setCacheRoot!
	 * 
	 * @return The root directory for this cache (relative or absolute)
	 */
	String getCacheRoot();

	/**
	 * Getter for the absolute root directory of this cache.
	 * 
	 * @return The root directory for this cache (absolute)
	 */
	File getAbsCacheRoot();

	/**
	 * Setter of minFreeSpace.
	 * 
	 * @param minFreeSpace The minFreeSpace to set.
	 */
	public void setMinFreeSpace(long minFreeSpace);
	
	/**
	 * Returns the min drive space that must be available on the caches drive.
	 * <p>
	 * This value is used to determine if this cache should be cleaned.
	 * 
	 * @return Min allowed free diskspace in bytes.
	 */
	long getMinFreeSpace();

	/**
	 * Setter of preferredFreeSpace.
	 * 
	 * @param preferredFreeSpace The preferredFreeSpace to set.
	 */
	void setPreferredFreeSpace(long preferredFreeSpace);
	
	/**
	 * Returns the free space that should be remain after cleaning the cache.
	 * <p>
	 * This value is used as lower watermark of the cleaning process.
	 * 
	 * @return Preferred free diskspace in bytes.
	 */
	long getPreferredFreeSpace();
	
	/**
	 * Returns the current free space on drive where this cache is stored.
	 * 
	 * @return Current free diskspace in bytes
	 */
	long showFreeSpace();
	
	/**
	 * Returns true if a client redirect should be used if requewsted DICOM object is not local.
	 * 
	 * @return True if client redirect should be used..
	 */
	public boolean isClientRedirect();
	
	/**
	 * Set the flag to determine if a client redirect should be used if  the requested DICOM object is not local.
	 * 
	 * @param clientRedirect True for client side redirect, false for server side redirect.
	 */
	public void setClientRedirect(boolean clientRedirect);

	/**
	 * Returns true if a server side redirect should be cached.
	 * 
	 * @return True if a server side redirected request should be cached.
	 */
	public boolean isRedirectCaching();

	/**
	 * Set the flag if caching is enabled for server side redirect.
	 * 
	 * @param redirectCaching True to enable caching.
	 */
	public void setRedirectCaching(boolean redirectCaching);
	
}
