/* $Id: SendMailService.java 1683 2005-04-13 12:29:23Z javawilli $
 * Copyright (c) 2004 by TIANI MEDGRAPH AG
 *
 * This file is part of dcm4che.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * Yo should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.dcm4chex.archive.mbean;

import java.io.File;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.dcm4cheri.util.StringUtils;
import org.dcm4chex.archive.config.RetryIntervalls;
import org.dcm4chex.archive.util.JMSDelegate;
import org.jboss.system.ServiceMBeanSupport;

/**
 * A service used to send email. 
 * After the reception of a JMS message containing aa email request
 * (encapulated in a Map) a new email is sent to the correspondig
 * address.
 * 
 * @author gunter.zeilinter@tiani.com
 * @author  <a href="mailto:umberto.cappellini@tiani.com">Umberto Cappellini</a
 * @version $Revision: 1683 $ $Date: 2005-04-13 20:29:23 +0800 (周三, 13 4月 2005) $
 * @since 08.04.2005
 */
public class SendMailService extends ServiceMBeanSupport
{
	public static final String MAIL_BODY = "body";
	public static final String MAIL_FILES = "files";
	public static final String MAIL_CC_ADDR = "ccAddr";
	public static final String MAIL_TO_ADDR = "toAddr";
	public static final String MAIL_REPLY_TO = "replyTo";
	public static final String MAIL_FROM_ADDR = "fromAddr";
	public static final String MAIL_SUBJECT = "subject";
	public static final String MAIL_RETRIES = "retries";
	public static final String MAIL_FAILURE_COUNT = "failureCount";

	/** Name of the JMS queue to receive 'send email' requests. */ 
    public static final String QUEUE = "Sendmail";
	
	private String smtpHost = "mail";

	private int smtpPort = 25;
	
	private boolean smtpAuth;

	private String smtpUser = "";

	private String smtpPassword = "";
	
	private RetryIntervalls retryIntervalls;

	public final String getSmtpHost()
	{
		return smtpHost;
	}

	public final void setSmtpHost(String smtpHost)
	{
		this.smtpHost = smtpHost;
	}

	/**
	 * @return Returns the smtpAuth.
	 */
	public boolean isSmtpAuth() {
		return smtpAuth;
	}
	/**
	 * @param smtpAuth The smtpAuth to set.
	 */
	public void setSmtpAuth(boolean smtpAuth) {
		this.smtpAuth = smtpAuth;
	}
	/**
	 * @return Returns the smtpPort.
	 */
	public int getSmtpPort() {
		return smtpPort;
	}
	/**
	 * @param smtpPort The smtpPort to set.
	 */
	public void setSmtpPort(int smtpPort) {
		this.smtpPort = smtpPort;
	}
	public final String getSmtpUser()
	{
		return smtpUser;
	}

	public final void setSmtpUser(String smtpUser)
	{
		this.smtpUser = smtpUser;
	}

	public final void setSmtpPassword(String smtpPassword)
	{
		this.smtpPassword = smtpPassword;
	}

    public final String getRetryIntervalls() {
        return retryIntervalls.toString();
    }

    public final void setRetryIntervalls(String s) {
        this.retryIntervalls = new RetryIntervalls(s);
    }
	
     
	/**
	 * JMS Message listener anonymous inner class.
	 * Upon a message reception, calls the method <i>process</i>
	 */
	private final MessageListener listener = new MessageListener()
	{

		public void onMessage(Message msg)
		{
			ObjectMessage objmsg = (ObjectMessage) msg;
			try
			{
				process((Map) objmsg.getObject());
			} catch (Throwable e)
			{
				log.error(e.getMessage(), e);
			}
		}
	};

	protected void startService() throws Exception
	{
        super.startService();
        JMSDelegate.startListening(QUEUE, listener);
	}

	protected void stopService() throws Exception
	{
		JMSDelegate.stopListening(QUEUE);
		super.stopService();
	}

	/**
	 * Core method of the service. Sends a new mail.
	 * A huge number of Exception types may be arised during the process of the 
	 * method, but every Exception is thrown as a MessagingException eventually
	 * including the Exception source that originated it.
	 *
	 * @param mailProps This Map holds the properties (subject, toAddress, body,..) 
	 * 			for the mail to be sent.
	 * @throws MessagingException if an Exception of any kind is thrown during
	 * @throws JMSException
	 * 
	 */
	private void process(Map mailProps)
		throws MessagingException, JMSException
	{
		log.info("process " + mailProps);


		// mail session and mime message creation
		Session session = Session.getInstance(createSessionProperties());
		//session.setDebugOut( System.out );
		//session.setDebug( true );
		log.info("Mail session props:"+session.getProperties());
		MimeMessage message = new MimeMessage(session);

		//set subject, sent date, from, to and reply-to addresses to the message
		initSimpleMail(mailProps, message);

		File[] files = (File[]) mailProps.get( MAIL_FILES );
		if (files != null && files.length > 0) //there are files to be sent
		{
			Multipart multipart = new MimeMultipart("mixed");

			// body of the message. 
			BodyPart textbodypart = new MimeBodyPart();
			textbodypart.setText((String)mailProps.get(MAIL_BODY));
			multipart.addBodyPart(textbodypart);

			//add each file as an attachment
			for (int i = 0; i < files.length; i++)
			{
				if ( files[i].isFile() ) {
					MimeBodyPart filebodypart = new MimeBodyPart();
					FileDataSource source = new FileDataSource(files[i]);
					filebodypart.setDataHandler(new DataHandler(source));
					filebodypart.setFileName(files[i].getName());
					//TODO filebodypart.setHeader("Content-Type", "Application/dicom");
					multipart.addBodyPart(filebodypart);
				}
			}
			message.setContent(multipart);
			log.info(
				new StringBuffer().append("sending message: ").append(
					files.length).append(
					" files attached"));
		} else {
			message.setText((String)mailProps.get(MAIL_BODY));
		}

		
		try {
			message.saveChanges();
            Transport.send(message);            
    		log.info("Message sent to " + mailProps.get(MAIL_TO_ADDR));
        } catch (Exception e) {
            log.warn("Failed to send " + mailProps, e);
            int failureCount = 0;
            Integer failures = (Integer) mailProps.get( MAIL_FAILURE_COUNT );
            if ( failures != null ) {
            	failureCount = failures.intValue();
            }
            failureCount++;
            mailProps.put( MAIL_FAILURE_COUNT, new Integer( failureCount ));
            final long delay = retryIntervalls.getIntervall(failureCount);
            if (delay == -1L) {
                log.error("Give up to send " + mailProps);
            } else {
                log.warn("Failed to send " + mailProps + ". Scheduling retry.");
                JMSDelegate.queue(QUEUE, (Serializable) mailProps, 0, System.currentTimeMillis() + delay);
            }
        }
		
	}

