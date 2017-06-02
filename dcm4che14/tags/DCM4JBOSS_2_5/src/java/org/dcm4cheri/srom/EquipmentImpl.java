/*$Id: EquipmentImpl.java 3656 2003-06-09 17:17:51Z gunterze $*/
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

package org.dcm4cheri.srom;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmValueException;
import org.dcm4che.dict.Tags;
import org.dcm4che.srom.Equipment;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0
 */
final class EquipmentImpl implements Equipment {
    
    // Constants -----------------------------------------------------
    
    // Attributes ----------------------------------------------------
    private String manufacturer;
    private String institutionName;
    private String institutionAddress;
    private String stationName;
    private String departmentName;
    private String modelName;

    // Constructor --------------------------------------------------------
    public EquipmentImpl(String manufacturer, String modelName,
            String stationName) {
        this.manufacturer = manufacturer;
        this.modelName = modelName;
        this.stationName = stationName;
    }

    public EquipmentImpl(Equipment other) {
        this(other.getManufacturer(), other.getModelName(), 
                other.getStationName());
        this.institutionName = other.getInstitutionName();
        this.institutionAddress = other.getInstitutionAddress();
        this.departmentName = other.getDepartmentName();
    }
    
    public EquipmentImpl(Dataset ds) throws DcmValueException {
        this(ds.getString(Tags.Manufacturer),
            ds.getString(Tags.ManufacturerModelName),
            ds.getString(Tags.StationName));
        this.institutionName = ds.getString(Tags.InstitutionName);
        this.institutionAddress = ds.getString(Tags.InstitutionAddress);
        this.departmentName = ds.getString(Tags.InstitutionalDepartmentName);
    }
    
    // Public --------------------------------------------------------
    
    public final String getManufacturer() {
        return manufacturer;
    }
    
    public final void setManufacturer(String manufacturer) {
        if (manufacturer == null)
            throw new NullPointerException();
        this.manufacturer = manufacturer;
    }
    
    public final String getInstitutionName() {
        return institutionName;
    }
    
    public final void setInstitutionName(String institutionName){
        this.institutionName = institutionName;
    }
    
    public final String getInstitutionAddress() {
        return institutionAddress;
    }
    
    public final void setInstitutionAddress(String institutionAddress){
        this.institutionAddress = institutionAddress;
    }
    
    public final String getStationName() {
        return stationName;
    }
    
    public final void setStationName(String stationName){
        this.stationName = stationName;
    }
    
    public final String getDepartmentName() {
        return departmentName;
    }
    
    public final void setDepartmentName(String departmentName){
        this.departmentName = departmentName;
    }
    
    public final String getModelName() {
        return modelName;
    }
    
    public final void setModelName(String modelName){
        this.modelName = modelName;
    }
    
    public String toString() {
        return "Equipment[manufacturer=" + manufacturer
                      + ",station=" + stationName
                      + ",model=" + modelName
                      + "]";
    }

    public void toDataset(Dataset ds) {
        ds.putLO(Tags.Manufacturer, manufacturer);
        if (institutionName != null)
            ds.putLO(Tags.InstitutionName, institutionName);
        if (institutionAddress != null)
            ds.putST(Tags.InstitutionAddress, institutionAddress);
        if (stationName != null)
            ds.putSH(Tags.StationName, stationName);
        if (departmentName != null)
            ds.putLO(Tags.InstitutionalDepartmentName, departmentName);
        if (modelName != null)
            ds.putLO(Tags.ManufacturerModelName, modelName);
    }
}
