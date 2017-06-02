/*$Id: AAbortTest.java 3514 2002-07-28 07:38:05Z gunterze $*/
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
public class AAbortTest extends ExtTestCase {

    public AAbortTest(java.lang.String testName) {
        super(testName);
    }        
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(AAbortTest.class);
        return suite;
    }

    private static final String A_ABORT = "data/AAbort.pdu";
    private final int SOURCE = AAbort.SERVICE_PROVIDER;
    private final int REASON = AAbort.INVALID_PDU_PARAMETER_VALUE;


    private AssociationFactory fact;
    
    protected void setUp() throws Exception {
        fact = AssociationFactory.getInstance();
    }

    public void testWrite() throws Exception {
        AAbort pdu = fact.newAAbort(SOURCE, REASON);
        ByteArrayOutputStream out = new ByteArrayOutputStream(10);
//        OutputStream out = new FileOutputStream(A_ABORT);        
        pdu.writeTo(out);
        out.close();
        assertEquals(load(A_ABORT), out.toByteArray());
    }

    public void testRead() throws Exception {
        InputStream in = new FileInputStream(A_ABORT);
        AAbort pdu = null;
        try {
            pdu = (AAbort)fact.readFrom(in, null);            
        } finally {
            try { in.close(); } catch (IOException ignore) {}
        }
        assertEquals(SOURCE, pdu.source());
        assertEquals(REASON, pdu.reason());
    }
}

