// $Id: Config.java 14499 2010-12-06 11:11:52Z kianusch $

package com.agfa.db.tools;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

class Config {
	private String fieldSeperator = ";";

	private String recordSeperator = "";

	private String textDelimitor = "";

	private String jdbcUrl = null;

	private String sql = null;

	private String sqlFilename = null;

	private boolean dump = false;

	private boolean insert = false;

	private boolean debug = false;

	private String tableName = null;

	private boolean header = false;

	private boolean csv = false;

	private boolean label = false;

	private boolean displayLobs = false;

	private boolean sqlFile = false;

	private boolean ignoreSqlError = false;

	private long commitInterval = -1;

	private int dbType = Jdbexp.DB_TYPES_UNKNOWN;

	private String xsl = null;

	public void setDbType(String s) {
		if (s.equalsIgnoreCase("ORACLE"))
			this.dbType = Jdbexp.DB_TYPES_ORACLE;
		else if (s.equalsIgnoreCase("MYSQL"))
			this.dbType = Jdbexp.DB_TYPES_MYSQL;
		else
			this.dbType = Jdbexp.DB_TYPES_UNKNOWN;
	}

	public int getDbType() {
		return this.dbType;
	}

	//
	public void setCommitInterval(long l) {
		this.commitInterval = l;
	}

	public long getCommitInterval() {
		return this.commitInterval;
	}

	//
	public void setFieldSeperator(String s) {
		this.fieldSeperator = s;
	}

	public String getFieldSeperator() {
		return this.fieldSeperator;
	}

	//
	public void setDelimitor(String s) {
		this.textDelimitor = s;
	}

	public String getDelimitor() {
		return this.textDelimitor;
	}

	//
	public String setJdbcUrl(String s) {
		this.jdbcUrl = s;
		return s;
	}

	public String getJdbcUrl() {
		return this.jdbcUrl;
	}

	//
	public void setRecordSeperator(String s) {
		this.recordSeperator = s;
	}

	public String getRecordSeperator() {
		return this.recordSeperator;
	}

	//
	public void setDisplayLobs(boolean b) {
		this.debug = displayLobs;
	}

	public boolean isDisplayLobs() {
		return this.displayLobs;
	}

	//
	public void setDebug(boolean b) {
		this.debug = b;
	}

	public boolean isDebug() {
		return this.debug;
	}

	//
	public void setHeader(boolean b) {
		this.header = b;
	}

	public boolean isHeader() {
		return this.header;
	}

	public void setXSL(String s) {
		this.xsl = s;
	}

	public String getXSL() {
		return this.xsl;
	}

	//
	public void setLabel(boolean b) {
		this.label = b;
		this.fieldSeperator = nl;
		this.recordSeperator = nl;
	}

	public boolean isLabel() {
		return this.label;
	}

	//
	public void setCsv(Boolean b) {
		this.csv = b;
		this.header = true;
		this.textDelimitor = "\"";
		this.fieldSeperator = ";";
	}

	public boolean isCsv() {
		return this.csv;
	}

	//
	public void setDump(boolean b, String s) {
		dump = b;
		tableName = s;
		textDelimitor = "\'";
		fieldSeperator = ",";
		recordSeperator = ";";
	}

	public boolean isDump() {
		return dump;
	}

	//
	public void setInsert(boolean b, String s) {
		insert = b;
		tableName = s;
		textDelimitor = "\'";
		fieldSeperator = ",";
	}

	public boolean isInsert() {
		return insert;
	}

	//
	public String setTableName(String s) {
		this.tableName = s;
		return s;
	}

	public String getTableName() {
		return this.tableName;
	}

	//
	public void setSqlFile(String s, boolean b, boolean i) {
		this.sqlFilename = s;
		this.sqlFile = b;
		this.ignoreSqlError = b;
	}

	public String getSqlFilename() {
		return this.sqlFilename;
	}

	public boolean isSqlFile() {
		return this.sqlFile;
	}

