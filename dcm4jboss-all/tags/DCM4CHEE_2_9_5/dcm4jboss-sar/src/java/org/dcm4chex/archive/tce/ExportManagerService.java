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

package org.dcm4chex.archive.tce;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import javax.ejb.CreateException;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.management.Notification;
import javax.management.NotificationFilterSupport;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.dcm4che.auditlog.AuditLoggerFactory;
import org.dcm4che.auditlog.Destination;
import org.dcm4che.auditlog.Patient;
import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.DcmParser;
import org.dcm4che.data.DcmParserFactory;
import org.dcm4che.data.DcmValueException;
import org.dcm4che.data.FileFormat;
import org.dcm4che.dict.DictionaryFactory;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDDictionary;
import org.dcm4che.dict.UIDs;
import org.dcm4che.dict.VRs;
import org.dcm4che.net.AAssociateAC;
import org.dcm4che.net.AAssociateRQ;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.Association;
import org.dcm4che.net.AssociationFactory;
import org.dcm4che.net.Dimse;
import org.dcm4che.net.DimseListener;
import org.dcm4che.net.PDU;
import org.dcm4che.net.PresContext;
import org.dcm4che.util.UIDGenerator;
import org.dcm4chex.archive.common.DatasetUtils;
import org.dcm4chex.archive.common.SeriesStored;
import org.dcm4chex.archive.config.DicomPriority;
import org.dcm4chex.archive.ejb.interfaces.StorageHome;
import org.dcm4chex.archive.ejb.jdbc.AECmd;
import org.dcm4chex.archive.ejb.jdbc.AEData;
import org.dcm4chex.archive.ejb.jdbc.FileInfo;
import org.dcm4chex.archive.ejb.jdbc.QueryCmd;
import org.dcm4chex.archive.ejb.jdbc.RetrieveCmd;
import org.dcm4chex.archive.exceptions.ConfigurationException;
import org.dcm4chex.archive.exceptions.UnkownAETException;
import org.dcm4chex.archive.mbean.TLSConfigDelegate;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.dcm4chex.archive.util.FileDataSource;
import org.dcm4chex.archive.util.FileUtils;
import org.dcm4chex.archive.util.HomeFactoryException;
import org.dcm4chex.archive.util.JMSDelegate;
import org.jboss.system.ServiceMBeanSupport;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 2495 $ $Date: 2006-05-26 20:33:15 +0800 (周五, 26 5月 2006) $
 * @since Dec 19, 2005
 */
