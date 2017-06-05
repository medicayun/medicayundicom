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
 * Portions created by the Initial Developer are Copyright (C) 2002-2005
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
package org.dcm4chex.archive.hl7;

import java.io.File;
import java.util.Collection;

import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;
import org.dcm4cheri.util.StringUtils;
import org.dcm4chex.archive.common.SeriesStored;
import org.dcm4chex.archive.dcm.mppsscp.MPPSScpService;
import org.dcm4chex.archive.ejb.interfaces.StudyPermissionDTO;
import org.dcm4chex.archive.ejb.interfaces.StudyPermissionManager;
import org.dcm4chex.archive.ejb.interfaces.StudyPermissionManagerHome;
import org.dcm4chex.archive.mbean.TemplatesDelegate;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.dcm4chex.archive.util.FileUtils;
import org.dom4j.Document;
import org.dom4j.io.DocumentSource;
import org.jboss.system.ServiceMBeanSupport;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Oct 16, 2007
 * 
 */
public class StudyPermissionService extends ServiceMBeanSupport {
    
    private ObjectName hl7ServerName;

    private ObjectName storeScpServiceName;

    private ObjectName mppsScpServiceName;

    private TemplatesDelegate templates = new TemplatesDelegate(this);
       
    private String hl7Stylesheet;
    
    private String seriesStylesheet;
    
    private String mppsStylesheet;
    
    private boolean updateOnHl7Received;
    
    private boolean updateOnSeriesStored;

    private boolean updateOnMppsCreate;

    public final ObjectName getHL7ServerName() {
        return hl7ServerName;
    }

    public final void setHL7ServerName(ObjectName hl7ServerName) {
        this.hl7ServerName = hl7ServerName;
    }

    public final ObjectName getStoreScpServiceName() {
        return storeScpServiceName;
    }

    public final void setStoreScpServiceName(ObjectName storeScpServiceName) {
        this.storeScpServiceName = storeScpServiceName;
    }

    public final ObjectName getMppsScpServiceName() {
        return mppsScpServiceName;
    }

    public final void setMppsScpServiceName(ObjectName name) {
        this.mppsScpServiceName = name;
    }

    public final ObjectName getTemplatesServiceName() {
        return templates.getTemplatesServiceName();
    }

    public final void setTemplatesServiceName(ObjectName serviceName) {
        templates.setTemplatesServiceName(serviceName);
    }
    
    public final String getHl7ReceivedStylesheet() {
        return hl7Stylesheet;
    }

    public final void setHl7ReceivedStylesheet(String xslPath) {
        this.hl7Stylesheet = xslPath;
    }

    public final String getSeriesStoredStylesheet() {
        return seriesStylesheet;
    }

    public final void setSeriesStoredStylesheet(String xslPath) {
        this.seriesStylesheet = xslPath;
    }

    public final String getMppsCreateStylesheet() {
        return mppsStylesheet;
    }

    public final void setMppsCreateStylesheet(String xslPath) {
        this.mppsStylesheet = xslPath;
    }

    public final boolean isUpdateOnHl7Received() {
        return updateOnHl7Received;
    }

    public final void setUpdateOnHl7Received(boolean updateOnHl7Received) {
        this.updateOnHl7Received = updateOnHl7Received;
    }

    public final boolean isUpdateOnMppsCreate() {
        return updateOnMppsCreate;
    }

    public final void setUpdateOnMppsCreate(boolean updateOnMppsCreate) {
        this.updateOnMppsCreate = updateOnMppsCreate;
    }

    public final boolean isUpdateOnSeriesStored() {
        return updateOnSeriesStored;
    }

    public final void setUpdateOnSeriesStored(boolean updateOnSeriesStored) {
        this.updateOnSeriesStored = updateOnSeriesStored;
    }

    private StudyPermissionManager getStudyPermissionManager()
            throws Exception {
        StudyPermissionManagerHome home = (StudyPermissionManagerHome)
                EJBHomeFactory.getFactory().lookup(
                        StudyPermissionManagerHome.class,
                        StudyPermissionManagerHome.JNDI_NAME);
        return home.create();
    }

