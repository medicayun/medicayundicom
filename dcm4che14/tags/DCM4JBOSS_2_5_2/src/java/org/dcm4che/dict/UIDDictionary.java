/*$Id: UIDDictionary.java 3572 2002-09-19 21:54:11Z gunterze $*/
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

package org.dcm4che.dict;

import java.io.File;
import java.io.IOException;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
public interface UIDDictionary {
    
    static final class Entry {
        public final String uid;
        public final String name;
        public Entry(String uid, String name) {
            this.uid = uid;
            this.name = name;
        }
	public String toString() {
	    return uid + "/" + name;
	}
    }
    
    String toString(String uid);
    
    int size();
    
    Entry lookup(String uid);
    
    void add(Entry entry);
    
    void load(InputSource xmlSource) throws IOException, SAXException;
    
    void load(File xmlFile) throws IOException, SAXException;
    
}
