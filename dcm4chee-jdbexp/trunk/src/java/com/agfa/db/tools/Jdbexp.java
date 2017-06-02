// $Id: Jdbexp.java 13161 2010-04-13 12:52:56Z kianusch $

package com.agfa.db.tools;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;

// import oracle.jdbc.*;

public class Jdbexp {

	public final static String ID = "$Id: Jdbexp.java 13161 2010-04-13 12:52:56Z kianusch $";
	public final static String REVISION = "$Revision: 13161 $";

	public static final int DB_TYPES_UNKNOWN = 0;
	public static final int DB_TYPES_ORACLE = 1;
	public static final int DB_TYPES_MYSQL = 2;

	public static final String PREPARESTATEMENT = "PrepareStatement";
	public static final String PREPARECALL = "PrepareCall";
	public static final String DEFINEFIELDS = "DefineFields";
	public static final String EXECUTE = "ExecuteStatement";
	
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

	private static String lob2String(Clob clob) throws SQLException, IOException {

		StringBuffer sb = new StringBuffer();
		int getLen = -1;
		long clobLen = clob.length();
		int readLen = (clobLen < 1024 * 1024 * 1024) ? (int) clobLen : 1024 * 1024 * 1024;
		byte[] tmpbyte = new byte[(int) readLen];

		InputStream inputStream = clob.getAsciiStream();
		while ((getLen = inputStream.read(tmpbyte, 0, readLen)) > 0) {
			sb.append(new String(tmpbyte, 0, getLen));
		}

		return sb.toString();
	}

