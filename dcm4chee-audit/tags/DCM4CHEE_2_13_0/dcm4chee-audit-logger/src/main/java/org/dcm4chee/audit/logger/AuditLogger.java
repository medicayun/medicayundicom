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
 * Agfa-Gevaert Group.
 * Portions created by the Initial Developer are Copyright (C) 2003-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See @authors listed below.
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

package org.dcm4chee.audit.logger;

import java.security.Principal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.management.AttributeChangeNotification;
import javax.management.AttributeChangeNotificationFilter;
import javax.management.MBeanServerNotification;
import javax.management.MalformedObjectNameException;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.security.jacc.PolicyContext;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.dcm4che2.audit.message.ApplicationActivityMessage;
import org.dcm4che2.audit.message.AuditEvent;
import org.dcm4che2.audit.message.AuditMessage;
import org.dcm4che2.audit.message.AuditSource;
import org.dcm4che2.audit.message.SecurityAlertMessage;
import org.jboss.security.SecurityAssociation;
import org.jboss.system.ServiceMBeanSupport;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Feb 25, 2007
 */
public class AuditLogger extends ServiceMBeanSupport {

    private static final String MBEAN_SERVER_DELEGATE =
            "JMImplementation:type=MBeanServerDelegate";

    private static final String REGISTER_ACNL =
            "Register ACN Listener to ";

    private static final String DEFER_REGISTER_ACNL =
            "Defer to register ACN Listener to ";

    private static final String UNREGISTER_ACNL =
            "Unregister ACN Listener from ";

    private static final String FAILED_TO_REGISTER_ACNL =
            "Failed to register ACN Listener to ";

    private static final String FAILED_TO_UNREGISTER_ACNL =
            "Failed to unregister ACN Listener from ";

    private static final String WEB_REQUEST_KEY =
            "javax.servlet.http.HttpServletRequest";

    private static final String[] TYPES = { "Network", "Security", "Hardware" };

    private static final AuditEvent.TypeCode[] TYPE_CODES = {
            SecurityAlertMessage.SOFTWARE_CONFIGURATION,
            SecurityAlertMessage.NETWORK_CONFIGURATION,
            SecurityAlertMessage.SECURITY_CONFIGURATION,
            SecurityAlertMessage.HARDWARE_CONFIGURATION };

    private static final NotificationFilter REG_NOTIF_FILTER =
            new NotificationFilter() {

        private static final long serialVersionUID = 6373294028145260060L;

        public boolean isNotificationEnabled(Notification notif) {
            return notif.getType().equals(
                    MBeanServerNotification.REGISTRATION_NOTIFICATION);
        }
    };

    private static final NotificationFilter UNREG_NOTIF_FILTER =
            new NotificationFilter() {

        private static final long serialVersionUID = 6373294028145260060L;

        public boolean isNotificationEnabled(Notification notif) {
            return notif.getType().equals(
                    MBeanServerNotification.UNREGISTRATION_NOTIFICATION);
        }
    };

    private AuditSource auditSource = AuditSource.getDefaultAuditSource();

    private String aclConfURL;

    private HashMap notRegisteredAcnSources;

    private HashMap registeredAcnSources = new HashMap();

    public boolean isIHEYr4() {
        return false;
    }

    public String getAuditSourceID() {
        return auditSource.getAuditSourceID();
    }

    public void setAuditSourceID(String id) {
        auditSource.setAuditSourceID(id);
    }

    private static String maskNull(String val, String nullVal) {
        return val == null ? nullVal : val;
    }

    private static String umaskNull(String val, String nullVal) {
        return nullVal.equals(val) ? null : val;
    }

    public String getAuditEnterpriseSiteID() {
        return maskNull(auditSource.getAuditEnterpriseSiteID(), "-");
    }

    public void setAuditEnterpriseSiteID(String id) {
        auditSource.setAuditEnterpriseSiteID(umaskNull(id, "-"));
    }

    public String getAuditSourceTypeCodes() {
        List l = auditSource.getAuditSourceTypeCodes();
        if (l.isEmpty()) {
            return "-";
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0, n = l.size(); i < n; i++) {
            if (i != 0) {
                sb.append(',');
            }
            sb.append(((AuditSource.TypeCode) l.get(i)).getCode());
        }
        return sb.toString();
    }

    public void setAuditSourceTypeCodes(String codes) {
        if (codes == null || codes.length() == 0 || "-".equals(codes)) {
            auditSource.clearAuditSourceTypeCodes();
            return;
        }
        String[] ss = codes.split(",");
        AuditSource.TypeCode[] types = new AuditSource.TypeCode[ss.length];
        for (int i = 0; i < types.length; i++) {
            types[i] = AuditSource.TypeCode.valueOf(ss[i]);
        }
        auditSource.clearAuditSourceTypeCodes();
        for (int i = 0; i < types.length; i++) {
            auditSource.addAuditSourceTypeCode(types[i]);
        }
    }

    public String getProcessID() {
        return AuditMessage.getProcessID();
    }

    public String getProcessName() {
        return AuditMessage.getProcessName();
    }

