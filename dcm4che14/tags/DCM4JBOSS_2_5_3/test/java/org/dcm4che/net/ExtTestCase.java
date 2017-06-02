/*$Id: ExtTestCase.java 3493 2002-07-14 16:03:36Z gunterze $*/
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

import junit.framework.*;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
class ExtTestCase extends TestCase {

    public ExtTestCase(java.lang.String testName) {
        super(testName);
    }        

    public static byte[] load(String id) throws IOException {
        File f = new File(id);
        InputStream in = new FileInputStream(f);        
        try {
            byte[] retval = new byte[(int)f.length()];
            in.read(retval);
            return retval;
        } finally {
            try { in.close(); } catch (IOException ignore) {};
        }
    }
    
    public static void assertEquals(byte[] expected, byte[] value) {
        TestCase.assertNotNull(value);
        TestCase.assertEquals(expected.length, value.length);
        for (int i = 0; i < expected.length; ++i) {
            TestCase.assertEquals("byte[" + i + "]", expected[i], value[i]);
        }
    }
}