    public boolean hasPermission(String suid, String action, String role)
            throws Exception {
        return getStudyPermissionManager().hasPermission(suid, action, role);
    }
    
    public Collection findByPatientPk(long pk) throws Exception {
        return getStudyPermissionManager().findByPatientPk(new Long(pk));
    }

    public Collection findByStudyIuid(String suid) throws Exception {
        return getStudyPermissionManager().findByStudyIuid(suid);
    }

    public Collection findByStudyIuidAndAction(String suid, String action)
            throws Exception {
        return getStudyPermissionManager().findByStudyIuidAndAction(suid,
                action);
    }

    public int grant(String suid, String actions, String role)
            throws Exception {
        // TODO Emit Audit Message
        return getStudyPermissionManager()
                .grant(suid, StringUtils.split(actions,','), role);
    }

    public boolean revoke(StudyPermissionDTO dto) throws Exception {
        // TODO Emit Audit Message
        return getStudyPermissionManager().revoke(dto);
    }

    public int grantForPatient(long patPk, String actions, String role)
            throws Exception {
        // TODO Emit Audit Message
        return getStudyPermissionManager()
                .grantForPatient(patPk, StringUtils.split(actions,','), role);
    }

    public int grantForPatient(String pid, String issuer, String actions,
            String role) throws Exception {
        // TODO Emit Audit Message
        return getStudyPermissionManager().grantForPatient(
                pid, issuer, StringUtils.split(actions,','), role);
    }
    
    public int revokeForPatient(long patPk, String actions, String role)
            throws Exception {
        // TODO Emit Audit Message
        return getStudyPermissionManager()
                .revokeForPatient(patPk, StringUtils.split(actions,','), role);
    }

    public int revokeForPatient(String pid, String issuer, String actions,
            String role) throws Exception {
        // TODO Emit Audit Message
        return getStudyPermissionManager().revokeForPatient(
                pid, issuer, StringUtils.split(actions,','), role);
    }
    
    public int countStudiesOfPatient(Long patPk) throws Exception {
        return getStudyPermissionManager().countStudiesOfPatient(patPk);
    }

    protected void startService() throws Exception {
        server.addNotificationListener(hl7ServerName,
                hl7ReceivedListener, HL7ServerService.NOTIF_FILTER, null);
        server.addNotificationListener(storeScpServiceName,
                seriesStoredListener, SeriesStored.NOTIF_FILTER, null);
        server.addNotificationListener(mppsScpServiceName,
                mppsReceivedListener, MPPSScpService.NOTIF_FILTER, null);
    }

    protected void stopService() throws Exception {
        server.removeNotificationListener(hl7ServerName,
                hl7ReceivedListener, HL7ServerService.NOTIF_FILTER, null);
        server.removeNotificationListener(storeScpServiceName,
                seriesStoredListener, SeriesStored.NOTIF_FILTER, null);
        server.removeNotificationListener(mppsScpServiceName,
                mppsReceivedListener, MPPSScpService.NOTIF_FILTER, null);
    }

    private final NotificationListener hl7ReceivedListener =
            new NotificationListener() {        
        public void handleNotification(Notification notif, Object handback) {
            Object[] hl7msg = (Object[]) notif.getUserData();
            StudyPermissionService.this.onHl7Received((Document) hl7msg[1]);
        }
    };

    private final NotificationListener seriesStoredListener =
            new NotificationListener() {
        public void handleNotification(Notification notif, Object handback) {
            StudyPermissionService.this.onSeriesStored((SeriesStored) notif
                    .getUserData());

        }
    };

    private final NotificationListener mppsReceivedListener =
            new NotificationListener() {
        public void handleNotification(Notification notif, Object handback) {
            StudyPermissionService.this.onMppsCreate((Dataset) notif
                    .getUserData());
        }
    };
    
