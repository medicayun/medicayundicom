/*$Id: DatasetSerializerTest.java 3505 2002-07-19 14:59:46Z gunterze $*/
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
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.Dataset;

import java.io.*;
import junit.framework.*;

/**
 *
 * @author gunter.zeilinger@tiani.com
 */
public class DatasetSerializerTest extends TestCase {
   
   public DatasetSerializerTest(java.lang.String testName) {
      super(testName);
   }
   
   public static void main(java.lang.String[] args) {
      junit.textui.TestRunner.run(suite());
   }
   
   public static Test suite() {
      TestSuite suite = new TestSuite(DatasetSerializerTest.class);
      return suite;
   }
   
   private static final File OUT_FILE = new File("data/TMP_TEST");
   private static final String EVR_LE = "data/examplef9.dcm";
   private static final String PART10_EVR_LE = "data/6AF8_10";
   private static final String JPEG_70 = "data/CT-MONO2-16-chest";
   private Dataset ds;
   
   protected void setUp() throws Exception {
      ds = DcmObjectFactory.getInstance().newDataset();
   }

   protected void tearDown() throws Exception {
      if (OUT_FILE.exists()) {
         OUT_FILE.delete();
      }
   }
   
   public void testEVR_LE() throws Exception {
      DataInputStream in = new DataInputStream(
      new BufferedInputStream(new FileInputStream(EVR_LE)));
      try {
         ds.readFile(in, null, -1);
      } finally {
         try { in.close(); } catch (Exception ignore) {}
      }
      ObjectOutputStream oo = new ObjectOutputStream(
      new BufferedOutputStream(new FileOutputStream(OUT_FILE)));
      try {
         oo.writeObject(ds);
      } finally {
         try { oo.close(); } catch (Exception ignore) {}
      }
      ObjectInputStream oin = new ObjectInputStream(
      new BufferedInputStream(new FileInputStream(OUT_FILE)));
      try {
         Dataset ds2 = (Dataset)oin.readObject();
         assertEquals(ds.size(), ds2.size());
      } finally {
         try { oin.close(); } catch (Exception ignore) {}
      }
   }
   
   public void testPART10_EVR_LE() throws Exception {
      DataInputStream in = new DataInputStream(
      new BufferedInputStream(new FileInputStream(PART10_EVR_LE)));
      try {
         ds.readFile(in, null, -1);
      } finally {
         try { in.close(); } catch (Exception ignore) {}
      }
      ObjectOutputStream oo = new ObjectOutputStream(
      new BufferedOutputStream(new FileOutputStream(OUT_FILE)));
      try {
         oo.writeObject(ds);
      } finally {
         try { oo.close(); } catch (Exception ignore) {}
      }
      ObjectInputStream oin = new ObjectInputStream(
      new BufferedInputStream(new FileInputStream(OUT_FILE)));
      try {
         Dataset ds2 = (Dataset)oin.readObject();
         assertEquals(ds.size(), ds2.size());
      } finally {
         try { oin.close(); } catch (Exception ignore) {}
      }
   }
   
   public void testJPEG_70() throws Exception {
      DataInputStream in = new DataInputStream(
      new BufferedInputStream(new FileInputStream(JPEG_70)));
      try {
         ds.readFile(in, null, -1);
      } finally {
         try { in.close(); } catch (Exception ignore) {}
      }
      ObjectOutputStream oo = new ObjectOutputStream(
      new BufferedOutputStream(new FileOutputStream(OUT_FILE)));
      try {
         oo.writeObject(ds);
      } finally {
         try { oo.close(); } catch (Exception ignore) {}
      }
      ObjectInputStream oin = new ObjectInputStream(
      new BufferedInputStream(new FileInputStream(OUT_FILE)));
      try {
         Dataset ds2 = (Dataset)oin.readObject();
         assertEquals(ds.size(), ds2.size());
      } finally {
         try { oin.close(); } catch (Exception ignore) {}
      }
   }
}
