// $Id: Jpdbi.java 14470 2010-11-30 22:19:21Z kianusch $

package com.agfa.db.tools;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmEncodeParam;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;

public class Jpdbi {
    public final static String VERSION = "2.1.1";

    public final static String ID = "$Id: Jpdbi.java 14470 2010-11-30 22:19:21Z kianusch $";

    public final static String REVISION = "$Revision: 14470 $";

    //

    final static String[] Tables = { "PATIENT", "STUDY", "SERIES", "INSTANCE", "FILES", "FILESYSTEM" };

    //

    final static String[] Attrs = { "PAT_ATTRS", "STUDY_ATTRS", "SERIES_ATTRS", "INST_ATTRS", null };

    //

    final static int PATIENT = 0;

    final static int STUDY = 1;

    final static int SERIE = 2;

    final static int INSTANCE = 3;

    final static int PATH = 4;

    final static int FILESYSTEM = 5;

    //

    final static int QUERY_SELECT = 0;

    final static int QUERY_FROM = 1;

    final static int QUERY_JOIN = 2;

    final static int QUERY_LINKS = 3;

    final static int QUERY_WHERE = 4;

    final static int QUERY_GROUP = 5;

    final static int QUERY_ORDER = 6;

    // DB Types

    public static final int DBTYPE_UNKNOWN = 0;

    public static final int DBTYPE_ORACLE = 1;

    public static final int DBTYPE_MYSQL = 2;

    //

    static void exit(int ExitCode) {
        System.out.flush();
        System.out.close();
        System.err.flush();
        System.err.close();
        System.exit(ExitCode);
    }

    static void exit(int ExitCode, String ErrorText) {
        System.err.println(ErrorText);
        exit(ExitCode);
    }

    private static int CountCharInString(char c, String s) {
        int cnt = 0;
        for (int pos = 0; pos < s.length(); pos++)
            if (s.charAt(pos) == c)
                cnt++;
        return cnt;
    }

