/*
$Id: Jpdbi.java 13101 2010-04-08 01:18:05Z kianusch $
 */

package com.agfa.db.tools;

/* GNU.GetOpt */
import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.URL;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import oracle.jdbc.driver.UpdatableResultSet;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmEncodeParam;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.DcmParser;
import org.dcm4che.data.DcmParserFactory;
import org.dcm4che.dict.DictionaryFactory;
import org.dcm4che.dict.TagDictionary;
import org.dcm4che.dict.Tags;
import org.dcm4che.srom.SOPInstanceRef;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import org.dcm4che.util.Base64;
import org.xml.sax.InputSource;

// import sun.util.calendar.BaseCalendar.Date;

public class Jpdbi {
    public static final int OPT_THISOPTIONDOESNOTEXIST = 1;

    public static final int OPT_DEBUG = 2;

    public static final int OPT_UPDATEPW = 4;

    public static final int OPT_SID = 5;

    public static final int OPT_PATH = 10;

    public static final int OPT_PATID = 11;

    public static final int OPT_SOP = 12;

    public static final int OPT_DATASET = 13;

    public static final int OPT_PATDS = 14;

    public static final int OPT_SERDS = 15;

    public static final int OPT_STYDS = 16;

    public static final int OPT_UPDATE = 17;

    public static final int OPT_UPDATE_CNT = 18;

    public static final int OPT_PATIENTLEVEL = 19;

    public static final int OPT_ISSUER = 20;

    public static final int OPT_PKS = 21;

    public static final int OPT_SERIESLEVEL = 22;

    public static final int OPT_STUDYLEVEL = 23;

    public static final int OPT_SOPDS = 24;

    public static final int OPT_RETR_AET = 25;

    public static final int OPT_FIELDS = 26;

    public static final int OPT_MWLLEVEL = 27;

    public static final int OPT_MWLDS = 28;

    public static final int OPT_MPPSLEVEL = 29;

    public static final int OPT_MPPSDS = 30;

    public static final int OPT_AUDITREP = 31;

    public static final int OPT_PRE214 = 32;

    public static final int OPT_DBALIAS = 33;

    public static final int OPT_DS_ONLY = 34;

    public static final int OPT_XLS = 35;

    // Mode

    public static final int MODE_DISPLAY = 1;

    public static final int MODE_UPDATE = 2;

    public static final int MODE_PASSWORD = 3;

    public static final int MODE_AUDITREP = 4;

    private static final DcmParserFactory pfact = DcmParserFactory.getInstance();

    private URL xslt = null;

    private LinkedList xsltParams = new LinkedList();

    private boolean xsltInc = false;

    private OutputStream out = System.out;

    private int[] excludeTags = {};

    private int excludeValueLengthLimit = Integer.MAX_VALUE;

    private File baseDir;

    private static String newline = System.getProperty("line.separator");

    private static boolean debug = false;

    // private static boolean force=false;

    private static long lastPat = -1L;

    private static long lastSty = -1L;

    private static long lastSeries = -1L;

    private static long lastSOP = -1L;

    private static String jdbcUrl = null;

    private static String series = null;

    private static String study = null;

    private static String stydate = null;

    private static String modality = null;

    private static String issuer = null;

    private static String patid = null;

    private static String anyid = null;

    private static boolean displaypath = false;

    private static boolean instance_level = false;

    private static boolean display_pks = false;

    private static boolean display_retr_aet = false;

    private static boolean display_fields = false;

    private static boolean patds = false;

    private static boolean mwlds = false;

    private static boolean styds = false;

    private static boolean serds = false;

    private static boolean dsonly = false;

    private static String lastname = null;

    private static String firstname = null;

    private static String birthdate = null;

    private static String upd = null;

    private static String upd_level = null;

    private static String queryCMD = null;

    private static long upd_cnt = 1;

    private static boolean upd_mod = false;

    private static boolean upd_name = false;

    private static boolean upd_multi = true;

    private static boolean patient_level = false;

    private static boolean study_level = false;

    private static boolean series_level = false;

    private static boolean mwl_level = false;

    private static boolean mpps_level = false;

    private static boolean mppsds = false;

    private static boolean sopds = false;

    private static boolean ThisOptionDoesNotExist = false;

    private static boolean upd_pw = false;

    private static boolean pre214 = false;

    private static String[] upd_dicom;

    private static String upd_password_sql;

    private static boolean auditrep = false;

    private static String auditid = null;

    private static String xls = null;

    private static SimpleDateFormat bdf = new SimpleDateFormat("yyyy-MM-dd");

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public final void setExcludeValueLengthLimit(int excludeValueLengthLimit) {
        this.excludeValueLengthLimit = excludeValueLengthLimit;
    }

    public final void setBaseDir(File baseDir) {
        this.baseDir = baseDir;
    }

    private TagDictionary dict = DictionaryFactory.getInstance().getDefaultTagDictionary();

    public Jpdbi(int Mode) {
        switch (Mode) {
        case MODE_DISPLAY:
            display();
            break;
        case MODE_AUDITREP:
            displayauditrep();
            break;
        case MODE_UPDATE:
            update();
            break;
        case MODE_PASSWORD:
            updatepassword();
            break;
        default:
            break;
        }
    }

    public Query makeWhere(String level) {
        String where = "";
        String from = "";
        String links = "";

        boolean patient_link = false;
        boolean study_link = false;
        boolean series_link = false;
        /*
         * boolean instance_link = false; boolean file_link = false; boolean
         * mwl_link = false;
         */
        if (mwl_level) {
            study_level = false;
            series_level = false;
            instance_level = false;
            displaypath = false;
            mpps_level = false;
        }

        if (mpps_level) {
            study_level = false;
            series_level = false;
            instance_level = false;
            displaypath = false;
            mwl_level = false;
        }

        Query query = new Query(null, null, null, null);

        if (level != null) {
            if (level.equalsIgnoreCase("ALL")) {
                patient_level = true;
                study_level = true;
                series_level = true;
                instance_level = true;
            }
            if (level.equalsIgnoreCase("PATIENT"))
                patient_level = true;
            if (level.equalsIgnoreCase("STUDY"))
                study_level = true;
            if (level.equalsIgnoreCase("SERIES"))
                series_level = true;
            if (level.equalsIgnoreCase("INSTANCE"))
                instance_level = true;
        }

        if (lastname != null || firstname != null) {
            if (lastname == null)
                lastname = "%";
            if (firstname == null)
                firstname = "%";

            where = where.concat(whereClause(where, "upper(pat_name)", "upper('" + lastname + "^" + firstname
                    + "^%^%')", false));
            patient_link = true;
        }

        if (birthdate != null) {
            if (pre214 == true)
                where = where.concat(whereClause(where, "PAT_BIRTHDATE", "{d'" + birthdate + "'}", false));
            else
                where = where.concat(whereClause(where, "PAT_BIRTHDATE", birthdate, true));
            patient_link = true;
        }

        if (issuer != null) {
            where = where.concat(whereClause(where, "PAT_ID_ISSUER", issuer, true));
            patient_link = true;
        }

        if (patid != null) {
            String issuer = null;
            int split = patid.indexOf(':');
            if (split == -1) {
                issuer = null;
            } else {
                issuer = patid.substring(0, split);
                patid = patid.substring(split + 1);
            }

            /*
             * System.out.print(patid+"\n"); System.out.print(issuer+"\n");
             */

            if (patid.length() > 0)
                where = where.concat(whereClause(where, "PAT_ID", patid, true));

            where = where.concat(whereClause(where, "PAT_ID_ISSUER", issuer, true));
            patient_link = true;
        }

        if (study != null) {
            where = where.concat(whereClause(where, "STUDY.STUDY_IUID", study, true));
            study_link = true;
        }

        if (stydate != null) {
            if (where != null && where != "")
                where = where.concat(" and ");
            long dummy = Long.parseLong(stydate);
            if (dummy <= 999) {
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DATE, (int) -dummy);
                String mydate = "" + cal.get(Calendar.YEAR) + "-" + (cal.get(Calendar.MONTH) + 1) + "-"
                        + (cal.get(Calendar.DATE));
                where = where + " STUDY_DATETIME" + ">= {ts'" + mydate + " 00:00:00'}";
            } else {
                where = where + " (STUDY_DATETIME" + ">= {ts'" + stydate + " 00:00:00'} and";
                where = where + " STUDY_DATETIME" + "<= {ts'" + stydate + " 23:59:59'})";
            }
            // series_level = true;
            study_link = true;
        }

