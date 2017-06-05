/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.dcm.mcmscu;

import java.io.IOException;
import java.net.Socket;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.ejb.CreateException;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.management.Notification;
import javax.management.NotificationListener;

import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Status;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.AAssociateAC;
import org.dcm4che.net.AAssociateRQ;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.Association;
import org.dcm4che.net.AssociationFactory;
import org.dcm4che.net.Dimse;
import org.dcm4che.net.FutureRSP;
import org.dcm4che.net.PDU;
import org.dcm4chex.archive.config.RetryIntervalls;
import org.dcm4chex.archive.ejb.interfaces.MediaComposer;
import org.dcm4chex.archive.ejb.interfaces.MediaComposerHome;
import org.dcm4chex.archive.ejb.interfaces.MediaDTO;
import org.dcm4chex.archive.ejb.jdbc.AECmd;
import org.dcm4chex.archive.ejb.jdbc.AEData;
import org.dcm4chex.archive.mbean.TimerSupport;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.dcm4chex.archive.util.FileUtils;
import org.dcm4chex.archive.util.HomeFactoryException;
import org.dcm4chex.archive.util.JMSDelegate;
import org.jboss.system.ServiceMBeanSupport;

/**
 * @author franz.willer
 *
 * MBean to configure and service media creation managment issues.
 * <p>
 * 1) Schedule media for offline storage.<br>
 * 2) Listen to an JMS queue for receiving media creation request from scheduler or WEB interface.<br>
 * 3) process a media creation request. (move instances and send media creation request and action to media creation managment AET)
 * 
 */
public class MCMScuService extends TimerSupport implements MessageListener {
	
    private static final long MIN_MAX_MEDIA_USAGE = 20 * FileUtils.MEGA;

	/** Name of the JMS queue to receive media creation requests from scheduler or WEB interface. */ 
    private static final String QUEUE = "MCMScu";

    /** Action command for media creation. */
    private static final int INITIATE_MEDIA_CREATION = 1;

    /** Action command for cancel media creation. */
    private static final int CANCEL_MEDIA_CREATION = 2;

    /** Holds the max. number of bytes that can be used to collect instances for a singe media. */
	private long maxMediaUsage = 680 * FileUtils.MEGA;
	
	/** Holds the min age of instances in ms for collecting. */ 
	private long minStudyAge;

    /** Holds the max age of a media for status COLLECTING. */
	private long maxStudyAge;

    private long scheduleMediaInterval;

    private long updateMediaStatusInterval;

    private long burnMediaInterval;
    
    private Integer scheduleMediaListenerID;
    
    private Integer updateMediaStatusListenerID;
    
    private Integer burnMediaListenerID;


    /** Holds the prefix that is used to generate the fileset id. */
	private String fileSetIdPrefix;
	
	/** Holds the calling AET. */
	private String callingAET;

	/** Holds the retrieve AET. (AET that perform the move). */
	private String retrieveAET;

	/** Holds the destination AET (AET that receive the instances for media creation). */
	private String destAET;

	/** Holds the media creation managment AET. (AET perform media creation) */
	private String mcMScpAET;
	
	/** Holds Association timeout in ms. */
	private int acTimeout;
	
	/** Holds DICOM message timeout in ms. */
	private int dimseTimeout;
	
	/** Holds max PDU length in bytes. */
	private int maxPDUlen = 16352;
	
	/** Holds socket close delay in ms. */
	private int soCloseDelay;
	
	/** Flag if media creation should use information extracted from instance. */
	private boolean useInstanceInfo = false;
	
	/** Holds type of none DICOM objects that should be stored on media. Default is NONE */
	private String includeNonDICOMObj = "NO";
	
	/** DICOM priority. Used for move and media creation action. */
	private int priority = 0;
	/** Array containing String values of priorities. (N-Action need text values). */
	private static final String[] prioStrings = new String[]{"MED","HIGH","LOW"};

	private static final AssociationFactory aFact = AssociationFactory.getInstance();
    private static final DcmObjectFactory oFact = DcmObjectFactory.getInstance();
    
	private MediaComposer mediaComposer;

    private final NotificationListener scheduleMediaListener = 
        new NotificationListener(){
            public void handleNotification(Notification notif, Object handback) {
                scheduleMedia();
            }};
    
    private final NotificationListener updateMediaStatusListener = 
        new NotificationListener(){
            public void handleNotification(Notification notif, Object handback) {
                updateMediaStatus();
            }};
    
    private final NotificationListener burnMediaListener = 
        new NotificationListener(){
            public void handleNotification(Notification notif, Object handback) {
                burnMedia();
            }};
            
