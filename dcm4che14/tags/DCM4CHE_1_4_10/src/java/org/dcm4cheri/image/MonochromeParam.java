/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * TIANI Medgraph AG.
 * Portions created by the Initial Developer are Copyright (C) 2002-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunter.zeilinger@tiani.com>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package org.dcm4cheri.image;

import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import org.dcm4che.image.ColorModelParam;

import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.util.Arrays;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
final class MonochromeParam extends BasicColorModelParam  {
   
   
   private final int inverse;
   private final float slope, intercept;
   private final float[] center, width;
   private final int hashcode;
   private final byte[] pv2dll;
   private final int andmask;
   private final int pvBits;
   private final int rshift;
   private final int lshift;
   
   private final static float[] EMPTY = {};
   private final static float[] nullToEmpty(float[] a) {
      return a == null ? EMPTY : a;
   }
   private final static float correctZeroSlope(float f) {
      return f == 0.f ? 1.f : f;
   }
   private final static int inBits(int len) {
      for (int i = 8, n = 256; i <= 16; ++i, n <<= 1) {
         if (n == len) {
            return i;
         }
      }
      throw new IllegalArgumentException("pv2dll length: " + len);
   }
   
   /** Creates a new instance of MonochromeParam */
   public MonochromeParam(Dataset ds, boolean inverse1, byte[] pv2dll) {
      super(ds);
      this.inverse = inverse1 ? -1 : 0;
      this.slope = correctZeroSlope(ds.getFloat(Tags.RescaleSlope, 1.f));
      this.intercept = ds.getFloat(Tags.RescaleIntercept, 0.f);
      this.center = nullToEmpty(ds.getFloats(Tags.WindowCenter));
      this.width = nullToEmpty(ds.getFloats(Tags.WindowWidth));
      for (int i = 0; i < width.length; ++i) {
         if (width[i] <= 0.f) {
            width[i] = (max - min) / slope;
         }
      }
      this.pv2dll = pv2dll;
      this.pvBits = inBits(pv2dll.length);
      this.andmask = (1<<pvBits)-1;
      if (bits > pvBits) {
         this.rshift = bits - pvBits;           
         this.lshift = 0;
      } else {
         this.lshift = pvBits - bits;           
         this.rshift = 0;
      }
      this.hashcode = hashcode(dataType, inverse, min, max,
         slope, intercept, center, width, pv2dll);      
   }
   
   private static int hashcode(int dataType, int inverse, int min, int max,
      float slope, float intercept, float[] center, float[] width,
      byte[] pv2dll)
   {
      StringBuffer sb = new StringBuffer();
      sb.append(dataType).append(inverse).append(min).append(max)
      .append(slope).append(intercept).append(pv2dll);
      if (Math.min(center.length, width.length) != 0) {
         sb.append(center[0]).append(width[0]);
      }
      return sb.toString().hashCode();
   }
   
   private MonochromeParam(MonochromeParam other, float center1, float width1,
      boolean inverse1)
   {
      super(other);
      this.inverse = inverse1 ? -1 : 0;
      this.slope = other.slope;
      this.intercept = other.intercept;
      this.center = new float[] { center1 };
      this.width = new float[] { width1 };
      this.pv2dll = other.pv2dll;
      this.pvBits = other.pvBits;
      this.andmask = other.andmask;
      this.lshift = other.lshift;
      this.rshift = other.rshift;
      this.hashcode = hashcode(dataType, inverse, min, max,
      slope, intercept, center, width, pv2dll);
   }
   
   public boolean isMonochrome() {
   		return true;
   }
   
   public ColorModelParam update(float center, float width, boolean inverse) {
      if (width < 0) {
         throw new IllegalArgumentException("width: " + width);
      }
      return new MonochromeParam(this, center, width, inverse);
   }
   
   public int hashCode() {
      return hashcode;
   }
   
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      }
      if (!(o instanceof MonochromeParam)) {
         return false;
      }
      MonochromeParam other = (MonochromeParam)o;
      if (this.getNumberOfWindows() == 0) {
         if (other.getNumberOfWindows() != 0) {
            return false;
         }
      } else {
         if (other.getNumberOfWindows() == 0
         || this.center[0] != other.center[0]
         || this.width[0] != other.width[0]) {
            return false;
         }
      }
      return this.inverse == other.inverse
         && this.intercept == other.intercept
         && this.slope == other.slope
         && this.max == other.max
         && this.min == other.min
         && this.pv2dll == other.pv2dll;
   }
   
   public final float getRescaleSlope() {
      return slope;
   }
   
   public final float getRescaleIntercept() {
      return intercept;
   }
   
   public final float getWindowCenter(int index) {
      return center[index];
   }
   
   public final float getWindowWidth(int index) {
      return width[index];
   }
   
   public final int getNumberOfWindows() {
      return Math.min(center.length, width.length);
   }
   
   public final boolean isCacheable() {
      return true;
   }
   
   public final boolean isInverse() {
      return inverse != 0;
   }
   
   public final float toMeasureValue(int pxValue) {
      return toSampleValue(pxValue)*slope + intercept;
   }
   
   public final int toPixelValue(float measureValue) {
      return (int)((measureValue - intercept) / slope);
   }
   
   private static int toARGB(byte grey) {
      int v = grey & 0xff;
      return 0xff000000 // alpha
         | (v << 16)
         | (v << 8)
         | (v);
   }
   
   public ColorModel newColorModel() {
      int[] cmap = new int[size];
      if (getNumberOfWindows() == 0) {
         if (min == 0) {
            for (int i = 0; i < size; ++i) {
               cmap[i] = toARGB(pv2dll[(((i>>rshift)<<lshift) ^ inverse) & andmask]);
            }
         } else {
            for (int i = 0, j = size>>1; j < size; ++j,++i) {
               cmap[i] = toARGB(pv2dll[(((j>>rshift)<<lshift) ^ inverse) & andmask]);
               cmap[j] = toARGB(pv2dll[(((i>>rshift)<<lshift) ^ inverse) & andmask]);
            }
         }
      } else {
         createCMAP(cmap, (int)((center[0] - intercept)/slope),
            (int)(width[0]/slope));
      }
      return new IndexColorModel(bits, size, cmap, 0, false, -1, dataType);
   }
   
   private void createCMAP(int[] cmap, int c, int w) {
      int u = c - (w>>1);
      int o = u + w;
      int cmin = toARGB(pv2dll[0]);
      int cmax = toARGB(pv2dll[pv2dll.length-1]);
      if (u > 0) {
         Arrays.fill(cmap, 0, Math.min(u,max),
            inverse == 0 ? cmin : cmax);
      }
      if (o < max) {
         Arrays.fill(cmap, Math.max(0,o), max,
            inverse == 0 ? cmax : cmin);
      }
      for (int i = Math.max(0,u), n = Math.min(o,max); i < n; ++i) {
         cmap[i] = toARGB(pv2dll[(((i-u)<<pvBits) / w ^ inverse) & andmask]);
      }
      if (min == 0) {
         return; // all done for unsigned px val
      }
      if (u > min) {
         Arrays.fill(cmap, size>>1, Math.min(u+size, size),
            inverse == 0 ? cmin : cmax);
      }
      if (o < 0) {
         Arrays.fill(cmap, Math.max(o+size,size>>1), size,
            inverse == 0 ? cmax : cmin);
      }
      for (int i = Math.max(min,u), n = Math.min(o,0); i < n; ++i) {
         cmap[i+size] = toARGB(pv2dll[(((i-u)<<pvBits) / w ^ inverse) & andmask]);
      }
   }
}
