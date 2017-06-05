/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.ejb.session;

import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;
import org.dcm4che.util.UIDGenerator;
import org.dcm4chex.archive.ejb.interfaces.FileLocal;
import org.dcm4chex.archive.ejb.interfaces.InstanceLocal;
import org.dcm4chex.archive.ejb.interfaces.InstanceLocalHome;
import org.dcm4chex.archive.ejb.interfaces.MediaDTO;
import org.dcm4chex.archive.ejb.interfaces.MediaLocal;
import org.dcm4chex.archive.ejb.interfaces.MediaLocalHome;
import org.dcm4chex.archive.ejb.interfaces.SeriesLocal;
import org.dcm4chex.archive.ejb.interfaces.SeriesLocalHome;
import org.dcm4chex.archive.ejb.interfaces.StudyLocal;
import org.dcm4chex.archive.ejb.interfaces.StudyLocalHome;
import org.dcm4chex.archive.ejb.util.InstanceCollector;
import org.dcm4chex.archive.ejb.util.InstanceCollector.InstanceContainer;

/**
 * @ejb.bean name="MediaComposer"
 *           type="Stateless"
 *           view-type="remote"
 *           jndi-name="ejb/MediaComposer"
 * 
 * @ejb.transaction-type type="Container"
 * @ejb.transaction type="Required"
 * 
 * @ejb.ejb-ref ejb-name="Media" 
 *              view-type="local"
 *              ref-name="ejb/Media" 
 * 
 * @ejb.ejb-ref ejb-name="Instance" 
 *              view-type="local"
 *              ref-name="ejb/Instance" 
 *  
 * @ejb.ejb-ref ejb-name="Study" 
 *              view-type="local"
 *              ref-name="ejb/Study" 
 *
 * @ejb.ejb-ref ejb-name="Series" 
 *              view-type="local"
 *              ref-name="ejb/Series" 
 * 
 * @author gunter.zeilinger@tiani.com
 * @version Revision $Date: 2005-02-07 08:40:46 +0800 (周一, 07 2月 2005) $
 * @since 14.12.2004
 */

public abstract class MediaComposerBean implements SessionBean {

	private static Logger log = Logger.getLogger( MediaComposerBean.class.getName() );
	
    private MediaLocalHome mediaHome;

    private InstanceLocalHome instHome;
    
    private StudyLocalHome studyHome;

    private SeriesLocalHome seriesHome;

    /**
     * Initialize this class.
     * <p>
     * Set the home interfaces for MediaLocal and InstanceLocal.
     * 
     * @param	arg0 The session context. 
     */
    public void setSessionContext(SessionContext arg0) throws EJBException,
            RemoteException {
        Context jndiCtx = null;
        try {
            jndiCtx = new InitialContext();
            mediaHome = (MediaLocalHome) jndiCtx
                    .lookup("java:comp/env/ejb/Media");
            instHome = (InstanceLocalHome) jndiCtx
                    .lookup("java:comp/env/ejb/Instance");
            studyHome = (StudyLocalHome) jndiCtx.lookup("java:comp/env/ejb/Study");
            seriesHome = (SeriesLocalHome) jndiCtx.lookup("java:comp/env/ejb/Series");
        } catch (NamingException e) {
            throw new EJBException(e);
        } finally {
            if (jndiCtx != null) {
                try {
                    jndiCtx.close();
                } catch (NamingException ignore) {
                }
            }
        }
    }

    /**
     * Set the home interfaces to null.
     *
     */
    public void unsetSessionContext() {
        mediaHome = null;
        instHome = null;
    }
    
