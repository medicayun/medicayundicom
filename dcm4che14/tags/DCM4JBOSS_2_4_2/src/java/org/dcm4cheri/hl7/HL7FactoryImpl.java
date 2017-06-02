/*$Id: HL7FactoryImpl.java 3573 2002-09-22 22:09:37Z gunterze $*/
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

package org.dcm4cheri.hl7;

import org.dcm4che.hl7.HL7Factory;
import org.dcm4che.hl7.HL7Message;
import org.dcm4che.hl7.MSHSegment;
import org.dcm4che.hl7.HL7Exception;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
public class HL7FactoryImpl extends HL7Factory {
  
    public MSHSegment parseMSH(byte[] data) throws HL7Exception { 
      return new MSHSegmentImpl(data);
   }

   public HL7Message parse(byte[] data)
   throws HL7Exception {   
      return new HL7MessageImpl(data);
   }   
}
