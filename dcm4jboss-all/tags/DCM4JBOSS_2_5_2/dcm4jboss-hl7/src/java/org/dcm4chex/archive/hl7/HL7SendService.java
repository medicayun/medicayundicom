package org.dcm4chex.archive.hl7;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.xml.transform.TransformerException;

import org.dcm4chex.archive.config.ForwardingRules;
import org.dcm4chex.archive.config.RetryIntervalls;
import org.dcm4chex.archive.ejb.jdbc.AECmd;
import org.dcm4chex.archive.ejb.jdbc.AEData;
import org.dcm4chex.archive.exceptions.UnkownAETException;
import org.dcm4chex.archive.mbean.TLSConfigDelegate;
import org.dcm4chex.archive.util.JMSDelegate;
import org.dom4j.Document;
import org.dom4j.io.SAXContentHandler;
import org.jboss.system.ServiceMBeanSupport;
import org.regenstrief.xhl7.HL7XMLReader;
import org.regenstrief.xhl7.MLLPDriver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public class HL7SendService 
		extends ServiceMBeanSupport 
		implements NotificationListener, MessageListener {
	
	private String queueName;
	
	private String sendingApplication;

	private String sendingFacility;
	
    private int acTimeout;

    private int soCloseDelay;
	
    private ObjectName hl7ServerName;

	private TLSConfigDelegate tlsConfig = new TLSConfigDelegate(this);

    private RetryIntervalls retryIntervalls = new RetryIntervalls();

	private ForwardingRules forwardingRules = new ForwardingRules("");
	
	private volatile long messageControlID = System.currentTimeMillis();
	
	private int concurrency = 1;

	public final int getConcurrency() {
		return concurrency;
	}

	public final void setConcurrency(int concurrency) throws Exception {
		if (concurrency <= 0)
			throw new IllegalArgumentException("Concurrency: " + concurrency);
		if (this.concurrency != concurrency) {
			final boolean restart = getState() == STARTED;
			if (restart)
				stop();
			this.concurrency = concurrency;
			if (restart)
				start();
		}
	}
	
	public final String getSendingApplication() {
		return sendingApplication;
	}

	public final void setSendingApplication(String sendingApplication) {
		this.sendingApplication = sendingApplication;
	}

	public final String getSendingFacility() {
		return sendingFacility;
	}

	public final void setSendingFacility(String sendingFacility) {
		this.sendingFacility = sendingFacility;
	}

	public String getRetryIntervalls() {
        return retryIntervalls.toString();
    }

    public void setRetryIntervalls(String text) {
        retryIntervalls = new RetryIntervalls(text);
    }

	public final int getAcTimeout() {
		return acTimeout;
	}

	public final void setAcTimeout(int acTimeout) {
		this.acTimeout = acTimeout;
	}

	public final int getSoCloseDelay() {
		return soCloseDelay;
	}

	public final void setSoCloseDelay(int soCloseDelay) {
		this.soCloseDelay = soCloseDelay;
	}

    public final ObjectName getTLSConfigName() {
        return tlsConfig.getTLSConfigName();
    }

    public final void setTLSConfigName(ObjectName tlsConfigName) {
        tlsConfig.setTLSConfigName(tlsConfigName);
    }
	
    public final String getQueueName() {
        return queueName;
    }
    
    public final void setQueueName(String queueName) {
        this.queueName = queueName;
    }
	
    public final ObjectName getHL7ServerName() {
        return hl7ServerName;
    }

    public final void setHL7ServerName(ObjectName hl7ServerName) {
        this.hl7ServerName = hl7ServerName;
    }
	
	public final String getForwardingRules() {
		return forwardingRules.toString();
	}

	public final void setForwardingRules(String s) {
		this.forwardingRules = new ForwardingRules(s);
	}

	protected void startService() throws Exception {
        JMSDelegate.startListening(queueName, this, concurrency);
        server.addNotificationListener(hl7ServerName,
                this,
                HL7ServerService.NOTIF_FILTER,
                null);
	}

	protected void stopService() throws Exception {
		server.removeNotificationListener(hl7ServerName,
                this,
                HL7ServerService.NOTIF_FILTER,
                null);				
        JMSDelegate.stopListening(queueName);
	}

	public void handleNotification(Notification notif, Object handback) {
		forward((byte[]) notif.getUserData());
	}

	public int forward(byte[] hl7msg) {
        XMLReader xmlReader = new HL7XMLReader();
        SAXContentHandler hl7in = new SAXContentHandler();
        xmlReader.setContentHandler(hl7in);
        InputSource in = new InputSource(new ByteArrayInputStream(hl7msg));
		in.setEncoding("ISO-8859-1");
		try {
			xmlReader.parse(in);
		} catch (Exception e) {
            log.error("Failed to parse HL7 message", e);
			return -1;
		}
        Document msg = hl7in.getDocument();
		MSH msh = new MSH(msg);
		Map param = new HashMap();
		param.put("sending", new String[]{
				msh.sendingApplication + '^' + msh.sendingFacility});
		param.put("receiving", new String[]{
				msh.receivingApplication + '^' + msh.receivingFacility});
		param.put("msgtype", new String[]{ 
				msh.messageType + '^' + msh.triggerEvent });
		String[] dests = forwardingRules.getForwardDestinationsFor(param);
		int count = 0;
		for (int i = 0; i < dests.length; i++) {
            HL7SendOrder order = new HL7SendOrder(hl7msg, dests[i]);
            try {
                log.info("Scheduling " + order);
                JMSDelegate.queue(queueName,
                        order,
                        Message.DEFAULT_PRIORITY,
                        0L);
				++count;
            } catch (JMSException e) {
                log.error("Failed to schedule " + order, e);
            }
		}
		return count;
	}

	public void onMessage(Message message) {
        ObjectMessage om = (ObjectMessage) message;
        try {
            HL7SendOrder order = (HL7SendOrder) om.getObject();
            try {
				log.info("Start processing " + order);
				sendTo(order.getHL7Message(), order.getReceiving());
	            order.setException(null);
				log.info("Finished processing " + order);
			} catch (Exception e) {
	            order.setException(e);
	            final int failureCount = order.getFailureCount() + 1;
	            order.setFailureCount(failureCount);
	            final long delay = retryIntervalls.getIntervall(failureCount);
	            if (delay == -1L) {
	                log.error("Give up to process " + order);
	            } else {
	                log.warn("Failed to process " + order + ". Scheduling retry.");
	                JMSDelegate.queue(queueName, order, 0, System
	                        .currentTimeMillis()
	                        + delay);
	            }
			}
         } catch (JMSException e) {
            log.error("jms error during processing message: " + message, e);
        } catch (Throwable e) {
            log.error("unexpected error during processing message: " + message,
                    e);
        }
	}

	public void sendTo(byte[] message, String receiving) 
			throws SQLException, IOException, UnkownAETException, 
					TransformerException, SAXException, HL7Exception {
		AEData aeData = new AECmd(receiving).getAEData();
		if (aeData == null) {
			throw new UnkownAETException("Unkown HL7 receiver application "
					+ receiving);
		}
		Socket s = tlsConfig.createSocket(aeData);
		MLLPDriver mllpDriver = new MLLPDriver(s.getInputStream(), s
				.getOutputStream(), true);
		writeMessage(message, receiving, mllpDriver.getOutputStream());
		mllpDriver.turn();
		if (acTimeout > 0) {
			s.setSoTimeout(acTimeout);
		}
		Document rsp = readMessage(mllpDriver.getInputStream());
		MSH msh = new MSH(rsp);
		if ("ACK".equals(msh.messageType)) {
			ACK ack = new ACK(rsp);
			if (!"AA".equals(ack.acknowledgmentCode))
				throw new HL7Exception(ack.acknowledgmentCode, ack.textMessage);
		} else {
			log.warn("Unsupport response message type: " + msh.messageType
					+ '^' + msh.triggerEvent
					+ ". Assume successful message forward.");
		}
		if (soCloseDelay > 0)
			try {
				Thread.sleep(soCloseDelay);
			} catch (InterruptedException ignore) {
			}
		s.close();
	}

	private Document readMessage(InputStream mllpIn)
			throws IOException, SAXException {
		InputSource in = new InputSource(mllpIn);
		in.setEncoding("ISO-8859-1");
		XMLReader xmlReader = new HL7XMLReader();
		SAXContentHandler hl7in = new SAXContentHandler();
		xmlReader.setContentHandler(hl7in);
		xmlReader.parse(in);
		Document msg = hl7in.getDocument();
		return msg;
	}
	
	private void writeMessage(byte[] message, String receiving, OutputStream out)
			throws UnsupportedEncodingException, IOException {
		out.write("MSH|^~\\&|".getBytes("ISO-8859-1"));
		out.write(sendingApplication.getBytes("ISO-8859-1"));
		out.write('|');
		out.write(sendingFacility.getBytes("ISO-8859-1"));
		out.write('|');
		final int delim = receiving.indexOf('^');
		out.write(receiving.substring(0, delim).getBytes("ISO-8859-1"));
		out.write('|');
		out.write(receiving.substring(delim + 1).getBytes("ISO-8859-1"));
		out.write('|');
		final SimpleDateFormat tsFormat = 
			new SimpleDateFormat("yyyyMMddHHmmss.SSS");
		out.write(tsFormat.format(new Date()).getBytes("ISO-8859-1"));
		// skip MSH:1-7
		int left = 0;
		for (int i = 0; i < 7; ++i)
			while (message[left++] != '|');
		// write MSH:8-9  
		int right = left;
		while (message[right++] != '|');
		while (message[right++] != '|');
		out.write(message, left-1, right - left + 1);
		out.write(String.valueOf(++messageControlID).getBytes("ISO-8859-1"));
		// skip MSH:10
		while (message[right++] != '|');
		// write remaining message
		out.write(message, right - 1, message.length - right + 1);
	}
}