    /**
     * Collect studies to media for storage.
     * <p>
     * <DL>
     * <DD>1) Find all instances that are not assigned to a media and are older as <code>time</code></DD>
     * <DD>2) collect instances to studies.</DD>
     * <DD>3) collect studies for media</DD>
     * <DD>4) assign media to studies</DD>
     * </DL>
     * @param time 			Timestamp: instances must be received before this timestamp.
     * @param maxMediaUsage	The number of bytes that can be used to store instances on a media.
     * @param prefix		Prefix for the FileSet id. Used if a new media object is created.
     * 
     * @ejb.interface-method
     */
    public int collectStudiesReceivedBefore(long time, long maxMediaUsage, String prefix) throws FinderException {
        Collection c = instHome.findNotOnMediaAndStudyReceivedBefore(
                new Timestamp(time));
        if ( c.size() < 1 ) return 0;
        Iterator iter = c.iterator();
        InstanceLocal instance;
        InstanceCollector collector = new InstanceCollector();
        while ( iter.hasNext() ) {
        	instance = (InstanceLocal) iter.next();
        	collector.add( instance );
        }
        int nrOfStudies = collector.getNumberOfStudies();
        log.info( "Collected for storage: "+c.size()+" instances in "+nrOfStudies+" studies !");

        splitTooLargeStudies( collector, maxMediaUsage, prefix );
        
        List mediaCollection = (List) mediaHome.findByStatus( MediaDTO.OPEN );
        Comparator comp = new Comparator() {
    		public int compare(Object arg0, Object arg1) {
    			MediaLocal ml1 = (MediaLocal) arg0;
    			MediaLocal ml2 = (MediaLocal) arg1;
    			return (int) ( ml2.getMediaUsage() - ml1.getMediaUsage() );//more usage before lower usage!
    		}
    	};
        Collections.sort( mediaCollection, comp );
        log.info("Number of 'COLLECTING' media found:"+mediaCollection.size() );
        MediaLocal mediaLocal;
        List instancesForMedia;
        long maxSize, collSize;
        while ( nrOfStudies > 0 ) {
        	try {
				mediaLocal = getNextMediaLocal( mediaCollection, prefix );
			} catch (CreateException e) {
				log.error("Cant create MediaLocal! skip "+nrOfStudies+" for assigning media!");
				break;
			}
        	maxSize = maxMediaUsage - mediaLocal.getMediaUsage();
        	if ( log.isDebugEnabled() ) log.debug("Collect for media:"+mediaLocal.getFilesetId()+" free:"+maxSize+" - Number of studies avail:"+nrOfStudies+ " totalSize:"+collector.getTotalSize() );
        	instancesForMedia = new ArrayList();
        	collSize = collector.collectInstancesForSize( instancesForMedia, maxSize );
        	if ( collSize > 0L ){
        		maxSize -= collSize; 
	          	if ( log.isDebugEnabled() ) log.debug("Initial collected: "+collSize+" free:"+maxSize+" bytes; instancesForMedia:"+instancesForMedia);
	        	while ( collector.getSmallestStudy() != null && maxSize >= collector.getSmallestStudy().getStudySize() ) { 
	        		//collector contains one ore more studies that can be collected to current media
	        		collSize = collector.collectInstancesForSize( instancesForMedia, maxSize );
	        		if ( collSize > 0L ) {
	                	maxSize -= collSize;
	                	if ( log.isDebugEnabled() ) log.debug("Additional Collected: "+collSize );
	        		} else {
	        			break; 
	        		}
	        	} 
	        	if ( log.isDebugEnabled() ) log.debug("Final collected for media:"+mediaLocal.getFilesetId()+" free:"+maxSize+" bytes; instancesForMedia:"+instancesForMedia);
        	}
        	//media full -> assign media -> new media
        	this.assignMedia( instancesForMedia, mediaLocal );
        	
        	nrOfStudies = collector.getNumberOfStudies();
        }
        
        return c.size();
    }
    
    /**
     * Returns the next MediaLocal object from <code>col</code>.
     * <p>
     * If <code>col</code> is empty, a new MediaLocal is created.
     * <p>
     * In the other case the returning MediaLocal object is removed from the collection.
     *  
     * @param coll 	Collection with MediaLocal objects.
     * 
     * @return		The next MediaLocal object from co or a new created MediaLocal.
     * 
     * @throws CreateException if create of a new MediaLocal failed.
     */
    private MediaLocal getNextMediaLocal( Collection coll, String prefix ) throws CreateException {
    	MediaLocal ml = null;
    	if ( coll.size() > 0 ) {
    		ml = (MediaLocal) coll.iterator().next();
    		coll.remove( ml );
    	} else {
			ml = createMedia( prefix );
    	}
    	return ml;
    }
    
