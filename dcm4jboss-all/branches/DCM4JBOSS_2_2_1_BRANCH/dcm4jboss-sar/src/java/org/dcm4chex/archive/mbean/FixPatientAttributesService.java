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
import java.sql.SQLException;
import java.util.List;

import javax.ejb.CreateException;
import javax.ejb.FinderException;

import org.apache.log4j.Logger;
import org.dcm4chex.archive.ejb.interfaces.FixPatientAttributes;
import org.dcm4chex.archive.ejb.interfaces.FixPatientAttributesHome;
import org.dcm4chex.archive.ejb.jdbc.QueryAllStudiesCmd;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.jboss.system.ServiceMBeanSupport;

/**
 * @author franz.willer@gwi-ag.com
 * @version $Revision: 2084 $ $Date: 2005-11-21 20:46:08 +0800 (周一, 21 11月 2005) $
 * @since 35.03.2005
 *
 */
public class FixPatientAttributesService extends ServiceMBeanSupport {

     private int limitNumberOfRecordsPerTask;

    private static final Logger log = Logger.getLogger(FixPatientAttributesService.class);




    public int getLimitNumberOfRecordsPerTask() {
        return limitNumberOfRecordsPerTask;
    }

    public void setLimitNumberOfRecordsPerTask(int limit) {
        this.limitNumberOfRecordsPerTask = limit;
    }

  
    public int checkPatientAttributes() throws RemoteException, FinderException, CreateException {
    	return checkPatientAttributes(false);
    }
    public int repairPatientAttributes() throws RemoteException, FinderException, CreateException {
     	return checkPatientAttributes(true);
    }
    
    private int checkPatientAttributes(boolean doUpdate) throws RemoteException, FinderException {
    	FixPatientAttributes checker = newFixPatientAttributes();
    	int offset = 0, total = 0;
    	int[] fixed;
		do {
			fixed = checker.checkPatientAttributes(offset,limitNumberOfRecordsPerTask, doUpdate);
			total += fixed[0];
    		offset += limitNumberOfRecordsPerTask;
		} while (fixed[1] == limitNumberOfRecordsPerTask);
    	return total;
    }
    
    public int checkStudyAttributes() throws RemoteException, FinderException, CreateException, SQLException {
    	return checkStudyAttributes(false);
    }
    public int repairStudyAttributes() throws RemoteException, FinderException, CreateException, SQLException {
     	return checkStudyAttributes(true);
    }
    
    private int checkStudyAttributes(boolean doUpdate) throws RemoteException, SQLException {
    	FixPatientAttributes checker = newFixPatientAttributes();
    	int offset = 0, total = 0;
    	QueryAllStudiesCmd cmd = new QueryAllStudiesCmd();
    	log.info(cmd.count()+" studies found to check study attributes!");
    	List studyPks;
    	try {
	    	while ( (studyPks = cmd.list( offset, limitNumberOfRecordsPerTask)).size() > 0 ) {
				total += checker.checkStudyAttributes(studyPks, doUpdate);
	    		offset += limitNumberOfRecordsPerTask;
			}
    	} finally {
    		cmd.close();
    	}
    	return total;
    }

    private FixPatientAttributes newFixPatientAttributes() {
        try {
        	FixPatientAttributesHome home = (FixPatientAttributesHome) EJBHomeFactory
                    .getFactory().lookup(FixPatientAttributesHome.class,
                    		FixPatientAttributesHome.JNDI_NAME);
            return home.create();
        } catch (Exception e) {
            throw new RuntimeException("Failed to access FixPatientAttributes EJB:",
                    e);
        }
    }
    
}