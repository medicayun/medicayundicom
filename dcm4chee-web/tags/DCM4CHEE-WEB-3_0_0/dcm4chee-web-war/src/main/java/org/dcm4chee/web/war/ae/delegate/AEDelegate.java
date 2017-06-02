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

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.RequestCycle;
import org.apache.wicket.protocol.http.WebRequestCycle;
import org.dcm4che2.audit.message.AuditMessage;
import org.dcm4che2.audit.message.SecurityAlertMessage;
import org.dcm4chee.archive.entity.AE;
import org.dcm4chee.archive.util.JNDIUtils;
import org.dcm4chee.web.common.delegate.BaseMBeanDelegate;
import org.dcm4chee.web.dao.ae.AEHomeLocal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Franz Willer <franz.willer@gmail.com>
 * @version $Revision$ $Date$
 * @since Aug 18, 2009
 */
public class AEDelegate extends BaseMBeanDelegate {

    private static Logger log = LoggerFactory.getLogger(AEDelegate.class);
    private static Logger auditLog = LoggerFactory.getLogger("auditlog");
    private static AEDelegate singleton;
    
    private AEDelegate() {
        super();
    }
    
    public static AEDelegate getInstance() {
        if (singleton == null)
            singleton = new AEDelegate();
        return singleton;
    }
    
    public void delete(AE ae) {
        ((AEHomeLocal) JNDIUtils.lookup(AEHomeLocal.JNDI_NAME)).removeAET(ae.getPk());
        logActorConfig("Delete AE:" + ae);
        clearCache();
    }

    public void updateOrCreate(AE ae) {
        if (ae.getPk() != -1) {
            clearCache();
        }
        String updOrCreate = ae.getPk() == -1 ? "Create new AE:" : "Update AE:";
        ((AEHomeLocal) JNDIUtils.lookup(AEHomeLocal.JNDI_NAME)).updateOrCreateAET(ae);
        logActorConfig(updOrCreate + ae);
    }
    
    private void logActorConfig(String desc) {
        log.info(desc);
        try {
            HttpServletRequest rq = ((WebRequestCycle)RequestCycle.get()).getWebRequest().getHttpServletRequest();
            String userId = rq.getRemoteUser();
            if (userId == null || userId.length() < 1)
                userId = "UNKNOWN_USER";
            String xForward = (String) rq.getHeader("x-forwarded-for");
            String ip, hostName;
            if (xForward != null) {
                int pos = xForward.indexOf(',');
                ip = (pos > 0 ? xForward.substring(0,pos) : xForward).trim();
            } else {
                ip = rq.getRemoteAddr();
            }
            if (AuditMessage.isEnableDNSLookups()) {
                try {
                    hostName = InetAddress.getByName(ip).getHostName();
                } catch (UnknownHostException ignore) {
                    hostName = ip;
                }
            } else {
                hostName = ip;
            }
            SecurityAlertMessage msg = new SecurityAlertMessage(SecurityAlertMessage.NETWORK_CONFIGURATION);
            msg.addReportingProcess(AuditMessage.getProcessID(),
                    AuditMessage.getLocalAETitles(),
                    AuditMessage.getProcessName(),
                    AuditMessage.getLocalHostName());
            msg.addPerformingPerson(userId, null, null, hostName);
            msg.addAlertSubjectWithNodeID(AuditMessage.getLocalNodeID(), desc);
            msg.validate();
            auditLog.info(msg.toString());
        } catch (Exception e) {
            log.warn("Failed to log ActorConfig:", e);
        }
    }
    
    public void clearCache() {
        log.debug("Clear AE Cache!");
        try {
            server.invoke(serviceObjectName, "clearCache", 
                new Object[]{}, 
                new String[]{});
        } catch (Exception x) {
            log.error("Clear AE Cache failed!", x);
        }
    }

    @Override
    public String getServiceNameCfgAttribute() {
        return "aeServiceName";
    }
    
    @Override
    public String getDefaultServiceObjectName() {
        return "dcm4chee.archive:service=AE";
    }
}
