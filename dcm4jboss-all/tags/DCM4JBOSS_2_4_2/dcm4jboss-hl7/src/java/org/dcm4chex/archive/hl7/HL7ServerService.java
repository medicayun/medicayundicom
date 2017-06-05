/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.hl7;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.Hashtable;

import javax.management.ObjectName;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.dcm4che.server.Server;
import org.dcm4che.server.ServerFactory;
import org.dcm4che.util.MLLP_Protocol;
import org.dcm4cheri.util.StringUtils;
import org.dcm4chex.archive.mbean.TLSConfigDelegate;
import org.dom4j.Document;
import org.dom4j.io.DocumentSource;
import org.dom4j.io.SAXContentHandler;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.system.server.ServerConfigLocator;
import org.regenstrief.xhl7.HL7XMLReader;
import org.regenstrief.xhl7.HL7XMLWriter;
import org.regenstrief.xhl7.MLLPDriver;
import org.regenstrief.xhl7.XMLWriter;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1744 $ $Date: 2005-05-24 22:20:47 +0800 (周二, 24 5月 2005) $
 * @since 25.10.2004
 *
 */
public class HL7ServerService extends ServiceMBeanSupport
	implements Server.Handler {

    private static final String ACK_XSL_URL = "resource:xsl/hl7/msh2ack.xsl";
    
    private static final String LOG_MSG_XSL_URL = "resource:xsl/hl7/logmsg.xsl";

    private static final TransformerFactory tf = 
        						TransformerFactory.newInstance();

    private File logDir;

    private Server hl7srv = ServerFactory.getInstance().newServer(this);

    private MLLP_Protocol protocol = MLLP_Protocol.MLLP;

    private ObjectName auditLogName;
    
    private boolean fileReceivedHL7AsXML;

    private TLSConfigDelegate tlsConfig = new TLSConfigDelegate(this);

    private Hashtable templates = new Hashtable();

    private Hashtable serviceRegistry = new Hashtable();
    
    private String[] noopMessageTypes = {};

    public ObjectName getAuditLoggerName() {
        return auditLogName;
    }

    public void setAuditLoggerName(ObjectName auditLogName) {
        this.auditLogName = auditLogName;
    }

    public final ObjectName getTLSConfigName() {
        return tlsConfig.getTLSConfigName();
    }

    public final void setTLSConfigName(ObjectName tlsConfigName) {
        tlsConfig.setTLSConfigName(tlsConfigName);
    }
    
    public int getPort() {
        return hl7srv.getPort();
    }

    public void setPort(int port) {
        hl7srv.setPort(port);
    }

    public final String getNoopMessageTypes() {
        return StringUtils.toString(noopMessageTypes,',');
    }

    public final void setNoopMessageTypes(String noopMessageTypes) {
        this.noopMessageTypes = StringUtils.split(noopMessageTypes,',');
    }
    
    public String getProtocolName() {
        return protocol.toString();
    }

    public void setProtocolName(String protocolName) {
        this.protocol = MLLP_Protocol.valueOf(protocolName);
    }

    public int getMaxClients() {
        return hl7srv.getMaxClients();
    }

    public void setMaxClients(int newMaxClients) {
        hl7srv.setMaxClients(newMaxClients);
    }

    public int getNumClients() {
        return hl7srv.getNumClients();
    }

    public int getMaxIdleThreads() {
        return hl7srv.getMaxIdleThreads();
    }
    
    public int getNumIdleThreads() {
        return hl7srv.getNumIdleThreads();
    }
    
    public void setMaxIdleThreads(int max) {
        hl7srv.setMaxIdleThreads(max);
    }

    public final boolean isFileReceivedHL7AsXML() {
        return fileReceivedHL7AsXML;
    }
    
    public final void setFileReceivedHL7AsXML(boolean fileReceivedHL7AsXML) {
        this.fileReceivedHL7AsXML = fileReceivedHL7AsXML;
    }
    
    public void registerService(String messageType, HL7Service service) {
        if (service != null)
            serviceRegistry.put(messageType, service);
        else
            serviceRegistry.remove(messageType);
    }
    
    public Templates getTemplates(String uri) 
    		throws TransformerConfigurationException {
        Templates tpl = (Templates) templates.get(uri);
        if (tpl == null) {
            tpl = tf.newTemplates(new StreamSource(uri));
            templates.put(uri, tpl);
        }
        return tpl;
    }

    protected void startService() throws Exception {
        logDir = new File(ServerConfigLocator.locate().getServerHomeDir(), "log");
        hl7srv.addHandshakeFailedListener(tlsConfig.handshakeFailedListener());
        hl7srv.setServerSocketFactory(tlsConfig.serverSocketFactory(protocol
                .getCipherSuites()));
        hl7srv.start();
    }

    protected void stopService() throws Exception {
        hl7srv.stop();
        templates.clear();
    }

    public void ack(Document document, ContentHandler hl7out, HL7Exception hl7ex) {
        try {
            Transformer t = getTemplates(ACK_XSL_URL).newTransformer();
            if (hl7ex != null) {
                t.setParameter("AcknowledgementCode", hl7ex
                        .getAcknowledgementCode());
                t.setParameter("TextMessage", hl7ex.getMessage());
            }
            t.transform(new DocumentSource(document), new SAXResult(hl7out));
        } catch (TransformerException e) {
            log.error("Failed to acknowlege message", e);
        }
    }

    private HL7Service getService(MSH msh) throws HL7Exception {
        String messageType = msh.messageType + '^' + msh.triggerEvent;
        HL7Service service = (HL7Service) serviceRegistry.get(messageType);
        if (service == null) {
            if (Arrays.asList(noopMessageTypes).indexOf(messageType) == -1)
                throw new HL7Exception.AR("Unsupported message type: " + messageType.replace('^','_'));
        }
        return service;
    }

    //  Server.Handler Implementation-------------------------------
    public void handle(Socket s) throws IOException {
        MLLPDriver mllpDriver = new MLLPDriver(s.getInputStream(), s
                .getOutputStream(), false);
        XMLReader xmlReader = new HL7XMLReader();
        XMLWriter xmlWriter = new HL7XMLWriter();
        xmlWriter.setOutputStream(mllpDriver.getOutputStream());
        ContentHandler hl7out = xmlWriter.getContentHandler();
        SAXContentHandler hl7in = new SAXContentHandler();
        xmlReader.setContentHandler(hl7in);
        while (mllpDriver.hasMoreInput()) {
            try {
                try {
                    InputSource in = new InputSource(mllpDriver.getInputStream());
					in.setEncoding("ISO-8859-1");
					xmlReader.parse(in);
                    Document msg = hl7in.getDocument();
                    logMessage(msg);
                    if (fileReceivedHL7AsXML)
                        fileReceivedHL7AsXML(msg);
                    MSH msh = new MSH(msg);
                    HL7Service service = getService(msh);
                    if (service == null || service.process(msh, msg, hl7out)) ack(msg, hl7out, null);
                } catch (SAXException e) {
                    throw new HL7Exception.AE("Failed to parse message ", e);
                }
            } catch (HL7Exception e) {
            	log.warn("Processing HL7 failed:", e);
                mllpDriver.discardPendingOutput();
                ack(hl7in.getDocument(), hl7out, e);
            }
            mllpDriver.turn();
        }
    }

    private void fileReceivedHL7AsXML(Document msg) {
        File f = new File(logDir, "hl7." + System.currentTimeMillis() + ".xml");
        try {
	        Transformer tr = tf.newTransformer();
	        tr.setOutputProperty(OutputKeys.INDENT, "yes");
	        tr.transform(new DocumentSource(msg), new StreamResult(f));
        } catch (Exception e) {
            log.warn("Failed to log HL7 to " + f, e);
        }
    }

    public void logMessage(Document document) {
        if (!log.isInfoEnabled()) return;
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            out.write("Received HL7 message:".getBytes());
            Transformer t = getTemplates(LOG_MSG_XSL_URL).newTransformer();
            t.transform(new DocumentSource(document), new StreamResult(out));
            log.info(out.toString());
        } catch (Exception e) {
            log.warn("Failed to log message", e);
        }
    }
    
    public boolean isSockedClosedByHandler() {
        return false;
    }

}