	/**
	 * Returns the prefix for FileSetID creation.
	 * 
	 * @return Returns the fileSetIdPrefix.
	 */
	public String getFileSetIdPrefix() {
		return fileSetIdPrefix;
	}
	/**
	 * Set the prefix for FileSetID creation.
	 * 
	 * @param fileSetIdPrefix The fileSetIdPrefix to set.
	 */
	public void setFileSetIdPrefix(String fileSetIdPrefix) {
		this.fileSetIdPrefix = fileSetIdPrefix.toUpperCase();
	}
	/**
	 * Returns the max media usage for collecting studies.
	 * <p>
	 * The number of bytes, that can be used to collect studies for a media.<br>
	 * This values is usually smaller than the real media size to save space for index and 
	 * optional html files.
	 * 
	 * @return Returns the maxMediaUsage in bytes.
	 */
	public String getMaxMediaUsage() {
		return FileUtils.formatSize(maxMediaUsage);
	}
	/**
	 * Set the max media usage for collecting studies.
	 * 
	 * @param maxMediaUsage The maxMediaUsage to set.
	 */
	public void setMaxMediaUsage(String str) {
		this.maxMediaUsage = FileUtils.parseSize(str, MIN_MAX_MEDIA_USAGE);
	}

	/**
	 * This value is used to get the search date from current date.
	 * <p>
	 * Instances must be older than the search date.  
	 * 
	 * @return Returns ##w (in weeks), ##d (in days), ##h (in hours).
	 */
	public String getMinStudyAge() {
		return RetryIntervalls.formatInterval(minStudyAge);
	}
	
	/**
	 * Setter for minStudyAge. 
	 * <p>
	 * This value is used to ensure that all instances of a study is stored on a single media.
	 * 
	 * @param age ##w (in weeks), ##d (in days), ##h (in hours).
	 */
	public void setMinStudyAge(String age) {
		this.minStudyAge = RetryIntervalls.parseInterval(age);
	}
	
    /**
     * Returns the maxMediaAge before burned on media.
     * 
     * @return ##w (in weeks), ##d (in days), ##h (in hours).
     */
    public String getMaxStudyAge() {
        return RetryIntervalls.formatInterval(maxStudyAge);
    }
    
    /**
     * Sets the max study age in days.
     * <p>
     * This value is used to determine how long an instance may not be stored offline.
     *  
     * @param maxStudyAge The maxStudyAge to set.
     */
    public void setMaxStudyAge(String age) {
        this.maxStudyAge = RetryIntervalls.parseInterval(age);
    }
    
    
    public final String getBurnMediaInterval() {
        return RetryIntervalls.formatIntervalZeroAsNever(burnMediaInterval);
    }
    
    public void setBurnMediaInterval(String interval) {
        this.burnMediaInterval = RetryIntervalls.parseIntervalOrNever(interval);
        if (getState() == STARTED) {
            stopScheduler(burnMediaListenerID, burnMediaListener);
            burnMediaListenerID = startScheduler(burnMediaInterval,
                    burnMediaListener);
        }
    }
    
    public final String getScheduleMediaInterval() {
        return RetryIntervalls.formatIntervalZeroAsNever(scheduleMediaInterval);
    }
    
    public void setScheduleMediaInterval(String interval) {
        this.scheduleMediaInterval = RetryIntervalls.parseIntervalOrNever(interval);
        if (getState() == STARTED) {
            stopScheduler(scheduleMediaListenerID, scheduleMediaListener);
            scheduleMediaListenerID = startScheduler(scheduleMediaInterval,
                    scheduleMediaListener);
        }
    }
    
    public final String getUpdateMediaStatusInterval() {
        return RetryIntervalls.formatIntervalZeroAsNever(updateMediaStatusInterval);
    }
    
    public void setUpdateMediaStatusInterval(String interval) {
        this.updateMediaStatusInterval = RetryIntervalls.parseIntervalOrNever(interval);
        if (getState() == STARTED) {
            stopScheduler(updateMediaStatusListenerID, updateMediaStatusListener);
            updateMediaStatusListenerID = startScheduler(updateMediaStatusInterval, updateMediaStatusListener);
        }
    }
    
	/**
	 * Returns the calling AET defined in this MBean.
	 * 
	 * @return The calling AET.
	 */
	public String getCallingAET() {
		return callingAET;
	}
	
	/**
	 * Set the calling AET.
	 * 
	 * @param aet The calling AET to set.
	 */
	public void setCallingAET( String aet ) {
		callingAET = aet;
	}
	
	/**
	 * Returns the retrieve AET defined in this MBean.
	 * <p>
	 * This AET performs the move operation.
	 * 
	 * @return The retrieve AET.
	 */
	public String getRetrieveAET() {
		return retrieveAET;
	}
	
	/**
	 * Set the retrieve AET.
	 * 
	 * @param aet The retrieve AET to set.
	 */
	public void setRetrieveAET( String aet ) {
		retrieveAET = aet;
	}
	
	/**
	 * Return the move destination AET.
	 * <p>
	 * This AET is the destination of the move operation.
	 * 
	 * @return the destination AET.
	 */
	public String getMoveDestinationAET() {
		return destAET;
	}
	
	/**
	 * Set the move destination AET.
	 * <p>
	 * This AET must be on the same system as <code>mcMScpAET</code>.
	 * 
	 * @param aet AET to set.
	 */
	public void setMoveDestinationAET( String aet ) {
		destAET = aet;
	}
	
	/**
	 * Returns the Media Creation Managment AET.
	 * <p>
	 * This AET performs the media creation.
	 * 
	 * @return The MCM AET.
	 */
	public String getMcmScpAET() {
		return mcMScpAET;
	}
	
