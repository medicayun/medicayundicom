/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4chex.archive.config;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1972 $ $Date: 2005-09-13 23:12:26 +0800 (周二, 13 9月 2005) $
 * @since Sep 13, 2005
 */
public class DicomPriority {
	private static final String LOW = "LOW";
	private static final String HIGH = "HIGH";
	private static final String MEDIUM = "MEDIUM";
	private static final String[] MAP = { MEDIUM, HIGH, LOW };

	public static String toString(int code) {
		try {
			return MAP[code];
		} catch (IndexOutOfBoundsException e) {
			throw new IllegalArgumentException("code: " + code);
		}
	}

	public static int toCode(String s) {
		if (s.equalsIgnoreCase(HIGH))
			return 1;
		if (s.equalsIgnoreCase(LOW))
			return 2;
		return 0;
	}
	
}
