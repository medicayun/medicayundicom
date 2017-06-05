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
package org.dcm4chex.xds.repository.web;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.apache.log4j.Logger;
import org.dcm4chex.xds.common.XDSResponseObject;
import org.dcm4chex.xds.mbean.XDSRegistryResponse;

/**
 * @author franz.willer@gwi-ag.com
 * @version $Revision: 2390 $ $Date: 2006-04-05 17:55:57 +0800 (周三, 05 4月 2006) $
 * @since Mar 08, 2006
 */
public class XDSRepositoryServlet extends HttpServlet {

	private static final int BUFFER_SIZE = 65535;
	private MessageFactory messageFactory;
	
	private XDSServiceDelegate delegate; 

	private List storedFiles = new ArrayList();
	/**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 1L;

	private static Logger log = Logger.getLogger(XDSRepositoryServlet.class.getName());
	
    public void init(ServletConfig config) throws ServletException {
        try {
        	messageFactory = MessageFactory.newInstance();
        	delegate = new XDSServiceDelegate();
        	delegate.init(config);
        } catch (SOAPException e) {
            throw new ServletException("Failed to create MessageFactory" , e);
        }
        
    }
	
	public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException{
		log.info("XDSRepositoryServlet.doGet called from "+request.getRemoteHost()+" !"+" doTest:"+request.getParameter("doTest"));
		if ( request.getParameter("doTest") != null) {
			doTest(request, response);
		} else {
			response.setContentType("text/plain");
			PrintWriter w = response.getWriter();
			w.println("You call this service using GET method (with a Browser?).");
			w.println("To use this Service you need a proper SOAP Client (using POST method)!");
			w.println("Nonetheless you can simply test this service with a Browser by setting request parameter doTest!");
			w.close();
		}
	}
	/**
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	private void doTest(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setContentType("text/plain");
		PrintWriter w = response.getWriter();
		w.println("Test not implemented!");
		w.close();
	}
	public void doPost( HttpServletRequest request, HttpServletResponse response ) throws IOException{
		log.info("XDSRepositoryServlet.doPost called from "+request.getRemoteHost()+" with URL "+request.getRequestURL()+" ! (secure :"+request.isSecure()+")");
		StringBuffer sb = new StringBuffer();
        BufferedInputStream bis = new BufferedInputStream(request.getInputStream());
        SOAPMessage message = null;
        XDSResponseObject xdsResponse;
        try{
            message = messageFactory.createMessage(getMimeHeaders(request),bis);
    		log.info("SOAP message parsed! "+request.getRemoteHost()+" !");
    		xdsResponse = delegate.exportDocument(message);
    		log.info("Export done! remoteHost:"+request.getRemoteHost()+" ! xdsResponse:"+xdsResponse);
        } catch (Exception x) {
        	xdsResponse = new XDSRegistryResponse( false, "Unexpected error in XDS service !: "+x.getMessage(),x);
		}
        try {
        	xdsResponse.execute(response);
        } catch (Throwable t) {
        	log.info("Sending response failed! remoteHost:"+request.getRemoteHost());
        }
	}

	public static MimeHeaders getMimeHeaders(HttpServletRequest request) {
        MimeHeaders mimeHeaders = new MimeHeaders();
        String name, value;
        for (Enumeration names = request.getHeaderNames() ; names.hasMoreElements() ; ) {
            name = (String) names.nextElement();
            value = request.getHeader(name);
            for (StringTokenizer st = new StringTokenizer(value, ",") ; st.hasMoreTokens() ; ) {
                mimeHeaders.addHeader(name, st.nextToken().trim());
            }
        }
        return mimeHeaders;
    }
	
	
}
