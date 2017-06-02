/*$Id: ColorModelFactory.java 3589 2002-11-21 22:38:48Z gunterze $*/
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

package org.dcm4che.image;

import org.dcm4che.Implementation;
import org.dcm4che.data.Dataset;
import java.awt.image.ColorModel;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
public abstract class ColorModelFactory {
   
   public static ColorModelFactory getInstance() {
      return (ColorModelFactory)Implementation.findFactory(
         "dcm4che.image.ColorModelFactory");
   }
   
   public abstract ColorModelParam makeParam(Dataset ds);
   
   public abstract ColorModelParam makeParam(Dataset ds, byte[] pv2dll);
   
   public abstract ColorModel getColorModel(ColorModelParam param);
}
