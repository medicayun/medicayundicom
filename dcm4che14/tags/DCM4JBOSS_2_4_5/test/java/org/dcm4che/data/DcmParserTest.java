/*$Id: DcmParserTest.java 3505 2002-07-19 14:59:46Z gunterze $*/
/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2002 by TIANI MEDGRAPH AG <gunter.zeilinger@tiani.com>     *
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

package org.dcm4che.data;

import org.dcm4che.data.DcmEncodeParam;
import org.dcm4che.data.DcmParserFactory;
import org.dcm4che.data.DcmParser;
import org.dcm4che.data.DcmHandler;

import java.io.*;

import junit.framework.*;

/**
 *
 * @author gunter.zeilinger@tiani.com
 */                                
public class DcmParserTest extends TestCase {
    
    public DcmParserTest(java.lang.String testName) {
        super(testName);
    }        
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(DcmParserTest.class);
        return suite;
    }

    private static final String EVR_LE = "data/examplef9.dcm";
    private static final String DICOMDIR = "data/DICOMDIR";
    private static final String PART10_EVR_LE = "data/6AF8_10";
    private static final String JPEG_70 = "data/CT-MONO2-16-chest";

    private static final DcmParserFactory pfact = DcmParserFactory.getInstance();
    private DcmParser parser;
    
    protected void setUp() throws Exception {
    }
    
    public void testEVR_LE() throws Exception {
        DataInputStream in = new DataInputStream(
                new BufferedInputStream(new FileInputStream(EVR_LE)));
        try {
            parser = pfact.newDcmParser(in);
            parser.parseDcmFile(null, -1);
        } finally {
            try { in.close(); } catch (Exception ignore) {}
        }
    }
    
    public void testDICOMDIR() throws Exception {
        DataInputStream in = new DataInputStream(
                new BufferedInputStream(new FileInputStream(DICOMDIR)));
        try {
            parser = pfact.newDcmParser(in);
            parser.parseDcmFile(null, -1);
        } finally {
            try { in.close(); } catch (Exception ignore) {}
        }
    }
    
    public void testPART10_EVR_LE() throws Exception {
        DataInputStream in = new DataInputStream(
                new BufferedInputStream(new FileInputStream(PART10_EVR_LE)));
        try {
            parser = pfact.newDcmParser(in);
            parser.parseDcmFile(null, -1);
        } finally {
            try { in.close(); } catch (Exception ignore) {}
        }
    }
    
    public void testJPEG_70() throws Exception {
        DataInputStream in = new DataInputStream(
                new BufferedInputStream(new FileInputStream(JPEG_70)));
        try {
            parser = pfact.newDcmParser(in);
            parser.parseDcmFile(null, -1);
        } finally {
            try { in.close(); } catch (Exception ignore) {}
        }
    }
}