    /**
     * Checks the <code>collector</code> for studies with a size greater than <code>maxMediaUsage</code>.
     * <p>
     * If the collector contains such studies, they will be splitted and assigned to new created media objects.
     * 
     * @param collector		The InstanceCollector with instances collected to studies.
     * @param maxMediaUsage	Max number of bytes that can be used to store instances on a media.
     * @param prefix		Prefix for FileSet id.
     */
    private void splitTooLargeStudies(InstanceCollector collector, long maxMediaUsage, String prefix ){
        InstanceContainer largest = collector.getLargestStudy();
        while ( largest != null && largest.getStudySize() > maxMediaUsage ){
        	if ( log.isInfoEnabled() ) log.info( "Study (pk="+largest.getStudyPk()+") to large ("+largest.getStudySize()+") for a single medium ("+maxMediaUsage+") !");
        	List splitList = collector.split( largest, maxMediaUsage );
        	if ( log.isInfoEnabled() ) log.info( "Study (pk="+largest.getStudyPk()+") splitted to "+splitList.size()+" media!");
        	
        	try {
        		Iterator iter = splitList.iterator();
        		InstanceContainer ic;
        		MediaLocal ml;
        		InstanceLocal il;
        		while ( iter.hasNext() ) {
        			ic = (InstanceContainer) iter.next();
        			ml = createMedia( prefix );
    				assignMedia( ic, ml );
        		}
			} catch (Exception e) {
				log.error("Split study for storage: Cant create new MediaLocal! studyPk:"+largest.getStudyPk(), e );
			}
        	largest = collector.getLargestStudy();
        }
    }

    /**
     * Creates a new media.
     * <p>
     * Set the fileset id with given prefix and the pk of the new media.
     * 
	 * @param prefix A prefix for fileset id.
	 * 
	 * @return The new created MediaLocal bean.
     * @throws CreateException
	 */
	private MediaLocal createMedia(String prefix) throws CreateException {
		MediaLocal ml = mediaHome.create( UIDGenerator.getInstance().createUID() );
		ml.setFilesetId( prefix+ml.getPk() );
		ml.setMediaStatus( MediaDTO.OPEN );
		if ( log.isInfoEnabled() ) log.info("New media created:"+ml.getFilesetId() );
		return ml;
	}

	/**
     * Assign the given medialLocal to all instances of given InstanceContainer.
     * 
     * @param container		The InstanceContainer with a study for given media
     * @param mediaLocal	The media that will be assigned.
     */
    private void assignMedia( InstanceContainer container, MediaLocal mediaLocal ) {
		Iterator iterInstances = container.getInstances().iterator();
		InstanceLocal il;
		while ( iterInstances.hasNext() ) {
			il = (InstanceLocal) iterInstances.next();
        	if ( log.isDebugEnabled() ) log.debug( "Assign media "+mediaLocal.getFilesetId()+
        										" to instance pk="+il.getPk()+"("+il.getSopIuid()+
												") of study pk="+container.getStudyPk()+"!");
			il.setMedia( mediaLocal );
			mediaLocal.setMediaUsage( mediaLocal.getMediaUsage() + getInstanceSize( il ) );
		}
    }
	/**
	 * Returns the size of the given instance.
	 * <p>
	 * If the instance contains more than one file, the size of the latest (with highest pk) file is used.
	 * <p>
	 * Copy of InstanceCollector.getInstanceSize method!!
	 * 
	 * @param instance The instance
	 * 
	 * @return The file size of the instance.
	 */
	private long getInstanceSize(InstanceLocal instance) {
		Collection col = instance.getFiles();
		if ( col.size() == 1 ) { FileLocal l;
			return ( (FileLocal) col.iterator().next() ).getFileSize();
		} else {
			long size = 0;
			int pk = Integer.MIN_VALUE;
			Iterator iter = col.iterator();
			FileLocal file;
			while ( iter.hasNext() ) {
				file = (FileLocal) iter.next();
				if ( file.getPk().intValue() > pk ) {
					pk = file.getPk().intValue();
					size = file.getFileSize();
				}
			}
			return size;
		}
	}

    /**
     * Assign the given MediaLocal to all instances of given collection of InstanceContainer(studies).
     * 
     * @param studies		Collection of InstanceContainer.
     * @param mediaLocal	The media that will be assigned.
     */
    private void assignMedia( Collection studies, MediaLocal mediaLocal) {
    	InstanceContainer ic;
    	Iterator iter = studies.iterator();
    	while ( iter.hasNext() ) {
    		ic = (InstanceContainer) iter.next();
    		assignMedia( ic, mediaLocal );
    	}
    }
    
    /**
     * Returns a list of all media with the given media status.
     * <p>
     * The list contains a MediaDTO object for each media with the given status.
     * 
     * @param status The media status
     * 
     * @return A list of MediaDTO objects.
     * 
     * @ejb.interface-method
     */
    public List getWithStatus(int status) throws FinderException {
        return toMediaDTOs(mediaHome.findByStatus(status));
    }
    
