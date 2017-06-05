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

/**
 * @author franz.willer@gwi-ag.com
 * @version $Revision: 2076 $ $Date: 2005-11-17 01:19:36 +0800 (周四, 17 11月 2005) $
 * @since Nov 9, 2005
 */
public class SeriesUpdated implements Serializable {
	
	private static final long serialVersionUID = 3689069551690199604L;

	private String seriesIUID;
	private String description;
	private String retrieveAET;

	public SeriesUpdated(String seriesIUID, String description, String retrieveAET) {
		this.seriesIUID = seriesIUID;
		this.description = description;
		this.retrieveAET = retrieveAET;
	}

	/**
	 * @return Returns the seriesIUID.
	 */
	public String getSeriesIUID() {
		return seriesIUID;
	}
	
	/**
	 * @return Returns the description.
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @return Returns the retrieveAET.
	 */
	public String getRetrieveAET() {
		return retrieveAET;
	}
}
