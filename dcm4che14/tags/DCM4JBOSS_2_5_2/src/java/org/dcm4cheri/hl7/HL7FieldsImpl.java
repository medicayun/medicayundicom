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

package org.dcm4cheri.hl7;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

/**
 * <description> 
 *
 * @see <related>
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @version $Revision: 3615 $ $Date: 2003-03-14 08:32:51 +0800 (周五, 14 3月 2003) $
 *   
 */
class HL7FieldsImpl implements HL7Fields
{
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   protected final byte[] data;
   protected final int off;
   protected final int len;
   protected final byte[] delim;
   protected HL7Fields[] subFields = null;
   
   // Static --------------------------------------------------------
   static final HL7Fields NULL = new HL7Fields() {
      public String toString() {
         return "";
      }

      public String get(int index) {
         return "";
      }
         
      public int size() {
         return 0;
      }
      
      public String get(int[] index) {
         return "";
      }
      
      public int size(int index) {
         return 0;
      }
         
      public int size(int[] index) {
         return 0;
      }
      
      public void writeTo(ByteArrayOutputStream out) {
      }

      public void writeTo(int index, ByteArrayOutputStream out) {
      }
   };
   
   static HL7Fields newInstance(byte[] data, int off, int len, byte[] delim) {
      return len == 0 ? NULL : new HL7FieldsImpl(data, off, len, delim);
   }
   
   // Constructors --------------------------------------------------
   public HL7FieldsImpl(byte[] data, int off, int len, byte[] delim) {
      if (data.length < off + len) {
         throw new IllegalArgumentException("data.length[" + data.length
               + "] < off[" + off + "] + len [" + len + "]");
      }
      if (delim.length == 0) {
         throw new IllegalArgumentException("delim.length == 0");
      }
      this.data = data;
      this.off = off;
      this.len = len;
      this.delim = delim;
   }
   
   // Public --------------------------------------------------------
   
   public int size() {
      initSubFields();
      return subFields.length;
   }

   public int size(int index) {
     if (delim.length == 0) {
         throw new IllegalArgumentException("delim.length == 0");
     }
     initSubFields();
     return index < subFields.length ? subFields[index].size() : 0;
   }

   public int size(int[] index) {
      if (index.length > delim.length) {
         throw new IllegalArgumentException("index.length[" + index.length
               + "] > delim.length[" + delim.length + "]");
      }
      switch (index.length) {
         case 0:
            return size();
         case 1:
            return size(index[0]);
         default:
            initSubFields();
            if (index[0] >= subFields.length) {
               return 0;
            }
            int[] index_1 = new int[index.length-1];
            System.arraycopy(index, 1, index_1, 0, index_1.length);
            return subFields[index[0]].size(index_1);
      }
   }

   public void writeTo(ByteArrayOutputStream out) {
      out.write(data, off, len);
   }

   public void writeTo(int index, ByteArrayOutputStream out) {
      initSubFields();
      if (index < subFields.length) {
         subFields[index].writeTo(out);
      }
   }

   public String toString() {
       try {
           return new String(data, off, len, "ISO-8859-1");
       } catch (UnsupportedEncodingException e) {
           return new String(data, off, len);
       }
   }
   
   public String get(int index) {
     if (delim.length == 0) {
         throw new IllegalArgumentException("delim.length == 0");
     }
     initSubFields();
     return index < subFields.length ? subFields[index].toString() : "";
   }      
   
   public String get(int[] index) {
      if (index.length > delim.length) {
         throw new IllegalArgumentException("index.length[" + index.length
               + "] > delim.length[" + delim.length + "]");
      }
      switch (index.length) {
         case 0:
            return toString();
         case 1:
            return get(index[0]);
         default:
            initSubFields();
            if (index[0] >= subFields.length) {
               return "";
            }
            int[] index_1 = new int[index.length-1];
            System.arraycopy(index, 1, index_1, 0, index_1.length);
            return subFields[index[0]].get(index_1);
      }
   }

   private void initSubFields() {
      if (subFields != null) {
         return;
      }
      
      int index = 0;
      for (int i = off, n = off + len; i < n; ++i) {
         if (data[i] == delim[0]) {
            ++index;
         }
      }
      subFields = new HL7Fields[index+1];
      byte[] delim_1 = new byte[delim.length-1];
      System.arraycopy(delim, 1, delim_1, 0, delim_1.length);
      index = 0;
      int left = off;
      for (int i = off, n = off + len; i < n; ++i) {
         if (data[i] == delim[0]) {
            subFields[index++] = newInstance(data, left, i - left, delim_1);
            left = i+1;
         }
      }
      subFields[index] = newInstance(data, left, off + len - left, delim_1);      
   }   
}