	private static String lob2String(Blob blob) throws SQLException, IOException {

		StringBuffer sb = new StringBuffer();
		int getLen = -1;
		long blobLen = blob.length();
		int readLen = (blobLen < 1024 * 1024 * 1024) ? (int) blobLen : 1024 * 1024 * 1024;
		byte[] tmpbyte = new byte[(int) readLen];

		InputStream inputStream = blob.getBinaryStream();
		while ((getLen = inputStream.read(tmpbyte, 0, readLen)) > 0) {
			sb.append(new String(tmpbyte, 0, getLen));
		}

		return sb.toString();
	}

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
			String StringDelimiter, boolean cvs, boolean dump, boolean inserts, boolean displaylobs) throws SQLException,
			IOException {
		String result = null;
		String value = null;
		switch (ColumnType) {
		case Types.TIMESTAMP:
			Timestamp ts = rs.getTimestamp(col);
			if (!rs.wasNull()) {
				value = StringDelimiter + CommandLine.fTimeStamp.format(ts) + StringDelimiter;
				if (inserts)
					value = "{ts " + value + "}";
			}
			break;
		case Types.TIME:
			Time time = rs.getTime(col);
			if (!rs.wasNull()) {
				value = StringDelimiter + CommandLine.fTime.format(time) + StringDelimiter;
				if (inserts)
					value = "{t " + value + "}";
			}
			break;
		case Types.DATE:
			Date date = rs.getDate(col);
			if (!rs.wasNull()) {
				value = StringDelimiter + CommandLine.fDate.format(date) + StringDelimiter;
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
				if (displaylobs || dump || inserts)
					value = "::" + Base64.Encode(lob2String(blob));
				else
					value = "[" + ColumnTypeName + ":" + blob.length() + "]";
			}
			break;

		case Types.CLOB:
			Clob clob = rs.getClob(col);
			if (!rs.wasNull()) {
				if (dump || inserts)
					value = "::" + Base64.Encode(lob2String(clob));
				else if (displaylobs)
					value = lob2String(clob);
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

	private static void evalResultSet(ResultSet rs, CommandLine cfg) throws SQLException, IOException {
		String fields = null;
		String PlaceHolder = "";

		ResultSetMetaData md = rs.getMetaData();
		int count = md.getColumnCount();

		if (cfg.header || cfg.dump || cfg.inserts) {
			fields = "";
			for (int j = 1; j <= count; j++) {
				if (cfg.csv)
					fields = fields.concat(cfg.TextDelimitor + md.getColumnName(j) + cfg.TextDelimitor);
				else
					fields = fields.concat(md.getColumnName(j));
				PlaceHolder = PlaceHolder.concat("?");
				if (j != count) {
					fields = fields.concat(cfg.FieldSeperator);
					PlaceHolder = PlaceHolder.concat(",");
				}
			}

			if (cfg.header) {
				fields = fields.concat(cfg.RecordSeperator + cfg.nl);
				System.out.print(fields);
			}
		}

		String prefix = "";
		String postfix = "";

		if (cfg.inserts) {
			prefix = "insert into " + cfg.tableName + " (" + fields + ") values (";
			postfix = ");";
		}

		if (cfg.dump) {
			switch (cfg.Database) {
			case DB_TYPES_ORACLE:
				System.out.print("-- " + PREPARECALL + ": begin ");
				break;
			default:
				System.out.print("-- " + PREPARESTATEMENT + ": ");
				break;
			}

			System.out.print("insert into " + cfg.tableName + " (" + fields + ") values (" + PlaceHolder + ");");

			switch (cfg.Database) {
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
					values = values.concat(cfg.FieldSeperator);
				values = values.concat(md.getColumnTypeName(j));
			}
			System.out.println(values + ";");
		}

		while (rs.next()) {
			String values = prefix;

			for (int j = 1; j <= count; j++) {
				String result = getColumn(rs, j, md.getColumnType(j), md.getColumnTypeName(j), cfg.TextDelimitor, cfg.csv,
						cfg.dump, cfg.inserts, cfg.displaylobs);
				if (j > 1)
					values = values.concat(cfg.FieldSeperator);

				if (cfg.label)
					values = values.concat(md.getColumnName(j) + ": ");

				values = values.concat(result);
			}

			values = values.concat(postfix + cfg.RecordSeperator + cfg.nl);
			System.out.print(values);
		}
		if (cfg.dump)
			System.out.println("--");

		rs.close();
	}

	private static void _Commit(Connection conn, boolean debug) throws SQLException {
		if (debug)
			if (debug)
				System.err.println("DEBUG: Commiting...");
		conn.commit();
	}

	public static void main(String[] argv) {
		String sql = null;
		Connection conn = null;
		Statement stmt = null;
		BufferedReader br = null;
		PrepStmt prepStmt = null;
		long StmtCount = 0;
		long unCommitted = 0;
		ResultSet rs = null;

		CommandLine cfg = new CommandLine(argv);

		if (cfg.debug)
			System.err.println("DEBUG: Connect Url: < " + cfg.jdbcUrl + " >");

		try {
			conn = DriverManager.getConnection(cfg.jdbcUrl);
			cfg.setDatabase(conn);

			if (cfg.commitInterval > 0) {
				if (cfg.debug)
					System.err.println("DEBUG: AutoCommit turned off. Committing after " + cfg.commitInterval
							+ " Statements.");
				conn.setAutoCommit(false);
			}

			stmt = conn.createStatement();
			prepStmt = new PrepStmt(conn, cfg.debug);

			if (cfg.sqlFile) {
				try {
					br = new BufferedReader(new FileReader(cfg.sqlFilename));
				} catch (FileNotFoundException e) {
					Jdbexp.exit(1, e.toString());
				}
				sql = sqlFileRead(br);
			} else {
				sql = cfg.sql.trim();
			}

			while (sql != null) {
				StmtCount++;
				if (sql.endsWith(";") && !sql.startsWith("--"))
					sql = sql.substring(0, sql.length() - 1);

				if (sql.length() > 0) {
					try {
						if (cfg.sqlFile && (prepStmt.pStmt != null || sql.startsWith("--"))) {
							// System.err.println("<"+sql+">");
							if (prepStmt.parse(sql, StmtCount)) {
								if (cfg.debug)
									System.err.println("DEBUG: [" + StmtCount + "] * Executing Call/PrepareStatement");
								if (prepStmt.execute()) {
									rs = prepStmt.getResultSet();
								} else {
									rs = null;
								}
							}
						} else {
							if (cfg.debug)
								System.err.println("DEBUG: [" + StmtCount + "] SQL: " + sql);
							if (stmt.execute(sql)) {
								rs = stmt.getResultSet();
							} else {
								rs = null;
							}
						}
						if (rs != null) {
							try {
								evalResultSet(rs, cfg);
							} catch (IOException e) {
								Jdbexp.exit(1, e.toString());
							}
						} else {
							unCommitted++;
						}
					} catch (SQLException e) {
						System.err.println("Error: [" + StmtCount + "] Statement: " + sql);
						System.err.print("Error: [" + StmtCount + "] " + e);
						if (cfg.ignoresqlerror == false) {
							Jdbexp.exit(1, "Aborting SQL Execution...");

						}
					}

					if (cfg.commitInterval > 0 && unCommitted >= cfg.commitInterval) {
						_Commit(conn, cfg.debug);
						unCommitted = 0;
					}
				}
				if (cfg.sqlFile)
					sql = sqlFileRead(br);
				else
					sql = null;
			}

			if (cfg.commitInterval > 0 && unCommitted > 0) {
				_Commit(conn, cfg.debug);
			}

			stmt.close();
			conn.close();
		} catch (SQLException e) {
			if (cfg.debug)
				e.printStackTrace();
			else
				Jdbexp.exit(1, e.toString());
		}
		Jdbexp.exit(0);
	}
}