	/**
	 * Set the Media Creation managment AET.
	 * <p>
	 * Thsi AET must be on the same system as <code>destAET</code>
	 * @param aet AET to set.
	 */
	public void setMcmScpAET( String aet ) {
		mcMScpAET = aet;
	}
	
	/**
	 * Returns the Association timeout in ms.
	 * 
	 * @return Returns the acTimeout.
	 */
	public int getAcTimeout() {
		return acTimeout;
	}
	/**
	 * Set the association timeout.
	 * 
	 * @param acTimeout The acTimeout in ms.
	 */
	public void setAcTimeout(int acTimeout) {
		this.acTimeout = acTimeout;
	}
	
	/**
	 * Returns the DICOM message timeout in ms.
	 * 
	 * @return Returns the dimseTimeout.
	 */
	public int getDimseTimeout() {
		return dimseTimeout;
	}
	
	/**
	 * Set the DICOM message timeout.
	 * 
	 * @param dimseTimeout The dimseTimeout in ms.
	 */
	public void setDimseTimeout(int dimseTimeout) {
		this.dimseTimeout = dimseTimeout;
	}
	
	/**
	 * Returns the socket close delay in ms.
	 * 
	 * @return Returns the soCloseDelay.
	 */
	public int getSoCloseDelay() {
		return soCloseDelay;
	}
	
	/**
	 * Set the socket close delay.
	 * 
	 * @param delay Socket close delay in ms.
	 */
	public void setSoCloseDelay( int delay ) {
		soCloseDelay = delay;
	}

	/**
	 * Returns the DICOM priority as int value.
	 * <p>
	 * This value is used for Move and media creation action (N-Action).
	 * 0..MED, 1..HIGH, 2..LOW
	 * 
	 * @return Returns the priority.
	 */
	public int getPriority() {
		return priority;
	}
	
	/**
	 * Set the DICOM priority.
	 * 
	 * @param priority The priority to set.
	 */
	public void setPriority(int priority) {
		if ( priority < 0 || priority > 2 ) priority = 0;
		this.priority = priority;
	}
	
	/**
	 * returns the max PDU length in bytes.
	 * 
	 * @return Returns the maxPDUlen.
	 */
	public int getMaxPDUlen() {
		return maxPDUlen;
	}
	
	/**
	 * Set the max PDU length.
	 * 
	 * @param maxPDUlen The maxPDUlen in bytes.
	 */
	public void setMaxPDUlen(int maxPDUlen) {
		this.maxPDUlen = maxPDUlen;
	}
	
	/**
	 * Returns 'NO' if the media should not contain none DICOM objects or the named type of none DICOM object.
	 * 
	 * @return The type of none DICOM Objects to include or NO.
	 */
	public String getIncludeNonDICOMObj() {
		return includeNonDICOMObj;
	}
	/**
	 * Set the type of none DICOM object that should be included by media creation.
	 * <p>
	 * Use NO if no such objects should be included.
	 * <p>
	 * Set the value to NO if argument is null!
	 * 
	 * @param includeNonDICOMObj The flag value to set.
	 */
	public void setIncludeNonDICOMObj(String includeNonDICOMObj) {
		this.includeNonDICOMObj = includeNonDICOMObj;
	}
	
	/**
	 * Returns true if media label should contain information extracted from instances.
	 * 
	 * @return Returns the useInstanceInfo flag.
	 */
	public boolean isUseInstanceInfo() {
		return useInstanceInfo;
	}
	
	/**
	 * Set the useInstanceInfo flag.
	 * <p>
	 * true..Media label should contain info extracted from instances.
	 * 
	 * @param useInstanceInfo The flag value to set.
	 */
	public void setUseInstanceInfo(boolean useInstanceInfo) {
		this.useInstanceInfo = useInstanceInfo;
	}
	

	/**
	 * Collect studies to media for media creation. (offline storage)
	 * <p>
	 * Search for instances that are older than <code>daysBefore</code> and are not already assigned to a media.<br>
	 * Collect this instances to studies to ensure that all instances of a study are on one media.<br>
	 * Assign instances to media for best media usage using maxMediaUsage as limit.
	 * 
	 * @return Number of instances
	 */
	public int scheduleMedia() {
        log.info("Check for studies for scheduling on media");
		MediaComposer mc = null;
		try {
			mc = this.lookupMediaComposer();
		} catch ( Exception x ) {
			log.error("Can not create MediaComposer!",x);
			return -1;
		}
		
		try {
			int size = mc.collectStudiesReceivedBefore( getSearchDate(), maxMediaUsage, getFileSetIdPrefix() );
			return size;
		} catch ( Exception x ) {
			log.error("Can not collect studies!",x);
			return -2;
		}
		
	}
	
	/**
	 * Returns the search date as long for finding instances to collect.
	 * <p>
	 * This value is current time minus <code>daysBefore</code> in ms.
	 * 
	 * @return The search date
	 */
	private long getSearchDate() {
		return System.currentTimeMillis() - minStudyAge;
	}
	
