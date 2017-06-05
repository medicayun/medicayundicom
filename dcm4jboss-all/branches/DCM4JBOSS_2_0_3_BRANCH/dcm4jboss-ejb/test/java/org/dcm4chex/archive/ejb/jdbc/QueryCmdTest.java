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
 * Date: 24.07.2003
 * Time: 12:11:55
 * CVS Revision: $Revision: 1275 $
 * Last CVS Commit: $Date: 2004-12-07 02:14:03 +0800 (周二, 07 12月 2004) $
 * Author of last CVS Commit: $Author: gunterze $
 */
package org.dcm4chex.archive.ejb.jdbc;

import org.apache.cactus.ServletTestCase;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;

/**
 * @author <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 *
 */
public class QueryCmdTest extends ServletTestCase
{
    private static final DcmObjectFactory dof = DcmObjectFactory.getInstance();
    
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(QueryCmdTest.class);
    }
    
    public QueryCmdTest(String name)
    {
        super(name);
    }

    public void testPatientQuery() throws Exception
    {
        Dataset keys = dof.newDataset();
        keys.putCS(Tags.QueryRetrieveLevel, "PATIENT");
        keys.putLO(Tags.PatientID);
        keys.putPN(Tags.PatientName);
        keys.putDA(Tags.PatientBirthDate);
        keys.putCS(Tags.PatientSex, "F");
        QueryCmd cmd = QueryCmd.create(keys);
        cmd.execute();
        try {
            while (cmd.next())
                cmd.getDataset();
        }
        finally {
            cmd.close();
        }        
    }
    
    public void testStudyQuery() throws Exception
    {
        Dataset keys = dof.newDataset();
        keys.putCS(Tags.QueryRetrieveLevel, "STUDY");
        keys.putLO(Tags.PatientID);
        keys.putPN(Tags.PatientName, "*ge*");
        keys.putUI(Tags.StudyInstanceUID);
        keys.putLO(Tags.StudyDescription);
        keys.putSH(Tags.StudyID);
        keys.putDA(Tags.StudyDate, "19970811");
        keys.putTM(Tags.StudyTime);
        keys.putCS(Tags.ModalitiesInStudy, "US");
        QueryCmd cmd = QueryCmd.create(keys);
        cmd.execute();
        try {
            while (cmd.next())
                cmd.getDataset();
        }
        finally {
            cmd.close();
        }        
    }
    
    public void testSeriesQuery() throws Exception
    {
        Dataset keys = dof.newDataset();
        keys.putCS(Tags.QueryRetrieveLevel, "SERIES");
        keys.putUI(Tags.SeriesInstanceUID);
        keys.putIS(Tags.SeriesNumber);
        keys.putDA(Tags.SeriesDate);
        keys.putTM(Tags.SeriesTime);
        keys.putCS(Tags.Modality, "US");
        QueryCmd cmd = QueryCmd.create(keys);
        cmd.execute();
        try {
            while (cmd.next())
                cmd.getDataset();
        }
        finally {
            cmd.close();
        }        
    }

    
    public void testImageQuery() throws Exception
    {
        Dataset keys = dof.newDataset();
        keys.putCS(Tags.QueryRetrieveLevel, "IMAGE");
        keys.putUI(Tags.SOPInstanceUID);
        keys.putUI(Tags.SOPClassUID);
        keys.putIS(Tags.InstanceNumber, "1");
        QueryCmd cmd = QueryCmd.create(keys);
        cmd.execute();
        try {
            while (cmd.next())
                cmd.getDataset();
        }
        finally {
            cmd.close();
        }        
    }
}
