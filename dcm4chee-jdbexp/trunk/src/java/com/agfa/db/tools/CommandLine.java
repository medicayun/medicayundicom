// $Id: CommandLine.java 13160 2010-04-13 12:49:33Z kianusch $

package com.agfa.db.tools;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

class CommandLine {
   String FieldSeperator = ";";

   String RecordSeperator = "";

   String TextDelimitor = "";

   String tableName = null;

   boolean debug = false;

   boolean header = false;

   boolean csv = false;

   boolean label = false;

   boolean dump = false;

   boolean displaylobs = false;

   boolean inserts = false;

   boolean sqlFile = false;

   boolean ignoresqlerror = false;

   String sqlFilename = null;

   String jdbcUrl = null;

   String sql = null;

   long commitInterval = -1;

   public int Database = Jdbexp.DB_TYPES_UNKNOWN;

   //
   String nl = System.getProperty("line.separator");

   static SimpleDateFormat fTimeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

   static SimpleDateFormat fTime = new SimpleDateFormat("HH:mm:ss");

   static SimpleDateFormat fDate = new SimpleDateFormat("yyyy-MM-dd");
   

   public static void LongHelp() {
       System.out.println("Usage: java -jar jdbexp [options] <sql> | --sqlfile <file> | -M <macro>");
       System.out.println("       java -jar jdbexp -h | --help");
       System.out.println("       java -jar jdbexp -v | --version");
       System.out.println("       java -jar jdbexp --jdbcurlhelp");
       System.out.println();
       System.out.println("        -v | --verson                    display version information");
       System.out.println();
       System.out.println("        -U | --url <jdbcURL>             DB jdbcURL to connect");
       System.out.println("             --db <alias>                DB alias to connect");
       System.out.println();
       System.out.println("        -d | --delimiter <delimiter>     set <delimiter> (')");
       System.out.println("        -H | --header                    display columnnames as header");
       System.out.println("        -L | --label                     display columnnames as labels (linewise)");
       System.out.println();
       System.out.println("        --csv                            export as CSV");
       System.out.println("        --insert                         export with insert statements");
       System.out.println("        --dump                           export as (nonportable) dump");
       System.out.println();
       System.out.println("        --sqlfile <file>                 use <file> for SQL statements");
       System.out.println("        --commit <interval>              commit after <interval> statements");
       System.out.println();
       System.out.println("        -M <macro> [arguments]           execute predefined macro");
       System.out.println();
       System.out.println("        -O | --out <file> [-z | -gzip]   write output to (gziped) <file>");
       System.out.println("        -E | --err <file>                write error to <file>");
       System.out.println("");
       System.out.println("        --jdbcurlhelp                    jdbcURL Syntax");
       System.out.println();
       System.out.println("        --debug                          debug mode");
       System.out.println();
   }

   public static void UrlHelp() {
       System.out.println("jdbc URL Examples:");
       System.out.println();
       System.out.println("ORACLE: jdbc:oracle:<drivertype>:<username/password>@<database>");
       System.out.println("");
       System.out.println("        <drivertype>: * thin");
       System.out.println("                      * oci");
       System.out.println("                      * kprb");
       System.out.println();
       System.out.println(" <username/password>: * is either empty or of the form <username>/<password>");
       System.out.println();
       System.out.println("          <database>: * <host>:<port>:<SID>");
       System.out.println("                      * //<host>:<port>/<service>");
       System.out.println("                      * <TNSName>");
       System.out.println();
       System.out.println("             example: * jdbc:oracle:thin:dbuser/dbpw@dbhost:1521:MYSID");
       System.out.println("                      * jdbc:oracle:thin:@MYTSNNAME");
       System.out.println();
   }

   public static void ShortHelp() {
       System.out.println("Usage: java -jar jdbexp <sql>");
       System.out.println("       java -jar jdbexp -h | --help");
       System.out.println("       java -jar jdbexp -v | --version");
       System.out.println();
       System.out.println("        -v | --version     display version information");
       System.out.println("        -h | --help        display extended help");
       System.out.println();
       System.out.println("        --debug            debug mode");
       System.out.println();
   }



   void setDatabase(Connection conn) throws SQLException {
       DatabaseMetaData dmd = conn.getMetaData();

       if (dmd.getDatabaseProductName().equalsIgnoreCase("ORACLE")) {
           Database = Jdbexp.DB_TYPES_ORACLE;
       } else if (dmd.getDatabaseProductName().equalsIgnoreCase("MYSQL")) {
           Database = Jdbexp.DB_TYPES_MYSQL;
       } else {
           Database = Jdbexp.DB_TYPES_UNKNOWN;
       }
   }