    public void setProcessName(String processName) {
        AuditMessage.setProcessName(processName);
    }

    public String getLocalAETitles() {
        return formatAETs(AuditMessage.getLocalAETitles());
    }

    private String formatAETs(String[] aets) {
        if (aets == null || aets.length == 0) {
            return "-";
        }
        if (aets.length == 1) {
            return aets[0];
        }
        StringBuffer sb = new StringBuffer(aets[0]);
        for (int i = 1; i < aets.length; i++) {
            sb.append('\\').append(aets[i]);
        }
        return null;
    }

    public void setLocalAETitles(String aets) {
        AuditMessage.setLocalAETitles(splitAETs(aets));
    }

    private String[] splitAETs(String aets) {
        if (aets == null) {
            return null;
        }
        String trim = aets.trim();
        if (trim.length() == 0 || "-".equals(trim)) {
            return null;
        }
        return trim.split("\\\\");
    }

    public boolean isEnableDNSLookups() {
        return AuditMessage.isEnableDNSLookups();
    }

    public void setEnableDNSLookups(boolean enableDNSLookups) {
        AuditMessage.setEnableDNSLookups(enableDNSLookups);
    }

    public boolean isIncludeXMLDeclaration() {
        return AuditMessage.isIncludeXMLDeclaration();
    }

    public void setIncludeXMLDeclaration(boolean incXMLDecl) {
        AuditMessage.setIncludeXMLDeclaration(incXMLDecl);
    }

    public boolean isTimezonedDateTime() {
        return AuditMessage.isTimezonedDateTime();
    }

    public void setTimezonedDateTime(boolean timezonedDateTime) {
        AuditMessage.setTimezonedDateTime(timezonedDateTime);
    }

    public boolean isUtcDateTime() {
        return AuditMessage.isUtcDateTime();
    }

    public void setUtcDateTime(boolean utcDateTime) {
        AuditMessage.setUtcDateTime(utcDateTime);
    }

    public final String getAttributeChangeConfigurationURL() {
        return aclConfURL;
    }

    public final void setAttributeChangeConfigurationURL(String url)
            throws Exception {
        HashMap tmp = new HashMap();
        SAXParser p = SAXParserFactory.newInstance().newSAXParser();
        p.parse(url, new ConfigHandler(tmp));
        this.aclConfURL = url;
        boolean started = getState() == STARTED;
        if (started) {
            unregisterAcnListeners();
        }
        this.notRegisteredAcnSources = tmp;
        if (started) {
            registerAcnListeners();
        }
    }

    protected void startService() throws Exception {
        registerMBeanServerListeners();
        registerAcnListeners();
        auditApplicationActivity(ApplicationActivityMessage.APPLICATION_START);
    }

    protected void stopService() {
        unregisterAcnListeners();
        unregisterMBeanServerListeners();
        auditApplicationActivity(ApplicationActivityMessage.APPLICATION_STOP);
    }

    private void auditApplicationActivity(AuditEvent.TypeCode type) {
        ApplicationActivityMessage msg = new ApplicationActivityMessage(type);
        msg.addApplication(AuditMessage.getProcessID(), AuditMessage
                .getLocalAETitles(), AuditMessage.getProcessName(),
                AuditMessage.getLocalHostName());
        msg.addApplicationLauncher(getPrincipal(), null, null, null);
        Logger.getLogger("auditlog").info(msg);
    }

    private String getPrincipal() {
        Principal p = SecurityAssociation.getPrincipal();
        return p != null ? p.getName() : System.getProperty("user.name");
    }

    private final NotificationListener regl = new NotificationListener() {

        public void handleNotification(Notification notif, Object handback) {
            MBeanServerNotification mbsn = (MBeanServerNotification) notif;
            ObjectName source = mbsn.getMBeanName();
            AttributeChangeNotificationFilter[] acnf =
                    (AttributeChangeNotificationFilter[])
                    notRegisteredAcnSources.remove(source);
            if (acnf != null) {
                (registerAcnListener(source, acnf) ? registeredAcnSources
                        : notRegisteredAcnSources).put(source, acnf);
            }
        }
    };

    private final NotificationListener unregl = new NotificationListener() {

        public void handleNotification(Notification notif, Object handback) {
            MBeanServerNotification mbsn = (MBeanServerNotification) notif;
            ObjectName source = mbsn.getMBeanName();
            AttributeChangeNotificationFilter[] acnf =
                    (AttributeChangeNotificationFilter[])
                    registeredAcnSources.remove(source);
            if (acnf != null) {
                unregisterAcnListener(source);
                notRegisteredAcnSources.put(source, acnf);
            }
        }
    };

