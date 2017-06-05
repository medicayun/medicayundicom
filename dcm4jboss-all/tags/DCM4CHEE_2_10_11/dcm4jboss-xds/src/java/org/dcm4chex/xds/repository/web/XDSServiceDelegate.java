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

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.servlet.ServletConfig;
import javax.xml.soap.SOAPMessage;

import org.apache.log4j.Logger;
import org.dcm4chex.xds.common.XDSResponseObject;
import org.dcm4chex.xds.mbean.XDSRegistryResponse;
import org.jboss.mx.util.MBeanServerLocator;

/**
 * @author franz.willer@gwi-ag.com
 * @version $Revision: 2389 $ $Date: 2006-04-05 17:55:23 +0800 (周三, 05 4月 2006) $
 * @since Mar 08, 2006
 */
public class XDSServiceDelegate {

    private static ObjectName xdsServiceName = null;
	private static MBeanServer server;
	
    private static Logger log = Logger.getLogger( XDSServiceDelegate.class.getName() );

    /** 
     * Iinitialize the XDS service delegator.
     * <p>
     * Set the name of the XDSService MBean with the servlet config param 'xdsServiceName'.
     * 
     * @param config The ServletConfig object.
     */
	public void init( ServletConfig config ) {
        if (server != null) return;
        server = MBeanServerLocator.locate();
        String s = config.getInitParameter("xdsServiceName");
        try {
			xdsServiceName = new ObjectName(s);
			
		} catch (Exception e) {
			log.error( "Exception in init! Servlet init parameter 'xdsServiceName' not valid",e );
		}
     
    }

	
	
	/**
	 * Makes the MBean call to export the document packed in a SOAP message.
	 * 
	 * @param msg	The document packed in a SOAP message.
	 * 
	 * @return The XDS response object.
	 */
	public XDSResponseObject exportDocument( SOAPMessage msg ) {
		XDSResponseObject resp = null;
		try {
			resp = (XDSResponseObject) server.invoke(xdsServiceName,
	                "exportDocument",
	                new Object[] { msg },
	                new String[] { SOAPMessage.class.getName() } );
		} catch ( Exception x ) {
			log.error( "Exception occured in exportDocument: "+x.getMessage(), x );
			resp = new XDSRegistryResponse( false, "Unexpected error in XDS service !: "+x.getMessage(),x);
		}
        return resp;
	}
	
}
