/* $Id: RetrieveCmd.java 1026 2004-03-03 18:22:28Z gunterze $
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
package org.dcm4chex.archive.ejb.jdbc;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;

import javax.sql.DataSource;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.dict.Tags;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision: 1026 $ $Date: 2004-03-04 02:22:28 +0800 (周四, 04 3月 2004) $
 * @since 26.08.2003
 */
public abstract class RetrieveCmd extends BaseCmd
{
    private static final String[] QRLEVEL =
        { "PATIENT", "STUDY", "SERIES", "IMAGE" };

    private static final String[] ENTITY =
        { "Patient", "Study", "Series", "Instance", "File" };

    private static final String[] SELECT_ATTRIBUTE =
        {
            "File.pk",
            "Patient.encodedAttributes",
            "Study.encodedAttributes",
            "Series.encodedAttributes",
            "Instance.encodedAttributes",
            "Instance.sopIuid",
            "Instance.sopCuid",
            "File.retrieveAETs",
            "File.directoryPath",
            "File.filePath",
            "File.fileTsuid",
            "File.fileMd5Field",
            "File.fileSize" };

    private static final String[] RELATIONS =
        {
            "Patient.pk",
            "Study.patient_fk",
            "Study.pk",
            "Series.study_fk",
            "Series.pk",
            "Instance.series_fk",
            "Instance.pk",
            "File.instance_fk",
             };

    public static RetrieveCmd create(DataSource ds, Dataset keys)
        throws SQLException
    {
        String qrLevel = keys.getString(Tags.QueryRetrieveLevel);
        switch (Arrays.asList(QRLEVEL).indexOf(qrLevel))
        {
            case 0 :
                return new PatientRetrieveCmd(ds, keys);
            case 1 :
                return new StudyRetrieveCmd(ds, keys);
            case 2 :
                return new SeriesRetrieveCmd(ds, keys);
            case 3 :
                return new ImageRetrieveCmd(ds, keys);
            default :
                throw new IllegalArgumentException(
                    "QueryRetrieveLevel=" + qrLevel);
        }
    }

    public static RetrieveCmd create(DataSource ds, DcmElement refSOPSeq)
        throws SQLException
    {
        return new FileRetrieveCmd(ds, refSOPSeq);
    }

    protected final SqlBuilder sqlBuilder = new SqlBuilder();

    protected RetrieveCmd(DataSource ds) throws SQLException
    {
        super(ds);
        sqlBuilder.setSelect(SELECT_ATTRIBUTE);
        sqlBuilder.setFrom(ENTITY);
        sqlBuilder.setRelations(RELATIONS);
    }

    public FileInfo[][] execute() throws SQLException
    {
        try
        {
            execute(sqlBuilder.getSql());
            LinkedHashMap map = new LinkedHashMap();
            ArrayList list;
            while (next())
            {
                FileInfo info =
                    new FileInfo(
                        rs.getInt(1),
                        rs.getBytes(2),
                        rs.getBytes(3),
                        rs.getBytes(4),
                        rs.getBytes(5),
                        rs.getString(6),
                        rs.getString(7),
                        rs.getString(8),
                        rs.getString(9),
                        rs.getString(10),
                        rs.getString(11),
                        rs.getString(12),
                        rs.getInt(13));
                list = (ArrayList) map.get(info.sopIUID);
                if (list == null)
                {
                    map.put(info.sopIUID, list = new ArrayList());
                }
                list.add(info);
            }
            FileInfo[][] result = new FileInfo[map.size()][];
            Iterator it = map.values().iterator();
            for (int i = 0; i < result.length; i++)
            {
                list = (ArrayList) it.next();
                result[i] =
                    (FileInfo[]) list.toArray(new FileInfo[list.size()]);
            }
            return result;
        } finally
        {
            close();
        }
    }

    static class PatientRetrieveCmd extends RetrieveCmd
    {
        PatientRetrieveCmd(DataSource ds, Dataset keys) throws SQLException
        {
            super(ds);
            String pid = keys.getString(Tags.PatientID);
            if (pid == null)
                throw new IllegalArgumentException("Missing PatientID");

            sqlBuilder.addWildCardMatch(
                "Patient.patientId",
                SqlBuilder.TYPE2,
                pid,
                false);
        }
    }

    static class StudyRetrieveCmd extends RetrieveCmd
    {
        StudyRetrieveCmd(DataSource ds, Dataset keys) throws SQLException
        {
            super(ds);
            String[] uid = keys.getStrings(Tags.StudyInstanceUID);
            if (uid == null || uid.length == 0)
                throw new IllegalArgumentException("Missing StudyInstanceUID");

            sqlBuilder.addListOfUidMatch(
                "Study.studyIuid",
                SqlBuilder.TYPE1,
                uid);
        }
    }

    static class SeriesRetrieveCmd extends RetrieveCmd
    {
        SeriesRetrieveCmd(DataSource ds, Dataset keys) throws SQLException
        {
            super(ds);
            String[] uid = keys.getStrings(Tags.SeriesInstanceUID);
            if (uid == null || uid.length == 0)
                throw new IllegalArgumentException("Missing SeriesInstanceUID");

            sqlBuilder.addListOfUidMatch(
                "Series.seriesIuid",
                SqlBuilder.TYPE1,
                uid);
        }
    }

    static class ImageRetrieveCmd extends RetrieveCmd
    {
        ImageRetrieveCmd(DataSource ds, Dataset keys) throws SQLException
        {
            super(ds);
            String[] uid = keys.getStrings(Tags.SOPInstanceUID);
            if (uid == null || uid.length == 0)
                throw new IllegalArgumentException("Missing SOPInstanceUID");

            sqlBuilder.addListOfUidMatch(
                "Instance.sopIuid",
                SqlBuilder.TYPE1,
                uid);
        }
    }

    static class FileRetrieveCmd extends RetrieveCmd
    {
        FileRetrieveCmd(DataSource ds, DcmElement refSOPSeq) throws SQLException
        {
            super(ds);
            String[] uid = new String[refSOPSeq.vm()];
            for (int i = 0; i < uid.length; i++) {
                uid[i] = refSOPSeq.getItem(i).getString(Tags.RefSOPInstanceUID);
            }

            sqlBuilder.addListOfUidMatch(
                "Instance.sopIuid",
                SqlBuilder.TYPE1,
                uid);
        }
    }
}