    /**
     * Find media for given search params.
     * <p>
     * Add all founded media to the given collection.<br>
     * This allows to fill a collection with sequential calls without clearing the collection.<br>
     * 
     * @param col		The collection to store the result.
     * @param after		'created after' Timestamp in milliseconds
     * @param before	'created before' Timestamp in milliseconds
     * @param stati		Media status (<code>null</code> to get all media for given time range)
     * @param offset	Offset of the find result. (used for paging.
     * @param limit		Max. number of results to return. (used for paging)
     * @param desc		Sort order. if true descending, false ascending order.
     * 
     * @return	The total number of search results.
     * 
     * @ejb.interface-method
     */
    public int findByCreatedTime( Collection col, Long after,
            Long before, int[] stati, Integer offset, Integer limit,
            boolean desc) throws FinderException {
    	Timestamp tsAfter = null;
    	if ( after != null ) tsAfter = new Timestamp(after.longValue() );
    	Timestamp tsBefore = null;
    	if ( before != null ) tsBefore = new Timestamp(before.longValue() );
    	col.addAll( toMediaDTOs( mediaHome.listByCreatedTime( stati, tsAfter,
    			tsBefore, offset, limit, desc ) ) );
    	return mediaHome.countByCreatedTime( stati,tsAfter, tsBefore );
    }

    /**
     * Find media for given search params.
     * <p>
     * Add all founded media to the given collection.<br>
     * This allows to fill a collection with sequential calls without clearing the collection.<br>
     * 
     * @param col		The collection to store the result.
     * @param after		'updated after' Timestamp in milliseconds
     * @param before	'updated before' Timestamp in milliseconds
     * @param stati		Media status (<code>null</code> to get all media for given time range)
     * @param offset	Offset of the find result. (used for paging.
     * @param limit		Max. number of results to return. (used for paging)
     * @param desc		Sort order. if true descending, false ascending order.
     * 
     * @return	The total number of search results.
     * 
     * @ejb.interface-method
     */
    public int findByUpdatedTime( Collection col, Long after,
            Long before, int[] stati, Integer offset, Integer limit,
            boolean desc) throws FinderException {
    	Timestamp tsAfter = null;
    	if ( after != null ) tsAfter = new Timestamp(after.longValue() );
    	Timestamp tsBefore = null;
    	if ( before != null ) tsBefore = new Timestamp(before.longValue() );
    	col.addAll( toMediaDTOs( mediaHome.listByUpdatedTime( stati, tsAfter,
    			tsBefore, offset, limit, desc ) ) );
    	return mediaHome.countByUpdatedTime( stati, tsAfter, tsBefore );
    }
    
    /**
     * Converts a collection of MediaLocal objects to a list of MediaDTO objects.
     * 
     * @param c Collection with MediaLocal objects.
     * 
     * @return List of MediaDTO objects.
     */
    private List toMediaDTOs(Collection c) {
        ArrayList list = new ArrayList();
        for (Iterator it = c.iterator(); it.hasNext();) {
            list.add(toMediaDTO((MediaLocal) it.next()));
        }
        return list;
    }

    /**
     * Creates a MediaDTO object for given given MediaLocal object.
     * 
     * @param media A MediaLocal object.
     * 
     * @return The MediaDTO object for given MediaLocal.
     */
    private MediaDTO toMediaDTO(MediaLocal media) {
        MediaDTO dto = new MediaDTO();
        dto.setPk(media.getPk().intValue());
        dto.setCreatedTime(media.getCreatedTime());
        dto.setUpdatedTime(media.getUpdatedTime());
        dto.setMediaUsage(media.getMediaUsage());
        dto.setMediaStatus(media.getMediaStatus());
        dto.setMediaStatusInfo(media.getMediaStatusInfo());
        dto.setFilesetId(media.getFilesetId());
        dto.setFilesetIuid(media.getFilesetIuid());
        dto.setMediaCreationRequestIuid(media.getMediaCreationRequestIuid());
        return dto;
    }    

    /**
     * Set the media creation request IUID.
     * 
     * @param pk 	Primary key of media.
     * @param iuid	Media creation request IUID to set.
     * 
     * @ejb.interface-method
     */
    public void setMediaCreationRequestIuid(int pk, String iuid)
    		throws FinderException {
        MediaLocal media = mediaHome.findByPrimaryKey(new Integer(pk));
        media.setMediaCreationRequestIuid(iuid);
    }

    /**
     * Set media staus and status info.
     * 
     * @param pk		Primary key of media.
     * @param status	Status to set.
     * @param info		Status info to set.
     * 
     * @ejb.interface-method
     */
    public void setMediaStatus(int pk, int status, String info)
    		throws FinderException {
    	if (log.isDebugEnabled()) log.debug("setMediaStatus: pk="+pk+", status:"+status+", info"+info);
        MediaLocal media = mediaHome.findByPrimaryKey(new Integer(pk));
        media.setMediaStatus(status);
        media.setMediaStatusInfo(info);
        if ( status == MediaDTO.COMPLETED )
        	updateSeriesAndStudies( media );
    }
    