    private ContentHandler newContentHandler() {
        return new DefaultHandler() {
            private StudyPermissionManager manager;
            private StudyPermissionManager manager() throws Exception {
                if (manager == null) {
                    manager = getStudyPermissionManager();
                }
                return manager;
            }
            public void startElement(String uri, String localName,
                    String qName, Attributes attrs) throws SAXException {
                try {
                    if (qName.equals("grant")) {                
                        String pid = attrs.getValue("pid");
                        String issuer = attrs.getValue("issuer");
                        String suid = attrs.getValue("suid");
                        String role = attrs.getValue("role");
                        if (role == null) {
                            throw new SAXException(
                                    "Missing role attribute of <grant>");
                        }
                        String action = attrs.getValue("action");
                        if (action == null) {
                            throw new SAXException(
                                    "Missing action attribute of <grant>");
                        }
                        String[] actions = StringUtils.split(action, ',');
                        if (pid != null) {
                            manager().grantForPatient(pid, issuer, actions, role);
                        }
                        if (suid != null) {
                            manager().grant(suid, actions, role);                    
                        }             
                    } else if (qName.equals("revoke")) {
                        String pid = attrs.getValue("pid");
                        String issuer = attrs.getValue("issuer");
                        String suid = attrs.getValue("suid");
                        String role = attrs.getValue("role");
                        if (role == null) {
                            throw new SAXException(
                                    "Missing role attribute of <revoke>");
                        }
                        String action = attrs.getValue("action");
                        if (action == null) {
                            throw new SAXException(
                                    "Missing action attribute of <revoke>");
                        }
                        String[] actions = StringUtils.split(action, ',');
                        if (pid != null) {
                            manager().revokeForPatient(pid, issuer, actions, role);
                        }
                        if (suid != null) {
                            manager().revoke(suid, actions, role);                    
                        }                             
                    }
                } catch (SAXException e) {
                    throw e;
                } catch (Exception e) {
                    throw new SAXException(e);
                }
            }

        };
    }
    
    private void onHl7Received(Document msg) {
        if (!updateOnHl7Received) return;
        try {
            File xslFile = FileUtils.toExistingFile(hl7Stylesheet);
            Transformer t = templates.getTemplates(xslFile).newTransformer();
            t.transform(new DocumentSource(msg),
                    new SAXResult(newContentHandler()));
        } catch (Exception e) {
            log.error("Failed to update permissions on HL7 received", e);
        }
    }
    
    private void onSeriesStored(SeriesStored stored) {
        if (!updateOnSeriesStored) return;
        try {
            Dataset ds = DcmObjectFactory.getInstance().newDataset();
            ds.putAll(stored.getPatientAttrs());
            ds.putAll(stored.getStudyAttrs());
            ds.putAll(stored.getSeriesAttrs());
            xslt(seriesStylesheet, ds, stored.getCallingAET());        
        } catch (Exception e) {
            log.error("Failed to update permissions on Series stored", e);
        }
    }

    private void onMppsCreate(Dataset ds) {
        if (!updateOnMppsCreate) {
            return;
        }
        Dataset ssa = ds.getItem(Tags.ScheduledStepAttributesSeq);
        if (ssa == null) {      // ignore MPPS N-SET
            return;
        }
        try {
            xslt(mppsStylesheet, ds, ds.getString(Tags.PerformedStationAET));        
        } catch (Exception e) {
            log.error("Failed to update permissions on MPPS received", e);
        }
    }

    private void xslt(String xslt, Dataset ds, String calling)
            throws Exception {
        SAXTransformerFactory tf = (SAXTransformerFactory)
        TransformerFactory.newInstance();
        File xslFile = FileUtils.toExistingFile(xslt);
        Templates tpl = templates.getTemplates(xslFile);
        TransformerHandler th = tf.newTransformerHandler(tpl);
        th.setResult(new SAXResult(newContentHandler()));
        ds.writeDataset2(th, null, null, 64, null);
    }
}
