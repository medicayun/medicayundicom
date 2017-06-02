/*$Id: TagDictionaryTest.java 3493 2002-07-14 16:03:36Z gunterze $*/
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

package org.dcm4che.dict;

import org.dcm4che.dict.TagDictionary;
import org.dcm4che.dict.DictionaryFactory;

import java.io.*;
import junit.framework.*;

/**
 *
 * @author gunter.zeilinger@tiani.com
 */                                
public class TagDictionaryTest extends TestCase {
    
    public TagDictionaryTest(java.lang.String testName) {
        super(testName);
    }        
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(TagDictionaryTest.class);
        return suite;
    }

    private static final String DICT_XML = "data/dictionary.xml";

    private DictionaryFactory factory;
    
    protected void setUp() throws Exception {
        factory = DictionaryFactory.getInstance();
    }
    
    public void testDefaultTagDictionary() throws Exception {
        check(factory.getDefaultTagDictionary());
    }
    
    public void testLoad() throws Exception {
        TagDictionary dict = factory.newTagDictionary();
        dict.load(new File(DICT_XML));
        check(dict);
    }
    
    private void check(TagDictionary dict) {
    }
}