	public boolean isIgnoreSqlError() {
		return this.ignoreSqlError;
	}

	//
	public void setSql(String s) {
		this.sql = s;
	}

	public String getSql() {
		return this.sql;
	}

	public boolean isSql() {
		return this.sql != null;
	}

	//

	String nl = System.getProperty("line.separator");

	static SimpleDateFormat fTimeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	static SimpleDateFormat fTime = new SimpleDateFormat("HH:mm:ss");

	static SimpleDateFormat fDate = new SimpleDateFormat("yyyy-MM-dd");

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
		System.out.println("MYSQL: jdbc:mysql://<host>/<database>[?user=<username>&password=<password>]");
		System.out.println();
		System.out.println("             example: * jdbc:mysql://dbhost/dbname?user=dbuser&password=dbpw");
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

	public void ParseCommandLine(String[] argv) {
		Properties defaultProps = new Properties();

		try {
			defaultProps.load(ClassLoader.getSystemResourceAsStream("com/agfa/db/tools/jdbexp.properties"));
		} catch (Exception e) {
			Jdbexp.exit(1, "I/O failed.");
		}

		Properties applicationProps = new Properties(defaultProps);

		File tmp = null;

		try {
			tmp = new File("/etc/jdbexp.properties");
			if (tmp.exists() && tmp.isFile() && tmp.canRead())
				applicationProps.load(new FileInputStream(tmp));

			tmp = new File("/etc/jdb.properties");
			if (tmp.exists() && tmp.isFile() && tmp.canRead())
				applicationProps.load(new FileInputStream(tmp));

			// IMPAX EE
			tmp = new File("/impax/config/db.properties");
			if (tmp.exists() && tmp.isFile() && tmp.canRead())
				applicationProps.load(new FileInputStream(tmp));
		} catch (Exception e) {
			Jdbexp.exit(1, "Properties: Can't find " + tmp);
		}

		String sqlMacro = null;
		String outFilename = null;
		String errFilename = null;

		try {
			CommandLineParser parser = new PosixParser();

			// create the Options
			Options options = new Options();

			options.addOption("h", "help", false, "print this message");
			options.addOption("v", "version", false, "version information");

			options.addOption(OptionBuilder.withLongOpt("delimiter").withDescription("field delimiter").hasArg()
					.withArgName("DELIMITER").create("d"));
			options.addOption(OptionBuilder.withLongOpt("commit").withDescription("commit after <COMMIT> statements")
					.hasArg().withArgName("COMMIT").create());
			options.addOption(OptionBuilder.withLongOpt("macro").withDescription("exec macro").hasArg().withArgName(
					"MACRO | list").create("M"));
			options.addOption(OptionBuilder.withLongOpt("out").withDescription("output stdout to FILE").hasArg()
					.withArgName("FILE").create("O"));
			options.addOption(OptionBuilder.withLongOpt("gzip").withDescription("compress output").create("z"));
			options.addOption(OptionBuilder.withLongOpt("err").withDescription("output stderr to FILE").hasArg()
					.withArgName("FILE").create("E"));
			options.addOption(OptionBuilder.withLongOpt("debug").withDescription("output debug").create());

			// URL
			options.addOption(OptionBuilder.withLongOpt("url").withDescription("jdbc url").hasArg().withArgName(
					"JDBCURL | help").create("U"));
			//
			options.addOption(OptionBuilder.withLongOpt("db").withDescription("DB alias").hasArg().withArgName(
					"ALIAS | list").create());

			options.addOption(OptionBuilder.withLongOpt("jdbcurlhelp").withDescription("print URL help").create());
			options
					.addOption(OptionBuilder.withLongOpt("displaylobs").withDescription("display LOB's as base64").create());
			options.addOption(OptionBuilder.withLongOpt("ignoresqlerrors").withDescription("ignore SQL errors").create());
			options.addOption(OptionBuilder.withLongOpt("sqlfile").withDescription("read statemtents from sqlfile")
					.hasArg().withArgName("FILE").create());

			options.addOption(OptionBuilder.withLongOpt("xsl").withDescription("transform via xsl").hasArg().withArgName(
					"FILE").create());

			OptionGroup displayGroup = new OptionGroup();
			displayGroup.addOption(OptionBuilder.withLongOpt("label").withDescription("display labels").create("L"));
			displayGroup.addOption(OptionBuilder.withLongOpt("header").withDescription("display header").create("H"));
			displayGroup.addOption(OptionBuilder.withLongOpt("csv").withDescription("display as csv").create("csv"));
			displayGroup.addOption(OptionBuilder.withLongOpt("insert").withDescription("display with insert statements")
					.create("insert"));
			displayGroup.addOption(OptionBuilder.withLongOpt("dump").withDescription("display as dump").create("dump"));
			options.addOptionGroup(displayGroup);

			// parse the command line arguments
			CommandLine line = parser.parse(options, argv);
			argv = line.getArgs();
			int i = 0;

			if (line.hasOption("debug"))
				this.debug = true;
			if (line.hasOption("displaylobs"))
				this.displayLobs = true;
			if (line.hasOption("commit")) {
				this.commitInterval = Long.parseLong(line.getOptionValue("commit").trim());
			}

			if (line.hasOption("xsl"))
				setXSL(line.getOptionValue("xsl").trim());

			if (line.hasOption("csv"))
				setCsv(true);

			if (line.hasOption("insert"))
				setInsert(true, null);

			if (line.hasOption("dump"))
				setDump(true, null);

			if (line.hasOption("H"))
				setHeader(true);

			if (line.hasOption("L"))
				setLabel(true);

			// MACRO
			if (line.hasOption("M")) {
				sqlMacro = line.getOptionValue("M");

				if (sqlMacro.equalsIgnoreCase("list")) {
					System.out.println("Defined macros:");
					Enumeration e = applicationProps.propertyNames();
					while (e.hasMoreElements()) {
						String key = (String) e.nextElement();
						if (key.startsWith("macro."))
							System.out.println("   " + key.substring(6) + ": <" + applicationProps.getProperty(key) + ">");
					}
					System.exit(0);
				} else {
					String myMACRO = applicationProps.getProperty("macro." + sqlMacro, "").trim();
					int j = 1;
					while (i < argv.length)
						myMACRO = myMACRO.replaceAll("\\$" + j++ + "\\$", argv[i++].trim());
					setSql(myMACRO);
				}
			}

			if (line.hasOption("d"))
				setFieldSeperator(line.getOptionValue("d"));

			if (line.hasOption("sqlfile"))
				setSqlFile(line.getOptionValue("sqlfile"), true, line.hasOption("ignoresqlerrors"));

			if (line.hasOption("h")) {
				// automatically generate the help statement
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("java -jar jdbexp.jar [options] <\"sql statement\">", options);
				System.exit(0);
			}

			if (line.hasOption("jdbcurlhelp")) {
				UrlHelp();
				System.exit(0);
			}

			if (line.hasOption("v")) {
				System.out.println("Jdbexp Version: " + Jdbexp.VERSION);
				System.out.println(Jdbexp.ID);
				System.out.println(Jdbexp.REVISION);
				System.exit(0);
			}

			if (line.hasOption("E")) {
				errFilename = line.getOptionValue("E");
				try {
					System.setErr(new PrintStream(new BufferedOutputStream(new FileOutputStream(errFilename))));
				} catch (Exception e) {
					Jdbexp.exit(1, e.toString());
				}
			}

			if (line.hasOption("O")) {
				outFilename = line.getOptionValue("O");
				try {
					if (line.hasOption("z")) {
						System.setOut(new PrintStream(new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(
								outFilename)))));
					} else {
						System.setOut(new PrintStream(new BufferedOutputStream(new FileOutputStream(outFilename))));
					}
				} catch (Exception e) {
					Jdbexp.exit(1, e.toString());
				}
			}

