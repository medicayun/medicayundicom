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
 * Agfa HealthCare.
 * Portions created by the Initial Developer are Copyright (C) 2006-2008
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See listed authors below.
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
package org.dcm4chex.archive.ejb.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since May 6, 2009
 */
public class QueryExternalRetrieveAETsOfSeriesCmd extends BaseReadCmd {

    private static final String SQL = "SELECT i.sop_iuid, i.ext_retr_aet "
        + "FROM instance i, series s "
        + "WHERE i.series_fk = s.pk AND s.series_iuid=?";

    public static int transactionIsolationLevel = 0;

    public QueryExternalRetrieveAETsOfSeriesCmd(String seriuid, int fetchSize)
            throws SQLException {
        super(JdbcProperties.getInstance().getDataSource(),
                transactionIsolationLevel, SQL);
        ((PreparedStatement) stmt).setString(1, seriuid);
        setFetchSize(fetchSize);
        execute();
    }

    public Collection<String[]> getRetrieveAETs() throws SQLException {
        Collection<String[]> c = new ArrayList<String[]>();
        try {
            while (next()) {
                c.add(new String[] { rs.getString(1), rs.getString(2) });
            }
        } finally {
            close();
        }
        return c;
    }
}