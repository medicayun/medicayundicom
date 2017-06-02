/*$Id: UIDDictionaryImpl.java 3572 2002-09-19 21:54:11Z gunterze $*/
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

package org.dcm4cheri.dict;

import org.dcm4che.dict.UIDDictionary;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.util.HashMap;
import java.util.Iterator;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
public class UIDDictionaryImpl implements UIDDictionary, java.io.Serializable {

    static final long serialVersionUID = -4793624142653062179L;

    private transient HashMap map = new HashMap(257);

    /** Creates a new instance of TagDictionaryImpl */
    public UIDDictionaryImpl() {
    }

    public Entry lookup(String uid) {
        Entry entry = (Entry)map.get(uid);
	return entry != null
	    ? entry
	    : new Entry(uid, "?");
    }
    
    public String toString(String uid) {
       return lookup(uid).toString();
    }
    
    public final void add(Entry entry) {
        map.put(entry.uid, entry);
    }
    
    public int size() {
        return map.size();
    }
    
    public void load(InputSource xmlSource) throws IOException, SAXException {
        new UIDDictionaryLoader(this).parse(xmlSource);
    }

    public void load(File xmlFile) throws IOException, SAXException {
        new UIDDictionaryLoader(this).parse(xmlFile);
    }
    
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(map.size());
        for (Iterator iter = map.values().iterator(); iter.hasNext();) {
            Entry e = (Entry)iter.next();
            out.writeUTF(e.uid);
            out.writeUTF(e.name);
       }
    }

    private void readObject(ObjectInputStream in) throws IOException {
        int n = in.readInt();
        map = new HashMap(n * 4 / 3 + 1);
        for (int i = 0; i < n; ++i) {
            add(new Entry(in.readUTF(), in.readUTF()));
        }
    }
}
