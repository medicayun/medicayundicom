/* $Id: InstanceDTO.java 945 2004-02-01 14:26:22Z gunterze $
 * Copyright (c) 2002,2003 by TIANI MEDGRAPH AG
 *
 * This file is part of dcm4che.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.dcm4chex.archive.ejb.interfaces;

import java.io.Serializable;

/**
 * 
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 945 $ $Date: 2004-02-01 22:26:22 +0800 (周日, 01 2月 2004) $
 * @since 14.01.2004
 */
public class InstanceDTO implements Serializable {

    public static final String DATETIME_FORMAT = "yyyy/MM/dd hh:mm:ss";

    private int pk;
    private String instanceNumber;
    private String sopIUID;
    private String sopCUID;
    private String contentDateTime;
    private String retrieveAETs;
    private int numberOfFiles;

    /**
     * @return
     */
    public final int getPk() {
        return pk;
    }

    /**
     * @param pk
     */
    public final void setPk(int pk) {
        this.pk = pk;
    }

    /**
     * @return
     */
    public final String getInstanceNumber() {
        return instanceNumber;
    }

    /**
     * @param instanceNumber
     */
    public final void setInstanceNumber(String instanceNumber) {
        this.instanceNumber = instanceNumber;
    }

    /**
     * @return
     */
    public final String getContentDateTime() {
        return contentDateTime;
    }

    /**
     * @param contentDateTime
     */
    public final void setContentDateTime(String contentDateTime) {
        this.contentDateTime = contentDateTime;
    }

    /**
     * @return
     */
    public final String getSopCUID() {
        return sopCUID;
    }

    /**
     * @param sopCUID
     */
    public final void setSopCUID(String sopCUID) {
        this.sopCUID = sopCUID;
    }

    /**
     * @return
     */
    public final String getSopIUID() {
        return sopIUID;
    }

    /**
     * @param sopIUID
     */
    public final void setSopIUID(String sopIUID) {
        this.sopIUID = sopIUID;
    }

    /**
     * @return
     */
    public String getRetrieveAETs() {
        return retrieveAETs;
    }

    /**
     * @param retrieveAETs
     */
    public void setRetrieveAETs(String retrieveAETs) {
        this.retrieveAETs = retrieveAETs;
    }

    /**
     * @return
     */
    public final int getNumberOfFiles() {
        return numberOfFiles;
    }

    /**
     * @param numberOfFiles
     */
    public final void setNumberOfFiles(int numberOfFiles) {
        this.numberOfFiles = numberOfFiles;
    }

}
