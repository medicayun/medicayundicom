// $Id: Jdbexp.java 14499 2010-12-06 11:11:52Z kianusch $

package com.agfa.db.tools;

import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import java.io.*;
import java.sql.*;

// import oracle.jdbc.*;

public class Jdbexp {

	public final static String VERSION = "2.1.2";

	public final static String ID = "$Id: Jdbexp.java 14499 2010-12-06 11:11:52Z kianusch $";

	public final static String REVISION = "$Revision: 14499 $";

	public static final int DB_TYPES_UNKNOWN = 0;

	public static final int DB_TYPES_ORACLE = 1;

	public static final int DB_TYPES_MYSQL = 2;

	public static final String PREPARESTATEMENT = "PrepareStatement";

	public static final String PREPARECALL = "PrepareCall";

	public static final String DEFINEFIELDS = "DefineFields";

	public static final String EXECUTE = "Execute";

	public static ByteArrayOutputStream baos = new ByteArrayOutputStream(8192);

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

	private static String lob2String(Clob clob) throws SQLException, IOException, TransformerException {
		return lob2String(clob, null);
	}

	private static String lob2String(Clob clob, String xsl) throws SQLException, IOException, TransformerException {
		StringBuffer sb = new StringBuffer();
		int getLen = -1;
		long clobLen = clob.length();
		int readLen = (clobLen < 1024 * 1024 * 1024) ? (int) clobLen : 1024 * 1024 * 1024;
		byte[] tmpbyte = new byte[(int) readLen];

		InputStream inputStream = clob.getAsciiStream();
		while ((getLen = inputStream.read(tmpbyte, 0, readLen)) > 0) {
			sb.append(new String(tmpbyte, 0, getLen));
		}

		if (xsl != null)
			return blob2StringViaXSL(sb.toString(), xsl);
		else
			return sb.toString();
	}

	private static String lob2String(Blob blob) throws SQLException, IOException, TransformerException {
		return lob2String(blob, null);
	}

	private static String lob2String(Blob blob, String xsl) throws SQLException, IOException, TransformerException {
		StringBuffer sb = new StringBuffer();
		int getLen = -1;
		long blobLen = blob.length();
		int readLen = (blobLen < 1024 * 1024 * 1024) ? (int) blobLen : 1024 * 1024 * 1024;
		byte[] tmpbyte = new byte[(int) readLen];

		InputStream inputStream = blob.getBinaryStream();
		while ((getLen = inputStream.read(tmpbyte, 0, readLen)) > 0) {
			sb.append(new String(tmpbyte, 0, getLen));
		}

		if (xsl != null)
			return blob2StringViaXSL(sb.toString(), xsl);
		else
			return sb.toString();
	}

	private static String blob2StringViaXSL(String s, String xsl) throws TransformerException {
			SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory.newInstance();
			Templates tp = tf.newTemplates(new StreamSource(xsl));

			StreamSource is = new StreamSource(new InputStreamReader(new ByteArrayInputStream(s.getBytes())));
			baos.reset();
			tp.newTransformer().transform(is, new StreamResult(baos));
			return baos.toString();
	}

	/*
	 * private static String blob2String(Blob blob) throws Exception {
	 * 
	 * StringBuffer sb = new StringBuffer(); int getLen = -1; int readLen = 1024;
	 * byte[] tmpbyte = new byte[readLen];
	 * 
	 * InputStream inputStream = blob.getBinaryStream(); while ((getLen =
	 * inputStream.read(tmpbyte, 0, readLen)) > 0) { sb.append(new
	 * String(tmpbyte, 0, getLen)); }
	 * 
	 * return sb.toString();
	 * 
	 * }// End method: blob2String(Blob)
	 */

	private static String sqlFileRead(BufferedReader br) {
		String s = null;
		StringBuffer sb = new StringBuffer();
		try {
			while ((s = br.readLine()) != null) {
				s = s.trim();
				if (s.length() > 0) {
					if (sb.length() > 0)
						sb.append(" ");
					sb.append(s);

					if (s.startsWith("--") || s.endsWith(";"))
						break;
				}
			}
		} catch (IOException e) {
			Jdbexp.exit(1, e.toString());

		}
		if (sb.length() > 0) {
			String sql = sb.toString();
			if (!sql.endsWith(";") && !sql.startsWith("--")) {
				Jdbexp.exit(127, "Missing ';' at the end of the sql-statement.");

			}
			return sql;
		}
		return null;
	}

