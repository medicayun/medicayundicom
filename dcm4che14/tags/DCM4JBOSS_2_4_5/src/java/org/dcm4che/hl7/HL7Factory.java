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

package org.dcm4che.hl7;

import org.dcm4che.Implementation;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version $Revision: 3545 $ $Date: 2002-08-12 16:23:30 +0800 (周一, 12 8月 2002) $
 */
public abstract class HL7Factory {

   public static HL7Factory getInstance() {
      return (HL7Factory)Implementation.findFactory(
            "dcm4che.hl7.HL7Factory");
   }
    
   public abstract MSHSegment parseMSH(byte[] data)
   throws HL7Exception;
   
   public abstract HL7Message parse(byte[] data)
   throws HL7Exception;
}
