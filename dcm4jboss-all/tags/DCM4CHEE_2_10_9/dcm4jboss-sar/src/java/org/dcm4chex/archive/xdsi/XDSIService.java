/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), available at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * TIANI Medgraph AG.
 * Portions created by the Initial Developer are Copyright (C) 2003-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunter.zeilinger@tiani.com>
 * Franz Willer <franz.willer@gwi-ag.com>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */
package org.dcm4chex.archive.xdsi;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;

import javax.activation.DataHandler;
import javax.management.Notification;
import javax.management.NotificationFilterSupport;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.RuntimeMBeanException;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4che.util.UIDGenerator;
import org.dcm4cheri.util.StringUtils;
import org.dcm4chex.archive.common.SeriesStored;
import org.dcm4chex.archive.dcm.ianscu.IANScuService;
import org.dcm4chex.archive.ejb.interfaces.FileDTO;
import org.dcm4chex.archive.ejb.jdbc.QueryCmd;
import org.dcm4chex.archive.ejb.jdbc.QueryFilesCmd;
import org.dcm4chex.archive.util.FileUtils;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.system.server.ServerConfig;
import org.jboss.system.server.ServerConfigLocator;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.sun.xml.messaging.saaj.util.JAXMStreamSource;

/**
 * @author franz.willer@gwi-ag.com
 * @version $Revision: 3024 $ $Date: 2006-12-21 01:19:27 +0800 (周四, 21 12月 2006) $
 * @since Feb 15, 2006
 */
public class XDSIService extends ServiceMBeanSupport {

	public static final String DOCUMENT_ID = "doc_1";
	public static final String AUTHOR_SPECIALITY = "authorSpeciality";
	public static final String AUTHOR_PERSON = "authorPerson";
	public static final String AUTHOR_ROLE = "authorRole";
	public static final String AUTHOR_ROLE_DISPLAYNAME = "authorRoleDisplayName";
	public  static final String AUTHOR_INSTITUTION = "authorInstitution";
	public  static final String SOURCE_ID = "sourceId";
	private static final String HEADER_VALUE = " ";
    
    private static final String NONE = "NONE";
	
    protected ObjectName auditLogName;
    
    protected ObjectName ianScuServiceName;

    protected ObjectName keyObjectServiceName;
    
    protected String[] autoPublishAETs;
    private String autoPublishDocTitle;
    
	private static Logger log = Logger.getLogger(XDSIService.class.getName());

    private static ServerConfig config = ServerConfigLocator.locate();
    
    private DocumentBuilderFactory dbFactory;

	private String testPath;
	
	
	private Map usr2author = new TreeMap();
	
	
// http attributes to document repository actor (synchron) 	
	private String docRepositoryURI;
	private String docRepositoryAET;
	private String proxyHost;
	private int proxyPort;
	private boolean useHttp;
	
// SMTP attributes to document repository actor (asynchron)	
	private String docRepositoryMailAddr;//TODO
	private String smtpHost;
	private int smtpPort;
	private boolean useSmtp;
	
// Metadata attributes
	private File propertyFile;
	private File docTitleCodeListFile;
	private File classCodeListFile;
	private File contentTypeCodeListFile;
	private File eventCodeListFile;
	private File healthCareFacilityCodeListFile;
    private File autoPublishPropertyFile;

	private List authorRoles = new ArrayList();
	private List confidentialityCodes;
	
	private Properties metadataProps = new Properties();

	private Map mapCodeLists = new HashMap();

    private ObjectName pixQueryServiceName;
    private String localDomain;
    private String affinityDomain;
    private String keystoreURL = "conf/identity.p12";
	private String keystorePassword;
    private String trustStoreURL = "conf/cacerts.jks";
	private String trustStorePassword;
	private HostnameVerifier origHostnameVerifier = null;
	private String allowedUrlHost = null;
	
	private String ridURL;
	
	private boolean logSOAPMessage = true;

        private final NotificationListener ianListener = 
            new NotificationListener() {
                public void handleNotification(Notification notif, Object handback) {
                    log.info("ianListener called!");
                    onIAN((Dataset) notif.getUserData());
                }

            };
        
    /**
	 * @return Returns the property file path.
	 */
	public String getPropertyFile() {
			return propertyFile.getPath();
	}
	public void setPropertyFile(String file) throws IOException {
		if ( file == null || file.trim().length() < 1) return;
		propertyFile = new File(file.replace('/', File.separatorChar));
		try {
			readPropertyFile();
		} catch ( Throwable ignore ) {
			log.warn("Property file "+file+" cant be read!");
		}
	}

	public String getDocTitleCodeListFile() {
		return docTitleCodeListFile == null ? null : docTitleCodeListFile.getPath();
	}
	public void setDocTitleCodeListFile(String file) throws IOException {
		if ( file == null || file.trim().length() < 1) {
			docTitleCodeListFile = null;
		} else {
			docTitleCodeListFile = new File(file.replace('/', File.separatorChar));
		}
	}
	
	public String getClassCodeListFile() {
		return classCodeListFile == null ? null : classCodeListFile.getPath();
	}
	public void setClassCodeListFile(String file) throws IOException {
		if ( file == null || file.trim().length() < 1) {
			classCodeListFile = null;
		} else {
			classCodeListFile = new File(file.replace('/', File.separatorChar));
		}
	}

