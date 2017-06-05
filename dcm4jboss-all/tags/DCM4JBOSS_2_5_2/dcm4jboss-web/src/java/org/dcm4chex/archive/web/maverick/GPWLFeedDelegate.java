/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.web.maverick;

import java.util.ArrayList;
import java.util.List;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.log4j.Logger;
import org.dcm4cheri.util.StringUtils;
import org.infohazard.maverick.flow.ControllerContext;
import org.jboss.mx.util.MBeanServerLocator;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1700 $ $Date: 2005-04-15 20:50:21 +0800 (周五, 15 4月 2005) $
 * @since 14.10.2004
 *
 */
public class GPWLFeedDelegate {

    private static Logger log = Logger.getLogger(GPWLFeedDelegate.class);

    private static MBeanServer server;

    private static ObjectName gpwlFeedServiceName;


    void init(ControllerContext ctx) throws Exception {
        if (gpwlFeedServiceName != null) return;
        GPWLFeedDelegate.server = MBeanServerLocator.locate();
        String s = ctx.getServletConfig().getInitParameter("gpwlFeedServiceName");
        GPWLFeedDelegate.gpwlFeedServiceName = new ObjectName(s);
    }

    public List getHumanPerformerList() {
    	Object o = null;
    	List l = new ArrayList();
        try {
            o = server.invoke(gpwlFeedServiceName,
                    "getHumanPerformer",
                    new Object[] {},
                    new String[] {});
        } catch (Exception e) {
            log.warn("Failed to get list of human performer!", e);
        }
        if ( o != null ) {
        	String[] sa = StringUtils.split( (String) o, ',');
        	for ( int i = 0 ; i < sa.length ; i++ ) {
        		if ( sa[i].length() > 3)
        			l.add( new CodeItem( sa[i] ) );
        	}
        }
        return l;
    }

    public List getTemplateList() {
    	Object o = null;
    	List l = new ArrayList();
        try {
            o = server.invoke(gpwlFeedServiceName,
                    "getTemplates",
                    new Object[] {},
                    new String[] {});
        } catch (Exception e) {
            log.warn("Failed to get template list!", e);
        }
        if ( o != null ) {
        	String[] sa = StringUtils.split( (String) o, ',');
        	for ( int i = 0 ; i < sa.length ; i++ ) {
        		if ( sa[i].length() > 3)
        			l.add( new CodeItem( sa[i] ) );
        	}
        }
        return l;
    }
    
    public boolean addWorklistItem( int studyPk, String templateFile, String humanPerformer, long scheduleDate ) {
    	log.info("addWorklistItem: studyPk:"+studyPk+" templateFile:"+templateFile+" humanPerformer:"+humanPerformer+" scheduleDate:"+scheduleDate);
        try {
            	server.invoke(gpwlFeedServiceName,
                    "addWorklistItem",
                    new Object[] { new Integer( studyPk ),
            					   templateFile,
								   humanPerformer,
								   new Long(scheduleDate)
            						},
                    new String[] { Integer.class.getName(), String.class.getName(), String.class.getName(), Long.class.getName() });
            	
            	return true;
        } catch (Exception e) {
            log.warn("Failed to add new work list item!", e);
            return false;
        }
    }
    
    public class CodeItem {
    	private String codeValue;
    	private String codeDesignator;
    	private String codeMeaning;
    	
    	public CodeItem( String codeString ) {
    		String[] sa = StringUtils.split( codeString, '^' );
    		codeValue = sa[0];
    		if ( sa.length > 2 ) {
    			codeDesignator = sa[1];
    			codeMeaning = sa[2];
    		} else {
    			codeMeaning = sa[1];
    		}
    		
    	}
		/**
		 * @return Returns the codeDesignator.
		 */
		public String getCodeDesignator() {
			return codeDesignator;
		}
		/**
		 * @param codeDesignator The codeDesignator to set.
		 */
		public void setCodeDesignator(String codeDesignator) {
			this.codeDesignator = codeDesignator;
		}
		/**
		 * @return Returns the codeMeaning.
		 */
		public String getCodeMeaning() {
			return codeMeaning;
		}
		/**
		 * @param codeMeaning The codeMeaning to set.
		 */
		public void setCodeMeaning(String codeMeaning) {
			this.codeMeaning = codeMeaning;
		}
		/**
		 * @return Returns the codeValue.
		 */
		public String getCodeValue() {
			return codeValue;
		}
		/**
		 * @param codeValue The codeValue to set.
		 */
		public void setCodeValue(String codeValue) {
			this.codeValue = codeValue;
		}
    }
}