/*$Id: PDataTFTest.java 3514 2002-07-28 07:38:05Z gunterze $*/
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

package org.dcm4che.net;

import java.io.*;
import java.util.*;

import junit.framework.*;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
public class PDataTFTest extends ExtTestCase {

    public PDataTFTest(java.lang.String testName) {
        super(testName);
    }        
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(PDataTFTest.class);
        return suite;
    }
    
    private static final String P_DATA_TF1 = "data/PDataTF1.pdu";
    private static final String P_DATA_TF2 = "data/PDataTF2.pdu";
    private final byte[] CMD = new byte[100];
    private final byte[] DATA = new byte[200];
    private final int PCID = 127;
    private final int MAX_LEN = 250;


    private AssociationFactory fact;
    
    protected void setUp() throws Exception {
        Arrays.fill(CMD, (byte)0xcc);
        Arrays.fill(DATA, (byte)0xdd);
        fact = AssociationFactory.getInstance();
    }
        
    public void testWrite() throws Exception {
        PDataTF pdu1 = fact.newPDataTF(MAX_LEN);
        assertEquals(MAX_LEN-6, pdu1.free());
        pdu1.openPDV(PCID, true);
        assertEquals(MAX_LEN-6, pdu1.free());
        assertEquals(CMD.length, pdu1.write(CMD, 0, CMD.length));
        assertEquals(MAX_LEN-6-CMD.length, pdu1.free());
        pdu1.closePDV(true);
        assertEquals(MAX_LEN-6-CMD.length-6, pdu1.free());
        pdu1.openPDV(PCID, false);
        int off = pdu1.write(DATA, 0, DATA.length);
        assertEquals(MAX_LEN-6-CMD.length-6, off);
        assertEquals(0, pdu1.free());
        assertTrue(!pdu1.write(0));
        pdu1.closePDV(false);
        assertEquals(-6, pdu1.free());
        ByteArrayOutputStream out1 = new ByteArrayOutputStream(MAX_LEN + 4);
//        OutputStream out1 = new FileOutputStream(P_DATA_TF1);        
        pdu1.writeTo(out1);
        out1.close();
        assertEquals(load(P_DATA_TF1), out1.toByteArray());
        PDataTF pdu2 = fact.newPDataTF(MAX_LEN);
        pdu2.openPDV(PCID, false);
        assertEquals(MAX_LEN-6, pdu2.free());
        assertTrue(pdu2.write(DATA[off++]));
        assertEquals(MAX_LEN-7, pdu2.free());
        assertEquals(DATA.length-off, pdu2.write(DATA, off, DATA.length-off));
        assertEquals(MAX_LEN-7-DATA.length+off, pdu2.free());
        pdu2.closePDV(true);
        assertEquals(MAX_LEN-7-DATA.length+off-6, pdu2.free());
        ByteArrayOutputStream out2 = new ByteArrayOutputStream(MAX_LEN + 4);
//        OutputStream out2 = new FileOutputStream(P_DATA_TF2);        
        pdu2.writeTo(out2);
        assertEquals(load(P_DATA_TF2), out2.toByteArray());
    }

    public void testRead() throws Exception {
        InputStream in1 = new FileInputStream(P_DATA_TF1);
        PDataTF pdu1 = null;
        try {
            pdu1 = (PDataTF)fact.readFrom(in1, null);            
        } finally {
            try { in1.close(); } catch (IOException ignore) {}
        }
        PDataTF.PDV pdv11 = pdu1.readPDV();
        assertNotNull(pdv11);
        assertEquals(CMD.length+2, pdv11.length());
        assertEquals(PCID, pdv11.pcid());
        assertTrue(pdv11.cmd());
        assertTrue(pdv11.last());
        byte[] cmd = new byte[CMD.length];
        InputStream pdv11in = pdv11.getInputStream();
        pdv11in.read(cmd);
        assertEquals(-1, pdv11in.read());
        assertEquals(CMD, cmd);
        PDataTF.PDV pdv12 = pdu1.readPDV();
        assertNotNull(pdv12);
        int off = pdv12.length()-2;
        assertEquals(MAX_LEN-6-CMD.length-6, off);
        assertEquals(PCID, pdv12.pcid());
        assertTrue(!pdv12.cmd());
        assertTrue(!pdv12.last());
        byte[] data = new byte[DATA.length];
        InputStream pdv12in = pdv12.getInputStream();
        pdv12in.read(data, 0, off);
        assertEquals(-1, pdv12in.read());
        assertNull(pdu1.readPDV());
        InputStream in2 = new FileInputStream(P_DATA_TF2);
        PDataTF pdu2 = null;
        try {
            pdu2 = (PDataTF)fact.readFrom(in2, null);            
        } finally {
            try { in2.close(); } catch (IOException ignore) {}
        }
        PDataTF.PDV pdv21 = pdu2.readPDV();
        assertNotNull(pdv21);
        assertEquals(DATA.length-off+2, pdv21.length());
        assertEquals(PCID, pdv21.pcid());
        assertTrue(!pdv21.cmd());
        assertTrue(pdv21.last());
        InputStream pdv21in = pdv21.getInputStream();
        pdv21in.read(data, off, DATA.length-off);
        assertEquals(-1, pdv21in.read());
        assertNull(pdu2.readPDV());
        assertEquals(DATA, data);
    }    
}