	private static String getColumn(ResultSet rs, int col, int ColumnType, String ColumnTypeName,
			String StringDelimiter, boolean cvs, boolean dump, boolean inserts, boolean displaylobs, String xsl)
			throws SQLException, IOException, TransformerException {
		String result = null;
		String value = null;

		switch (ColumnType) {
		case Types.TIMESTAMP:
			Timestamp ts = rs.getTimestamp(col);
			if (!rs.wasNull()) {
				value = StringDelimiter + Config.fTimeStamp.format(ts) + StringDelimiter;
				if (inserts)
					value = "{ts " + value + "}";
			}
			break;
		case Types.TIME:
			Time time = rs.getTime(col);
			if (!rs.wasNull()) {
				value = StringDelimiter + Config.fTime.format(time) + StringDelimiter;
				if (inserts)
					value = "{t " + value + "}";
			}
			break;
		case Types.DATE:
			Date date = rs.getDate(col);
			if (!rs.wasNull()) {
				value = StringDelimiter + Config.fDate.format(date) + StringDelimiter;
				if (inserts)
					value = "{d " + value + "}";
			}
			break;

		case Types.NUMERIC:
		case Types.BIGINT:
		case Types.INTEGER:
			result = rs.getString(col);
			if (!rs.wasNull())
				value = result;
			break;

		case Types.VARCHAR:
		case Types.CHAR:
			result = rs.getString(col);
			if (!rs.wasNull()) {
				if (cvs)
					value = StringDelimiter + result.replaceAll("\"", "\"\"") + StringDelimiter;
				if (dump || inserts)
					value = StringDelimiter + result.replaceAll("'", "''") + StringDelimiter;
				else
					value = StringDelimiter + result + StringDelimiter;
			}
			break;

		case Types.BLOB:
		case Types.LONGVARBINARY:
			Blob blob = rs.getBlob(col);
			if (!rs.wasNull()) {
				if (displaylobs || dump || inserts || xsl != null) {
					value = lob2String(blob, xsl);
					if (xsl == null) {
						value = "::" + Base64.Encode(value);
					}
				} else
					value = "[" + ColumnTypeName + ":" + blob.length() + "]";
			}
			break;

		case Types.CLOB:
			Clob clob = rs.getClob(col);
			if (!rs.wasNull()) {
				if (dump || inserts  || xsl != null | displaylobs) {
					value = lob2String(clob, xsl);
					if (dump || inserts) {
						value = "::" + Base64.Encode(value);
					}
				}
				else
					value = "[" + ColumnTypeName + ":" + clob.length() + "]";
			}
			break;

		default:
			value = "[" + ColumnTypeName + "|" + ColumnType + "]";
			break;
		}

		if (rs.wasNull())
			return "NULL";

		return value;
	}

	private static void evalResultSet(ResultSet rs, Config cfg) throws SQLException, IOException, TransformerException {
		String fields = null;
		String PlaceHolder = "";

		ResultSetMetaData md = rs.getMetaData();
		int count = md.getColumnCount();

		if (cfg.isHeader() || cfg.isDump() || cfg.isInsert()) {
			fields = "";
			for (int j = 1; j <= count; j++) {
				if (cfg.isCsv())
					fields = fields.concat(cfg.getDelimitor() + md.getColumnName(j) + cfg.getDelimitor());
				else
					fields = fields.concat(md.getColumnName(j));
				PlaceHolder = PlaceHolder.concat("?");
				if (j != count) {
					fields = fields.concat(cfg.getFieldSeperator());
					PlaceHolder = PlaceHolder.concat(",");
				}
			}

			if (cfg.isHeader()) {
				fields = fields.concat(cfg.getRecordSeperator() + cfg.nl);
				System.out.print(fields);
			}
		}

		String prefix = "";
		String postfix = "";

		if (cfg.isInsert()) {
			prefix = "insert into " + cfg.getTableName() + " (" + fields + ") values (";
			postfix = ");";
		}

		if (cfg.isDump()) {
			switch (cfg.getDbType()) {
			case DB_TYPES_ORACLE:
				System.out.print("-- " + PREPARECALL + ": begin ");
				break;
			default:
				System.out.print("-- " + PREPARESTATEMENT + ": ");
				break;
			}

			System.out.print("insert into " + cfg.getTableName() + " (" + fields + ") values (" + PlaceHolder + ");");

			switch (cfg.getDbType()) {
			case DB_TYPES_ORACLE:
				System.out.println(" end;");
				break;
			default:
				System.out.println();
				break;
			}

			String values = "-- " + DEFINEFIELDS + ": ";
			for (int j = 1; j <= count; j++) {
				if (j > 1)
					values = values.concat(cfg.getFieldSeperator());
				values = values.concat(md.getColumnTypeName(j));
			}
			System.out.println(values + ";");
		}

		while (rs.next()) {
			String values = prefix;

			for (int j = 1; j <= count; j++) {
				String result = getColumn(rs, j, md.getColumnType(j), md.getColumnTypeName(j), cfg.getDelimitor(), cfg
						.isCsv(), cfg.isDump(), cfg.isInsert(), cfg.isDisplayLobs(), cfg.getXSL());
				if (j > 1)
					values = values.concat(cfg.getFieldSeperator());

				if (cfg.isLabel())
					values = values.concat(md.getColumnName(j) + ": ");

				values = values.concat(result);
			}

			values = values.concat(postfix + cfg.getRecordSeperator() + cfg.nl);
			System.out.print(values);
		}
		if (cfg.isDump())
			System.out.println("--");

		rs.close();
	}

