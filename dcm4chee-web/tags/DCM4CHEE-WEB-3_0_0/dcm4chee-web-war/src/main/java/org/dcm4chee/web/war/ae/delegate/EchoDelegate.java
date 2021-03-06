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
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa-Gevaert AG.
 * Portions created by the Initial Developer are Copyright (C) 2008
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See listed authors below.
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

package org.dcm4chee.web.war.ae.delegate;

import java.net.UnknownHostException;

import org.dcm4chee.archive.entity.AE;
import org.dcm4chee.web.common.delegate.BaseMBeanDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Franz Willer <franz.willer@gmail.com>
 * @version $Revision$ $Date$
 * @since Aug 18, 2009
 */
public class EchoDelegate extends BaseMBeanDelegate {

    private static Logger log = LoggerFactory.getLogger(EchoDelegate.class);
    
    public EchoDelegate() {
        super();
    }
    
    public String echo(AE ae, int nrOfTests) {
        log.debug("ECHO:"+ae);
        try {
            return (String) server.invoke(serviceObjectName, "echo", 
                new Object[]{ae, nrOfTests}, 
                new String[]{AE.class.getName(), int.class.getName()});
        } catch (Exception x) {
            String msg = "DICOM Echo failed! Reason:"+x.getMessage();
            log.error(msg,x);
            return msg;
        }
    }

    public boolean ping(String host) throws UnknownHostException {
        try {
            return (Boolean) server.invoke(serviceObjectName, "ping", 
                new Object[]{host}, 
                new String[]{String.class.getName()});
        } catch (UnknownHostException x) {
            throw x;
        } catch (Exception x) {
            if ( x.getCause() instanceof UnknownHostException) {
                throw (UnknownHostException) x.getCause();
            }
            log.error("ICMP PING failed! Reason:"+x.getMessage(),x);
            return false;
        }
    }

    @Override
    public String getServiceNameCfgAttribute() {
        return "echoServiceName";
    }
    
    @Override
    public String getDefaultServiceObjectName() {
        return "dcm4chee.web:service=EchoService";
    }
}
