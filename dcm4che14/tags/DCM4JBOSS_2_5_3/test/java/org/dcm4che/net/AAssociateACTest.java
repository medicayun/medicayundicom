/*$Id: AAssociateACTest.java 3493 2002-07-14 16:03:36Z gunterze $*/
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

import org.dcm4che.dict.UIDs;

import java.io.*;
import java.util.*;

import junit.framework.*;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
public class AAssociateACTest extends AAssociateRQACTest {

    public AAssociateACTest(java.lang.String testName) {
        super(testName);
    }        
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(AAssociateACTest.class);
        return suite;
    }

    protected static final String A_ASSOCIATE_AC = "data/AAssociateAC.pdu";    
    
    protected void check(AAssociateAC ac) {
        super.check(ac);
        Collection c = ac.listPresContext();
        assertEquals(3, c.size());
        Iterator pcit = c.iterator();
        PresContext pc1 = (PresContext)pcit.next();
        assertEquals(PresContext.ACCEPTANCE, pc1.result());
        assertEquals(TS_UID1, pc1.getTransferSyntaxUID());
        assertEquals(pc1, ac.getPresContext(1));
        PresContext pc2 = (PresContext)pcit.next();
        assertEquals(PresContext.ACCEPTANCE, pc2.result());
        assertEquals(TS_UID2, pc2.getTransferSyntaxUID());
        assertEquals(pc2, ac.getPresContext(3));
        PresContext pc3 = (PresContext)pcit.next();
        assertEquals(PresContext.TRANSFER_SYNTAXES_NOT_SUPPORTED, pc3.result());
        assertEquals(TS_UID3, pc3.getTransferSyntaxUID());
        assertEquals(pc3, ac.getPresContext(5));
        assertTrue(!pcit.hasNext());
    }
        
    protected void set(AAssociateAC ac) {
        super.set(ac);
        ac.addPresContext(fact.newPresContext(1,
                PresContext.ACCEPTANCE, TS_UID1));
        ac.addPresContext(fact.newPresContext(3,
                PresContext.ACCEPTANCE, TS_UID2));
        ac.addPresContext(fact.newPresContext(5,
                PresContext.TRANSFER_SYNTAXES_NOT_SUPPORTED, TS_UID3));
    }
    
    public void testWrite() throws Exception {
        AAssociateAC ac = fact.newAAssociateAC();
        set(ac);
        check(ac);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
//        OutputStream out = new FileOutputStream(A_ASSOCIATE_AC);        
        ac.writeTo(out);
        out.close();
        assertEquals(load(A_ASSOCIATE_AC), out.toByteArray());
    }

    public void testRead() throws Exception {
        InputStream in = new FileInputStream(A_ASSOCIATE_AC);
        AAssociateAC pdu = null;
        try {
            pdu = (AAssociateAC)fact.readFrom(in, null);            
        } finally {
            try { in.close(); } catch (IOException ignore) {}
        }
        check(pdu);        
     }
}
