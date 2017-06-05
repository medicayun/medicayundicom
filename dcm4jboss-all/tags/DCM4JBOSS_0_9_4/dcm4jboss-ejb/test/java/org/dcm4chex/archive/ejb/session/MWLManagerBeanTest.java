/* $Id: MWLManagerBeanTest.java 1070 2004-03-24 20:49:49Z gunterze $
 * Copyright (c) 2002,2003 by TIANI MEDGRAPH AG
 *
 * This file is part of dcm4che.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.dcm4chex.archive.ejb.session;

import java.io.FileInputStream;
import java.io.IOException;

import javax.naming.Context;
import javax.naming.InitialContext;

import junit.framework.TestCase;

import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import org.dcm4cheri.util.DatasetUtils;
import org.dcm4chex.archive.ejb.interfaces.MWLManager;
import org.dcm4chex.archive.ejb.interfaces.MWLManagerHome;
import org.xml.sax.SAXException;

/**
 * @author <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 * @version $Revision: 1070 $ $Date: 2004-03-25 04:49:49 +0800 (周四, 25 3月 2004) $
 */
public class MWLManagerBeanTest extends TestCase
{

    public static final String FILE = "mwlitem.xml";

    private MWLManager mwlManager;

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(MWLManagerBeanTest.class);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        Context ctx = new InitialContext();
        MWLManagerHome home = (MWLManagerHome) ctx.lookup(MWLManagerHome.JNDI_NAME);
        ctx.close();
        mwlManager = home.create();
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        mwlManager.remove();
    }

    /**
     * Constructor for StudyBeanTest.
     * @param name
     */
    public MWLManagerBeanTest(String name)
    {
        super(name);
    }

    public void testAddAndRemoveWorklistItem() throws Exception
    {
        Dataset ds = loadMWLItemFromFile();
        Dataset spsItem = ds.getItem(Tags.SPSSeq);
        String spsId1ds = spsItem.getString(Tags.SPSID);
        // insert first entry with sps-id -> returns contained sps-id
        String spsId1ret = mwlManager.addWorklistItem(ds);
        assertEquals(spsId1ds, spsId1ret);        

        // insert second entry without sps-id -> returns new generated sps-id
        spsItem.remove(Tags.SPSID);
        String spsId2ret = mwlManager.addWorklistItem(ds);
        
        // remove first entry
        mwlManager.removeWorklistItem(spsId1ret);

        // remove second entry -> returned entry contains generated sps-id
        Dataset dsRet = mwlManager.removeWorklistItem(spsId2ret);                
        Dataset spsItemRet = dsRet.getItem(Tags.SPSSeq);
        String spsId2ds = spsItemRet.getString(Tags.SPSID);
        assertEquals(spsId2ret, spsId2ds);
    }

    private Dataset loadMWLItemFromFile() throws SAXException, IOException {
        FileInputStream fis = new FileInputStream(FILE);
        try {
            return DatasetUtils.fromXML(fis);
        } finally {
            fis.close();
        }
    }
}
