/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.common;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1489 $ $Date: 2005-02-05 01:46:16 +0800 (周六, 05 2月 2005) $
 * @since 06.10.2004
 *
 */
public class Availability {

	public static final int ONLINE = 0;
	public static final int NEARLINE = 1;
	public static final int OFFLINE = 2;
	public static final int UNAVAILABLE = 3;

	private static final String[] AVAILABILITY = { "ONLINE", "NEARLINE",
            "OFFLINE", "UNAVAILABLE"};

    public static final String toString(int value) {
        return AVAILABILITY[value];
    }

    private final int value;

    public Availability(int value) {
        if (value < 0 || value >= AVAILABILITY.length)
                throw new IllegalArgumentException("value: " + value);
        this.value = value;
    }

    public final String toString() {
        return AVAILABILITY[value];
    }

    public final int getValue() {
        return value;
    }
}