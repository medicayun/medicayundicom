/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.cdw.common;


/**
 * @author gunter.zeilinter@tiani.com
 * @version $Revision: 4127 $ $Date: 2004-06-24 21:34:08 +0800 (周四, 24 6月 2004) $
 * @since 22.06.2004
 *
 */
public class ConfigurationException extends RuntimeException {

    /**
     * 
     */
    public ConfigurationException() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @param message
     */
    public ConfigurationException(String message) {
        super(message);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param cause
     */
    public ConfigurationException(Throwable cause) {
        super(cause);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param message
     * @param cause
     */
    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
        // TODO Auto-generated constructor stub
    }

}