        if (series != null) {
            where = where.concat(whereClause(where, "SERIES_IUID", series, true));
            series_link = true;
        }

        if (modality != null) {
            /*
             * where = where.concat(whereClause(where, "upper(MODS_IN_STUDY)",
             * "upper('%" + modality + "%')", false));
             */
            where = where.concat(whereClause(where, "upper(MODALITY)", "upper('" + modality + "')", false));
            series_link = true;
        }

        if (queryCMD != null) {
            String queryLevel = null;
            String queryField = null;
            String queryValue = null;

            int split = queryCMD.indexOf('=');

            if (split == -1) {
                queryCMD = null;
            } else {
                queryValue = queryCMD.substring(split + 1);
                queryCMD = queryCMD.substring(0, split);
                split = queryCMD.indexOf('.');
                if (split > -1) {
                    queryLevel = queryCMD.substring(0, split);
                    queryField = queryCMD.substring(split + 1);

                    if (queryLevel.compareToIgnoreCase("study") == 0)
                        study_level = true;
                    if (queryLevel.compareToIgnoreCase("series") == 0)
                        series_level = true;
                    if (queryLevel.compareToIgnoreCase("instance") == 0)
                        instance_level = true;
                    if (queryValue.indexOf('%') > -1 || queryValue.indexOf('_') > -1) {
                        where = where.concat(whereClause(where, queryLevel + "." + queryField, queryValue, true));
                    } else {                        
                        if (where != null && where != "")
                            where = where.concat(" and ");
                        where = where.concat(queryLevel + "." + queryField + " like '" + queryValue + "' ");
                    }
                }
            }
        }

        if (anyid != null) {
            String operator = null;
            if (where != null && where != "")
                where = where.concat(" and ");

            if (anyid.indexOf('%') != -1)
                operator = " like ";
            else
                operator = " = ";

            where = where.concat("( STUDY.STUDY_IUID " + operator + "'" + anyid + "' or SERIES_IUID" + operator + "'"
                    + anyid + "') ");
            series_link = true;
        }

        if (mppsds) {
            mpps_level = true;
        } else if (mwlds) {
            mwl_level = true;
        } else {
            if (sopds)
                instance_level = true;
            if (serds)
                series_level = true;
            if (styds)
                study_level = true;
            if (patds)
                patient_level = true;
        }

        if (displaypath) {
            from = "FILES,FILESYSTEM,";
            links = links + "FILESYSTEM.PK=FILESYSTEM_FK and INSTANCE.PK=INSTANCE_FK and ";
            instance_level = true;
        }
        if (instance_level) {
            from = from + "INSTANCE,";
            links = links + "SERIES.PK=SERIES_FK and ";
            series_level = true;
        }
        if (series_level || series_link) {
            from = from + "SERIES,";
            links = links + "STUDY.PK=STUDY_FK and ";
            study_link = true;
            if (series_level)
                study_level = true;
        }
        if (study_level || study_link) {
            from = from + "STUDY,";
            links = links + "PATIENT.PK=STUDY.PATIENT_FK";
            patient_link = true;
            if (study_level)
                patient_level = true;
        }
        if (mwl_level) {
            from = from + "MWL_ITEM,";
            if (study_level || study_link)
                links = links + " and ";
            links = links + "PATIENT.PK=MWL_ITEM.PATIENT_FK";
            patient_level = true;
        }
        if (mpps_level) {
            from = from + "MPPS,";
            if (study_level || study_link)
                links = links + " and ";
            links = links + "PATIENT.PK=MPPS.PATIENT_FK";
            patient_level = true;
            if (!(series_link || series_level)) {
                from = from + "SERIES,";
            }
            links = links + " and MPPS.PK=SERIES.MPPS_FK(+)";
            series_level = true;
        }
        if (patient_level || patient_link) {
            from = from + "PATIENT";
        }

        query.from = from;
        query.links = links;
        query.where = where;