			// db alias
			if (line.hasOption("db")) {
				if (line.getOptionValue("db").equalsIgnoreCase("list")) {
					System.out.println("Defined DB connections:");
					Enumeration e = applicationProps.propertyNames();
					while (e.hasMoreElements()) {
						String key = (String) e.nextElement();
						if (key.equals("jdbc.url"))
							System.out.println("   DEFAULT <" + applicationProps.getProperty(key) + ">");
						if (key.startsWith("jdbc.url.")) {
							System.out.println("   " + key.substring(9) + " <" + applicationProps.getProperty(key) + ">");
						}
					}
					System.exit(0);
				} else if (setJdbcUrl(applicationProps.getProperty("jdbc.url." + line.getOptionValue("db"))) == null) {
					Jdbexp.exit(1, "ERROR: DB Alias: < " + line.getOptionValue("db") + " > not found!");
				}
			}

			if (!isSql() && i < argv.length)
				setSql(argv[i++].trim());

			// jdbc url
			if (line.hasOption("U")) {
				if (line.getOptionValue("U").equalsIgnoreCase("help")) {
					UrlHelp();
					System.exit(0);
				}

				setJdbcUrl(line.getOptionValue("U"));
			}

			if (getJdbcUrl() == null)
				if (setJdbcUrl(System.getProperty("jdbc.url")) == null) {
					String s = applicationProps.getProperty("jdbc.url");
					if (s.startsWith("$")) {
						String s1 = applicationProps.getProperty("jdbc.url." + s.substring(1));
						if (s1 == null) {
							Jdbexp.exit(1, "ERROR: DB Alias: < " + s.substring(1) + " > not found!");
						}
						s = s1;
					}
					if (setJdbcUrl(s) == null) {
						Jdbexp.exit(1, "ERROR: Missing JDBC Url.");
					}
				}

