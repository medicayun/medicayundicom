/*$Id: TagDictionaryImpl.java 3656 2003-06-09 17:17:51Z gunterze $*/
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

import org.dcm4che.dict.Tags;
import org.dcm4cheri.util.IntHashtable2;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.util.Iterator;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
public class TagDictionaryImpl implements org.dcm4che.dict.TagDictionary,
                                          java.io.Serializable {

    static final long serialVersionUID = 5940638712350400261L;

    private transient IntHashtable2[] tables = {
        new IntHashtable2()
    };

    /** Creates a new instance of TagDictionaryImpl */
    public TagDictionaryImpl() {
    }

    public Entry lookup(int tag) {
        Object ret;
        for (int i = 0; i < tables.length; ++i) {
            if ((ret = tables[i].get(tag)) != null) {
                return (Entry)ret;
            }
        }
        return null;
    }

    public String toString(int tag) {
       Entry e = lookup(tag);
       return e != null
            ? (Tags.toString(tag) + " " + e.name)
            : Tags.toString(tag);
    }    
    
    /** Adds record to dictionary
     * @param entry dictionary record
     */
    public final void add(Entry entry) {
        getTableForMask(entry.mask).put(entry.tag, entry);
    }
    
    public int size() {
        int count = 0;
        for (int i = 0; i < tables.length; ++i) {
            count += tables[i].size();
        }
        return count;
    }

    private IntHashtable2 getTableForMask(int mask) {
        for (int i = 0; i < tables.length; ++i) {
            if (mask == tables[i].mask()) {
                return tables[i];
            }
        }
        IntHashtable2[] tmp = tables;
        tables = new IntHashtable2[tmp.length + 1];
        System.arraycopy(tmp,0,tables,0,tmp.length);
        IntHashtable2 newTable = new IntHashtable2();
        newTable.mask(mask);
        tables[tmp.length] = newTable;
        tmp = null;
        return newTable;
    }
    
    public void load(InputSource xmlSource) throws IOException, SAXException {
        new TagDictionaryLoader(this).parse(xmlSource);
    }

    public void load(File xmlFile) throws IOException, SAXException {
        new TagDictionaryLoader(this).parse(xmlFile);
    }
    
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(tables.length);
        for (int i = 0; i < tables.length; ++i)
            writeTable(out, tables[i]);
    }

    private void writeTable(ObjectOutputStream out, IntHashtable2 table)
            throws IOException {
        out.writeInt(table.mask());
        out.writeInt(table.size());
        for (Iterator it = table.iterator(); it.hasNext();) {
            Entry entry = (Entry)it.next();
            out.writeInt(entry.tag);
            out.writeUTF(entry.vr);
            out.writeUTF(entry.vm);
            out.writeUTF(entry.name);
        }        
    }
    
    private void readObject(ObjectInputStream in) throws IOException {
        tables = new IntHashtable2[in.readInt()];
        for (int i = 0; i < tables.length; ++i)
            tables[i] = readTable(in);
    }

    private IntHashtable2 readTable(ObjectInputStream in) throws IOException {
        int mask = in.readInt();
        int n = in.readInt();
        IntHashtable2 table = new IntHashtable2(n);
        table.mask(mask);
        for (int i = 0; i < n; ++i) {
            Entry entry = new Entry(in.readInt(), mask, in.readUTF(),
                    in.readUTF(), in.readUTF());
            table.put(entry.tag, entry);
        }
        return table;
    }
}
