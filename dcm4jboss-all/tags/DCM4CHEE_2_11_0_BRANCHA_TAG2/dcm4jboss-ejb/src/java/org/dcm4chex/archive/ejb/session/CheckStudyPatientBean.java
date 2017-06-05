/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), available at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * TIANI Medgraph AG.
 * Portions created by the Initial Developer are Copyright (C) 2003-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunter.zeilinger@tiani.com>
 * Franz Willer <franz.willer@gwi-ag.com>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package org.dcm4chex.archive.ejb.session;

import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.FinderException;
import javax.ejb.ObjectNotFoundException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.dcm4che.data.Dataset;
import org.dcm4chex.archive.common.Availability;
import org.dcm4chex.archive.ejb.interfaces.ContentEditLocalHome;
import org.dcm4chex.archive.ejb.interfaces.FileDTO;
import org.dcm4chex.archive.ejb.interfaces.FileLocal;
import org.dcm4chex.archive.ejb.interfaces.InstanceLocal;
import org.dcm4chex.archive.ejb.interfaces.PatientLocal;
import org.dcm4chex.archive.ejb.interfaces.PatientLocalHome;
import org.dcm4chex.archive.ejb.interfaces.SeriesLocal;
import org.dcm4chex.archive.ejb.interfaces.StudyLocal;
import org.dcm4chex.archive.ejb.interfaces.StudyLocalHome;

/**
 * 
 * @author franz.willer@gwi-ag.com
 * @version $Revision: 3392 $ $Date: 2007-06-19 22:18:50 +0800 (周二, 19 6月 2007) $
 * @since 08.08.2005
 * 
 * @ejb.bean
 *  name="CheckStudyPatient"
 *  type="Stateless"
 *  view-type="both"
 *  jndi-name="ejb/CheckStudyPatient"
 * 
 * @ejb.transaction-type 
 *  type="Container"
 * 
 * @ejb.transaction 
 *  type="Required"
 * 
 * @ejb.ejb-ref ejb-name="Study" view-type="local" ref-name="ejb/Study"
 * @ejb.ejb-ref ejb-name="ContentEdit" view-type="local" ref-name="ejb/ContentEdit" 
 */
public abstract class CheckStudyPatientBean implements SessionBean {

    private StudyLocalHome studyHome;

    private PatientLocalHome patHome;

    private ContentEditLocalHome contentEditHome;
    
    private static final Logger log = Logger.getLogger(CheckStudyPatientBean.class);

    public void setSessionContext(SessionContext arg0) throws EJBException,
            RemoteException {
        Context jndiCtx = null;
        try {
            jndiCtx = new InitialContext();
            studyHome = (StudyLocalHome) jndiCtx
                    .lookup("java:comp/env/ejb/Study");
            contentEditHome = (ContentEditLocalHome) jndiCtx
    		.lookup("java:comp/env/ejb/ContentEdit");
            patHome = (PatientLocalHome) jndiCtx
                .lookup("java:comp/env/ejb/Patient");
        } catch (NamingException e) {
            throw new EJBException(e);
        } finally {
            if (jndiCtx != null) {
                try {
                    jndiCtx.close();
                } catch (NamingException ignore) {
                }
            }
        }
    }

    public void unsetSessionContext() {
        patHome = null;
        studyHome = null;
        contentEditHome = null;
    }
    
    /**
     * Return studies to check patient info of study.
     * 
     * @param status 	Study Status as int value.
     * @param sourceAET	Source AET.
     * @param limit		Max number of returned studies.
     * 
     * @return Collection with Dataset and Studies and .
     * 
     * @ejb.interface-method
     */
    public Collection findStudiesForTest(Integer status, String sourceAET, int limit) throws FinderException {
        if ( log.isDebugEnabled() ) log.debug("findStudyWithPatientCoercion: status:"+status+" sourceAET:"+sourceAET+" limit:"+limit);
    	Collection col;
    	if ( status != null ) {
    		if ( sourceAET != null ) {
    			col = studyHome.findStudiesWithStatusFromAE(status.intValue(),sourceAET,limit);
    		} else {
    			col = studyHome.findStudiesWithStatus(status.intValue(),new Timestamp( System.currentTimeMillis()),limit);
    		}
    	} else {
			col = studyHome.findStudiesFromAE(sourceAET,limit);
    	}
    	StudyLocal study;
    	SeriesLocal series;
    	InstanceLocal instance;
    	Dataset ds;
    	FileDTO dto;
    	Collection result = new ArrayList();
    	for ( Iterator iter = col.iterator() ; iter.hasNext() ; ) {
    		study = (StudyLocal) iter.next();
    		if ( study.getAvailabilitySafe() != Availability.ONLINE) continue;
    		ds = study.getAttributes(true);
    		ds.putAll( study.getPatient().getAttributes(false) );
    		series = (SeriesLocal)study.getSeries().iterator().next();
    		instance = (InstanceLocal) series.getInstances().iterator().next();
    		dto = ((FileLocal) instance.getFiles().iterator().next() ).getFileDTO();
    		result.add(new Object[]{ds,dto});
    	}
    	return result;
      }
    

    

    /**
     * @throws CreateException
     * @throws FinderException
     * @ejb.interface-method
     */
    public Dataset moveStudyToNewPatient(Dataset patDS, long studyPk )
            throws CreateException, FinderException {
    	PatientLocal pat = getOrCreatePatient(patDS);
        contentEditHome.create().moveStudies(new long[]{studyPk}, pat.getPk().longValue());
    	return patDS;
    }
 
    private PatientLocal getOrCreatePatient(Dataset ds)
            throws CreateException, FinderException {
        try {
            return patHome.searchFor(ds, false);
        } catch (ObjectNotFoundException e) {
            return patHome.create(ds);
        }
    }

    /**
     * @throws FinderException
     * @ejb.interface-method
     */
    public void updateStudyStatus(long studyPk, Integer studyStatus )
            throws FinderException  {
    	if ( studyStatus == null ) return;
    	StudyLocal study = studyHome.findByPrimaryKey(new Long(studyPk));
    	study.setStudyStatus( studyStatus.intValue());
    }
 
}