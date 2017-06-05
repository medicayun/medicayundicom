/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.mbean;

import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import javax.ejb.CreateException;
import javax.management.ObjectName;

import org.apache.log4j.Logger;
import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import org.dcm4chex.archive.ejb.interfaces.ContentEdit;
import org.dcm4chex.archive.ejb.interfaces.ContentEditHome;
import org.dcm4chex.archive.ejb.interfaces.ContentManager;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.dcm4chex.archive.util.HomeFactoryException;
import org.jboss.system.ServiceMBeanSupport;

/**
 * @author franz.willer@tiani.com
 * @version $Revision: 1831 $ $Date: 2005-06-30 22:59:13 +0800 (周四, 30 6月 2005) $
 * @since 17.02.2005
 */
public class ContentEditService extends ServiceMBeanSupport {

    private static final String DATETIME_FORMAT = "yyyyMMddHHmmss";

	public static final String EVENT_TYPE = "org.dcm4chex.archive.mbean.ContentEditService";

    private ContentEdit ce;
	private ContentEdit contentEdit;
    private static Logger log = Logger.getLogger( ContentEditService.class.getName() );

    private ObjectName hl7SendServiceName;
	private String sendingApplication;
	private String sendingFacility;
	private String receivingApplication;
	private String receivingFacility;

	private ObjectName studyMgtScuServiceName;
	private String callingAET;
	private String calledAET;
	
	private ContentManager contentMgr;
	
	private static long msgCtrlid = System.currentTimeMillis();

	public ContentEditService() {
    }
    
    protected void startService() throws Exception {
    }

    protected void stopService() throws Exception {
    }
    
    public String getEjbProviderURL() {
        return EJBHomeFactory.getEjbProviderURL();
    }

    public void setEjbProviderURL(String ejbProviderURL) {
        EJBHomeFactory.setEjbProviderURL(ejbProviderURL);
    }

    public final ObjectName getHL7SendServiceName() {
        return hl7SendServiceName;
    }

    public final void setHL7SendServiceName(ObjectName name) {
        this.hl7SendServiceName = name;
    }
    
    public final ObjectName getStudyMgtScuServiceName() {
        return studyMgtScuServiceName;
    }

    public final void setStudyMgtScuServiceName(ObjectName name) {
        this.studyMgtScuServiceName = name;
    }
    
	/**
	 * @return Returns the receivingApplication.
	 */
	public String getReceivingApplication() {
		return receivingApplication;
	}
	/**
	 * @param receivingApplication The receivingApplication to set.
	 */
	public void setReceivingApplication(String receivingApplication) {
		this.receivingApplication = receivingApplication;
	}
	/**
	 * @return Returns the receivingFacility.
	 */
	public String getReceivingFacility() {
		return receivingFacility;
	}
	/**
	 * @param receivingFacility The receivingFacility to set.
	 */
	public void setReceivingFacility(String receivingFacility) {
		this.receivingFacility = receivingFacility;
	}
	/**
	 * @return Returns the sendingApplication.
	 */
	public String getSendingApplication() {
		return sendingApplication;
	}
	/**
	 * @param sendingApplication The sendingApplication to set.
	 */
	public void setSendingApplication(String sendingApplication) {
		this.sendingApplication = sendingApplication;
	}
	/**
	 * @return Returns the sendingFacility.
	 */
	public String getSendingFacility() {
		return sendingFacility;
	}
	/**
	 * @param sendingFacility The sendingFacility to set.
	 */
	public void setSendingFacility(String sendingFacility) {
		this.sendingFacility = sendingFacility;
	}
	/**
	 * @return Returns the calledAET.
	 */
	public String getCalledAET() {
		return calledAET;
	}
	/**
	 * @param calledAET The calledAET to set.
	 */
	public void setCalledAET(String calledAET) {
		this.calledAET = calledAET;
	}
	/**
	 * @return Returns the callingAET.
	 */
	public String getCallingAET() {
		return callingAET;
	}
	/**
	 * @param callingAET The callingAET to set.
	 */
	public void setCallingAET(String callingAET) {
		this.callingAET = callingAET;
	}
	
    public Dataset createPatient(Dataset ds) throws RemoteException, CreateException, HomeFactoryException {
    	if ( log.isDebugEnabled() ) log.debug("create Partient");
    	Dataset ds1 = lookupContentEdit().createPatient( ds );
    	sendHL7PatientXXX( ds, "ADT^A04" );//use update to create patient, msg type is 'Register a patient'
    	return ds1;
    }
    
    public void mergePatients(Integer patPk, int[] mergedPks) throws RemoteException, HomeFactoryException, CreateException {
    	if ( log.isDebugEnabled() ) log.debug("merge Partient");
    	Map map = lookupContentEdit().mergePatients( patPk.intValue(), mergedPks );
    	Collection col = (Collection)map.get("NOTIFICATION_DS");
    	Iterator iter = col.iterator();
    	sendHL7PatientMerge((Dataset) map.get("DOMINANT"), (Dataset[]) map.get("MERGED") );
    	
    }
    
