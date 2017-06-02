/*                                                                           *
 *  Copyright (c) 2002, 2003 by TIANI MEDGRAPH AG                            *
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
 */

package org.dcm4che.hl7;

/**
 * <description> 
 *
 * @author  <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 * @version $Revision: 3615 $ $Date: 2003-03-14 08:32:51 +0800 (周五, 14 3月 2003) $
 *   
 */
public interface MSHSegment extends HL7Segment
{
   String getSendingApplication();
   String getSendingFacility();
   String getReceivingApplication();
   String getReceivingFacility();
   String getMessageType();   
   String getTriggerEvent();
   String getMessageControlID();
   String getCharacterSet();
   String getCharacterSetAsISO_IR();

   byte[] makeACK_AA();    
   byte[] makeACK_AR(String errText, String errCode, String errComment);    
   byte[] makeACK_AE(String errText, String errCode, String errComment);
}
