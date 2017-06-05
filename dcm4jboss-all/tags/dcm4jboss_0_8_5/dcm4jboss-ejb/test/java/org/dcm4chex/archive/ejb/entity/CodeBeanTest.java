/*
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
/* 
 * File: $Source$
 * Author: gunter
 * Date: 16.07.2003
 * Time: 22:50:40
 * CVS Revision: $Revision: 857 $
 * Last CVS Commit: $Date: 2003-08-28 04:44:53 +0800 (周四, 28 8月 2003) $
 * Author of last CVS Commit: $Author: gunterze $
 */
package org.dcm4chex.archive.ejb.entity;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.cactus.ServletTestCase;
import org.dcm4chex.archive.ejb.interfaces.CodeLocalHome;

/**
 * @author <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 *
 */
public class CodeBeanTest extends ServletTestCase
{

    public static final String VALUE_ = "999";
    public static final String MEANING_ = "Meaning of code 999";
    public static final String DESIGNATOR = "99DCM4CHE";
    private CodeLocalHome codeHome;
    private Object[] codePks;

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(CodeBeanTest.class);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        Context ctx = new InitialContext();
        codeHome = (CodeLocalHome) ctx.lookup("java:comp/env/ejb/Code");
        ctx.close();
        codePks = new Object[5];
        for (int i = 0; i < 5; ++i)
        {
            String value = VALUE_ + i;
            String meaning = MEANING_ + i;
            codePks[i] = codeHome.create(value, DESIGNATOR, null, meaning).getPrimaryKey();
        }
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        for (int i = 0; i < 5; ++i)
        {
            codeHome.remove(codePks[i]);
        }
    }

    /**
     * Constructor for StudyBeanTest.
     * @param arg0
     */
    public CodeBeanTest(String arg0)
    {
        super(arg0);
    }

    public void testFindByValueAndDesignator() throws Exception
    {
        for (int i = 0; i < 5; i++)
        {
            String value = VALUE_ + i;
            codeHome.findByValueAndDesignator(value, DESIGNATOR);
        }
    }
}
