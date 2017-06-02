/*  Copyright (c) 2002,2003 by TIANI MEDGRAPH AG
 *
 *  This file is part of dcm4che.
 *
 *  This library is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU Lesser General Public License as published
 *  by the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.dcm4che.data;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Date;

/** Element in <code>DcmObject</code>.
 *
 * @author     <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @since March 2002
 * @version $Revision: 3719 $ $Date: 2003-09-01 23:41:37 +0800 (周一, 01 9月 2003) $
 * @see "DICOM Part 5: Data Structures and Encoding, 7.1 Data Elements"
 * @see "DICOM Part 7: Message Exchange, 6.3.1 Command Set Structure"
 */
public interface DcmElement {
   
   int tag();
   
   int vr();
   
   int vm();
   
   int length();
   
   boolean isEmpty();
   
   int hashCode();
   
   DcmElement share();
      
   ByteBuffer getByteBuffer();
   
   ByteBuffer getByteBuffer(ByteOrder byteOrder);
   
   boolean hasDataFragments();
   
   ByteBuffer getDataFragment(int index);
   
   ByteBuffer getDataFragment(int index, ByteOrder byteOrder);
   
   int getDataFragmentLength(int index);
   
   String getString(Charset cs) throws DcmValueException;
   
   String getString(int index, Charset cs) throws DcmValueException;
   
   String[] getStrings(Charset cs) throws DcmValueException;
   
   String getBoundedString(int maxLen, Charset cs)
   throws DcmValueException;
   
   String getBoundedString(int maxLen, int index, Charset cs)
   throws DcmValueException;
   
   String[] getBoundedStrings(int maxLen, Charset cs)
   throws DcmValueException;
   
   int getInt() throws DcmValueException;
   
   int getInt(int index) throws DcmValueException;
   
   int[] getInts() throws DcmValueException;
   
   int getTag() throws DcmValueException;
   
   int getTag(int index) throws DcmValueException;
   
   int[] getTags() throws DcmValueException;
   
   float getFloat() throws DcmValueException;
   
   float getFloat(int index) throws DcmValueException;
   
   float[] getFloats() throws DcmValueException;
   
   double getDouble() throws DcmValueException;
   
   double getDouble(int index) throws DcmValueException;
   
   double[] getDoubles() throws DcmValueException;
   
   Date getDate() throws DcmValueException;
   
   Date getDate(int index) throws DcmValueException;
   
   Date[] getDates() throws DcmValueException;

   Date[] getDateRange() throws DcmValueException;

   Date[] getDateRange(int index) throws DcmValueException;
   
   PersonName getPersonName(Charset cs)  throws DcmValueException;
    
   PersonName getPersonName(int index, Charset cs) throws DcmValueException;
   
   void addDataFragment(ByteBuffer byteBuffer);
   
   boolean hasItems();
   
   Dataset addNewItem();
   
   void addItem(Dataset item);
   
   Dataset getItem();
   
   Dataset getItem(int index);
   
   DcmElement setStreamPosition(long streamPos);
   
   long getStreamPosition();

}
