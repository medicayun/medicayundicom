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
 * Date: 09.07.2003
 * Time: 09:50:40
 * CVS Revision: $Revision: 1559 $
 * Last CVS Commit: $Date: 2005-02-25 23:20:31 +0800 (周五, 25 2月 2005) $
 * Author of last CVS Commit: $Author: gunterze $
 */
package org.dcm4chex.archive.ejb.entity;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.cactus.ServletTestCase;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;
import org.dcm4chex.archive.common.PrivateTags;
import org.dcm4chex.archive.ejb.interfaces.InstanceLocal;
import org.dcm4chex.archive.ejb.interfaces.InstanceLocalHome;
import org.dcm4chex.archive.ejb.interfaces.PatientLocal;
import org.dcm4chex.archive.ejb.interfaces.PatientLocalHome;
import org.dcm4chex.archive.ejb.interfaces.SeriesLocal;
import org.dcm4chex.archive.ejb.interfaces.SeriesLocalHome;
import org.dcm4chex.archive.ejb.interfaces.StudyLocal;
import org.dcm4chex.archive.ejb.interfaces.StudyLocalHome;

/**
 * @author <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 *
 */
public class InstanceBeanTest extends ServletTestCase {

    public static final String AET = "MODALITY_AET";
    public static final String PID = "P-999999";
    public static final String PNAME = "Test^InstanceBean";
    public static final String SUID = "1.2.40.0.13.1.1.9999";
    public static final String sUID = "1.2.40.0.13.1.1.9999.1";
    public static final String CUID = "1.2.40.0.13.1.1.9999.2";
    public static final String UID_ = "1.2.40.0.13.1.1.9999.";
    private static final DcmObjectFactory dof = DcmObjectFactory.getInstance();

    private PatientLocalHome patHome;
    private StudyLocalHome studyHome;
    private SeriesLocalHome seriesHome;
    private InstanceLocalHome instHome;
    private Object patPk;

    public static void main(String[] args) {
        junit.textui.TestRunner.run(InstanceBeanTest.class);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        Context ctx = new InitialContext();
        patHome = (PatientLocalHome) ctx.lookup("java:comp/env/ejb/Patient");
        studyHome = (StudyLocalHome) ctx.lookup("java:comp/env/ejb/Study");
        seriesHome = (SeriesLocalHome) ctx.lookup("java:comp/env/ejb/Series");
        instHome = (InstanceLocalHome) ctx.lookup("java:comp/env/ejb/Instance");
        ctx.close();
        Dataset ds = dof.newDataset();
        ds.setPrivateCreatorID(PrivateTags.CreatorID);
        ds.putAE(PrivateTags.CallingAET, AET);
        ds.putLO(Tags.PatientID, PID);
        ds.putPN(Tags.PatientName, PNAME);
        ds.putUI(Tags.StudyInstanceUID, SUID);
        ds.putUI(Tags.SeriesInstanceUID, sUID);
        ds.putUI(Tags.SOPClassUID, CUID);
        PatientLocal pat = patHome.create(ds);
        patPk = pat.getPrimaryKey();
        StudyLocal study = studyHome.create(ds, pat);
        SeriesLocal series = seriesHome.create(ds, study);
        for (int i = 0; i < 5; ++i) {
            ds.putUI(Tags.SOPInstanceUID, UID_ + i);
            instHome.create(ds, series);
        }
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        patHome.remove(patPk);
    }

    /**
     * Constructor for StudyBeanTest.
     * @param arg0
     */
    public InstanceBeanTest(String arg0) {
        super(arg0);
    }

    public void testFindByIuid() throws Exception {
        for (int i = 0; i < 5; i++) {
            InstanceLocal inst = instHome.findBySopIuid(UID_ + i);
        }
    }
}