	/**
	 * Start listening to the JMS queue deined in <code>QUEUE</code>
	 * <p>
	 * This queue is used to receive media creation request from scheduler or web interface.
	 */
    protected void startService() throws Exception {
        super.startService();
        scheduleMediaListenerID = startScheduler(scheduleMediaInterval, scheduleMediaListener);
        updateMediaStatusListenerID = startScheduler(updateMediaStatusInterval, updateMediaStatusListener);
        burnMediaListenerID = startScheduler(burnMediaInterval, burnMediaListener);
        JMSDelegate.startListening(QUEUE, this);
    }

	/**
	 * Stop listening to the JMS queue deined in <code>QUEUE</code>
	 * 
	 */
    protected void stopService() throws Exception {
        stopScheduler(scheduleMediaListenerID, scheduleMediaListener);
        stopScheduler(updateMediaStatusListenerID, updateMediaStatusListener);
        stopScheduler(burnMediaListenerID, burnMediaListener);
        JMSDelegate.stopListening(QUEUE);
        super.stopService();
    }

    /**
     * Handles a JMS message.
     * 
     * @param message The JMS message to handle.
     */
    public void onMessage(Message message) {
        ObjectMessage om = (ObjectMessage) message;
        MediaDTO mediaDTO = null;
        try {
            mediaDTO = (MediaDTO) om.getObject();
            log.info("Start processing " + mediaDTO);
            process(mediaDTO);
        } catch (JMSException e) {
            handleError( mediaDTO, "jms error during processing message: " + message, e);
        } catch (Throwable e) {
            handleError(mediaDTO, "unexpected error during processing message: " + message,
                    e);
        }
    }

    /**
     * Process a media creation request received from JMS queue.
     * <p>
     * <DL>
     * <DD>1) Set media status to PROCESSING.</DD>
     * <DD>2) Move instances to <code>destAET</code> using <code>retrieveAET</code>.</DD>
     * <DD>3) Send media creation request to <code>mcMScpAET</code>. </DD>
     * <DD>4) Send N-Action command for media creation to <code>mcMScpAET</code>. </DD>
     * </DL>
     * 
     * @param mediaDTO The MediaDTO object for the media to create.
     * 
     * @throws RemoteException
     * @throws FinderException
     * @throws HomeFactoryException
     * @throws CreateException
     */
    private void process(MediaDTO mediaDTO) throws RemoteException, FinderException, HomeFactoryException, CreateException {
    	this.lookupMediaComposer().setMediaCreationRequestIuid( mediaDTO.getPk(), null );
    	this.lookupMediaComposer().setMediaStatus( mediaDTO.getPk(), MediaDTO.TRANSFERING, "" );
        if ( processMove( mediaDTO ) ) {
        	log.info("Move instances of "+mediaDTO.getFilesetId()+" done!");
        	this.lookupMediaComposer().setMediaStatus( mediaDTO.getPk(), MediaDTO.BURNING, "" );
                	if ( processMediaCreation( mediaDTO ) ) {
            	log.info("Sending media creation request of "+mediaDTO.getFilesetId()+" done!");
            	return;
        	}
        }
        log.error("processing "+mediaDTO.getFilesetId()+" failed!!!");
    }
    
    /**
     * Process the move command on study level.
     * <p>
     * Move instances of all studies defined for given media to <code>destAET</code>.<br>
     * Use <code>retrieveAET</code> to perform the move command.
     * <p>
     * Moves all instances of the studies regardless if all instances assigned to this media.<br>
     * (The normal case is that all instances of a study is assigned to one media!)<br>
     * This has no effect for the creted media because the media creation request contains only instances assigned to this media!<br>
     * The unassigned instances are deleted after a timeout by the <code>mcMScpAET</code>.  
     * 
     * @param mediaDTO The MediaDTO to process.
     * 
     * @return true if move was sucessful, false if move failed.
     */
	private boolean processMove( MediaDTO mediaDTO ) {
    	ActiveAssociation assoc = null;
    	try {
    		AEData aeData = new AECmd( this.getRetrieveAET() ).execute();
			assoc = openAssoc( aeData.getHostName(), aeData.getPort(), getMoveAssocReq() );
			if ( assoc == null ) {
				handleError( mediaDTO, "processMove failed for "+mediaDTO.getFilesetId()+"! Reason: couldnt open association!", null);
				return false;
			}
			Association as = assoc.getAssociation();
			if (as.getAcceptedTransferSyntaxUID(1) == null) {
            	handleError( mediaDTO, "processMove failed for "+mediaDTO.getFilesetId()+" Reason: "+getRetrieveAET()+" doesnt support Study root move!", null );
				return false;
			}
			Command cmd = oFact.newCommand();
            String[] studyUIDs = getStudyUids( mediaDTO );
            Dataset ds = oFact.newDataset();

            ds.putCS(Tags.QueryRetrieveLevel, "STUDY");
            for ( int i = 0, len = studyUIDs.length ; i < len ; i++ ) {
                cmd.initCMoveRQ(as.nextMsgID(), UIDs.StudyRootQueryRetrieveInformationModelMOVE, 
        				getPriority() , this.getMoveDestinationAET() );
                ds.putUI(Tags.StudyInstanceUID, studyUIDs[i] );
	            Dimse moveRQ = aFact.newDimse(1, cmd, ds);
	            FutureRSP rsp = assoc.invoke(moveRQ);
	            Dimse dimse = rsp.get();
	            if ( ! checkResponse( dimse, mediaDTO, "for study:"+studyUIDs[i] ) ) {
	            	return false;
	            }
            }
		} catch (Exception e) {
			handleError( mediaDTO, "processMove failed for "+mediaDTO.getFilesetId()+"! Reason: unexpected error", e);
			return false;
		} finally {
			if ( assoc != null )
				try {
					assoc.release( true );
				} catch (Exception e1) {
					log.error( "Cant release association for move "+assoc.getAssociation(),e1);
				}
		}
    	return true;
    }
    
