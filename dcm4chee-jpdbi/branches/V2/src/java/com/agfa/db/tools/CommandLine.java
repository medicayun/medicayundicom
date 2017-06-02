// $Id: CommandLine.java 13137 2010-04-10 11:07:29Z kianusch $

package com.agfa.db.tools;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Properties;
import java.util.zip.GZIPOutputStream;

class CommandLine {
    BitSet levels = new BitSet();

    BitSet displayDS = new BitSet();

    BitSet updateDS = new BitSet();

    public ArrayList<String> update = new ArrayList<String>();

    public ArrayList<String> extendedquery = new ArrayList<String>();

    boolean displayPKS = false;

    boolean displayFields = false;

    boolean displayAET = false;

    public boolean displayFSInfo = false;

    boolean debug = false;

    boolean expert = false;

    public long UpdCount = 0;

    String jdbcUrl = null;

    public int Database = DBTypes.UNKNOWN;

    String nl = System.getProperty("line.separator");

    final static SimpleDateFormat fTimeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    final static SimpleDateFormat fTime = new SimpleDateFormat("HH:mm:ss");

    final static SimpleDateFormat fDate = new SimpleDateFormat("yyyy-MM-dd");

    // PATIENT LEVEL
    public String FirstName = null;

    public String LastName = null;

    public String BirthDate = null;

    public String PatID = null;

    public String PatIssuer = null;

    // STUDY
    public String StudyIUID = null;

    public String StudyDATE = null;

    // SERIES
    public String SeriesIUID = null;

    public String SeriesMOD = null;

    public boolean pre214 = false;

    public boolean nonempty = false;

    Properties applicationProps = null;

