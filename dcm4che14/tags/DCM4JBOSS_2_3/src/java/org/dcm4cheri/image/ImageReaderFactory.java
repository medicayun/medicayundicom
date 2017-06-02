/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4cheri.image;

import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 3847 $ $Date: 2004-12-24 00:51:09 +0800 (周五, 24 12月 2004) $
 * @since 22.12.2004
 */
public class ImageReaderFactory {
        
    private static final ImageReaderFactory instance = new ImageReaderFactory();
    public static final ImageReaderFactory getInstance() {
        return instance;
    }
    
    private final Properties map = new Properties();
        
    private ImageReaderFactory() {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try {
            map.load(loader.getResourceAsStream(
                    "org/dcm4cheri/image/ImageReaderFactory.properties"));
        } catch (IOException e) {
            throw new ConfigurationException("failed not load resource:", e); 
        }
    }
    
    public ImageReader getReaderForTransferSyntax(String tsuid) {
        String s = map.getProperty(tsuid);
        if (s == null)
            throw new UnsupportedOperationException(
                    "No Image Reader available for Transfer Syntax:" + tsuid);
        int delim = s.indexOf(',');
        if (delim == -1)
            throw new ConfigurationException("Missing ',' in " + tsuid + "=" + s); 
        final String formatName = s.substring(0, delim);
        final String className = s.substring(delim+1);
        for (Iterator it = ImageIO.getImageReadersByFormatName(formatName);
        		it.hasNext();) {
		    ImageReader r = (ImageReader) it.next();
		    if (className.equals(r.getClass().getName()))
		            return r;
        }
    	throw new ConfigurationException("No Image Reader for format:" + formatName);        
    }
    
    static class ConfigurationException extends RuntimeException {
        ConfigurationException(String msg) {
            super(msg);
        }
        ConfigurationException(String msg, Exception x) {
            super(msg,x);
        }
    }
}