    /**
     * Process the media creation request.
     * <DL>
     * <DD>1) Send media creation request to <code>mcMScpAET</code>. </DD>
     * <DD>2) Send N-Action command for media creation to <code>mcMScpAET</code>. </DD>
     * </DL>
     * <p>
     * 
	 * @param mediaDTO The MediaDTO object to process.
	 */
	private boolean processMediaCreation(MediaDTO mediaDTO) {
    	ActiveAssociation assoc = null;
    	String iuid = null;
    	try {
//get association for media creation request and action.    		
    		AEData aeData = new AECmd( this.getMcmScpAET() ).execute();
			assoc = openAssoc( aeData.getHostName(), aeData.getPort(), getMediaCreationAssocReq() );
			if ( assoc == null ) {
				handleError( mediaDTO, "processMediaCreation failed for "+mediaDTO.getFilesetId()+"! Reason: couldnt open association!", null);
				return false;
			}
			Association as = assoc.getAssociation();
			if (as.getAcceptedTransferSyntaxUID(1) == null) {
            	handleError( mediaDTO, "processMediaCreation failed for "+mediaDTO.getFilesetId()+" Reason: "+getMcmScpAET()+" doesnt support media creation request!", null );
				return false;
			}
//send media creation request.			
			Command cmd = oFact.newCommand();
            cmd.initNCreateRQ(1, UIDs.MediaCreationManagementSOPClass, null );
            Dataset ds = getMediaCreationReqDS( mediaDTO );
            Dimse mcRQ = aFact.newDimse(1, cmd, ds);
            FutureRSP rsp = assoc.invoke(mcRQ);
            Dimse dimse = rsp.get();
            if ( ! checkResponse( dimse, mediaDTO, "" ) ) {
            	return false;
            }
            iuid = dimse.getCommand().getAffectedSOPInstanceUID();
            Command cmdRsp = dimse.getCommand();
            Dataset dataRsp = dimse.getDataset();
//send action
            FutureRSP futureRsp = assoc.invoke(aFact.newDimse(1, oFact.newCommand().initNActionRQ(3,
                            UIDs.MediaCreationManagementSOPClass, iuid, INITIATE_MEDIA_CREATION),
							getMediaCreationActionDS() ) );
            dimse = futureRsp.get();
            this.lookupMediaComposer().setMediaCreationRequestIuid( mediaDTO.getPk(), iuid );
            if ( ! checkResponse( dimse, mediaDTO, "" ) ) {
            	return false;
            }
            
		} catch (Exception e) {
			handleError( mediaDTO, "processMediaCreation failed for "+mediaDTO.getFilesetId()+"! Reason: unexpected error", e);
			return false;
		} finally {
			if ( assoc != null )
				try {
					assoc.release( true );
				} catch (Exception e1) {
					log.error( "Cant release association for media creation request"+assoc.getAssociation(),e1);
				}
		}
    	return true;
	}

	/**
	 * Get the initialized media creation request dataset.
	 * 
	 * @return The Dataset for media creation request.
	 * 
	 * @throws CreateException
	 * @throws HomeFactoryException
	 * @throws FinderException
	 * @throws RemoteException
	 */
	private Dataset getMediaCreationReqDS( MediaDTO mediaDTO ) throws RemoteException, FinderException, HomeFactoryException, CreateException {
		Dataset ds = lookupMediaComposer().prepareMediaCreationRequest( mediaDTO.getPk() );
		ds.putCS(Tags.LabelUsingInformationExtractedFromInstances, this.isUseInstanceInfo() ? "YES" : "NO");
		ds.putCS(Tags.IncludeNonDICOMObjects, includeNonDICOMObj);
		return ds;
	}

	/**
	 * Get the dataset for N-Action command.
	 * <p>
	 * Set the defined priority in dataset.
	 * 
	 * @return Dateset for N-Action command
	 */
	private Dataset getMediaCreationActionDS() {
		Dataset ds = oFact.newDataset();
		ds.putCS( Tags.RequestPriority, prioStrings[ priority ] );
		return ds;
	}
	
	/**
	 * Handles an error.
	 * <p>
	 * <DL>
	 * <DD>1) Log the error.</DD>
	 * <DD>2) Set the media status to an error status with status info.
	 * </DL>
	 * 
	 * @param mediaDTO 	The media effected.
	 * @param msg		The error message. (for logging and status info)
	 * @param x			A throwable object used for logging.
	 */
	private void handleError( MediaDTO mediaDTO, String msg, Throwable x ) {
		log.error( msg, x );
		if ( mediaDTO != null ) {
			try {
				this.lookupMediaComposer().setMediaStatus( mediaDTO.getPk(), MediaDTO.ERROR, msg );
			} catch (Exception e) {
				log.error("cant set error media status for "+mediaDTO.getFilesetId() );
			}
		}
		
	}
	
