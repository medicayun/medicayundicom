/*$Id: AAssociateRQTest.java 3493 2002-07-14 16:03:36Z gunterze $*/
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
public class AAssociateRQTest extends AAssociateRQACTest {

    public AAssociateRQTest(java.lang.String testName) {
        super(testName);
    }        
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(AAssociateRQTest.class);
        return suite;
    }

    protected static final String A_ASSOCIATE_RQ = "data/AAssociateRQ.pdu";
    
    
    protected void check(AAssociateRQ rq) {
        super.check(rq);
        Collection c = rq.listPresContext();
        assertEquals(3, c.size());
        Iterator pcit = c.iterator();
        PresContext pc1 = (PresContext)pcit.next();
        assertEquals(AS_UID1, pc1.getAbstractSyntaxUID());
        List ts1 = pc1.getTransferSyntaxUIDs();
        assertEquals(1, ts1.size());
        assertEquals(TS_UID1, ts1.get(0));
        assertEquals(pc1, rq.getPresContext(1));
        PresContext pc2 = (PresContext)pcit.next();
        assertEquals(AS_UID2, pc2.getAbstractSyntaxUID());
        List ts2 = pc2.getTransferSyntaxUIDs();
        assertEquals(2, ts2.size());
        assertEquals(TS_UID1, ts2.get(0));
        assertEquals(TS_UID2, ts2.get(1));
        assertEquals(pc2, rq.getPresContext(3));
        PresContext pc3 = (PresContext)pcit.next();
        assertEquals(AS_UID2, pc3.getAbstractSyntaxUID());
        List ts3 = pc3.getTransferSyntaxUIDs();
        assertEquals(1, ts3.size());
        assertEquals(TS_UID3, ts3.get(0));
        assertEquals(pc3, rq.getPresContext(5));
        assertTrue(!pcit.hasNext());
    }
        
    protected void set(AAssociateRQ rq) {
        super.set(rq);
        rq.addPresContext(fact.newPresContext(rq.nextPCID(), AS_UID1,
                new String[] { TS_UID1 }));
        rq.addPresContext(fact.newPresContext(rq.nextPCID(), AS_UID2,
                new String[] { TS_UID1, TS_UID2 }));
        rq.addPresContext(fact.newPresContext(rq.nextPCID(), AS_UID2,
                new String[] { TS_UID3 }));
    }
    
    public void testWrite() throws Exception {
        AAssociateRQ rq = fact.newAAssociateRQ();
        set(rq);
        check(rq);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
//        OutputStream out = new FileOutputStream(A_ASSOCIATE_RQ);        
        rq.writeTo(out);
        out.close();
        assertEquals(load(A_ASSOCIATE_RQ), out.toByteArray());
    }

    public void testRead() throws Exception {
        InputStream in = new FileInputStream(A_ASSOCIATE_RQ);
        AAssociateRQ pdu = null;
        try {
            pdu = (AAssociateRQ)fact.readFrom(in, null);            
        } finally {
            try { in.close(); } catch (IOException ignore) {}
        }
        check(pdu);        
     }
}
