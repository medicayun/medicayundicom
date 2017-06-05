/* $Id: PresentationStateDTO.java 945 2004-02-01 14:26:22Z gunterze $
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
public class PresentationStateDTO extends InstanceDTO {
    
    private String presentationCreationDateTime;
    private String presentationLabel;
    private String presentationCreatorName;
    private String presentationDescription;
    private int numberOfReferencedImages;
    /**
     * @return
     */
    public final int getNumberOfReferencedImages() {
        return numberOfReferencedImages;
    }

    /**
     * @param numberOfReferencedImages
     */
    public final void setNumberOfReferencedImages(int numberOfReferencedImages) {
        this.numberOfReferencedImages = numberOfReferencedImages;
    }

    /**
     * @return
     */
    public final String getPresentationCreationDateTime() {
        return presentationCreationDateTime;
    }

    /**
     * @param presentationCreationDateTime
     */
    public final void setPresentationCreationDateTime(String presentationCreationDateTime) {
        this.presentationCreationDateTime = presentationCreationDateTime;
    }

    /**
     * @return
     */
    public final String getPresentationCreatorName() {
        return presentationCreatorName;
    }

    /**
     * @param presentationCreatorName
     */
    public final void setPresentationCreatorName(String presentationCreatorName) {
        this.presentationCreatorName = presentationCreatorName;
    }

    /**
     * @return
     */
    public final String getPresentationDescription() {
        return presentationDescription;
    }

    /**
     * @param presentationDescription
     */
    public final void setPresentationDescription(String presentationDescription) {
        this.presentationDescription = presentationDescription;
    }

    /**
     * @return
     */
    public final String getPresentationLabel() {
        return presentationLabel;
    }

    /**
     * @param presentationLabel
     */
    public final void setPresentationLabel(String presentationLabel) {
        this.presentationLabel = presentationLabel;
    }

}
