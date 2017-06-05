/*
 * Created on 29.12.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.dcm4chex.archive.web.maverick.mpps;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.dcm4che.data.Dataset;
import org.dcm4chex.archive.ejb.jdbc.MPPSFilter;
import org.dcm4chex.archive.ejb.jdbc.MPPSQueryCmd;
import org.dcm4chex.archive.ejb.jdbc.QueryCmd;

/**
 * @author franz.willer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MPPSDelegate {
		
	    private static Logger log = Logger.getLogger( MPPSDelegate.class.getName() );

		public Logger getLogger() {
			return log;
		}
		
		/**
		 * Makes the MBean call to get the list of worklist entries for given filter (ds).
		 * 
		 * @param filter	The WADO request.
		 * 
		 * @return The list of worklist entries ( Each item in the list is a Dataset of one scheduled procedure step).
		 */
		public List findMppsEntries( MPPSFilter filter ) {
			List resp = null;
			MPPSQueryCmd cmd = null;
			try {
				resp = new ArrayList();
				cmd = new MPPSQueryCmd( filter );
				cmd.execute();
				while ( cmd.next() ) {
					resp.add( cmd.getDataset() );
				}
			} catch ( Exception x ) {
				log.error( "Exception occured in getMWLEntries: "+x.getMessage(), x );
			}
			if ( cmd != null ) cmd.close();
	        return resp;
		}
		

}
