// $Id: Display.java 13124 2010-04-09 17:51:03Z kianusch $

package com.agfa.db.tools;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.LinkedList;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.dcm4che.data.Dataset;
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
	private static int excludeValueLengthLimit = Integer.MAX_VALUE;
	private static URL xslt = null;
	private static LinkedList xsltParams = new LinkedList();
	private static boolean xsltInc = false;
	private static TagDictionary dict = DictionaryFactory.getInstance().getDefaultTagDictionary();

	public static long LastPatient = -2;

	public static long LastStudy = -2;

	public static long LastSerie = -2;

	public static long LastInstance = -2;

	public static long LastFile = -2;

	public final void setExcludeValueLengthLimit(int excludeValueLengthLimit) {
		this.excludeValueLengthLimit = excludeValueLengthLimit;
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
	 * if (blob != null) { InputStream bis = blob.getBinaryStream(); Dataset ds =
	 * DcmObjectFactory.getInstance().newDataset();
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

	static long Patient(ResultSet rs, ResultSetMetaData md, CommandLine cfg) throws SQLException, IOException {
		String out = "";
		long tmpKey = rs.getLong("A");

		if (rs.wasNull()) {
			System.out.println("No Patient Object");
			return -2;
		}

		if (LastPatient != tmpKey) {
			LastPatient = tmpKey;

			if (cfg.displayFields)
				out = out.concat("NAME:");
			out = out.concat(("" + rs.getString("PAT_NAME")).replace('^', ' ').trim() + " ");

			if (cfg.displayFields)
				out = out.concat("SEX:");
			out = out.concat(rs.getString("PAT_SEX") + " ");

			if (cfg.displayFields)
				out = out.concat("BIRTHDATE:");
			if (cfg.pre214) {
				Timestamp tmpBD = rs.getTimestamp("BD");
				if (tmpBD != null) {
					out = out.concat(CommandLine.fDate.format(tmpBD) + " ");
				} else {
					out = out.concat("- ");
				}
			} else {
				String tmpBD = rs.getString("BD");
				if (tmpBD != null && tmpBD.length() == 8) {
					out = out.concat(tmpBD.substring(0, 4) + "-" + tmpBD.substring(4, 6) + "-" + tmpBD.substring(6) + " ");
				} else {
					out = out.concat("- ");
				}
			}

			if (cfg.displayFields)
				out = out.concat("ISSUER/ID:");
			out = out.concat("<");
			out = out.concat(rs.getString("PAT_ID_ISSUER") + "");
			out = out.concat(":");
			out = out.concat(rs.getString("PAT_ID") + "");
			out = out.concat(">");

			if (cfg.displayPKS) {
				out = out.concat(" ");
				if (cfg.displayFields)
					out = out.concat("PK:");
				out = out.concat("[" + tmpKey + "]");
			}
			System.out.println(out);
			if (cfg.displayDS.get(Jpdbi.PATIENT)) {
				DataSet(rs, "PAT_ATTRS");			
			}
			return tmpKey;
		}
		return -1;
	}

	static long Study(ResultSet rs, ResultSetMetaData md, CommandLine cfg) throws SQLException, IOException {
		String out = "";
		long tmpKey = rs.getLong("B");
		if (rs.wasNull()) {
			System.out.println(" No Study Objects");
			return -2;
		}

		if (LastStudy != tmpKey) {
			LastStudy = tmpKey;
			if (cfg.displayFields)
				out = out.concat("IUID:");
			out = out.concat(rs.getString("STYIUID") + " ");
			if (cfg.displayFields)
				out = out.concat("DATE:");
			Timestamp tmpSD = rs.getTimestamp("STYD");
			if (tmpSD != null) {
				out = out.concat(CommandLine.fTimeStamp.format(tmpSD) + " ");
			} else {
				out = out.concat("- ");
			}
			if (cfg.displayFields)
				out = out.concat("MODS:");
			String tmpMOD = rs.getString("STYMODS");
			if (tmpMOD != null) {
				out = out.concat("(" + tmpMOD.replace('\\', ',') + ") ");
			} else {
				out = out.concat("( - ) ");
			}

			if (cfg.displayFields)
				out = out.concat("#SER:");
			out = out.concat(rs.getLong("STYNUMSER") + " ");
			if (cfg.displayFields)
				out = out.concat("#INST:");
			out = out.concat(rs.getLong("STYNUMINST") + " ");

			if (cfg.displayFields)
				out = out.concat("AVAIL:");
			out = out.concat(rs.getLong("STYAVAIL") + "");

			if (cfg.displayAET) {
				out = out.concat(" ");
				if (cfg.displayFields)
					out = out.concat("RETAET:");
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
				out = out.concat("{" + tmpAET + "}");
			}

			if (cfg.displayPKS) {
				out = out.concat(" ");
				if (cfg.displayFields)
					out = out.concat("PK:");
				out = out.concat("[" + tmpKey + "]");
			}

			System.out.println(" " + out);
			if (cfg.displayDS.get(Jpdbi.STUDY)) {
				DataSet(rs, "STUDY_ATTRS");			
			}
			return tmpKey;
		}
		return -1;

	}

	static long Serie(ResultSet rs, ResultSetMetaData md, CommandLine cfg) throws SQLException, IOException {
		String out = "";
		long tmpKey = rs.getLong("C");
		if (rs.wasNull()) {
			System.out.println("  No Series Objects");
			return -2;
		}

		if (LastSerie != tmpKey) {
			LastSerie = tmpKey;
			if (cfg.displayFields)
				out = out.concat("IUID:");
			out = out.concat(rs.getString("SERIUID") + " ");
			// out=out.concat(result.getString("SD") + " ");
			if (cfg.displayFields)
				out = out.concat("MOD:");
			out = out.concat(rs.getString("SERMOD") + " ");
			if (cfg.displayFields)
				out = out.concat("#INST:");
			out = out.concat(rs.getLong("SERNUMINST") + " ");
			if (cfg.displayFields)
				out = out.concat("AVAIL:");
			out = out.concat(rs.getLong("SERAVAIL") + " ");
			if (cfg.displayFields)
				out = out.concat("STAT:");
			out = out.concat(rs.getLong("SERSTATUS") + "");
			if (cfg.displayAET) {
				out = out.concat(" ");
				if (cfg.displayFields)
					out = out.concat("SRC::RETAET:");
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
				out = out.concat("{" + srcAET + "::" + tmpAET + "}");
			}
			if (cfg.displayPKS) {
				out = out.concat(" ");
				if (cfg.displayFields)
					out = out.concat("PK:");
				out = out.concat("[" + tmpKey + "]");
			}
			System.out.println("  " + out);
			if (cfg.displayDS.get(Jpdbi.SERIE)) {
				DataSet(rs, "SERIES_ATTRS");			
			}
			return tmpKey;
		}
		return -1;
	}

	static long Instance(ResultSet rs, ResultSetMetaData md, CommandLine cfg) throws SQLException, IOException {
		String out = "";
		long tmpKey = rs.getLong("D");
		if (rs.wasNull()) {
			System.out.println("   No Instance Objects");
			return -2;
		}

		if (LastInstance != tmpKey) {
			LastInstance = tmpKey;
			if (cfg.displayFields)
				out = out.concat("IUID:");
			out = out.concat(rs.getString("SOPIUID") + " ");
			if (cfg.displayFields)
				out = out.concat("AVAIL:");
			out = out.concat(rs.getString("INSTAVAIL") + " ");
			if (cfg.displayFields)
				out = out.concat("STAT:");
			out = out.concat(rs.getString("INSTSTATUS") + "");
			if (cfg.displayAET) {
				out = out.concat(" ");
				if (cfg.displayFields)
					out = out.concat("RETAET:");
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
				out = out.concat("{" + tmpAET + "}");
			}
			if (cfg.displayPKS) {
				out = out.concat(" ");
				if (cfg.displayFields)
					out = out.concat("PK:");
				out = out.concat("[" + tmpKey + "]");
			}
			System.out.println("   " + out);
			if (cfg.displayDS.get(Jpdbi.INSTANCE)) {
				DataSet(rs, "INST_ATTRS");			
			}
			return tmpKey;
		}
		return -1;
	}

	static long Path(ResultSet rs, ResultSetMetaData md, CommandLine cfg) throws SQLException, IOException {
		String out = "";
		long tmpKey = rs.getLong("E");
		if (rs.wasNull()) {
			System.out.println("    No File Objects");
			return -2;
		}

		if (cfg.displayFields)
			out = out.concat("PATH:");
		out = out.concat(rs.getString("DIRPATH") + "/" + rs.getString("FILEPATH") + " ");
		if (cfg.displayFields)
			out = out.concat("SIZE:");
		out = out.concat(rs.getString("FILESIZE") + " ");
		if (cfg.displayFields)
			out = out.concat("AVAIL:");
		out = out.concat(rs.getString("FSAVAIL") + "");

		if (cfg.displayFSInfo) {
			out = out.concat(" ");
			if (cfg.displayFields)
				out = out.concat("FSPK:");
			out = out.concat("[" + rs.getLong("F") + "] ");
			if (cfg.displayFields)
				out = out.concat("GROUP:");
			out = out.concat(rs.getString("FSGRP") + "");
		}

		if (cfg.displayPKS) {
			out = out.concat(" ");
			if (cfg.displayFields)
				out = out.concat("PK:");
			out = out.concat("[" + tmpKey + "]");
		}
		System.out.println("    " + out);
		return tmpKey;
	}
}