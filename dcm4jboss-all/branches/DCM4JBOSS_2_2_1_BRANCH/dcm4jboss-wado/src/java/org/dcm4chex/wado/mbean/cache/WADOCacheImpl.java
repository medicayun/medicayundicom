/*
 * Created on 03.12.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.dcm4chex.wado.mbean.cache;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.jboss.system.server.ServerConfigLocator;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

/**
 * @author franz.willer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WADOCacheImpl implements WADOCache {

	public static final String DEFAULT_CACHE_ROOT = "/wadocache";

	private static WADOCacheImpl singleton = null;
	
	private static Logger log = Logger.getLogger( WADOCacheImpl.class.getName() );
	
	/** the root folder where this cache stores the image files. */
	private File cacheRoot = null;
	
	/** The config string that is used to set cacheRoot. */
	private String cacheRootCfgString = null;
	
	/** holds the min cache size. */
	private long preferredFreeSpace = 300000000;

	/** holds the max cache size. */
	private long minFreeSpace = 200000000;
	
	/** Flag to indicate if client side redirection should be used if DICOM object is not locally available. */
	private boolean clientRedirect = false;
	
	/** Flag to indicate if caching should be used in case of server side redirect, */
	private boolean redirectCaching = true;
	
	/** Buffer size for read/write */
	private static final int BUF_LEN = 65536;
	
	/**
	 * Creates a WADOCacheImpl instance.. 
	 */
	private WADOCacheImpl() {
		
	}
	
	/**
	 * Returns the singleton instance of WADOCache.
	 * @return
	 */
	public static WADOCache getInstance() {
		if ( singleton == null ) {
			singleton = new WADOCacheImpl();
		}
		return singleton;
	}
	
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
	public BufferedImage getImage(String studyUID, String seriesUID, String instanceUID) {
		return _readJpgFile( getImageFile( studyUID, seriesUID, instanceUID ) );
	}

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
	public File getImageFile(String studyUID, String seriesUID, String instanceUID) {
		File file = this._getImageFile( "default", studyUID, seriesUID, instanceUID );
		if ( file.exists() ) {
			file.setLastModified( System.currentTimeMillis() ); //set last modified because File has only lastModified timestamp visible.
			return file;
		} else {
			return null;
		}
	}

	/**
	 * Put an image as BufferedImage to the cache.
	 * <p>
	 * Stores the image on the default path of this cache.
	 * 
	 * @param image			The image as BufferedImage.
	 * @param studyUID		Unique identifier of the study.
	 * @param seriesUID		Unique identifier of the series.
	 * @param instanceUID	Unique identifier of the instance.
	 * 
	 * @return The File object of the image in this cache.
	 * 
	 * @throws IOException
	 */
	public File putImage( BufferedImage image, String studyUID, String seriesUID, String instanceUID) throws IOException {
		File file = this._getImageFile( "default", studyUID, seriesUID, instanceUID );
		_writeImageFile( image, file);
		cleanCache( true );
		return file;
	}

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
	public BufferedImage getImage(String studyUID, String seriesUID, String instanceUID, String rows, String columns) {
		return _readJpgFile( getImageFile( studyUID, seriesUID, instanceUID, rows, columns ) );
	}

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
	public File getImageFile(String studyUID, String seriesUID, String instanceUID, String rows, String columns) {
		File file = this._getImageFile( rows+"-"+columns, studyUID, seriesUID, instanceUID );
		if ( file.exists() ) {
			file.setLastModified( System.currentTimeMillis() ); //set last modified because File has only lastModified timestamp visible.
			return file;
		} else {
			return null;
		}
	}

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
	public File putImage( BufferedImage image, String studyUID, String seriesUID,
			String instanceUID, String rows, String columns) throws IOException {
		File file = this._getImageFile( rows+"-"+columns, studyUID, seriesUID, instanceUID );
		_writeImageFile( image, file);
		cleanCache( true );
		return file;
	}

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
	public File putStream( InputStream stream, String studyUID, String seriesUID, String instanceUID, String rows, String columns ) throws IOException {
		File file;
		if ( rows == null && columns == null ) {
			file = this._getImageFile( "default", studyUID, seriesUID, instanceUID );
		} else {
			file = this._getImageFile( rows+"-"+columns, studyUID, seriesUID, instanceUID );
		}
		if ( ! file.getParentFile().exists() ) {
			file.getParentFile().mkdirs();
		}
		
		BufferedOutputStream out = new BufferedOutputStream( new FileOutputStream( file ) );
		byte[] buf = new byte[BUF_LEN];
		try {
			int len = stream.read( buf );
			while ( len > 0 ) {
				out.write( buf, 0, len );
				len = stream.read( buf );
			}
		} finally {
			stream.close();
			out.flush();
			out.close();
		}
		return file; 
	}
	
	/**
	 * Clears this cache.
	 * <p>
	 * Remove all images in this cache.
	 */
	public void clearCache() {
		log.info("Clear cache called: cacheRoot:"+getAbsCacheRoot() );
		delTree( this.getAbsCacheRoot() );
	}

	/**
	 * Deletes all files in given directory.
	 * <p>
	 * Do nothing if <code>file</code> is not a directory or is <code>null</code>!
	 * 
	 * @param file A directory.
	 */
	public static void delTree(File file) {
		if ( file == null ) return;
		if ( !file.isDirectory() ) return;
		
		File[] files = file.listFiles();
		if ( files == null ) {
			log.warn("WADOCache: File "+file+" should be a directory! But listFiles returns null!");
			return;
		}
		for ( int i = 0, len = files.length ; i < len ; i++ ) {
			if ( files[i].isDirectory() ) {
				delTree( files[i] );
				if ( ! files[i].delete() ) {
					log.warn("WADOCache: Diretory can't be deleted:"+files[i]);
				}
			} else {
				if ( ! files[i].delete() ) {
					log.warn("WADOCache: File can't be deleted:"+files[i]);
				}
			}
		}
	}

	/**
	 * Removes old entries to shrink this cache.
	 * <p>
	 * This method can be called to run on the same thread ( e.g. if started via JMX console) or 
	 * in a seperate thread (if clean is handled automatically by WADOCache).
	 * <DL>
	 * <DD> 1) check if clean is necessary: <code>getFreeSpace &lt; getMinFreeSpace </code></DD>
	 * <DD> 2) delete the oldest files until <code>getFreeSpace &gt; getPreferredFreeSpace</code></DD>
	 * </DL>
	 * 
	 * @param	background	If true, clean runs in a seperate thread.
	 */
	public void cleanCache( boolean background ) {
		long currFree = getFreeSpace();
		if ( log.isDebugEnabled() ) log.debug("WADOCache.cleancache: free:"+currFree+" minFreeSpace:"+getMinFreeSpace() );
		if ( currFree < getMinFreeSpace() ) {
			final long sizeToDel = getPreferredFreeSpace() - currFree;
			if ( background ) {
		        Thread t = new Thread(new Runnable() {
		            public void run() {
	                	_clean( sizeToDel );
		            }
		        });
		        t.start();
			} else {
            	_clean( sizeToDel );
			}
		} else {
			if ( log.isDebugEnabled() ) log.debug("WADOCache.cleancache: nothing todo" );
		}
	}

	/**
	 * Deletes old files to free the given amount of disk space.
	 * <p>
	 * If a directory is empty after deleting a file, the directory will also be deleted.
	 */
	private void _clean(long sizeToDel ) {
		FileToDelContainer ftd = new FileToDelContainer( this.getAbsCacheRoot(), sizeToDel );
		Iterator iter = ftd.getFilesToDelete().iterator();
		File file;
		while ( iter.hasNext() ) {
			file = (File) iter.next();
			file.delete();
			_deleteDirWhenEmpty( file.getParentFile() );
		}
	}
	
	/**
	 * Deletes the given directory and all parents if they are empty.
	 * 
	 * @param dir Directory
	 */
	private void _deleteDirWhenEmpty( File dir ) {
		if ( dir == null ) return;
		File[] files = dir.listFiles();
		if ( files != null && files.length == 0 ) {
			dir.delete();
			_deleteDirWhenEmpty( dir.getParentFile() );
		}
	}

	/**
	 * Returns the min drive space that must be available on the caches drive.
	 * <p>
	 * This value is used to determine if this cache should be cleaned.
	 * 
	 * @return Min allowed free diskspace in bytes.
	 */
	public long getMinFreeSpace() {
		return this.minFreeSpace;
	}

	/**
	 * Setter of minFreeSpace.
	 * 
	 * @param minFreeSpace The minFreeSpace to set.
	 */
	public void setMinFreeSpace(long minFreeSpace) {
		this.minFreeSpace = minFreeSpace;
	}
	
	/**
	 * Returns the free space that should be remain after cleaning the cache.
	 * <p>
	 * This value is used as lower watermark of the cleaning process.
	 * 
	 * @return Preferred free diskspace in bytes.
	 */
	public long getPreferredFreeSpace() {
		return this.preferredFreeSpace;
	}
	
	/**
	 * Setter of preferredFreeSpace.
	 * 
	 * @param preferredFreeSpace The preferredFreeSpace to set.
	 */
	public void setPreferredFreeSpace(long preferredFreeSpace) {
		this.preferredFreeSpace = preferredFreeSpace;
	}
	
	/**
	 * Returns current free disk space in bytes.
	 * 
	 * @return disk space available on the drive where this cache is stored.
	 */
	public long getFreeSpace() {
		se.mog.io.File file = new se.mog.io.File( getAbsCacheRoot() );
		return file.getDiskSpaceAvailable();
	}

	/**
	 * Returns the root directory of this cache.
	 * <p>
	 * Returns the absolute or relative path as set with setCacheRoot.
	 * 
	 * @return root directory of this cache (absolute or relative).
	 */
	public String getCacheRoot() {
		if ( cacheRootCfgString == null ) {
			setCacheRoot( DEFAULT_CACHE_ROOT );
		}
		return cacheRootCfgString;
	}
	
	public File getAbsCacheRoot() {
		if ( cacheRoot == null ) setCacheRoot( DEFAULT_CACHE_ROOT );
		return cacheRoot;
	}
	
	public void setCacheRoot( String newRoot) {
		if ( newRoot == null ) return;
		cacheRootCfgString = newRoot;
		File newRootFile = new File( newRoot );
        if ( ! newRootFile.isAbsolute() ) {
        	try {
        		File serverHomeDir = ServerConfigLocator.locate().getServerHomeDir();
        		newRootFile = new File( serverHomeDir, newRoot );
        	} catch ( Throwable t ) {
        	}
        }
		if ( cacheRoot != null ) {
			try {
				cacheRoot.renameTo( newRootFile ); //move only possible if same partition.
			} catch (Throwable t) {
				//TODO copy cache to new dest?
				delTree( cacheRoot );
				cacheRoot.delete();
			}
		}
		cacheRoot = newRootFile;
		
		if ( ! cacheRoot.exists() )
			cacheRoot.mkdirs();
		
	}
	/**
	 * @return Returns the clientRedirect.
	 */
	public boolean isClientRedirect() {
		return clientRedirect;
	}
	/**
	 * @param clientRedirect The clientRedirect to set.
	 */
	public void setClientRedirect(boolean clientRedirect) {
		this.clientRedirect = clientRedirect;
	}
	/**
	 * @return Returns the redirectCaching.
	 */
	public boolean isRedirectCaching() {
		return redirectCaching;
	}
	/**
	 * @param redirectCaching The redirectCaching to set.
	 */
	public void setRedirectCaching(boolean redirectCaching) {
		this.redirectCaching = redirectCaching;
	}
	
	/**
	 * Returns the File object references the file where the image is placed within this cache.
	 * <p>
	 * The subdir argument is used to seperate default images and special sized images.
	 * <p>
	 * The directory and file names for studyID, seriesID and instaneID are calculated with
	 * <code>_getSubDirName</code>
	 * 
	 * <DL>
	 * <DT>The File object was build like:</DT>
	 * <DD> &lt;root&gt;/[&lt;subdir&gt;/&lt;]studyID&gt;/&lt;seriesID&gt;/&lt;instanceID&gt;</DD>
	 * </DL>
	 * 
	 * @param subdir 		The subdirectory
	 * @param studyUID		Unique identifier of the study.
	 * @param seriesUID		Unique identifier of the series.
	 * @param instanceUID	Unique identifier of the instance.
	 * 
	 * @return
	 */
	private File _getImageFile( String subdir, String studyUID, String seriesUID, String instanceUID ) {
		File file = getAbsCacheRoot();
		if ( subdir != null )
			file = new File( this.getAbsCacheRoot(), subdir ); 
		file = new File( file, _getSubDirName( studyUID ) );
		file = new File( file, _getSubDirName( seriesUID ) );
		file = new File( file, _getSubDirName( instanceUID )+".jpg" ); 
		return file;
	}

	/**
	 * Returns the directory name for given UID.
	 * <p>
	 * This current implementation use the UID as subdirectory name.
	 * 
	 * @param uid	A unique DICOM identifier
	 * 
	 * @return directory name.
	 */
	private String _getSubDirName(String uid) {
		// TODO calculate a shorter name (hash?)
		return uid;
	}

	/**
	 * Writes an image to the given file.
	 * 
	 * @param image	The image.
	 * @param file	The file within this cache to store the image.
	 * 
	 * @throws IOException
	 */
	private void _writeImageFile(BufferedImage bi, File file) throws IOException {
		if ( ! file.getParentFile().exists() ) {
			file.getParentFile().mkdirs();
		}
		OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
		try {
			JPEGImageEncoder enc = JPEGCodec.createJPEGEncoder(out);
			enc.encode(bi);
		} catch ( IOException x ) {
			if ( file.exists() ) file.delete();
			throw x;
		} finally {
			out.close();
		}
	}
	
	/**
	 * Reads an jpg file into a BufferedImage.
	 * 
	 * @param jpgFile
	 * @return BufferedImage from jpg file.
	 */
	private BufferedImage _readJpgFile(File jpgFile) {
		if ( jpgFile == null ) return null;
		//TODO real work!
		return null;
	}

}
