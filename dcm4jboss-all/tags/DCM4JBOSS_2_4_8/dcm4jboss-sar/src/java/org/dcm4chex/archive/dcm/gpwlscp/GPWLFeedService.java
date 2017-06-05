/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.dcm.gpwlscp;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4che.util.UIDGenerator;
import org.dcm4chex.archive.common.DatasetUtils;
import org.dcm4chex.archive.dcm.mppsscp.MPPSScpService;
import org.dcm4chex.archive.ejb.interfaces.ContentManager;
import org.dcm4chex.archive.ejb.interfaces.ContentManagerHome;
import org.dcm4chex.archive.ejb.interfaces.GPWLManager;
import org.dcm4chex.archive.ejb.interfaces.GPWLManagerHome;
import org.dcm4chex.archive.ejb.interfaces.MPPSManager;
import org.dcm4chex.archive.ejb.interfaces.MPPSManagerHome;
import org.dcm4chex.archive.ejb.interfaces.MWLManager;
import org.dcm4chex.archive.ejb.interfaces.MWLManagerHome;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.dcm4chex.archive.util.HomeFactoryException;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.system.server.ServerConfig;
import org.jboss.system.server.ServerConfigLocator;
import org.xml.sax.InputSource;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1711 $ $Date: 2005-04-23 01:14:47 +0800 (周六, 23 4月 2005) $
 * @since 05.04.2005
 *
 */

