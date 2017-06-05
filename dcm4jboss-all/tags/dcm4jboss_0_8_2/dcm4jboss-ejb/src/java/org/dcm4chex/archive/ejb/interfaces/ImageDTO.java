/* $Id: ImageDTO.java 945 2004-02-01 14:26:22Z gunterze $
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

/**
 * 
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 945 $ $Date: 2004-02-01 22:26:22 +0800 (周日, 01 2月 2004) $
 * @since 30.01.2004
 */
public class ImageDTO extends InstanceDTO {
    private String imageType;
    private String photometricInterpretation;
    private int rows;
    private int columns;
    private int numberOfFrames;
    private int bitsAllocated;
    
    /**
     * @return
     */
    public final int getBitsAllocated() {
        return bitsAllocated;
    }

    /**
     * @param bitsAllocated
     */
    public final void setBitsAllocated(int bitsAllocated) {
        this.bitsAllocated = bitsAllocated;
    }

    /**
     * @return
     */
    public final int getColumns() {
        return columns;
    }

    /**
     * @param columns
     */
    public final void setColumns(int columns) {
        this.columns = columns;
    }

    /**
     * @return
     */
    public final String getImageType() {
        return imageType;
    }

    /**
     * @param imageType
     */
    public final void setImageType(String imageType) {
        this.imageType = imageType;
    }

    /**
     * @return
     */
    public final int getNumberOfFrames() {
        return numberOfFrames;
    }

    /**
     * @param numberOfFrames
     */
    public final void setNumberOfFrames(int numberOfFrames) {
        this.numberOfFrames = numberOfFrames;
    }

    /**
     * @return
     */
    public final String getPhotometricInterpretation() {
        return photometricInterpretation;
    }

    /**
     * @param photometricInterpretation
     */
    public final void setPhotometricInterpretation(String photometricInterpretation) {
        this.photometricInterpretation = photometricInterpretation;
    }

    /**
     * @return
     */
    public final int getRows() {
        return rows;
    }

    /**
     * @param rows
     */
    public final void setRows(int rows) {
        this.rows = rows;
    }

}
