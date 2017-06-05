/* $Id: DTOFactory.java 1025 2004-03-02 23:55:32Z gunterze $
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

import java.text.SimpleDateFormat;
import java.util.Date;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4cheri.util.StringUtils;

/**
 * 
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1025 $ $Date: 2004-03-03 07:55:32 +0800 (周三, 03 3月 2004) $
 * @since 21.01.2004
 */
public final class DTOFactory {

    private DTOFactory() {
    }

    public static PatientDTO newPatientDTO(int pk, Dataset ds) {
        PatientDTO pat = new PatientDTO();
        pat.setPk(pk);
        pat.setPatientID(ds.getString(Tags.PatientID, ""));
        pat.setPatientName(ds.getString(Tags.PatientName, ""));
        pat.setPatientBirthDate(
            format(ds.getDate(Tags.PatientBirthDate), PatientDTO.DATE_FORMAT));
        pat.setPatientSex(ds.getString(Tags.PatientSex, ""));
        return pat;
    }

    public static StudyDTO newStudyDTO(
        int pk,
        Dataset ds,
        String modsInStudy,
        int numSeries,
        int numInst,
        String retrieveAETs,
        int availability) {
        StudyDTO study = new StudyDTO();
        study.setPk(pk);
        study.setAccessionNumber(ds.getString(Tags.AccessionNumber, ""));
        study.setStudyID(ds.getString(Tags.StudyID, ""));
        study.setStudyIUID(ds.getString(Tags.StudyInstanceUID, ""));
        study.setStudyDateTime(
            format(
                ds.getDateTime(Tags.StudyDate, Tags.StudyTime),
                StudyDTO.DATETIME_FORMAT));
        study.setStudyDescription(ds.getString(Tags.StudyDescription, ""));
        study.setModalitiesInStudy(modsInStudy);
        study.setNumberOfSeries(numSeries);
        study.setNumberOfInstances(numInst);
        study.setRetrieveAETs(retrieveAETs);
        study.setAvailability(availability);
        return study;
    }

    public static SeriesDTO newSeriesDTO(
        int pk,
        Dataset ds,
        int numInst,
        String retrieveAETs,
        int availability) {
        SeriesDTO series = new SeriesDTO();
        series.setPk(pk);
        series.setModality(ds.getString(Tags.Modality, ""));
        series.setBodyPartExamined(ds.getString(Tags.BodyPartExamined, ""));
        series.setLaterality(ds.getString(Tags.Laterality, ""));
        series.setSeriesNumber(ds.getString(Tags.SeriesNumber, ""));
        series.setSeriesIUID(ds.getString(Tags.SeriesInstanceUID, ""));
        series.setSeriesDateTime(
            format(
                ds.getDateTime(Tags.SeriesDate, Tags.SeriesTime),
                SeriesDTO.DATETIME_FORMAT));
        series.setSeriesDescription(ds.getString(Tags.SeriesDescription, ""));
        series.setNumberOfInstances(numInst);
        series.setRetrieveAETs(retrieveAETs);
        series.setAvailability(availability);
        return series;
    }

    public static InstanceDTO newInstanceDTO(
        int pk,
        Dataset ds,
        String retrieveAETs,
        int numberOfFiles,
        int availability,
        boolean commitment) {
        String cuid = ds.getString(Tags.SOPClassUID);
        if (UIDs.GrayscaleSoftcopyPresentationStateStorage.equals(cuid)) {
            return newPresentationStateDTO(
                pk,
                ds,
                retrieveAETs,
                numberOfFiles,
                availability,
                commitment);
        };
        if (UIDs.BasicTextSR.equals(cuid)
            || UIDs.EnhancedSR.equals(cuid)
            || UIDs.ComprehensiveSR.equals(cuid)
            || UIDs.KeyObjectSelectionDocument.equals(cuid)) {
            return newStructuredReportDTO(
                pk,
                ds,
                retrieveAETs,
                numberOfFiles,
                availability,
                commitment);
        }
        return newImageDTO(
            pk,
            ds,
            retrieveAETs,
            numberOfFiles,
            availability,
            commitment);
    }