	/**
	 * public method used (principally for debug reasons) to send a dcmmail message
 	 * without the correspondent reception of a JMS message.
	 */
	public String send(
		String subject,
		String fromAddress,
		String toAddress,
		String body)
	{
		HashMap mail = new HashMap();
		mail.put( MAIL_BODY, body );
		mail.put( MAIL_FROM_ADDR, fromAddress);
		mail.put(MAIL_SUBJECT, subject);
		mail.put(MAIL_TO_ADDR, toAddress);

		try
		{
			JMSDelegate.queue( QUEUE, mail, Message.DEFAULT_PRIORITY, 0L);
			return "Mail '"+subject+"' succesfully sent to "+ toAddress;
		} catch (Exception e)
		{
			log.error(e.getMessage(), e);
			return "Failed to send mail '"+subject+"' to "+ toAddress+ " ! Reason:"+e.getMessage();
		}
	}


	/**
	 * @param files
	 * @return
	 */
	private Object string2Files(String files) {
		if ( files == null ) return null;
		try {
			String[] sa = StringUtils.split( files, ',' );
			File[] fa = new File[ sa.length ];
			for ( int i = 0, len = fa.length ; i < len ; i++ ) {
				fa[i] = new File( sa[i] );
			}
			return fa;
		} catch ( Exception x ) {
			log.warn("File attachments ignored! Reason:"+x.getMessage(), x );
			return null;
		}
	}

	/**
	 * Creates and initialize a mail session Properties object.
	 * @return an initialized a mail session Properties object.
	 */
	private Properties createSessionProperties()
	{
		Properties properties = new Properties();

		if (getSmtpHost().equals(null))
		{
			properties.put("mail.smtp.host", "127.0.0.1");
		} else
		{
			properties.put("mail.smtp.host", smtpHost);
		}
		
		properties.put("mail.smtp.port", String.valueOf(this.smtpPort));

		if (smtpUser != null
			&& smtpUser.length() > 0
			&& smtpPassword != null
			&& smtpPassword.length() > 0)
		{
			properties.put("mail.smtp.auth", String.valueOf(smtpAuth));
			properties.put("mail.smtp.user", smtpUser);
			properties.put("mail.smtp.password", smtpPassword);
		} else
		{
			properties.put("mail.smtp.auth", "false");
		}

		return properties;
	}

	
	/**
	 * Sets from, to and reply-to addresses for this message.
	 * Reply-to address is set equal to the From address.
	 * 
	 * @param dcmMail the object representig the dicom mail
	 * @param message the MimeMessage mail messagge
	 * @throws AddressException if addresses are not present or not valid
	 * @throws MessagingException -
	 */
		private void initSimpleMail(Map map, MimeMessage message)
		throws AddressException, MessagingException {
			
			//set subject and date to the message		
			if (map.get(MAIL_SUBJECT) != null)
				message.setSubject( (String) map.get(MAIL_SUBJECT) );
			message.setSentDate(new Date());
			
		String fromAddr = (String) map.get(MAIL_FROM_ADDR);
		if ( fromAddr == null ) {
			//throw new AddressException("no from address");
		} else {
			try
			{
				message.setFrom(new InternetAddress(fromAddr));
				String replyTo = (String) map.get(MAIL_REPLY_TO);
				if ( replyTo == null ) replyTo = fromAddr;
				try {
					message.setReplyTo(
							new Address[] {
									new InternetAddress(fromAddr)});
				} catch ( Exception x ) {
					//ignore; some implementation doesnt support this method!
				}
			} catch (AddressException e) {
				throw new AddressException( "invalid from address: " + fromAddr + ": " + e.getMessage());
			}
		}

		String toAddr = (String) map.get(MAIL_TO_ADDR);
		if ( toAddr == null ) {
			throw new AddressException("no to address");
		} else {
			try
			{
				message.setRecipients(
					javax.mail.Message.RecipientType.TO,
					InternetAddress.parse(toAddr));
			} catch (AddressException e)
			{
				throw new AddressException(
					"invalid to address: "
						+ toAddr
						+ ": "
						+ e.getMessage());
			}
		}
		String ccAddr = (String) map.get(MAIL_CC_ADDR);
		if ( ccAddr != null ) {
			try
			{
				message.setRecipients(
					javax.mail.Message.RecipientType.CC,
					InternetAddress.parse(ccAddr));
			} catch (AddressException e)
			{
				throw new AddressException(
					"invalid cc address: "
						+ ccAddr
						+ ": "
						+ e.getMessage());
			}
		}
	}

}
