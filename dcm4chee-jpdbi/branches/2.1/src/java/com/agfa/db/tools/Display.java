// $Id: Display.java 14436 2010-11-27 22:04:41Z kianusch $

package com.agfa.db.tools;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.URL;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.tree.VariableHeightLayoutCache;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.dcm4che.data.DcmParser;
import org.dcm4che.data.DcmParserFactory;
import org.dcm4che.dict.DictionaryFactory;
import org.dcm4che.dict.TagDictionary;

public class Display {
    // dcm2xml
    private static File baseDir;

    private static OutputStream out = System.out;

    private static final DcmParserFactory pfact = DcmParserFactory.getInstance();

    private static int[] excludeTags = {};

    private static URL xslt = null;

    private static LinkedList xsltParams = new LinkedList();

    private static boolean xsltInc = false;

    private static TagDictionary dict = DictionaryFactory.getInstance().getDefaultTagDictionary();

    public static long LastPatient = -2;

    public static long LastStudy = -2;

    public static long LastSerie = -2;

    public static long LastInstance = -2;

    public static long LastFile = -2;

    private static int excludeValueLengthLimit = Integer.MAX_VALUE;

    public final void setExcludeValueLengthLimit(int excludeValueLengthLimit) {
        this.excludeValueLengthLimit = excludeValueLengthLimit;
    }

    private static String DisplayValue(ResultSet rs, String label, String dbField, int dbFieldType, boolean displayLabel,
            boolean displayValue) throws SQLException {
        if (!displayValue)
            return "";

        String s;
        
        switch (dbFieldType) {
        case Types.NUMERIC:
            Long l = rs.getLong(dbField);
            s = l+"";
            break;
        default:
            s = rs.getString(dbField);
            break;
        }
        
        return DisplayValue(label, s, displayLabel, true);
    }

    private static String DisplayValue(ResultSet rs, String label, String dbField, int dbFieldType, boolean displayLabel)
            throws SQLException {
        return DisplayValue(rs, label, dbField, dbFieldType, displayLabel, true);
    }

    private static String DisplayValue(String label, String field, boolean displayLabel, boolean displayField) {
        String preFix = "";
        String postFix = "";

        if (label.startsWith("{}")) {
            preFix = "{";
            postFix = "}";
            label = label.substring(2);
        } else if (label.startsWith("[]")) {
            preFix = "[";
            postFix = "]";
            label = label.substring(2);
        } else if (label.startsWith("()")) {
            preFix = "(";
            postFix = ")";
            label = label.substring(2);
        } else if (label.startsWith("<>")) {
            preFix = "<";
            postFix = ">";
            label = label.substring(2);
        }

        if (displayField) {
            String out = "";
            if (displayLabel)
                out += label+":";
            out += preFix + field + postFix;
            out += " ";
            return out;
        }
        return "";
    }

    private static String DisplayValue(String label, String field, boolean displayLabel) {
        return DisplayValue(label, field, displayLabel, true);
    }

    private static void DataSet(ResultSet rs, String field) throws SQLException, IOException {

        Blob blob = rs.getBlob(field);

        if (blob != null) {
            InputStream bis = blob.getBinaryStream();

            DcmParser parser = pfact.newDcmParser(bis);
            try {
                parser.setSAXHandler2(getTransformerHandler(), dict, excludeTags, excludeValueLengthLimit, baseDir);
            } catch (TransformerConfigurationException e) {
                e.printStackTrace();
            }
            parser.parseDcmFile(parser.detectFileFormat(), -1);
        }
    }

    /*
     * private static void displayDataSet(ResultSet result, String ATTR) throws
     * SQLException, IOException {
     * 
     * Blob blob=result.getBlob(ATTR);
     * 
     * if (blob != null) { InputStream bis = blob.getBinaryStream(); Dataset ds
     * = DcmObjectFactory.getInstance().newDataset();
     * 
     * try { ds.readFile(bis, null, -1);
     * 
     * ds.setFileMetaInfo(null); } catch (IOException e) { throw new
     * IllegalArgumentException("" + e); } System.out.println("<"+ATTR+">");
     * ds.dumpDataset(System.out, null); System.out.println("</"+ATTR+">"); } }
     */

