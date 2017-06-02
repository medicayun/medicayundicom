/*$Id: DictionaryFactoryImpl.java 3656 2003-06-09 17:17:51Z gunterze $*/
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

import org.dcm4che.dict.TagDictionary;
import org.dcm4che.dict.UIDDictionary;

import java.io.*;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
public class DictionaryFactoryImpl
        extends org.dcm4che.dict.DictionaryFactory {

    private static final String DEF_TAG_DICT =
            "org/dcm4cheri/dict/TagDictionary.ser";    
    private static final String DEF_UID_DICT =
            "org/dcm4cheri/dict/UIDDictionary.ser";    
    private static TagDictionary defTagDict;
    private static UIDDictionary defUIDDict;
    
    /** Creates a new instance of DictionaryFactoryImpl */
    public DictionaryFactoryImpl() {
    }
    
    public TagDictionary newTagDictionary() {
        return new TagDictionaryImpl();
    }

    public TagDictionary getDefaultTagDictionary() {
        if (defTagDict != null)
            return defTagDict;
        synchronized (this) {
            if (defTagDict != null)
                return defTagDict;
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            InputStream in = loader.getResourceAsStream(DEF_TAG_DICT);
            if (in == null)
                throw new RuntimeException("Missing " + DEF_TAG_DICT);
            ObjectInputStream ois = null;
            try {
                ois = new ObjectInputStream(new BufferedInputStream(in));
                return (defTagDict = (TagDictionary)ois.readObject());
            } catch (Exception ex) {                
                throw new RuntimeException("Load DefaultTagDictionary from "
                        + DEF_TAG_DICT + " failed!", ex);
            } finally {
                try {
                    (ois != null ? ois : in).close();
                } catch (IOException ignore) {}
            }
        }           
    }
    
    static void initDefTagDict(File xmlFile, File serFile)
            throws Exception {
        TagDictionaryImpl dict = new TagDictionaryImpl();
        dict.load(xmlFile);
        serFile.getParentFile().mkdirs();
        ObjectOutputStream oos = new ObjectOutputStream(
            new BufferedOutputStream(new FileOutputStream(serFile)));
        try {
            oos.writeObject(dict);
            System.out.println("Create: " + serFile);
        } finally {
            oos.close();
        }
    }
    
    public UIDDictionary newUIDDictionary() {
        return new UIDDictionaryImpl();
    }

    public UIDDictionary getDefaultUIDDictionary() {
        if (defUIDDict != null)
            return defUIDDict;
        synchronized (this) {
            if (defUIDDict != null)
                return defUIDDict;
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            InputStream in = loader.getResourceAsStream(DEF_UID_DICT);
            if (in == null)
                throw new RuntimeException("Missing " + DEF_UID_DICT);
            ObjectInputStream ois = null;
            try {
                ois = new ObjectInputStream(new BufferedInputStream(in));
                return (defUIDDict = (UIDDictionary)ois.readObject());
            } catch (Exception ex) {                
                throw new RuntimeException("Load DefaultUIDDictionary from "
                        + DEF_UID_DICT + " failed!", ex);
            } finally {
                try {
                    (ois != null ? ois : in).close();
                } catch (IOException ignore) {}
            }
        }           
    }

    static void initDefUIDDict(File xmlFile, File serFile)
            throws Exception {
        UIDDictionaryImpl dict = new UIDDictionaryImpl();
        dict.load(xmlFile);
        serFile.getParentFile().mkdirs();
        ObjectOutputStream oos = new ObjectOutputStream(
            new BufferedOutputStream(new FileOutputStream(serFile)));
        try {
            oos.writeObject(dict);
            System.out.println("Create: " + serFile);
        } finally {
            oos.close();
        }
    }
    
    public static void main(String args[]) throws Exception {
        if (args.length != 2) {
            System.out.println(
"Usage: java -cp <classpath> org/dcm4cheri/dict/DictionaryFactoryImpl \\\n" +
"  <dictionary.xml> <resdir>");
            System.exit(1);
        }
        File resdir = new File(args[1]);
        initDefTagDict(new File(args[0]), new File(resdir, DEF_TAG_DICT));
        initDefUIDDict(new File(args[0]), new File(resdir, DEF_UID_DICT));
    }
}
