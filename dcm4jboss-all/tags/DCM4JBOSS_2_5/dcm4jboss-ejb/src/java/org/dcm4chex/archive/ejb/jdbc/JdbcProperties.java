/* $Id: JdbcProperties.java 1666 2005-04-03 22:35:05Z gunterze $
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

import java.io.InputStream;
import java.util.Properties;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision: 1666 $ $Date: 2005-04-04 06:35:05 +0800 (周一, 04 4月 2005) $
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

    private static final String HSQL_VAL = "Hypersonic SQL";
    private static final String PSQL_VAL = "PostgreSQL 7.2";
    private static final String MYSQL_VAL = "mySQL";
    private static final String DB2_VAL = "DB2";
    private static final String ORACLE_VAL = "Oracle9i";
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

    public int getDatabase() {
        return database;
    }

    public String getDataSource() {
        return datasource;
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
        throw new IllegalArgumentException(
            DS_MAPPING_KEY + "=" + mapping);
    }
}