	public String getContentTypeCodeListFile() {
		return contentTypeCodeListFile == null ? null : contentTypeCodeListFile.getPath();
	}
	public void setContentTypeCodeListFile(String file) throws IOException {
		if ( file == null || file.trim().length() < 1) {
			contentTypeCodeListFile = null;
		} else {
			contentTypeCodeListFile = new File(file.replace('/', File.separatorChar));
		}
	}
	
	public String getHealthCareFacilityCodeListFile() {
		return healthCareFacilityCodeListFile == null ? null : healthCareFacilityCodeListFile.getPath();
	}
	public void setHealthCareFacilityCodeListFile(String file) throws IOException {
		if ( file == null || file.trim().length() < 1) {
			healthCareFacilityCodeListFile = null;
		} else {
			healthCareFacilityCodeListFile = new File(file.replace('/', File.separatorChar));
		}
	}
	
	public String getEventCodeListFile() {
		return eventCodeListFile == null ? null : eventCodeListFile.getPath();
	}
	public void setEventCodeListFile(String file) throws IOException {
		if ( file == null || file.trim().length() < 1) {
			eventCodeListFile = null;
		} else {
			eventCodeListFile = new File(file.replace('/', File.separatorChar));
		}
	}
	
	public String getConfidentialityCodes() {
		return getListString(confidentialityCodes);
	}
	public void setConfidentialityCodes(String codes) throws IOException {
		confidentialityCodes = setListString( codes );;
	}

	/**
	 * @return Returns the authorPerson or a user to authorPerson mapping.
	 */
	public String getAuthorPersonMapping() {
		if ( usr2author.isEmpty() ) {
			return metadataProps.getProperty(AUTHOR_PERSON);
		} else {
			return this.getMappingString(usr2author);
		}
	}
	
	/**
	 * Set either a fix authorPerson or a mapping user to authorPerson.
	 * <p>
	 * Mapping format: &lt;user&gt;=&lt;authorPerson&gt;<br>
	 * Use either newline or semicolon to seperate mappings.
	 * <p>
	 * If '=' is ommited, a fixed autorPerson is set in <code>metadataProps</code>
	 * 
	 * @param s The authorPerson(-mapping) to set.
	 */
	public void setAuthorPersonMapping(String s) {
		if ( s == null || s.trim().length() < 1) return;
		usr2author.clear();
		if ( s.indexOf('=') == -1) {
			metadataProps.setProperty(AUTHOR_PERSON, s); //NO mapping user -> authorPerson; use fix authorPerson instead
		} else {
			this.addMappingString(s, usr2author);
		}
	}

	/**
	 * get the authorPerson value for given user.
	 * 
	 * @param user
	 * @return
	 */
	public String getAuthorPerson( String user ) {
		String person = (String)usr2author.get(user);
		if ( person == null ) {
			person = metadataProps.getProperty(AUTHOR_PERSON);
		}
		return person;
	}
	
	
	public String getSourceID() {
		return metadataProps.getProperty(SOURCE_ID);
	}

	public void setSourceID(String id ) {
		metadataProps.setProperty(SOURCE_ID, id == null ? "" : id);
	}
	
	/**
	 * @return Returns a list of authorRoles (with displayName) as String.
	 */
	public String getAuthorRoles() {
		return getListString(authorRoles);
	}
	
	/**
	 * Set authorRoles (with displayName).
	 * <p>
	 * Format: &lt;role&gt;^&lt;displayName&gt;<br>
	 * Use either newline or semicolon to seperate roles.
	 * <p>
	 * @param s The roles to set.
	 */
	public void setAuthorRoles(String s) {
		if ( s == null || s.trim().length() < 1) return;
		authorRoles = setListString(s);
	}

	public Properties joinMetadataProperties(Properties props) {
		Properties p = new Properties();//we should not change metadataProps!
		p.putAll(metadataProps);
		if ( props == null )
			p.putAll(props);
		return p;
	}
	
//http
	/**
	 * @return Returns the docRepositoryURI.
	 */
	public String getDocRepositoryURI() {
		return docRepositoryURI;
	}
	/**
	 * @param docRepositoryURI The docRepositoryURI to set.
	 */
	public void setDocRepositoryURI(String docRepositoryURI) {
		this.docRepositoryURI = docRepositoryURI;
	}

