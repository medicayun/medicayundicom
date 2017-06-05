/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.ejb.entity;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.cactus.ServletTestCase;
import org.dcm4chex.archive.ejb.interfaces.FileLocal;
import org.dcm4chex.archive.ejb.interfaces.FileLocalHome;
import org.dcm4chex.archive.ejb.interfaces.FileSystemLocal;
import org.dcm4chex.archive.ejb.interfaces.FileSystemLocalHome;

/**
 * @author <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 * @version $Revision: 1237 $ $Date: 2004-11-30 06:28:36 +0800 (周二, 30 11月 2004) $
 */
public class FileBeanTest extends ServletTestCase {
    public static final String RETRIEVE_AETS = "QR_SCP";
    public static final String DIRPATH = "/var/local/archive";
    public static final String FILEID = "2003/07/11/12345678/9ABCDEF0";
    public static final String TSUID = "1.2.40.0.13.1.1.9999.3";
    public static final int SIZE = 567890;
    public static final byte[] MD5 =
        { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16 };

    private FileLocalHome fileHome;
    private FileSystemLocalHome fileSystemHome;

    public static void main(String[] args) {
        junit.textui.TestRunner.run(FileBeanTest.class);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        Context ctx = new InitialContext();
        fileSystemHome = (FileSystemLocalHome) ctx.lookup("java:comp/env/ejb/FileSystem");
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
        FileSystemLocal fs =
            fileSystemHome.create(
                DIRPATH,
                RETRIEVE_AETS);
        FileLocal file =
            fileHome.create(
                FILEID,
                TSUID,
                SIZE,
                MD5,
                null,
                fs);
        file.remove();
        fs.remove();
    }

    private static String getHostName() throws UnknownHostException {
        String hostname = InetAddress.getLocalHost().getHostName();
        int pos = hostname.indexOf('.');
        return pos != -1 ? hostname.substring(0, pos) : hostname;
    }
}
