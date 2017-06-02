/*$Id: DirBuilderImpl.java 3846 2004-12-23 16:33:09Z arminkanitsar $*/
/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2002 by TIANI MEDGRAPH AG                                  *
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

package org.dcm4cheri.media;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.FileFormat;
import org.dcm4che.data.FileMetaInfo;
import org.dcm4che.dict.Tags;
import org.dcm4che.media.DirBuilder;
import org.dcm4che.media.DirBuilderFactory;
import org.dcm4che.media.DirBuilderPref;
import org.dcm4che.media.DirRecord;
import org.dcm4che.media.DirWriter;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
final class DirBuilderImpl implements DirBuilder {
   
   private final DirWriter writer;
   private final DirBuilderPref pref;
   
   private String curPatID;
   private DirRecord curPatRec;
   private String curStudyUID;
   private DirRecord curStudyRec;
   private String curSeriesUID;
   private DirRecord curSeriesRec;
   
   /** Creates a new instance of DirBuilderImpl */
   public DirBuilderImpl(DirWriter writer, DirBuilderPref pref) {
      this.writer = writer;
      this.pref = pref;
   }
   
   
   public DirWriter getDirWriter() {
       return writer;
   }
   
   public int addFileRef(File file) throws IOException {
      InputStream in = new BufferedInputStream(new FileInputStream(file));
      Dataset ds = DirReaderImpl.factory.newDataset();
      try {
         ds.readFile(in, FileFormat.DICOM_FILE, Tags.PixelData);
      } finally {
         in.close();
      }
      return addFileRef(writer.toFileIDs(file), ds);
   }
   
   public int addFileRef(String[] fileIDs, Dataset ds) throws IOException {
      FileMetaInfo fmi = ds.getFileMetaInfo();
      if (fmi == null) {
         throw new IllegalArgumentException("Missing File Meta Information");
      }
      String tsUID = fmi.getTransferSyntaxUID();
      if (tsUID == null) {
         throw new IllegalArgumentException("Missing Transfer Syntax UID");
      }
      String classUID = fmi.getMediaStorageSOPClassUID();
      if (classUID == null) {
         throw new IllegalArgumentException("Missing SOP Class UID");
      }
      if (!classUID.equals(ds.getString(Tags.SOPClassUID))) {
         throw new IllegalArgumentException("Mismatch SOP Class UID");
      }
      String type = DirBuilderFactory.getRecordType(classUID);
      Dataset filter = pref.getFilterForRecordType(type);
      if (filter == null) {
         return 0;
      }
      String instUID = fmi.getMediaStorageSOPInstanceUID();
      if (instUID == null) {
         throw new IllegalArgumentException("Missing SOP Instance UID");
      }
      if (!instUID.equals(ds.getString(Tags.SOPInstanceUID))) {
         throw new IllegalArgumentException("Mismatch SOP Instance UID");
      }
      String seriesUID = ds.getString(Tags.SeriesInstanceUID);
      if (seriesUID == null) {
         throw new IllegalArgumentException("Missing Series Instance UID");
      }
      String studyUID = ds.getString(Tags.StudyInstanceUID);
      if (studyUID == null) {
         throw new IllegalArgumentException("Missing Study Instance UID");
      }
      String patID = ds.getString(Tags.PatientID, "");
      int count = 0;
      if (!patID.equals(curPatID)) {
         count += addPatRec(ds, patID);
      }
      if (!studyUID.equals(curStudyUID)) {
         count += addStudyRec(ds, studyUID);
      }
      if (!seriesUID.equals(curSeriesUID)) {
         count += addSeriesRec(ds, seriesUID);
      }
      writer.add(curSeriesRec, type,
         ds.subSet(filter), fileIDs, classUID, instUID, tsUID);
      ++count;
      return count;
   }
   
   private int addPatRec(Dataset ds, String patID) throws IOException {
      writer.commit();
      this.curSeriesUID = null;
      this.curSeriesRec = null;
      this.curStudyUID = null;
      this.curStudyRec = null;
      this.curPatID = patID;
      for (DirRecord dr = writer.getFirstRecord(true); dr != null;
      dr = dr.getNextSibling(true)) {
         if (DirRecord.PATIENT.equals(dr.getType()) && patID.equals(
         dr.getDataset().getString(Tags.PatientID))) {
            curPatRec = dr;
            return 0;
         }
      }
      curPatRec = writer.add(null, DirRecord.PATIENT,
         ds.subSet(pref.getFilterForRecordType(DirRecord.PATIENT)));
      return 1;
   }
   
   private int addStudyRec(Dataset ds, String studyUID) throws IOException {
      writer.commit();
      this.curSeriesUID = null;
      this.curSeriesRec = null;
      this.curStudyUID = studyUID;
      curPatRec.reload();
      for (DirRecord dr = curPatRec.getFirstChild(true); dr != null;
      dr = dr.getNextSibling(true)) {
         if (DirRecord.STUDY.equals(dr.getType()) && studyUID.equals(
         dr.getDataset().getString(Tags.StudyInstanceUID))) {
            curStudyRec = dr;
            return 0;
         }
      }
      curStudyRec = writer.add(curPatRec, DirRecord.STUDY,
         ds.subSet(pref.getFilterForRecordType(DirRecord.STUDY)));
      return 1;
   }
   
   private int addSeriesRec(Dataset ds, String seriesUID) throws IOException {
      writer.commit();
      this.curSeriesUID = seriesUID;
      curStudyRec.reload();
      for (DirRecord dr = curStudyRec.getFirstChild(true); dr != null;
      dr = dr.getNextSibling(true)) {
         if (DirRecord.SERIES.equals(dr.getType()) && seriesUID.equals(
         dr.getDataset().getString(Tags.SeriesInstanceUID))) {
            curSeriesRec = dr;
            return 0;
         }
      }
      curSeriesRec = writer.add(curStudyRec, DirRecord.SERIES,
         ds.subSet(pref.getFilterForRecordType(DirRecord.SERIES)));
      return 1;
   }
   
   public void close() throws IOException {
      writer.close();
   }
   
}