public class ExportManagerService extends ServiceMBeanSupport implements
		NotificationListener, MessageListener, DimseListener {

    private static final int PCID = 1;

	private static final UIDGenerator uidgen = UIDGenerator.getInstance();

    private static final String[] NONE = {};
	
	private static final NotificationFilterSupport seriesStoredFilter 
			= new NotificationFilterSupport();

	static {
		seriesStoredFilter.enableType(SeriesStored.class.getName());
	}
	
	private ObjectName storeScpServiceName;

	private String queueName;

	private int concurrency = 1;
	
	private String[] exportSelectorTitles = NONE;
	private String[] delayReasons = NONE;
	private String[][] personNames = null;

	private File dispConfigFile;
	private Hashtable configs = new Hashtable();
	
    private TLSConfigDelegate tlsConfig = new TLSConfigDelegate(this);

    private String callingAET;

    private int acTimeout;

    private int dimseTimeout;

    private int soCloseDelay;

    private int bufferSize;
    
    private boolean deleteKeyObjects;

    private ObjectName auditLogName;

    public final int getBufferSize() {
        return bufferSize ;
    }

    public final void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }
    
	public final String getExportSelectorTitles() {
        return codes2str(exportSelectorTitles);
	}

    public final void setExportSelectorTitles(String s) {
        this.exportSelectorTitles = str2codes(s);
	}

	public final String getDelayReasons() {
        return codes2str(delayReasons);
	}

    public final void setDelayReasons(String s) {
        this.delayReasons = str2codes(s);
	}

	public final String getPersonNameMapping() {
		if ( personNames == null ) return "NONE\r\n";
		StringBuffer sb = new StringBuffer();
		for ( int i = 0 ; i < personNames.length ; i++) {
			sb.append(personNames[i][0]).append(":").append(personNames[i][1]);
            sb.append("\r\n");
		}
        return sb.toString();
	}

	public final void setPersonNameMapping(String s) {
	    StringTokenizer st = new StringTokenizer( s, ";\t\r\n");
	    this.personNames = new String[st.countTokens()][2];
	    String pn;
	    int pos;
	    for ( int i = 0 ; i < personNames.length ; i++ ) {
	        pn = st.nextToken();
	        pos = pn.indexOf(':');
            if (pos == -1) { // assume NONE
                personNames = null;
                return;
            }
	        personNames[i] = new String[2];
	        personNames[i][0] = pn.substring(0,pos++);
	        personNames[i][1] = pn.substring(pos);
	    }
	}
    
    private String codes2str(String[] codes)
    {
        if (codes.length == 0)
            return "NONE";
        String sep = System.getProperty("line.separator", "\n");
        StringBuffer sb = new StringBuffer(codes[0]);        
        for (int i = 1; i < codes.length; ++i)
            sb.append((i & 1) != 0 ? "^" : sep).append(codes[i]);
        
        return sb.toString();
    }

	private String[] str2codes(String s)
	{
	    if (s.equalsIgnoreCase("NONE"))
	        return NONE;
	    StringTokenizer stk = new StringTokenizer(s, "^,; \r\n\t");
	    String[] tmp = new String[stk.countTokens() & ~1];
	    for (int i = 0; i < tmp.length; i++)
	        tmp[i] = stk.nextToken();
	    return tmp;
	}

    public String getEjbProviderURL() {
		return EJBHomeFactory.getEjbProviderURL();
	}

	public void setEjbProviderURL(String ejbProviderURL) {
		EJBHomeFactory.setEjbProviderURL(ejbProviderURL);
	}

	public final String getDispositionConfigFile() {
		return dispConfigFile.getPath();
	}

	public final void setDispositionConfigFile(String path) {
		this.dispConfigFile = new File(path.replace('/', File.separatorChar));
	}

    public String getCallingAET() {
        return callingAET;
    }

    public void setCallingAET(String aet) {
        this.callingAET = aet;
    }
    
	/**
	 * @return Returns the deleteKeyObjects.
	 */
	public boolean isDeleteKeyObjects() {
		return deleteKeyObjects;
	}
	/**
	 * @param deleteKeyObjects The deleteKeyObjects to set.
	 */
	public void setDeleteKeyObjects(boolean deleteKeyObjects) {
		this.deleteKeyObjects = deleteKeyObjects;
	}
    public final int getAcTimeout() {
        return acTimeout;
    }

    public final void setAcTimeout(int acTimeout) {
        this.acTimeout = acTimeout;
    }

    public final int getDimseTimeout() {
        return dimseTimeout;
    }

    public final void setDimseTimeout(int dimseTimeout) {
        this.dimseTimeout = dimseTimeout;
    }

    public final int getSoCloseDelay() {
        return soCloseDelay;
    }

    public final void setSoCloseDelay(int soCloseDelay) {
        this.soCloseDelay = soCloseDelay;
    }
    
	public final ObjectName getStoreScpServiceName() {
		return storeScpServiceName;
	}

	public final void setStoreScpServiceName(ObjectName storeScpServiceName) {
		this.storeScpServiceName = storeScpServiceName;
	}

    public final ObjectName getAuditLoggerName() {
        return auditLogName;
    }

    public final void setAuditLoggerName(ObjectName auditLogName) {
        this.auditLogName = auditLogName;
    }
    
	public final String getQueueName() {
		return queueName;
	}

	public final void setQueueName(String queueName) {
		this.queueName = queueName;
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

    public final ObjectName getTLSConfigName() {
        return tlsConfig.getTLSConfigName();
    }

    public final void setTLSConfigName(ObjectName tlsConfigName) {
        tlsConfig.setTLSConfigName(tlsConfigName);
    }

    public final int getReceiveBufferSize() {
        return tlsConfig.getReceiveBufferSize();        
    }
    
    public final void setReceiveBufferSize(int size) {
        tlsConfig.setReceiveBufferSize(size);
    }

    public final int getSendBufferSize() {
        return tlsConfig.getSendBufferSize();        
    }
    
    public final void setSendBufferSize(int size) {
        tlsConfig.setSendBufferSize(size);
    }
        
    public final boolean isTcpNoDelay() {
        return tlsConfig.isTcpNoDelay();
    }

    public final void setTcpNoDelay(boolean on) {
        tlsConfig.setTcpNoDelay(on);
    }
        
	protected void startService() throws Exception {
		JMSDelegate.startListening(queueName, this, concurrency);
		server.addNotificationListener(storeScpServiceName, this,
				seriesStoredFilter, null);
	}

	protected void stopService() throws Exception {
		server.removeNotificationListener(storeScpServiceName, this,
				seriesStoredFilter, null);
		JMSDelegate.stopListening(queueName);
	}

	public void handleNotification(Notification notif, Object handback) {
		SeriesStored seriesStored = (SeriesStored) notif.getUserData();
        Dataset ian = seriesStored.getIAN();
		String suid = ian.getString(Tags.StudyInstanceUID);
		for (int i = 1; i < exportSelectorTitles.length; i++, i++)
			onSeriesStored(suid, exportSelectorTitles[i-1], exportSelectorTitles[i]);
	}	
	
	private void onSeriesStored(String suid, String code, String designator) {
		List list = queryExportSelectors(suid, code, designator);
		for (Iterator iter = list.iterator(); iter.hasNext();) {
			Dataset manifest = (Dataset) iter.next();
            if (!isAllReceived(manifest))
            	return;
            try
            {
                manifest = loadManifest(manifest);
                if (isDelayed(manifest))
                	return;
                schedule(new ExportTFOrder(manifest), 0L);
                if ( isDeleteKeyObjects() )
                	delete(manifest);
            }
            catch (Exception e)
            {
    			log.error("Failed to process export selector with iuid=" 
    					+ manifest.getString(Tags.SOPInstanceUID), e);
            }
		}		
	}

	private StorageHome getStorageHome() throws HomeFactoryException {
        return (StorageHome) EJBHomeFactory.getFactory().lookup(
                StorageHome.class, StorageHome.JNDI_NAME);
    }

    private void delete(Dataset manifest) 
    throws RemoteException, FinderException, RemoveException, CreateException, 
    		HomeFactoryException {
		ArrayList list = new ArrayList();
		list.add(manifest.getString(Tags.SOPInstanceUID));
        copyIUIDs(manifest.get(Tags.IdenticalDocumentsSeq), list);
        final String[] uids = (String[]) list.toArray(new String[list.size()]);
		getStorageHome().create().deleteInstances(uids, true, false);
	}

	private Dataset loadManifest(Dataset sel) throws SQLException, IOException
    {
        Dataset keys = DcmObjectFactory.getInstance().newDataset();
        keys.putUI(Tags.SOPInstanceUID, sel.getString(Tags.SOPInstanceUID));
        RetrieveCmd cmd = RetrieveCmd.createInstanceRetrieve(keys);
        FileInfo fileInfo = cmd.getFileInfos()[0][0];
        File file = FileUtils.toFile(fileInfo.basedir, fileInfo.fileID);
        log.info("M-READ file:" + file);
        FileInputStream fis = new FileInputStream(file);
        try
        {
            BufferedInputStream bis = new BufferedInputStream(fis);
            DcmParser parser = DcmParserFactory.getInstance().newDcmParser(bis);
            Dataset ds = DcmObjectFactory.getInstance().newDataset();
            parser.setDcmHandler(ds.getDcmHandler());
            parser.parseDcmFile(FileFormat.DICOM_FILE, -1);
            DatasetUtils.fromByteArray(fileInfo.instAttrs, ds);
            DatasetUtils.fromByteArray(fileInfo.seriesAttrs, ds);
            DatasetUtils.fromByteArray(fileInfo.studyAttrs, ds);
            DatasetUtils.fromByteArray(fileInfo.patAttrs, ds);
            return ds;
        }
        finally
        {
            fis.close();
        }
    }

	private List queryExportSelectors(String suid, String code, String designator) {
		ArrayList list = new ArrayList();
		Dataset keys = DcmObjectFactory.getInstance().newDataset();
		keys.putUI(Tags.StudyInstanceUID, suid);
		keys.putUI(Tags.SOPClassUID, UIDs.KeyObjectSelectionDocument);
		Dataset item = keys.putSQ(Tags.ConceptNameCodeSeq).addNewItem();
		item.putSH(Tags.CodeValue, code);
		item.putSH(Tags.CodingSchemeDesignator, designator);
		QueryCmd query = null;
		try {
			query = QueryCmd.createInstanceQuery(keys, false, true);
			query.execute();
			while (query.next())
				list.add(query.getDataset());			
		} catch (SQLException e) {
			log.error("Query DB for Export Selectors " + code + '^'
					+ designator + " of study " + suid + " failed!", e);
		} finally {
			if (query != null)
				query.close();
		}
		return list;
	}

	private boolean isAllReceived(Dataset sel) {
		Dataset keys = DcmObjectFactory.getInstance().newDataset();
		ArrayList list = new ArrayList();
        copyIUIDs(sel.get(Tags.IdenticalDocumentsSeq), list);
		copyIUIDs(sel.get(Tags.CurrentRequestedProcedureEvidenceSeq), list);
		final String[] iuids = (String[]) list.toArray(new String[list.size()]);
		log.info("Check if " + iuids.length + " referenced objects were already received");
		keys.putUI(Tags.SOPInstanceUID, iuids);
		QueryCmd query = null;
		try {
			query = QueryCmd.createInstanceQuery(keys, false, true);
			query.execute();
			for (int i = iuids.length; i > 0; i--) {
				if (!query.next())
				{
					log.info("Waiting for receive of " + i
							+ " referenced objects");
					return false;
				}
			}
			log.info("All " + iuids.length + " referenced objects received!");
			return true;
		} catch (SQLException e) {
			log.error("Query DB for Referenced Instances failed!", e);
		} finally {
			if (query != null)
				query.close();
		}
		return false;
	}
	
	private boolean isDelayed(Dataset manifest) {
        DcmElement sq = manifest.get(Tags.ContentSeq);
        for (int i = 0, n = sq.countItems(); i < n; i++) {
            Dataset item = sq.getItem(i);
            Dataset cn = item.getItem(Tags.ConceptNameCodeSeq);
            if (cn != null && "113011".equals(cn.getString(Tags.CodeValue))
                    && "DCM".equals(cn.getString(Tags.CodingSchemeDesignator)))
            {
            	Dataset code = item.getItem(Tags.ConceptCodeSeq);
            	if (code == null)
            	{
            		log.warn("Missing Value for Document Title Modifier in Export Selector:");
            		log.warn(manifest);
            		return false;
            	}
        		final String cv = code.getString(Tags.CodeValue);
				final String cs = code.getString(Tags.CodingSchemeDesignator);
				log.info("Detect Document Title Modifier in Export Selector:" 
        				+ cv + "^" + cs + "^" + code.getString(Tags.CodeMeaning));
        		for (int j = 1; j < delayReasons.length; j++,j++) {
					if (cv.equals(delayReasons[j-1]) && cs.equals(delayReasons[j]))
					{
						log.info("Delay Export of teaching files");
						return true;
					}
            	}
            }
        }
        return false;
	}
	
	
	public void onMessage(Message message) {
		ObjectMessage om = (ObjectMessage) message;
		try {
			ExportTFOrder order = (ExportTFOrder) om.getObject();
			log.info("Start processing " + order);
			try {
				process(order);
				log.info("Finished processing " + order);
			} catch (Exception e) {
				log.warn("Failed to process " + order, e);
			}
		} catch (Throwable e) {
			log.error("unexpected error during processing message: " + message,
					e);
		}
	}

	private void schedule(ExportTFOrder order, long scheduledTime)
	throws JMSException {
		log.info("Scheduling " + order);
		JMSDelegate.queue(queueName, order, Message.DEFAULT_PRIORITY,
				scheduledTime);
	}
	
	private void process(ExportTFOrder order) throws Exception {
		Dataset manifest = order.getManifest();
		Properties config = getConfig(manifest);
		String dest = config.getProperty("destination");
        final String export = config.getProperty("export");
        final int prior = DicomPriority.toCode(config.getProperty("export-priority", "MEDIUM"));
        final int exportManifest = export.indexOf("MANIFEST");
        final boolean exportInstances = export.indexOf("INSTANCES") != -1;
        final boolean exportMedia = export.indexOf("MEDIA") != -1;
        
        HashMap attrs = new HashMap();
		FileInfo[] fileInfos = queryAttrs(manifest, attrs);
        int[] pcids = new int[fileInfos.length + 1];
        ActiveAssociation a = openAssociation(fileInfos, pcids, dest, 
                exportManifest != -1, exportInstances, exportMedia);
        HashMap iuidMap = new HashMap(); 
        coerceAttributes(manifest, config, iuidMap);
        if (!"NO".equalsIgnoreCase(config.getProperty("remove-delay-reason")))
            removeDelayReason(manifest);
        if (exportManifest == 0)
            sendManifests(a, pcids[fileInfos.length], manifest, prior);
        if (exportInstances)
        {
            byte[] b = new byte[bufferSize];
            for (int i = 0; i < fileInfos.length; i++)
            {
                FileInfo info = fileInfos[i];
                Dataset ds = (Dataset) attrs.get(info.sopIUID);
                sendInstance(a, pcids[i], ds, info, config,  iuidMap, b, prior);
            }
        }
        if (exportManifest > 0)
            sendManifests(a, pcids[fileInfos.length], manifest, prior);
        if (exportMedia) {
            sendMediaCreationRequest(a, manifest, config);
        }
        a.release(true);
	}

    private void sendMediaCreationRequest(ActiveAssociation aa,
            Dataset manifest, Properties config)
    throws InterruptedException, IOException {
        AuditLoggerFactory alf = AuditLoggerFactory.getInstance();
        Patient pat = alf.newPatient(
                manifest.getString(Tags.PatientID),
                manifest.getString(Tags.PatientName));
        DcmObjectFactory df = DcmObjectFactory.getInstance();
        Command cmd = df.newCommand();
        Association a = aa.getAssociation();
        PresContext pc = a.getAcceptedPresContext(
                UIDs.MediaCreationManagementSOPClass, UIDs.ImplicitVRLittleEndian);
        int pid = pc.pcid();
        String iuid = uidgen.createUID();
        cmd.initNCreateRQ(a.nextMsgID(),
                UIDs.MediaCreationManagementSOPClass, iuid);
        Dataset data = df.newDataset();
        String s;
        if ((s = config.getProperty("create-media-label-from-instances")) != null) {
            data.putCS(Tags.LabelUsingInformationExtractedFromInstances, s);
        }
        if ((s = config.getProperty("create-media-label-text")) != null) {
            data.putUT(Tags.LabelText, s);
        }
        if ((s = config.getProperty("create-media-label-style")) != null) {
            data.putCS(Tags.LabelStyleSelection, s);
        }
        if ((s = config.getProperty("create-media-disposition")) != null) {
            data.putLT(Tags.MediaDisposition, s);
        }
        if ((s = config.getProperty("create-media-allow-media-splitting")) != null) {
            data.putCS(Tags.AllowMediaSplitting, s);
        }
        if ((s = config.getProperty("create-media-allow-lossy-compression")) != null) {
            data.putCS(Tags.AllowLossyCompression, s);
        }
        if ((s = config.getProperty("create-media-include-non-dicom")) != null) {
            data.putCS(Tags.IncludeNonDICOMObjects, s);
        }
        if ((s = config.getProperty("create-media-include-display-app")) != null) {
            data.putCS(Tags.IncludeDisplayApplication, s);
        }
        data.putCS(Tags.PreserveCompositeInstancesAfterMediaCreation, "NO");
        String appProfile = config.getProperty("create-media-app-profile");
        DcmElement refSOPSeq = data.putSQ(Tags.RefSOPSeq);
        DcmElement sopInstRefSeq = manifest.get(Tags.CurrentRequestedProcedureEvidenceSeq);
        for (int i = 0, n = sopInstRefSeq.countItems(); i < n; i++) {
            Dataset refStudyItem = sopInstRefSeq.getItem(i);
            pat.addStudyInstanceUID(refStudyItem.getString(Tags.StudyInstanceUID));
            DcmElement refSerSeq = refStudyItem.get(Tags.RefSeriesSeq);
            for (int j = 0, m = refSerSeq.countItems(); j < m; j++) {
                Dataset refSer = refSerSeq.getItem(j);
                DcmElement srcRefSOPSeq = refSer.get(Tags.RefSOPSeq);
                for (int k = 0, l = srcRefSOPSeq.countItems(); k < l; k++) {                    
                    Dataset srcRefSOP = srcRefSOPSeq.getItem(k);
                    Dataset refSOP = df.newDataset();
                    refSOP.putUI(Tags.RefSOPClassUID,
                            srcRefSOP.getString(Tags.RefSOPClassUID));
                    refSOP.putUI(Tags.RefSOPInstanceUID,
                            srcRefSOP.getString(Tags.RefSOPInstanceUID));
                    if (appProfile != null) {
                        refSOP.putCS(Tags.RequestedMediaApplicationProfile, appProfile);
                    }
                    refSOPSeq.addItem(refSOP);
                }
            }
        }
        AssociationFactory af = AssociationFactory.getInstance();
        Dimse dimse = af.newDimse(pid, cmd, data);
        Command rsp = aa.invoke(dimse).get().getCommand();
        if (rsp.getStatus() != 0) {
            log.warn("Request Media Creation failed: " + rsp);
            return;
        }
        cmd.clear();
        data.clear();
        cmd.initNActionRQ(a.nextMsgID(), UIDs.MediaCreationManagementSOPClass,
                iuid, 1);
        if ((s = config.getProperty("create-media-copies")) != null) {
            data.putIS(Tags.NumberOfCopies, s);
        }
        if ((s = config.getProperty("create-media-priority")) != null) {
            data.putCS(Tags.RequestPriority, s);
        }        
        dimse = af.newDimse(pid, cmd, data);
        rsp = aa.invoke(dimse).get().getCommand();
        if (rsp.getStatus() != 0) {
            log.warn("Initiate Media Creation failed: " + rsp);
            return;
        }
        logExport(extractPersonObserverName(manifest), pat, 
                config.getProperty("create-media-type"));
    }

    private String extractPersonObserverName(Dataset manifest) {
        DcmElement contentSeq = manifest.get(Tags.ContentSeq);
        for (int i = 0, n = contentSeq.countItems(); i < n; i++) {
            Dataset item = contentSeq.getItem(i);
            Dataset conceptName = item.getItem(Tags.ConceptNameCodeSeq);
            if ("121008".equals(conceptName.getString(Tags.CodeValue)) && 
                    "DCM".equals(conceptName.getString(Tags.CodingSchemeDesignator))) {
                return item.getString(Tags.PersonName);
            }
        }
        return null;
    }

    private void logExport(String user, Patient pat, String mediaType) {
        if (auditLogName == null) return;
        try {
            server.invoke(auditLogName,
                    "logExport",
                    new Object[] { user , new Patient[]{pat}, mediaType, null, null},
                    new String[] { String.class.getName(),
                        Patient[].class.getName(), String.class.getName(), 
                        String.class.getName(), Destination.class.getName()});
        } catch (Exception e) {
            log.warn("Audit Log failed:", e);
        }
    }
    
    public void dimseReceived(Association assoc, Dimse dimse)
    {
        // TODO Auto-generated method stub
        
    }
    	
    private void sendInstance(ActiveAssociation a, int pcid, Dataset attrs, 
            FileInfo fileInfo, Properties config, HashMap iuidMap, byte[] buffer, int prior) 
    throws InterruptedException, IOException
    {
        coerceAttributes(attrs, config, iuidMap);
        File f = FileUtils.toFile(fileInfo.basedir, fileInfo.fileID);
        Command cmd = DcmObjectFactory.getInstance().newCommand();
        cmd.initCStoreRQ(a.getAssociation().nextMsgID(), fileInfo.sopCUID,
                fileInfo.sopIUID, prior);
        FileDataSource src = new FileDataSource(f, attrs, buffer);
        Dimse dimse = AssociationFactory.getInstance().newDimse(pcid, cmd, src);
        a.invoke(dimse, this);        
    }

    private void coerceAttributes(Dataset attrs, Properties config, HashMap iuidMap)
    throws DcmValueException
    {
    	String cuid = attrs.getString(Tags.SOPClassUID);
    	String iuid = attrs.getString(Tags.SOPInstanceUID);
    	UIDDictionary dict = DictionaryFactory.getInstance().getDefaultUIDDictionary();
        int numpasses = Integer.parseInt(config.getProperty("num-coerce-passes", "0"));
        for (int i = 0; i < numpasses; i++)
        {
        	int count = 0;
            String prefix = "" + (i+1) + ".";
            for (Iterator iter = config.entrySet().iterator(); iter.hasNext();)
            {
                Map.Entry e = (Map.Entry) iter.next();
                String key = (String) e.getKey();
                if (key.startsWith(prefix))
                {
                    coerceAttribute(attrs, key.substring(prefix.length()), (String) e.getValue());
                    ++count;
                }
            }
            log.info("Coerce " + count + " attributes in " 
            		+ dict.toString(cuid) + " with iuid:" + iuid + " in "
            		+ prefix + "pass");
        }
        boolean replaceUIDs = "YES".equalsIgnoreCase(
                config.getProperty("replace-uids"));
        if (replaceUIDs)
        {
            int count = replaceUIDs(attrs, iuidMap);
            log.info("Replace " + count + " UIDs in " 
            		+ dict.toString(cuid) + " with original iuid:" + iuid);
        }
    }

    private void sendManifests(ActiveAssociation a, int pcid, Dataset manifest, 
            int prior)
    throws InterruptedException, IOException
    {
        sendManifest(a, pcid, manifest, prior);
        DcmElement identicalsq = manifest.get(Tags.IdenticalDocumentsSeq);
        if (identicalsq != null && !identicalsq.isEmpty())
        {
            Dataset studyItem = DcmObjectFactory.getInstance().newDataset();
            studyItem.putUI(Tags.StudyInstanceUID, manifest.getString(Tags.StudyInstanceUID));
            Dataset seriesItem = studyItem.putSQ(Tags.RefSeriesSeq).addNewItem();
            seriesItem.putUI(Tags.SeriesInstanceUID, manifest.getString(Tags.SeriesInstanceUID));
            Dataset refSOPItem = seriesItem.putSQ(Tags.RefSOPSeq).addNewItem();
            refSOPItem.putUI(Tags.RefSOPInstanceUID, manifest.getString(Tags.SOPInstanceUID));
            refSOPItem.putUI(Tags.RefSOPClassUID, manifest.getString(Tags.SOPClassUID));
            for (int i = 0, n = identicalsq.countItems(); i < n; i++)
            {
                Dataset otherStudyItem = identicalsq.getItem(i);
                manifest.putUI(Tags.StudyInstanceUID,
                		otherStudyItem.getString(Tags.StudyInstanceUID));
                Dataset otherSeriesItem = otherStudyItem.getItem(Tags.RefSeriesSeq);
                manifest.putUI(Tags.SeriesInstanceUID,
                		otherSeriesItem.getString(Tags.SeriesInstanceUID));
                Dataset otherRefSOPItem = otherSeriesItem.getItem(Tags.RefSOPSeq);
                manifest.putUI(Tags.SOPInstanceUID, 
                        otherRefSOPItem.getString(Tags.RefSOPInstanceUID));
                DcmElement otherIdenticalsq = manifest.putSQ(Tags.IdenticalDocumentsSeq);
                for (int j = 0; j < n; j++)
                    otherIdenticalsq.addItem(i == j ? studyItem : identicalsq.getItem(j));                 
                sendManifest(a, pcid, manifest, prior);
            }
        }
    }

    private void removeDelayReason(Dataset manifest)
    {
        DcmElement oldsq = manifest.get(Tags.ContentSeq);
        DcmElement newsq = manifest.putSQ(Tags.ContentSeq);
        for (int i = 0, n = oldsq.countItems(); i < n; i++) {
            Dataset item = oldsq.getItem(i);
            Dataset cn = item.getItem(Tags.ConceptNameCodeSeq);
            if (cn != null && "113011".equals(cn.getString(Tags.CodeValue))
                    && "DCM".equals(cn.getString(Tags.CodingSchemeDesignator)))
                        continue;
            newsq.addItem(item);
        }        
    }

    private void sendManifest(ActiveAssociation a, int pcid, Dataset manifest, int prior)
    throws InterruptedException, IOException
    {
        Command cmd = DcmObjectFactory.getInstance().newCommand();
        cmd.initCStoreRQ(a.getAssociation().nextMsgID(),
                manifest.getString(Tags.SOPClassUID),
                manifest.getString(Tags.SOPInstanceUID), prior);
        Dimse dimse = AssociationFactory.getInstance().newDimse(pcid, cmd, manifest);
        a.invoke(dimse, this);        
    }

    private ActiveAssociation openAssociation(FileInfo[] fileInfos, int[] pcids,
            String dest, boolean exportManifest, boolean exportInstances,
            boolean exportMedia)
    throws SQLException, UnkownAETException, IOException, InterruptedException
    {
        AEData aeData = new AECmd(dest).getAEData();
        if (aeData == null) {
            throw new UnkownAETException("Unkown Destination AET: " + dest);
        }
        AssociationFactory af = AssociationFactory.getInstance();

        AAssociateRQ rq = af.newAAssociateRQ();
        rq.setCallingAET(callingAET);
        rq.setCalledAET(dest);
        HashMap cuids = new HashMap();
        if (exportManifest)
            cuids.put(UIDs.KeyObjectSelectionDocument, new HashSet());
        if (exportInstances)
            for (int i = 0; i < fileInfos.length; i++)
            {
                FileInfo info = fileInfos[i];
                Set tsuids = (Set) cuids.get(info.sopCUID);
                if (tsuids == null)
                     cuids.put(info.sopCUID, tsuids = new HashSet());
                tsuids.add(info.tsUID);
            }
        for (Iterator iter = cuids.entrySet().iterator(); iter.hasNext();)
        {
            Map.Entry e = (Map.Entry) iter.next();
            String cuid = (String) e.getKey();
            Set tsuids = (Set) e.getValue();
            tsuids.add(UIDs.ImplicitVRLittleEndian);
            for (Iterator iterator = tsuids.iterator(); iterator.hasNext();)
            {
                String tsuid = (String) iterator.next();
                PresContext pc = af.newPresContext(rq.nextPCID(), cuid, tsuid);
                rq.addPresContext(pc);
            }
        }
        if (exportMedia) {
            PresContext pc = af.newPresContext(rq.nextPCID(),
                    UIDs.MediaCreationManagementSOPClass,
                    UIDs.ImplicitVRLittleEndian);
            rq.addPresContext(pc);
        }
        Association a = af.newRequestor(tlsConfig.createSocket(aeData));
        a.setAcTimeout(acTimeout);
        a.setDimseTimeout(dimseTimeout);
        a.setSoCloseDelay(soCloseDelay);
        PDU ac = a.connect(rq);
        if (!(ac instanceof AAssociateAC)) {
            throw new IOException("Association not accepted by " + dest + ": " + ac);
        }
        ActiveAssociation aa = af.newActiveAssociation(a, null);
        aa.start();
        if (exportManifest)
        {
            PresContext pc = a.getAcceptedPresContext(
                    UIDs.KeyObjectSelectionDocument, UIDs.ImplicitVRLittleEndian);
            if (pc == null)
                throwStorageNotSupported(aa, UIDs.KeyObjectSelectionDocument, dest);
            pcids[fileInfos.length] = pc.pcid();
        }
        if (exportInstances)
            for (int i = 0; i < fileInfos.length; i++)
            {
                FileInfo info = fileInfos[i];
                PresContext pc = a.getAcceptedPresContext(
                        info.sopCUID, info.tsUID);
                if (pc == null)
                {
                    pc = a.getAcceptedPresContext(
                            info.sopCUID, UIDs.ImplicitVRLittleEndian);
                    if (pc == null)
                        throwStorageNotSupported(aa, info.sopCUID, dest);
                }
                pcids[i] = pc.pcid();
            }
        if (exportMedia) {
            PresContext pc = a.getAcceptedPresContext(
                    UIDs.MediaCreationManagementSOPClass, UIDs.ImplicitVRLittleEndian);
            if (pc == null)
                throwStorageNotSupported(aa, UIDs.MediaCreationManagementSOPClass, dest);            
        }
        return aa;
    }

    private void throwStorageNotSupported(ActiveAssociation aa, String uid, String dest)
    throws IOException, InterruptedException
    {
        aa.release(false);
        UIDDictionary dd = DictionaryFactory.getInstance().getDefaultUIDDictionary();
        throw new IOException(dd.toString(uid) + " not supported by " + dest);
    }

    private Properties getConfig(Dataset manifest) throws IOException {
        File indexFile = FileUtils.resolve(dispConfigFile);
        Properties index = getProperties(indexFile);
        DcmElement sq = manifest.get(Tags.ContentSeq);
        for (int i = 0, n = sq.countItems(); i < n; i++) {
            Dataset item = sq.getItem(i);
            if (!"TEXT".equals(item.getString(Tags.ValueType)))
                continue;
            Dataset cn = item.getItem(Tags.ConceptNameCodeSeq);
            if (cn != null && "113012".equals(cn.getString(Tags.CodeValue))
                    && "DCM".equals(cn.getString(Tags.CodingSchemeDesignator)))
            {
                String fname = index.getProperty(item.getString(Tags.TextValue));
                if (fname != null)
                    return getProperties(toConfigFile(indexFile, fname));
                break;
            }
        }
        Dataset code = manifest.getItem(Tags.ConceptNameCodeSeq);
        final String key = code.getString(Tags.CodeValue) + '^' 
                + code.getString(Tags.CodingSchemeDesignator);
        String fname = index.getProperty(key);
        if (fname == null) {
            throw new ConfigurationException(
                    "Missing entry for Concept Name Code " + key + " in "
                    +  indexFile);
        }            
        return getProperties(toConfigFile(indexFile, fname));
	}

    private File toConfigFile(File indexFile, String fname)
    {
        return new File(indexFile.getParentFile(), 
                fname.replace('/', File.separatorChar));
    }       

    private Properties getProperties(File f) throws IOException
    {
        Properties config = (Properties) configs.get(f);
        if (config == null)
        {
            config = new Properties();
            FileInputStream in = new FileInputStream(f);
    		try {
    			config.load(new BufferedInputStream(in));
    		} finally {
    			in.close();
    		}
    		configs.put(f, config);
        }
        return config;
    }
	
	private FileInfo[] queryAttrs(Dataset sel, Map attrs)
	throws Exception {
		ArrayList list = new ArrayList();
		copyIUIDs(sel.get(Tags.CurrentRequestedProcedureEvidenceSeq), list);
		Dataset keys = DcmObjectFactory.getInstance().newDataset();
		keys.putUI(Tags.SOPInstanceUID,
				(String[]) list.toArray(new String[list.size()]));
		RetrieveCmd cmd = RetrieveCmd.createInstanceRetrieve(keys);
		String patID = sel.getString(Tags.PatientID);
		FileInfo[][] a = cmd.getFileInfos();
        FileInfo[] b = new FileInfo[a.length];
		for (int i = 0; i < a.length; i++) {
			FileInfo info = b[i] = a[i][0];
			if (!equals(patID, info.patID))
				throw new Exception("Export Selector references studies for different patients");
            Dataset mergeAttrs = DatasetUtils.fromByteArray(info.patAttrs,
                    DatasetUtils.fromByteArray(info.studyAttrs,
                            DatasetUtils.fromByteArray(info.seriesAttrs,
                                    DatasetUtils.fromByteArray(info.instAttrs))));
            attrs.put(info.sopIUID, mergeAttrs);
		}
		return b;
	}

	private void copyIUIDs(DcmElement sq1, List list) {
		if (sq1 == null)
			return;
		for (int i1 = 0, n1 = sq1.countItems(); i1 < n1; ++i1) {
			Dataset item1 = sq1.getItem(i1);
			DcmElement sq2 = item1.get(Tags.RefSeriesSeq);
			for (int i2 = 0, n2 = sq2.countItems(); i2 < n2; ++i2) {
				Dataset item2 = sq2.getItem(i2);
				DcmElement sq3 = item2.get(Tags.RefSOPSeq);
				for (int i3 = 0, n3 = sq3.countItems(); i3 < n3; ++i3) {
					Dataset item3 = sq3.getItem(i3);
					String iuid = item3.getString(Tags.RefSOPInstanceUID);
					list.add(iuid);
				}
			}
		}
	}

	private static boolean equals(Object o1, Object o2) {
		return o1 == null ? o2 == null : o1.equals(o2);
	}
	
	private void coerceAttribute(Dataset attrs, String key, String pattern) {
		int tag = Tags.forName(key);
		if (pattern.length() == 0)
			deleteValue(attrs, tag);
		else if (pattern.equals("firstDayOfMonth()"))
			setFirstDayOfMonth(attrs, tag);
		else
			changeValue(attrs, tag, pattern);
	}

	private void deleteValue(Dataset attrs, int tag) {
		attrs.putXX(tag);
	}

	private void setFirstDayOfMonth(Dataset attrs, int tag) {
		DcmElement el = attrs.get(tag);
		if (el == null)
			return;
		try {
			Date date = el.getDate();
			date.setDate(1);
			if (el.vr() == VRs.DA)
				attrs.putDA(tag, date);
			else if (el.vr() == VRs.DT)
				attrs.putDT(tag, date);
			else {
				log.warn("Unexpected VR, delete value - " + el);
				attrs.putXX(tag);
			}
		} catch (DcmValueException e) {
			log.warn("Delete illegal Date value: " + el, e);
			attrs.putXX(tag);
		}
	}

	private void changeValue(Dataset attrs, int tag, String pattern) {
		StringBuffer sb = new StringBuffer();
		StringTokenizer stk = new StringTokenizer(pattern, "#${}", true);
		while (stk.hasMoreTokens()) {
			String tk = stk.nextToken();
			int ch = tk.charAt(0);
			if (ch != '#' && ch != '$' || !stk.hasMoreTokens())
			{
				sb.append(tk);
				continue;
			}
			tk = stk.nextToken();
			if (!tk.equals("{")  || !stk.hasMoreTokens())
			{
				sb.append(ch).append(tk);
				continue;
			}
			tk = stk.nextToken();
			int srctag = Tags.forName(tk);
			String s = attrs.getString(srctag);
			if (s != null)
			{
				if (ch == '#')
					s = Integer.toString(s.hashCode());
				sb.append(s);
			}
			if (stk.hasMoreTokens())
				stk.nextToken(); // skip "}"
		}
		attrs.putXX(tag, sb.toString());
	}

    private int replaceUIDs(Dataset ds, HashMap uidmap)
    throws DcmValueException
    {
    	int count = 0;
        for (Iterator iter = ds.iterator(); iter.hasNext();)
        {
            DcmElement elm = (DcmElement) iter.next();
            int tag = elm.tag();
            if (tag == Tags.SOPInstanceUID 
                    || tag == Tags.RefSOPInstanceUID 
                    || tag == Tags.SeriesInstanceUID
                    || tag == Tags.StudyInstanceUID) {
                String from = elm.getString(null);
                String to = (String) uidmap.get(from);
                if (to == null)
                {
                    to = uidgen.createUID();
                    uidmap.put(from, to);
                }
                ds.putUI(elm.tag(), to);
                count++;
            } else if (elm.vr() == VRs.SQ) {
                for (int i = 0, n = elm.countItems(); i < n; i++)
                    count += replaceUIDs(elm.getItem(i), uidmap);
            }
        }
        return count;        
    }

    public Collection listConfiguredDispositions() throws IOException
    {
        File indexFile = FileUtils.resolve(dispConfigFile);
        Properties index = getProperties(indexFile);
        ArrayList list = new ArrayList(index.keySet());
        Collections.sort(list);
        return list;
    }
    
    public String getObserverPerson(String user) {
    	if ( personNames != null ) {
	    	for ( int i = 0 ; i < personNames.length ; i++ ) {
	    		if ( personNames[i][0].equals(user)){
	    			return personNames[i][1];
	    		}
	    	}
    	}
    	return callingAET+"^"+user;
    }
    
    public void clearConfigCache() {
    	configs.clear();
    }
    
    public void storeExportSelection(Dataset manifest, int prior)
    throws SQLException, UnkownAETException, IOException, InterruptedException
    {
		String dest;
    	try {
    		String calledAETs = (String) server.getAttribute(this.storeScpServiceName,"CalledAETitles");
    		int pos = calledAETs.indexOf('\\');
    	    dest = pos == -1 ? calledAETs : calledAETs.substring(0,pos);
    	} catch ( Exception x ) {
    		x.printStackTrace();
    		throw new UnkownAETException("Cant get a CalledAET from StoreSCP service! Reason:"+x.getMessage());
    	}
    	ActiveAssociation aa = openAssociation(manifest.getString(Tags.SOPClassUID), dest);
    	sendManifests(aa, PCID, manifest, prior);
    	aa.release(true);
    }

    private ActiveAssociation openAssociation(String cuid, String dest)
    throws SQLException, UnkownAETException, IOException, InterruptedException
    {
        AEData aeData = new AECmd(dest).getAEData();
        if (aeData == null) {
            throw new UnkownAETException("Unkown Destination AET: " + dest);
        }
        AssociationFactory af = AssociationFactory.getInstance();

        AAssociateRQ rq = af.newAAssociateRQ();
        rq.setCallingAET(callingAET);
        rq.setCalledAET(dest);
        PresContext pc = af.newPresContext(PCID, cuid, UIDs.ImplicitVRLittleEndian);
        rq.addPresContext(pc);
        Association a = af.newRequestor(tlsConfig.createSocket(aeData));
        a.setAcTimeout(acTimeout);
        a.setDimseTimeout(dimseTimeout);
        a.setSoCloseDelay(soCloseDelay);
        PDU ac = a.connect(rq);
        if (!(ac instanceof AAssociateAC)) {
            throw new IOException("Association not accepted by " + dest + ": " + ac);
        }
        ActiveAssociation aa = af.newActiveAssociation(a, null);
        aa.start();
        if (((AAssociateAC) ac).getPresContext(PCID).result() != PresContext.ACCEPTANCE)
            throwStorageNotSupported(aa, cuid, dest);
        return aa;
    }
}
