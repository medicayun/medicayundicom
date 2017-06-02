/*$Id: PDataTFImpl.java 3575 2002-09-26 13:56:31Z gunterze $*/
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
import java.util.*;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
final class PDataTFImpl implements PDataTF {
   
   private static final int DEF_MAX_LENGTH = 0xFFFF;
   private static final int MIN_MAX_LENGTH = 128;
   private final byte[] buf;
   private int pdulen;
   private int wpos;
   private final LinkedList pdvs = new LinkedList();
   private final Iterator it;
   private PDVImpl curPDV = null;
   
   static PDataTFImpl parse(UnparsedPDUImpl raw) throws PDUException {
      if (raw.buffer() == null) {
         throw new PDUException(
         "PDU length exceeds supported maximum " + raw,
         new AAbortImpl(AAbort.SERVICE_PROVIDER,
         AAbort.REASON_NOT_SPECIFIED));
      }
      return new PDataTFImpl(raw.length(), raw.buffer());
   }
   
   private PDataTFImpl(int pdulen, byte[] buf) throws PDUException {
      this.pdulen = pdulen;
      this.wpos = pdulen + 12;
      this.buf = buf;
      int off = 6;
      while (off <= pdulen) {
         PDVImpl pdv = new PDVImpl(off);
         pdvs.add(pdv);
         off += 4 + pdv.length();
      }
      if (off != pdulen + 6) {
         throw new PDUException("Illegal " + toString(),
         new AAbortImpl(AAbort.SERVICE_PROVIDER,
         AAbort.INVALID_PDU_PARAMETER_VALUE));
      }
      this.it = pdvs.iterator();
   }
   
   PDataTFImpl(int maxLength) {
      if (maxLength == 0) {
         maxLength = DEF_MAX_LENGTH;
      }
      if (maxLength < MIN_MAX_LENGTH
      || maxLength > UnparsedPDUImpl.MAX_LENGTH) {
         throw new IllegalArgumentException("maxLength:" + maxLength);
      }
      this.pdulen = 0;
      this.wpos = 12;
      this.buf = new byte[6 + maxLength];
      this.it = null;
   }
   
   public void clear() {
      if (it != null) {
         throw new IllegalStateException("P-DATA-TF read only");
      }
      pdulen = 0;
      wpos = 12;
      pdvs.clear();
   }
   
   public PDV readPDV() {
      if (it == null) {
         throw new IllegalStateException("P-DATA-TF write only");
      }
      return it.hasNext() ? (PDV)it.next() : null;
   }
   
   public String toString(boolean verbose) {
      return toString();
   }
   
   public String toString() {
      return toStringBuffer(new StringBuffer()).toString();
   }
   
   StringBuffer toStringBuffer(StringBuffer sb) {
      sb.append("P-DATA-TF[pdulen=").append(pdulen).append("]");
      Iterator it = pdvs.iterator();
      while (it.hasNext()) {
         sb.append("\n\t").append(it.next());
      }
      return sb;
   }
   
   public final int free() {
      return buf.length - wpos;
   }
   
   public void openPDV(int pcid, boolean cmd) {
      if (it != null) {
         throw new IllegalStateException("P-DATA-TF read only");
      }
      if ((pcid & 1) == 0) {
         throw new IllegalArgumentException("pcid=" + pcid);
      }
      if (curPDV != null) {
         throw new IllegalStateException("Open PDV " + curPDV);
      }
      if (free() < 0) {
         throw new IllegalStateException("Maximal length of PDU reached");
      }
      curPDV = new PDVImpl(6 + pdulen);
      curPDV.pcid(pcid);
      curPDV.cmd(cmd);
      pdulen += 6;
   }
   
   boolean isOpenPDV() {
      return curPDV != null;
   }
   
   boolean isEmpty() {
      return pdvs.isEmpty();
   }
   
   public void closePDV(boolean last) {
      if (curPDV == null) {
         throw new IllegalStateException("No open PDV");
      }
      curPDV.last(last);
      curPDV.close();
      pdvs.add(curPDV);
      curPDV = null;
      wpos += 6;
   }
   
   public final boolean write(int b) {
      if (curPDV == null) {
         throw new IllegalStateException("No open PDV");
      }
      if (wpos >= buf.length) return false;
      buf[wpos++] = (byte)b;
      ++pdulen;
      return true;
   }
   
   public final int write(byte[] b, int off, int len) {
      if (curPDV == null) {
         throw new IllegalStateException("No open PDV");
      }
      int wlen = Math.min(len, buf.length - wpos);
      System.arraycopy(b, off, buf, wpos, wlen);
      wpos += wlen;
      pdulen += wlen;
      return wlen;
   }
   
   public void writeTo(OutputStream out) throws IOException {
      if (curPDV != null) {
         throw new IllegalStateException("Open PDV " + curPDV);
      }
      buf[0] = (byte)4;
      buf[1] = (byte)0;
      buf[2] = (byte)(pdulen >> 24);
      buf[3] = (byte)(pdulen >> 16);
      buf[4] = (byte)(pdulen >> 8);
      buf[5] = (byte)(pdulen >> 0);
      out.write(buf, 0, pdulen + 6);
   }
   
   final class PDVImpl implements PDataTF.PDV {
      final int off;
      PDVImpl(int off) {
         this.off = off;
         buf[off+5] &= 3;
      }
      
      final void pcid(int pcid) {
         buf[off+4] = (byte)pcid;
      }
      
      final void length(int pdvLen) {
         buf[off] = (byte)(pdvLen >> 24);
         buf[off+1] = (byte)(pdvLen >> 16);
         buf[off+2] = (byte)(pdvLen >> 8);
         buf[off+3] = (byte)(pdvLen >> 0);
      }
      
      final void cmd(boolean cmd) {
         if (cmd) {
            buf[off+5] |= 1;
         } else {
            buf[off+5] &= ~1;
         }
      }
      
      final void last(boolean last) {
         if (last) {
            buf[off+5] |= 2;
         } else {
            buf[off+5] &= ~2;
         }
      }
      
      final void close() {
         length(wpos - off - 4);
      }
      
      public final int length() {
         return ((buf[off] & 0xff) << 24)
         | ((buf[off+1] & 0xff) << 16)
         | ((buf[off+2] & 0xff) << 8)
         | ((buf[off+3] & 0xff) << 0);
      }
      
      public final int pcid() {
         return buf[off+4] & 0xFF;
      }
      
      public final boolean cmd() {
         return (buf[off+5] & 1) != 0;
      }
      
      public final boolean last() {
         return (buf[off+5] & 2) != 0;
      }
      
      public final InputStream getInputStream() {
         return new ByteArrayInputStream(buf, off + 6, length() - 2);
      }
           
      public String toString() {
         return toStringBuffer(new StringBuffer()).toString();
      }
      
      StringBuffer toStringBuffer(StringBuffer sb) {
         return sb.append("PDV[pc-").append(pcid())
         .append(cmd() ? ",cmd" : ",data")
         .append(last() ? "(last),off=" : ",off=").append(off)
         .append(",pdvlen=").append(length())
         .append("]");
      }
   }
}
