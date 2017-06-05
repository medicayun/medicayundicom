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
	public static final String DEFAULT_WADO_SUBDIR = "default";
	public static final String DEFAULT_RID_SUBDIR = "rid";
	public static final String DEFAULT_WADO_EXT_SUBDIR = "wfind";

	private static WADOCacheImpl singletonWADO = null;
	private static WADOCacheImpl singletonRID = null;
	private static WADOCacheImpl singletonWADOExt = null;
	
	private static Logger log = Logger.getLogger( WADOCacheImpl.class.getName() );
	
	/** the root folder where this cache stores the image files. */
	private File cacheRoot = null;
	
	/** The config string that is used to set cacheRoot. */
	private String cacheRootCfgString = null;

	/** 
	 * Default subdirectory. this is used to split directory structer for WADO and RID cache 
	 * and for WADO cache to split 'default' requests and requests with rows and columns.
	 */
	private String defaultSubdir = "default";

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
	public static WADOCache getWADOCache() {
		if ( singletonWADO == null ) {
			singletonWADO = new WADOCacheImpl();
			singletonWADO.defaultSubdir = DEFAULT_WADO_SUBDIR;
		}
		return singletonWADO;
	}

	/**
	 * Returns the singleton instance of WADOCache.
	 * @return
	 */
	public static WADOCache getRIDCache() {
		if ( singletonRID == null ) {
			singletonRID = new WADOCacheImpl();
			singletonRID.defaultSubdir = DEFAULT_RID_SUBDIR;
		}
		return singletonRID;
	}

	/**
	 * Returns the singleton instance of WADOCache.
	 * @return
	 */
	public static WADOCache getWADOExtCache() {
		if ( singletonWADOExt == null ) {
			singletonWADOExt = new WADOCacheImpl();
			singletonWADOExt.defaultSubdir = DEFAULT_WADO_EXT_SUBDIR;
		}
		return singletonWADOExt;
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
		File file = this._getImageFile( defaultSubdir, studyUID, seriesUID, instanceUID, null );
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
		File file = this._getImageFile( defaultSubdir, studyUID, seriesUID, instanceUID, null );
		_writeImageFile( image, file);
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
		File file = this._getImageFile( rows+"-"+columns, studyUID, seriesUID, instanceUID, null );
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
		File file = this._getImageFile( rows+"-"+columns, studyUID, seriesUID, instanceUID, null );
		_writeImageFile( image, file);
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
			file = this._getImageFile( defaultSubdir, studyUID, seriesUID, instanceUID, null );
		} else {
			file = this._getImageFile( rows+"-"+columns, studyUID, seriesUID, instanceUID, null );
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
	 * Return the File object to get or store a file for given arguments.
	 * <p>
	 * If the cache object referenced with arguments is'nt in this cache the returned file object
	 * exists() method will result false!
	 * @param studyUID		Unique identifier of the study.
	 * @param seriesUID		Unique identifier of the series.
	 * @param instanceUID	Unique identifier of the instance.
	 * 
	 * @return File object to get or store a file.
	 */
	public File getFileObject( String studyUID, String seriesUID, String instanceUID, String contentType ) {
		return this._getImageFile( defaultSubdir, studyUID, seriesUID, instanceUID, contentType );
	}

	
	/**
	 * Clears this cache.
	 * <p>
	 * Remove all images in this cache.
	 */
	public void clearCache() {
		log.info("Clear cache called: cacheRoot:"+getAbsCacheRoot() );
		if ( this == singletonWADO ) {
			log.info("Clear WADO cache!");
			File[] files = getAbsCacheRoot().listFiles();
			if ( files == null ) {
				log.warn("WADO cache not cleared! Reason: cache root is not a directory or cant be read.");
				return;
			}
			for ( int i = 0, len = files.length ; i < len ; i++ ) {
				if ( ! files[i].getName().equals( DEFAULT_RID_SUBDIR ) ) {
					delTree( files[i] );
					if ( ! files[i].getName().equals( DEFAULT_WADO_SUBDIR ) ) {//dont del default dir
						files[i].delete();
					}
				}
			}
		} else if (this == singletonWADOExt ) {
			log.info("Clear WADOExt cache!");
			delTree( new File( this.getAbsCacheRoot(), DEFAULT_WADO_EXT_SUBDIR ) );
		} else {
			log.info("Clear RID cache!");
			delTree( new File( this.getAbsCacheRoot(), DEFAULT_RID_SUBDIR ) );
		}
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
	 * Removes old entries of this chache to free disk space.
	 * <p>
	 * This method can be called to run on the same thread ( e.g. if started via JMX console) or 
	 * in a seperate thread (if clean is handled automatically by WADOCache).
	 * <DL>
	 * <DD> 1) check if clean is necessary: <code>showFreeSpace &lt; getMinFreeSpace </code></DD>
	 * <DD> 2) delete the oldest files until <code>showFreeSpace &gt; getPreferredFreeSpace</code></DD>
	 * </DL>
	 * 
	 * @param	background	If true, clean runs in a seperate thread.
	 */
	public void freeDiskSpace( boolean background ) {
		long currFree = showFreeSpace();
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
		log.info( "Free disk space for "+( this==singletonWADO ? "WADO":"RID" )+" cache!");
		FileToDelContainer ftd = new FileToDelContainer( new File( getAbsCacheRoot(), defaultSubdir ), sizeToDel );
		if ( this == singletonWADO) {
			File[] files = getAbsCacheRoot().listFiles();
			if ( files != null ) {
				for ( int i = 0, len = files.length ; i < len ; i++ ) {
					if ( ! files[i].getName().equals( DEFAULT_RID_SUBDIR ) && ! files[i].getName().equals( DEFAULT_WADO_SUBDIR ) ) {
						ftd.searchDirectory( new File( this.getAbsCacheRoot(), files[i].getName() ) );
					}
				}
			}
		}
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
		if ( dir.equals( getAbsCacheRoot() ) ) return;
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
	public long showFreeSpace() {
		se.mog.io.File file = new se.mog.io.File( getAbsCacheRoot() );
		log.info("getFreeDiskSpace from :"+getAbsCacheRoot()+" free:"+file.getDiskSpaceAvailable() );
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
	 * @param subdir 		The subdirectory
	 * @param studyUID		Unique identifier of the study.
	 * @param seriesUID		Unique identifier of the series.
	 * @param instanceUID	Unique identifier of the instance.
	 * @param contentType TODO
	 * 
	 * @return
	 */
	private File _getImageFile( String subdir, String studyUID, String seriesUID, String instanceUID, String contentType ) {
		if ( contentType == null ) contentType = "image/jpg";//use jpg instead of jpeg here because for extension jpeg is set to jpg.
		File file = getAbsCacheRoot();
		if ( subdir != null )
			file = new File( this.getAbsCacheRoot(), subdir ); 
		String ext = getFileExtension( contentType );
		if ( ext.length() < 1 )
			file = new File( file, contentType.replace('/', '_') );
		if ( studyUID != null ) 
			file = new File( file, _getSubDirName( studyUID ) );
		if ( seriesUID != null )
			file = new File( file, _getSubDirName( seriesUID ) );
		file = new File( file, _getSubDirName( instanceUID )+ext ); 
		return file;
	}

	/**
	 * @param contentType
	 * @return
	 */
	private String getFileExtension(String contentType) {
		int pos = contentType.indexOf("/");
		String ext = "";
		if ( pos != -1 ) {
			ext = contentType.substring( pos+1 );
			if ( ext.equalsIgnoreCase("jpeg") ) ext = "jpg";
			else if ( ext.equalsIgnoreCase("svg+xml") ) ext = "svg";
			//do some other mapping here;
			ext = "." + ext;
		}
		return ext;
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
