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

/**
 * @author franz.willer
 *
 * This Interface defines the basic methods of an Icon cache for DICOM images.
 * <p>
 * The icons are represented as File objects to allow streaming of the image data.
 */
public interface IconCache {

	/**
	 * Get an icon as BufferedImage from cache.
	 * <p>
	 * This method returns the icon from the default path of this cache.<br>
	 * 
	 * @param studyUID		Unique identifier of the study.
	 * @param seriesUID		Unique identifier of the series.
	 * @param instanceUID	Unique identifier of the instance.
	 * 
	 * @return	The image if in cache or null.
	 */
	BufferedImage getIcon( String studyUID, String seriesUID, String instanceUID );
	
	/**
	 * Get an icon as file from cache.
	 * <p>
	 * This method returns the icon from the default path of this cache.<br>
	 * 
	 * @param studyUID		Unique identifier of the study.
	 * @param seriesUID		Unique identifier of the series.
	 * @param instanceUID	Unique identifier of the instance.
	 * 
	 * @return	The File of the image if in cache or null.
	 */
	File getIconFile( String studyUID, String seriesUID, String instanceUID );
	
	/**
	 * Put an icon as BufferedImage to the cache.
	 * <p>
	 * Stores the image on the default path of this cache.
	 * 
	 * @param image			The image of the icon.
	 * @param studyUID		Unique identifier of the study.
	 * @param seriesUID		Unique identifier of the series.
	 * @param instanceUID	Unique identifier of the instance.
	 * 
	 * @return The File object of the image in this cache.
	 * 
	 * @throws IOException
	 */
	File putIcon( BufferedImage image, String studyUID, String seriesUID, String instanceUID ) throws IOException;
	
	/**
	 * Get an icon of special size from cache.
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
	BufferedImage getIcon( String studyUID, String seriesUID, String instanceUID, String rows, String columns );

	/**
	 * Get an icon of special size from cache.
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
	File getIconFile( String studyUID, String seriesUID, String instanceUID, String rows, String columns );

	/**
	 * Put an icon of special size to this cache.
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
	File putIcon( BufferedImage image, String studyUID, String seriesUID, String instanceUID, String pixelRows, String pixelColumns ) throws IOException;
	
	/**
	 * Clears this cache.
	 * <p>
	 * Remove all icons in this cache.
	 */
	void clearCache();
	
	/**
	 * Removes old entries to shrink this cache.
	 * 
	 * @param background If true clean process runs in a seperate thread.
	 */
	void cleanCache( boolean background );
	
	
	void setCacheRoot( String newRoot );
	
	File getCacheRoot();
	
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
	
}
