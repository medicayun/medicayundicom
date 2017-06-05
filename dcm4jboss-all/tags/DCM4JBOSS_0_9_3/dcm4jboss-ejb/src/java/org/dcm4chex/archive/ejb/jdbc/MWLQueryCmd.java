/* $Id: MWLQueryCmd.java 1177 2004-10-07 12:17:09Z gunterze $
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

import javax.sql.DataSource;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmDecodeParam;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;
import org.dcm4cheri.util.DatasetUtils;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1177 $ $Date: 2004-10-07 20:17:09 +0800 (周四, 07 10月 2004) $
 * @since 10.02.2004
 */
public class MWLQueryCmd extends BaseCmd {

    private static final String[] FROM = { "Patient", "MWLItem"};

    private static final String[] SELECT = { "Patient.encodedAttributes",
            "MWLItem.encodedAttributes"};

    private static final String[] RELATIONS = { "Patient.pk",
    		"MWLItem.patient_fk"};

    private static final DcmObjectFactory dof = DcmObjectFactory.getInstance();

    private final SqlBuilder sqlBuilder = new SqlBuilder();

    private final Dataset keys;

    /**
     * @param ds
     * @throws SQLException
     */
    public MWLQueryCmd(DataSource ds, Dataset keys) throws SQLException {
        super(ds);
        this.keys = keys;
        // ensure keys contains (8,0005) for use as result filter
        if (!keys.contains(Tags.SpecificCharacterSet)) {
            keys.putCS(Tags.SpecificCharacterSet);
        }
        sqlBuilder.setSelect(SELECT);
        sqlBuilder.setFrom(FROM);
        sqlBuilder.setRelations(RELATIONS);
        Dataset spsItem = keys.getItem(Tags.SPSSeq);
        if (spsItem != null) {
            sqlBuilder.addSingleValueMatch("MWLItem.spsId",
                    SqlBuilder.TYPE1,
                    spsItem.getString(Tags.SPSID));
            sqlBuilder.addRangeMatch("MWLItem.spsStartDateTime",
                    SqlBuilder.TYPE1,
                    spsItem.getDateTimeRange(Tags.SPSStartDate,
                            Tags.SPSStartTime));
            sqlBuilder.addSingleValueMatch("MWLItem.modality",
                    SqlBuilder.TYPE1,
                    spsItem.getString(Tags.Modality));
            sqlBuilder.addSingleValueMatch("MWLItem.scheduledStationAET",
                    SqlBuilder.TYPE1,
                    spsItem.getString(Tags.ScheduledStationAET));
            sqlBuilder.addWildCardMatch("MWLItem.performingPhysicianName",
                    SqlBuilder.TYPE2,
                    spsItem.getString(Tags.PerformingPhysicianName),
                    false);
        }
        sqlBuilder.addSingleValueMatch("MWLItem.requestedProcedureId",
                SqlBuilder.TYPE1,
                keys.getString(Tags.RequestedProcedureID));
        sqlBuilder.addSingleValueMatch("MWLItem.accessionNumber",
                SqlBuilder.TYPE2,
                keys.getString(Tags.AccessionNumber));
        sqlBuilder.addSingleValueMatch("Patient.patientId",
                SqlBuilder.TYPE1,
                keys.getString(Tags.PatientID));
        sqlBuilder.addWildCardMatch("Patient.patientName",
                SqlBuilder.TYPE1,
                keys.getString(Tags.PatientName),
                true);
    }

    public void execute() throws SQLException {
        execute(sqlBuilder.getSql());
    }

    public Dataset getDataset() throws SQLException {
        Dataset ds = dof.newDataset();
        DatasetUtils.fromByteArray(rs.getBytes(1),
                DcmDecodeParam.EVR_LE, ds);
        DatasetUtils.fromByteArray(rs.getBytes(2),
                DcmDecodeParam.EVR_LE, ds);
        QueryCmd.adjustDataset(ds, keys);
        return ds.subSet(keys);
    }
}