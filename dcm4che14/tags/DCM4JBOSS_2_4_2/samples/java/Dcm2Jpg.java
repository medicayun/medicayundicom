/*$Id: Dcm2Jpg.java 3660 2003-06-22 22:33:03Z gunterze $*/
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

import org.dcm4che.imageio.plugins.DcmImageReadParam;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 */
public class Dcm2Jpg {
   
   public void convert(File src, File dest, byte[] lut) throws IOException {
      Iterator iter = ImageIO.getImageReadersByFormatName("DICOM");
      ImageReader reader = (ImageReader)iter.next();
      DcmImageReadParam param = (DcmImageReadParam) reader.getDefaultReadParam();
      param.setPValToDDL(lut);
      ImageInputStream iis = ImageIO.createImageInputStream(src);
      BufferedImage bi;
      try {
         reader.setInput(iis, false);
         bi = reader.read(0, param);
         if (bi == null) {
            System.out.println("\nError: " + src + " - couldn't read!");
            return;
         }
      } finally {
         try { iis.close(); } catch (IOException ignore) {}
      }
      OutputStream out = new BufferedOutputStream(new FileOutputStream(dest));
      try {
         JPEGImageEncoder enc = JPEGCodec.createJPEGEncoder(out);
         enc.encode(bi);
      }
      finally {
         out.close();
      }
      System.out.print('.');
   }
   
   public int mconvert(String[] args, int optind, File destDir, byte[] lut)
   throws IOException {
      int count = 0;
      for (int i = optind, n = args.length-1; i < n; ++i) {
         File src = new File(args[i]);
         count += mconvert(src, new File(destDir, src.getName()), lut);
      }
      return count;
   }
   
   public int mconvert(File src, File dest, byte[] lut) throws IOException {
      if (src.isFile()) {
         convert(src, dest, lut);
         return 1;
      }
      File[] files = src.listFiles();
      if (files.length > 0 && !dest.exists()) {
         dest.mkdirs();
      }
      int count = 0;
      for (int i = 0; i < files.length; ++i) {
         count += mconvert(files[i], new File(dest, files[i].getName()), lut);
      }
      return count;
   }
   
   /**
    * @param args the command line arguments
    */
   public static void main(String args[]) throws Exception {
      Dcm2Jpg dcm2jpg = new Dcm2Jpg();
      
      int argc = args.length;
      if (argc < 2 || args[0].equals("-lut") && argc < 4) {
         exit("dcm2jpg.jar: missing argument\n");
      }
      
      byte[] lut = null;
      int optind = 0;
      if (args[0].equals("-lut")) {
         lut = readLUT(args[1]);
         optind = 2;
      }
      
      File dest = new File(args[args.length-1]);
      long t1 = System.currentTimeMillis();
      int count = 1;
      if (dest.isDirectory()) {
         count = dcm2jpg.mconvert(args, optind, dest, lut);
      } else {
         File src = new File(args[optind]);
         if (argc > optind+2|| src.isDirectory()) {
            exit("dcm2jpg.jar: when converting several files, "
            + "last argument must be a directory\n");
         }
         dcm2jpg.convert(src, dest, lut);
      }
      long t2 = System.currentTimeMillis();
      System.out.println("\nconverted " + count + " files in "
      + (t2-t1)/1000f + " s.");
   }
   
   private static byte[] readLUT(String lutFile) throws IOException {
      ArrayList a = new ArrayList();
      BufferedReader br = new BufferedReader(new FileReader(lutFile));
      try {
         String s;
         while ((s = br.readLine()) != null) {
            if (s.trim().length() > 0) {
               a.add(new Short(s));
            }
         }
      } finally {
         try { br.close(); } catch (IOException ignore) {}
      }
      byte[] b = new byte[a.size()];
      for (int i = 0; i < b.length; ++i) {
         b[i] = ((Short) a.get(i)).byteValue();
      }
      return b;
   }
   
   private static void exit(String prompt) {
      System.err.println(prompt);
      System.err.println(USAGE);
      System.exit(1);
   }
   
   private static final String USAGE =
   "Usage: java -jar dcm2jpg.jar [-lut LUT] SOURCE DEST\n" +
   "    or java -jar dcm2jpg.jar [-lut LUT] SOURCE... DIRECTORY\n\n" +
   "Convert DICOM image(s) to JPEG(s).\n\n";
}
