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

package org.dcm4chee.xdsb.repository.web;

import java.io.IOException;

import javax.activation.DataHandler;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.soap.SOAPMessage;

import org.jboss.mx.util.MBeanServerLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XDSbWebRetrieveServlet extends HttpServlet {

    private static MBeanServer server;

    private Logger log = LoggerFactory.getLogger(XDSbWebRetrieveServlet.class);

    private ObjectName xdsbRepoServiceName;

    /**
     * Handles the POST requset in the doGet method.
     * 
     * @param rq   The http request.
     * @param rsp  The http response.
     */
    public void doPost( HttpServletRequest rq, HttpServletResponse rsp ){
        doGet( rq, rsp);
    }

    /**
     * Handles the GET requset.
     * 
     * @param rq   The http request.
     * @param rsp  The http response.
     */
    public void doGet( HttpServletRequest rq, HttpServletResponse rsp ){
        String docUID = rq.getParameter("docUID");
        String repoUid = rq.getParameter("repoUID");
        String homeId = rq.getParameter("homeID");
        log.info("HANDLE XDSbWebRetrieve REQUEST! docUID:"+docUID+" repoUID:"+repoUid+" homeId:"+homeId);
        try {
            DataHandler dh = getDocument(docUID, repoUid, homeId);
            if ( dh != null ) {
                rsp.setStatus(HttpServletResponse.SC_OK);
                rsp.setContentType(dh.getContentType());
                dh.writeTo(rsp.getOutputStream());
            } else {
                rsp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            log.error("Error getting XDS Document "+docUID+" from repository "+repoUid+" (homeCommunity:"+homeId+")", e);
            rsp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            
        }
    }
    
    private DataHandler getDocument(String docUID, String repoUid, String homeId) throws InstanceNotFoundException, MBeanException, ReflectionException {
        return (DataHandler) server.invoke(xdsbRepoServiceName,
                "retrieveDocument",
                new Object[] { docUID, repoUid, homeId, Boolean.TRUE },
                new String[] { String.class.getName(), 
                               String.class.getName(),
                               String.class.getName(),
                               "boolean"} );   
    }

    public void init(ServletConfig config) throws ServletException {
        if (server == null) {
            server = MBeanServerLocator.locate();
            String s = config.getInitParameter("xdsbRepoServiceName");
            try {
                xdsbRepoServiceName = new ObjectName(s);
    
            } catch (Exception e) {
                log.error( "Exception in init! Servlet init parameter 'xdsbRepoServiceName' not valid",e );
            }
        }
    }    
}
