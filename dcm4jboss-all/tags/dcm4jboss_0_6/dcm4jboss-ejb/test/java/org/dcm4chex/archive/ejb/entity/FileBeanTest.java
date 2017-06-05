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
 * Date: 11.07.2003
 * Time: 18:50:40
 * CVS Revision: $Revision: 1006 $
 * Last CVS Commit: $Date: 2004-02-23 04:09:10 +0800 (周一, 23 2月 2004) $
 * Author of last CVS Commit: $Author: gunterze $
 */
package org.dcm4chex.archive.ejb.entity;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.cactus.ServletTestCase;
import org.dcm4chex.archive.ejb.interfaces.FileLocal;
import org.dcm4chex.archive.ejb.interfaces.FileLocalHome;

/**
 * @author <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 *
 */
public class FileBeanTest extends ServletTestCase {
    public static final String RETRIEVE_AETS = "QR_SCP";
    public static final String BASEDIR = "/var/local/archive";
    public static final String FILEID = "2003/07/11/12345678/9ABCDEF0";
    public static final String TSUID = "1.2.40.0.13.1.1.9999.3";
    public static final int SIZE = 567890;
    public static final byte[] MD5 =
        { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16 };

    private FileLocalHome fileHome;

    public static void main(String[] args) {
        junit.textui.TestRunner.run(FileBeanTest.class);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        Context ctx = new InitialContext();
        fileHome = (FileLocalHome) ctx.lookup("java:comp/env/ejb/File");
        ctx.close();
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {}

    /**
     * Constructor for StudyBeanTest.
     * @param arg0
     */
    public FileBeanTest(String arg0) {
        super(arg0);
    }

    public void testCreate() throws Exception {
        FileLocal file =
            fileHome.create(
                RETRIEVE_AETS,
                BASEDIR,
                FILEID,
                TSUID,
                SIZE,
                MD5,
                null);
        file.remove();
    }

    private static String getHostName() throws UnknownHostException {
        String hostname = InetAddress.getLocalHost().getHostName();
        int pos = hostname.indexOf('.');
        return pos != -1 ? hostname.substring(0, pos) : hostname;
    }
}
