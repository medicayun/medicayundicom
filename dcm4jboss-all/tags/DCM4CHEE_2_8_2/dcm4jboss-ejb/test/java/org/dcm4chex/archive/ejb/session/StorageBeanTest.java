/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), available at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * TIANI Medgraph AG.
 * Portions created by the Initial Developer are Copyright (C) 2003-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunter.zeilinger@tiani.com>
 * Franz Willer <franz.willer@gwi-ag.com>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package org.dcm4chex.archive.ejb.session;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;

import javax.naming.Context;
import javax.naming.InitialContext;

import junit.framework.TestCase;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.FileFormat;
import org.dcm4che.dict.Tags;
import org.dcm4chex.archive.common.PrivateTags;
import org.dcm4chex.archive.ejb.interfaces.Storage;
import org.dcm4chex.archive.ejb.interfaces.StorageHome;

/**
 * @author <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 * @version $Revision: 2512 $ $Date: 2006-06-01 02:37:02 +0800 (周四, 01 6月 2006) $
 */
public class StorageBeanTest extends TestCase {

    public static final String CALLING_AET = "STORE_SCU";
    public static final String CALLED_AET = "STORE_SCP";
    public static final String RETRIEVE_AET = "QR_SCP";
    public static final String DIR = "storage";
    public static final String AET = "StorageBeanTest";
    public static final DcmObjectFactory objFact =
        DcmObjectFactory.getInstance();

    private Storage storage;
 
    public static void main(String[] args) {
        junit.textui.TestRunner.run(StorageBeanTest.class);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        Context ctx = new InitialContext();
        StorageHome home = (StorageHome) ctx.lookup(StorageHome.JNDI_NAME);
        ctx.close();
        storage = home.create();
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        storage.remove();
    }

    /**
     * Constructor for StudyBeanTest.
     * @param name
     */
    public StorageBeanTest(String name) {
        super(name);
    }

    public void testStore() throws Exception {
        doStore("", new File(DIR));
    }

    private void doStore(String path, File file) throws Exception {
        if (file.isDirectory()) {
            File[] f = file.listFiles();
            for (int i = 0; i < f.length; i++) {
                doStore(path + '/' + f[i].getName(), f[i]);
            }
            return;
        }
        MessageDigest md = MessageDigest.getInstance("MD5");
        Dataset ds = loadDataset(file, md);
        ds.setPrivateCreatorID(PrivateTags.CreatorID);
        ds.putAE(PrivateTags.CallingAET, CALLING_AET);
        ds.putAE(PrivateTags.CalledAET, CALLED_AET);
        ds.putAE(Tags.RetrieveAET, RETRIEVE_AET);
        Long assocpk = storage.initAssociation(CALLING_AET, CALLED_AET, RETRIEVE_AET);
        storage.store(assocpk, ds, "/", path.substring(1), (int) file.length(),
                md.digest());
        storage.removeAssociation(assocpk);
    }

    private Dataset loadDataset(File file, MessageDigest md)
        throws IOException {
        InputStream is = new FileInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(is);
        DigestInputStream dis = new DigestInputStream(bis, md);
        Dataset ds = objFact.newDataset();
        try {
            ds.readFile(dis, FileFormat.DICOM_FILE, -1);
        } finally {
            try {
                dis.close();
            } catch (IOException ignore) {
            }
        }
        if (ds.getFileMetaInfo() == null) {
        }
        ds.remove(Tags.PixelData);
        return ds;
    }

}
