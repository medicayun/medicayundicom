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

/*$Id: PersonName.java 3493 2002-07-14 16:03:36Z gunterze $*/

package org.dcm4che.data;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
public interface PersonName {
    /**  
     * Field number for get and set indicating the family name complex 
     */
    public static final int FAMILY = 0;
  
    /**  
     * Field number for get and set indicating the given name complex 
     */
    public static final int GIVEN = 1;
  
    /**  
     * Field number for get and set indicating the middle name 
     */
    public static final int MIDDLE = 2;
  
    /**  
     * Field number for get and set indicating the name prefix 
     */
    public static final int PREFIX = 3;
  
    /**  
     * Field number for get and set indicating the name suffix 
     */
    public static final int SUFFIX = 4;
  
    /** 
     * Gets the value for a given name field.
     * 
     * @param field the given name field.
     * @return the value for the given name field
     */
    public String get(int field);

    /** 
     * Sets the name field with the given value.
     * 
     * @param field the given name field.
     * @param value the value to be set for the given name field.
     */
    public void set(int field, String value);
    
    /**
     * Returns ideographic representation.
     *
     * @return ideographic representation
     * */
    public PersonName getIdeographic();

    /** 
     * Returns phonetic representation.
     * 
     * @return phonetic representation
     */
    public PersonName getPhonetic();
    
    /** 
     * Sets ideographic representation.
     *
     * @param the ideographic representation to be set
     */
    public void setIdeographic(PersonName ideographic);
    
    /** 
     * Sets phonetic representation.
     *
     * @param the phonetic representation to be set
     */
    public void setPhonetic(PersonName phonetic);
    
}