    public Dataset createStudy(Dataset ds, Integer patPk) throws CreateException, RemoteException, HomeFactoryException {
    	if ( log.isDebugEnabled() ) log.debug("create study:");log.debug(ds);
    	Dataset ds1 = lookupContentEdit().createStudy( ds, patPk.intValue() );
    	sendStudyMgt( ds1.getString(Tags.StudyInstanceUID), Command.N_CREATE_RQ, 0, ds1);
    	return ds1;
    }
    
    public Dataset createSeries(Dataset ds, Integer studyPk) throws CreateException, RemoteException, HomeFactoryException {
    	if ( log.isDebugEnabled() ) log.debug("create Series");
    	Dataset ds1 =  lookupContentEdit().createSeries( ds, studyPk.intValue() );
    	if ( log.isDebugEnabled() ) {log.debug("create Series ds1:"); log.debug( ds1 );}
		sendStudyMgt( ds1.getString( Tags.StudyInstanceUID), Command.N_SET_RQ, 0, ds1);
    	return ds1;
    }
    
    public void updatePatient(Dataset ds) throws RemoteException, HomeFactoryException, CreateException {
    	if ( log.isDebugEnabled() ) log.debug("update Partient");
    	lookupContentEdit().updatePatient( ds );
    	sendHL7PatientXXX( ds, "ADT^A08" );
    }
    
    private void sendStudyMgt(String iuid, int commandField, int actionTypeID, Dataset dataset) {
    	String infoStr = "iuid"+iuid+" cmd:"+commandField+ " action:"+actionTypeID+" ds:";
		if (log.isDebugEnabled()) {log.debug("send StudyMgt command: "+infoStr);log.debug(dataset);}

		
		try {
            server.invoke(this.studyMgtScuServiceName,
                    "forward",
                    new Object[] { this.callingAET,
            					   this.calledAET,
								   iuid, 
								   new Integer(commandField), new Integer(actionTypeID),
								   dataset },
                    new String[] { String.class.getName(),
            					   String.class.getName(),
            					   String.class.getName(), 
								   int.class.getName(), int.class.getName(),
								   Dataset.class.getName() });
        } catch (Exception e) {
            log.error("Failed to send StudyMgt command:"+infoStr, e);
        }
    }
    
    /**
	 * @param ds
	 */
	private void sendHL7PatientXXX(Dataset ds,String msgType) {
		String timeStamp = new SimpleDateFormat(DATETIME_FORMAT).format( new Date() );
		StringBuffer sb = getMSH(msgType);//get MSH for patient information update (ADT^A08)
		addEVN(sb);
		addPID( sb, ds );
		sb.append("\r");
		sb.append("PV1||||||||||||||||||||||||||||||||||||||||||||||||||||");//PatientClass(2),VisitNr(19) and VisitIndicator(51) ???
		sendHL7Msg( sb.toString() );
	}

	private void sendHL7PatientMerge(Dataset dsDominant, Dataset[] priorPats) {
		StringBuffer sb = getMSH("ADT^A40");//get MSH for patient merge (ADT^A40)
		addEVN(sb);
		addPID( sb, dsDominant );
		int SBlen = sb.length();
		for ( int i = 0, len = priorPats.length ; i < len ; i++ ) {
			sb.setLength( SBlen );
			addMRG( sb, priorPats[i] );
			sendHL7Msg( sb.toString() );
		}
		
	}
	
	private void sendHL7Msg( String msg ) {
		log.debug("send HL7 message:"+msg);
        try {
            server.invoke(this.hl7SendServiceName,
                    "forward",
                    new Object[] { msg.getBytes("ISO-8859-1") },
                    new String[] { byte[].class.getName() });
        } catch (Exception e) {
            log.error("Failed to send HL7 message:"+msg, e);
        }
		
	}
	
	/**
	 * @return
	 */
	private StringBuffer getMSH(String msgType) {
		StringBuffer sb = new StringBuffer();
		sb.append("MSH|^~\\&|");
		sb.append( getSendingApplication() ).append("|").append( getSendingFacility() ).append("|");
		sb.append( getReceivingApplication() ).append("|").append( getReceivingFacility() ).append("|");
		sb.append( new SimpleDateFormat(DATETIME_FORMAT).format( new Date() ) ).append("||").append(msgType).append("|");
		sb.append( getMsgCtrlId() ).append("|P|2.3.1||||||||");
		return sb;
	}

	private void addEVN( StringBuffer sb) {
		String timeStamp = new SimpleDateFormat(DATETIME_FORMAT).format( new Date() );
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
		if ( name != null ) sb.append("patName");
	}
	
