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

package org.dcm4chex.archive.hl7;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.xml.transform.TransformerException;

import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import org.dcm4chex.archive.config.ForwardingRules;
import org.dcm4chex.archive.config.RetryIntervalls;
import org.dcm4chex.archive.ejb.jdbc.AECmd;
import org.dcm4chex.archive.ejb.jdbc.AEData;
import org.dcm4chex.archive.exceptions.UnkownAETException;
import org.dcm4chex.archive.mbean.JMSDelegate;
import org.dcm4chex.archive.mbean.TLSConfigDelegate;
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

    private static final String ISO_8859_1 = "ISO-8859-1";
	private static final String LOCAL_HL7_AET = "LOCAL^LOCAL";
	private static final String DATETIME_FORMAT = "yyyyMMddHHmmss";
	private static long msgCtrlid = System.currentTimeMillis();
    private static long queryTag = System.currentTimeMillis();
	
	private String queueName;
	
	private String sendingApplication;

	private String sendingFacility;
    
    private String pixQueryName;

    private String pixManager;
    
    private int acTimeout;

    private int soCloseDelay;
	
    private ObjectName hl7ServerName;

	private TLSConfigDelegate tlsConfig = new TLSConfigDelegate(this);

    private RetryIntervalls retryIntervalls = new RetryIntervalls();

	private ForwardingRules forwardingRules = new ForwardingRules("");
	
	private volatile long messageControlID = System.currentTimeMillis();
	
	private int concurrency = 1;

        private JMSDelegate jmsDelegate = new JMSDelegate(this);

        public final ObjectName getJmsServiceName() {
            return jmsDelegate.getJmsServiceName();
        }

        public final void setJmsServiceName(ObjectName jmsServiceName) {
            jmsDelegate.setJmsServiceName(jmsServiceName);
        }
    
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

	public final String getPIXQueryName() {
        return pixQueryName;
    }

    public final void setPIXQueryName(String pixQueryName) {
        this.pixQueryName = pixQueryName;
    }

    public final String getPIXManager() {
        return pixManager;
    }

    public final void setPIXManager(String pixManager) {
        this.pixManager = pixManager;
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
        jmsDelegate.startListening(queueName, this, concurrency);
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
        jmsDelegate.stopListening(queueName);
	}

	public void handleNotification(Notification notif, Object handback) {
		forward((byte[]) notif.getUserData());
	}

	public int forward(byte[] hl7msg) {
        XMLReader xmlReader = new HL7XMLReader();
        SAXContentHandler hl7in = new SAXContentHandler();
        xmlReader.setContentHandler(hl7in);
		try {
			InputSource in = new InputSource(new InputStreamReader(
					new ByteArrayInputStream(hl7msg), ISO_8859_1));
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
                jmsDelegate.queue(queueName,
                        order,
                        Message.DEFAULT_PRIORITY,
                        0L);
				++count;
            } catch (Exception e) {
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
	                jmsDelegate.queue(queueName, order, 0, System
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

	public Document invoke(byte[] message, String receiver)
    throws UnkownAETException, IOException, SQLException, SAXException {
		AEData aeData;
		if (LOCAL_HL7_AET.equals(receiver)) {
			aeData = new AEData(-1, receiver, "localhost", getLocalHL7Port(), null);
			
		} else {
			aeData = new AECmd(receiver).getAEData();
			if (aeData == null) {
				throw new UnkownAETException("Unkown HL7 receiver application "
						+ receiver);
			}
		}
		Socket s = tlsConfig.createSocket(aeData);
        try {
    		MLLPDriver mllpDriver = new MLLPDriver(s.getInputStream(), s
    				.getOutputStream(), true);
    		writeMessage(message, receiver, mllpDriver.getOutputStream());
    		mllpDriver.turn();
    		if (acTimeout > 0) {
    			s.setSoTimeout(acTimeout);
    		}
    		return readMessage(mllpDriver.getInputStream());
        } finally {
            if (soCloseDelay > 0)
                try {
                    Thread.sleep(soCloseDelay);
                } catch (InterruptedException ignore) {
                }
            s.close();
        }
    }
    
    public void sendTo(byte[] message, String receiver) 
        throws SQLException, IOException, UnkownAETException, 
                TransformerException, SAXException, HL7Exception {
		Document rsp = invoke(message, receiver);
        MSH msh = new MSH(rsp );
		if ("ACK".equals(msh.messageType)) {
			ACK ack = new ACK(rsp);
			if (!"AA".equals(ack.acknowledgmentCode))
				throw new HL7Exception(ack.acknowledgmentCode, ack.textMessage);
		} else {
			log.warn("Unsupport response message type: " + msh.messageType
					+ '^' + msh.triggerEvent
					+ ". Assume successful message forward.");
		}
	}

	/**
	 * @return
	 */
	private int getLocalHL7Port() {
		try {
			return ((Integer) this.getServer().getAttribute( hl7ServerName, "Port")).intValue();
		} catch (Exception x) {
			log.error("Cant get local HL7 port!", x);
			return -1;
		}
	}

	private Document readMessage(InputStream mllpIn)
			throws IOException, SAXException {
		InputSource in = new InputSource(mllpIn);
		in.setEncoding(ISO_8859_1);
		XMLReader xmlReader = new HL7XMLReader();
		SAXContentHandler hl7in = new SAXContentHandler();
		xmlReader.setContentHandler(hl7in);
		xmlReader.parse(in);
		Document msg = hl7in.getDocument();
		return msg;
	}
	
	private void writeMessage(byte[] message, String receiving, OutputStream out)
			throws UnsupportedEncodingException, IOException {
		out.write("MSH|^~\\&|".getBytes(ISO_8859_1));
		out.write(sendingApplication.getBytes(ISO_8859_1));
		out.write('|');
		out.write(sendingFacility.getBytes(ISO_8859_1));
		out.write('|');
		final int delim = receiving.indexOf('^');
		out.write(receiving.substring(0, delim).getBytes(ISO_8859_1));
		out.write('|');
		out.write(receiving.substring(delim + 1).getBytes(ISO_8859_1));
		out.write('|');
		final SimpleDateFormat tsFormat = 
			new SimpleDateFormat("yyyyMMddHHmmss.SSS");
		out.write(tsFormat.format(new Date()).getBytes(ISO_8859_1));
		// skip MSH:1-7
		int left = 0;
		for (int i = 0; i < 7; ++i)
			while (message[left++] != '|');
		// write MSH:8-9  
		int right = left;
		while (message[right++] != '|');
		while (message[right++] != '|');
		out.write(message, left-1, right - left + 1);
		out.write(String.valueOf(++messageControlID).getBytes(ISO_8859_1));
		// skip MSH:10
		while (message[right++] != '|');
		// write remaining message
		out.write(message, right - 1, message.length - right + 1);
	}
	
    /**
	 * @param ds
     * @throws HL7Exception
     * @throws SAXException
     * @throws TransformerException
     * @throws UnkownAETException
     * @throws IOException
     * @throws SQLException
	 */
	public void sendHL7PatientXXX(Dataset ds,String msgType, String sending, String receiving, boolean useForward) 
				throws SQLException, IOException, UnkownAETException, TransformerException, SAXException, HL7Exception {
        String timestamp = new SimpleDateFormat(DATETIME_FORMAT).format( new Date() );
		StringBuffer sb = makeMSH(timestamp, msgType, sending, receiving, "2.3.1");//get MSH for patient information update (ADT^A08)
		addEVN(sb, timestamp);
		addPID( sb, ds );
		sb.append("\r");
		sb.append("PV1||||||||||||||||||||||||||||||||||||||||||||||||||||");//PatientClass(2),VisitNr(19) and VisitIndicator(51) ???
		if ( useForward ) {
			forward( sb.toString().getBytes(ISO_8859_1) );
		} else {
			sendTo( sb.toString().getBytes(ISO_8859_1), receiving );
		}
	}

	public void sendHL7PatientMerge(Dataset dsDominant, Dataset[] priorPats, String sending, String receiving, boolean useForward) 
				throws SQLException, IOException, UnkownAETException, TransformerException, SAXException, HL7Exception {
        String timestamp = new SimpleDateFormat(DATETIME_FORMAT).format( new Date() );
		StringBuffer sb = makeMSH(timestamp, "ADT^A40", sending, receiving, "2.3.1");//get MSH for patient merge (ADT^A40)
		addEVN(sb, timestamp);
		addPID( sb, dsDominant );
		int SBlen = sb.length();
		for ( int i = 0, len = priorPats.length ; i < len ; i++ ) {
			sb.setLength( SBlen );
			addMRG( sb, priorPats[i] );
			if ( useForward ) {
				forward( sb.toString().getBytes(ISO_8859_1) );
			} else {
				sendTo( sb.toString().getBytes(ISO_8859_1), receiving );
			}
		}
		
	}

    public String showCorrespondingPIDs(String patientID, String issuer) {
        try {
            return queryCorrespondingPIDs(patientID, issuer).toString();
        } catch (Exception e) {
            return e.getMessage();
        }
    }
    
	public List queryCorrespondingPIDs(String patientID, String issuer) throws Exception {
		return queryCorrespondingPIDs(patientID, issuer, null);
	}
	public List queryCorrespondingPIDs(String patientID, String issuer, String[] domains)
    throws Exception { 
        String timestamp = new SimpleDateFormat(DATETIME_FORMAT).format( new Date() );
	    StringBuffer sb = makeMSH(timestamp, "QBP^Q23",
                null, pixManager, "2.5");
        addQPD(sb, patientID, issuer, domains);
	    sb.append("\rRCP|I||||||");
        String s = sb.toString();
        log.info("Query PIX Manager " + pixManager + ":\n" + s.replace('\r', '\n'));
        Document msg = invoke(s.getBytes(ISO_8859_1), pixManager);
        MSH msh = new MSH(msg);
        if (!"RSP".equals(msh.messageType) || !"K23".equals(msh.triggerEvent)) {
            String prompt = "Unsupport response message type: "
                + msh.messageType + '^' + msh.triggerEvent;
            log.error(prompt );
            throw new IOException(prompt);
        }
        RSP rsp = new RSP(msg);
        if (!"AA".equals(rsp.acknowledgmentCode)) {
            log.error("PIX Query fails with code " + rsp.acknowledgmentCode
                    + " - " + rsp.textMessage);
            throw new HL7Exception(rsp.acknowledgmentCode, rsp.textMessage);
        }
        return rsp.getPatientIDs();
	}
    
    private StringBuffer makeMSH(String timestamp, String msgType,
            String sending, String receiving, String version) {
		StringBuffer sb = new StringBuffer();
		sb.append("MSH|^~\\&|");
		int delim;
		if ( sending == null || sending.trim().length() == 0 ) {
			sb.append( getSendingApplication() ).append("|");
			sb.append( getSendingFacility() ).append("|");
		} else {
			delim = sending.indexOf('^');
			sb.append( sending.substring(0,delim)).append("|");
	        sb.append( sending.substring(delim+1)).append("|");
		}
		delim = receiving.indexOf('^');
		sb.append( receiving.substring(0,delim)).append("|");
        sb.append( receiving.substring(delim+1)).append("|");
        sb.append( timestamp ).append("||");
        sb.append(msgType).append("|");
		sb.append( getMsgCtrlId() ).append("|P|");
        sb.append(version).append("||||||||");
		return sb;
	}

	private void addEVN( StringBuffer sb, String timeStamp) {
		sb.append("\rEVN||").append(timeStamp).append("||||").append(timeStamp);
	}
	/**
	 * @param sb PID will be added to this StringBuffer. 
	 * @param ds Dataset to get PID informations
	 */
	private void addPID( StringBuffer sb, Dataset ds) {
		String s;
		Date d;
		sb.append("\rPID|||");
		appendPatIDwithIssuer(sb,ds);
		sb.append("||");
		addPersonName(sb, ds.getString( Tags.PatientName ));
		sb.append("||");
		d = ds.getDateTime( Tags.PatientBirthDate, Tags.PatientBirthTime );
		if ( d != null ) sb.append( new SimpleDateFormat(DATETIME_FORMAT).format(d) );
		sb.append("|");
		s = ds.getString( Tags.PatientSex );
		if ( s != null ) sb.append( s );
		sb.append("||||||||||||||||||||||");//patient Account number ???(field 18)
	}

	// concerns different order of name suffix, prefix in HL7 XPN compared to DICOM PN
	private void addPersonName(StringBuffer sb, final String patName) {
		StringTokenizer stk = new StringTokenizer(patName, "^", true);
		for (int i = 0; i < 6 && stk.hasMoreTokens(); ++i) {
			sb.append(stk.nextToken());
		}
		if (stk.hasMoreTokens()) {
			String prefix = stk.nextToken();
			if (stk.hasMoreTokens()) {
				stk.nextToken(); // skip delim
				if (stk.hasMoreTokens()) {
					sb.append(stk.nextToken()); // name suffix
				}
			}
			sb.append('^').append(prefix);
		}
	}
	
	private void appendPatIDwithIssuer( StringBuffer sb, Dataset ds ) {
		sb.append( ds.getString(Tags.PatientID));
		String s = ds.getString( Tags.IssuerOfPatientID );
		if ( s != null )
			sb.append("^^^").append(s); //issuer of patient ID
	}

	/**
	 * @param sb
	 * @param ds
	 */
	private void addMRG(StringBuffer sb, Dataset ds) {
		sb.append("\rMRG|");
		appendPatIDwithIssuer(sb,ds);
		sb.append("||||||");
		String name = ds.getString(Tags.PatientName);
		sb.append( name != null ? name : "patName" ); 
	}
	
    private void addQPD(StringBuffer sb, String patientID, String issuer, String[] domains) {
        sb.append("\rQPD|").append(getPIXQueryName()).append('|');
        sb.append(getQueryTag()).append('|');
        sb.append(patientID).append("^^^").append(issuer);
        if ( domains != null && domains.length > 0 ) {
        	sb.append("|^^^").append(domains[0]);
        	for ( int i = 1 ; i < domains.length ; i++) {
        		sb.append("~^^^").append(domains[i]);//~ is repeat delimiter used in makeMSH
        	}
        }
    }

	/**
	 * should this method on a central hl7 sending place? 
	 * @return
	 */
	public synchronized long getMsgCtrlId() {
		return msgCtrlid++;
	}

    public synchronized long getQueryTag() {
        return queryTag++;
    }
    
}