	/**
	 * @return Returns the docRepositoryAET.
	 */
	public String getDocRepositoryAET() {
		return docRepositoryAET == null ? "NONE" : docRepositoryAET;
	}
	/**
	 * @param docRepositoryAET The docRepositoryAET to set.
	 */
	public void setDocRepositoryAET(String docRepositoryAET) {
		if ( "NONE".equals(docRepositoryAET))
			this.docRepositoryAET = null;
		else
			this.docRepositoryAET = docRepositoryAET;
	}
	/**
	 * @return Returns the proxyHost.
	 */
	public String getProxyHost() {
		return proxyHost == null ? "NONE" : proxyHost;
	}
	/**
	 * @param proxyHost The proxyHost to set.
	 */
	public void setProxyHost(String proxyHost) {
		if ( "NONE".equals(proxyHost) ) 
			this.proxyHost = null;
		else
			this.proxyHost = proxyHost;
	}
	/**
	 * @return Returns the proxyPort.
	 */
	public int getProxyPort() {
		return proxyPort;
	}
	/**
	 * @param proxyPort The proxyPort to set.
	 */
	public void setProxyPort(int proxyPort) {
		this.proxyPort = proxyPort;
	}
	/**
	 * @param keyStorePassword The keyStorePassword to set.
	 */
	public void setKeystorePassword(String keyStorePassword) {
		this.keystorePassword = keyStorePassword;
	}
	/**
	 * @return Returns the keyStoreURL.
	 */
	public String getKeystoreURL() {
		return keystoreURL;
	}
	/**
	 * @param keyStoreURL The keyStoreURL to set.
	 */
	public void setKeystoreURL(String keyStoreURL) {
		this.keystoreURL = keyStoreURL;
	}
	/**
	 * @return Returns the trustStore.
	 */
	public String getTrustStoreURL() {
		return trustStoreURL == null ? "NONE" : trustStoreURL;
	}
	/**
	 * @param trustStore The trustStore to set.
	 */
	public void setTrustStoreURL(String trustStoreURL) {
		if ( "NONE".equals(trustStoreURL ) ) {
			this.trustStoreURL = null;
		} else {
			this.trustStoreURL = trustStoreURL;
		}
	}
	/**
	 * @param trustStorePassword The trustStorePassword to set.
	 */
	public void setTrustStorePassword(String trustStorePassword) {
		this.trustStorePassword = trustStorePassword;
	}
	/**
	 * @return Returns the allowedUrlHost.
	 */
	public String getAllowedUrlHost() {
		return allowedUrlHost == null ? "CERT" : allowedUrlHost;
	}
	/**
	 * @param allowedUrlHost The allowedUrlHost to set.
	 */
	public void setAllowedUrlHost(String allowedUrlHost) {
		this.allowedUrlHost = "CERT".equals(allowedUrlHost) ? null : allowedUrlHost;
	}
	/**
	 * @return Returns the useHttp.
	 */
	public boolean isUseHttp() {
		return useHttp;
	}
	/**
	 * @param useHttp The useHttp to set.
	 */
	public void setUseHttp(boolean useHttp) {
		this.useHttp = useHttp;
	}
	
	/**
	 * @return Returns the testPath.
	 */
	public String getTestPath() {
		return testPath == null ? "NONE":testPath;
	}
	/**
	 * @param testPath The testPath to set.
	 */
	public void setTestPath(String testPath) {
		if ( "NONE".equals(testPath) ) 
			this.testPath = null;
		else
			this.testPath = testPath;
	}

    public final ObjectName getAuditLoggerName() {
        return auditLogName;
    }

    public final void setAuditLoggerName(ObjectName auditLogName) {
        this.auditLogName = auditLogName;
    }
	
	public final ObjectName getPixQueryServiceName() {
        return pixQueryServiceName;
    }

    public final void setPixQueryServiceName(ObjectName name) {
        this.pixQueryServiceName = name;
    }

    public final ObjectName getIANScuServiceName() {
        return ianScuServiceName;
    }

    public final void setIANScuServiceName(ObjectName ianScuServiceName) {
        this.ianScuServiceName = ianScuServiceName;
    }
    
    public final ObjectName getKeyObjectServiceName() {
        return keyObjectServiceName;
    }

    public final void setKeyObjectServiceName(ObjectName keyObjectServiceName) {
        this.keyObjectServiceName = keyObjectServiceName;
    }
    
    public final String getAutoPublishAETs() {
        return autoPublishAETs.length > 0 ? StringUtils.toString(autoPublishAETs,
                '\\') : NONE;
    }

    public final void setAutoPublishAETs(String autoPublishAETs) {
        this.autoPublishAETs = NONE.equalsIgnoreCase(autoPublishAETs)
                ? new String[0]
                : StringUtils.split(autoPublishAETs, '\\');
    }
    
    public final String getAutoPublishDocTitle() {
        return autoPublishDocTitle;
    }
    public final void setAutoPublishDocTitle(String autoPublishDocTitle ) {
        this.autoPublishDocTitle = autoPublishDocTitle;
    }
    
    public String getAutoPublishPropertyFile() {
        return autoPublishPropertyFile == null ? "NONE" : autoPublishPropertyFile.getPath();
    }
    public void setAutoPublishPropertyFile(String file) throws IOException {
        if ( file == null || file.trim().length() < 1 || file.equalsIgnoreCase("NONE")) {
            autoPublishPropertyFile = null;
        } else {
            autoPublishPropertyFile = new File(file.replace('/', File.separatorChar));
        }
    }
    
    public String getLocalDomain() {
    	return localDomain == null ? "NONE" : localDomain;
    }
    public void setLocalDomain(String domain) {
        localDomain = ( domain==null || 
                		domain.trim().length()<1 || 
                		domain.equalsIgnoreCase("NONE") ) ? null : domain;
    }
    
    public String getAffinityDomain() {
    	return affinityDomain;
    }
    public void setAffinityDomain(String domain) {
    	affinityDomain = domain;
    }
	
