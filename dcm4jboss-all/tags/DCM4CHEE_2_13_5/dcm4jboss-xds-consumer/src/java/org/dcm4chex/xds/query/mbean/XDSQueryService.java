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
package org.dcm4chex.xds.query.mbean;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.management.ObjectName;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.xml.registry.BulkResponse;
import javax.xml.soap.SOAPException;

import org.apache.log4j.Logger;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.system.server.ServerConfigLocator;

/**
 * @author franz.willer@gwi-ag.com
 * @version $Revision: 6078 $ $Date: 2008-02-25 21:30:10 +0800 (周一, 25 2月 2008) $
 * @since Mar, 2007
 */
public class XDSQueryService extends ServiceMBeanSupport {

	private static Logger log = Logger.getLogger(XDSQueryService.class.getName());

    private XDSStoredQuery xdsQuery;
    
// http attributes to document registry actor (synchron) 	
	private String proxyHost;
	private int proxyPort;

    private String keystoreURL = "resource:identity.p12";
	private String keystorePassword;
    private String trustStoreURL = "resource:cacerts.jks";
	private String trustStorePassword;
	private HostnameVerifier origHostnameVerifier = null;
	private String allowedUrlHost = null;

    private String xdsQueryURI;

    private ObjectName pixQueryServiceName;
    private String affinityDomain;

	public XDSQueryService() {
           this.xdsQuery = new XDSStoredQuery( this );
	}
//http

