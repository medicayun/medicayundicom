/*
 * Created on 29.12.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.dcm4chex.archive.web.maverick.mcmc;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.servlet.ServletConfig;

import org.apache.log4j.Logger;
import org.jboss.mx.util.MBeanServerLocator;

/**
 * @author franz.willer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MCMScuDelegate {
	   private static ObjectName mcmScuServiceName = null;
		private static MBeanServer server;
		
	    private static Logger log = Logger.getLogger( MCMScuDelegate.class.getName() );

	    /** 
	     * Iinitialize the MCM service delegator.
	     * <p>
	     * Set the name of the MCM MBean with the servlet config param 'mcmScuServiceName'.
	     * 
	     * @param config The ServletConfig object.
	     */
		public void init( ServletConfig config ) {
	        if (server != null) return;
	        server = MBeanServerLocator.locate();
	        String s = config.getInitParameter("mcmScuServiceName");
	        try {
	        	mcmScuServiceName = new ObjectName(s);
				
			} catch (Exception e) {
				log.error( "Exception in init! ",e );
			}
	       
	    }

		public Logger getLogger() {
			return log;
		}
		
		/**
		 * Makes the MBean call to update the media status.
		 * 
		 * 
		 * @return An info string for status of media creation (nr of medias done, failed and processing).
		 */
		public String updateMediaStatus() {
			String resp = null;
			try {
		        Object o = server.invoke(mcmScuServiceName,
		                "updateMediaStatus",
		                null,
		                null );
		        resp = (String) o;
			} catch ( Exception x ) {
				log.error( "Exception occured in updateMediaStatus: "+x.getMessage(), x );
			}
	        return resp;
		}
		
		/**
		 * Checks if Media Creation SCP is available.
		 * <p>
		 * Checks move destination AET and MCM_SCP AET.
		 * 
		 * @return true if available.
		 */
		public boolean checkMcmScpAvail() {
			String resp = "";
			try {
		        Object o = server.invoke(mcmScuServiceName,
		                "checkMcmScpAvail",
		                null,
		                null );
		        resp = (String) o;
			} catch ( Exception x ) {
				if ( log.isDebugEnabled() ) log.debug( "Exception occured in checkMcmScpAvail: "+x.getMessage(), x );
			}
			return "OK".equals( resp );
			
		}
		
		public boolean deleteMedia( int pk ) {
			try {
		        Object o = server.invoke(mcmScuServiceName,
		                "deleteMedia",
		                new Object[]{ new Integer( pk ) },
		                new String[]{ Integer.class.getName() } );
		        return true;
			} catch ( Exception x ) {
				log.error( "Exception occured in deleteMedia("+pk+"): "+x.getMessage(), x );
				return false;
			}
			
		}

}