	/**
	 * Adds a 'mappingString' (format:&lt;key&gt;=&lt;value&gt;...) to a map.
	 * 
	 * @param s
	 */
	private void addMappingString(String s, Map map) {
		StringTokenizer st = new StringTokenizer( s, ",;\n\r\t ");
		String t;
		int pos;
		while ( st.hasMoreTokens() ) {
			t = st.nextToken();
			pos = t.indexOf('=');
			if ( pos == -1) {
				map.put(t,t);
			} else {
				map.put(t.substring(0,pos), t.substring(++pos));
			}
		}
	}
	/**
	 * Returns the String representation of a map
	 * @return
	 */
	private String getMappingString(Map map) {
		if ( map == null || map.isEmpty() ) return null;
		StringBuffer sb = new StringBuffer();
		String key;
		for ( Iterator iter = map.keySet().iterator() ; iter.hasNext() ; ) {
			key = iter.next().toString();
			sb.append(key).append('=').append(map.get(key)).append( System.getProperty("line.separator", "\n"));
		}
		return sb.toString();
	}

	private List setListString(String s) {
        List l = new ArrayList();
        if ( NONE.equals(s) ) return l;
		StringTokenizer st = new StringTokenizer( s, ";\n\r");
		while ( st.hasMoreTokens() ) {
			l.add(st.nextToken());
		}
		return l;
	}

	private String getListString(List l) {
		if ( l == null || l.isEmpty() ) return NONE;
		StringBuffer sb = new StringBuffer();
		for ( Iterator iter = l.iterator() ; iter.hasNext() ; ) {
			sb.append(iter.next()).append( System.getProperty("line.separator", "\n"));
		}
		return sb.toString();
	}

	/**
	 * @return Returns the ridURL.
	 */
	public String getRidURL() {
		return ridURL;
	}
	/**
	 * @param ridURL The ridURL to set.
	 */
	public void setRidURL(String ridURL) {
		this.ridURL = ridURL;
	}
    /**
     * @return Returns the logSOAPMessage.
     */
    public boolean isLogSOAPMessage() {
        return logSOAPMessage;
    }
    /**
     * @param logSOAPMessage The logSOAPMessage to set.
     */
    public void setLogSOAPMessage(boolean logSOAPMessage) {
        this.logSOAPMessage = logSOAPMessage;
    }
// Operations	
	
	/**
	 * @throws IOException
	 */
	public void readPropertyFile() throws IOException {
		File propFile = FileUtils.resolve(this.propertyFile);
		BufferedInputStream bis= new BufferedInputStream( new FileInputStream( propFile ));
		try {
			metadataProps.clear();
			metadataProps.load(bis);
		} finally {
			bis.close();
		}
	}
    
	public List listAuthorRoles() throws IOException {
		return this.authorRoles;
	}
	public List listDocTitleCodes() throws IOException {
		return readCodeFile(docTitleCodeListFile);
	}
	public List listEventCodes() throws IOException {
		return readCodeFile(eventCodeListFile);
	}
	public List listClassCodes() throws IOException {
		return readCodeFile(classCodeListFile);
	}
	public List listContentTypeCodes() throws IOException {
		return readCodeFile(contentTypeCodeListFile);
	}
	public List listHealthCareFacilityTypeCodes() throws IOException {
		return readCodeFile(healthCareFacilityCodeListFile);
	}
	public List listConfidentialityCodes() throws IOException {
		return confidentialityCodes;
	}
	
	/**
	 * @throws IOException
	 * 
	 */
	public List readCodeFile(File codeFile) throws IOException {
		if ( codeFile == null ) return new ArrayList();
		List l = (List) mapCodeLists.get(codeFile);
		if ( l == null ) {
			l = new ArrayList();
			File file = FileUtils.resolve(codeFile);
			if ( file.exists() ) {
				BufferedReader r = new BufferedReader( new FileReader(file));
				String line;
				while ( (line = r.readLine()) != null ) {
					if ( ! (line.charAt(0) == '#') ) {
						l.add( line );
					}
				}
				log.debug("Codes read from code file "+codeFile);
				log.debug("Codes:"+l);
				mapCodeLists.put(codeFile,l);
			} else {
				log.warn("Code File "+file+" does not exist! return empty code list!");
			}
		}
		return l;
	}
	
	public void clearCodeFileCache() {
		mapCodeLists.clear();
	}
		
	
	
	public boolean nistTest(String testID){
		if ( testID == null || testID.trim().length() < 1) {
			File dir = new File( testPath );
			File[] testDirs = dir.listFiles();
			boolean result = true;
			for ( int i = 0; i< testDirs.length; i++) {
				if ( testDirs[i].isDirectory())
					if ( !nistTest(testDirs[i].getName()) ) result = false;
			}
			return result;
		}
		log.info("\n\nPerform NIST test:"+testID);
        File propFile = new File( new File( testPath, testID), "test.properties");
    	Properties props = new Properties();
        try {
        	props.load(new FileInputStream(propFile));
        } catch(FileNotFoundException e) {
            log.warn(" Not a test Directory");
            return false;
        } catch(IOException e) {
            log.error("Error reading test.properties file!",e);
        }
		log.info(" Test with URI:"+props.getProperty("url"));
        String docs = null;
        if ( !props.getProperty("NumOfDoc","0").equals("0") ) {
	        StringBuffer sb = new StringBuffer();
	        int idx=1;
	        String doc, docPropName;
	        while ( (doc = props.getProperty( docPropName="doc"+idx++) ) != null) {
	        	sb.append(',').append(doc).append('|').append(props.get(docPropName+"mimeType"));
	        	sb.append('|').append(props.getProperty(docPropName+"UUID"));
	        }
	        if ( sb.length() > 1 ) docs = sb.toString().substring(1);
        }
		return sendSOAP(propFile.getParentFile().getAbsolutePath()+"/",props.getProperty("metadatafile"),docs, props.getProperty("url")  );
	}
	public boolean sendSOAP( String metaDataFilename, String docNames, String url ) {
		return sendSOAP(null,metaDataFilename,docNames, url);
	}
	public boolean sendSOAP( String baseDir, String metaDataFilename, String docNames, String url ) {
		if ( baseDir == null ) baseDir = testPath == null ? "": testPath+"/";
		File metaDataFile = new File( baseDir+metaDataFilename);
		
		XDSIDocument[] docFiles = null;
		if ( docNames != null && docNames.trim().length() > 0) {
			StringTokenizer st = new StringTokenizer( docNames, "," );
			docFiles = new XDSIDocument[ st.countTokens() ];
			for ( int i=0; st.hasMoreTokens(); i++ ) {
				docFiles[i] = XDSIFileDocument.valueOf( baseDir+st.nextToken() );
			}
		}
		return sendSOAP( readXMLFile(metaDataFile), docFiles, url );
	}
	
