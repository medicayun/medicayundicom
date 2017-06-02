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

package org.dcm4chee.xds.common.delegate;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.dcm4chee.xds.common.XDSConstants;
import org.dcm4chee.xds.common.exception.XDSException;
import org.dcm4chee.xds.common.infoset.ObjectFactory;
import org.jboss.mx.util.MBeanServerLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author franz.willer@gwi-ag.com
 * @version $Revision: 2389 $ $Date: 2006-04-05 11:55:23 +0200 (Mi, 05 Apr 2006) $
 * @since Mar 08, 2006
 */
public  class XdsHttpCfgDelegate {
    public static final int CFG_RSP_OK = 1;
    public static final int CFG_RSP_ERROR = -1;
    public static final int CFG_RSP_IGNORED = 0;
    public static final int CFG_RSP_ALREADY = 2;

    private static final String DEFAULT_XDS_HTTP_CFG_SERVICE_NAME = "dcm4chee.xds:service=XdsHttpCfgService";
    private static ObjectName xdsHttpCfgServiceName = null;
    private static MBeanServer server;
    private ObjectFactory objFac = new ObjectFactory();

    private Logger log = LoggerFactory.getLogger(XdsHttpCfgDelegate.class);
    private boolean tlsIsConfigured = false;

    public XdsHttpCfgDelegate() {
        init();
    }
    public void init() {
        if (server != null) return;
        server = MBeanServerLocator.locate();
        String s = DEFAULT_XDS_HTTP_CFG_SERVICE_NAME;
        try {
            xdsHttpCfgServiceName = new ObjectName(s);
        } catch (Exception e) {
            log.error( "Exception in init! Cant create ObjectName for "+s,e );
        }
    }

    public static ObjectName getXdsRepositoryServiceName() {
        return xdsHttpCfgServiceName;
    }
    public static void setXdsRepositoryServiceName(
            ObjectName xdsHttpCfgServiceName) {
        XdsHttpCfgDelegate.xdsHttpCfgServiceName = xdsHttpCfgServiceName;
    }
    /**
     * Makes the MBean call to export the document packed in a SOAP message.
     * @param submitRequest 
     * 
     * @param msg The document(s) packed in a SOAP message.
     * 
     * @return List of StoredDocument objects.
     */
    public int configTLS( String url ) {
        if (tlsIsConfigured) {
            log.debug("Already configured! Omit call of HttpCfgService!");
            return CFG_RSP_ALREADY;
        }
        try {
            int rsp = ((Integer) server.invoke(xdsHttpCfgServiceName,
                    "configTLS",
                    new Object[] { url },
                    new String[] { String.class.getName() } ) ).intValue();
            if ( rsp == CFG_RSP_OK || rsp == CFG_RSP_ALREADY ) {
                tlsIsConfigured  = true;
            }
            log.debug("configTLS of HttpCfgService return (1..OK, 2..already configured, 0..ignored, -1..error):"+rsp);
            return rsp;
        } catch ( Exception x ) {
            log.error( "Exception occured in configTLS: "+x.getMessage(), x );
            return CFG_RSP_ERROR;
        }
    }
}