    private final NotificationListener acnl = new NotificationListener() {

        public void handleNotification(Notification notif, Object handback) {
            AttributeChangeNotification scn = (AttributeChangeNotification) notif;
            Object newValue = scn.getNewValue();
            Object oldValue = scn.getOldValue();
            if (newValue instanceof String && oldValue instanceof String 
                        ? ((String) newValue).trim().equals(
                                ((String) oldValue).trim())
                        : newValue.equals(oldValue)) {
                return;
            }
            SecurityAlertMessage msg = new SecurityAlertMessage(
                    (AuditEvent.TypeCode) handback);
            msg.addReportingProcess(AuditMessage.getProcessID(), AuditMessage
                    .getLocalAETitles(), AuditMessage.getProcessName(),
                    AuditMessage.getLocalHostName());
            msg.addPerformingPerson(getPrincipal(), null, null, getHostname());
            msg.addAlertSubjectWithURI("jmx:" + scn.getSource(), toText(scn));
            Logger.getLogger("auditlog").info(msg);
        }

        private String toText(AttributeChangeNotification scn) {
            return "Change " + scn.getAttributeName() + " from '"
                    + scn.getOldValue() + "' to '" + scn.getNewValue() + "'.";
        }
    };

    private static String getHostname() {
        try {
            HttpServletRequest rq = (HttpServletRequest) PolicyContext
                    .getContext(WEB_REQUEST_KEY);
            return rq != null ? rq.getRemoteHost() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private static class ConfigHandler extends DefaultHandler {

        private final HashMap sources;

        private ObjectName source;

        private AttributeChangeNotificationFilter[] acnf =
                new AttributeChangeNotificationFilter[4];

        private List types = Arrays.asList(TYPES);

        public ConfigHandler(HashMap sources) {
            this.sources = sources;
        }

        public void startElement(String uri, String localName, String qName,
                Attributes attrs) throws SAXException {
            try {
                if (source != null) {
                    if (qName.equals("attribute")) {
                        int type = types.indexOf(attrs.getValue("type")) + 1;
                        if (acnf[type] == null) {
                            acnf[type] = new AttributeChangeNotificationFilter();
                        }
                        acnf[type].enableAttribute(attrs.getValue("name"));
                    }
                } else if (qName.equals("mbean")) {
                    source = new ObjectName(attrs.getValue("name"));
                }
            } catch (Exception e) {
                throw new SAXException(e);
            }
        }

        private boolean hasAcnf() {
            return acnf[0] != null || acnf[1] != null || acnf[2] != null
                    || acnf[3] != null;
        }

        public void endElement(String uri, String localName, String qName)
                throws SAXException {
            if (qName.equals("mbean")) {
                if (hasAcnf()) {
                    sources.put(source, acnf);
                    acnf = new AttributeChangeNotificationFilter[4];
                }
                source = null;
            }
        }
    }

    private void registerAcnListeners() {
        for (Iterator iter = notRegisteredAcnSources.entrySet().iterator(); iter
                .hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            ObjectName source = (ObjectName) entry.getKey();
            AttributeChangeNotificationFilter[] acnf =
                    (AttributeChangeNotificationFilter[]) entry.getValue();
            if (registerAcnListener(source, acnf)) {
                registeredAcnSources.put(source, acnf);
                iter.remove();
            }
        }
    }

    private void registerMBeanServerListeners() throws Exception {
        ObjectName serverDelegate = new ObjectName(MBEAN_SERVER_DELEGATE);
        server.addNotificationListener(serverDelegate, regl, REG_NOTIF_FILTER,
                null);

        server.addNotificationListener(serverDelegate, unregl,
                UNREG_NOTIF_FILTER, null);
    }

    private void unregisterMBeanServerListeners() {
        ObjectName serverDelegate;
        try {
            serverDelegate = new ObjectName(MBEAN_SERVER_DELEGATE);
        } catch (MalformedObjectNameException e) {
            throw new RuntimeException(e);
        }
        try {
            server.removeNotificationListener(serverDelegate, regl);
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
        try {
            server.removeNotificationListener(serverDelegate, unregl);
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
    }

    private boolean registerAcnListener(ObjectName source,
            AttributeChangeNotificationFilter[] acnf) {
        if (!server.isRegistered(source)) {
            if (log.isDebugEnabled()) {
                log.debug(DEFER_REGISTER_ACNL + source);
            }
            return false;
        }
        if (log.isDebugEnabled()) {
            log.debug(REGISTER_ACNL + source);
        }
        int registered = 0;
        for (int i = 0; i < acnf.length; i++) {
            if (acnf[i] != null) {
                try {
                    server.addNotificationListener(source, acnl, acnf[i],
                            TYPE_CODES[i]);
                    registered++;
                } catch (Exception e) {
                    log.warn(FAILED_TO_REGISTER_ACNL + source, e);
                    if (registered == 0) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private void unregisterAcnListeners() {
        for (Iterator iter = registeredAcnSources.keySet().iterator(); iter
                .hasNext();) {
            unregisterAcnListener((ObjectName) iter.next());
        }
        notRegisteredAcnSources.putAll(registeredAcnSources);
        registeredAcnSources.clear();
    }

    private void unregisterAcnListener(ObjectName source) {
        if (server.isRegistered(source)) {
            try {
                if (log.isDebugEnabled()) {
                    log.debug(UNREGISTER_ACNL + source);
                }
                server.removeNotificationListener(source, acnl);
            } catch (Exception e) {
                log.warn(FAILED_TO_UNREGISTER_ACNL + source, e);
            }
        }
    }

}