	/**
     * @return the xdsQueryURI
     */
    public String getXDSQueryURI() {
        return xdsQueryURI;
    }
    /**
     * @param xdsQueryURI the xdsQueryURI to set
     */
    public void setXDSQueryURI(String xdsQueryURI) {
        this.xdsQueryURI = xdsQueryURI;
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
	public void setKeyStorePassword(String keyStorePassword) {
		this.keystorePassword = keyStorePassword;
	}
	/**
	 * @return Returns the keyStoreURL.
	 */
	public String getKeyStoreURL() {
		return keystoreURL;
	}
	/**
	 * @param keyStoreURL The keyStoreURL to set.
	 */
	public void setKeyStoreURL(String keyStoreURL) {
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
	
    public String getAffinityDomain() {
        return affinityDomain;
    }

    public void setAffinityDomain(String affinityDomain) {
        this.affinityDomain = affinityDomain;
    }

    public final ObjectName getPixQueryServiceName() {
        return pixQueryServiceName;
    }

    public final void setPixQueryServiceName(ObjectName name) {
        this.pixQueryServiceName = name;
    }
    
	
    private String replace(String s, String pattern, String replace) {
		int pos = s.indexOf(pattern);
		if ( pos == -1 ) return s;
		String s1 = s.substring(0,pos);
		String s2 = s.substring(pos+pattern.length());
		s = s1+replace+s2;
		return replace(s,pattern,replace);
	}

    public BulkResponse findDocuments(String patId, String status, boolean useLeafClass) throws SOAPException {
        return xdsQuery.findDocuments( getAffinityDomainPatientID(patId), status, useLeafClass);
    }

    public BulkResponse findFolders(String patId, String status, boolean useLeafClass) throws SOAPException {
        return xdsQuery.findFolders(getAffinityDomainPatientID(patId), status, useLeafClass);
    }

    public BulkResponse findSubmissionSets(String patId, String status) throws SOAPException {
        return xdsQuery.findSubmissionSets( getAffinityDomainPatientID(patId), status);
    }

    public BulkResponse getAll(String patId, String docStatus, String submissionSetStatus, String folderStatus) throws SOAPException {
        return xdsQuery.getAll( getAffinityDomainPatientID(patId), docStatus, submissionSetStatus, folderStatus);
    }
    
    public BulkResponse getDocuments(String uuids) throws SOAPException {
        return getDocuments(getListString(uuids));
    }
    public BulkResponse getDocuments(List uuids) throws SOAPException {
        return xdsQuery.getDocuments(uuids);
    }
    public BulkResponse getFolders(String uuids) throws SOAPException {
        return getFolders(getListString(uuids));
    }
    public BulkResponse getFolders(List uuids) throws SOAPException {
        return xdsQuery.getFolders(uuids);
    }

    public BulkResponse getAssociations(String uuids) throws SOAPException {
        return getAssociations(getListString(uuids));
    }
    public BulkResponse getAssociations(List uuids) throws SOAPException {
        return xdsQuery.getAssociations(uuids);
    }

    public BulkResponse getDocumentsAndAssocs(String uuids) throws SOAPException {
        return getDocumentsAndAssocs(getListString(uuids));
    }
    public BulkResponse getDocumentsAndAssocs(List uuids) throws SOAPException {
        return xdsQuery.getDocumentsAndAssocs(uuids);
    }

    public BulkResponse getSubmissionSets(String uuids) throws SOAPException {
        return getSubmissionSets(getListString(uuids));
    }
    public BulkResponse getSubmissionSets(List uuids) throws SOAPException {
        return xdsQuery.getSubmissionSets(uuids);
    }
    
    
    public BulkResponse getSubmissionSetAndContents(String uuid) throws SOAPException {
        return xdsQuery.getSubmissionSetAndContents(uuid);
    }
    
    public BulkResponse getFolderAndContents(String uuid) throws SOAPException {
        return xdsQuery.getFolderAndContents(uuid);
    }

    public BulkResponse getFoldersForDocument(String uuid) throws SOAPException {
        return xdsQuery.getFoldersForDocument(uuid);
    }

    public BulkResponse getRelatedDocuments(String uuid, String assocTypes) throws SOAPException {
        return getRelatedDocuments(uuid, getListString(assocTypes));
    }
    public BulkResponse getRelatedDocuments(String uuid, List assocTypes) throws SOAPException {
        return xdsQuery.getRelatedDocuments( uuid, assocTypes);
    }

    private ArrayList getListString(String uuids) {
        StringTokenizer st = new StringTokenizer(uuids,",");
        ArrayList l = new ArrayList();
        while ( st.hasMoreTokens() ) {
            l.add( st.nextToken().trim() );
        }
        return l;
    }
 
    protected String getAffinityDomainPatientID(String patId) {
		if ( affinityDomain.charAt(0) == '=') {
			if ( affinityDomain.length() == 1 ) {
				log.info("PIX Query disabled: use patId: "+patId);
				return patId;
			} else if (affinityDomain.charAt(1)=='?') {
				log.info("PIX Query disabled: replace issuer with affinity domain! orig PatId:"+patId);
				int pos = patId.indexOf("^^^");
				if ( pos != -1 ) patId = patId.substring(0,pos);
				log.debug("patID changed! new patId:"+patId+"^^^"+affinityDomain.substring(2)+")");
				return patId+"^^^"+affinityDomain.substring(2);
			} else {
				log.info("PIX Query disabled: replace configured patient ID! :"+affinityDomain.substring(1));
				return affinityDomain.substring(1);
			}
		}
        if ( this.pixQueryServiceName == null ) {
            log.info("PIX Query disabled: use source patient ID!");
            return patId+"^^^"+affinityDomain;
        } else {
            try {
                List pids = (List) server.invoke(this.pixQueryServiceName,
                        "queryCorrespondingPIDs",
                        new Object[] { patId, "", new String[]{affinityDomain} },
                        new String[] { String.class.getName(), String.class.getName(), String[].class.getName() });
                String pid;
                for ( Iterator iter = pids.iterator() ; iter.hasNext() ; ) {
	            	pid = toPIDString((String[]) iter.next());
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

	private String toPIDString(String[] pid) {
		if (pid == null || pid.length < 1) return "";
		StringBuffer sb = new StringBuffer(pid[0]);
		log.debug("pid[0]:"+pid[0]);
		if ( pid.length > 1 ) {
			sb.append("^^^").append(pid[1]);
			log.debug("pid[1]:"+pid[1]);
		}
        for (int i = 2 ; i < pid.length; i++) {
            sb.append('&').append(pid[i]);
    		log.debug("pid["+i+"]:"+pid[i]);
        }
        return sb.toString();
	}

    protected void configProxyAndTLS(String url) {
        String protocol = url.startsWith("https") ? "https" : "http";
        if ( proxyHost != null && proxyHost.trim().length() > 1 ) {
            System.setProperty( protocol+".proxyHost", proxyHost);
            System.setProperty(protocol+".proxyPort", String.valueOf(proxyPort));
        } else {
            System.setProperty(protocol+".proxyHost", "");
            System.setProperty(protocol+".proxyPort", "");
        }
        if ( "https".equals(protocol) && trustStoreURL != null ) {
            String keyStorePath = resolvePath(keystoreURL);
            String trustStorePath = resolvePath(trustStoreURL);
            System.setProperty("javax.net.ssl.keyStore", keyStorePath);
            if ( keystorePassword != null ) 
                System.setProperty("javax.net.ssl.keyStorePassword", keystorePassword);
            System.setProperty("javax.net.ssl.keyStoreType","PKCS12");
            System.setProperty("javax.net.ssl.trustStore", trustStorePath);
            if ( trustStorePassword != null )
                System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);
            if ( origHostnameVerifier == null) {
                origHostnameVerifier = HttpsURLConnection.getDefaultHostnameVerifier();
                HostnameVerifier hv = new HostnameVerifier() {
                    public boolean verify(String urlHostName, SSLSession session) {
                        if ( !origHostnameVerifier.verify ( urlHostName, session)) {
                            if ( isAllowedUrlHost(urlHostName)) {
                                log.warn("HostnameVerifier: URL Host: "+urlHostName+" vs. "+session.getPeerHost());
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
        
    }
    public static String resolvePath(String fn) {
        File f = new File(fn);
        if (f.isAbsolute()) return f.getAbsolutePath();
        File serverHomeDir = ServerConfigLocator.locate().getServerHomeDir();
        return new File(serverHomeDir, f.getPath()).getAbsolutePath();
    }
    
}