    public static void UpdateStudyModality(Connection connection, long pk, Config cfg) {
        String sql = "select distinct MODALITY from SERIES where STUDY_FK=" + pk;
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            String MODALITIES = "";
            String MOD = null;
            while (rs.next()) {
                MOD = rs.getString(1);
                if (MODALITIES.length() > 0)
                    MODALITIES += "\\";
                MODALITIES += MOD;
            }
            rs.close();
            sql = "update STUDY set MODS_IN_STUDY='" + MODALITIES + "' where PK=" + pk;
            if (cfg.isDebug())
                System.err.println("DEBUG: " + sql);
            else
                stmt.executeUpdate(sql);
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Update DB Field only

    private static void UpdateField(PreparedStatement stmt, Long pk, String uid, boolean debug) throws SQLException {
        if (debug)
            System.err.println("DEBUG: Update: < PK=" + pk + " >");
        else {
            int p = 1;
            if (uid != null)
                stmt.setString(p++, uid);
            stmt.setLong(p, pk);
            stmt.execute();
        }
    }

    // Update DicomBlob and DB Field or both
    static void UpdateField(ResultSet rs, PreparedStatement stmt, Long pk, String field, String[][] update, String uid,
            boolean debug) throws SQLException, IOException {

        if (debug)
            System.err.println("DEBUG: Reading " + field + "...");

        Blob bl = rs.getBlob(field);

        if (bl != null) {
            InputStream bis = bl.getBinaryStream();
            Dataset ds = DcmObjectFactory.getInstance().newDataset();
            ds.readFile(bis, null, -1);
            bis.close();

            if (debug) {
                System.err.println("<" + field + " OLD>");
                ds.dumpDataset(System.err, null);
                System.err.println("</" + field + "OLD>");
            }

            for (int loop = 0; loop < update.length; loop++) {
                String DcmField = update[loop][1];
                String DcmValue = update[loop][2];

                if (DcmField != null) {

                    int TAG = 0;
                    if (DcmField.startsWith("x", 0))
                        TAG = Integer.parseInt(DcmField.substring(1), 16);
                    else
                        TAG = Tags.forName(DcmField);
                    if (DcmValue == null) {
                        if (ds.contains(TAG))
                            ds.remove(TAG);
                        else
                            ds = null;

                    } else {
                        if (DcmValue.equals("%UID%")) {
                            ds.putXX(TAG, uid);
                        } else {
                            ds.putXX(TAG, DcmValue);
                        }
                    }
                }
            }

            if (debug) {
                System.err.println("<" + field + " NEW>");
                if (ds == null) {
                    System.err.println("No Changes to DataSet");
                } else {
                    ds.dumpDataset(System.err, null);
                }
                System.err.println("</" + field + " NEW>");
            }

            if (ds != null) {
                if (debug)
                    System.err.println("DEBUG: Update < PK=" + pk + " >");
                else {
                    int p = 1;

                    int len = ds.calcLength(DcmEncodeParam.EVR_LE);
                    ByteArrayOutputStream bos = new ByteArrayOutputStream(len);
                    ds.writeDataset(bos, DcmEncodeParam.EVR_LE);

                    if (uid != null)
                        stmt.setString(p++, uid);

                    stmt.setBinaryStream(p++, new ByteArrayInputStream(bos.toByteArray()), len);
                    stmt.setLong(p, pk);
                    stmt.execute();
                }
            }
        } else {
            System.err.println("DEBUG: Reading " + field + " failed.");
        }

    }

    private static void ParseQuery(Connection conn, Config cfg) throws SQLException, IOException {
        Statement stmt = conn.createStatement();
        ResultSet rs = null;
        boolean updUid = false;
        String uidTemplate = null;

        String[] query = cfg.getSqlPortions();

        String SQLStatement = "";

        if (cfg.isIgnoreEmpty()) {
            SQLStatement += "from " + query[QUERY_FROM] + " " + query[QUERY_JOIN] + " ";
            SQLStatement += "where " + query[QUERY_LINKS] + " " + query[QUERY_WHERE];
        } else {
            SQLStatement += "from " + query[QUERY_FROM] + " " + query[QUERY_JOIN] + " ";
            SQLStatement += "where " + query[QUERY_LINKS] + " " + query[QUERY_WHERE];
        }

        // Construct Count Statement
        String CountStatement = "select COUNT(*) CNT " + SQLStatement;
        // Construct Select Statement
        String QueryStatement = "select " + query[QUERY_SELECT] + " " + SQLStatement;

        if (cfg.isDebug()) {
            System.err.println("DEBUG: Count: < " + CountStatement + " >");
            System.err.println("DEBUG: Query: < " + QueryStatement + " >");
        }

        String[][] update = cfg.getUpdDicom();
        boolean multi = false;
        boolean UpdModality = false;
        String UpdateStatement = null;
        int UpdateLevel = -1;
        boolean DoUpdate = false;
        PreparedStatement UpdStmt = null;

        if (update != null) {
            UpdateLevel = cfg.getUpdateLevel().nextSetBit(0);

            if (update[0][4].equals("t"))
                multi = true;

            for (int loop = 0; loop < update.length; loop++) {
                if (update[loop][0] != null) {
                    String UpdValue = update[loop][2];
                    // Special Case Patient Name
                    if (update[loop][0].equals("PAT_NAME") && UpdateLevel == Jpdbi.PATIENT && UpdValue != null) {
                        int cnt = CountCharInString('^', UpdValue);
                        while (cnt++ < 4)
                            UpdValue += "^";
                        update[loop][2] = UpdValue;
                    }
                    // Special Case Modality
                    if (update[loop][0].equals("MODALITY"))
                        UpdModality = true;

                    if (UpdateStatement == null)
                        UpdateStatement = "";
                    else if (UpdateStatement.length() > 0)
                        UpdateStatement += ",";
                    UpdateStatement += update[loop][0].toUpperCase();

                    if (UpdValue.equals("%UID%") || UpdValue.startsWith("+")) {
                        UpdateStatement += "=?";
                        if (UpdValue.startsWith("+")) {
                            uidTemplate = UpdValue.substring(1);
                        } else {
                            uidTemplate = cfg.getUidBase();
                        }
                        if (cfg.isDebug()) {
                            System.err.println("DEBUG: Update Template: < " + uidTemplate + " >");
                        }
                    } else {
                        UpdateStatement += (UpdValue == null) ? "=null" : "='" + UpdValue + "'";
                    }
                }
            }

            if (!cfg.getUpdateDS().isEmpty()) {
                if (UpdateStatement == null)
                    UpdateStatement = "";
                else if (UpdateStatement.length() > 0)
                    UpdateStatement += ",";
                UpdateStatement += Jpdbi.Attrs[cfg.getUpdateDS().nextSetBit(0)];
                UpdateStatement += "=?";
            }

            UpdateStatement += " where PK=?";
            DoUpdate = true;

            if (cfg.isDebug()) {
                System.err.println("DEBUG: Update: < " + "update " + Jpdbi.Tables[UpdateLevel] + " set "
                        + UpdateStatement + " >");
            }
        }

        rs = stmt.executeQuery(CountStatement);
        rs.next();
        long rows = rs.getLong(1);
        rs.close();

        if (rows > 0) {
            if (DoUpdate) {
                if (!multi && rows != 1) {
                    Jpdbi.exit(1, "Multiple Updates not allowed on this Configuration.");
                }

                if (cfg.getUpdateCount() == -666 || rows == cfg.getUpdateCount()) {
                    UpdStmt = conn.prepareStatement("update " + Jpdbi.Tables[UpdateLevel] + " set " + UpdateStatement);
                } else {
                    Jpdbi.exit(1, "Updating [" + rows + "] rows.  Please supply correct \"--count\" option.");
                }
            }

            rs = stmt.executeQuery(QueryStatement);
            ResultSetMetaData md = rs.getMetaData();

            long PK = -1;
            long LastPK = -1;
            long cnt = 0L;

            while (rs.next()) {
                cnt++;
                for (int i = 0; i < Jpdbi.Tables.length; i++) {
                    if (cfg.isDisplayLevel(i)) {
                        switch (i) {
                        case Jpdbi.PATIENT:
                            PK = Display.Patient(rs, md, cfg);
                            break;
                        case Jpdbi.STUDY:
                            PK = Display.Study(rs, md, cfg);
                            if (PK > -1) {
                                if (UpdModality && PK != LastPK && LastPK > -1)
                                    UpdateStudyModality(conn, LastPK, cfg);
                                LastPK = PK;
                            }
                            break;
                        case Jpdbi.SERIE:
                            PK = Display.Serie(rs, md, cfg);
                            break;
                        case Jpdbi.INSTANCE:
                            PK = Display.Instance(rs, md, cfg);
                            break;
                        case Jpdbi.PATH:
                            Display.Path(rs, md, cfg);
                            break;
                        default:
                            PK = -1;
                            break;
                        }
                        if (cfg.isUpdateLevel(i) && PK > -1) {
                            String uid = null;
                            if (uidTemplate != null)
                                uid = Uid.Generate(uidTemplate, cnt);

                            if (cfg.getUpdateDS().isEmpty())
                                UpdateField(UpdStmt, PK, uid, cfg.isDebug());
                            else
                                UpdateField(rs, UpdStmt, PK, Jpdbi.Attrs[i], update, uid, cfg.isDebug());
                        }
                    }
                }
            }

            if (UpdModality && LastPK > -1)
                UpdateStudyModality(conn, LastPK, cfg);

            rs.close();
        } else {
            System.err.println("Query returns 0 rows.");
        }
        stmt.close();
    }

    public static void main(String[] argv) {
        System.setProperty("java.awt.headless", "true");

        Connection conn = null;
        final Config cfg = new Config();
        cfg.ParseCommandLine(argv);

        if (cfg.isDebug()) {
            System.err.println("DEBUG: Connect Url: < " + cfg.getJdbcUrl() + " >");
        }

        try {
            conn = DriverManager.getConnection(cfg.getJdbcUrl());
            DatabaseMetaData dmd = conn.getMetaData();
            cfg.setDbType(dmd.getDatabaseProductName());

            // setPreparedStatements(conn);

            ParseQuery(conn, cfg);

            conn.close();
        } catch (Exception e) {
            if (cfg.isDebug())
                e.printStackTrace();
            else
                Jpdbi.exit(1, e.toString());
        }
        Jpdbi.exit(0);
    }
}