	private static void _Commit(Connection conn, boolean debug) throws SQLException {
		if (debug)
			System.err.println("DEBUG: Commiting...");
		conn.commit();
	}

	public static void main(String[] argv) {
		String sql = null;
		Connection conn = null;
		Statement stmt = null;
		BufferedReader br = null;
		SQL prepStmt = null;
		long StmtCount = 0;
		long unCommitted = 0;

		Config cfg = new Config();
		cfg.ParseCommandLine(argv);

		if (cfg.isDebug())
			System.err.println("DEBUG: Connect Url: < " + cfg.getJdbcUrl() + " >");

		try {
			conn = DriverManager.getConnection(cfg.getJdbcUrl());
			DatabaseMetaData dmd = conn.getMetaData();
			cfg.setDbType(dmd.getDatabaseProductName());

			if (cfg.getCommitInterval() > 0) {
				if (cfg.isDebug())
					System.err.println("DEBUG: AutoCommit turned off. Committing after " + cfg.getCommitInterval()
							+ " Statements.");
				conn.setAutoCommit(false);
			}

			stmt = conn.createStatement();
			prepStmt = new SQL(conn, cfg.isDebug());

			if (cfg.isSqlFile()) {
				try {
					br = new BufferedReader(new FileReader(cfg.getSqlFilename()));
				} catch (FileNotFoundException e) {
					Jdbexp.exit(1, e.toString());
				}
				sql = sqlFileRead(br);
			} else {
				sql = cfg.getSql().trim();
			}

			while (sql != null) {
				StmtCount++;
				if (sql.endsWith(";") && !sql.startsWith("--"))
					sql = sql.substring(0, sql.length() - 1);

				if (sql.length() > 0) {
					try {
						ResultSet rs = null;
						if (cfg.isSqlFile() && (prepStmt.isPrepareStatement() || sql.startsWith("--"))) {
							// System.err.println("<"+sql+">");
							if (prepStmt.parse(sql, StmtCount)) {
								if (cfg.isDebug())
									System.err.println("DEBUG: [" + StmtCount + "] * Executing Call/PrepareStatement");
								if (prepStmt.execute()) {
									rs = prepStmt.getResultSet();
								}
							}
						} else {
							if (cfg.isDebug())
								System.err.println("DEBUG: [" + StmtCount + "] SQL: " + sql);
							if (stmt.execute(sql)) {
								rs = stmt.getResultSet();
							}
						}
						if (rs != null) {
							try {
								evalResultSet(rs, cfg);
								rs.close();
							} catch (IOException e) {
								Jdbexp.exit(1, e.toString());
							}
						} else {
							unCommitted++;
						}
					} catch (Exception e) {
						System.err.println("Error: [" + StmtCount + "] Statement: " + sql);
						System.err.print("Error: [" + StmtCount + "] " + e);
						if (cfg.isIgnoreSqlError() == false) {
							Jdbexp.exit(1, "Aborting SQL Execution...");

						}
					}

					if (cfg.getCommitInterval() > 0 && unCommitted >= cfg.getCommitInterval()) {
						_Commit(conn, cfg.isDebug());
						unCommitted = 0;
					}
				}
				if (cfg.isSqlFile())
					sql = sqlFileRead(br);
				else
					sql = null;
			}

			if (cfg.getCommitInterval() > 0 && unCommitted > 0) {
				_Commit(conn, cfg.isDebug());
			}

			stmt.close();
			conn.close();
		} catch (SQLException e) {
			if (cfg.isDebug())
				e.printStackTrace();
			else
				Jdbexp.exit(1, e.toString());
		}
		Jdbexp.exit(0);
	}
}