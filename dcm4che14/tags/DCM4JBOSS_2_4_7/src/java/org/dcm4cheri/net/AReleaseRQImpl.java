/*$Id: AReleaseRQImpl.java 3538 2002-08-06 17:45:17Z gunterze $*/
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

package org.dcm4cheri.net;

import org.dcm4che.net.*;

import java.io.*;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
final class AReleaseRQImpl implements AReleaseRQ {
   
   private AReleaseRQImpl() {
   }
   
   private static final AReleaseRQImpl instance = new AReleaseRQImpl();
   
   public static AReleaseRQImpl getInstance() {
      return instance;
   }
   
   public static AReleaseRQImpl parse(UnparsedPDUImpl raw)
   throws PDUException {
      if (raw.length() != 4) {
         throw new PDUException("Illegal A-RELEASE-RP " + raw,
         new AAbortImpl(AAbort.SERVICE_PROVIDER,
         AAbort.INVALID_PDU_PARAMETER_VALUE));
      }
      return instance;
   }
   
   private static final byte[] BYTES = { 5, 0, 0, 0, 0, 4, 0, 0, 0, 0 };
   public void writeTo(OutputStream out) throws IOException {
      out.write(BYTES);
//      out.flush();
   }
   
   public String toString(boolean verbose) {
      return toString();
   }

   public String toString() {
      return "A-RELEASE-RQ";
   }
}
