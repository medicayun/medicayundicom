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

import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.dict.Tags;
import org.dcm4chex.archive.dcm.mppsscp.MPPSScpService;
import org.dcm4chex.archive.ejb.interfaces.MPPSManager;
import org.dcm4chex.archive.ejb.interfaces.MPPSManagerHome;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.dcm4chex.archive.util.HomeFactoryException;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.system.server.ServerConfigLocator;
import org.regenstrief.xhl7.HL7XMLWriter;
import org.regenstrief.xhl7.XMLWriter;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 2016 $ $Date: 2005-10-12 23:14:41 +0800 (周三, 12 10月 2005) $
 * @since Oct 3, 2005
 */
public class MPPS2ORMService
extends ServiceMBeanSupport
implements NotificationListener {

	private static final int INIT_BUFFER_SIZE = 512;
	private ObjectName mppsScpServiceName;
	private ObjectName hl7SendServiceName;
	private String stylesheetURL;
	private Templates stylesheet;
	private String sendingApplication;
	private String sendingFacility;
	private String receivingApplication;
	private String receivingFacility;
	private boolean ignoreUnscheduled;
	private boolean ignoreInProgress;
	private boolean oneORMperSPS;
	private File logDir;
	private boolean logXSLT;

	public final String getStylesheetURL() {
		return stylesheetURL;
	}

	public final void setStylesheetURL(String mpps2ormStylesheetURL) {
		this.stylesheetURL = mpps2ormStylesheetURL;
		this.stylesheet = null;
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

    public final String getReceivingApplication() {
		return receivingApplication;
	}

	public final void setReceivingApplication(String receivingApplication) {
		this.receivingApplication = receivingApplication;
	}

	public final String getReceivingFacility() {
		return receivingFacility;
	}

	public final void setReceivingFacility(String receivingFacility) {
		this.receivingFacility = receivingFacility;
	}

	public final boolean isIgnoreUnscheduled() {
		return ignoreUnscheduled;
	}

	public final void setIgnoreUnscheduled(boolean ignoreUnscheduled) {
		this.ignoreUnscheduled = ignoreUnscheduled;
	}

	public final boolean isIgnoreInProgress() {
		return ignoreInProgress;
	}

	public final void setIgnoreInProgress(boolean ignoreInProgress) {
		this.ignoreInProgress = ignoreInProgress;
	}

	public final boolean isOneORMperSPS() {
		return oneORMperSPS;
	}

	public final void setOneORMperSPS(boolean splitMPPS) {
		this.oneORMperSPS = splitMPPS;
	}

	public final boolean isLogXSLT() {
		return logXSLT;
	}

	public final void setLogXSLT(boolean logXSLT) {
		this.logXSLT = logXSLT;
	}

	public String getEjbProviderURL() {
        return EJBHomeFactory.getEjbProviderURL();
    }        

    public void setEjbProviderURL(String ejbProviderURL) {
        EJBHomeFactory.setEjbProviderURL(ejbProviderURL);
    }

	private MPPSManagerHome getMPPSManagerHome() throws HomeFactoryException {
        return (MPPSManagerHome) EJBHomeFactory.getFactory().lookup(
                MPPSManagerHome.class, MPPSManagerHome.JNDI_NAME);
    }
	
	public final ObjectName getMppsScpServiceName() {
        return mppsScpServiceName;
    }

    public final void setMppsScpServiceName(ObjectName mppsScpServiceName) {
        this.mppsScpServiceName = mppsScpServiceName;
    }

    public final ObjectName getHl7SendServiceName() {
		return hl7SendServiceName;
	}

	public final void setHl7SendServiceName(ObjectName hl7SendServiceName) {
		this.hl7SendServiceName = hl7SendServiceName;
	}

	protected void startService() throws Exception {
        server.addNotificationListener(mppsScpServiceName,
                this,
                MPPSScpService.NOTIF_FILTER,
                null);
        logDir = new File(ServerConfigLocator.locate().getServerHomeDir(), "log");
    }

    protected void stopService() throws Exception {
        server.removeNotificationListener(mppsScpServiceName,
                this,
                MPPSScpService.NOTIF_FILTER,
                null);
    }
	
	/* (non-Javadoc)
	 * @see javax.management.NotificationListener#handleNotification(javax.management.Notification, java.lang.Object)
	 */
	public void handleNotification(Notification notif, Object handback) {
        Dataset mpps = (Dataset) notif.getUserData();
		if (ignoreInProgress && "IN PROGRESS".equals(mpps.getString(Tags.PPSStatus)))
			return;
		
		final String iuid = mpps.getString(Tags.SOPInstanceUID);
		mpps = getMPPS(iuid);
		DcmElement sq = mpps.get(Tags.ScheduledStepAttributesSeq);
		if (sq == null || sq.isEmpty()) {
			log.error("Missing Scheduled Step Attributes Seq in MPPS - " + iuid);
			return;
		}
		if (ignoreUnscheduled 
				&& sq.getItem().getString(Tags.AccessionNumber) == null) {
			return;
		}
		if (oneORMperSPS) {
			for (int i = 0, n = sq.vm(); i < n; i++) {
				mpps.putSQ(Tags.ScheduledStepAttributesSeq).addItem(
						sq.getItem(i));
				scheduleORM(makeORM(mpps));
			}			
		} else {
			scheduleORM(makeORM(mpps));
		}
	}

	private void scheduleORM(byte[] bs) {
		if (bs == null)
			return;
		try {
			server.invoke(hl7SendServiceName, "forward",
					new Object[] { bs }, 
					new String[] { byte[].class.getName() });
		} catch (Exception e) {
			log.error("Failed to schedule ORM", e);
		}		
	}

	private byte[] makeORM(Dataset mpps)
	{
		if (mpps == null)
			return null;
		try {
			if (logXSLT)
				try {
					logXSLT(mpps);
				} catch (Exception e) {
					log.warn("Failed to log XSLT:", e);
				}
			ByteArrayOutputStream out = 
					new ByteArrayOutputStream(INIT_BUFFER_SIZE);
			TransformerHandler th = getTransformerHandler();
	        XMLWriter xmlWriter = new HL7XMLWriter();
	        xmlWriter.setOutputStream(out);
			th.setResult(new SAXResult(xmlWriter.getContentHandler()));
			mpps.writeDataset2(th, null, null, 64, null);
			log.info(new String(out.toByteArray()));
			return out.toByteArray();
		} catch (Exception e) {
			log.error("Failed to convert MPPS to ORM", e);
			log.error(mpps);
			return null;
		}
	}


	private void logXSLT(Dataset mpps) throws Exception {
		SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory
        .newInstance();
		String uid = mpps.getString(Tags.SOPInstanceUID);
		logXSLT(mpps, tf.newTransformerHandler(),
				new File(logDir, "mpps-" + uid + ".xml"));
		logXSLT(mpps, getTransformerHandler(),
				new File(logDir, "mpps-" + uid + ".orm.xml"));
	}

	private void logXSLT(Dataset mpps, TransformerHandler th, File logFile)
	throws Exception {
		th.setResult(new StreamResult(logFile));
		mpps.writeDataset2(th, null, null, 64, null);
 	}


	private TransformerHandler getTransformerHandler()
	throws Exception
	{
		SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory
        .newInstance();
		if (stylesheet == null) {
			stylesheet = tf.newTemplates(
					new StreamSource(stylesheetURL));
		}
		TransformerHandler th = tf.newTransformerHandler(stylesheet);		
		Transformer t = th.getTransformer();
        t.setParameter("SendingApplication", sendingApplication);
        t.setParameter("SendingFacility", sendingFacility);
        t.setParameter("ReceivingApplication", receivingApplication);
        t.setParameter("ReceivingFacility", receivingFacility);
		return th;
	}
	
	
	private Dataset getMPPS(String iuid) {
		try {
			MPPSManager mgr = getMPPSManagerHome().create();
			try {
				return mgr.getMPPS(iuid);
			} finally {
				try {
					mgr.remove();
				} catch (Exception ignore) {
				}
			}
		} catch (Exception e) {
			log.error("Failed to load MPPS - " + iuid, e);
			return null;
		}
	}
}
