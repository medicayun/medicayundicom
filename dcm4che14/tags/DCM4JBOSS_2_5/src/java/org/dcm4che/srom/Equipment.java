/*$Id: Equipment.java 3493 2002-07-14 16:03:36Z gunterze $*/
/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2001,2002 by TIANI MEDGRAPH AG <gunter.zeilinger@tiani.com>*
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

/**
 * The <code>Equipment</code> interface represents some of the fields of the
 * <i>DICOM General Equipment Module</i>.
 * 
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0
 *
 * @see "DICOM Part 3: Information Object Definitions,
 * Annex C.7.5.1 General Equipment Module"
 */
public interface Equipment {
    
    // Constants -----------------------------------------------------
    
    // Public --------------------------------------------------------

    /**
     * Returns the DICOM <i>Manufacturer</i>.
     * <br>DICOM Tag: <code>(0008,0070)</code>
     * <br>
     * Manufacturer of the equipment that produced the digital images.
     *
     * @return  the Manufacturer.
     */
    public String getManufacturer();
    
    /**
     * Sets the DICOM <i>Manufacturer</i>.
     * <br>DICOM Tag: <code>(0008,0070)</code>
     * <br>
     * Manufacturer of the equipment that produced the digital images.
     *
     * @param manufacturer  the Manufacturer.
     */
    public void setManufacturer(String manufacturer);
    
    /**
     * Returns the DICOM <i>Institution Name</i>.
     * <br>DICOM Tag: <code>(0008,0080)</code>
     * <br>
     * Institution where the equipment is located that produced the 
     * digital images.
     *
     * @return  the Institution Name.
     */
    public String getInstitutionName();
    
    /**
     * Sets the DICOM <i>Institution Name</i>.
     * <br>DICOM Tag: <code>(0008,0080)</code>
     * <br>
     * Institution where the equipment is located that produced the 
     * digital images.
     *
     * @param institutionName  the Institution Name.
     */
    public void setInstitutionName(String institutionName);
    
    /**
     * Returns the DICOM <i>Institution Address</i>.
     * <br>DICOM Tag: <code>(0008,0081)</code>
     * <br>
     * Mailing address of the institution where the equipment is 
     * located that produced the digital images.
     *
     * @return  the Institution Address.
     */
    public String getInstitutionAddress();
    
    /**
     * Sets the DICOM <i>Institution Address</i>.
     * <br>DICOM Tag: <code>(0008,0081)</code>
     * <br>
     * Mailing address of the institution where the equipment is 
     * located that produced the digital images.
     *
     * @param institutionAddress  the Institution Address.
     */
    public void setInstitutionAddress(String institutionAddress);
    
    /**
     * Returns the DICOM <i>Station Name</i>.
     * <br>DICOM Tag: <code>(0008,1010)</code>
     * <br>
     * User defined name identifying the machine that produced the 
     * digital images.
     *
     * @return  the Station Name.
     */
    public String getStationName();
    
    /**
     * Sets the DICOM <i>Station Name</i>.
     * <br>DICOM Tag: <code>(0008,1010)</code>
     * <br>
     * User defined name identifying the machine that produced the 
     * digital images.
     *
     * @param stationName  the Station Name.
     */
    public void setStationName(String stationName);
    
    /**
     * Returns the DICOM <i>Institutional Department Name</i>.
     * <br>DICOM Tag: <code>(0008,1040)</code>
     * <br>
     * Department in the institution where the equipment is located 
     * that produced the digital images.
     *
     * @return  the Institutional Department Name.
     */
    public String getDepartmentName();
    
    /**
     * Sets the DICOM <i>Institutional Department Name</i>.
     * <br>DICOM Tag: <code>(0008,1040)</code>
     * <br>
     * Department in the institution where the equipment is located 
     * that produced the digital images.
     *
     * @param departmentName  the Institutional Department Name.
     */
    public void setDepartmentName(String departmentName);
    
    /**
     * Returns the DICOM <i>Manufacturer's Model Name</i>.
     * <br>DICOM Tag: <code>(0008,1090)</code>
     * <br>
     * Manufacturer's model number of the equipment that produced 
     * the digital images.
     *
     * @return  the Manufacturer's Model Name.
     */
    public String getModelName();
    
    /**
     * Sets the DICOM <i>Manufacturer's Model Name</i>.
     * <br>DICOM Tag: <code>(0008,1090)</code>
     * <br>
     * Manufacturer's model number of the equipment that produced 
     * the digital images.
     *
     * @param modelName  the Manufacturer's Model Name.
     */
    public void setModelName(String modelName);
    
    public void toDataset(Dataset ds);
}//end interface Equipment
