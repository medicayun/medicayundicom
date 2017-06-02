/*  Copyright (c) 2001,2002,2003 by TIANI MEDGRAPH AG
 *
 *  This file is part of dcm4che.
 *
 *  This library is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU Lesser General Public License as published
 *  by the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.dcm4che.media;

import java.io.*;
import junit.framework.*;
import org.dcm4che.data.*;

import org.dcm4che.dict.*;

/**
 * @author     <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @since      May, 2002
 * @version    $Revision: 3762 $ $Date: 2004-04-05 19:04:00 +0800 (周一, 05 4月 2004) $
 */
public class DirReaderTest extends TestCase
{

    /**
     *  The main program for the DirReaderTest class
     *
     * @param  args  The command line arguments
     */
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(suite());
    }


    private final static DcmObjectFactory dof = DcmObjectFactory.getInstance();
    private final static String DICOMDIR_ID = "data/DICOMDIR";
    private final static String CLASS_UID = "1.2.840.10008.1.3.10";


    /**
     *  A unit test suite for JUnit
     *
     * @return    The test suite
     */
    public static Test suite()
    {
        return new TestSuite(DirReaderTest.class);
    }


    /**
     * Creates new DictionaryTest
     *
     * @param  name  Description of the Parameter
     */
    public DirReaderTest(String name)
    {
        super(name);
    }


    /**
     *  The JUnit setup method
     *
     * @exception  Exception  Description of the Exception
     */
    protected void setUp()
        throws Exception
    {
    }


    /**
     *  A unit test for JUnit
     *
     * @exception  Exception  Description of the Exception
     */
    public void testIterateRoot()
        throws Exception
    {
        DirReader r =
                DirBuilderFactory.getInstance().newDirReader(new File(DICOMDIR_ID));
        try {
            Dataset fsi = r.getFileSetInfo();
            assertEquals(0, fsi.getInt(Tags.FileSetConsistencyFlag, -1));
            FileMetaInfo fmi = fsi.getFileMetaInfo();
            assertEquals(CLASS_UID,
                    fmi.getString(Tags.MediaStorageSOPClassUID));
            assertEquals("NEMA97CD", fsi.getString(Tags.FileSetID));
            DirRecord rec = r.getFirstRecord(false);
            assertNotNull(rec);
            assertEquals("PATIENT", rec.getType());
            assertEquals(DirRecord.IN_USE, rec.getInUseFlag());
            assertEquals("TXSP-H-035",
                    rec.getDataset().getString(Tags.PatientID, null));
            int count = 1;
            while ((rec = rec.getNextSibling(false)) != null) {
                assertEquals("PATIENT", rec.getType());
                assertEquals(DirRecord.IN_USE, rec.getInUseFlag());
                ++count;
            }
            assertEquals(81, count);
        } finally {
            r.close();
        }
    }


    /**
     *  A unit test for JUnit
     *
     * @exception  Exception  Description of the Exception
     */
    public void testIterateChilds()
        throws Exception
    {
        DirReader r = DirBuilderFactory.getInstance().
                newDirReader(new File(DICOMDIR_ID));
        try {
            int count = 0;
            for (DirRecord pat = r.getFirstRecord(false); pat != null;
                    pat = pat.getNextSibling(false)) {
                ++count;
                assertEquals("PATIENT", pat.getType());
                assertEquals(DirRecord.IN_USE, pat.getInUseFlag());
                for (DirRecord study = pat.getFirstChild(false); study != null;
                        study = study.getNextSibling(false)) {
                    ++count;
                    assertEquals("STUDY", study.getType());
                    assertEquals(DirRecord.IN_USE, study.getInUseFlag());
                    for (DirRecord series = study.getFirstChild(false);
                            series != null; series = series.getNextSibling(false)) {
                        ++count;
                        assertEquals("SERIES", series.getType());
                        assertEquals(DirRecord.IN_USE, series.getInUseFlag());
                        for (DirRecord image = series.getFirstChild(false);
                                image != null; image = image.getNextSibling(false)) {
                            ++count;
                            assertEquals("IMAGE", image.getType());
                            assertEquals(DirRecord.IN_USE, image.getInUseFlag());
                        }
                    }
                }
            }
            assertEquals(1203, count);
        } finally {
            r.close();
        }
    }


    /**
     *  A unit test for JUnit
     *
     * @exception  Exception  Description of the Exception
     */
    public void testQueryRecord()
        throws Exception
    {
        DirReader r = DirBuilderFactory.getInstance().
                newDirReader(new File(DICOMDIR_ID));
        try {
            Dataset patKeys = dof.newDataset();
            patKeys.putPN(Tags.PatientName, "*^volunteer*");
            Dataset studyKeys = dof.newDataset();
            studyKeys.putDA(Tags.StudyDate, "19970801-");
            DirRecord patRec = r.getFirstRecordBy("PATIENT", patKeys, false);
            assertNull(patRec);
            patRec = r.getFirstRecordBy("PATIENT", patKeys, true);
            assertNotNull(patRec);
            DirRecord studyRec = patRec.getFirstChildBy("STUDY", studyKeys, false);
            assertNotNull(studyRec);
            patRec = patRec.getNextSiblingBy("PATIENT", patKeys, true);
            assertNotNull(patRec);
            studyRec = patRec.getFirstChildBy("STUDY", studyKeys, false);
            assertNull(studyRec);
            patRec = patRec.getNextSiblingBy("PATIENT", patKeys, true);
            assertNotNull(patRec);
            studyRec = patRec.getFirstChildBy("STUDY", studyKeys, false);
            assertNull(studyRec);
            patRec = patRec.getNextSiblingBy("PATIENT", patKeys, true);
            assertNotNull(patRec);
            studyRec = patRec.getFirstChildBy("STUDY", studyKeys, false);
            assertNull(studyRec);
            patRec = patRec.getNextSiblingBy("PATIENT", patKeys, true);
            assertNull(patRec);
        } finally {
            r.close();
        }
    }

}