    private static void initInstanceDTO(
        InstanceDTO inst,
        int pk,
        Dataset ds,
        String retrieveAETs,
        int numberOfFiles,
        int availability,
        boolean commitment) {
        inst.setPk(pk);
        inst.setInstanceNumber(ds.getString(Tags.InstanceNumber, ""));
        inst.setSopCUID(ds.getString(Tags.SOPClassUID, ""));
        inst.setSopIUID(ds.getString(Tags.SOPInstanceUID, ""));
        inst.setContentDateTime(
            format(
                ds.getDateTime(Tags.ContentDate, Tags.ContentTime),
                InstanceDTO.DATETIME_FORMAT));
        inst.setRetrieveAETs(retrieveAETs);
        inst.setNumberOfFiles(numberOfFiles);
        inst.setAvailability(availability);
        inst.setCommitment(commitment);
    }

    private static InstanceDTO newImageDTO(
        int pk,
        Dataset ds,
        String retrieveAETs,
        int numberOfFiles,
        int availability,
        boolean commitment) {
        ImageDTO img = new ImageDTO();
        initInstanceDTO(
            img,
            pk,
            ds,
            retrieveAETs,
            numberOfFiles,
            availability,
            commitment);
        img.setImageType(
            StringUtils.toString(ds.getStrings(Tags.ImageType), '\\'));
        img.setPhotometricInterpretation(
            ds.getString(Tags.PhotometricInterpretation, ""));
        img.setRows(ds.getInt(Tags.Rows, 0));
        img.setColumns(ds.getInt(Tags.Columns, 0));
        img.setNumberOfFrames(ds.getInt(Tags.NumberOfFrames, 1));
        img.setBitsAllocated(ds.getInt(Tags.BitsAllocated, 0));
        return img;
    }

    private static InstanceDTO newStructuredReportDTO(
        int pk,
        Dataset ds,
        String retrieveAETs,
        int numberOfFiles,
        int availability,
        boolean commitment) {
        StructuredReportDTO sr = new StructuredReportDTO();
        initInstanceDTO(
            sr,
            pk,
            ds,
            retrieveAETs,
            numberOfFiles,
            availability,
            commitment);
        sr.setDocumentTitle(getConceptNameCodeMeaning(ds));
        sr.setCompletionFlag(ds.getString(Tags.CompletionFlag, ""));
        sr.setVerificationFlag(ds.getString(Tags.VerificationFlag, ""));
        return sr;
    }

    private static String getConceptNameCodeMeaning(Dataset ds) {
        Dataset item = ds.getItem(Tags.ConceptNameCodeSeq);
        return item != null ? item.getString(Tags.CodeMeaning, "") : "";
    }

    private static InstanceDTO newPresentationStateDTO(
        int pk,
        Dataset ds,
        String retrieveAETs,
        int numberOfFiles,
        int availability,
        boolean commitment) {
        PresentationStateDTO ps = new PresentationStateDTO();
        initInstanceDTO(
            ps,
            pk,
            ds,
            retrieveAETs,
            numberOfFiles,
            availability,
            commitment);
        ps.setPresentationCreationDateTime(
            format(
                ds.getDateTime(
                    Tags.PresentationCreationDate,
                    Tags.PresentationCreationTime),
                InstanceDTO.DATETIME_FORMAT));
        ps.setPresentationCreatorName(
            ds.getString(Tags.PresentationCreatorName, ""));
        ps.setPresentationLabel(ds.getString(Tags.PresentationLabel, ""));
        ps.setPresentationDescription(
            ds.getString(Tags.PresentationDescription, ""));
        ps.setNumberOfReferencedImages(countRefImages(ds));
        return ps;
    }

    private static int countRefImages(Dataset ds) {
        int count = 0;
        DcmElement serieSq = ds.get(Tags.RefSeriesSeq);
        if (serieSq != null) {
            for (int i = 0, n = serieSq.vm(); i < n; ++i) {
                DcmElement imageSq = serieSq.getItem(i).get(Tags.RefImageSeq);
                if (imageSq != null) {
                    count += imageSq.vm();
                }
            }
        }
        return count;
    }

    private static String format(Date date, String pattern) {
        return date != null ? new SimpleDateFormat(pattern).format(date) : "";
    }
}
