/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.web.maverick;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.log4j.Logger;
import org.dcm4che.data.Dataset;
import org.infohazard.maverick.flow.ControllerContext;
import org.jboss.mx.util.MBeanServerLocator;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1700 $ $Date: 2005-04-15 20:50:21 +0800 (周五, 15 4月 2005) $
 * @since 14.10.2004
 *
 */
public class ContentEditDelegate {

    private static Logger log = Logger.getLogger(ContentEditDelegate.class);

    private static MBeanServer server;

    private static ObjectName contentEditName;


    void init(ControllerContext ctx) throws Exception {
        if (contentEditName != null) return;
        ContentEditDelegate.server = MBeanServerLocator.locate();
        String s = ctx.getServletConfig().getInitParameter("contentEditName");
        ContentEditDelegate.contentEditName = new ObjectName(s);
    }

    public Dataset createPatient(Dataset ds) {
    	Object o = null;
        try {
            o = server.invoke(contentEditName,
                    "createPatient",
                    new Object[] { ds },
                    new String[] { Dataset.class.getName() });
        } catch (Exception e) {
            log.warn("Failed to create Patient:", e);
        }
        return (Dataset) o;
    }

    public Dataset createStudy(Dataset ds, int patPk) {
    	Object o = null;
        try {
            o = server.invoke(contentEditName,
                    "createStudy",
                    new Object[] { ds, new Integer( patPk ) },
                    new String[] { Dataset.class.getName(), Integer.class.getName() });
        } catch (Exception e) {
            log.warn("Failed to create Study:", e);
        }
        return (Dataset) o;
    }

    public Dataset createSeries(Dataset ds, int studyPk) {
    	Object o = null;
        try {
            o = server.invoke(contentEditName,
                    "createSeries",
                    new Object[] { ds, new Integer( studyPk ) },
                    new String[] { Dataset.class.getName(), Integer.class.getName() });
        } catch (Exception e) {
            log.warn("Failed to create Series:", e);
        }
        return (Dataset) o;
    }

    public void mergePatients(int pk, int[] patPks) {
        try {
            server.invoke(contentEditName,
                    "mergePatients",
                    new Object[] { new Integer(pk), patPks },
                    new String[] { Integer.class.getName(), int[].class.getName() });
        } catch (Exception e) {
            log.warn("Failed to merge Patients:", e);
        }
    }
    
    public void updatePatient(Dataset ds) {
        try {
            server.invoke(contentEditName,
                    "updatePatient",
                    new Object[] { ds },
                    new String[] { Dataset.class.getName() });
        } catch (Exception e) {
            log.warn("Failed to update Patient:", e);
        }
    }
    
    public void updateStudy(Dataset ds) {
        try {
            server.invoke(contentEditName,
                    "updateStudy",
                    new Object[] { ds },
                    new String[] { Dataset.class.getName() });
        } catch (Exception e) {
            log.warn("Failed to update Study:", e);
        }
    }
   
    public void updateSeries(Dataset ds) {
        try {
            server.invoke(contentEditName,
                    "updateSeries",
                    new Object[] { ds },
                    new String[] { Dataset.class.getName() });
        } catch (Exception e) {
            log.warn("Failed to update Series:", e);
        }
    }
 
    public void deleteInstance( int pk ) {
        try {
            server.invoke(contentEditName,
                    "deleteInstance",
                    new Object[] { new Integer( pk ) },
                    new String[] { Integer.class.getName() });
        } catch (Exception e) {
            log.warn("Failed to delete Instance:", e);
        }
    }
    
    public void deleteSeries( int pk ) {
        try {
            server.invoke(contentEditName,
                    "deleteSeries",
                    new Object[] { new Integer( pk ) },
                    new String[] { Integer.class.getName() });
        } catch (Exception e) {
            log.warn("Failed to delete Series:", e);
        }
    }
 
    public void deleteStudy( int pk ) {
        try {
            server.invoke(contentEditName,
                    "deleteStudy",
                    new Object[] { new Integer( pk ) },
                    new String[] { Integer.class.getName() });
        } catch (Exception e) {
            log.warn("Failed to delete Study:", e);
        }
    }

    public void deletePatient( int pk ) {
        try {
            server.invoke(contentEditName,
                    "deletePatient",
                    new Object[] { new Integer( pk ) },
                    new String[] { Integer.class.getName() });
        } catch (Exception e) {
            log.warn("Failed to delete Patient:", e);
        }
    }

    public void moveStudies(int[] study_pks, int patient_pk) {
        try {
            server.invoke(contentEditName,
                    "moveStudies",
                    new Object[] { study_pks, new Integer(patient_pk) },
                    new String[] { int[].class.getName(), Integer.class.getName() });
        } catch (Exception e) {
            log.warn("Failed to move Studies:", e);
        }
     	
    }
 
    public void moveSeries(int[] series_pks, int study_pk) {
        try {
            server.invoke(contentEditName,
                    "moveSeries",
                    new Object[] { series_pks, new Integer(study_pk) },
                    new String[] { int[].class.getName(), Integer.class.getName() });
        } catch (Exception e) {
            log.warn("Failed to move Series:", e);
        }
     	
    }

    public void moveInstances(int[] instance_pks, int series_pk) {
        try {
            server.invoke(contentEditName,
                    "moveInstances",
                    new Object[] { instance_pks, new Integer(series_pk) },
                    new String[] { int[].class.getName(), Integer.class.getName() });
        } catch (Exception e) {
            log.warn("Failed to move Instances:", e);
        }
     	
    }

}