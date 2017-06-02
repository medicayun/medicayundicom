/* $Id: DirBuilderTest.java 3505 2002-07-19 14:59:46Z gunterze $ */
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

package org.dcm4che.media;

import org.dcm4che.dict.*;
import org.dcm4che.data.*;

import junit.framework.*;

import java.io.*;
import java.util.*;

/**
 *
 * @author  gunter zeilinger
 * @version 1.0.0
 */
public class DirBuilderTest extends TestCase {
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    private static final String INST_UID = "1.2.40.0.13.1.1.99";
    private static final String FILE_SET_ID = "FILE_SET_ID";
    private static final File OUT_FILE = new File("data/TMP_TEST");
    private static final String[] FILE_IDs = {
        "data/6AF8_10",
        "data/MRABDO",
        "data/6AF8_30",
        "data/MRABDOR",
    };
    
    public static Test suite() {
        return new TestSuite(DirBuilderTest.class);
    }
    
    public DirBuilderTest(String name) {
        super(name);
    }
    
    private DcmObjectFactory objFact = DcmObjectFactory.getInstance();
    private DirBuilderFactory fact;
    private DirBuilderPref pref;
    
    protected void setUp() throws Exception {
        fact = DirBuilderFactory.getInstance();
        pref = fact.newDirBuilderPref();
        pref.setFilterForRecordType("PATIENT", getPatientFilter());
        pref.setFilterForRecordType("STUDY", getStudyFilter());
        pref.setFilterForRecordType("SERIES", getSeriesFilter());
        pref.setFilterForRecordType("IMAGE", getImageFilter());
    }
    
    protected void tearDown() throws Exception {
        if (OUT_FILE.exists()) {
            OUT_FILE.delete();
        }
    }
    
    private Dataset getPatientFilter() {
        Dataset retval = objFact.newDataset();
        retval.putCS(Tags.SpecificCharacterSet);
        retval.putPN(Tags.PatientName);
        retval.putLO(Tags.PatientID);
        retval.putDA(Tags.PatientBirthDate);
        retval.putCS(Tags.PatientSex);
        return retval;
    }
    
    private Dataset getStudyFilter() {
        Dataset retval = objFact.newDataset();
        retval.putCS(Tags.SpecificCharacterSet);
        retval.putDA(Tags.StudyDate);
        retval.putTM(Tags.StudyTime);
        retval.putSH(Tags.AccessionNumber);
        retval.putPN(Tags.ReferringPhysicianName);
        retval.putLO(Tags.StudyDescription);
        retval.putSQ(Tags.ProcedureCodeSeq);
        retval.putUI(Tags.StudyInstanceUID);
        retval.putSH(Tags.StudyID);
        return retval;
    }
    
    private Dataset getSeriesFilter() {
        Dataset retval = objFact.newDataset();
        retval.putCS(Tags.SpecificCharacterSet);
        retval.putDA(Tags.SeriesDate);
        retval.putTM(Tags.SeriesTime);
        retval.putCS(Tags.Modality);
        retval.putLO(Tags.Manufacturer);
        retval.putLO(Tags.SeriesDescription);
        retval.putCS(Tags.BodyPartExamined);
        retval.putUI(Tags.SeriesInstanceUID);
        retval.putIS(Tags.SeriesNumber);
        retval.putCS(Tags.Laterality);
        return retval;
    }
    
    private Dataset getImageFilter() {
        Dataset retval = objFact.newDataset();
        retval.putCS(Tags.SpecificCharacterSet);
        retval.putDA(Tags.ContentDate);
        retval.putTM(Tags.ContentTime);
        retval.putSQ(Tags.RefImageSeq);
        retval.putLO(Tags.ContrastBolusAgent);
        retval.putIS(Tags.InstanceNumber);
        retval.putIS(Tags.NumberOfFrames);
        return retval;
    }
    
    public void testAddFileRef() throws Exception {
        DirWriter w1 = fact.newDirWriter(OUT_FILE, INST_UID, FILE_SET_ID,
        null, null, null);
        DirBuilder b1 = fact.newDirBuilder(w1, pref);
        try {
            int c = 0;
            c += b1.addFileRef(new File(FILE_IDs[0]));
            c += b1.addFileRef(new File(FILE_IDs[1]));
            c += b1.addFileRef(new File(FILE_IDs[2]));
            c += b1.addFileRef(new File(FILE_IDs[3]));
            assertEquals(15, c);
        } finally {
            b1.close();
        }
        
    }
 }