   CommandLine(String[] argv) {
       Properties defaultProps = new Properties();

       try {
           defaultProps.load(ClassLoader.getSystemResourceAsStream("com/agfa/db/tools/jdbexp.properties"));
       } catch (Exception e) {
      	 Jdbexp.exit(1, "I/O failed.");
       }

       Properties applicationProps = new Properties(defaultProps);

       File tmp = null;

       tmp = new File("/etc/jdbexp.properties");
       if (tmp.exists() && tmp.isFile() && tmp.canRead()) {
           try {
               applicationProps.load(new FileInputStream(tmp));
           } catch (Exception e) {
         	  Jdbexp.exit(1, "Can't find " + tmp);
           }
       }
       tmp = new File("/etc/jdb.properties");
       if (tmp.exists() && tmp.isFile() && tmp.canRead()) {
           try {
               applicationProps.load(new FileInputStream(tmp));
           } catch (Exception e) {
         	  Jdbexp.exit(1, "Can't find " + tmp);
           }
       }

       String dbalias = null;
       String sqlMacro = null;
       String outFilename = null;
       String errFilename = null;

       boolean gzip = false;

       final int OPT_DEBUG = 1;
       final int OPT_SQLFILE = 2;
       final int OPT_DBALIAS = 3;
       final int OPT_CSV = 4;
       final int OPT_IGNORESQLERROR = 5;
       final int OPT_COMMIT = 6;
       final int OPT_INSERTS = 7;
       final int OPT_DUMP = 8;
       final int OPT_URLHELP = 9;
       final int OPT_DISPLAYLOBS = 10;

       final LongOpt[] longopts = { new LongOpt("label", LongOpt.NO_ARGUMENT, null, 'L'),
               new LongOpt("displaylobs", LongOpt.NO_ARGUMENT, null, OPT_DISPLAYLOBS),
               new LongOpt("csv", LongOpt.NO_ARGUMENT, null, OPT_CSV),
               new LongOpt("jdbcurlhelp", LongOpt.NO_ARGUMENT, null, OPT_URLHELP),
               new LongOpt("ignoresqlerr", LongOpt.NO_ARGUMENT, null, OPT_IGNORESQLERROR),
               new LongOpt("insert", LongOpt.NO_ARGUMENT, null, OPT_INSERTS),
               new LongOpt("dump", LongOpt.NO_ARGUMENT, null, OPT_DUMP),
               new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h'),
               new LongOpt("debug", LongOpt.NO_ARGUMENT, null, OPT_DEBUG),
               new LongOpt("header", LongOpt.NO_ARGUMENT, null, 'H'),
               new LongOpt("version", LongOpt.NO_ARGUMENT, null, 'v'),
               new LongOpt("gzip", LongOpt.NO_ARGUMENT, null, 'z'),
               new LongOpt("out", LongOpt.REQUIRED_ARGUMENT, null, 'O'),
               new LongOpt("err", LongOpt.REQUIRED_ARGUMENT, null, 'E'),
               new LongOpt("url", LongOpt.REQUIRED_ARGUMENT, null, 'U'),
               new LongOpt("delimiter", LongOpt.REQUIRED_ARGUMENT, null, 'd'),
               new LongOpt("commit", LongOpt.REQUIRED_ARGUMENT, null, OPT_COMMIT),
               new LongOpt("db", LongOpt.REQUIRED_ARGUMENT, null, OPT_DBALIAS),
               new LongOpt("sqlfile", LongOpt.REQUIRED_ARGUMENT, null, OPT_SQLFILE) };

       Getopt g = new Getopt("jdbexp2", argv, ":U:d:M:E:O:hvLHz", longopts);
       g.setOpterr(true);

       int c;

       while ((c = g.getopt()) != -1)
           switch (c) {
           case OPT_DEBUG:
               debug = true;
               break;

           case OPT_DISPLAYLOBS:
               displaylobs = true;
               break;

           case OPT_IGNORESQLERROR:
               ignoresqlerror = true;
               break;

           case OPT_COMMIT:
               String commits = g.getOptarg();
               commitInterval = Long.parseLong(commits.trim());
               break;

           case OPT_INSERTS:
               inserts = true;
               TextDelimitor = "\'";
               FieldSeperator = ",";
               break;

           case OPT_DUMP:
               dump = true;
               TextDelimitor = "\'";
               FieldSeperator = ",";
               RecordSeperator = ";";
               break;

           case 'M':
               sqlMacro = g.getOptarg();
               break;

           case OPT_CSV:
               csv = true;
               header = true;
               TextDelimitor = "\"";
               FieldSeperator = ";";
               break;

           case 'H':
               header = true;
               break;

           case 'L':
               label = true;
               FieldSeperator = nl;
               RecordSeperator = nl;
               break;

           case 'd':
               FieldSeperator = g.getOptarg();
               break;

           case 'h':
               LongHelp();
               System.exit(0);
               break;

           case OPT_URLHELP:
               UrlHelp();
               System.exit(0);
               break;

           case 'U':
               jdbcUrl = g.getOptarg();
               break;

           case OPT_SQLFILE:
               sqlFilename = g.getOptarg();
               sqlFile = true;
               break;

           case OPT_DBALIAS:
               dbalias = g.getOptarg();
               break;

           case 'v':
               System.out.println(Jdbexp.ID);
               System.out.println(Jdbexp.REVISION);
               System.exit(0);
               break;

           case 'E':
               errFilename = g.getOptarg();
               break;

           case 'O':
               outFilename = g.getOptarg();
               break;

           case 'z':
               gzip = true;
               break;

           default:
         	  Jdbexp.exit(1, "Error encountered during Argument Parsing." + nl + "Use -h for help.");
           break;
           }

       int i = g.getOptind();

       if (errFilename != null) {
           try {
               System.setErr(new PrintStream(new BufferedOutputStream(new FileOutputStream(errFilename))));
           } catch (Exception e) {
         	  Jdbexp.exit(1, e.toString());
           }
       }

       if (outFilename != null) {
           try {
               if (gzip == true)
                   System.setOut(new PrintStream(new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(
                           outFilename)))));
               else
                   System.setOut(new PrintStream(new BufferedOutputStream(new FileOutputStream(outFilename))));

           } catch (Exception e) {
         	  Jdbexp.exit(1, e.toString());
           }
       }

       if (sqlMacro != null) {
           if (sqlMacro.equalsIgnoreCase("list")) {
               System.out.println("MACROS:");
               Enumeration e = applicationProps.propertyNames();
               while (e.hasMoreElements()) {
                   String key = (String) e.nextElement();
                   if (key.startsWith("macro."))
                       System.out.println(key + ": " + applicationProps.getProperty(key));
               }
               System.exit(0);
           } else {
               sql = applicationProps.getProperty("macro." + sqlMacro, "").trim();
               int j = 1;
               while (i < argv.length)
                   sql = sql.replaceAll("\\$" + j++ + "\\$", argv[i++].trim());
           }
       } else {
           if (i < argv.length)
               sql = argv[i++].trim();
       }

       if (i < argv.length) {
           System.err.println("Error: Too many arguments.");
       }

       
       if (gzip && outFilename == null) {
      	 Jdbexp.exit(1, "Error: gzip option only makes sense with out option.");
       }

       if (sqlFile && sql != null) {
           System.err.println("Error: Either SQLFILE or SQL Statement must be provided.");
       }

       if ((!sqlFile && sql == null) || (sqlFile && sql != null) || (i < argv.length)) {
           ShortHelp();
           System.exit(1);
       }

       if (dump || inserts) {
           Pattern pattern = Pattern.compile("^select\\s+.*\\sfrom\\s+(\\S+)", Pattern.CASE_INSENSITIVE);
           Matcher matcher = pattern.matcher(sql.replaceAll("\\s*,\\s*", ","));
           if (matcher.find()) {
               tableName = matcher.group(1);
               if (tableName.split("\\W").length != 1) {
               	Jdbexp.exit(1, "Error: SQL Error - Multiple tables (" + tableName + ") specified?");
               }
           } else {
         	  Jdbexp.exit(1, "Error: SQL Error - No table specified?");
           }
       }

       if (csv && label) {
      	 Jdbexp.exit(1, "Error: Use either label or csv option.");
       }

       if (header && label) {
      	 Jdbexp.exit(1, "Error: Use either label or header option.");
       }

       String jdbcDriverClass = System.getProperty("jdbc.driver");

       if (jdbcDriverClass == null)
           jdbcDriverClass = applicationProps.getProperty("jdbc.driver", "oracle.jdbc.driver.OracleDriver");

       try {
           Class.forName(jdbcDriverClass);
       } catch (ClassNotFoundException e) {
      	 Jdbexp.exit(-1, e.toString());
       }

       if (jdbcUrl == null) {
           if (dbalias == null)
               dbalias = applicationProps.getProperty("jdbc.url.DEFAULT");
           if (dbalias != null) {
               jdbcUrl = applicationProps.getProperty("jdbc.url." + dbalias);
               if (jdbcUrl == null) {
               	Jdbexp.exit(1, "ERROR: DB Alias: < " + dbalias + " > not found!");

               }
           }
           if (jdbcUrl == null)
               jdbcUrl = System.getProperty("jdbc.url");

           if (jdbcUrl == null)
               jdbcUrl = applicationProps.getProperty("jdbc.url");
       }
   }
}
