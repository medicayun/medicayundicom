/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.mbean;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmDecodeParam;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4che.util.UIDGenerator;
import org.dcm4chex.archive.common.DatasetUtils;
import org.dcm4chex.archive.ejb.jdbc.FileInfo;
import org.dcm4chex.archive.ejb.jdbc.RetrieveCmd;
import org.dcm4chex.archive.ejb.jdbc.RetrieveStudyDatesCmd;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.jboss.system.ServiceMBeanSupport;

/**
 * @author franz.willer@tiani.com
 * @version $Revision: 1847 $ $Date: 2005-07-13 23:38:45 +0800 (周三, 13 7月 2005) $
 * @since 17.02.2005
 */
public class StudyInfoService extends ServiceMBeanSupport {

    private static Logger log = Logger.getLogger( StudyInfoService.class.getName() );

	DcmObjectFactory dof = DcmObjectFactory.getInstance();
    
	public StudyInfoService() {
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
    
    private Dataset getQueryDS(String level, String uid ){
    	Dataset dsQ = dof.newDataset();
    	if ( "STUDY".equals(level) ) 
    		dsQ.putUI(Tags.StudyInstanceUID, uid);
    	else if ( "SERIES".equals(level) ) 
    		dsQ.putUI(Tags.SeriesInstanceUID, uid);
    	else if ( "IMAGE".equals(level) ) 
			dsQ.putUI(Tags.SOPInstanceUID, uid);
		else
			throw new IllegalArgumentException("Argument level must be either STUDY,SERIES or IMAGE! level:"+level);
    	dsQ.putCS(Tags.QueryRetrieveLevel, level);
    	return dsQ;
    }
    
    public boolean checkOutdated( Date date, String level, String uid ) {
    	Dataset dsQ = getQueryDS(level, uid);
    	try {
			RetrieveStudyDatesCmd cmd = RetrieveStudyDatesCmd.create(dsQ);
			Date mrDate = cmd.getMostRecentUpdatedTime();
			return date.before(mrDate);
		} catch (SQLException x) {
			log.error("Error while RetrieveStudyDatesCmd!", x);
		}
    	return true;
    }

    public Dataset retrieveStudyInfo( String level, String uid ) throws SQLException, IOException {
    	Dataset dsQ = getQueryDS(level, uid);
    	RetrieveCmd retrieveCmd = RetrieveCmd.create(dsQ);
    	Map all = retrieveCmd.getStudyFileInfo();//map containing series with series.pk as key, map with List of instances as value

    	Iterator iterSeries = all.values().iterator();
    	Iterator iterInstances;
    	List files;
    	FileInfo fileInfo = (FileInfo)((List)((Map)all.values().iterator().next() ).values().iterator().next() ).get(0);
    	Dataset ds = dof.newDataset();
    	ds.putUI(Tags.SOPClassUID,UIDs.TianiStudyInfo);
    	ds.putUI(Tags.SOPInstanceUID,UIDGenerator.getInstance().createUID());
    	DcmElement inputInfoSQ = ds.putSQ(Tags.InputInformationSeq);
    	Dataset dsInfo = DatasetUtils.fromByteArray(fileInfo.patAttrs,
                DcmDecodeParam.EVR_LE,
                null);
    	DatasetUtils.fromByteArray(fileInfo.studyAttrs,
                DcmDecodeParam.EVR_LE,
                dsInfo);
    	inputInfoSQ.addItem(dsInfo);
    	DcmElement seriesSQ = dsInfo.putSQ(Tags.RefSeriesSeq);
    	Dataset dsSeries;
    	Collection col;
    	while ( iterSeries.hasNext() ) {
    		col = ((Map) iterSeries.next()).values();
        	iterInstances = col.iterator();
        	fileInfo = (FileInfo)((List) col.iterator().next()).get(0);
    		dsSeries = DatasetUtils.fromByteArray(fileInfo.seriesAttrs,
                    DcmDecodeParam.EVR_LE,
	                null);
    		seriesSQ.addItem(dsSeries);
    		DcmElement sopSQ = dsSeries.putSQ(Tags.RefSOPSeq);
    		Dataset dsSOP;
    		while ( iterInstances.hasNext() ) {
            	files = (List) iterInstances.next();
            	fileInfo = (FileInfo) files.get(0);
    			dsSOP = DatasetUtils.fromByteArray(fileInfo.instAttrs,
                        DcmDecodeParam.EVR_LE,
		                null);
    			sopSQ.addItem(dsSOP);
            	String[]tsa = new String[files.size()];
            	for ( int i = 0; i < tsa.length ; i++) {
            		tsa[i] = ( (FileInfo) files.get(i)).tsUID;
            	}
    			dsSOP.putUI(Tags.RefSOPTransferSyntaxUIDInFile, tsa );
    		}
    	}
        if ( log.isDebugEnabled() ) {
        	log.debug("retrieveStudyInfo ("+level+","+uid+"):"); log.debug(ds);
        }
    	return ds;
    }
   

 
}
