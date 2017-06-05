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

import javax.ejb.CreateException;
import javax.ejb.FinderException;

import org.apache.log4j.Logger;
import org.dcm4chex.archive.ejb.interfaces.FixPatientAttributes;
import org.dcm4chex.archive.ejb.interfaces.FixPatientAttributesHome;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.jboss.system.ServiceMBeanSupport;

/**
 * @author franz.willer@gwi-ag.com
 * @version $Revision: 1931 $ $Date: 2005-08-30 23:09:49 +0800 (周二, 30 8月 2005) $
 * @since 35.03.2005
 *
 */
public class FixPatientAttributesService extends ServiceMBeanSupport {

     private int limitNumberOfPatientsPerTask;

    private static final Logger log = Logger.getLogger(FixPatientAttributesService.class);




    public int getLimitNumberOfPatientsPerTask() {
        return limitNumberOfPatientsPerTask;
    }

    public void setLimitNumberOfPatientsPerTask(int limit) {
        this.limitNumberOfPatientsPerTask = limit;
    }

  
    public int check() throws RemoteException, FinderException, CreateException {
    	return checkPatientAttributes(false);
    }
    public int repair() throws RemoteException, FinderException, CreateException {
     	return checkPatientAttributes(true);
    }
    
    private int checkPatientAttributes(boolean doUpdate) throws RemoteException, FinderException {
    	FixPatientAttributes checker = newFixPatientAttributes();
    	int offset = 0, total = 0;
    	Integer fixed;
    	fixed = checker.checkPatientAttributes(offset,limitNumberOfPatientsPerTask, doUpdate);
    	while ( fixed != null ) {
    		total += fixed.intValue();
    		offset += limitNumberOfPatientsPerTask;
    		fixed = checker.checkPatientAttributes(offset,limitNumberOfPatientsPerTask, doUpdate);
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