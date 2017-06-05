package org.dcm4chex.wado.web;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.dcm4chex.wado.common.WADORequestObject;
import org.dcm4chex.wado.common.WADOResponseObject;
import org.dcm4chex.wado.mbean.WADOStreamResponseObjectImpl;
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
public class WADOServiceDelegate {

    private static ObjectName wadoServiceName = null;
    private static ObjectName wadoExtServiceName = null;
	private static MBeanServer server;
	
    private static Logger log = Logger.getLogger( WADOServiceDelegate.class.getName() );

    /** 
     * Iinitialize the WADO service delegator.
     * <p>
     * Set the name of the WADOService MBean with the servlet config param 'wadoServiceName'.
     * 
     * @param config The ServletConfig object.
     */
	public void init( ServletConfig config ) {
        if (server != null) return;
        server = MBeanServerLocator.locate();
        String s = config.getInitParameter("wadoServiceName");
        try {
			wadoServiceName = new ObjectName(s);
			
		} catch (Exception e) {
			log.error( "Exception in init! Servlet init parameter 'wadoServiceName' not valid",e );
		}
        s = config.getInitParameter("wadoExtServiceName");
        try {
			wadoExtServiceName = new ObjectName(s);
			
		} catch (Exception e) {
			log.error( "Exception in init! Servlet init parameter 'wadoExtServiceName' not valid",e );
		}
       
    }

	public Logger getLogger() {
		return log;
	}
	
	public boolean isWadoExtAllowed() {
		try {
			return ( (Boolean)server.getAttribute( wadoServiceName, "ExtendedWADOAllowed" ) ).booleanValue();
		} catch (Exception x) {
			log.error("Exception occured in isWadoExtAllowed: "+x.getMessage(), x);
			return false;
		}
	}
	
	/**
	 * Makes the MBean call to get the WADO response object for given WADO request.
	 * 
	 * @param reqVO	The WADO request.
	 * 
	 * @return The WADO response object.
	 */
	public WADOResponseObject getWADOObject( WADORequestObject reqVO ) {
		WADOResponseObject resp = null;
		ObjectName serviceName = "WADO".equalsIgnoreCase( reqVO.getRequestType() ) ? wadoServiceName : wadoExtServiceName;
		try {
	        Object o = server.invoke(serviceName,
	                "getWADOObject",
	                new Object[] { reqVO },
	                new String[] { WADORequestObject.class.getName() } );
	        resp = (WADOResponseObject) o;
		} catch ( Exception x ) {
			log.error( "Exception occured in getWADOObject: "+x.getMessage(), x );
			resp = new WADOStreamResponseObjectImpl( null, "text.html", HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unexpected error in WADO service ("+serviceName+"): "+x.getMessage());
		}
        return resp;
	}
	
	public String getExtendedWADORequestType() {
		try {
			return ( (String)server.getAttribute( wadoServiceName, "ExtendedWADORequestType" ) );
		} catch (Exception x) {
			log.error("Exception occured in getExtendedWADORequestType: "+x.getMessage(), x);
			return "WADOext";
		}
	}

}