    private static TransformerHandler getTransformerHandler() throws TransformerConfigurationException, IOException {
        SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory.newInstance();
        TransformerHandler th = null;
        // xslt = new URL ("file:///tmp/my.xls");

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

    static long Patient(ResultSet rs, ResultSetMetaData md, Config cfg) throws SQLException, IOException {
        String out = "";
        long tmpKey = rs.getLong("A");

        if (rs.wasNull()) {
            System.out.println("No Patient Object");
            return -2;
        }

        if (LastPatient != tmpKey) {
            LastPatient = tmpKey;

            out += DisplayValue("PATIENT", ("" + rs.getString("PAT_NAME")).replace('^', ' ').trim(), cfg.isDisplayFields());
            out += DisplayValue(rs, "SEX", "PAT_SEX", Types.VARCHAR, cfg.isDisplayFields());

            String BD = "-";
            if (cfg.isPre214()) {
                Timestamp tmpBD = rs.getTimestamp("BD");
                if (tmpBD != null) {
                    BD = cfg.fDate.format(tmpBD);
                }
            } else {
                String tmpBD = rs.getString("BD");
                if (tmpBD != null && tmpBD.length() == 8) {
                    BD = tmpBD.substring(0, 4) + "-" + tmpBD.substring(4, 6) + "-" + tmpBD.substring(6);
                }
            }
            out += DisplayValue("BIRTHDATE", BD, cfg.isDisplayFields());

            out += DisplayValue("<>ISSUER/ID", rs.getString("PAT_ID_ISSUER") + ":" + rs.getString("PAT_ID"),
                    cfg.isDisplayFields());
            out += DisplayValue("[]PK", tmpKey+"", cfg.isDisplayFields(), cfg.isDisplayPKs());

            System.out.println(out.trim());

            if (cfg.isDisplayDS(Jpdbi.PATIENT)) {
                DataSet(rs, "PAT_ATTRS");
            }
            return tmpKey;
        }
        return -1;
    }

    static long Study(ResultSet rs, ResultSetMetaData md, Config cfg) throws SQLException, IOException {
        String out = "";
        long tmpKey = rs.getLong("B");
        if (rs.wasNull()) {
            System.out.println(" No Study Objects");
            return -2;
        }

        if (LastStudy != tmpKey) {
            LastStudy = tmpKey;

            out += DisplayValue(rs, "STUDY", "STYIUID", Types.VARCHAR, cfg.isDisplayFields());

            String TS = "-";
            Timestamp tmpSD = rs.getTimestamp("STYD");
            if (tmpSD != null) {
                TS = cfg.fTimeStamp.format(tmpSD);
            }
            out += DisplayValue("DATE", TS, cfg.isDisplayFields());

            String tmpMOD = rs.getString("STYMODS");
            if (tmpMOD != null) {
                tmpMOD = tmpMOD.replace('\\', ',');
            } else {
                tmpMOD = "-";
            }
            out += DisplayValue("MOD", tmpMOD, cfg.isDisplayFields());
            out += DisplayValue(rs, "#SER", "STYNUMSER", Types.NUMERIC, cfg.isDisplayFields(), cfg.isDisplayStatus());
            out += DisplayValue(rs, "#INST", "STYNUMINST", Types.NUMERIC, cfg.isDisplayFields(), cfg.isDisplayStatus());
            out += DisplayValue(rs, "AVAIL", "STYAVAIL", Types.NUMERIC, cfg.isDisplayFields(), cfg.isDisplayStatus());

            if (cfg.isDisplayAETs()) {
                String tmpAET = null;
                String tmpAET1 = rs.getString("STYRETAET");
                String tmpAET2 = rs.getString("STYEXTRETAET");
                tmpAET = tmpAET1;
                if (tmpAET2 != null) {
                    if (tmpAET != null) {
                        tmpAET = tmpAET + "," + tmpAET2;
                    } else {
                        tmpAET = tmpAET2;
                    }
                }
                out += DisplayValue("{}RETAET", tmpAET, cfg.isDisplayFields());
            }

            out += DisplayValue("[]PK", tmpKey + "", cfg.isDisplayFields(), cfg.isDisplayPKs());

            System.out.println(" " + out.trim());
            if (cfg.isDisplayDS(Jpdbi.STUDY)) {
                DataSet(rs, "STUDY_ATTRS");
            }
            return tmpKey;
        }
        return -1;

    }

    static long Serie(ResultSet rs, ResultSetMetaData md, Config cfg) throws SQLException, IOException {
        String out = "";
        long tmpKey = rs.getLong("C");
        if (rs.wasNull()) {
            System.out.println("  No Series Objects");
            return -2;
        }

        if (LastSerie != tmpKey) {
            LastSerie = tmpKey;
            out += DisplayValue(rs, "SERIES", "SERIUID", Types.VARCHAR, cfg.isDisplayFields());
            out += DisplayValue(rs, "MOD", "SERMOD", Types.VARCHAR, cfg.isDisplayFields());
            out += DisplayValue(rs, "#INST", "SERNUMINST", Types.NUMERIC, cfg.isDisplayFields(), cfg.isDisplayStatus());
            out += DisplayValue(rs, "AVAIL", "SERAVAIL", Types.NUMERIC, cfg.isDisplayFields(), cfg.isDisplayStatus());
            out += DisplayValue(rs, "STAT", "SERSTATUS", Types.NUMERIC, cfg.isDisplayFields(), cfg.isDisplayStatus());

            if (cfg.isDisplayAETs()) {
                String tmpAET = null;
                String tmpAET1 = rs.getString("SERRETAET");
                String tmpAET2 = rs.getString("SEREXTRETAET");
                String srcAET = rs.getString("SERSRCAET");
                tmpAET = tmpAET1;
                if (tmpAET2 != null) {
                    if (tmpAET != null) {
                        tmpAET = tmpAET + "," + tmpAET2;
                    } else {
                        tmpAET = tmpAET2;
                    }
                }
                out += DisplayValue("{}SRC::RETAET", srcAET + "::" + tmpAET, cfg.isDisplayFields());
            }

            out += DisplayValue("[]PK", tmpKey + "", cfg.isDisplayFields(), cfg.isDisplayPKs());

            System.out.println("  " + out.trim());
            if (cfg.isDisplayDS(Jpdbi.SERIE)) {
                DataSet(rs, "SERIES_ATTRS");
            }
            return tmpKey;
        }
        return -1;
    }

    static long Instance(ResultSet rs, ResultSetMetaData md, Config cfg) throws SQLException, IOException {
        String out = "";
        long tmpKey = rs.getLong("D");
        if (rs.wasNull()) {
            System.out.println("   No Instance Objects");
            return -2;
        }

        if (LastInstance != tmpKey) {
            LastInstance = tmpKey;
            out += DisplayValue(rs, "INSTANCE", "SOPIUID", Types.VARCHAR, cfg.isDisplayFields());
            out += DisplayValue(rs, "AVAIL", "INSTAVAIL", Types.NUMERIC, cfg.isDisplayFields(), cfg.isDisplayStatus());
            out += DisplayValue(rs, "STAT", "INSTSTATUS", Types.NUMERIC, cfg.isDisplayFields(), cfg.isDisplayStatus());

            if (cfg.isDisplayAETs()) {
                String tmpAET = null;
                String tmpAET1 = rs.getString("INSTRETAET");
                String tmpAET2 = rs.getString("INSTEXTRETAET");
                tmpAET = tmpAET1;
                if (tmpAET2 != null) {
                    if (tmpAET != null) {
                        tmpAET = tmpAET + "," + tmpAET2;
                    } else {
                        tmpAET = tmpAET2;
                    }
                }
                out += DisplayValue("{}RETAET", tmpAET, cfg.isDisplayFields());
            }

            out += DisplayValue("[]PK", tmpKey + "", cfg.isDisplayFields(), cfg.isDisplayPKs());
            System.out.println("   " + out.trim());
            if (cfg.isDisplayDS(Jpdbi.INSTANCE)) {
                DataSet(rs, "INST_ATTRS");
            }
            return tmpKey;
        }
        return -1;
    }

    static long Path(ResultSet rs, ResultSetMetaData md, Config cfg) throws SQLException, IOException {
        String out = "";
        long tmpKey = rs.getLong("E");
        if (rs.wasNull()) {
            System.out.println("    No File Objects");
            return -2;
        }

        String tmpPath = rs.getString("DIRPATH") + "/" + rs.getString("FILEPATH");
        out += DisplayValue("PATH", tmpPath, cfg.isDisplayFields());
        out += DisplayValue(rs, "SIZE", "FILESIZE", Types.NUMERIC, cfg.isDisplayFields(), cfg.isDisplayStatus());
        out += DisplayValue(rs, "STAT", "FILESTATUS", Types.NUMERIC, cfg.isDisplayFields(), cfg.isDisplayStatus());
        out += DisplayValue(rs, "AVAIL", "FSAVAIL", Types.NUMERIC, cfg.isDisplayFields(), cfg.isDisplayStatus());

        if (cfg.isDisplayLevel(Jpdbi.FILESYSTEM)) {
            out += DisplayValue(rs, "GROUP", "FSGRP", Types.VARCHAR, cfg.isDisplayFields());
            out += DisplayValue(rs, "[]FSPK", "F", Types.NUMERIC, cfg.isDisplayFields());
            out += DisplayValue(rs, "{}FSAET", "FSRETAET", Types.VARCHAR, cfg.isDisplayFields(), cfg.isDisplayAETs());
        }

        out += DisplayValue("[]PK", tmpKey + "", cfg.isDisplayFields(), cfg.isDisplayPKs());
        System.out.println("    " + out);
        return tmpKey;
    }
}
