package org.dcm4chex.wado.web;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.servlet.ServletConfig;

import org.apache.log4j.Logger;
import org.dcm4chex.wado.common.RIDRequestObject;
import org.dcm4chex.wado.common.WADOResponseObject;
import org.jboss.mx.util.MBeanServerLocator;

/*
 * Created on 10.12.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

/**
 * @author franz.willer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class RIDServiceDelegate {

    private static ObjectName ridServiceName = null;
	private static MBeanServer server;
	
    private static Logger log = Logger.getLogger( RIDServiceDelegate.class.getName() );

    /** 
     * Iinitialize the RID service delegator.
     * <p>
     * Set the name of the WADOService MBean with the servlet config param 'wadoServiceName'.
     * 
     * @param config The ServletConfig object.
     */
	public void init( ServletConfig config ) {
        if (server != null) return;
        server = MBeanServerLocator.locate();
        String s = config.getInitParameter("ridServiceName");
        try {
			ridServiceName = new ObjectName(s);
			
		} catch (Exception e) {
			log.error( "Exception in init! ",e );
		}
       
    }

	public Logger getLogger() {
		return log;
	}
	
	/**
	 * Makes the MBean call to Retrieve Information for Display (SUMMARY).
	 * 
	 * @param reqVO	The RID request (Summary informationrequest).
	 * 
	 * @return The WADO response object.
	 */
	public WADOResponseObject getRIDSummary( RIDRequestObject reqVO ) {
		log.info( "RIDdelegate.getRIDSummary called");
		WADOResponseObject resp = null;
		try {
	        Object o = server.invoke(ridServiceName,
	                "getRIDSummary",
	                new Object[] { reqVO },
	                new String[] { RIDRequestObject.class.getName() } );
	        resp = (WADOResponseObject) o;
		} catch ( Exception x ) {
			log.error( "Exception occured in getRIDSummary: "+x.getMessage(), x );
		}
        return resp;
	}
	
	/**
	 * Makes the MBean call to Retrieve Document for Display.
	 * 
	 * @param reqVO	The RID request (Retrieve Document for Display).
	 * 
	 * @return The WADO response object.
	 */
	public WADOResponseObject getRIDDocument( RIDRequestObject reqVO ) {
		log.info( "RIDdelegate.getRIDDocument called");
		WADOResponseObject resp = null;
		try {
	        Object o = server.invoke(ridServiceName,
	                "getRIDDocument",
	                new Object[] { reqVO },
	                new String[] { RIDRequestObject.class.getName() } );
	        resp = (WADOResponseObject) o;
		} catch ( Exception x ) {
			log.error( "Exception occured in getRIDDocument: "+x.getMessage(), x );
		}
        return resp;
	}

}