	/**
	 * Check a response DICOM message for return status.
	 * <p>
	 * Log a warning if status is <code>Status.AttributeValueOutOfRange</code>.
	 * <p>
	 * Call <code>handleError</code> with status info and error comment if message indicates a failure.
	 * <p>
	 * This method is generic for the usage in this class because the status values 
	 * of move, create and action doesnt conflict!
	 *  
	 * @param mediaDTO  The effected media
	 * @param dimse		The DICOM message to check.
	 * 
	 * @return true if status is OK, false if message indicates a failure.
	 */
	private boolean checkResponse(Dimse rsp, MediaDTO mediaDTO, String msg) {
        Command cmdRsp = rsp.getCommand();
        Dataset dataRsp = null;
		try {
			dataRsp = rsp.getDataset();
		} catch (IOException e) {
			log.error("Cant get Dataset from response message!", e );
		}
		int status = cmdRsp.getStatus();
        switch (status) {
        	case Status.AttributeValueOutOfRange:
        		log.warn("Warning: Attribute Value Out Of Range: "
                    + cmdRsp.getString(Tags.ErrorComment, "") + dataRsp);
        	case Status.Success:
        		return true;
        }
        log.error("Media creation failed! "+" ["+msg+"]"+"Failure Status " + Integer.toHexString(status) + ": "
                + cmdRsp.getString(Tags.ErrorComment, "") + dataRsp);
        handleError( mediaDTO, "Media creation failed! return status:"+Integer.toHexString(status)+" Reason:"+cmdRsp.getString(Tags.ErrorComment, "")+" ["+msg+"]", null );
        return false;
	}
	
	/**
	 * Return an array of study UIDs for given media.
	 * 
	 * @param mediaDTO The MediaDTO object.
	 * 
	 * @return String array with study UIDs.
	 *  
     * @throws HomeFactoryException
     * @throws CreateException
     * @throws FinderException
     * @throws RemoteException
	 */
	private String[] getStudyUids(MediaDTO mediaDTO) throws HomeFactoryException, RemoteException, FinderException, CreateException {
		Collection c =  this.lookupMediaComposer().getStudyUIDSForMedia(mediaDTO.getPk() );
		return (String[]) c.toArray( new String[ c.size() ] );
	}
	
	/**
	 * Open a DICOM association for given host, port and assocition request.
	 * 
	 * @param host		Host to create the association.
	 * @param port		Port number to create the association.
	 * @param assocRQ	The association request object.
	 * 
	 * @return	The Active association object.
	 * 
	 * @throws IOException
	 * @throws GeneralSecurityException
	 */
	private ActiveAssociation openAssoc( String host, int port, AAssociateRQ assocRQ ) throws IOException, GeneralSecurityException {
		Association assoc = aFact.newRequestor( new Socket( host, port ) );
		assoc.setAcTimeout(acTimeout);
		assoc.setDimseTimeout(dimseTimeout);
        assoc.setSoCloseDelay(soCloseDelay);
		PDU assocAC = assoc.connect(assocRQ);
		if (!(assocAC instanceof AAssociateAC)) { return null; }
		ActiveAssociation retval = aFact.newActiveAssociation(assoc, null);
		retval.start();
		return retval;
    }
  
	/**
	 * returns the association request object for move.
	 * 
	 * @return Association request for move.
	 */
    private AAssociateRQ getMoveAssocReq() {
    	AAssociateRQ moveAssocRQ = aFact.newAAssociateRQ();
        moveAssocRQ.setCalledAET( getRetrieveAET() );
        moveAssocRQ.setCallingAET( getCallingAET() );
        moveAssocRQ.setMaxPDULength( maxPDUlen );
        moveAssocRQ.addPresContext(aFact.newPresContext(1,
                UIDs.StudyRootQueryRetrieveInformationModelMOVE,
                new String[]{UIDs.forName("ExplicitVRLittleEndian"),
        					 UIDs.forName("ImplicitVRLittleEndian")} ));
    	return moveAssocRQ;
    }
    
    /**
     * Return the association request for media creation.
     * <p>
     * This association is used for sending media creation request and action command.
     * 
	 * @return Association for media creation.
	 */
	private AAssociateRQ getMediaCreationAssocReq() {
    	AAssociateRQ mcAssocRQ = aFact.newAAssociateRQ();
    	mcAssocRQ.setCalledAET( this.getMcmScpAET() );
    	mcAssocRQ.setCallingAET( getCallingAET() );
    	mcAssocRQ.setMaxPDULength( maxPDUlen );
    	mcAssocRQ.addPresContext(aFact.newPresContext(1,
                UIDs.MediaCreationManagementSOPClass,
                new String[]{UIDs.forName("ExplicitVRLittleEndian"),
        					 UIDs.forName("ImplicitVRLittleEndian")} ));
    	return mcAssocRQ;
	}
	
