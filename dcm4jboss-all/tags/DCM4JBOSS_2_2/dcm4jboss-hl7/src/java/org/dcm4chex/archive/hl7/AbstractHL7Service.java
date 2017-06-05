/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.hl7;

import java.io.StringWriter;
import java.util.StringTokenizer;

import javax.management.ObjectName;
import javax.xml.transform.Templates;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmObjectFactory;
import org.jboss.system.ServiceMBeanSupport;

/**
 * @author gunter.zeilinger@tiani.com
 * @version Revision $Date: 2005-02-19 02:51:13 +0800 (周六, 19 2月 2005) $
 * @since 26.12.2004
 */

public abstract class AbstractHL7Service extends ServiceMBeanSupport implements
        HL7Service {

    static DcmObjectFactory dof = DcmObjectFactory.getInstance();
    
    private ObjectName hl7ServerName;

    private String messageTypes;

    public final ObjectName getHL7ServerName() {
        return hl7ServerName;
    }

    public final void setHL7ServerName(ObjectName hl7ServerName) {
        this.hl7ServerName = hl7ServerName;
    }

    public final String getMessageTypes() {
        return messageTypes;
    }

    public final void setMessageTypes(String messageTypes) {
        if (getState() == STARTED)
            registerService(null);
        this.messageTypes = messageTypes;
        if (getState() == STARTED)
            registerService(this);
    }

    private void registerService(HL7Service service) {
        try {
            StringTokenizer stk = new StringTokenizer(messageTypes, ", ");
            while (stk.hasMoreTokens()) {
                server.invoke(hl7ServerName, "registerService", new Object[] {
                    stk.nextToken(), service }, new String[] {
                    String.class.getName(), HL7Service.class.getName() });
            }
        } catch (Exception e) {
            log.error("Failed to register HL7 service", e);
            throw new RuntimeException("Failed to register HL7 service", e);
        }
    }

    protected Templates getTemplates(String uri) {
        try {
            return (Templates) server.invoke(hl7ServerName, "getTemplates",
                    new Object[] { uri },
                    new String[] { String.class.getName() });
        } catch (Exception e) {
            String prompt = "Failed to load XSL " + uri;
            log.error(prompt, e);
            throw new RuntimeException(prompt, e);
        }
    }
    
    protected void startService() throws Exception {
        registerService(this);
    }

    protected void stopService() throws Exception {
        registerService(null);
    }

    public void logDataset(String prompt, Dataset ds) {
        if (!log.isDebugEnabled()) { return; }
        try {
            StringWriter w = new StringWriter();
            w.write(prompt);
            ds.dumpDataset(w, null);
            log.debug(w.toString());
        } catch (Exception e) {
            log.warn("Failed to dump dataset", e);
        }
    }

}