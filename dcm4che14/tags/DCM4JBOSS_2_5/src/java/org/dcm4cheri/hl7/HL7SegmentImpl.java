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

import org.dcm4che.hl7.HL7Segment;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * <description> 
 *
 * @see <related>
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @version $Revision: 3550 $ $Date: 2002-08-19 00:44:43 +0800 (周一, 19 8月 2002) $
 *   
 * <p><b>Revisions:</b>
 *
 * <p><b>yyyymmdd author:</b>
 * <ul>
 * <li> explicit fix description (no line numbers but methods) go 
 *            beyond the cvs commit message
 * </ul>
 */
public class HL7SegmentImpl extends HL7FieldsImpl
implements HL7Segment
{
   // Constants -----------------------------------------------------   
   private static final int MIN_LEN = 4;
   private static final byte[] DELIM = {
      (byte)'|', (byte)'~', (byte)'^', (byte)'&'
   };

   // Attributes ----------------------------------------------------
   private final String id;
   
   // Static --------------------------------------------------------
   static final ResourceBundle DICT =
         ResourceBundle.getBundle("org/dcm4cheri/hl7/HL7Dictionary");
   
   static String getName(String key, String defVal) {
      try {
         return DICT.getString(key);
      } catch (MissingResourceException e) {
         return defVal;
      }
   }
   
   // Constructors --------------------------------------------------
   HL7SegmentImpl(byte[] data, int off, int len) {
      super(data, off, len, DELIM);
      if (len < MIN_LEN || data[off+3] != (byte)'|') {
         throw new IllegalArgumentException(toString());
      }
      this.id = super.get(0);
   }
   
   // Public --------------------------------------------------------
   public String id() {
      return id;
   }
   
   public String get(int seq, int rep) {
      return super.get(new int[]{ seq, rep-1 });
   }
   
   public String get(int seq, int rep, int comp) {
      return super.get(new int[]{ seq, rep-1, comp-1 });
   }
   
   public String get(int seq, int rep, int comp, int sub) {
      return super.get(new int[]{ seq, rep-1, comp-1, sub-1 });
   }
   
   public int size(int seq, int rep) {
      return super.size(new int[]{ seq, rep-1 });
   }

   public int size(int seq, int rep, int comp) {
      return super.size(new int[]{ seq, rep-1, comp-1 });
   }
   
   StringBuffer toVerboseStringBuffer(StringBuffer sb) {
      sb.append(id).append(" - ").append(getName(id, ""));
      for (int i = 1, n = size(); i < n; ++i) {
         String key = id + '.' + i;
         sb.append("\n\t").append(key)
           .append(": ").append(get(i)).append("\t\t//")
           .append(getName(key, "????"));
      }
      return sb;
   }

   public String toVerboseString() {
      return toVerboseStringBuffer(new StringBuffer()).toString();
   }      
}
