/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.notif;

import java.io.Serializable;
import java.util.Map;

/**
 * @author franz.willer
 * @version $Revision: 1913 $ $Date: 2005-08-25 16:32:49 +0800 (周四, 25 8月 2005) $
 * @since 18.08.2005
 */
public class IANNotificationVO implements Serializable{

	/**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 200508170001L;
	private Map ians;
	private String calledAET;
	private String callingAET;

	/**
	 * @param ians
	 * @param calledAET
	 * @param callingAET
	 */
	public IANNotificationVO(Map ians, String calledAET, String callingAET) {
		this.ians = ians;
		this.calledAET = calledAET;
		this.callingAET = callingAET;
	}
	public String getCallingAET() {
		return callingAET;
	}
	public String getCalledAET() {
		return calledAET;
	}
	
	public Map getIANs() {
		return ians;
	}
	
}
