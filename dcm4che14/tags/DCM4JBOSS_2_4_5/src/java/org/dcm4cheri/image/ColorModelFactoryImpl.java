/*$Id: ColorModelFactoryImpl.java 3623 2003-04-10 10:17:34Z jforaci $*/
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

package org.dcm4cheri.image;

import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import org.dcm4che.image.ColorModelFactory;
import org.dcm4che.image.ColorModelParam;

import java.awt.image.ColorModel;
import java.util.WeakHashMap;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
public class ColorModelFactoryImpl extends ColorModelFactory {
   private static final WeakHashMap cache = new WeakHashMap();
   
   private final static byte[] P2D_IDENTITY = new byte[256];
   static { for (int i = 0; i < 256; ++i) P2D_IDENTITY[i] = (byte)i; }
   
   /** Creates a new instance of ColorModelFactoryImpl */
   public ColorModelFactoryImpl() {
   }
   
   public ColorModel getColorModel(ColorModelParam param) {
      if (!param.isCacheable()) {
         return param.newColorModel();
      }
      ColorModel cm = (ColorModel)cache.get(param);
      if (cm == null) {
         cache.put(param, cm = param.newColorModel());
      }
      return cm;
   }
   
   public ColorModelParam makeParam(Dataset ds) {
      return makeParam(ds, null);
   }
   
   public ColorModelParam makeParam(Dataset ds, byte[] pv2dll) {
      String pmi =
         ds.getString(Tags.PhotometricInterpretation, "MONOCHROME2");
      if ("PALETTE COLOR".equals(pmi)) {
         return new PaletteColorParam(ds);
      }
      boolean mono1 = "MONOCHROME1".equals(pmi);
      if (!mono1 && !"MONOCHROME2".equals(pmi)) {
         throw new UnsupportedOperationException("pmi: " + pmi);
      }
      String pLUTShape = ds.getString(Tags.PresentationLUTShape);
      return new MonochromeParam(ds,
         pLUTShape == null ? mono1 : "INVERSE".equals(pLUTShape),
         pv2dll == null ? P2D_IDENTITY : pv2dll);
   }
   
}