	public boolean sendSOAP( Document metaData, XDSIDocument[] docs, String url ) {
		if ( url == null ) url = this.docRepositoryURI;
        log.info("Send 'Provide and Register Document Set' request to "+url+" (proxy:"+proxyHost+":"+proxyPort+")");
		SOAPConnection conn = null;
		try {
			String protocol = url.startsWith("https") ? "https" : "http";
			if ( proxyHost != null && proxyHost.trim().length() > 1 ) {
				System.setProperty( protocol+".proxyHost", proxyHost);
				System.setProperty(protocol+".proxyPort", String.valueOf(proxyPort));
			} else {
				System.setProperty(protocol+".proxyHost", "");
				System.setProperty(protocol+".proxyPort", "");
			}
			if ( "https".equals(protocol) && trustStoreURL != null ) {
				String keyURL = resolvePath(keystoreURL);
				String trustURL = resolvePath(trustStoreURL);
				log.debug("Use TLS with keystore:"+keyURL+" and truststore:"+trustURL);
				System.setProperty("javax.net.ssl.keyStore", keyURL);
				System.setProperty("javax.net.ssl.keyStorePassword", keystorePassword);
				System.setProperty("javax.net.ssl.keyStoreType","PKCS12");
				System.setProperty("javax.net.ssl.trustStore", trustURL);
				System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);
				if ( origHostnameVerifier == null) {
					origHostnameVerifier = HttpsURLConnection.getDefaultHostnameVerifier();
					HostnameVerifier hv = new HostnameVerifier() {
					    public boolean verify(String urlHostName, SSLSession session) {
					    	if ( !origHostnameVerifier.verify ( urlHostName, session)) {
					    		if ( isAllowedUrlHost(urlHostName)) {
					    			System.out.println("Warning: URL Host: "+urlHostName+" vs. "+session.getPeerHost());
					    		} else {
					    			return false;
					    		}
					    	}
				    		return true;
					    }

						private boolean isAllowedUrlHost(String urlHostName) {
							if (allowedUrlHost == null) return false;
							if ( allowedUrlHost.equals("*")) return true;
							return allowedUrlHost.equals(urlHostName);
						}
	
					};
					 
					HttpsURLConnection.setDefaultHostnameVerifier(hv);
				}

				
			}			
		    MessageFactory messageFactory = MessageFactory.newInstance();
		    SOAPMessage message = messageFactory.createMessage();
		    SOAPEnvelope envelope = message.getSOAPPart().getEnvelope();
		    SOAPBody soapBody = envelope.getBody();
		    SOAPElement bodyElement = soapBody.addDocument(metaData);
   			if ( docs != null ) {
		   		for (int i = 0; i < docs.length; i++) {
		   			DataHandler dhAttachment = docs[i].getDataHandler();
		   			AttachmentPart part = message.createAttachmentPart(dhAttachment);
		   			part.setMimeHeader("Content-Type", docs[i].getMimeType());
		   			part.setContentId(docs[i].getDocumentID());
		   			if ( log.isDebugEnabled()){
		   				log.debug("Add Attachment Part ("+(i+1)+"/"+docs.length+")! Document ID:"+part.getContentId()+" mime:"+docs[i].getMimeType());
		   			}
		   			message.addAttachmentPart(part);
		   		}
   			}
            SOAPConnectionFactory connFactory = SOAPConnectionFactory.newInstance();
           
            conn = connFactory.createConnection();
    		if ( log.isDebugEnabled()){
	            log.debug("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
	            log.debug("send request to "+url+" (proxy:"+proxyHost+":"+proxyPort+")");
	            log.debug("-------------------------------- request  ----------------------------------");
	            dumpSOAPMessage(message);
    		}           
            SOAPMessage response = conn.call(message, url);
            log.debug("-------------------------------- response ----------------------------------");
            dumpSOAPMessage(response);//we always want to see the response msg in log!
            log.debug("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
            return checkResponse( response );
		} catch ( Throwable x ) {
			log.error("Cant send SOAP message! Reason:", x);
			return false;
		} finally {
			if ( conn != null ) try {
					conn.close();
				} catch (SOAPException ignore) {}
		}
		
	}
	
	
    public static String resolvePath(String fn) {
    	File f = new File(fn);
        if (f.isAbsolute()) return f.getAbsolutePath();
        File serverHomeDir = ServerConfigLocator.locate().getServerHomeDir();
        return new File(serverHomeDir, f.getPath()).getAbsolutePath();
    }
	
	public boolean sendSOAP(String kosIuid, Properties mdProps) throws SQLException {
		Dataset kos = queryInstance( kosIuid, UIDs.KeyObjectSelectionDocument );
		if ( kos == null ) return false;
		if ( mdProps == null ) mdProps = this.metadataProps;
		XDSMetadata md = new XDSMetadata(kos, mdProps);
		Document metadata = md.getMetadata();
        List files = new QueryFilesCmd(kosIuid).getFileDTOs();
        if ( files == null || files.size() == 0 ) {
        	
        }
        FileDTO fileDTO = (FileDTO) files.iterator().next();
		File file = FileUtils.toFile(fileDTO.getDirectoryPath(), fileDTO.getFilePath());
		XDSIDocument doc = new XDSIFileDocument(file,"application/dicom",DOCUMENT_ID);
		return sendSOAP(metadata,new XDSIDocument[]{doc} , null);
	}
	
	
	private Dataset queryInstance(String kosIuid, String sopClassUID) {
		Dataset keys = DcmObjectFactory.getInstance().newDataset();
		keys.putUI(Tags.SOPInstanceUID, kosIuid);
		keys.putUI(Tags.SOPClassUID, sopClassUID);
		QueryCmd query = null;
		try {
			query = QueryCmd.createInstanceQuery(keys, false, true);
			query.execute();
			if (query.next())
				return query.getDataset();			
		} catch (SQLException e) {
			log.error("Query for Key Object Selection iuid:" + kosIuid + " failed!", e);
		} finally {
			if (query != null)
				query.close();
		}
		return null;
	}

	public boolean sendSOAP(String kosIuid) throws SQLException {
		Dataset kos = queryInstance( kosIuid, UIDs.KeyObjectSelectionDocument );
		return sendSOAP(kos,null);
	}
	public boolean sendSOAP(Dataset kos, Properties mdProps) throws SQLException {
		if ( log.isDebugEnabled()) {
			log.debug("Key Selection Object:");log.debug(kos);
		}
		if ( kos == null ) return false;
		if ( mdProps == null ) mdProps = this.metadataProps;
		mdProps.setProperty("xadPatientID", getAffinityDomainPatientID(kos));
		XDSMetadata md = new XDSMetadata(kos, mdProps);
		Document metadata = md.getMetadata();
		XDSIDocument doc = new XDSIDatasetDocument(kos,"application/dicom",DOCUMENT_ID);
		boolean b = sendSOAP(metadata,new XDSIDocument[]{doc} , null);
		if ( b ) logExport( kos.getString(Tags.PatientID), 
							kos.getString(Tags.PatientName),
							this.getDocRepositoryURI(), 
							docRepositoryAET,
							getSUIDs(kos));
		return b;
	}

	public boolean exportPDF(String iuid) throws SQLException, MalformedURLException {
		return exportPDF(iuid,null);
	}
	public boolean exportPDF(String iuid, Properties mdProps) throws SQLException, MalformedURLException {
		log.debug("export PDF to XDS Instance UID:"+iuid);
		Dataset ds = queryInstance(iuid, null);
		if ( ds == null ) return false;
		String pdfUID = UIDGenerator.getInstance().createUID();
		log.info("Document UID of exported PDF:"+pdfUID);
		ds.putUI(Tags.SOPInstanceUID,pdfUID);
		if ( mdProps == null ) mdProps = this.metadataProps;
		mdProps.setProperty("mimetype", "application/pdf");
		mdProps.setProperty("xadPatientID", getAffinityDomainPatientID(ds));
		XDSMetadata md = new XDSMetadata(ds, mdProps);
		Document metadata = md.getMetadata();
		XDSIDocument doc = new XDSIURLDocument(new URL(ridURL+iuid),"application/pdf",DOCUMENT_ID);
		//XDSIDocument doc = new XDSIFileDocument(new File("C:/xdstest.pdf"), "application/pdf",DOCUMENT_ID);
		boolean b = sendSOAP(metadata,new XDSIDocument[]{doc} , null);
		if ( b ) logExport( ds.getString(Tags.PatientID), 
							ds.getString(Tags.PatientName),
							this.getDocRepositoryURI(), 
							docRepositoryAET,
							getSUIDs(ds));
		return b;
	}
	
    /**
	 * @param kos
	 * @return
	 */
	private Set getSUIDs(Dataset kos) {
		Set suids = null;
		DcmElement sq = kos.get(Tags.CurrentRequestedProcedureEvidenceSeq);
		if ( sq != null ) {
			suids = new LinkedHashSet();
			for ( int i = 0,len=sq.countItems() ; i < len ; i++ ) {
				suids.add(sq.getItem(i).getString(Tags.StudyInstanceUID));
			}
		}
		return suids;
	}
	/**
	 * @param patId
	 * @param patName
	 * @param node Network node
	 * @param aet Application Entity Title
	 */
	private void logExport(String patId, String patName, String node, String aet, Set suids) {
		if ( this.auditLogName == null ) return;
        try {
    		URL url = new URL(node);
    		InetAddress inet = InetAddress.getByName(url.getHost());
            server.invoke(auditLogName,
                    "logExport",
                    new Object[] { patId, patName, "XDSI Export", 
            						suids,
            						inet.getHostAddress(), inet.getHostName(), aet},
                    new String[] { String.class.getName(), String.class.getName(), String.class.getName(), 
            						Set.class.getName(),
									String.class.getName(), String.class.getName(), String.class.getName()});
        } catch (Exception e) {
            log.warn("Audit Log failed:", e);
        }		
	}
	/**
	 * @param kos
	 * @return
	 */
	private String getAffinityDomainPatientID(Dataset kos) {
		String patID = kos.getString(Tags.PatientID);
		String issuer = kos.getString(Tags.IssuerOfPatientID);
		if ( affinityDomain.charAt(0) == '=') {
			if ( affinityDomain.length() == 1 ) {
				patID+="^^^";
				if ( issuer == null ) return patID;
				return patID+issuer;
			} else if (affinityDomain.charAt(1)=='?') {
				log.info("PIX Query disabled: replace issuer with affinity domain! ");
				log.debug("patID changed! ("+patID+"^^^"+issuer+" -> "+patID+"^^^"+affinityDomain.substring(2)+")");
				return patID+"^^^"+affinityDomain.substring(2);
			} else {
				log.info("PIX Query disabled: replace configured patient ID! :"+affinityDomain.substring(1));
				return affinityDomain.substring(1);
			}
		}
		if ( this.pixQueryServiceName == null ) {
			log.info("PIX Query disabled: use source patient ID!");
			patID+="^^^";
			if ( issuer == null ) return patID;
			return patID+issuer;
		} else {
	        try {
	            if ( localDomain != null ) {
	                if ( localDomain.charAt(0) == '=') {
	                    String oldIssuer = issuer;
	                    issuer = localDomain.substring(1);
	                    log.info("PIX Query: Local affinity domain changed from "+oldIssuer+" to "+issuer);
	                } else if ( issuer == null ) {
	                    log.info("PIX Query: Unknown local affinity domain changed to "+issuer);
	                    issuer = localDomain;
	                }
	            } else if ( issuer == null ) {
	                issuer = "";
	            }
	            List pids = (List) server.invoke(this.pixQueryServiceName,
	                    "queryCorrespondingPIDs",
	                    new Object[] { patID, issuer, new String[]{affinityDomain} },
	                    new String[] { String.class.getName(), String.class.getName(), String[].class.getName() });
	            String pid;
	            for ( Iterator iter = pids.iterator() ; iter.hasNext() ; ) {
	            	pid = (String) iter.next();
	            	if ( isFromDomain(pid) ) {
	            		return pid;
	            	}
	            }
            	log.error("Patient ID is not known in Affinity domain:"+affinityDomain);
	        	return null;
	        } catch (Exception e) {
	            log.error("Failed to get patientID for Affinity Domain:", e);
	            return null;
	        }
		}
	}
	/**
	 * @param pid
	 * @return
	 */
	private boolean isFromDomain(String pid) {
		int pos = 0;
		for ( int i = 0 ; i < 3 ; i++) {
			pos = pid.indexOf('^', pos);
			if ( pos == -1 ) {
				log.warn("patient id does not contain domain (issuer)! :"+pid);
				return false;
			}
			pos++;
		}
		return pid.substring(pos).equals(this.affinityDomain);
	}
	/**
	 * @param response
	 * @return
     * @throws SOAPException
	 */
	private boolean checkResponse(SOAPMessage response) throws SOAPException {
		log.info("checkResponse:"+response);
		try {
//OK i give up! response.getSOAPBody() doesnt work (can not get/create Envelope due to an namespace	trouble ?!)
//So i read the DOM from SOAPPart content! 
//TODO somehow who know the right xalan, xerces, jaxp, ssaj,.. version mix to get it work 			
			JAXMStreamSource src = (JAXMStreamSource) response.getSOAPPart().getContent();
			NodeList nl;
	        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setNamespaceAware(true);
            DocumentBuilder builder = dbFactory.newDocumentBuilder();
	        Document d = builder.parse( src.getInputStream() );
			nl = d.getElementsByTagName("RegistryResponse");
			log.debug("RegistryResponse NodeList:"+nl);
			if ( nl.getLength() != 0  ) {
				Node n = nl.item(0);
				String status = n.getAttributes().getNamedItem("status").getNodeValue();
				log.info("XDSI: SOAP response status."+status);
				if ( "Failure".equals(status) ) {
					NodeList errors = d.getElementsByTagName("RegistryError");
					StringBuffer sb = new StringBuffer();
					Node errNode;
					for ( int i = 0, len=errors.getLength(); i < len ; i++ ) {
					    sb.setLength(0); 
					    sb.append("Error (").append(i).append("):");
					    if ( (errNode = errors.item(i)) != null && errNode.getFirstChild() != null ) {
					        sb.append( errNode.getFirstChild().getNodeValue());
					    }
						log.info(sb.toString());
					}
					return false;
				}
			} else {
				log.warn("XDSI: SOAP response without RegistryResponse!");
			}
			return true;
		} catch ( Exception x ) {
			log.error("Cant check response!", x);
			return false;
		}
	}
	/**
	 * @param message
	 * @return
     * @throws IOException
     * @throws SOAPException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	private void dumpSOAPMessage(SOAPMessage message) throws SOAPException, IOException, ParserConfigurationException, SAXException {
	    if ( ! logSOAPMessage ) return;
		Source s = message.getSOAPPart().getContent();
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            out.write("SOAP message:".getBytes());
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.transform(s, new StreamResult(out));
            log.debug(out.toString());
        } catch (Exception e) {
            log.warn("Failed to log SOAP message", e);
        }
	}
	
	private Document getDocumentFromMessage( SOAPMessage message ) throws SOAPException, ParserConfigurationException, SAXException, IOException {
		JAXMStreamSource src = (JAXMStreamSource) message.getSOAPPart().getContent();
		if ( dbFactory == null ) {
	        dbFactory = DocumentBuilderFactory.newInstance();
	        dbFactory.setNamespaceAware(true);
		}
        DocumentBuilder builder = dbFactory.newDocumentBuilder();
        Document d = builder.parse( src.getInputStream() );
        NodeList nl = d.getElementsByTagNameNS("urn:oasis:names:tc:ebxml-regrep:registry:xsd:2.1","SubmitObjectsRequest");
        nl.item(0);
        return d;
	}
	
	private Document readXMLFile(File xmlFile){
        Document document = null;
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        try {
            dbFactory.setNamespaceAware(true);
            DocumentBuilder builder = dbFactory.newDocumentBuilder();
            document = builder.parse(xmlFile);
        } catch (Exception x) {
        	log.error("Cant read xml file:"+xmlFile, x);
        }
        return document;
    }
        
    protected void startService() throws Exception {
        server.addNotificationListener(ianScuServiceName,
                ianListener, IANScuService.NOTIF_FILTER, null);
    }

    protected void stopService() throws Exception {
        server.removeNotificationListener(ianScuServiceName,
                ianListener, IANScuService.NOTIF_FILTER, null);
    }
    
    private void onIAN(Dataset mpps) {
        log.debug("Received mpps");log.debug(mpps);
        if (Arrays.asList(autoPublishAETs).indexOf(
                mpps.getString(Tags.PerformedStationAET)) != -1) {
            List iuids = getIUIDS(mpps);
            log.debug("iuids:"+iuids);
            Dataset manifest = getKeyObject(iuids, getAutoPublishRootInfo(mpps), null);
            log.debug("Created manifest KOS:");
            log.debug(manifest);
            try {
                sendSOAP(manifest, getAutoPublishMetadataProperties(mpps));
            } catch (SQLException x) {
                log.error("XDS-I Autopublish failed! Reason:",x );
            }
            return;
        }
        // TODO        
    }
    
    private List getIUIDS(Dataset mpps) {
        List l = new ArrayList();
        DcmElement refSerSQ = mpps.get(Tags.PerformedSeriesSeq);
        if ( refSerSQ != null ) {
            Dataset item;
            DcmElement refSopSQ;
            for ( int i = 0 ,len = refSerSQ.countItems() ; i < len ; i++){
                refSopSQ = refSerSQ.getItem(i).get(Tags.RefImageSeq);
                for ( int j = 0 ,len1 = refSerSQ.countItems() ; j < len1 ; j++){
                    item = refSopSQ.getItem(j);
                    l.add( item.getString(Tags.RefSOPInstanceUID));
                }
            }
        }
        return l;
    }
    private Dataset getAutoPublishRootInfo(Dataset mpps) {
        Dataset rootInfo = DcmObjectFactory.getInstance().newDataset();
        DcmElement sq = rootInfo.putSQ(Tags.ConceptNameCodeSeq);
        Dataset item = sq.addNewItem();
        StringTokenizer st = new StringTokenizer(autoPublishDocTitle,"^");
        item.putSH(Tags.CodeValue,st.hasMoreTokens() ? st.nextToken():"autoPublish");
        item.putLO(Tags.CodeMeaning, st.hasMoreTokens() ? st.nextToken():"default doctitle for autopublish");
        item.putSH(Tags.CodingSchemeDesignator,st.hasMoreTokens() ? st.nextToken():null);
        return rootInfo;
    }
    
    private Properties getAutoPublishMetadataProperties(Dataset mpps) {
        Properties props = new Properties();
        BufferedInputStream bis = null;
        try {
            if (autoPublishPropertyFile == null ) return props;
            File propFile = FileUtils.resolve(this.autoPublishPropertyFile);
            bis= new BufferedInputStream( new FileInputStream( propFile ));
            props.load(bis);
            return props;
        } catch (IOException x) {
            log.error("Cant read Metadata Properties for AutoPublish!",x);
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException ignore) {}
            }
        }
        return props;
    }

    private Dataset getKeyObject(Collection iuids, Dataset rootInfo, List contentItems) {
        Object o = null;
        try {
            o = server.invoke(keyObjectServiceName,
                    "getKeyObject",
                    new Object[] { iuids, rootInfo, contentItems },
                    new String[] { Collection.class.getName(), Dataset.class.getName(), Collection.class.getName() });
        } catch (RuntimeMBeanException x) {
            log.warn("RuntimeException thrown in KeyObject Service:"+x.getCause());
            throw new IllegalArgumentException(x.getCause().getMessage());
        } catch (Exception e) {
            log.warn("Failed to create Key Object:", e);
            throw new IllegalArgumentException("Error: KeyObject Service cant create manifest Key Object! Reason:"+e.getClass().getName());
        }
        return (Dataset) o;
    }

}