    public static void Url() {
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

    public static void Help() {
        System.out.println("Usage: java -jar jpdbi [options] [<LastName> [<FirstName> [<Brithdate>]]]");
        System.out.println("       java -jar jpdbi -h | --help");
        System.out.println("       java -jar jpdbi -v | --version");
        System.out.println();
        System.out.println("        -v|--version                   display version information");
        System.out.println("        -h|--help                      display extended help");
        System.out.println();
        System.out.println("        -U|--url <jdbcURL>             DB jdbcURL to connect");
        System.out.println("           --db <alias>                DB alias to connect");
        System.out.println();
        System.out.println("        Search options:");
        System.out.println("          -s|--studyiuid <iuid>        Study-IUID");
        System.out.println("          -S|--seriesiuid <iuid>       Series-IUID");
        System.out.println("          --issuer <ISSUER>            ISSUER");
        System.out.println("          --patid [ISSUER:]PATID       (ISSUER and) Patient-ID");
        System.out.println("          -d|--date [<date>|<n>]       StudyDate or n-past days");
        System.out.println("          -m|--modality <MODALITY>     Modality");
        System.out.println();
        System.out.println("          -q|--query <statement>       Extended query");
        System.out.println();
        System.out.println("        Display Options:");
        System.out.println("          -F                           include fieldnames");
        System.out.println("          --aet                        AET's");
        System.out.println("          --pks                        primary keys");
        System.out.println("          --study-level                patient+study");
        System.out.println("          --series-level                +series)");
        System.out.println("          --instance-level               +instance");
        System.out.println("          --path [--fs]                    +path [+FS information (FS-PK, Group)]");
        System.out.println("          --ignorenonempty             ignore patients with no studies/series/...");
        System.out.println();
        System.out.println("        Output Options:");
        System.out.println("          -O|--out <file> [-z|--gzip]   output to (gziped) <file>");
        System.out.println("          -E|--err <file>               error to <file>");
        System.out.println("");
        System.out.println("        --jdbcurlhelp                   jdbcURL Syntax");
        System.out.println();
        System.out.println("        --debug                         debug mode");
        System.out.println();
        System.out.println("        Use % as wildcard statement");
        System.out.println();
    }

    void setDatabase(Connection conn) throws SQLException {
        DatabaseMetaData dmd = conn.getMetaData();

        if (dmd.getDatabaseProductName().equalsIgnoreCase("ORACLE")) {
            Database = DBTypes.ORACLE;
        } else if (dmd.getDatabaseProductName().equalsIgnoreCase("MYSQL")) {
            Database = DBTypes.MYSQL;
        } else {
            Database = DBTypes.UNKNOWN;
        }
    }

    CommandLine(String[] argv) {
        Properties defaultProps = new Properties();

        try {
            defaultProps.load(ClassLoader.getSystemResourceAsStream("com/agfa/db/tools/jpdbi.properties"));
        } catch (Exception e) {
            _System.exit(1, "I/O failed.");
        }

        applicationProps = new Properties(defaultProps);

        File tmp = null;

        tmp = new File("/etc/jpdbi.properties");
        if (tmp.exists() && tmp.isFile() && tmp.canRead()) {
            try {
                applicationProps.load(new FileInputStream(tmp));
            } catch (Exception e) {
                _System.exit(1, "Can't find " + tmp);
            }
        }
        tmp = new File("/etc/jdb.properties");
        if (tmp.exists() && tmp.isFile() && tmp.canRead()) {
            try {
                applicationProps.load(new FileInputStream(tmp));
            } catch (Exception e) {
                _System.exit(1, "Can't find " + tmp);
            }
        }

        String dbalias = null;
        String outFilename = null;
        String errFilename = null;

        boolean gzip = false;

        final int OPT_DEBUG = 1;
        final int OPT_URLHELP = 2;
        final int OPT_DBALIAS = 3;
        final int OPT_PATID = 4;
        final int OPT_PATISSUER = 5;
        final int OPT_EXPERT = 6;
        final int OPT_UPDCOUNT = 7;

        final int OPT_DISPLAYPKS = 8;
        final int OPT_PATH = 9;
        final int OPT_DISPLAYAET = 10;
        final int OPT_FSINFO = 11;
        final int OPT_UPDATE = 12;
        final int OPT_NONEMPTY = 13;

        //
        final int OPT_STUDYLEVEL = 30;
        final int OPT_SERIESLEVEL = 31;
        final int OPT_SOPLEVEL = 32;

        //
        final int OPT_DS = 20;
        final int OPT_PATDS = 21;
        final int OPT_STYDS = 22;
        final int OPT_SERDS = 23;
        final int OPT_SOPDS = 24;

        int version = 0;

        final LongOpt[] longopts = {
                // DataSets
                new LongOpt("patds", LongOpt.NO_ARGUMENT, null, OPT_PATDS),
                new LongOpt("studyds", LongOpt.NO_ARGUMENT, null, OPT_STYDS),
                new LongOpt("seriesds", LongOpt.NO_ARGUMENT, null, OPT_SERDS),
                new LongOpt("sopds", LongOpt.NO_ARGUMENT, null, OPT_SOPDS),

                // Output
                new LongOpt("gzip", LongOpt.NO_ARGUMENT, null, 'z'),
                new LongOpt("out", LongOpt.REQUIRED_ARGUMENT, null, 'O'),
                new LongOpt("err", LongOpt.REQUIRED_ARGUMENT, null, 'E'),

                //
                new LongOpt("url", LongOpt.REQUIRED_ARGUMENT, null, 'U'),
                new LongOpt("db", LongOpt.REQUIRED_ARGUMENT, null, OPT_DBALIAS),

                //
                new LongOpt("pks", LongOpt.NO_ARGUMENT, null, OPT_DISPLAYPKS),
                new LongOpt("ignorenonempty", LongOpt.NO_ARGUMENT, null, OPT_NONEMPTY),
                new LongOpt("aet", LongOpt.NO_ARGUMENT, null, OPT_DISPLAYAET),

                //
                new LongOpt("query", LongOpt.REQUIRED_ARGUMENT, null, 'q'),

                // UPDATE
                new LongOpt("update", LongOpt.REQUIRED_ARGUMENT, null, OPT_UPDATE),
                new LongOpt("count", LongOpt.REQUIRED_ARGUMENT, null, OPT_UPDCOUNT),

                // PATIENT LEVEL - FirstName, LastName, BirthDate
                new LongOpt("patid", LongOpt.REQUIRED_ARGUMENT, null, OPT_PATID),
                new LongOpt("issuer", LongOpt.REQUIRED_ARGUMENT, null, OPT_PATISSUER),

                // STUDY LEVEL
                new LongOpt("studyiuid", LongOpt.REQUIRED_ARGUMENT, null, 's'),
                new LongOpt("study-level", LongOpt.NO_ARGUMENT, null, OPT_STUDYLEVEL),
                new LongOpt("date", LongOpt.REQUIRED_ARGUMENT, null, 'd'),

                // SERIES LEVEL
                new LongOpt("seriesiuid", LongOpt.REQUIRED_ARGUMENT, null, 'S'),
                new LongOpt("modality", LongOpt.REQUIRED_ARGUMENT, null, 'm'),
                new LongOpt("series-level", LongOpt.NO_ARGUMENT, null, OPT_SERIESLEVEL),

                // INSTANCE LEVEL
                new LongOpt("instance-level", LongOpt.NO_ARGUMENT, null, OPT_SOPLEVEL),
                new LongOpt("sop-level", LongOpt.NO_ARGUMENT, null, OPT_SOPLEVEL),

                // PATH
                new LongOpt("path", LongOpt.NO_ARGUMENT, null, OPT_PATH),
                new LongOpt("fs", LongOpt.NO_ARGUMENT, null, OPT_FSINFO),

                //
                new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h'),
                new LongOpt("jdbcurlhelp", LongOpt.NO_ARGUMENT, null, OPT_URLHELP),
                new LongOpt("debug", LongOpt.NO_ARGUMENT, null, OPT_DEBUG),
                new LongOpt("version", LongOpt.NO_ARGUMENT, null, 'v'),
                new LongOpt("ThisOptionDoesNotExist", LongOpt.NO_ARGUMENT, null, OPT_EXPERT),
        //
        };

        Getopt g = new Getopt("jpdbi", argv, ":U:m:E:O:s:S:q:q:hvzLF", longopts);
        g.setOpterr(true);

        int c;

        while ((c = g.getopt()) != -1)
            switch (c) {
            case OPT_DEBUG:
                debug = true;
                break;

            case 'h':
                Help();
                System.exit(0);
                break;

            case OPT_URLHELP:
                Url();
                System.exit(0);
                break;

            case 'U':
                jdbcUrl = g.getOptarg();
                break;

            case OPT_DBALIAS:
                dbalias = g.getOptarg();
                break;

            case 'v':
                version++;
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

            case 's':
                StudyIUID = g.getOptarg();
                break;

            case 'd':
                StudyDATE = g.getOptarg();
                break;

            case 'S':
                SeriesIUID = g.getOptarg();
                break;

            case OPT_PATID:
                PatID = g.getOptarg();
                break;

            case OPT_PATISSUER:
                PatIssuer = g.getOptarg();
                break;

            case 'm':
                SeriesMOD = g.getOptarg();
                break;

            case 'L':
            case 'F':
                displayFields = true;
                break;

            case OPT_DISPLAYPKS:
                displayPKS = true;
                break;

            case OPT_DISPLAYAET:
                displayAET = true;
                break;

            case OPT_NONEMPTY:
                nonempty = true;
                break;

            case OPT_EXPERT:
                expert = true;
                break;

            case OPT_UPDCOUNT:
                UpdCount = Long.parseLong(g.getOptarg());
                break;

            case OPT_STUDYLEVEL:
                levels.set(Jpdbi.STUDY);
                break;

            case OPT_SERIESLEVEL:
                levels.set(Jpdbi.SERIE);
                break;

            case OPT_SOPLEVEL:
                levels.set(Jpdbi.INSTANCE);
                break;

            case OPT_PATH:
                levels.set(Jpdbi.PATH);
                break;

            case OPT_FSINFO:
                displayFSInfo = true;
                break;

            case OPT_UPDATE:
                update.add(g.getOptarg());
                break;

            case 'q':
                extendedquery.add(g.getOptarg());
                break;

            case OPT_PATDS:
                displayDS.set(Jpdbi.PATIENT);
                break;

            case OPT_STYDS:
                displayDS.set(Jpdbi.STUDY);
                break;

            case OPT_SERDS:
                displayDS.set(Jpdbi.SERIE);
                break;

            case OPT_SOPDS:
                displayDS.set(Jpdbi.INSTANCE);
                break;

            default:
                _System.exit(1, "Error encountered during Argument Parsing." + nl + "Use -h for help.");
                break;
            }

        int i = g.getOptind();

        if (version > 0) {
            String[] R = Jpdbi.REVISION.split(" ");

            if (version == 1)
                System.err.println(Jpdbi.VERSION + "." + R[1]);

            if (version > 1)
                System.err.println("JPDBI Version: " + Jpdbi.VERSION + "." + R[1]);

            if (version > 2) {
                System.out.println(Jpdbi.ID);
                System.out.println(Jpdbi.REVISION);
            }
            System.exit(0);
        }

        if (errFilename != null) {
            try {
                System.setErr(new PrintStream(new BufferedOutputStream(new FileOutputStream(errFilename))));
            } catch (Exception e) {
                _System.exit(1, e.toString());
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
                _System.exit(1, e.toString());
            }
        }

        if (i < argv.length)
            LastName = argv[i++].trim();
        if (i < argv.length)
            FirstName = argv[i++].trim();
        if (i < argv.length)
            BirthDate = argv[i++].trim();

        /*
         * if (i < argv.length) sql = argv[i++].trim(); }
         */

        if (i < argv.length) {
            System.err.println("Error: Too many arguments.");
        }

        if (gzip && outFilename == null) {
            _System.exit(1, "Error: gzip option only makes sense with out option.");
        }

        if (i < argv.length) {
            Help();
            System.exit(1);
        }

        String jdbcDriverClass = System.getProperty("jdbc.driver");

        if (jdbcDriverClass == null)
            jdbcDriverClass = applicationProps.getProperty("jdbc.driver", "oracle.jdbc.driver.OracleDriver");

        try {
            Class.forName(jdbcDriverClass);
        } catch (ClassNotFoundException e) {
            _System.exit(-1, e.toString());
        }

        if (jdbcUrl == null) {
            if (dbalias == null)
                dbalias = applicationProps.getProperty("jdbc.url.DEFAULT");
            if (dbalias != null) {
                jdbcUrl = applicationProps.getProperty("jdbc.url." + dbalias);
                if (jdbcUrl == null) {
                    _System.exit(1, "ERROR: DB Alias: < " + dbalias + " > not found!");

                }
            }
            if (jdbcUrl == null)
                jdbcUrl = System.getProperty("jdbc.url");

            if (jdbcUrl == null)
                jdbcUrl = applicationProps.getProperty("jdbc.url");
        }
    }
}