        /*
         * if (level != null) { boolean error = false; if (patient_table &&
         * !level.equalsIgnoreCase("PATIENT")) error = true; if (study_table &&
         * !level.equalsIgnoreCase("STUDY")) error = true; if (series_table &&
         * !level.equalsIgnoreCase("SERIES")) error = true; if (instance_table &&
         * !level.equalsIgnoreCase("INSTANCE")) error = true; if (error) return
         * null; }
         */
        return query;
    }

    public void update_modality(Connection connection, long fk) {

        String sql = "select distinct MODALITY from " + upd_level + " where STUDY_FK=" + fk;
        Statement statement;
        try {
            statement = connection.createStatement();
            ResultSet rs1 = statement.executeQuery(sql);
            String MODALITIES = "";
            String MOD = null;
            while (rs1.next()) {
                MOD = rs1.getString(1);
                if (MODALITIES.length() > 0) {
                    MODALITIES = MODALITIES + "\\";
                }
                MODALITIES = MODALITIES + MOD;
            }
            sql = "update STUDY set MODS_IN_STUDY='" + MODALITIES + "' where PK=" + fk;
            if (debug)
                System.out.println(sql);
            else
                statement.executeUpdate(sql);
            statement.close();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void displayauditrep() {
        try {
            Connection connection = DriverManager.getConnection(jdbcUrl);
            Statement statement = connection.createStatement();
            String sql = "select XMLDATA from audit_record where PK='" + auditid + "'";
            if (debug)
                System.out.println(sql);

            ResultSet rs = statement.executeQuery(sql);

            while (rs.next()) {
                // displayDataSet(rs, "XMLDATA");
                displayXMLDATA(rs);
            }

            System.out.print(newline);
            connection.close();
            System.out.flush();
            System.out.close();

            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updatepassword() {
        try {
            Connection connection = DriverManager.getConnection(jdbcUrl);
            Statement statement = connection.createStatement();
            String sql = upd_password_sql;
            if (debug)
                System.out.println(sql);
            statement.executeUpdate(sql);
            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void update() {
        Query query = makeWhere(upd_level);

        if (query.where != null && query.where != "") {

            try {
                Connection connection = DriverManager.getConnection(jdbcUrl);
                Statement statement = connection.createStatement();

                String sql = null;
                String select = null;
                String attr = null;
                String fk_field = null;

                if (upd != null) {
                    if (upd_level.equalsIgnoreCase("ALL")) {
                        attr = "PAT_ATTRS,STUDY_ATTRS,SERIES_ATTRS,INST_ATTRS";
                        fk_field = "SERIES.PK=SERIES_FK and STUDY.PK=STUDY_FK and PATIENT.PK=PATIENT_FK";
                    }
                    if (upd_level.equalsIgnoreCase("PATIENT")) {
                        attr = "PAT_ATTRS";
                        fk_field = "";
                    }
                    if (upd_level.equalsIgnoreCase("STUDY")) {
                        attr = "STUDY_ATTRS";
                        fk_field = "PATIENT_FK FK,";
                    }
                    if (upd_level.equalsIgnoreCase("SERIES")) {
                        attr = "SERIES_ATTRS";
                        fk_field = "STUDY_FK FK,";
                    }
                    if (upd_level.equalsIgnoreCase("INSTANCE")) {
                        attr = "INST_ATTRS";
                        fk_field = "SERIES_FK FK,";
                    }

                    // sql = "select count(*) from " + upd_level + " where " +
                    // where;

                    sql = " from " + query.from + " where ";
                    if (query.links != null && query.links != "")
                        sql = sql + query.links;
                    if (query.links != null && query.links != "" && query.where != null && query.where != "")
                        sql = sql + " and ";
                    if (query.where != null && query.where != "")
                        sql = sql + query.where;

                    select = "select count(*) " + sql;

                    if (debug)
                        System.out.println(select);
                    ResultSet rs = statement.executeQuery(select);
                    rs.next();
                    long last_fk = -1;
                    long rowsRead = 0;
                    long rows = rs.getLong(1);
                    if (debug)
                        System.err.println("Res: " + rows);
                    if (rows > 0 && (ThisOptionDoesNotExist == true || rows == upd_cnt) && (upd_multi || rows == 1)) {
                        if (upd_level.equalsIgnoreCase("ALL")) {
                            select = "select PATIENT.PK PAT,STUDY.PK STY,SERIES.PK SER, INSTANCE.PK INST, PAT_ATTRS, STUDY_ATTRS, SERIES_ATTRS, INST_ATTRS "
                                    + attr + sql;
                            if (debug)
                                System.err.println(select);
                            rs = statement.executeQuery(select);
                            while (rs.next()) {
                                rowsRead++;
                                long pk = rs.getLong("INST");
                                updateDataSet(connection, rs, pk, "INST_ATTRS", upd_dicom, upd, "INSTANCE");
                                pk = rs.getLong("SER");
                                updateDataSet(connection, rs, pk, "SERIES_ATTRS", upd_dicom, upd, "SERIES");
                                pk = rs.getLong("STY");
                                updateDataSet(connection, rs, pk, "STUDY_ATTRS", upd_dicom, upd, "STUDY");
                                pk = rs.getLong("PAT");
                                updateDataSet(connection, rs, pk, "PAT_ATTRS", upd_dicom, upd, "PATIENT");
                            }
                        } else {
                            select = "select " + upd_level + ".PK P," + fk_field + attr + sql;
                            if (debug)
                                System.err.println(select);
                            rs = statement.executeQuery(select);
                            while (rs.next()) {
                                rowsRead++;
                                long pk = rs.getLong("P");
                                updateDataSet(connection, rs, pk, attr, upd_dicom, upd, upd_level);

                                if (upd_mod) {
                                    long fk = rs.getLong("FK");
                                    if (last_fk == -1)
                                        last_fk = fk;
                                    if (fk != last_fk) {
                                        update_modality(connection, last_fk);
                                    } else if (rowsRead == rows) {
                                        update_modality(connection, fk);
                                    }
                                    last_fk = fk;
                                }
                            }
                        }
                    } else {
                        if (upd_multi)
                            System.err.println("Update Count Mismatch. [" + rows + "]");
                        else
                            System.err.println("Multiple Updates not allowed on this Field.");
                        System.exit(1);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("Where clause empty.");
            System.exit(1);
        }
    }

    public void display() {
        if (patient_level == false) {
            patient_level = true;
            study_level = true;
        }
        Query query = makeWhere(null);
        if (query.where != null && query.where != "") {
            try {
                Connection connection = DriverManager.getConnection(jdbcUrl);
                Statement statement = connection.createStatement();

                String select = null;
                String sql = null;
                String order = null;

                // Update DataSet

                // if (patient_level) {
                select = "PATIENT.PK A, PAT_NAME, PAT_SEX, PAT_BIRTHDATE BD, PAT_ID, PAT_ID_ISSUER";
                if (patds) {
                    select = select + ", PAT_ATTRS";
                }
                order = "PAT_NAME, A";
                if (mpps_level) {
                    select = select
                            + ", MPPS.PK P, MPPS_IUID, MPPS.STATION_AET MPPSSTAAET, MPPS.MODALITY MPPSMOD, MPPS.ACCESSION_NO, MPPS_STATUS";
                    if (mppsds) {
                        select = select + ", MPPS_ATTRS";
                    }
                }
                if (mwl_level) {
                    select = select
                            + ", MWL_ITEM.PK M, SPS_ID, MWL_ITEM.START_DATETIME SD, STATION_AET, MWL_ITEM.MODALITY MWLMOD, MWL_ITEM.ACCESSION_NO, MWL_ITEM.STUDY_IUID MWLSTYIUID";
                    if (mwlds) {
                        select = select + ", ITEM_ATTRS";
                    }
                }
                if (study_level) {
                    select = select
                            + ", STUDY.PK B"
                            + ", STUDY.STUDY_IUID STYSTYIUID, STUDY_DATETIME SD, MODS_IN_STUDY, num_series, study.num_instances STYNI, study.availability STYA";
                    if (display_retr_aet) {
                        select = select + ", STUDY.EXT_RETR_AET STYEXTRETAET, STUDY.RETRIEVE_AETS STYRETAET";
                    }
                    if (styds) {
                        select = select + ", STUDY_ATTRS";
                    }
                    order = order + ", B";
                }
                if (series_level) {
                    select = select
                            + ", SERIES.PK C"
                            + ", SERIES_IUID, SERIES.MODALITY MODALITY, SERIES.NUM_INSTANCES SERNI, SERIES.AVAILABILITY SERA, series_status";
                    if (display_retr_aet) {
                        select = select
                                + ", SERIES.EXT_RETR_AET SEREXTRETAET, SERIES.RETRIEVE_AETS SERRETAET, SERIES.SRC_AET SRCAET";
                    }
                    if (serds) {
                        select = select + ", SERIES_ATTRS";
                    }
                    order = order + ", C";
                }
                if (instance_level) {
                    select = select + ", instance.pk D"
                            + ", sop_iuid, inst_no Z, instance.availability INSTA, inst_status";
                    if (display_retr_aet) {
                        select = select + ", INSTANCE.EXT_RETR_AET SOPEXTRETAET, INSTANCE.RETRIEVE_AETS SOPRETAET";
                    }
                    if (sopds) {
                        select = select + ", INST_ATTRS";
                    }
                    order = order + ", D";
                }
                if (displaypath) {
                    select = select + ", files.pk E";
                    select = select + ", DIRPATH, FILEPATH, FILE_SIZE, filesystem.availability FSA";
                    order = order + ", E";
                }

                sql = "select " + select;
                sql = sql + " from " + query.from;
                sql = sql + " where ";
                if (query.links != null && query.links != "")
                    sql = sql + "(" + query.links + ")";
                if (query.links != null && query.links != "" && query.where != null && query.where != "")
                    sql = sql + " and ";
                if (query.where != null && query.where != "")
                    sql = sql + query.where;
                sql = sql + " order by " + order;

                if (debug)
                    System.err.println(sql);

                ResultSet rs = statement.executeQuery(sql);

                while (rs.next()) {
                    displayPatient(rs);
                    if (mpps_level) {
                        displayMPPS(rs);
                        displaySeries(rs);
                    } else if (mwl_level)
                        displayMWL(rs);
                    else {
                        if (study_level)
                            displayStudy(rs);
                        if (series_level)
                            displaySeries(rs);
                        if (instance_level)
                            displaySOP(rs);
                        if (displaypath)
                            displayPath(rs);
                    }
                }

                System.out.print(newline);
                connection.close();
                System.out.flush();
                System.out.close();

                connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("No filter criteria given...");
            System.err.println("Use at least % if you know what you are doing,");
            System.err.println("or use --help for help.");
            System.exit(1);
        }
    }

    private static void displayHelp() {
        System.out.println("Usage: java -jar jpdbi [options] [<Lastname> [<Firstname> [<Birthdate>]]]");
        System.out.println("       java -jar jpdbi -v");
        System.out.print(newline);
        System.out.println("        -v                display version information");
        System.out.print(newline);
        System.out.println("        -c config         Use specified config properties");
        System.out.print(newline);
        System.out.println("        --db alias        DB Alias to connect to");
        System.out.println("        --url|-U jdbcURL  DB URL to connect");
        System.out.println("        -a host           DB host to connect to (localhost)");
        System.out.println("        -u username       username to connect to DB");
        System.out.println("        -p password       password to connect to DB");
        System.out.println("        --sid sid         DB SID");
        System.out.println("        --port port       DB port");

        System.out.print(newline);
        System.out.println("        --display-audit-rep pk");
        System.out.print(newline);
        System.out.println("        -s StudyUID             Search for Dicom Study UID");
        System.out.println("        -S SeriesUID            Search for Dicom Series UID");
        System.out.println("        -i UID                  Search for Dicom Study or Series UID");
        System.out.println("        -m Modality             Search for Modality");
        System.out.println("        --patid [ISSUER:]PATID  Search for (ISSUER and) PATID");

        System.out.print(newline);
        System.out.println("        -d YYYYMMDD             Search for Study Date");
        System.out.println("        -d <0-999>              Search last N days");

        System.out.print(newline);
        System.out.println("        --pks             Display PrimaryKeys");
        System.out.println("        --retr-aet        Display Retreive AETs");
        System.out.println("        --dataset         Display all DCM DataSet");
        System.out.println("        --patient-ds      Display DataSet on Patient Level");
        System.out.println("        --study-ds        Display DataSet on Study Level");
        System.out.println("        --series-ds       Display DataSet on Series Level");
        System.out.println("        --sop-ds          Display DataSet on SOP Level");
        System.out.println("        --mpps-ds         Display DataSet on MPPS Level");
        System.out.println("        --ds-only         Display DataSet only (no other output)");
        System.out.println("        --xls <xls-file>  Render Blob via XLS");
        System.out.println("        --patient-level   Display Patient Level only");
        System.out.println("        --study-level     Display up to Study Level (default)");
        System.out.println("        --series-level    Display up to Series Level");
        System.out.println("        --sop             Display SOP UID");
        System.out.println("        --path            Display Path");
        System.out.print(newline);
        System.out.println("        --query <table.field=value>     Query non Standard Field");
        System.out.println("        --updatepw username password    Update Password for IMPAX User");

        /*
         * System.out.print(newline); System.out.println(" --force Display even
         * if result would return more that 500 entries");
         */

        System.out.print(newline);
        System.out.println("        --debug           Display SQL Statement");

        System.out.print(newline);
        System.out.println("    Use % as wildcard statement");

        System.out.print(newline);
    }

    private void displayPatient(ResultSet result) throws SQLException {
        long tmpKey = result.getLong("A");

        if (lastPat != tmpKey) {
            lastPat = tmpKey;
            if (dsonly != true) {
                if (display_fields)
                    System.out.print("NAME:");
                System.out.print(result.getString("PAT_NAME").replace('^', ' ').trim() + " ");

                if (display_fields)
                    System.out.print("SEX:");
                System.out.print(result.getString("PAT_SEX") + " ");

                if (display_fields)
                    System.out.print("BIRTHDATE:");
                if (pre214) {
                    Timestamp tmpBD = result.getTimestamp("BD");
                    if (tmpBD != null) {
                        System.out.print(bdf.format(tmpBD) + " ");
                    } else {
                        System.out.print("- ");
                    }
                } else {
                    String tmpBD = result.getString("BD");
                    if (tmpBD != null && tmpBD.length() == 8) {
                        System.out.print(tmpBD.substring(0, 4) + "-" + tmpBD.substring(4, 6) + "-" + tmpBD.substring(6)
                                + " ");
                    } else {
                        System.out.print("- ");
                    }
                }

                if (display_fields)
                    System.out.print("ISSUER/ID:");
                System.out.print("<");
                System.out.print(result.getString("PAT_ID_ISSUER"));
                System.out.print(":");
                System.out.print(result.getString("PAT_ID"));
                System.out.print(">");

                if (display_pks) {
                    System.out.print(" ");
                    if (display_fields)
                        System.out.print("PK:");
                    System.out.print("[" + tmpKey + "]");
                }
                System.out.print(newline);
            }
            if (patds)
                try {
                    displayDataSet(result, "PAT_ATTRS");
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

        }
    }

    private void displayMWL(ResultSet result) throws SQLException {
        long tmpKey = result.getLong("M");

        if (lastSty != tmpKey) {
            lastSty = tmpKey;
            System.out.print(" ");
            if (display_fields)
                System.out.print("SPSID:");
            System.out.print(result.getString("SPS_ID") + " ");
            if (display_fields)
                System.out.print("STY-IUID:");
            System.out.print(result.getString("MWLSTYIUID") + " ");
            if (display_fields)
                System.out.print("STARTDATE/TIME:");
            Timestamp tmpSD = result.getTimestamp("SD");
            if (tmpSD != null) {
                System.out.print(sdf.format(result.getTimestamp("SD")) + " ");
            } else {
                System.out.print("- ");
            }
            if (display_fields)
                System.out.print("ST-AET:");
            System.out.print(result.getString("STATION_AET") + " ");
            if (display_fields)
                System.out.print("MOD:");
            System.out.print(result.getString("MWLMOD") + " ");
            if (display_fields)
                System.out.print("ACC#:");
            System.out.print(result.getString("ACCESSION_NO"));
            if (display_pks) {
                System.out.print(" ");
                if (display_fields)
                    System.out.print("PK:");
                System.out.print("[" + tmpKey + "]");
            }
            System.out.print(newline);

            if (mwlds)
                try {
                    displayDataSet(result, "ITEM_ATTRS");
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
        }
    }

    private static String blob2String(Blob blob) throws Exception {

        StringBuffer sb = new StringBuffer();
        int getLen = -1;
        int readLen = 1024;
        byte[] tmpbyte = new byte[readLen];

        InputStream inputStream = blob.getBinaryStream();
        while ((getLen = inputStream.read(tmpbyte, 0, readLen)) > 0) {
            sb.append(new String(tmpbyte, 0, getLen));
        }

        return sb.toString();

    }// End method: blob2String(Blob)

    private void displayMPPS(ResultSet result) throws SQLException {
        long tmpKey = result.getLong("P");

        if (lastSty != tmpKey) {
            lastSty = tmpKey;
            System.out.print(" ");
            if (display_fields)
                System.out.print("MPPSIUID:");
            System.out.print(result.getString("MPPS_IUID") + " ");
            if (display_fields)
                System.out.print("ST-AET:");
            System.out.print(result.getString("MPPSSTAAET") + " ");
            if (display_fields)
                System.out.print("MOD:");
            System.out.print(result.getString("MPPSMOD") + " ");
            if (display_fields)
                System.out.print("ACC#:");
            System.out.print(result.getString("ACCESSION_NO"));
            if (display_pks) {
                System.out.print(" ");
                if (display_fields)
                    System.out.print("PK:");
                System.out.print("[" + tmpKey + "]");
            }
            System.out.print(newline);

            if (mppsds)
                try {
                    displayDataSet(result, "MPPS_ATTRS");
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
        }
    }

    private void displayStudy(ResultSet result) throws SQLException {
        long tmpKey = result.getLong("B");

        if (lastSty != tmpKey) {
            lastSty = tmpKey;
            if (dsonly != true) {
                System.out.print(" ");
                if (display_fields)
                    System.out.print("IUID:");
                System.out.print(result.getString("STYSTYIUID") + " ");
                if (display_fields)
                    System.out.print("DATE:");
                Timestamp tmpSD = result.getTimestamp("SD");
                if (tmpSD != null) {
                    System.out.print(sdf.format(result.getTimestamp("SD")) + " ");
                } else {
                    System.out.print("- ");
                }
                if (display_fields)
                    System.out.print("MODS:");
                String tmpMOD = result.getString("MODS_IN_STUDY");
                if (tmpMOD != null) {
                    System.out.print("(" + tmpMOD.replace('\\', ',') + ") ");
                } else {
                    System.out.print("( - ) ");
                }

                if (display_fields)
                    System.out.print("#SER:");
                System.out.print(result.getLong("NUM_SERIES") + " ");
                if (display_fields)
                    System.out.print("#INST:");
                System.out.print(result.getLong("STYNI") + " ");

                if (display_fields)
                    System.out.print("AVAIL:");
                System.out.print(result.getLong("STYA"));

                if (display_retr_aet) {
                    System.out.print(" ");
                    if (display_fields)
                        System.out.print("RETAET:");
                    String tmpAET = null;
                    String tmpAET1 = result.getString("STYRETAET");
                    String tmpAET2 = result.getString("STYEXTRETAET");
                    tmpAET = tmpAET1;
                    if (tmpAET2 != null) {
                        if (tmpAET != null) {
                            tmpAET = tmpAET + "," + tmpAET2;
                        } else {
                            tmpAET = tmpAET2;
                        }
                    }
                    System.out.print("{" + tmpAET + "}");
                }
                if (display_pks) {
                    System.out.print(" ");
                    if (display_fields)
                        System.out.print("PK:");
                    System.out.print("[" + tmpKey + "]");
                }
                System.out.print(newline);
            }
            if (styds)
                try {
                    displayDataSet(result, "STUDY_ATTRS");
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

        }
    }

    private void displaySeries(ResultSet result) throws SQLException {
        String tmpIUID = result.getString("SERIES_IUID");
        long tmpKey = result.getLong("C");

        if (tmpIUID == null) {
            lastSeries = -1;
        } else {
            if (lastSeries != tmpKey) {
                lastSeries = tmpKey;
                if (dsonly != true) {

                    System.out.print("  ");
                    if (display_fields)
                        System.out.print("IUID:");
                    System.out.print(tmpIUID + " ");
                    // System.out.print(result.getString("SD") + " ");
                    if (display_fields)
                        System.out.print("MOD:");
                    System.out.print(result.getString("MODALITY") + " ");
                    if (display_fields)
                        System.out.print("#INST:");
                    System.out.print(result.getLong("SERNI") + " ");
                    if (display_fields)
                        System.out.print("AVAIL:");
                    System.out.print(result.getLong("SERA") + " ");
                    if (display_fields)
                        System.out.print("STAT:");
                    System.out.print(result.getLong("SERIES_STATUS"));
                    if (display_retr_aet) {
                        System.out.print(" ");
                        if (display_fields)
                            System.out.print("SRC::RETAET:");
                        String tmpAET = null;
                        String tmpAET1 = result.getString("SERRETAET");
                        String tmpAET2 = result.getString("SEREXTRETAET");
                        String srcAET = result.getString("SRCAET");
                        tmpAET = tmpAET1;
                        if (tmpAET2 != null) {
                            if (tmpAET != null) {
                                tmpAET = tmpAET + "," + tmpAET2;
                            } else {
                                tmpAET = tmpAET2;
                            }
                        }
                        System.out.print("{" + srcAET + "::" + tmpAET + "}");
                    }
                    if (display_pks) {
                        System.out.print(" ");
                        if (display_fields)
                            System.out.print("PK:");
                        System.out.print("[" + tmpKey + "]");
                    }
                    System.out.print(newline);
                }
                if (serds)
                    try {
                        displayDataSet(result, "SERIES_ATTRS");
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
            }
        }
    }

    private void displaySOP(ResultSet result) throws SQLException {
        String tmpSOP = result.getString("SOP_IUID");
        long tmpKey = result.getLong("D");

        if (tmpSOP == null) {
            lastSOP = -1;
        } else {
            if (lastSOP != tmpKey) {
                lastSOP = tmpKey;
                if (dsonly != true) {

                    System.out.print("   ");
                    if (display_fields)
                        System.out.print("IUID:");
                    System.out.print(tmpSOP + " ");
                    if (display_fields)
                        System.out.print("AVAIL:");
                    System.out.print(result.getString("INSTA") + " ");
                    if (display_fields)
                        System.out.print("STAT:");
                    System.out.print(result.getString("INST_STATUS"));
                    if (display_retr_aet) {
                        System.out.print(" ");
                        if (display_fields)
                            System.out.print("RETAET:");
                        String tmpAET = null;
                        String tmpAET1 = result.getString("SOPRETAET");
                        String tmpAET2 = result.getString("SOPEXTRETAET");
                        tmpAET = tmpAET1;
                        if (tmpAET2 != null) {
                            if (tmpAET != null) {
                                tmpAET = tmpAET + "," + tmpAET2;
                            } else {
                                tmpAET = tmpAET2;
                            }
                        }
                        System.out.print("{" + tmpAET + "}");
                    }
                    if (display_pks) {
                        System.out.print(" ");
                        if (display_fields)
                            System.out.print("PK:");
                        System.out.print("[" + tmpKey + "]");
                    }
                    System.out.print(newline);
                }
                if (sopds)
                    try {
                        displayDataSet(result, "INST_ATTRS");
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
            }
        }
    }

    private static void displayPath(ResultSet result) throws SQLException {
        System.out.print("    ");
        if (display_fields)
            System.out.print("PATH:");
        System.out.print(result.getString("DIRPATH") + "/");
        System.out.print(result.getString("FILEPATH") + " ");
        if (display_fields)
            System.out.print("SIZE:");
        System.out.print(result.getString("FILE_SIZE") + " ");
        if (display_fields)
            System.out.print("AVAIL:");
        System.out.print(result.getString("FSA"));
        if (display_pks) {
            System.out.print(" ");
            if (display_fields)
                System.out.print("PK:");
            System.out.print("[" + result.getLong("E") + "]");
        }
        System.out.print(newline);
    }

    private static String whereClause(String where, String field, String value, boolean delimiter) {
        String statement = "";

        if (value != null) {
            if (where != null && where != "")
                statement = " and ";

            statement = statement.concat(" " + field + " ");
            if (value.length() == 0)
                statement = statement.concat("is NULL");
            else {
                if (value.indexOf('%') != -1)
                    statement = statement.concat("like ");
                else
                    statement = statement.concat("= ");

                if (delimiter)
                    statement = statement.concat("'" + value + "'");
                else
                    statement = statement.concat(value);
            }

            if (debug)
                System.err.println(statement);
        }

        return statement;
    }

    private void displayXMLDATA(ResultSet result) throws SQLException {
        Blob blob = result.getBlob("XMLDATA");

        if (blob != null) {

            try {
                String s = blob2String(blob);
                // System.out.println( s );
                ByteArrayInputStream bis = new ByteArrayInputStream(s.getBytes());
                StreamSource is = new StreamSource(new InputStreamReader(bis));
                SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory.newInstance();
                Templates tp = tf.newTemplates(new StreamSource("file:///tmp/arr-details.xsl"));
                tp.newTransformer().transform(is, new StreamResult(System.out));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    /*
     * private static void displayDataSet(ResultSet result, String ATTR) throws
     * SQLException, IOException {
     * 
     * Blob blob=result.getBlob(ATTR);
     * 
     * if (blob != null) { InputStream bis = blob.getBinaryStream(); Dataset ds =
     * DcmObjectFactory.getInstance().newDataset();
     * 
     * try { ds.readFile(bis, null, -1);
     * 
     * ds.setFileMetaInfo(null); } catch (IOException e) { throw new
     * IllegalArgumentException("" + e); } System.out.println("<"+ATTR+">");
     * ds.dumpDataset(System.out, null); System.out.println("</"+ATTR+">"); } }
     */

    private TransformerHandler getTransformerHandler() throws TransformerConfigurationException, IOException {
        SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory.newInstance();
        TransformerHandler th = null;
        if (xls != null)
            xslt = new URL("file://" + xls);

        if (xslt != null) {
            if (xsltInc) {
                tf.setAttribute("http://xml.apache.org/xalan/features/incremental", Boolean.TRUE);
            }
            th = tf.newTransformerHandler(new StreamSource(xslt.openStream(), xslt.toExternalForm()));
            Transformer t = th.getTransformer();
            for (Iterator it = xsltParams.iterator(); it.hasNext();) {
                String s = (String) it.next();
                int eqPos = s.indexOf('=');
                t.setParameter(s.substring(0, eqPos), s.substring(eqPos + 1));
            }
        } else {
            th = tf.newTransformerHandler();
            th.getTransformer().setOutputProperty(OutputKeys.INDENT, "yes");
        }
        th.setResult(new StreamResult(out));
        return th;
    }

    private Dataset updateDataSet(Connection connection, ResultSet result, long PK, String ATTR, String[] UPD,
            String upd, String table) throws SQLException, IOException {

        String[] Line = new String[2];

        Blob blob = result.getBlob(ATTR);

        if (blob != null) {
            InputStream bis = blob.getBinaryStream();
            Dataset ds = DcmObjectFactory.getInstance().newDataset();
            ds.readFile(bis, null, -1);
            bis.close();

            if (debug) {
                System.out.println("<" + ATTR + " OLD>");
                ds.dumpDataset(System.out, null);
                System.out.println("</" + ATTR + "OLD>");
            }

            for (int loop = 0; loop < UPD.length; loop++) {
                Line = UPD[loop].split("=");
                int TAG = 0;
                if (Line[0].startsWith("x", 0)) {
                    TAG = Integer.parseInt(Line[0].substring(1), 16);
                } else {
                    TAG = Tags.forName(Line[0]);
                }
                if (Line[1].equals("_REMOVE_")) {
                    if (ds.contains(TAG)) {
                        ds.remove(TAG);
                    } else {
                        ds = null;
                    }
                } else {
                    ds.putXX(TAG, Line[1]);
                }
            }

            if (debug) {
                System.out.println("<" + ATTR + " NEW>");
                if (ds == null) {
                    System.out.println("No Changes to DataSet");
                } else {
                    ds.dumpDataset(System.out, null);
                }
                System.out.println("</" + ATTR + " NEW>");
            }

            if (ds != null) {
                String sql = null;
                sql = "update " + table + " set " + ATTR + "=?";
                if (upd.length() > 0)
                    sql = sql + "," + upd;

                sql = sql + " where PK=" + PK;

                if (debug)
                    System.err.println(sql);
                else {
                    PreparedStatement pstmt = connection.prepareStatement(sql);
                    int len = ds.calcLength(DcmEncodeParam.EVR_LE);
                    ByteArrayOutputStream bos = new ByteArrayOutputStream(len);
                    ds.writeDataset(bos, DcmEncodeParam.EVR_LE);
                    pstmt.setBinaryStream(1, new ByteArrayInputStream(bos.toByteArray()), len);
                    pstmt.execute();
                    pstmt.close();
                }
            }
        }

        return null;
    }

    /*
     * private static void displayDataSet(ResultSet result, String ATTR) throws
     * SQLException, IOException {
     * 
     * Blob blob=result.getBlob(ATTR);
     * 
     * if (blob != null) { InputStream bis = blob.getBinaryStream(); Dataset ds =
     * DcmObjectFactory.getInstance().newDataset(); try { ds.readFile(bis, null,
     * -1);
     * 
     * ds.setFileMetaInfo(null); } catch (IOException e) { throw new
     * IllegalArgumentException("" + e); } System.out.println("<"+ATTR+">");
     * ds.dumpDataset(System.out, null); System.out.println("</"+ATTR+">"); } }
     */

    private void displayDataSet(ResultSet result, String ATTR) throws SQLException, IOException {

        Blob blob = result.getBlob(ATTR);

        if (blob != null) {
            InputStream bis = blob.getBinaryStream();

            DcmParser parser = pfact.newDcmParser(bis);
            try {
                parser.setSAXHandler2(getTransformerHandler(), dict, excludeTags, excludeValueLengthLimit, baseDir);
            } catch (TransformerConfigurationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            // parser.setDcmHandler(ds.getDcmHandler());
            parser.parseDcmFile(parser.detectFileFormat(), -1);

            /*
             * System.out.println("<"+ATTR+">"); ds.dumpDataset(System.out,
             * null); System.out.println("</"+ATTR+">");
             */
        }
    }

    public static void main(String[] argv) {
        Properties defaultProps = new Properties();

        String ID = "$Id: Jpdbi.java 13101 2010-04-08 01:18:05Z kianusch $";
        String REVISION = "$Revision: 13101 $";

        try {
            defaultProps.load(ClassLoader.getSystemResourceAsStream("com/agfa/db/tools/jpdbi.properties"));
        } catch (Exception e) {
            System.err.println("I/O failed.");
        }

        Properties applicationProps = new Properties(defaultProps);

        File tmp = null;

        tmp = new File("/etc/jpdbi.properties");
        if (tmp.exists() && tmp.isFile() && tmp.canRead()) {
            try {
                applicationProps.load(new FileInputStream(tmp));
            } catch (Exception e) {
                System.err.println("Can't find " + tmp);
            }
        }
        tmp = new File("/etc/jdb.properties");
        if (tmp.exists() && tmp.isFile() && tmp.canRead()) {
            try {
                applicationProps.load(new FileInputStream(tmp));
            } catch (Exception e) {
                System.err.println("Can't find " + tmp);
            }
        }
        // get system newline
        if (newline == null || newline.length() <= 0)
            newline = "\n";

        // Parse Command Line
        boolean help = false;
        boolean version = false;
        String configFile = null;
        String outFile = null;

        String db_host = null;
        String db_sid = null;
        String db_port = null;
        String db_un = null;
        String db_pw = null;

        String db_alias = null;
        String db_url = null;

        String updates = null;

        boolean db_param = false;

        LongOpt[] longopts = new LongOpt[50];
        // 
        longopts[0] = new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h');
        longopts[1] = new LongOpt("debug", LongOpt.NO_ARGUMENT, null, OPT_DEBUG);
        longopts[2] = new LongOpt("sid", LongOpt.REQUIRED_ARGUMENT, null, OPT_SID);
        longopts[3] = new LongOpt("port", LongOpt.REQUIRED_ARGUMENT, null, 'P');
        longopts[4] = new LongOpt("username", LongOpt.REQUIRED_ARGUMENT, null, 'u');
        longopts[5] = new LongOpt("password", LongOpt.REQUIRED_ARGUMENT, null, 'p');
        longopts[6] = new LongOpt("cfg", LongOpt.REQUIRED_ARGUMENT, null, 'c');
        longopts[7] = new LongOpt("host", LongOpt.REQUIRED_ARGUMENT, null, 'a');

        longopts[8] = new LongOpt("version", LongOpt.NO_ARGUMENT, null, 'v');
        longopts[9] = new LongOpt("outfile", LongOpt.REQUIRED_ARGUMENT, null, 'O');

        longopts[10] = new LongOpt("series", LongOpt.REQUIRED_ARGUMENT, null, 'S');
        longopts[11] = new LongOpt("study", LongOpt.REQUIRED_ARGUMENT, null, 's');
        longopts[12] = new LongOpt("patid", LongOpt.REQUIRED_ARGUMENT, null, OPT_PATID);
        longopts[13] = new LongOpt("any", LongOpt.REQUIRED_ARGUMENT, null, 'i');
        longopts[14] = new LongOpt("modality", LongOpt.REQUIRED_ARGUMENT, null, 'm');
        longopts[15] = new LongOpt("stydate", LongOpt.REQUIRED_ARGUMENT, null, 'd');

        longopts[16] = new LongOpt("path", LongOpt.NO_ARGUMENT, null, OPT_PATH);
        longopts[17] = new LongOpt("sop", LongOpt.NO_ARGUMENT, null, OPT_SOP);

        longopts[18] = new LongOpt("update", LongOpt.REQUIRED_ARGUMENT, null, OPT_UPDATE);
        longopts[19] = new LongOpt("count", LongOpt.REQUIRED_ARGUMENT, null, OPT_UPDATE_CNT);
        longopts[20] = new LongOpt("patonly", LongOpt.NO_ARGUMENT, null, OPT_PATIENTLEVEL);
        longopts[21] = new LongOpt("issuer", LongOpt.REQUIRED_ARGUMENT, null, OPT_ISSUER);

        longopts[22] = new LongOpt("patient-level", LongOpt.NO_ARGUMENT, null, OPT_PATIENTLEVEL);
        longopts[23] = new LongOpt("study-level", LongOpt.NO_ARGUMENT, null, OPT_STUDYLEVEL);
        longopts[24] = new LongOpt("series-level", LongOpt.NO_ARGUMENT, null, OPT_SERIESLEVEL);

        longopts[25] = new LongOpt("retr-aet", LongOpt.NO_ARGUMENT, null, OPT_RETR_AET);
        longopts[26] = new LongOpt("pks", LongOpt.NO_ARGUMENT, null, OPT_PKS);

        longopts[27] = new LongOpt("dataset", LongOpt.NO_ARGUMENT, null, OPT_DATASET);
        longopts[28] = new LongOpt("patds", LongOpt.NO_ARGUMENT, null, OPT_PATDS);
        longopts[29] = new LongOpt("serds", LongOpt.NO_ARGUMENT, null, OPT_SERDS);
        longopts[30] = new LongOpt("styds", LongOpt.NO_ARGUMENT, null, OPT_STYDS);

        longopts[31] = new LongOpt("patient-ds", LongOpt.NO_ARGUMENT, null, OPT_PATDS);
        longopts[32] = new LongOpt("series-ds", LongOpt.NO_ARGUMENT, null, OPT_SERDS);
        longopts[33] = new LongOpt("study-ds", LongOpt.NO_ARGUMENT, null, OPT_STYDS);

        longopts[34] = new LongOpt("mwl", LongOpt.NO_ARGUMENT, null, OPT_MWLLEVEL);
        longopts[35] = new LongOpt("mwl-level", LongOpt.NO_ARGUMENT, null, OPT_MWLLEVEL);
        longopts[36] = new LongOpt("mwl-ds", LongOpt.NO_ARGUMENT, null, OPT_MWLDS);

        longopts[37] = new LongOpt("mpps", LongOpt.NO_ARGUMENT, null, OPT_MPPSLEVEL);
        longopts[38] = new LongOpt("mpps-level", LongOpt.NO_ARGUMENT, null, OPT_MPPSLEVEL);
        longopts[39] = new LongOpt("mpps-ds", LongOpt.NO_ARGUMENT, null, OPT_MPPSDS);
        longopts[40] = new LongOpt("sop-ds", LongOpt.NO_ARGUMENT, null, OPT_SOPDS);
        longopts[41] = new LongOpt("ThisOptionDoesNotExist", LongOpt.NO_ARGUMENT, null, OPT_THISOPTIONDOESNOTEXIST);
        longopts[42] = new LongOpt("updatepw", LongOpt.NO_ARGUMENT, null, OPT_UPDATEPW);
        longopts[43] = new LongOpt("display-audit-repository", LongOpt.REQUIRED_ARGUMENT, null, OPT_AUDITREP);
        longopts[44] = new LongOpt("pre214", LongOpt.NO_ARGUMENT, null, OPT_PRE214);
        longopts[45] = new LongOpt("url", LongOpt.REQUIRED_ARGUMENT, null, 'U');
        longopts[46] = new LongOpt("query", LongOpt.REQUIRED_ARGUMENT, null, 'q');
        longopts[47] = new LongOpt("db", LongOpt.REQUIRED_ARGUMENT, null, OPT_DBALIAS);

        longopts[48] = new LongOpt("ds-only", LongOpt.NO_ARGUMENT, null, OPT_DS_ONLY);
        longopts[49] = new LongOpt("xls", LongOpt.REQUIRED_ARGUMENT, null, OPT_XLS);

        int MODE = MODE_DISPLAY;

        Getopt g = new Getopt(Jpdbi.class.getName(), argv, ":q:U:a:P:u:p:c:o:O:S:s:i:m:d:hvF;", longopts);
        g.setOpterr(true);

        int c;
        while ((c = g.getopt()) != -1)
            switch (c) {
            case OPT_DS_ONLY:
                dsonly = true;
                break;
            //
            case OPT_XLS:
                dsonly = true;
                xls = g.getOptarg();
                break;
            //
            case OPT_DBALIAS:
                db_alias = g.getOptarg();
                db_param = true;
                break;
            //
            case OPT_DEBUG:
                debug = true;
                break;
            //
            case OPT_RETR_AET:
                display_retr_aet = true;
                break;
            //
            case OPT_PKS:
                display_pks = true;
                break;
            //
            case 'h':
                help = true;
                break;
            // host
            case 'a':
                db_host = g.getOptarg();
                db_param = true;
                break;
            // Connection Url
            case 'U':
                db_url = g.getOptarg();
                db_param = true;
                break;
            // port
            case 'P':
                db_port = g.getOptarg();
                db_param = true;
                break;
            // username
            case 'u':
                db_un = g.getOptarg();
                db_param = true;
                break;
            // password
            case 'p':
                db_pw = g.getOptarg();
                db_param = true;
                break;
            // sid
            case OPT_SID:
                db_sid = g.getOptarg();
                db_param = true;
                break;
            // cfg
            case 'q':
                queryCMD = g.getOptarg();
                break;
            // version
            case 'c':
                configFile = g.getOptarg();
                break;
            // version
            case 'v':
                version = true;
                break;
            // version
            case 'F':
                display_fields = true;
                break;
            // OutFile
            case 'O':
            case 'o':
                outFile = g.getOptarg();
                break;

            // series
            case 'S':
                series = g.getOptarg();
                break;
            // study
            case 's':
                study = g.getOptarg();
                break;
            // any (study or series)
            case 'i':
                anyid = g.getOptarg();
                break;
            // modality
            case 'm':
                modality = g.getOptarg();
                break;
            // stydate
            case 'd':
                stydate = g.getOptarg();
                break;
            // PatID
            case OPT_PATID:
                patid = g.getOptarg();
                break;
            //
            case OPT_ISSUER:
                issuer = g.getOptarg();
                break;
            //
            case OPT_UPDATEPW:
                upd_pw = true;
                break;
            //
            case OPT_AUDITREP:
                auditrep = true;
                auditid = g.getOptarg();
                MODE = MODE_AUDITREP;
                break;
            //
            case OPT_PRE214:
                pre214 = true;
                break;
            case OPT_PATIENTLEVEL:
                patient_level = true;
                break;
            case OPT_STUDYLEVEL:
                study_level = true;
                break;
            case OPT_SERIESLEVEL:
                series_level = true;
                break;
            case OPT_SOP:
                instance_level = true;
                break;
            case OPT_MWLLEVEL:
                mwl_level = true;
                break;
            //
            case OPT_MWLDS:
                mwlds = true;
                break;
            //
            case OPT_MPPSLEVEL:
                mpps_level = true;
                break;
            //
            case OPT_MPPSDS:
                mppsds = true;
                break;
            //
            case OPT_PATH:
                displaypath = true;
                break;
            case OPT_THISOPTIONDOESNOTEXIST:
                ThisOptionDoesNotExist = true;
                break;
            //
            //
            case OPT_DATASET:
                patds = true;
                styds = true;
                serds = true;
                sopds = true;
                break;
            case OPT_PATDS:
                patds = true;
                break;
            case OPT_STYDS:
                styds = true;
                break;
            case OPT_SERDS:
                serds = true;
                break;
            case OPT_SOPDS:
                sopds = true;
                break;
            //
            case OPT_UPDATE:
                updates = g.getOptarg();
                MODE = MODE_UPDATE;
                break;
            case OPT_UPDATE_CNT:
                upd_cnt = Long.parseLong(g.getOptarg());
                break;
            default:
                System.err.println("Error encountered during Argument Parsing.");
                System.err.println("Use -h for help.");
                System.exit(1);
                break;
            }

        int i = g.getOptind();

        if (help) {
            displayHelp();
            return;
        }

        if (configFile != null) {
            try {
                applicationProps.load(new FileInputStream(configFile));
            } catch (Exception e) {
                System.out.println("Couldn't load config file! using default config file");
            }
        }

        if (auditrep == true) {
            MODE = MODE_AUDITREP;
        } else if (upd_pw == true) {
            String impaxuser = null;
            String impaxpw = null;

            byte[] hashBytes = null;

            if (i < argv.length)
                impaxuser = argv[i++].trim();
            if (i < argv.length)
                impaxpw = argv[i++].trim();
            if (i < argv.length || impaxpw == null) {
                System.out.println("Option '--updatepw' requires two Arguments!");
                return;
            }

            try {
                MessageDigest md = MessageDigest.getInstance("SHA");
                hashBytes = md.digest(impaxpw.getBytes("UTF-8"));
            } catch (Exception e) {
                e.printStackTrace();
            }
            String encodedString = Base64.byteArrayToBase64(hashBytes);
            upd_password_sql = "UPDATE users SET passwd='" + encodedString + "' WHERE user_id='" + impaxuser + "'";
            MODE = MODE_PASSWORD;
        } else {
            if (i < argv.length)
                lastname = argv[i++].trim();
            if (i < argv.length)
                firstname = argv[i++].trim();
            if (i < argv.length)
                birthdate = argv[i++].trim();

            if (version) {
                System.out.println(REVISION);
                System.out.println(ID);
                return;
            }

            if (updates != null) {
                String old_level = null;
                String upd_dcm = null;
                String upd_db = null;
                String upd_field = null;
                String upd_value = null;
                String upd_m = null;
                String Updates[] = updates.split(",");
                upd = null;
                upd_dicom = new String[Updates.length];

                for (int loop = 0; loop < Updates.length; loop++) {
                    int split = Updates[loop].indexOf('=');
                    if (split != -1) {
                        upd_field = Updates[loop].substring(0, split).toLowerCase();
                        upd_value = Updates[loop].substring(split + 1);

                        upd_level = applicationProps.getProperty("update." + upd_field + ".level", "").trim();
                        if (old_level == null) {
                            old_level = upd_level;
                        }
                        if (upd_level.length() > 0) {
                            if (old_level.equalsIgnoreCase(upd_level)) {
                                upd_dcm = applicationProps.getProperty("update." + upd_field + ".dcm", "").trim();
                                upd_db = applicationProps.getProperty("update." + upd_field + ".dbfield", "").trim();
                                upd_m = applicationProps.getProperty("update." + upd_field + ".multi", "").trim();

                                if (upd_m.equalsIgnoreCase("yes") || upd_m.equalsIgnoreCase("true")) {
                                    upd_multi = upd_multi && true;
                                } else {
                                    upd_multi = false;
                                }

                                if (upd_db.equalsIgnoreCase("PAT_NAME") && upd_level.equalsIgnoreCase("PATIENT")) {
                                    int cnt = 0;
                                    for (int pos = 0; pos < upd_value.length(); pos++)
                                        if (upd_value.charAt(pos) == '^')
                                            cnt++;
                                    for (; cnt < 4; cnt++)
                                        upd_value = upd_value + "^";
                                }

                                if (upd != null && upd.length() > 0) {
                                    upd = upd + ",";
                                }

                                if (upd_dcm.length() > 0) {
                                    upd_dicom[loop] = upd_dcm + "=" + upd_value;
                                } else {
                                    upd_dicom[loop] = null;
                                }

                                if (upd_db.equalsIgnoreCase("MODALITY") && upd_level.equalsIgnoreCase("SERIES")) {
                                    upd_mod = true;
                                }

                                if (upd_db.equalsIgnoreCase("NONE")) {
                                    if (upd == null)
                                        upd = "";
                                } else {
                                    if (upd == null)
                                        upd = "";
                                    if (upd_value.equals("_REMOVE_")) {
                                        upd = upd + upd_db + "=null";
                                    } else {
                                        upd = upd + upd_db + "='" + upd_value + "'";
                                    }
                                }
                            } else {
                                System.err.println("Multilevel updates not supported.");
                                System.exit(1);
                            }
                        } else {
                            System.err.println("Update not defined.");
                            System.exit(1);
                        }

                    } else {
                        System.err.println("Update syntax error.");
                        System.exit(1);
                    }
                }
            }
        }

        String jdbcDriverClass = applicationProps.getProperty("jdbc.driver", "oracle.jdbc.driver.OracleDriver");

        try {
            Class.forName(jdbcDriverClass);
        } catch (ClassNotFoundException e) {
            System.err.println(e);
            System.exit(-1);
        }

        if (db_url != null)
            jdbcUrl = db_url;
        else {
            if (db_param != true)
                db_alias = applicationProps.getProperty("jdbc.url.DEFAULT");

            if (db_alias != null)
                jdbcUrl = applicationProps.getProperty("jdbc.url." + db_alias);

            if (jdbcUrl == null) {
                jdbcUrl = applicationProps.getProperty("jdbc.url");

                if (db_sid == null)
                    db_sid = applicationProps.getProperty("jdbc.sid");
                if (db_host == null)
                    db_host = applicationProps.getProperty("jdbc.host");
                if (db_un == null)
                    db_un = applicationProps.getProperty("jdbc.username");
                if (db_port == null)
                    db_port = applicationProps.getProperty("jdbc.port");
                if (db_pw == null)
                    db_pw = applicationProps.getProperty("jdbc.password");

                if (db_host != null)
                    jdbcUrl = jdbcUrl.replaceAll("\\$HOST\\$", db_host);

                if (db_sid != null)
                    jdbcUrl = jdbcUrl.replaceAll("\\$SID\\$", db_sid);

                if (db_sid != null)
                    jdbcUrl = jdbcUrl.replaceAll("\\$PORT\\$", db_port);

                if (db_un != null)
                    jdbcUrl = jdbcUrl.replaceAll("\\$USERNAME\\$", db_un);

                if (db_pw != null)
                    jdbcUrl = jdbcUrl.replaceAll("\\$PASSWORD\\$", db_pw);
            }
        }

        if (debug)
            System.out.println("Connection Url:< " + jdbcUrl + " >");

        if (pre214 == false)
            pre214 = (applicationProps.getProperty("pre214").equals("true"));

        // applicationProps.list(System.out);

        if (outFile != null) {
            try {
                System.setOut(new PrintStream(new BufferedOutputStream(new FileOutputStream(outFile))));
            } catch (Exception e) {
                System.err.println(e);
            }
        }

        Jpdbi instance = new Jpdbi(MODE);
    }

    public class Query {
        private String where, from, links, order;

        public Query(String where, String from, String links, String order) {
            this.where = where;
            this.links = links;
            this.from = from;
            this.order = order;
        }
    }
}