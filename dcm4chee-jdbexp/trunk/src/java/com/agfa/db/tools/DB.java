// $Id: DB.java 13160 2010-04-13 12:49:33Z kianusch $

package com.agfa.db.tools;

import java.io.ByteArrayInputStream;
import java.sql.*;

class PrepStmt {
   private int FT[] = null;

   Connection conn = null;

   private boolean debug = false;

   PreparedStatement pStmt = null;

   private long cnt = -1;

   private boolean active = false;

   PrepStmt(Connection _conn, boolean _debug) throws SQLException {
       debug = _debug;
       conn = _conn;
       cnt = -1;
       active = false;
   }

   public ResultSet getResultSet() throws SQLException {
       return pStmt.getResultSet();
   }

   public boolean execute() throws SQLException {
       return pStmt.execute();
   }

   public void close(long StmtCount) throws Exception {
       if (debug)
           System.err.println("DEBUG: [" + StmtCount + "] * Release PrepareStatement/Call");

       if (pStmt != null) {
           pStmt.close();
           pStmt = null;
       }
       active = false;
       cnt = -1;
   }

   public boolean parse(String sql, long StmtCount) throws SQLException {
       if (sql.equals("--")) {
           if (debug)
               System.err.println("DEBUG: [" + StmtCount + "] * Release PrepareStatement/Call");

           if (pStmt != null) {
               pStmt.close();
               pStmt = null;
           }
           active = false;
           cnt = -1;
       } else if (sql.startsWith("--")) {
           sql = sql.substring(2).trim();
           if (debug)
               System.err.println("DEBUG: [" + StmtCount + "] * " + sql);

           if (sql.startsWith(Jdbexp.PREPARESTATEMENT + ":") && sql.endsWith(";")) {
               cnt = 0;
               active = true;
               pStmt = conn.prepareStatement(sql.substring(Jdbexp.PREPARESTATEMENT.length() + 1,
                       (sql.length() - 1)).trim());
           } else if (sql.startsWith(Jdbexp.PREPARECALL + ":") && sql.endsWith(";")) {
               cnt = 0;
               active = true;
               pStmt = conn.prepareCall(sql.substring(Jdbexp.PREPARECALL.length() + 1).trim());
           } else if (sql.equalsIgnoreCase(Jdbexp.EXECUTE)) {
               if (active)
                   return true;
               else
               	Jdbexp.exit(1, "ERROR: [" + StmtCount + "] * Set Statment before supplying executing");
           } else if (sql.startsWith(Jdbexp.DEFINEFIELDS + ":") && sql.endsWith(";")) {
               sql = sql.substring(Jdbexp.DEFINEFIELDS.length() + 1, (sql.length() - 1)).trim();
               String FTT[] = sql.split(",");
               int NumFields = FTT.length;
               FT = new int[NumFields];

               for (int loop = 0; loop < NumFields; loop++) {
                   FT[loop] = Types.NULL;

                   if (FTT[loop].equalsIgnoreCase("Raw"))
                       FT[loop] = Types.BLOB;
                   if (FTT[loop].equalsIgnoreCase("Blob"))
                       FT[loop] = Types.BLOB;
                   if (FTT[loop].equalsIgnoreCase("LongBlob"))
                       FT[loop] = Types.LONGVARBINARY;
                   if (FTT[loop].equalsIgnoreCase("Binary"))
                       FT[loop] = Types.BINARY;
                   if (FTT[loop].equalsIgnoreCase("VARBINARY"))
                       FT[loop] = Types.VARBINARY;

                   if (FTT[loop].equalsIgnoreCase("Clob"))
                       FT[loop] = Types.CLOB;
                   if (FTT[loop].equalsIgnoreCase("Text"))
                       FT[loop] = Types.CLOB;

                   if (FTT[loop].equalsIgnoreCase("Number"))
                       FT[loop] = Types.NUMERIC;
                   if (FTT[loop].equalsIgnoreCase("Numeric"))
                       FT[loop] = Types.NUMERIC;

                   if (FTT[loop].equalsIgnoreCase("BIGINT"))
                       FT[loop] = Types.BIGINT;
                   if (FTT[loop].equalsIgnoreCase("INT"))
                       FT[loop] = Types.INTEGER;

                   if (FTT[loop].equalsIgnoreCase("Char"))
                       FT[loop] = Types.CHAR;
                   if (FTT[loop].equalsIgnoreCase("String"))
                       FT[loop] = Types.VARCHAR;
                   if (FTT[loop].equalsIgnoreCase("Varchar"))
                       FT[loop] = Types.VARCHAR;
                   if (FTT[loop].equalsIgnoreCase("VarChar2"))
                       FT[loop] = Types.VARCHAR;
                   if (FTT[loop].equalsIgnoreCase("LONG"))
                       FT[loop] = Types.LONGVARCHAR;

                   if (FTT[loop].equalsIgnoreCase("DATETIME"))
                       FT[loop] = Types.TIMESTAMP;
                   if (FTT[loop].equalsIgnoreCase("TimeStamp"))
                       FT[loop] = Types.TIMESTAMP;
                   if (FTT[loop].equalsIgnoreCase("Date"))
                       FT[loop] = Types.DATE;
                   if (FTT[loop].equalsIgnoreCase("Time"))
                       FT[loop] = Types.TIME;

                   if (FT[loop] == Types.NULL) {
                  	 Jdbexp.exit(1, "ERROR: Unknown Type " + FTT[loop] + " for field " + (loop + 1));
                   }
               }
           } else {
               if (debug)
                   System.err.println("DEBUG: [" + StmtCount + "] * Comment: " + sql);
           }
       } else {
           if (active) {
               if (debug)
                   System.err.println("DEBUG: [" + StmtCount + "] * Set Values: " + sql);

               cnt++;

               String FV[] = sql.split(",(?=(?:[^']*'[^']*')*(?![^']*\'))");
               int NumFields = FV.length;
               String s = null;

               if (NumFields > 0 && FT != null && NumFields == FT.length) {
                   for (int loop = 0; loop < NumFields; loop++) {
                       if (!FV[loop].equalsIgnoreCase("null")) {
                           s = FV[loop];
                           switch (FT[loop]) {
                           case Types.VARCHAR:
                           case Types.CHAR:
                               if (s.startsWith("'") && s.endsWith("'")) {
                                   s = s.substring(1, (s.length() - 1)).replaceAll("''", "'");
                               }
                               pStmt.setString(loop + 1, s);
                               break;
                           case Types.NUMERIC:
                           case Types.BIGINT:
                           case Types.INTEGER:
                               // LONG
                               pStmt.setLong(loop + 1, Long.parseLong(s));
                               break;
                           case Types.CLOB:
                           case Types.LONGVARCHAR:
                               if (s.startsWith("::")) {
                                   s = s.substring(2);
                               }
                               byte decodedClob[] = Base64.Decode(s);
                               pStmt.setAsciiStream(loop + 1, new ByteArrayInputStream(decodedClob),
                                       decodedClob.length);

                               /*
                                * oracle.sql.CLOB b =
                                * oracle.sql.CLOB.createTemporary(
                                * (oracle.jdbc.driver.OracleConnection) conn,
                                * true, oracle.sql.CLOB.DURATION_SESSION);
                                * b.open(oracle.sql.CLOB.MODE_READWRITE);
                                * OutputStream out = b.getAsciiOutputStream();
                                * out.write(decoded); out.close();
                                * pStmt.setObject(loop + 1, b,
                                * java.sql.Types.CLOB); b.close();
                                */
                               break;

                           case Types.BLOB:
                           case Types.BINARY:
                           case Types.VARBINARY:
                           case Types.LONGVARBINARY:
                               if (s.startsWith("::"))
                                   s = s.substring(2);
                               byte decodedBlob[] = Base64.Decode(s);
                               /*
                                * oracle.sql.BLOB b =
                                * oracle.sql.BLOB.createTemporary(
                                * (oracle.jdbc.driver.OracleConnection) conn,
                                * true, oracle.sql.BLOB.DURATION_SESSION);
                                * b.open(oracle.sql.BLOB.MODE_READWRITE);
                                * OutputStream out = b.getBinaryOutputStream();
                                * out.write(decoded); out.close();
                                * pStmt.setObject(loop + 1, b,
                                * java.sql.Types.BLOB); b.close();
                                */
                               pStmt.setBinaryStream(loop + 1, new ByteArrayInputStream(decodedBlob),
                                       decodedBlob.length);
                               break;
                           case Types.TIMESTAMP:
                               if (s.startsWith("'") && s.endsWith("'")) {
                                   s = s.substring(1, (s.length() - 1));
                               }
                               pStmt.setTimestamp(loop + 1, Timestamp.valueOf(s));
                               break;
                           case Types.DATE:
                               if (s.startsWith("'") && s.endsWith("'")) {
                                   s = s.substring(1, (s.length() - 1));
                               }
                               pStmt.setDate(loop + 1, Date.valueOf(s));
                               break;
                           case Types.TIME:
                               if (s.startsWith("'") && s.endsWith("'")) {
                                   s = s.substring(1, (s.length() - 1));
                               }
                               pStmt.setTime(loop + 1, Time.valueOf(s));
                               break;
                           default:
                           	Jdbexp.exit(1, "ERROR: [" + StmtCount + "] * Unknown Type for field " + (loop + 1));
                           break;
                           }

                       } else {
                           pStmt.setNull(loop + 1, FT[loop]);
                       }
                   }
                   return true;
               } else {
               	Jdbexp.exit(1, "ERROR: [" + StmtCount + "] * Set Values: Wrong number of arguments");
               }
           } else {
         	  Jdbexp.exit(1, "ERROR: [" + StmtCount + "] * Set Statment before supplying values");
           }
       }

       return false;
   }
}