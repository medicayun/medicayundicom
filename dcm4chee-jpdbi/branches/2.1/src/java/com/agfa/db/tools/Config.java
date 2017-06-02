// $Id: Config.java 13176 2010-04-14 17:21:26Z kianusch $

package com.agfa.db.tools;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
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
	private static boolean isLongNumber(String num) {
		try {
			Long.parseLong(num);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	private static String addWhere(String w, String f, String v, int t) {
		return addWhere(w, f, v, t, false);
	}

	private static String addWhere(String w, String f, String v, int t, boolean u) {
		if (v == null)
			return w;

		if (w.length() > 0)
			w += " and ";

		if (v.length() == 0) {
			w += f + " is NULL";
		} else {
			if (u)
				w += "upper(" + f + ")";
			else
				w += f;

			switch (t) {
			case Types.VARCHAR:
				if (v.indexOf('%') > -1 || v.indexOf('_') > -1)
					w += " like ";
				else
					w += "=";

				if (u)
					w += "upper('" + v + "')";
				else
					w += "'" + v + "'";
				break;
			case Types.DATE:
				w += "={d '" + v + "'}";
				break;
			case Types.TIMESTAMP:
				w += "={ts '" + v + "'}";
				break;
			}
		}

		return w;
	}

	private static String PrependSql(String s, String c) {
		if (c.length() > 0)
			c = "," + c;
		c = s + c;
		return c;
	}

	//
	private static String[][] updDicom = null;

	public static String[][] setUpdDicom(String[][] updDicom1) {
		updDicom = updDicom1;
		return updDicom;
	}

	public String[][] getUpdDicom() {
		return updDicom;
	}

	//
	private static String[] sqlPortions = new String[] { null, null, null, null, null, null, null };

	public static String[] setSqlPortions(String select, String from, String join, String links, String where,
			String group, String order) {
		sqlPortions[0] = select;
		sqlPortions[1] = from;
		sqlPortions[2] = join;
		sqlPortions[3] = links;
		sqlPortions[4] = where;
		sqlPortions[5] = group;
		sqlPortions[6] = order;

		return sqlPortions;
	}

	public String[] getSqlPortions() {
		return sqlPortions;
	}

	//

	private static Properties props = null;

	public void setProps(Properties propsL) {
		props = propsL;
	}

	public static Properties getProps() {
		return props;
	}

	private static boolean debug = false;

	//
	public void setDebug(boolean b) {
		debug = b;
	}

	public boolean isDebug() {
		return debug;
	}

	//
	private static boolean displayStatus = false;

	public static boolean isDisplayStatus() {
		return displayStatus;
	}

	public static void setDisplayStatus(boolean b) {
		displayStatus = b;
	}

	//
	private boolean displayFields = false;

	public void setDisplayFields(boolean b) {
		displayFields = b;
	}

	public boolean isDisplayFields() {
		return displayFields;
	}

	//
	private int dbType = Jpdbi.DBTYPE_UNKNOWN;

	public void setDbType(String s) {
		if (s.equalsIgnoreCase("ORACLE"))
			dbType = Jpdbi.DBTYPE_ORACLE;
		else if (s.equalsIgnoreCase("MYSQL"))
			dbType = Jpdbi.DBTYPE_MYSQL;
		else
			dbType = Jpdbi.DBTYPE_UNKNOWN;
	}

	public int getDbType() {
		return dbType;
	}

	//
	private String jdbcUrl = null;

	public String setJdbcUrl(String s) {
		jdbcUrl = s;
		return s;
	}

	public String getJdbcUrl() {
		return jdbcUrl;
	}

	//
	private static String patientName = null;

	public String setPatientName(String Lastname, String Firstname) {
		if (Lastname == null)
			Lastname = "%";

		if (Firstname == null)
			Firstname = "%";

		patientName = Lastname + "^" + Firstname + "^%^%";
		return patientName;
	}

	public String setPatientName(String patientNameL) {
		patientName = patientNameL;
		return patientName;
	}

	public static String getPatientName() {
		return patientName;
	}

	//
	private static String patientBirthDate = null;

	public String setPatientBirthDate(String patientBirthDateL) {
		patientBirthDate = patientBirthDateL;
		return patientBirthDate;
	}

	public static String getPatientBirthDate() {
		return patientBirthDate;
	}

	//
	private static String modality = null;

	public String setModality(String s) {
		modality = s;
		return s;
	}

	public static String getModality() {
		return modality;
	}

	//
	private static String studyIuid = null;

	public static String getStudyIuid() {
		return studyIuid;
	}

	public void setStudyIuid(String studyIuidL) {
		studyIuid = studyIuidL;
	}

	private static String seriesIuid = null;

	public static String getSeriesIuid() {
		return seriesIuid;
	}

	public void setSeriesIuid(String seriesIuidL) {
		seriesIuid = seriesIuidL;
	}

	private static String studyDate = null;

	public static String getStudyDate() {
		return studyDate;
	}

	public void setStudyDate(String studyDateL) {
		studyDate = studyDateL;
	}

	private static String patientId = null;

	private static String issuer = null;

	public static String getPatientId() {
		return patientId;
	}

	public String setPatientId(String patientIdL) {
		int split = patientIdL.indexOf(':');
		if (split == -1) {
			patientId = patientIdL;
		} else {
			issuer = patientIdL.substring(0, split);
			patientId = patientIdL.substring(split + 1);
		}
		return patientId;
	}

	public static String getIssuer() {
		return issuer;
	}

	public void setIssuer(String issuerL) {
		issuer = issuerL;
	}

	private boolean displayPKs = false;

	public boolean isDisplayPKs() {
		return displayPKs;
	}

	public void setDisplayPKs(boolean displayPKsL) {
		displayPKs = displayPKsL;
	}

	private static boolean displayAETs = false;

	public boolean isDisplayAETs() {
		return displayAETs;
	}

	public void setDisplayAETs(boolean displayAETsL) {
		displayAETs = displayAETsL;
	}

	private static boolean pre214 = false;

	public boolean isPre214() {
		return pre214;
	}

	public void setPre214(boolean pre214l) {
		pre214 = pre214l;
	}

	private boolean ignoreEmpty = true;

	public boolean isIgnoreEmpty() {
		return ignoreEmpty;
	}

	public void setIgnoreEmpty(boolean ignoreEmptyL) {
		ignoreEmpty = ignoreEmptyL;
	}

	private long updateCount = 1;

	public long getUpdateCount() {
		return updateCount;
	}

	public void setUpdateCount(long updateCountL) {
		updateCount = updateCountL;
	}

	private static String[] update = null;

	public static String[] getUpdate() {
		return update;
	}

	public void setUpdate(String[] updateL) {
		update = updateL;
	}

	private static String query = null;

	public static String getQuery() {
		return query;
	}

	public void setQuery(String query1) {
		query = query1;
	}

	private static BitSet displayDS = new BitSet();

	public static BitSet getDisplayDS() {
		return displayDS;
	}

	public boolean isDisplayDS(int displayDS1) {
		return displayDS.get(displayDS1);
	}

	public void setDisplayDS(int displayDS1) {
		displayDS.set(displayDS1);
	}

	private static BitSet updateDS = new BitSet();

	public BitSet getUpdateDS() {
		return updateDS;
	}

	public static boolean isUpdateDS(int updateDS1) {
		return updateDS.get(updateDS1);
	}

	public static void setUpdateDS(int updateDS1) {
		updateDS.set(updateDS1);
	}

	private static BitSet displayLevel = new BitSet();

	public BitSet getDisplayLevel() {
		return displayLevel;
	}

	public boolean isDisplayLevel(int displayLevel1) {
		return displayLevel.get(displayLevel1);
	}

	public static void setDisplayLevel(int displayLevel1) {
		displayLevel.set(displayLevel1);
	}

	private static BitSet updateLevel = new BitSet();

	public BitSet getUpdateLevel() {
		return updateLevel;
	}

	public boolean isUpdateLevel(int updateLevel1) {
		return updateLevel.get(updateLevel1);
	}

	public static void setUpdateLevel(int updateLevel1) {
		updateLevel.set(updateLevel1);
	}

	//
	private String uidBase = null;

	public String setUidBase(String s) {
		uidBase = s;
		return s;
	}

	public String getUidBase() {
		return uidBase;
	}

	String nl = System.getProperty("line.separator");

	static SimpleDateFormat fTimeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	static SimpleDateFormat fTime = new SimpleDateFormat("HH:mm:ss");

	static SimpleDateFormat fDate = new SimpleDateFormat("yyyy-MM-dd");

	public static void UidHelp() {
		System.out.println("UID Template:");
		System.out.println();
		System.out.println(" %Y ... year(4)*");
		System.out.println(" %m ... month(2)*");
		System.out.println(" %d ... day(2)*");
		System.out.println("");
		System.out.println(" %H ... hour(2)*");
		System.out.println(" %M ... minute(2)*");
		System.out.println(" %S ... second(2)*");
		System.out.println("");
		System.out.println(" %n ... milliseconds(3)*");
		System.out.println("");
		System.out.println(" %N ... nanoseconds");
		System.out.println(" %s ... epoche - seconds since 1970-01-01 00:00:00 UTC");
		System.out.println("");
		System.out.println(" %Z ... counter");
		System.out.println("");
		System.out.println(" *) padded with zeros");
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

	public void ParseCommandLine(String[] argv) {
		Properties defaultProps = new Properties();

		try {
			defaultProps.load(ClassLoader.getSystemResourceAsStream("com/agfa/db/tools/jpdbi.properties"));
		} catch (Exception e) {
			Jpdbi.exit(1, "I/O failed.");
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
			Jpdbi.exit(1, "Properties: Can't find " + tmp);
		}

		String outFilename = null;
		String errFilename = null;

		try {
			CommandLineParser parser = new PosixParser();

			// create the Options
			Options options = new Options();

			options.addOption("h", "help", false, "print this message");
			options.addOption("v", "version", false, "version information");
			options.addOption("F", false, "display field names");
			options.addOption("L", false, "display field names");

			options.addOption(OptionBuilder.withLongOpt("status").withDescription("display status, availability and more")
					.create());

			// DataSets
			options.addOption(OptionBuilder.withLongOpt("patientds").withDescription("print patient dataset").create());
			options.addOption(OptionBuilder.withLongOpt("studyds").withDescription("print study dataset").create());
			options.addOption(OptionBuilder.withLongOpt("seriesds").withDescription("print series dataset").create());
			options.addOption(OptionBuilder.withLongOpt("sopds").withDescription("print instance dataset").create());
			// Output
			options.addOption(OptionBuilder.withLongOpt("out").withDescription("output stdout to FILE").hasArg()
					.withArgName("FILE").create("O"));
			options.addOption(OptionBuilder.withLongOpt("gzip").withDescription("compress output").create("z"));
			options.addOption(OptionBuilder.withLongOpt("err").withDescription("output stderr to FILE").hasArg()
					.withArgName("FILE").create("E"));
			// URL
			options.addOption(OptionBuilder.withLongOpt("url").withDescription("jdbc url").hasArg().withArgName("JDBCURL | help")
					.create("U"));
			//
			options.addOption(OptionBuilder.withLongOpt("db").withDescription("DB alias").hasArg().withArgName(
					"ALIAS | list").create());
			//
			options.addOption(OptionBuilder.withLongOpt("uid").withDescription("UID template").hasArg().withArgName(
					"TEMPLATE").create());
			//
			options.addOption(OptionBuilder.withLongOpt("pks").withDescription("print primary keys").create());
			// options.addOption(OptionBuilder.withLongOpt("ignoreempty").withDescription("ignore patients with no studies/series").create("i"));
			options.addOption(OptionBuilder.withLongOpt("all").withDescription("display all results").create("a"));
			options.addOption(OptionBuilder.withLongOpt("aet").withDescription("print AE titles").create());
			//
			options.addOption(OptionBuilder.withLongOpt("debug").withDescription("output debug").create());
			options.addOption(OptionBuilder.withLongOpt("jdbcurlhelp").withDescription("print URL help").create());
			//
			options.addOption(OptionBuilder.withLongOpt("query").withDescription("extended query").hasArg().withArgName(
					"STATEMENT").create("q"));
			//
			options.addOption(OptionBuilder.withLongOpt("update").withDescription("update database and/or dataset")
					.hasArgs().withArgName("FILED=VALUE | list").create());
			options.addOption(OptionBuilder.withLongOpt("count").hasArg().withArgName("NUMBER").create());
			//
			options.addOption(OptionBuilder.withLongOpt("patid").withDescription("query patient ID").hasArg().withArgName(
					"[ISSUER:]PATID").create());
			options.addOption(OptionBuilder.withLongOpt("issuer").withDescription("query issuer").hasArg().withArgName(
					"ISSUER").create());
			//
			options.addOption(OptionBuilder.withLongOpt("studyiuid").withDescription("query study IUID").hasArg()
					.withArgName("STUDYIUID").create("s"));
			options.addOption(OptionBuilder.withLongOpt("date").withDescription("query study date").hasArg().withArgName(
					"YYYY-MM-DD | N").create("d"));
			//
			options.addOption(OptionBuilder.withLongOpt("seriesiuid").withDescription("query series IUID").hasArg()
					.withArgName("SERIESIUID").create("S"));
			options.addOption(OptionBuilder.withLongOpt("modality").withDescription("query modality").hasArg()
					.withArgName("MODALITY").create("m"));
			//
			options.addOption(OptionBuilder.withLongOpt("fs").withDescription("display FS group & PK").create());
			options.addOption(OptionBuilder.withLongOpt("pre214").withDescription("query pre214 archives").create());

			OptionGroup optionGroup = new OptionGroup();

			optionGroup.addOption(OptionBuilder.withLongOpt("patient-level").withDescription("display patient level")
					.create("l1"));
			optionGroup.addOption(OptionBuilder.withLongOpt("series-level").withDescription("display series level")
					.create("l3"));
			optionGroup.addOption(OptionBuilder.withLongOpt("study-level").withDescription("display study level").create(
					"l2"));
			optionGroup.addOption(OptionBuilder.withLongOpt("instance-level").withDescription("display instance level")
					.create("l4"));
			optionGroup.addOption(OptionBuilder.withLongOpt("path").withDescription("display object path").create("l5"));

			options.addOptionGroup(optionGroup);

			// options.addOption(OptionBuilder.withLongOpt("ThisOptionDoesNotExist").create());

			// parse the command line arguments
			CommandLine line = parser.parse(options, argv);
			argv = line.getArgs();
			int i = 0;

			if (line.hasOption("debug"))
				setDebug(true);

			/*
			 * if (line.hasOption("ignoreempty")) setIgnoreEmpty(true);
			 */
			if (line.hasOption("all"))
				setIgnoreEmpty(false);

			if (line.hasOption("F") || line.hasOption("L"))
				setDisplayFields(true);

			// Modality
			if (line.hasOption("m"))
				setModality(line.getOptionValue("m"));

			// StudyID
			if (line.hasOption("s"))
				setStudyIuid(line.getOptionValue("s"));

			// SeriesID
			if (line.hasOption("S"))
				setSeriesIuid(line.getOptionValue("S"));

			if (line.hasOption("d"))
				setStudyDate(line.getOptionValue("d"));

			if (line.hasOption("patid"))
				setPatientId(line.getOptionValue("patid"));

			if (line.hasOption("issuer"))
				setIssuer(line.getOptionValue("issuer"));

			if (line.hasOption("pks"))
				setDisplayPKs(true);

			if (line.hasOption("status"))
				setDisplayStatus(true);

			if (line.hasOption("aet"))
				setDisplayAETs(true);

			if (line.hasOption("pre214"))
				setPre214(true);

			if (line.hasOption("count"))
				setUpdateCount(Long.parseLong(line.getOptionValue("count")));

			/*
			 * if (line.hasOption("ThisOptionDoesNotExists")) setUpdateCount(-666);
			 */

			if (line.hasOption("patient-level"))
				setDisplayLevel(Jpdbi.PATIENT);
			if (line.hasOption("study-level"))
				setDisplayLevel(Jpdbi.STUDY);
			if (line.hasOption("series-level"))
				setDisplayLevel(Jpdbi.SERIE);
			if (line.hasOption("instance-level") || line.hasOption("sop-level"))
				setDisplayLevel(Jpdbi.INSTANCE);
			if (line.hasOption("path"))
				setDisplayLevel(Jpdbi.PATH);
			if (line.hasOption("fs"))
				setDisplayLevel(Jpdbi.FILESYSTEM);

			if (line.hasOption("update")) {
				if (line.getOptionValue("update").equalsIgnoreCase("list")) {
					System.out.println("Defined updates:");
					Enumeration e = applicationProps.propertyNames();
					while (e.hasMoreElements()) {
						String key = (String) e.nextElement();
						if (key.startsWith("update.")) {
							System.out.println("   " + key.substring(7) + " <" + applicationProps.getProperty(key) + ">");
						}
					}
					System.exit(0);
				} else {
					setUpdate(line.getOptionValues("update"));
				}
			}

			if (line.hasOption("query"))
				setQuery(line.getOptionValue("query"));

			if (line.hasOption("patientds"))
				setDisplayDS(Jpdbi.PATIENT);
			if (line.hasOption("studyds"))
				setDisplayDS(Jpdbi.STUDY);
			if (line.hasOption("seriesds"))
				setDisplayDS(Jpdbi.SERIE);
			if (line.hasOption("instanceds") || line.hasOption("sopds"))
				setDisplayDS(Jpdbi.INSTANCE);

			if (line.hasOption("h")) {
				// automatically generate the help statement
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("java -jar jpdbi.jar [options] [FirstName [LastName [BirthDate]]]", options);
				System.exit(0);
			}

			if (line.hasOption("jdbcurlhelp")) {
				UrlHelp();
				System.exit(0);
			}

			if (line.hasOption("v")) {
				System.out.println("Jpdbi Version: " + Jpdbi.VERSION);
				System.out.println(Jpdbi.ID);
				System.out.println(Jpdbi.REVISION);
				System.exit(0);
			}

			if (line.hasOption("E")) {
				errFilename = line.getOptionValue("E");
				try {
					System.setErr(new PrintStream(new BufferedOutputStream(new FileOutputStream(errFilename))));
				} catch (Exception e) {
					Jpdbi.exit(1, e.toString());
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
					Jpdbi.exit(1, e.toString());
				}
			}

			// CommandLine without OPTIONS
			// Patient Name
			String LastName = null;
			if (i < argv.length)
				LastName = argv[i++];

			String FirstName = null;
			if (i < argv.length)
				FirstName = argv[i++];

			if (LastName != null || FirstName != null)
				setPatientName(LastName, FirstName);

			// Patient Birth Date
			if (i < argv.length)
				setPatientBirthDate(argv[i++]);

			if (i < argv.length)
				System.err.println("Error: Too many arguments.");

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
					Jpdbi.exit(1, "ERROR: DB Alias: < " + line.getOptionValue("db") + " > not found!");
				}
			}

			String jdbcDriverClass = System.getProperty("jdbc.driver");

			if (getJdbcUrl() != null && getJdbcUrl().startsWith("{") && getJdbcUrl().contains("}")) {
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
				Jpdbi.exit(-1, e.toString());
			}

			// jdbc url
			if (line.hasOption("U")) {
				if (line.getOptionValue("U").equalsIgnoreCase("help")) {
					UrlHelp();
					System.exit(0);
				}

				setJdbcUrl(line.getOptionValue("U"));
			}

			if (getJdbcUrl() == null) {
				if (setJdbcUrl(System.getProperty("jdbc.url")) == null) {
					String s = applicationProps.getProperty("jdbc.url");
					if (s.startsWith("$")) {
						String s1 = applicationProps.getProperty("jdbc.url." + s.substring(1));
						if (s1 == null) {
							Jpdbi.exit(1, "ERROR: DB Alias: < " + s.substring(1) + " > not found!");
						}
						s = s1;
					}
					if (setJdbcUrl(s) == null) {
						Jpdbi.exit(1, "ERROR: Missing JDBC Url.");
					}
				}
			}

			if (line.hasOption("uid")) {
				String myUidTemplate = line.getOptionValue("uid");
				if (myUidTemplate.equalsIgnoreCase("help")) {
					UidHelp();
					System.exit(0);
				} else {
					setUidBase(myUidTemplate);
				}
			} else {
				setUidBase(applicationProps.getProperty("uid.template"));
			}

			setProps(applicationProps);

			if (getDisplayLevel().isEmpty())
				setDisplayLevel(Jpdbi.STUDY);
		} catch (ParseException exp) {
			Jpdbi.exit(1, "Unexpected exception:" + exp.getMessage());
		}

		// Build Update
		{
			String[][] UpdDicom = null;
			if (getUpdate() != null) {
				Hashtable<String, Boolean> ht = new Hashtable<String, Boolean>();
				boolean UpdMulti = true;

				String TmpLevel = null;

				String TmpUpdDCM = null;
				String TmpUpdDB = null;
				String TmpUpdTYPE = null;
				String TmpUpdMULTI = null;

				int CntUpdates = getUpdate().length;

				if (CntUpdates > 0) {
					UpdDicom = new String[CntUpdates][5];

					for (int loop = 0; loop < CntUpdates; loop++) {
						String Update = getUpdate()[loop];
						int split = Update.indexOf('=');
						if (split != -1) {
							String UpdField = Update.substring(0, split).toLowerCase();
							if (!ht.containsKey(UpdField)) {
								String UpdValue = Update.substring(split + 1);
								String UpdDef = getProps().getProperty("update." + UpdField, "").trim();
								if (UpdDef.length() > 0) {
									String[] TmpUpdate = UpdDef.split(":");
									if (!(TmpUpdate.length < 3 || TmpUpdate.length > 5)) {
										if (TmpLevel == null || TmpLevel.equals(TmpUpdate[0].toUpperCase().trim())) {
											// Setting Level
											TmpLevel = TmpUpdate[0].toUpperCase().trim();

											TmpUpdDB = TmpUpdate[1].toUpperCase().trim();
											if (TmpUpdDB.equals("NULL") || TmpUpdDB.length() == 0)
												TmpUpdDB = null;
											TmpUpdDCM = TmpUpdate[2].trim();
											if (TmpUpdDCM.equalsIgnoreCase("NULL") || TmpUpdDCM.length() == 0)
												TmpUpdDCM = null;
											TmpUpdTYPE = "VARCHAR";
											if (TmpUpdate.length > 3) {
												TmpUpdTYPE = TmpUpdate[3].trim().toUpperCase();
												if (TmpUpdTYPE.length() == 0)
													TmpUpdTYPE = "VARCHAR";
											}

											TmpUpdMULTI = "false";
											if (TmpUpdate.length > 4) {
												TmpUpdMULTI = TmpUpdate[4].trim().toLowerCase();
												if (!(TmpUpdMULTI.equals("true") || TmpUpdMULTI.equals("yes")))
													TmpUpdTYPE = "false";
											}

											if (TmpUpdMULTI.equals("false"))
												UpdMulti = false;
											else
												UpdMulti &= true;

											if (UpdValue.equalsIgnoreCase("_REMOVE_")) {
												UpdValue = null;
											}

											if (TmpUpdDCM != null) {
												ht.put(UpdField, true);
												UpdDicom[loop][0] = TmpUpdDB;
												UpdDicom[loop][1] = TmpUpdDCM;
												UpdDicom[loop][2] = UpdValue;
												UpdDicom[loop][3] = TmpUpdTYPE;
											}
										} else {
											Jpdbi.exit(1, "Multilevel updates not supported [" + TmpLevel + " & " + TmpUpdate[0]
													+ "].");
										}
									} else {
										Jpdbi.exit(1, "Update definition problem [" + UpdDef + "].");
									}
								} else {
									Jpdbi.exit(1, "Update not defined [" + UpdField + "].");
								}
							} else {
								Jpdbi.exit(1, "Duplicate Update [" + UpdField + "].");
							}
						} else {
							Jpdbi.exit(1, "Update syntax error [" + Update + "].");
						}

						// Set levels && updateLevel according to TABLES to be
						// updated
						for (int i = 0; i < Jpdbi.Tables.length; i++) {
							if (TmpLevel.equals(Jpdbi.Tables[i])) {
								setDisplayLevel(i);
								setUpdateLevel(i);
								if (TmpUpdDCM != null)
									setUpdateDS(i);
							}
						}
					}
					UpdDicom[0][4] = (UpdMulti ? "t" : "f");

					setUpdDicom(UpdDicom);

					if (isDebug())
						System.err.println("DEBUG: Update Level: " + TmpLevel);
				}
			}
		}

		{
			boolean patientLink = false;
			boolean studyLink = false;
			boolean seriesLink = false;
			boolean instanceLink = false;
			boolean filesLink = false;
			// boolean InstanceLink=false;
			// BUILD WHERE CLAUSE
			String where = "";
			setDisplayLevel(Jpdbi.PATIENT);

			// Extended QUERY
			if (getQuery() != null) {
				String[] wrds = getQuery().toUpperCase().split("\\W+");

				for (int i = 0; i < wrds.length; i++) {
					if (wrds[i].equals("PATIENT")) {
						patientLink = true;
					} else if (wrds[i].equals("STUDY")) {
						studyLink = true;
					} else if (wrds[i].equals("SERIES")) {
						seriesLink = true;
					} else if (wrds[i].equals("INSTANCE")) {
						instanceLink = true;
					} else if (wrds[i].equals("FILES") || wrds[i].equals("FILESYSTEM")) {
						filesLink = true;
					}
				}
				if (where.length() > 0)
					where += " and " + getQuery();
				else
					where = getQuery();
			}

			// Patient
			{
				String myWhere = "";

				if (getPatientName() != null)
					myWhere = addWhere(myWhere, "pat_name", getPatientName(), Types.VARCHAR, true);

				if (getPatientBirthDate() != null) {
					if (isPre214())
						myWhere = addWhere(myWhere, "PAT_BIRTHDATE", getPatientBirthDate(), Types.DATE);
					else
						myWhere = addWhere(myWhere, "PAT_BIRTHDATE", getPatientBirthDate(), Types.VARCHAR);
				}

				if (getPatientId() != null)
					myWhere = addWhere(myWhere, "PAT_ID", getPatientId(), Types.VARCHAR);

				if (getIssuer() != null)
					myWhere = addWhere(myWhere, "PAT_ID_ISSUER", getIssuer(), Types.VARCHAR);

				if (myWhere.length() > 0) {
					patientLink = true;

					if (where.length() > 0)
						where = where.concat(" and " + myWhere);
					else
						where = myWhere;
				}
			}

			// Study
			{
				String myWhere = "";
				String StudyDATE = getStudyDate();

				if (StudyDATE != null) {
					long dummy = -1;

					if (isLongNumber(StudyDATE))
						dummy = Long.parseLong(StudyDATE);
					else {
						if (StudyDATE.equalsIgnoreCase("today") || StudyDATE.equalsIgnoreCase("heute"))
							dummy = 0;
						if (StudyDATE.equalsIgnoreCase("yesterday") || StudyDATE.equalsIgnoreCase("gestern"))
							dummy = 1;
					}

					if (dummy >= 0 && dummy <= 999) {
						Calendar cal = Calendar.getInstance();
						cal.add(Calendar.DATE, (int) -dummy);
						String mydate = "" + cal.get(Calendar.YEAR) + "-" + (cal.get(Calendar.MONTH) + 1) + "-"
								+ (cal.get(Calendar.DATE)) + " 00:00:00";
						// myWhere = addWhere(myWhere, "STUDY.STUDY_DATETIME",
						// mydate,
						// Types.TIMESTAMP);
						myWhere += " STUDY_DATETIME" + ">= {ts '" + mydate + "'}";
					} else {
						myWhere += " (STUDY_DATETIME" + ">= {ts '" + StudyDATE + " 00:00:00'} and";
						myWhere += "  STUDY_DATETIME" + "<= {ts '" + StudyDATE + " 23:59:59'})";
					}
				}

				if (getStudyIuid() != null)
					myWhere = addWhere(myWhere, "STUDY.STUDY_IUID", getStudyIuid(), Types.VARCHAR);

				if (myWhere.length() > 0) {
					studyLink = true;

					if (where.length() > 0)
						where = where.concat(" and " + myWhere);
					else
						where = myWhere;
				}
			}

			// Series
			{
				String myWhere = "";
				if (getSeriesIuid() != null)
					myWhere = addWhere(myWhere, "SERIES.SERIES_IUID", getSeriesIuid(), Types.VARCHAR);

				if (getModality() != null)
					myWhere = addWhere(myWhere, "SERIES.MODALITY", getModality(), Types.VARCHAR);

				if (myWhere.length() > 0) {
					seriesLink = true;

					if (where.length() > 0)
						where = where.concat(" and " + myWhere);
					else
						where = myWhere;
				}
			}

			// BUILD from, links, join, select, order

			String from = "";
			String links = "";
			String join = "";
			String select = "";
			String order = "";

			if (filesLink || isDisplayLevel(Jpdbi.PATH)) {
				if (filesLink || isIgnoreEmpty()) {
					instanceLink = true;
					from += ",FILES,FILESYSTEM";
					links += "INSTANCE.PK=INSTANCE_FK and FILESYSTEM.PK=FILES.FILESYSTEM_FK and ";
				} else {
					join = "left join FILESYSTEM on FILESYSTEM.PK=FILES.FILESYSTEM_FK " + join;
					join = "left join FILES on INSTANCE.PK=INSTANCE_FK " + join;
				}
				setDisplayLevel(Jpdbi.INSTANCE);
				if (isDisplayAETs())
					select = PrependSql("FILESYSTEM.RETRIEVE_AET FSRETAET", select);
				if (isDisplayLevel(Jpdbi.FILESYSTEM)) {
					select = PrependSql("FILESYSTEM.PK F, FS_GROUP_ID FSGRP", select);
				}
				select = PrependSql(
						"FILES.PK E, DIRPATH, FILEPATH, FILE_SIZE FILESIZE, FILE_STATUS FILESTATUS, filesystem.availability FSAVAIL",
						select);
				order = PrependSql("E", order);
			}

			if (instanceLink || isDisplayLevel(Jpdbi.INSTANCE) || isUpdateDS(Jpdbi.INSTANCE)) {
				if (instanceLink || isIgnoreEmpty()) {
					seriesLink = true;
					from = ",INSTANCE" + from;
					links += "SERIES.PK=SERIES_FK and ";
				} else {
					join = "left join INSTANCE on SERIES.PK=SERIES_FK " + join;
				}
				if (isDisplayDS(Jpdbi.INSTANCE) || isUpdateDS(Jpdbi.INSTANCE))
					select = PrependSql("INST_ATTRS ", select);
				setDisplayLevel(Jpdbi.SERIE);
				if (isDisplayAETs())
					select = PrependSql("INSTANCE.EXT_RETR_AET INSTEXTRETAET, INSTANCE.RETRIEVE_AETS INSTRETAET", select);

				select = PrependSql(
						"INSTANCE.PK D, SOP_IUID SOPIUID, inst_no INSTNUM, instance.availability INSTAVAIL, inst_status INSTSTATUS",
						select);
				order = PrependSql("D", order);
			}

			if (seriesLink || isDisplayLevel(Jpdbi.SERIE) || isUpdateDS(Jpdbi.SERIE)) {
				if (seriesLink || isIgnoreEmpty()) {
					studyLink = true;
					from = ",SERIES" + from;
					links += "STUDY.PK=SERIES.STUDY_FK and ";
				} else {
					join = "left join SERIES on STUDY.PK=STUDY_FK " + join;
				}
				if (isDisplayDS(Jpdbi.SERIE) || isUpdateDS(Jpdbi.SERIE))
					select = PrependSql("SERIES_ATTRS ", select);

				if (isDisplayLevel(Jpdbi.SERIE)) {
					setDisplayLevel(Jpdbi.STUDY);
					if (isDisplayAETs())
						select = PrependSql(
								"SERIES.EXT_RETR_AET SEREXTRETAET, SERIES.RETRIEVE_AETS SERRETAET, SERIES.SRC_AET SERSRCAET",
								select);

					select = PrependSql(
							"SERIES.PK C, SERIES_IUID SERIUID, SERIES.MODALITY SERMOD, SERIES.NUM_INSTANCES SERNUMINST, SERIES.AVAILABILITY SERAVAIL, series_status SERSTATUS",
							select);
					order = PrependSql("C", order);
				}
			}

			if (studyLink || isDisplayLevel(Jpdbi.STUDY) || isUpdateDS(Jpdbi.STUDY)) {
				if (studyLink || isIgnoreEmpty()) {
					patientLink = true;
					from = ",STUDY" + from;
					links += "PATIENT.PK=STUDY.PATIENT_FK";
				} else {
					join = "left join STUDY on PATIENT.PK=STUDY.PATIENT_FK " + join;
				}
				if (isDisplayDS(Jpdbi.STUDY) || isUpdateDS(Jpdbi.STUDY))
					select = PrependSql("STUDY_ATTRS ", select);
				if (isDisplayLevel(Jpdbi.STUDY)) {
					setDisplayLevel(Jpdbi.PATIENT);
					if (isDisplayAETs())
						select = PrependSql("STUDY.EXT_RETR_AET STYEXTRETAET, STUDY.RETRIEVE_AETS STYRETAET", select);
					select = PrependSql(
							"STUDY.PK B, STUDY.STUDY_IUID STYIUID, STUDY_DATETIME STYD, MODS_IN_STUDY STYMODS, num_series STYNUMSER, study.num_instances STYNUMINST, study.availability STYAVAIL",
							select);

					order = PrependSql("B", order);
				}
			}

			if (patientLink || isDisplayLevel(Jpdbi.PATIENT) || isUpdateDS(Jpdbi.PATIENT)) {
				if (patientLink) {
					from = "PATIENT" + from;
				} else {
					join = "PATIENT " + join;
				}

				if (isDisplayDS(Jpdbi.PATIENT) || isUpdateDS(Jpdbi.PATIENT))
					select = PrependSql("PAT_ATTRS ", select);
				if (isDisplayLevel(Jpdbi.PATIENT)) {
					select = PrependSql("PATIENT.PK A, PAT_NAME, PAT_SEX, PAT_BIRTHDATE BD, PAT_ID, PAT_ID_ISSUER ", select);
					order = PrependSql("PAT_NAME,A", order);
				}
			}

			if (links.length() > 0 && where.length() > 0) {
				links += " and ";
			}

			if (where.length() == 0) {
				System.err.println("No filter criteria given...");
				System.err.println("Use at least % if you know what you are doing,");
				System.err.println("or use --help for help.");
				System.exit(1);
			}

			setSqlPortions(select, from, join, links, where, null, order);

		}
	}
}