			String jdbcDriverClass = System.getProperty("jdbc.driver");

			if (getJdbcUrl().startsWith("{") && getJdbcUrl().contains("}")) {
				String tmpJdbcUrl = getJdbcUrl();
				int pos = tmpJdbcUrl.indexOf("}");
				jdbcDriverClass = tmpJdbcUrl.substring(1, pos);
				setJdbcUrl(tmpJdbcUrl.substring(pos + 1));
			}

			if (jdbcDriverClass == null)
				jdbcDriverClass = applicationProps.getProperty("jdbc.driver", "oracle.jdbc.driver.OracleDriver");

			try {
				Class.forName(jdbcDriverClass);
			} catch (ClassNotFoundException e) {
				Jdbexp.exit(-1, e.toString());
			}

			if (i < argv.length)
				System.err.println("Error: Too many arguments.");

			if (isSqlFile() && isSql()) {
				System.err.println("Error: Either SQLFILE or SQL Statement must be provided.");
			}

			if ((!isSqlFile() && !isSql()) || (isSqlFile() && isSql()) || (i < argv.length)) {
				ShortHelp();
				System.exit(1);
			}

			if (isDump() || isInsert()) {
				String tmpSQL = getSql();
				if (isDebug())
					System.err.println("DEBUG: Dumping SQL CMD " + tmpSQL);
				Pattern pattern = Pattern.compile("^select\\s+.*\\sfrom\\s+(\\S+)", Pattern.CASE_INSENSITIVE);
				Matcher matcher = pattern.matcher(tmpSQL.replaceAll("\\s*,\\s*", ","));
				if (matcher.find()) {
					String tmpTN = matcher.group(1);
					if (tmpTN.split("\\W").length != 1)
						Jdbexp.exit(1, "Error: SQL Error - Multiple tables (" + tmpTN + ") specified?");
					setTableName(tmpTN);
					if (isDebug())
						System.err.println("DEBUG: Dumping Table " + tmpTN);
				} else {
					Jdbexp.exit(1, "Error: SQL Error - No table specified?");
				}
			}
		} catch (ParseException exp) {
			Jdbexp.exit(1, "Unexpected exception:" + exp.getMessage());
		}
	}
}