    /**
     * Return the association request for SecondaryCaptureImageStorage.
     * <p>
     * This association is used for testing availability of move destination.
     * 
	 * @return Association for SecondaryCaptureImageStorage.
	 */
	private AAssociateRQ getStore2ndCaptureAssocReq() {
    	AAssociateRQ mcAssocRQ = aFact.newAAssociateRQ();
    	mcAssocRQ.setCalledAET( this.getMoveDestinationAET() );
    	mcAssocRQ.setCallingAET( getCallingAET() );
    	mcAssocRQ.setMaxPDULength( maxPDUlen );
    	mcAssocRQ.addPresContext(aFact.newPresContext(1,
                UIDs.SecondaryCaptureImageStorage,
                new String[]{UIDs.forName("ExplicitVRLittleEndian"),
        					 UIDs.forName("ImplicitVRLittleEndian")} ));
    	return mcAssocRQ;
	}
	
	
    public String updateMediaStatus() {
    	ActiveAssociation assoc = null;
    	try {
	//get all media of status PROCESSING.
			List procList = this.lookupMediaComposer().getWithStatus( MediaDTO.BURNING );
            if (procList.isEmpty())
                return "No Media in processing status.";
			
//          get association for media creation request and action.          
            AEData aeData = new AECmd( this.getMcmScpAET() ).execute();
            if ( aeData == null ) {
                log.error("Cant get media creation status! Reason: Unknown AET:"+getMcmScpAET());
                return "Error: Unknown AET:"+getMcmScpAET();
            }
            assoc = openAssoc( aeData.getHostName(), aeData.getPort(), getMediaCreationAssocReq() );
            if ( assoc == null ) {
                log.error("Cant get media creation status! Reason: couldnt open association!" );
                return "Error: could not open association!";
            }
            Association as = assoc.getAssociation();
            if (as.getAcceptedTransferSyntaxUID(1) == null) {
                log.error( "Cant get media creation status! Reason: "+getMcmScpAET()+" doesnt support media creation managment!", null );
                return "Error: "+getMcmScpAET()+" doesnt support media creation managment!";
            }
			String iuid = null;
			MediaDTO mediaDTO = null;
			int[] getAttrs = new int[]{Tags.ExecutionStatus, Tags.ExecutionStatusInfo, Tags.FailedSOPSeq};
			int mediaStatus;
			int mediaWithAction = 0;
			int mediaDone = 0;
			int mediaFailed = 0;
			String statusString;
			for ( Iterator iter = procList.iterator() ; iter.hasNext() ; ) {
				mediaDTO = (MediaDTO) iter.next();
				iuid = mediaDTO.getMediaCreationRequestIuid();
				mediaStatus = mediaDTO.getMediaStatus();
				if ( iuid != null && iuid.length() > 0 ) {
					mediaWithAction++;
			        FutureRSP futureRsp = assoc.invoke(aFact.newDimse(1, oFact
			                .newCommand().initNGetRQ( as.nextMsgID(),
			                        UIDs.MediaCreationManagementSOPClass, iuid, getAttrs)));
			        Dimse rsp = futureRsp.get();
			        Command cmdRsp = rsp.getCommand();
			        Dataset dataRsp = rsp.getDataset();
			        int status = cmdRsp.getStatus();
			        if (status != 0) {
			            log.error("Cant get media creation status! Failure Status:" + Integer.toHexString(status) + ": "
			                    + cmdRsp.getString(Tags.ErrorComment, "")
			                    + (dataRsp == null ? "" : ("\n" + dataRsp)));
			        } else {
			            if (log.isDebugEnabled() ) log.debug("Received Attributes:\n" + dataRsp);
			            String execStatus = dataRsp.getString( Tags.ExecutionStatus );
			            if ( "DONE".equals( execStatus ) ) {
			            	mediaDone++;
			            	mediaComposer.setMediaStatus( mediaDTO.getPk(), MediaDTO.COMPLETED, "successfully completed");
			            	if ( log.isInfoEnabled() ) log.info("Media "+ mediaDTO.getFilesetId()+" successfully created!");
			            } else if ( "FAILURE".equals( execStatus ) ) {
			            	mediaFailed++;
			            	String info = dataRsp.getString( Tags.ExecutionStatusInfo );
			            	if ( "NO_INSTANCE".equals( info ) ) {
			            		info = info +"("+ dataRsp.vm( Tags.FailedSOPSeq )+ " number of instances missing)";
			            	}
			            	log.error("Cant create media "+mediaDTO.getFilesetId()+"! Reason:"+info);
			            	mediaComposer.setMediaStatus( mediaDTO.getPk(), MediaDTO.ERROR, info );
			            } else {
			            	mediaComposer.setMediaStatus( mediaDTO.getPk(), mediaStatus, execStatus);
			            }
			        }
				}//end if iuid
			}//end for
			return "Media creation status:"+mediaFailed+" media FAILED, "+mediaDone+" media done! Total: "+
					procList.size()+" media processing / " + mediaWithAction +" with N-ACTION";
    	} catch ( Exception x ) {
    		log.error("Cant get media creation status! Reason: unexpected error.", x);
    		return "Cant get media create status: Unexpected error"+x.getMessage();
		} finally {
			if ( assoc != null )
				try {
					assoc.release( true );
				} catch (Exception e1) {
					log.error( "Cant release association for media creation status request"+assoc.getAssociation(),e1);
				}
    	}
    }
    