	/**
	 * should this method on a central hl7 sending place? 
	 * @return
	 */
	private long getMsgCtrlId() {
		return msgCtrlid++;
	}

	public void updateStudy(Dataset ds) throws RemoteException, HomeFactoryException, CreateException {
    	if ( log.isDebugEnabled() ) log.debug("update Study");
    	Dataset dsN = lookupContentEdit().updateStudy( ds );
		sendStudyMgt( ds.getString( Tags.StudyInstanceUID), Command.N_SET_RQ, 0, ds);
    }
    
    public void updateSeries(Dataset ds) throws RemoteException, HomeFactoryException, CreateException {
    	if ( log.isDebugEnabled() ) log.debug("update Series");
    	Dataset dsN = lookupContentEdit().updateSeries( ds );
    	if ( log.isDebugEnabled() ) {log.debug("update series: dsN:");log.debug(dsN);}
		sendStudyMgt( dsN.getString( Tags.StudyInstanceUID), Command.N_SET_RQ, 0, dsN);
    }
    
    public void deleteSeries(Integer seriesPk) throws RemoteException, HomeFactoryException, CreateException {
    	if ( log.isDebugEnabled() ) log.debug("delete Series");
    	Dataset ds = lookupContentEdit().deleteSeries( seriesPk.intValue() );
    	if ( log.isDebugEnabled() ) {log.debug("delete series: ds:");log.debug(ds); }
		sendStudyMgt( ds.getString( Tags.StudyInstanceUID), Command.N_ACTION_RQ, 1, ds);
    }
    
    public void deleteStudy(Integer studyPk) throws RemoteException, HomeFactoryException, CreateException {
    	if ( log.isDebugEnabled() ) log.debug("delete Study");
    	Dataset ds = lookupContentEdit().deleteStudy( studyPk.intValue() );
    	if ( log.isDebugEnabled() ) {log.debug("delete study: ds:");log.debug(ds); }
		sendStudyMgt( ds.getString( Tags.StudyInstanceUID), Command.N_DELETE_RQ, 0, ds);
   }
    
    public void deletePatient(Integer patPk) throws RemoteException, HomeFactoryException, CreateException {
    	if ( log.isDebugEnabled() ) log.debug("delete Patient");
    	Dataset ds = lookupContentEdit().deletePatient( patPk.intValue() );
    	sendHL7PatientXXX( ds, "ADT^A23" );//Send Patient delete message
    }    
    
    public void deleteInstance(Integer pk) throws RemoteException, HomeFactoryException, CreateException {
    	Dataset ds = lookupContentEdit().deleteInstance( pk.intValue() );
    	if ( log.isDebugEnabled() ) {log.debug("delete instance: ds:");log.debug(ds); }
		sendStudyMgt( ds.getString( Tags.StudyInstanceUID), Command.N_ACTION_RQ, 2, ds);
    }
    
    public void moveStudies(int[] study_pks, Integer patient_pk) throws RemoteException, HomeFactoryException, CreateException {
    	if ( log.isDebugEnabled() ) log.debug("move Studies");
    	Collection col = lookupContentEdit().moveStudies( study_pks, patient_pk.intValue() );
    	Iterator iter = col.iterator();
    	Dataset ds;
    	while( iter.hasNext() ) {
    		ds = (Dataset) iter.next();
    		sendStudyMgt( ds.getString( Tags.StudyInstanceUID), Command.N_SET_RQ, 0, ds);
    	}
    }   
    
    public void moveSeries(int[] series_pks, Integer study_pk) throws RemoteException, HomeFactoryException, CreateException {
    	if ( log.isDebugEnabled() ) log.debug("move Series");
    	Dataset ds = lookupContentEdit().moveSeries( series_pks, study_pk.intValue() );
		sendStudyMgt( ds.getString( Tags.StudyInstanceUID), Command.N_SET_RQ, 0, ds);
    }
    
    public void moveInstances(int[] instance_pks, Integer series_pk) throws RemoteException, HomeFactoryException, CreateException {
    	if ( log.isDebugEnabled() ) log.debug("move Instances");
    	Dataset ds = lookupContentEdit().moveInstances(instance_pks, series_pk.intValue() );
		sendStudyMgt( ds.getString( Tags.StudyInstanceUID), Command.N_SET_RQ, 0, ds);
    }
    
   

    private ContentEdit lookupContentEdit() throws HomeFactoryException, RemoteException, CreateException  {
    	if ( contentEdit != null ) return contentEdit;
        ContentEditHome home = (ContentEditHome) EJBHomeFactory.getFactory()
                .lookup(ContentEditHome.class, ContentEditHome.JNDI_NAME);
        contentEdit = home.create();
        return contentEdit;
    }

 
}
