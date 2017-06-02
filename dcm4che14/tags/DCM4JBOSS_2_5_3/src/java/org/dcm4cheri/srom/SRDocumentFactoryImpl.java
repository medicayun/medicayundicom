/*$Id: SRDocumentFactoryImpl.java 3656 2003-06-09 17:17:51Z gunterze $*/
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

import org.dcm4che.srom.*;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.DcmValueException;

import java.util.Date;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0
 */
public class SRDocumentFactoryImpl extends SRDocumentFactory
{
    // Constants -----------------------------------------------------
    static final DcmObjectFactory dsfact = DcmObjectFactory.getInstance();
    
    // Attributes ----------------------------------------------------

    // Constructors --------------------------------------------------
    
    // Methodes --------------------------------------------------------
    public RefSOP newRefSOP(String refSOPClassUID, String refSOPInstanceUID) {
        return new RefSOPImpl(refSOPClassUID, refSOPInstanceUID);
    }

    public IconImage newIconImage(int rows, int columns, byte[] pixelData) {
        return new IconImageImpl(rows, columns, pixelData);
    }
        
    public Patient newPatient(String patientID, String patientName,
            Patient.Sex patientSex, Date patientBirthDate) {
        return new PatientImpl(patientID, patientName, patientSex,
                patientBirthDate);
    }
    
    public Study newStudy(String studyInstanceUID, String studyID,
            Date studyDateTime, String referringPhysicianName,
            String accessionNumber, String studyDescription, Code[] procCodes) {
        return new StudyImpl(studyInstanceUID, studyID, studyDateTime,
                referringPhysicianName, accessionNumber, studyDescription,
                procCodes);
    }
    
    public Series newSeries(String modality, String seriesInstanceUID,
            int seriesNumber, RefSOP refStudyComponent) {
        return new SeriesImpl(modality, seriesInstanceUID, seriesNumber,
                refStudyComponent);
    }
    
    public Series newSRSeries(String seriesInstanceUID, int seriesNumber,
            RefSOP refStudyComponent) {
        return new SeriesImpl("SR", seriesInstanceUID, seriesNumber,
                refStudyComponent);
    }
    
    public Series newKOSeries(String seriesInstanceUID, int seriesNumber,
            RefSOP refStudyComponent) {
        return new SeriesImpl("KO", seriesInstanceUID, seriesNumber,
                refStudyComponent);
    }

    public Equipment newEquipment(String manufacturer, String modelName,
            String stationName) {
        return new EquipmentImpl(manufacturer, modelName, stationName);
    }

    public Code newCode(String codeValue, String codingSchemeDesignator,
            String codingSchemeVersion, String codeMeaning) {
        return new CodeImpl(codeValue, codingSchemeDesignator,
                codingSchemeVersion, codeMeaning);
    }

    public Code newCode(String codeValue, String codingSchemeDesignator,
            String codeMeaning) {
        return new CodeImpl(codeValue, codingSchemeDesignator,
                null, codeMeaning);
    }

    public Template newTemplate(String templateIdentifier,
            String mappingResource, Date templateVersion,
            Date templateLocalVersion) {
        return new TemplateImpl(templateIdentifier, mappingResource,
                templateVersion, templateLocalVersion);
    }

    public Template newTemplate(String templateIdentifier,
            String mappingResource) {
        return new TemplateImpl(templateIdentifier, mappingResource, null, null);
    }

    public Verification newVerification(Date time, String observerName,
            String observerOrg, Code observerCode) {
        return new VerificationImpl(time, observerName, observerOrg, observerCode);
    }
    
    public Request newRequest(String studyInstanceUID,
            String accessionNumber, String fillerOrderNumber,
            String placerOrderNumber, String procedureID,
            String procedureDescription, Code procedureCode) {
        return new RequestImpl(studyInstanceUID, accessionNumber,
                fillerOrderNumber, placerOrderNumber, procedureID,
                procedureDescription, procedureCode);
    }

    public SOPInstanceRef newSOPInstanceRef(String sopClassUID,
            String sopInstanceUID, String seriesInstanceUID,
            String studyInstanceUID) {
        return new SOPInstanceRefImpl(sopClassUID, sopInstanceUID,
                seriesInstanceUID, studyInstanceUID);
    }
    
    public TCoordContent.Positions.Sample newSamplePositions(int[] indexes) {
        return new TCoordContentImpl.SamplePositions(indexes);
    }

    public TCoordContent.Positions.Relative newRelativePositions(float[] offsets) {
        return new TCoordContentImpl.RelativePositions(offsets);
    }

    public TCoordContent.Positions.Absolute newAbsolutePositions(Date[] dateTimes) {
        return new TCoordContentImpl.AbsolutePositions(dateTimes);
    }
    
    public KeyObject newKeyObject(Patient patient, Study study,
            Series series, Equipment equipment, String sopInstanceUID,
            int instanceNumber, Date obsDateTime, Code title,
            boolean separate) {
        return new KeyObjectImpl(patient, study, series, equipment,
                sopInstanceUID, instanceNumber, obsDateTime, title, separate);
    }

    public SRDocument newSRDocument(Patient patient, Study study,
            Series series, Equipment equipment, String sopClassUID,
            String sopInstanceUID, int instanceNumber, Date obsDateTime,
            Template template, Code title, boolean separate) {
        return new SRDocumentImpl(patient, study, series, equipment,
                sopClassUID, sopInstanceUID, instanceNumber,
                obsDateTime, template, title, separate);
    }

    public Patient newPatient(Dataset ds) throws DcmValueException {
        return new PatientImpl(ds);
    }

    public Study newStudy(Dataset ds) throws DcmValueException {
        return new StudyImpl(ds);
    }
    
    public Series newSeries(Dataset ds) throws DcmValueException {
        return new SeriesImpl(ds);
    }

    public Equipment newEquipment(Dataset ds) throws DcmValueException {
        return new EquipmentImpl(ds);
    }

    public Code newCode(Dataset ds) throws DcmValueException {
        return CodeImpl.newCode(ds);
    }

    public RefSOP newRefSOP(Dataset ds) throws DcmValueException {
        return RefSOPImpl.newRefSOP(ds);
    }
    
    public Template newTemplate(Dataset ds) throws DcmValueException {
        return TemplateImpl.newTemplate(ds);
    }

    public SRDocument newSRDocument(Dataset ds) throws DcmValueException {
        return SRDocumentImpl.newSRDocument(ds);
    }

    public KeyObject newKeyObject(Dataset ds) throws DcmValueException {
        return KeyObjectImpl.newKeyObject(ds);
    }
    
    public IconImage newIconImage(Dataset ds) throws DcmValueException {
        return IconImageImpl.newIconImage(ds);
    }
    
    public HL7SRExport newHL7SRExport(
            String sendingApplication, String sendingFacility,
            String receivingApplication, String receivingFacility) {
        return new HL7SRExportImpl(sendingApplication, sendingFacility,
            receivingApplication, receivingFacility);
    }
}
