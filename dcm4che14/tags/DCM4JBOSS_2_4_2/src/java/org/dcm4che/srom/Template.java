/*$Id: Template.java 3493 2002-07-14 16:03:36Z gunterze $*/
/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2001,2002 by TIANI MEDGRAPH AG <gunter.zeilinger@tiani.com> *
 *                                                                           *
 *  This file is part of dcm4che.                                            *
 *                                                                           *
 *  This library is free software; you can redistribute it and/or modify it  *
 *  under the terms of the GNU Lesser General Public License as published    *
 *  by the Free Software Foundation; either version 2 of the License, or     *
 *  (at your option) any later version.                                      *
 *                                                                           *
 *  This library is distributed in the hope that it will be useful, but      *
 *  WITHOUT ANY WARRANTY; without even the implied warranty of               *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        *
 *  Lesser General Public License for more details.                          *
 *                                                                           *
 *  You should have received a copy of the GNU Lesser General Public         *
 *  License along with this library; if not, write to the Free Software      *
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA  *
 *                                                                           *
 *****************************************************************************/

package org.dcm4che.srom;

import org.dcm4che.data.Dataset;

import java.util.Date;


/**
 * The <code>Template</code> interface represents a
 * <i>DICOM SR Template</i>.
 * <br>
 * <br>
 * <cite>
 * A Template for SR Documents defines a set of constraints on the 
 * relationships and content (Value Types, Codes, etc.) of Content Items 
 * that reference such a Template. Specific Templates for SR Documents are 
 * defined either by the DICOM Standard or by users of the Standard for 
 * particular purposes.
 * </cite>
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0
 *
 * @see "DICOM Part 3: Information Object Definitions,
 * 9 TEMPLATE IDENTIFICATION MACRO"
 */
public interface Template {
    // Constants -----------------------------------------------------
    
    // Public --------------------------------------------------------
  
    /**
     * Returns the value of field <i>Template Identifier</i>.
     * <br>DICOM Tag: <code>(0040,DB00)</code>
     * <br>Tag Name: <code>Template Identifier</code>
     * <br>
     * Template identifier.
     * 
     * @return  the value of field <i>Template Identifier</i>.
     */
    public String getTemplateIdentifier();
    
    /**
     * Returns the value of field <i>Mapping Resource</i>.
     * <br>DICOM Tag: <code>(0008,0105)</code>
     * <br>Tag Name: <code>Mapping Resource</code>
     * <br>
     * Mapping Resource that defines the template. 
     *
     * @return  the value of field <i>Mapping Resource</i>.
     *
     * @see "DICOM Part 3: Information Object Definitions,
     * 8.4 MAPPING RESOURCE"
     */
    public String getMappingResource();
    
    /**
     * Returns the value of field <i>Template Version</i>.
     * <br>DICOM Tag: <code>(0040,DB06)</code>
     * <br>Tag Name: <code>Template Version</code>
     * <br>
     * Version of the Template. 
     * Required if the <i>Template Identifier</i> <code>(0040,DB00)</code> 
     * and <i>Mapping Resource</i> <code>(0008,0105)</code> are not sufficient 
     * to identify the template unambiguously.
     *
     * @return  the value of field <i>Template Version</i>.
     */
    public Date getTemplateVersion();
    
    /**
     * Returns the value of field <i>Template Local Version</i>.
     * <br>DICOM Tag: <code>(0040,DB07)</code>
     * <br>Tag Name: <code>Template Local Version</code>
     * <br>
     * Local version number assigned to a template that contains
     * private extensions. Required if the value of 
     * <i>Template Extension Flag</i> <code>(0040,DB0B)</code> 
     * is "<code>Y</code>".
     *
     * @return  the value of field <i>Template Local Version</i>.
     */
    public Date getTemplateLocalVersion();
    
    /**
     * Compares two <code>Template</code>s for equality.
     *
     * @param obj  the <code>Template</code> object to compare this
     *             object for equality with.
     *
     * @return <code>true</code> if <code>obj</code> has the same values
     *         as this <code>Template</code> object <code>false</code> 
     *         otherwise.
     */
    public boolean equals(Object obj);
    
    public void toDataset(Dataset ds);
    
}//end interface Template
