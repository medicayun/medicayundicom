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

package org.dcm4chex.archive.ejb.jdbc;

import java.io.InputStream;
import java.util.Properties;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision: 3426 $ $Date: 2007-06-29 00:00:41 +0800 (周五, 29 6月 2007) $
 * @since 25.08.2003
 */
public class JdbcProperties extends Properties {

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 1L;
    public static final int HSQL = 0;
    public static final int PSQL = 1;
    public static final int MYSQL = 2;
    public static final int DB2 = 3;
    public static final int ORACLE = 4;
    public static final int MSSQL = 5;

    private static final String HSQL_VAL = "Hypersonic SQL";
    private static final String PSQL_VAL = "PostgreSQL 8.0";
    private static final String MYSQL_VAL = "mySQL";
    private static final String DB2_VAL = "DB2";
    private static final String ORACLE_VAL = "Oracle9i";
    private static final String MSSQL_VAL = "MS SQLSERVER2000";
    private static final String DATASOURCE_KEY = "datasource";
    private static final String DS_MAPPING_KEY = "datasource-mapping";
    private static final JdbcProperties instance = new JdbcProperties();

    private final String datasource;
    private final int database;

    public static JdbcProperties getInstance() {
        return instance;
    }

    public String[] getProperties(String[] keys) {
        String[] values = new String[keys.length];
        for (int i = 0; i < keys.length; i++)
            values[i] = getProperty(keys[i]);
        return values;
    }

    public String getProperty(String key) {
        if (key == null || Character.isLowerCase(key.charAt(0)))
                return key;
        String value = super.getProperty(key);
        if (value == null)
            throw new IllegalArgumentException("key: " + key);
        return value;
    }

    public final int getDatabase() {
        return database;
    }

    public final String getDataSource() {
        return datasource;
    }

    public final String getEscape() {
        return database == MYSQL || database == PSQL ? "" : " ESCAPE '\\'";
    }
    
    private JdbcProperties() {
        try {
            InputStream in =
                JdbcProperties.class.getResourceAsStream("Jdbc.properties");
            load(in);
            in.close();
            database = toDatabase(super.getProperty(DS_MAPPING_KEY));
            datasource = super.getProperty(DATASOURCE_KEY);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load jdbc properties", e);
        }
    }

    private static int toDatabase(String mapping) {
        if (HSQL_VAL.equals(mapping)) {
            return HSQL;
        }
        if (PSQL_VAL.equals(mapping)) {
            return PSQL;
        }
        if (MYSQL_VAL.equals(mapping)) {
            return MYSQL;
        }
        if (DB2_VAL.equals(mapping)) {
            return DB2;
        }
        if (ORACLE_VAL.equals(mapping)) {
            return ORACLE;
        }
        if (MSSQL_VAL.equals(mapping)) {
            return MSSQL;
        }
        throw new IllegalArgumentException(
            DS_MAPPING_KEY + "=" + mapping);
    }
}
