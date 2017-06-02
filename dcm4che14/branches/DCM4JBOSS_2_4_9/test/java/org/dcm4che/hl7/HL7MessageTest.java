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

package org.dcm4che.hl7;

import java.io.*;
import java.util.*;

import junit.framework.*;
/**
 * <description>
 *
 * @see <related>
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @version $Revision: 3550 $ $Date: 2002-08-19 00:44:43 +0800 (周一, 19 8月 2002) $
 * @since August 11, 2002
 *
 */
public class HL7MessageTest  extends TestCase {

    public HL7MessageTest(java.lang.String testName) {
        super(testName);
    }        
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(HL7MessageTest.class);
        return suite;
    }

    private static final String ORM_O01 = "data/ORM_O01.hl7";
    private static final String ADT_A40 = "data/ADT_A40.hl7";

    private static final HL7Factory fact = HL7Factory.getInstance();
    
    protected void setUp() throws Exception {
    }
    
    private byte[] readDataFrom(String fname) throws Exception {
        File f = new File(fname);
        byte[] data = new byte[(int)f.length()];
        InputStream in = new FileInputStream(f);
        try {
            in.read(data);
        } finally {
            try { in.close(); } catch (IOException e) {}
        }
        return data;
    }
     
    public void testORM_O01() throws Exception {
        HL7Message msg = fact.parse(readDataFrom(ORM_O01));
//        System.out.println(msg.toVerboseString());
        MSHSegment msh = msg.header();
        assertEquals("ORM", msh.getMessageType());
        assertEquals("O01", msh.getTriggerEvent());
        Iterator it = msg.segments().iterator();
        assertTrue(it.hasNext());
        HL7Segment pid = (HL7Segment) it.next();
        assertEquals("PID", pid.id());        
        assertTrue(it.hasNext());
        HL7Segment pv1 = (HL7Segment) it.next();
        assertEquals("PV1", pv1.id());        
        assertTrue(it.hasNext());
        HL7Segment orc = (HL7Segment) it.next();
        assertEquals("ORC", orc.id());        
        assertTrue(it.hasNext());
        HL7Segment obr = (HL7Segment) it.next();
        assertEquals("OBR", obr.id());        
        assertTrue(it.hasNext());
        HL7Segment zds = (HL7Segment) it.next();
        assertEquals("ZDS", zds.id());        
        assertTrue(!it.hasNext());
    }
    
    public void testADT_A40() throws Exception {
        HL7Message msg = fact.parse(readDataFrom(ADT_A40));
//        System.out.println(msg.toVerboseString());
        MSHSegment msh = msg.header();
        assertEquals("ADT", msh.getMessageType());
        assertEquals("A40", msh.getTriggerEvent());
        Iterator it = msg.segments().iterator();
        assertTrue(it.hasNext());
        HL7Segment evn = (HL7Segment) it.next();
        assertEquals("EVN", evn.id());
        assertTrue(it.hasNext());
        HL7Segment pid = (HL7Segment) it.next();
        assertEquals("PID", pid.id());        
        assertTrue(it.hasNext());
        HL7Segment mrg = (HL7Segment) it.next();
        assertEquals("MRG", mrg.id());        
        assertTrue(!it.hasNext());
    }
}