public class GPWLFeedService extends ServiceMBeanSupport implements
		NotificationListener {

	private static final int[] PAT_ATTR_TAGS = {
		Tags.PatientName,
		Tags.PatientID,
		Tags.PatientBirthDate,
		Tags.PatientSex,
	};
	private static final int[] REF_RQ_TAGS_FROM_MPPS_SSA = {
		Tags.AccessionNumber,
		Tags.RefStudySeq,
		Tags.StudyInstanceUID,
		Tags.RequestedProcedureDescription,
		Tags.RequestedProcedureID,
	};
	
	private static final int[] REF_RQ_TAGS_FROM_MWL_ITEM = {
		Tags.RequestedProcedureCodeSeq,
		Tags.RequestingPhysician,
	};

	private static final int[] REF_RQ_TAGS_IN_MPPS_SSA_AND_MWL_ITEM = {
		Tags.AccessionNumber,
		Tags.RequestedProcedureDescription,
		Tags.RequestedProcedureID,
	};

    private ObjectName mppsScpServiceName;
    
    private Map humanPerformer = null;
	private Map templates = null;
	
	private URL templateURL  = null;
    
    private static DcmObjectFactory dof = DcmObjectFactory.getInstance();
	
    private static ServerConfig config = ServerConfigLocator.locate();

    public String getEjbProviderURL() {
        return EJBHomeFactory.getEjbProviderURL();
    }        

    public void setEjbProviderURL(String ejbProviderURL) {
        EJBHomeFactory.setEjbProviderURL(ejbProviderURL);
    }
    
	public final ObjectName getMppsScpServiceName() {
        return mppsScpServiceName;
    }

    public final void setMppsScpServiceName(ObjectName mppsScpServiceName) {
        this.mppsScpServiceName = mppsScpServiceName;
    }

	/**
	 * @return Returns the physicians.
	 */
	public String getHumanPerformer() {
		return codes2String( humanPerformer );
	}
	
	/**
	 * @param performer The human performer(s) to set.
	 */
	public void setHumanPerformer(String performer) {
		this.humanPerformer = string2Codes( performer, "PACS_TIANI" );
	}

	public String getTemplates() {
		return codes2String( templates);
	}
	
	/**
	 * Set templates as codes.
	 * <p>
	 * code value is filename, code meaning is template name, designator is ignored
	 * @param templates The templates to set.
	 */
	public void setTemplates(String templates) {
		this.templates = string2Codes( templates, null );
	}
	
	/**
	 * @return Returns the configURL.
	 */
	public String getTemplateURL() {
		String s = templateURL.toString();
		String base = config.getServerConfigURL().toExternalForm();
		if ( s.length() > base.length() && s.startsWith( base ) ) {
			return s.substring( base.length() );
		} else {
			return s;
		}
	}
	/**
	 * @param configURL The configURL to set.
	 * @throws MalformedURLException
	 */
	public void setTemplateURL(String configURL) throws MalformedURLException {
		try {
			this.templateURL = new URL( configURL );
		} catch (MalformedURLException e) { //not an URL -> relative to server config URL
			if ( configURL.indexOf( ':' ) != -1 ) throw e;//no protocol or drive letter in relative URL allowed!
			this.templateURL = new URL( config.getServerConfigURL().toExternalForm() + configURL);
		}
	}
	private String codes2String( Map codes ) {
		if ( codes == null || codes.isEmpty() ) return "";
		StringBuffer sb = new StringBuffer();
		Dataset ds;
		String design;
		for ( Iterator iter = codes.values().iterator() ; iter.hasNext() ;  ) {
			ds = (Dataset) iter.next();
			design = ds.getString( Tags.CodingSchemeDesignator );
			sb.append( ds.getString( Tags.CodeValue ) ).append("^");
			if ( design != null ) sb.append( design ).append("^");
			sb.append( ds.getString( Tags.CodeMeaning ) ).append(",");
		}
		
		return sb.substring(0, sb.length()-1);
	}
	
	private Map string2Codes( String codes, String defaultDesign ) {
		StringTokenizer st = new StringTokenizer(codes,",");
		Map map = new HashMap();
		int nrOfTokens;
		StringTokenizer stCode;
		Dataset ds;
		String codeValue;
		while ( st.hasMoreTokens() ) {
			stCode = new StringTokenizer( st.nextToken(), "^" );
			nrOfTokens = stCode.countTokens();
			if ( nrOfTokens < 2 ) {
				throw new IllegalArgumentException("Wrong format of human performer configuration! (<codeValue>[^<designator>]^<meaning>)");
			}
			ds = dof.newDataset();
			codeValue = stCode.nextToken();
	        ds.putSH(Tags.CodeValue, codeValue );
	        if ( nrOfTokens > 2 ) {
	        	ds.putSH(Tags.CodingSchemeDesignator, stCode.nextToken());
	        } else if ( defaultDesign != null ){
	        	ds.putSH(Tags.CodingSchemeDesignator, defaultDesign );
	        }
	        ds.putLO(Tags.CodeMeaning, stCode.nextToken());
	        map.put( codeValue, ds );
		}
		return map;
	}
	
    protected void startService() throws Exception {
        server.addNotificationListener(mppsScpServiceName,
                this,
                MPPSScpService.NOTIF_FILTER,
                null);
    }

    protected void stopService() throws Exception {
        server.removeNotificationListener(mppsScpServiceName,
                this,
                MPPSScpService.NOTIF_FILTER,
                null);
    }

	public void handleNotification(Notification notif, Object handback) {
        Dataset mpps = (Dataset) notif.getUserData();
        // if N_CREATE
        if (mpps.contains(Tags.ScheduledStepAttributesSeq)) return;
        if (!"COMPLETED".equals(mpps.getString(Tags.PPSStatus))) return;
        final String mppsiuid = mpps.getString(Tags.SOPInstanceUID);
       	addWorklistItem(makeGPWLItem(getMPPS(mppsiuid)));
    }
	
	public void addWorklistItem( Integer studyPk, String templateFile, String humanPerformerCode, Long scheduleDate ) throws Exception {
		if ( log.isDebugEnabled() ) log.debug( "load template file: "+ (templateURL+"/"+templateFile) );
		Dataset ds = DatasetUtils.fromXML(new InputSource( templateURL+"/"+templateFile ) );
		
		ContentManager cm = getContentManager();
		//patient 
		Dataset patDS = cm.getPatientForStudy( studyPk.intValue() );
		if ( log.isDebugEnabled() ) {
			log.debug("Patient Dataset:"); log.debug(patDS);
		}
		
		ds.putAll(patDS.subSet(PAT_ATTR_TAGS));
		//
		Dataset sopInstRef = cm.getSOPInstanceRefMacro( studyPk.intValue(), false );
		String studyIUID = sopInstRef.getString( Tags.StudyInstanceUID );
		ds.putUI(Tags.SOPInstanceUID, UIDGenerator.getInstance().createUID());
		ds.putUI(Tags.StudyInstanceUID, studyIUID);
		DcmElement inSq = ds.putSQ(Tags.InputInformationSeq);
		inSq.addItem( sopInstRef );
		
		//Scheduled Human Performer Seq
		DcmElement schedHPSq = ds.putSQ(Tags.ScheduledHumanPerformersSeq);
		Dataset item = schedHPSq.addNewItem();
		DcmElement hpCodeSq = item.putSQ(Tags.HumanPerformerCodeSeq);
		Dataset dsCode = (Dataset) this.humanPerformer.get( humanPerformerCode );
		log.info(dsCode);
		if ( dsCode != null ) {
			hpCodeSq.addItem( dsCode );
			item.putPN( Tags.HumanPerformerName, dsCode.getString( Tags.CodeMeaning ) );
		}
		
		//Scheduled Procedure Step Start Date and Time
		ds.putDT( Tags.SPSStartDateAndTime, new Date( scheduleDate.longValue() ) );
		
		if (log.isDebugEnabled() ) {
			log.debug("GPSPS Dataset:"); log.debug(ds);
		}
		
		addWorklistItem( ds );
	}

	private void addWorklistItem(Dataset ds) {
		if (ds == null) return;
		try {
			GPWLManager mgr = getGPWLManagerHome().create();
			try {
				mgr.addWorklistItem(ds);
			} finally {
				try {
					mgr.remove();
				} catch (Exception ignore) {
				}
			}
		} catch (Exception e) {
			log.error("Failed to add Worklist Item:", e);
		}
	}

	private Dataset makeGPWLItem(Dataset mpps) {
        Dataset codeItem = mpps.getItem(Tags.ProcedureCodeSeq);
        StringBuffer sb = new StringBuffer("resource:gpwl/");
        final String codeValueDesignator = codeItem.getString(Tags.CodingSchemeDesignator);
        final String codeValue = codeItem.getString(Tags.CodeValue);
		sb.append(codeValue);
        sb.append('.');
		sb.append(codeValueDesignator);
        sb.append(".xml");
        for (int i = 14, n = sb.length(); i < n; ++i) {
        	char ch = sb.charAt(i);
        	if (!(ch >= '0' && ch <= '9'
        			|| ch >= 'A' && ch <= 'Z' || ch >= 'a' && ch <= 'z'
        			|| ch == ':' || ch == '-' || ch == '_' || ch == '.')) {
        		sb.setCharAt(i, '_');
        	}
        }
        try {
			Dataset gpsps = DatasetUtils.fromXML(new InputSource(sb.toString()));
			gpsps.putAll(mpps.subSet(PAT_ATTR_TAGS));
			gpsps.putUI(Tags.SOPClassUID, UIDs.GeneralPurposeScheduledProcedureStepSOPClass);
			final String iuid = UIDGenerator.getInstance().createUID();
			gpsps.putUI(Tags.SOPInstanceUID, iuid);
			DcmElement ssaSq = mpps.get(Tags.ScheduledStepAttributesSeq);
			String siuid = ssaSq.getItem().getString(Tags.StudyInstanceUID);
			gpsps.putUI(Tags.StudyInstanceUID, siuid);
			gpsps.putSH(Tags.SPSID, mpps.getString(Tags.PPSID));
			if (!gpsps.contains(Tags.SPSStartDateAndTime)) {
				gpsps.putDT(Tags.SPSStartDateAndTime, new Date());
			}
			DcmElement ppsSq = gpsps.putSQ(Tags.RefPPSSeq);
			Dataset refPPS = ppsSq.addNewItem();
			refPPS.putUI(Tags.RefSOPClassUID,
					mpps.getString(Tags.SOPClassUID));
			refPPS.putUI(Tags.RefSOPInstanceUID,
					mpps.getString(Tags.SOPInstanceUID));
			DcmElement perfSeriesSq = mpps.get(Tags.PerformedSeriesSeq);
			DcmElement inSq = gpsps.putSQ(Tags.InputInformationSeq);
			Dataset inputInfo = inSq.addNewItem();
			inputInfo.putUI(Tags.StudyInstanceUID, siuid);
			DcmElement inSeriesSq = inputInfo.putSQ(Tags.RefSeriesSeq);
			for (int i = 0, n = perfSeriesSq.vm(); i < n; ++i) {
				Dataset perfSeries = perfSeriesSq.getItem(i);
				Dataset inSeries = inSeriesSq.addNewItem();
				inSeries.putUI(Tags.SeriesInstanceUID,
						perfSeries.getString(Tags.SeriesInstanceUID));
				DcmElement inRefSopSq = inSeries.putSQ(Tags.RefSOPSeq);
				DcmElement refImgSopSq = perfSeries.get(Tags.RefImageSeq);
				for (int j = 0, m = refImgSopSq.vm(); j < m; ++j) {
					inRefSopSq.addItem(refImgSopSq.getItem(j));
				}
				DcmElement refNoImgSopSq = perfSeries.get(Tags.RefNonImageCompositeSOPInstanceSeq);
				for (int j = 0, m = refNoImgSopSq.vm(); j < m; ++j) {
					inRefSopSq.addItem(refNoImgSopSq.getItem(j));
				}
			}
			if (!gpsps.contains(Tags.RefRequestSeq)) {
				initRefRequestSeq(gpsps, ssaSq);
			}
	       	log.info("create workitem using template " + sb);
	       	log.debug(gpsps);
			return gpsps;
		} catch (FileNotFoundException e) {
			log.info("no workitem configured for procedure");
			log.info(codeItem);
		} catch (Exception e) {
			log.error("Failed to load workitem configuration from " + sb, e);
		}
		return null;        
	}

	private void initRefRequestSeq(Dataset gpsps, DcmElement ssaSq)
			throws Exception {
		MWLManager mwlManager = getMWLManagerHome().create();
		try {
			DcmObjectFactory dof = DcmObjectFactory.getInstance();
			DcmElement refRqSq = gpsps.putSQ(Tags.RefRequestSeq);
			for (int i = 0, n = ssaSq.vm(); i < n; ++i) {
				Dataset ssa = ssaSq.getItem(i);
				String spsid = ssa.getString(Tags.SPSID);
				if (spsid != null) {
					Dataset refRq = dof.newDataset();
					refRq.putAll(ssa.subSet(REF_RQ_TAGS_FROM_MPPS_SSA));
					try {
						Dataset mwlItem = mwlManager.getWorklistItem(spsid);
						if (mwlItem == null) {
							log.warn("No such MWL item[spsid=" + spsid
									+ "] -> use request info available in MPPS");													
						} else if (checkConsistency(mwlItem, ssa)) {
							refRq.putAll(mwlItem.subSet(REF_RQ_TAGS_FROM_MWL_ITEM));							
						}
					} catch (Exception e) {
						log.warn("Failed to access MWL item[spsid=" + spsid
								+ "] -> use request info available in MPPS", e);						
					}
					refRqSq.addItem(refRq);
				}
			}
		} finally {
			mwlManager.remove();
		}
	}

	private boolean checkConsistency(Dataset mwlItem, Dataset ssa) {
		boolean ok = true;
		DcmElement mwlAttr, ssaAttr;
		int tag;
		for (int i = 0; i < REF_RQ_TAGS_IN_MPPS_SSA_AND_MWL_ITEM.length; ++i) {
			tag = REF_RQ_TAGS_IN_MPPS_SSA_AND_MWL_ITEM[i];
			mwlAttr = mwlItem.get(tag);
			ssaAttr = ssa.get(tag);
			if (mwlAttr != null && ssaAttr != null && !mwlAttr.equals(ssaAttr)) {
				log.warn("MPPS SSA attribute: " + ssaAttr
						+ " does not match " + mwlAttr + " of referenced MWL Item[spsid="
						+ ssa.getString(Tags.SPSID));
				ok = false;
			}
		}
		return ok;
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

	private MPPSManagerHome getMPPSManagerHome() throws HomeFactoryException {
        return (MPPSManagerHome) EJBHomeFactory.getFactory().lookup(
                MPPSManagerHome.class, MPPSManagerHome.JNDI_NAME);
    }

    private MWLManagerHome getMWLManagerHome() throws HomeFactoryException {
        return (MWLManagerHome) EJBHomeFactory.getFactory().lookup(
                MWLManagerHome.class, MWLManagerHome.JNDI_NAME);
    }

	private GPWLManagerHome getGPWLManagerHome() throws HomeFactoryException {
        return (GPWLManagerHome) EJBHomeFactory.getFactory().lookup(
                GPWLManagerHome.class, GPWLManagerHome.JNDI_NAME);
    }
	
    private ContentManager getContentManager() throws Exception {
        ContentManagerHome home = (ContentManagerHome) EJBHomeFactory.getFactory()
                .lookup(ContentManagerHome.class, ContentManagerHome.JNDI_NAME);
        return home.create();
    }
	
}