    /**
     * Returns a collection of study IUIDs of a given media.
     * 
     * @param pk Primary key of the media.
     * 
     * @return Collection with study IUIDs.
     * 
     * @ejb.interface-method
     */
    public Collection getStudyUIDSForMedia( int pk ) throws FinderException {
    	Collection c = new ArrayList();
    	MediaLocal media = mediaHome.findByPrimaryKey( new Integer(pk) );
    	Collection studies = studyHome.findStudiesOnMedia( media );
    	for ( Iterator iter = studies.iterator(); iter.hasNext() ; ) {
    		c.add( ((StudyLocal) iter.next()).getStudyIuid() );
    	}
    	return c;
    }
    
    /**
     * Returns a dataset for media creation request for given media.
     * <p>
     * <DL>
     * <DT>Set following Tags in dataset.</DT>
     * <DD>SpecificCharacterSet</DD>
     * <DD>StorageMediaFileSetID</DD>
     * <DD>StorageMediaFileSetUID</DD>
     * <DD>RefSOPSeq with instances of the media.</DD>
     * </DL>
     * 
     * @param pk Primary key of the media.
     * 
     * @return Prepared Dataset for media creation request.
     * 
     * @ejb.interface-method
     */
    public Dataset prepareMediaCreationRequest( int pk ) throws FinderException {
    	MediaLocal media = mediaHome.findByPrimaryKey( new Integer(pk) );
		Dataset ds = DcmObjectFactory.getInstance().newDataset();
		ds.putCS(Tags.SpecificCharacterSet, "ISO_IR 100");
		ds.putSH(Tags.StorageMediaFileSetID, media.getFilesetId() );
		ds.putUI(Tags.StorageMediaFileSetUID, media.getFilesetIuid() );
    	Collection c = media.getInstances();
    	InstanceLocal il;
        DcmElement refSOPSeq = ds.putSQ(Tags.RefSOPSeq);
    	for ( Iterator iter = c.iterator() ; iter.hasNext() ; ) {
    		il = (InstanceLocal) iter.next();
        	Dataset item = refSOPSeq.addNewItem();
            item.putUI(Tags.RefSOPInstanceUID, il.getSopIuid());
            item.putUI(Tags.RefSOPClassUID, il.getSopCuid());
    	}
    	return ds;
    }
    
    private void updateSeriesAndStudies( MediaLocal media ) throws FinderException {
    	Collection series = seriesHome.findSeriesOnMedia( media );
    	Iterator iter = series.iterator();
    	while ( iter.hasNext() ) {
    		final SeriesLocal ser = ( (SeriesLocal) iter.next() );
			ser.updateDerivedFields(false, false, false, true, false);
    	}
    	Collection studies = studyHome.findStudiesOnMedia( media );
    	iter = studies.iterator();
    	while ( iter.hasNext() ){
    		final StudyLocal sty = ( (StudyLocal) iter.next());
			sty.updateDerivedFields(false, false, false, true, false, false);
    	}
    }
    
    /**
     * Deletes a media.
     * <p>
     * Update derived fields from series and studies after media is successfully deleted.
     * 
     * @param mediaPk Primary key of the media.
     * 
     * @ejb.interface-method
     */
    public void deleteMedia( Integer mediaPk ) throws EJBException, RemoveException, FinderException {
    	MediaLocal media = mediaHome.findByPrimaryKey( mediaPk );
    	Collection series = seriesHome.findSeriesOnMedia( media );
      	Collection studies = studyHome.findStudiesOnMedia( media );
        String filesetId = media.getFilesetId();
        mediaHome.remove( mediaPk );
    	log.info("Media "+filesetId+" removed!");
      	Iterator iter = series.iterator();
    	while ( iter.hasNext() ) {
    		SeriesLocal ser = (SeriesLocal) iter.next();
			ser.updateDerivedFields(false, false, false, true, true);
    	}
    	if ( log.isDebugEnabled() ) log.debug( "Series updated after media "+filesetId+" was deleted!");
    	iter = studies.iterator();
    	while ( iter.hasNext() ){
    		StudyLocal sty = (StudyLocal) iter.next();
    		sty.updateDerivedFields(false, false, false, true, true, false);
     	}
    	if ( log.isDebugEnabled() ) log.debug( "Studies updated after media "+filesetId+" was deleted!");
   	
    }
}