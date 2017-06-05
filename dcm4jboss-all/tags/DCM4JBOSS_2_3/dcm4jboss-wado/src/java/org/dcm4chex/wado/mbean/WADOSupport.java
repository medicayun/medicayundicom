/*
 * Created on 10.12.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.dcm4chex.wado.mbean;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.dcm4chex.wado.common.WADORequestObject;
import org.dcm4chex.wado.common.WADOResponseObject;
import org.dcm4chex.wado.mbean.cache.WADOCache;
import org.dcm4chex.wado.mbean.cache.WADOCacheImpl;
import org.jboss.mx.util.MBeanServerLocator;

/**
 * @author franz.willer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WADOSupport {
	
public static final String CONTENT_TYPE_JPEG = "image/jpeg";
public static final String CONTENT_TYPE_DICOM = "application/dicom";

private static Logger log = Logger.getLogger( WADOService.class.getName() );

private static ObjectName fileSystemMgtName = null;

private static MBeanServer server;

private static final int BUF_LEN = 65536;

public WADOSupport( MBeanServer mbServer ) {
	if ( server != null ) {
		server = mbServer;
	} else {
		server = MBeanServerLocator.locate();
	}
}

/**
 * Handles a WADO request and returns a WADO response object.
 * <p>
 * </DL>
 * <DT>If the request was successfull:</DT>
 * <DD>	The WADO response object contains a File and a corresponding content type.</DD>
 * <DD>	the return code was OK and the error message <code>null</code>.</DD>
 * <DT>If the requested object is not local:</DT>
 * <DD>	If <code>WADOCacheImpl.isClientRedirect() is false</code> the server tries to connect via WADO to the remote WADO server to get the object.</DD>
 * <DD> if clientRedirect is enabled, the WADOResponse object return code is set to <code>HttpServletResponse.SC_TEMPORARY_REDIRECT</code> and 
 *      error message is set to the hostname to redirect.</DD>
 * <DT>If the request was not successfull (not found or an error):</DT>
 * <DD>	The return code of the WADO response object is set to a http error code and an error message was set.</DD>
 * <DD>	The file of the WADO response is <code>null</code>. The content type is not specified for this case.</DD>
 * </DL> 
 * @param req The WADO request object.
 * 
 * @return The WADO response object.
 */
public WADOResponseObject getWADOObject( WADORequestObject req ) {
	List contentTypes = req.getContentTypes();
	if ( contentTypes == null || contentTypes.contains( CONTENT_TYPE_JPEG ) ) {
		return this.handleJpg( req );
	} else if ( contentTypes.contains( CONTENT_TYPE_DICOM ) ) {
		return handleDicom( req );
	} else {
		return new WADOResponseObjectImpl( null, CONTENT_TYPE_DICOM, HttpServletResponse.SC_NOT_IMPLEMENTED, "This method is not implemented!");
		
	}
}

