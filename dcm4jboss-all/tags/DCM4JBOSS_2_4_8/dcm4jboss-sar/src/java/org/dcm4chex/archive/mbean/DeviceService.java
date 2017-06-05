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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.CreateException;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;
import org.dcm4cheri.util.StringUtils;
import org.dcm4chex.archive.ejb.interfaces.CodeToDeviceMapping;
import org.dcm4chex.archive.ejb.interfaces.CodeToDeviceMappingHome;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.dcm4chex.archive.util.HomeFactoryException;
import org.jboss.system.ServiceMBeanSupport;

/**
 * @author franz.willer@tiani.com
 * @version $Revision: 1547 $ $Date: 2005-02-22 02:05:11 +0800 (周二, 22 2月 2005) $
 * @since 17.02.2005
 */
public class DeviceService extends ServiceMBeanSupport {

	private static final String STANDARD_XSL_URL = "resource:xsl/device/import.xsl";
	
    private CodeToDeviceMapping mapper;
	private Map templates = new HashMap();

	private static final TransformerFactory tf = TransformerFactory.newInstance();
    private static final DcmObjectFactory dof = DcmObjectFactory.getInstance();

	public DeviceService() {
    }
    
    protected void startService() throws Exception {
    }

    protected void stopService() throws Exception {
    }

    
    public String importDeviceMapping(String importURL)  {
        Dataset ds = dof.newDataset();
        try {
	        Transformer t = getTemplates(STANDARD_XSL_URL).newTransformer();
	        t.transform(new StreamSource(importURL),
	        		new SAXResult(ds.getSAXHandler2(null)));
	        lookupMapper().createMapping( ds );
        } catch ( Exception x ) {
        	x.printStackTrace();
        	return "Exception:"+x.getMessage();
        }
     	return "Device mapping from url '"+importURL+"' imported!";
    }

    public String deleteMapping() throws RemoteException, FinderException, RemoveException, HomeFactoryException, CreateException {
    	return "Status:"+lookupMapper().deleteMapping();
    }

    public String deleteDevice(String device) throws RemoteException, FinderException, RemoveException, HomeFactoryException, CreateException {
    	return "Status:"+lookupMapper().deleteDevice( device );
    }
    
    public Collection getDeviceList( String protocol ) throws RemoteException, HomeFactoryException, CreateException, FinderException {
        String[] sa = StringUtils.split( protocol, '^' );
        if ( sa.length < 3 ) {
        	throw new IllegalArgumentException( "Wrong protocol format! Use <code>^<meaning>^<designator> !");
        }
        Dataset ds = dof.newDataset(); 
        DcmElement sq = ds.putSQ( Tags.SPSSeq );
        Dataset ds1 = sq.addNewItem();
        DcmElement codeSq = ds1.putSQ( Tags.ScheduledProtocolCodeSeq );
        Dataset ds2 = codeSq.addNewItem();
        ds2.putSH( Tags.CodeValue, sa[0] );
    	ds2.putSH( Tags.CodeMeaning, sa[1]);
    	ds2.putSH( Tags.CodingSchemeDesignator, sa[2] );
    	Dataset ds3 = addScheduledStationInfo( ds );
    	sq = ds3.get( Tags.SPSSeq );
    	ds1 = sq.getItem();
    	String dev = ds1.getString( Tags.ScheduledStationName );
    	List l = new ArrayList(); l.add( dev );
    	return l;
    }
    
    public Dataset addScheduledStationInfo( Dataset ds ) throws RemoteException, FinderException, HomeFactoryException, CreateException {
    	return lookupMapper().addScheduledStationInfo(ds);
    }
    
    public Templates getTemplates(String uri) throws TransformerConfigurationException {
		Templates tpl = (Templates) templates.get(uri);
		if (tpl == null) {
		    tpl = tf.newTemplates(new StreamSource(uri));
		    templates.put(uri, tpl);
		}
		return tpl;
    }


   
	/**
	 * Returns the CodeToDeviceMapping session bean.
	 * 
	 * @return The CodeToDeviceMapping.
	 * 
	 * @throws HomeFactoryException
	 * @throws RemoteException
	 * @throws CreateException
	 */
    private CodeToDeviceMapping lookupMapper() throws HomeFactoryException, RemoteException, CreateException {
    	if ( mapper == null ) {
    		CodeToDeviceMappingHome home = (CodeToDeviceMappingHome) EJBHomeFactory
	        .getFactory().lookup(CodeToDeviceMappingHome.class,
	        		CodeToDeviceMappingHome.JNDI_NAME);
    		mapper = home.create();
    	}
    	return mapper;
    }
    
}