    /**
     * Initiate creation of Media with studies older than MaxStudyAge.
     * 
     * @return Number of media creations initiated.
     */
    public int burnMedia() {
        log.info("Check for scheduled Media to burn");
        try {
        	int nrOfMedia = 0;
        	Collection c = lookupMediaComposer().getWithStatus( MediaDTO.OPEN );
        	long maxAgeDate = System.currentTimeMillis() - ( maxStudyAge - minStudyAge );//media is created after minStudyAge -> max media age is maxStudyAge-minStudyAge
        	MediaDTO mediaDTO;
        	for ( Iterator iter = c.iterator() ; iter.hasNext() ; ) {
        		mediaDTO = (MediaDTO) iter.next();
        		if ( mediaDTO.getCreatedTime().getTime() < maxAgeDate ) {
        			process( mediaDTO );
        			nrOfMedia++;
        		}
        	}
    		return nrOfMedia;
        } catch (Exception e) {
            log.error("Failed to initiate media creation:", e);
            return -1;
        }
    }
    
    /**
     * Checks the availability of Media Creation Managment SCP service.
     * <p>
     * Checks if the move destination is available and support SecondaryCaptureImageStorage.<br>
     * Checks if the MediaCreation managment service is availabel.
     * <p>
     * 
     * @return Returns OK, MOVE_DEST_UNAVAIL or MCM_SCP_UNAVAIL
     */
    public String checkMcmScpAvail() {
    	String ret = "MOVE_DEST_UNAVAIL";
    	ActiveAssociation assoc = null;
    	AEData aeData;
   	//check move dest.
    	try {
			aeData = new AECmd( this.getMoveDestinationAET() ).execute();
			assoc = openAssoc( aeData.getHostName(), aeData.getPort(), getStore2ndCaptureAssocReq() );
			if ( assoc == null ) {
				if ( log.isDebugEnabled() ) log.debug("Move destination ("+getMoveDestinationAET()+") is not available! Reason: couldnt open association:" );
			}
			Association as = assoc.getAssociation();
			if (as.getAcceptedTransferSyntaxUID(1) == null) {
	        	if ( log.isDebugEnabled() ) log.debug( "Move destination ("+getMoveDestinationAET()+") is not available! Reason: doesnt support media creation managment!", null );
			}
			ret = "OK";
    	} catch ( Exception x ) {
    		if ( log.isDebugEnabled() ) log.debug( "Move destination ("+getMoveDestinationAET()+") is not available! Reason: Exception:", x );
		} finally {
			if ( assoc != null )
				try {
					assoc.release( true );
				} catch (Exception e1) {
					if ( log.isDebugEnabled() ) log.debug( "Cant release association for checkMcmScpAvail: MOVE_DEST"+assoc.getAssociation(),e1);
				}
    	}
		if ( ! "OK".equals( ret ) ) return ret;
    	
 	//check mcm scp.
		ret = "MCM_SCP_UNAVAIL";
    	try {
			aeData = new AECmd( this.getMcmScpAET() ).execute();
			assoc = openAssoc( aeData.getHostName(), aeData.getPort(), getMediaCreationAssocReq() );
			if ( assoc == null ) {
				if ( log.isDebugEnabled() ) log.debug("MCM SCP ("+getMcmScpAET()+") is not available! Reason: couldnt open association:" );
			}
			Association as = assoc.getAssociation();
			if (as.getAcceptedTransferSyntaxUID(1) == null) {
				if ( log.isDebugEnabled() ) log.debug( "MCM SCP ("+getMcmScpAET()+") is not available! Reason: doesnt support media creation managment!", null );
			}
			ret = "OK";
    	} catch ( Exception x ) {
    		if ( log.isDebugEnabled() ) log.debug( "MCM SCP ("+getMcmScpAET()+") is not available! Reason: Exception:", x );
		} finally {
			if ( assoc != null )
				try {
					assoc.release( true );
				} catch (Exception e1) {
					if ( log.isDebugEnabled() ) log.debug( "Cant release association for checkMcmScpAvail: MCM_SCP"+assoc.getAssociation(),e1);
				}
    	}
    	
    	return ret;
    }
    
    public void deleteMedia( Integer mediaPk ) throws RemoteException, RemoveException, FinderException, HomeFactoryException, CreateException {
    	lookupMediaComposer().deleteMedia( mediaPk );
    }
	
    
	/**
	 * Returns the MediaComposer session bean.
	 * 
	 * @return The MediaComposer.
	 * 
	 * @throws HomeFactoryException
	 * @throws RemoteException
	 * @throws CreateException
	 */
    private MediaComposer lookupMediaComposer() throws HomeFactoryException, RemoteException, CreateException {
    	if ( mediaComposer == null ) {
    		MediaComposerHome home = (MediaComposerHome) EJBHomeFactory
	        .getFactory().lookup(MediaComposerHome.class,
	        		MediaComposerHome.JNDI_NAME);
    		mediaComposer = home.create();
    	}
    	return mediaComposer;
    }

}