public WADOResponseObject handleDicom( WADORequestObject req ) {
	File file = null;
	try {
		file = this.getDICOMFile( req.getStudyUID(), req.getSeriesUID(), req.getObjectUID() );
		if ( file == null ) {
			if ( log.isDebugEnabled() ) log.debug("Dicom object not found: "+req);
			return new WADOResponseObjectImpl( null, CONTENT_TYPE_DICOM, HttpServletResponse.SC_NOT_FOUND, "DICOM object not found!");
		}
	} catch (IOException x) {
		log.error("Exception in handleDicom: "+x.getMessage(), x);
		return new WADOResponseObjectImpl( null, CONTENT_TYPE_DICOM, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unexpected error! Cant get dicom object");
	} catch ( NeedRedirectionException nre ) {
		if ( ! WADOCacheImpl.getWADOCache().isClientRedirect() )  {
			return getRemoteDICOMFile( nre.getHostname(), req);
		} else {
			return new WADOResponseObjectImpl( null, CONTENT_TYPE_DICOM, HttpServletResponse.SC_TEMPORARY_REDIRECT, getRedirectURL( nre.getHostname(), req ).toString() ); //error message is set to redirect host!
		}
	}
	try {
		return new WADOResponseObjectImpl( new FileInputStream( file ), CONTENT_TYPE_DICOM, HttpServletResponse.SC_OK, null);
	} catch (FileNotFoundException x) {
		log.error("Exception in handleDicom: "+x.getMessage(), x);
		return new WADOResponseObjectImpl( null, CONTENT_TYPE_DICOM, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unexpected error! Cant get dicom object");
	}	
}

/**
 * Handles a request for content type image/jpeg.
 * <p>
 * Use this method first if conetnt type jpeg is possible to get advantage of the cache.
 * <p>
 * 
 * @param studyUID		The unique id of a study.
 * @param seriesUID		The unique id of a series.
 * @param instanceUID	The unique id of an instance.
 * @param rows			The number of pixel rows (integer String)
 * @param columns		the number of pixel columns (integer String)
 * 
 * @return	The WADO response object containing the file of the image.
 */
public WADOResponseObject handleJpg( WADORequestObject req ){
	String studyUID = req.getStudyUID();
	String seriesUID = req.getSeriesUID();
	String instanceUID = req.getObjectUID();
	String rows = req.getRows();
	String columns = req.getColumns();
	String frameNumber = req.getFrameNumber();
	try {
		File file = getJpg( studyUID, seriesUID, instanceUID, rows, columns, frameNumber );
		if ( file != null ) {
			return new WADOResponseObjectImpl( new FileInputStream( file ), CONTENT_TYPE_JPEG, HttpServletResponse.SC_OK, null);
		} else {
			return new WADOResponseObjectImpl( null, CONTENT_TYPE_JPEG, HttpServletResponse.SC_NOT_FOUND, "DICOM object not found!");
		}
	} catch ( NeedRedirectionException nre ) {
		if ( ! WADOCacheImpl.getWADOCache().isClientRedirect() )  {
			return getRemoteDICOMFile( nre.getHostname(), req);
		} else {
			return new WADOResponseObjectImpl( null, CONTENT_TYPE_JPEG, HttpServletResponse.SC_TEMPORARY_REDIRECT, getRedirectURL( nre.getHostname(), req ).toString() ); //error message is set to redirect host!
		}
	} catch ( NoImageException x1 ) {
		return new WADOResponseObjectImpl( null, CONTENT_TYPE_JPEG, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Cant get jpeg from requested object");		
	} catch ( Exception x ) {
		log.error("Exception in handleJpg: "+x.getMessage(), x);
		return new WADOResponseObjectImpl( null, CONTENT_TYPE_JPEG, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unexpected error! Cant get jpeg");
	}
}
public File getJpg( String studyUID, String seriesUID, String instanceUID,
								String rows, String columns, String frameNumber	) throws IOException, NeedRedirectionException, NoImageException {
	int frame = 0;
	if ( frameNumber != null ) {
		frame = Integer.parseInt( frameNumber );
	}
	WADOCache cache = WADOCacheImpl.getWADOCache();
	File file;
	BufferedImage bi = null;
	if ( rows == null ) {
		file = cache.getImageFile( studyUID, seriesUID, instanceUID );
	} else {
		file = cache.getImageFile( studyUID, seriesUID, instanceUID, rows, columns );
	}
	if ( file == null ) {
		File dicomFile = getDICOMFile( studyUID, seriesUID, instanceUID );
		if ( dicomFile != null ) {
			bi = getImage( dicomFile, frame, rows, columns );
		} else {
			return null;
		}
		if ( bi != null ) {
			if ( rows == null ) {
				file = cache.putImage( bi, studyUID, seriesUID, instanceUID );
			} else {
				file = cache.putImage( bi, studyUID, seriesUID, instanceUID, rows, columns );
			}
		} else {
			throw new NoImageException();
		}
	}
		
	return file;
}
/*_*/

/**
 * Returns the DICOM file for given arguments.
 * <p>
 * Use the FileSystemMgtService MBean to localize the DICOM file.
 * 
 * @param studyUID		Unique identifier of the study.
 * @param seriesUID		Unique identifier of the series.
 * @param instanceUID	Unique identifier of the instance.
 * 
 * @return The File object or null if not found.
 * 
 * @throws IOException
 */
public File getDICOMFile( String studyUID, String seriesUID, String instanceUID ) throws IOException, NeedRedirectionException {
    File file;
    Object dicomObject = null;
	try {
        dicomObject = server.invoke(fileSystemMgtName,
                "locateInstance",
                new Object[] { instanceUID },
                new String[] { String.class.getName() } );
        
    } catch (Exception e) {
        log.error("Failed to get DICOM file", e);
    }
    if ( dicomObject == null ) return null; //not found!
    if ( dicomObject instanceof File ) return (File) dicomObject; //We have the File!
    if ( dicomObject instanceof String ) {
    	throw new NeedRedirectionException( (String) dicomObject );
    }
	return null;
}

private URL getRedirectURL( String hostname, WADORequestObject req ) {
	StringBuffer sb = new StringBuffer();
	sb.append( "/dcm4jboss-wado/wado?requestType=WADO");
	Map mapParam = req.getRequestParams();
	Iterator iter = mapParam.keySet().iterator();
	Object key;
	while ( iter.hasNext() ) {
		key = iter.next();
		sb.append("&").append(key).append("=").append( ( (String[]) mapParam.get(key))[0] );
	}
	URL url = null;
	try {
		url = new URL("http",hostname,8080, sb.toString() );
	} catch (MalformedURLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	if (log.isDebugEnabled() ) log.debug("redirect url:"+url );
	return url;
}

/**
 * Tries to get the DICOM file from an external WADO service.
 * 
 * @param hostname		Hostname of remote WADO service.
 * @param studyUID		Unique identifier of the study.
 * @param seriesUID		Unique identifier of the series.
 * @param instanceUID	Unique identifier of the instance.
 * 
 * @return The File object or null if not found.
 */
private WADOResponseObject getRemoteDICOMFile(String hostname, WADORequestObject req ) {
	if ( log.isInfoEnabled() ) log.info("WADO request redirected to hostname:"+hostname);
	URL url = null;
	try {
		url = getRedirectURL( hostname, req );
		if (log.isDebugEnabled() ) log.debug("redirect url:"+url );
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		String authHeader = (String)req.getRequestHeaders().get("Authorization");
		if ( authHeader != null ) {
			conn.addRequestProperty("Authorization", authHeader );
		}
		conn.connect();
		if (log.isDebugEnabled() ) log.debug("conn.getResponseCode():"+conn.getResponseCode() );
		if ( conn.getResponseCode() != HttpServletResponse.SC_OK ) {
			if (log.isInfoEnabled() ) log.info("Remote WADO server responses with:"+conn.getResponseMessage() );
			return new WADOResponseObjectImpl( null, conn.getContentType(), conn.getResponseCode(), conn.getResponseMessage() );
		}
		InputStream is = conn.getInputStream();
		if ( WADOCacheImpl.getWADOCache().isRedirectCaching() && CONTENT_TYPE_JPEG.equals( conn.getContentType() ) ) {
			File file = WADOCacheImpl.getWADOCache().putStream( is, req.getStudyUID(), 
													req.getSeriesUID(), 
													req.getObjectUID(), 
													req.getRows(), 
													req.getColumns() );
			is = new FileInputStream( file );
		}
		return new WADOResponseObjectImpl( is, conn.getContentType(), HttpServletResponse.SC_OK, null);
	} catch (Exception e) {
		log.error("Can't connect to remote WADO service:"+url, e);
		e.printStackTrace();
		return null;
	}
}

/**
 * Get the image from DICOM file.
 * <p>
 * If <code>rows or columns</code> not null, the original image will be scaled.
 * 
 * @param file		A DICOM file.
 * @param frame
 * @param rows			Image height in pixel.
 * @param columns		Image width in pixel.
 *
 * @return
 * @throws IOException
 */
private BufferedImage getImage(File file, int frame, String rows, String columns) throws IOException {
    Iterator it = ImageIO.getImageReadersByFormatName("DICOM");
    if (!it.hasNext())
            return null; //TODO more usefull stuff
    ImageReader reader = (ImageReader) it.next();
    ImageInputStream in = new FileImageInputStream( file );
    reader.setInput( in );
    BufferedImage bi = null;
    try {
    	bi = reader.read( frame );
    } catch ( Exception x ) {
    	if (log.isDebugEnabled()) log.debug("Cant read image:", x);
    	return null;
    }
	if ( rows != null || columns != null ) {
		bi = resize( bi, rows, columns );
	}
	return bi;
}

/**
 * Resize the given image.
 * 
 * @param bi		The image as BufferedImage.
 * @param rows			Image height in pixel.
 * @param columns		Image width in pixel.
 * 
 * @return
 */
private BufferedImage resize( BufferedImage bi, String rows, String columns ) {
	int h0 = bi.getHeight();
	int w0 = bi.getWidth();
	double ratio = (double)w0 / h0;
	int newH = -1 , newW = -1;
	if ( rows != null ) {
		newH = Integer.parseInt( rows );
	}
	if ( columns != null) {
		newW = Integer.parseInt( columns );
	}
	if ( newW == -1 ) 
		newW = (int) (newH * ratio);
	else if ( newH == -1 ) 
		newH = (int) (newW / ratio);
	int w = newW;
	int h = newH;
    w = (int) Math.min(w, h * ratio);
    h = (int) Math.min(h, w / ratio);
	if ( w != w0 || h != h0 ) {
        AffineTransform scale = AffineTransform.getScaleInstance((double) w
                / w0, (double) h / h0);
        AffineTransformOp scaleOp = new AffineTransformOp(scale,
                AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        BufferedImage biDest = scaleOp.filter( bi, null );
		return biDest;
    } else {
    	return bi;
    }
}

/**
 * Set the name of the FileSystemMgtBean.
 * <p>
 * This bean is used to retrieve the DICOM object.
 * 
 * @param fileSystemMgtName The fileSystemMgtName to set.
 */
public void setFileSystemMgtName(ObjectName fileSystemMgtName) {
	WADOSupport.fileSystemMgtName = fileSystemMgtName;
}

/**
 * Get the name of the FileSystemMgtBean.
 * <p>
 * This bean is used to retrieve the DICOM object.
 * 
 * @return Returns the fileSystemMgtName.
 */
public ObjectName getFileSystemMgtName() {
	return fileSystemMgtName;
}

/**
 * Inner exception class to handle WADO redirection.
 *  
 * @author franz.willer
 *
 * Holds the hostname of the WADO server that have direct access of the requested object.
 */
class NeedRedirectionException extends Exception {

	/** Comment for <code>serialVersionUID</code> */
	private static final long serialVersionUID = 1L;
	/** holds the hostname to redirect */
	private String hostname;

	/**
	 * Creates a NeedRedirectionException instance.
	 * 
	 * @param hostname the target of redirection.
	 */
	public NeedRedirectionException( String hostname ) {
		this.hostname = hostname;
	}
	
	/**
	 * Returns the hostname to redirect.
	 * 
	 * @return
	 */
	public String getHostname() {
		return this.hostname;
	}
}



/**
 * Inner exception class to handle error if DCM object is not a image.
 *  
 * @author franz.willer
 *
 */
class NoImageException extends Exception {

	/**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 1L;

}

}

