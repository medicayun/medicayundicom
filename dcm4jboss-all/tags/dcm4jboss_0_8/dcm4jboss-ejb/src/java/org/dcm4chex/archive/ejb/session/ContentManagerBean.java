/* $Id: ContentManagerBean.java 1133 2004-06-15 11:13:47Z gunterze $
 * Copyright (c) 2002,2003 by TIANI MEDGRAPH AG
 *
 * This file is part of dcm4che.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.dcm4chex.archive.ejb.session;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.ejb.EJBException;
import javax.ejb.FinderException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.dcm4chex.archive.ejb.interfaces.DTOFactory;
import org.dcm4chex.archive.ejb.interfaces.InstanceLocal;
import org.dcm4chex.archive.ejb.interfaces.PatientLocalHome;
import org.dcm4chex.archive.ejb.interfaces.SeriesLocal;
import org.dcm4chex.archive.ejb.interfaces.SeriesLocalHome;
import org.dcm4chex.archive.ejb.interfaces.StudyFilterDTO;
import org.dcm4chex.archive.ejb.interfaces.StudyLocal;
import org.dcm4chex.archive.ejb.interfaces.StudyLocalHome;
import org.dcm4chex.archive.ejb.jdbc.CountStudiesCmd;
import org.dcm4chex.archive.ejb.jdbc.QueryStudiesCmd;

/**
 * 
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1133 $ $Date: 2004-06-15 19:13:47 +0800 (周二, 15 6月 2004) $
 * @since 14.01.2004
 * 
 * @ejb.bean
 *  name="ContentManager"
 *  type="Stateless"
 *  view-type="remote"
 *  jndi-name="ejb/ContentManager"
 * 
 * @ejb.transaction-type 
 *  type="Container"
 * 
 * @ejb.transaction 
 *  type="NotSupported"
 * 
 * @ejb.ejb-ref
 *  ejb-name="Patient" 
 *  view-type="local"
 *  ref-name="ejb/Patient" 
 * 
 * @ejb.ejb-ref
 *  ejb-name="Study" 
 *  view-type="local"
 *  ref-name="ejb/Study" 
 * 
 * @ejb.ejb-ref
 *  ejb-name="Series" 
 *  view-type="local"
 *  ref-name="ejb/Series" 
 * 
 * @ejb.ejb-ref
 *  ejb-name="Instance" 
 *  view-type="local"
 *  ref-name="ejb/Instance" 
 *
 * @ejb:resource-ref
 *  res-name="jdbc/DefaultDS"
 *  res-type="javax.sql.DataSource"
 *  res-auth="Container"
 * 
 * @jboss:resource-ref
 *  res-ref-name="jdbc/DefaultDS"
 *  resource-name="java:/DefaultDS"
 */
public abstract class ContentManagerBean implements SessionBean {

    private DataSource ds;
    private PatientLocalHome patHome;
    private StudyLocalHome studyHome;
    private SeriesLocalHome seriesHome;

    public void setSessionContext(SessionContext arg0)
        throws EJBException, RemoteException {
        Context jndiCtx = null;
        try {
            jndiCtx = new InitialContext();
            ds = (DataSource) jndiCtx.lookup("java:comp/env/jdbc/DefaultDS");
            patHome =
                (PatientLocalHome) jndiCtx.lookup("java:comp/env/ejb/Patient");
            studyHome =
                (StudyLocalHome) jndiCtx.lookup("java:comp/env/ejb/Study");
            seriesHome =
                (SeriesLocalHome) jndiCtx.lookup("java:comp/env/ejb/Series");
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
        seriesHome = null;
    }

    /**
     * @ejb.interface-method
     */
    public int countStudies(StudyFilterDTO filter) {
        try {
            return new CountStudiesCmd(ds, filter).execute();
        } catch (SQLException e) {
            throw new EJBException(e);
        }
    }

    /**
     * @ejb.interface-method
     */
    public List listPatients(StudyFilterDTO filter, int offset, int limit) {
        try {
            return new QueryStudiesCmd(ds, filter, offset, limit).execute();
        } catch (SQLException e) {
            throw new EJBException(e);
        }
    }

    /**
     * @ejb.interface-method
     * @ejb.transaction type="Required"
     */
    public List listStudiesOfPatient(int patientPk) throws FinderException {
        Collection c =
            patHome.findByPrimaryKey(new Integer(patientPk)).getStudies();
        List result = new ArrayList(c.size());
        for (Iterator it = c.iterator(); it.hasNext();) {
            StudyLocal study = (StudyLocal) it.next();
            result.add(
                DTOFactory.newStudyDTO(
                    study.getPk().intValue(),
                    study.getAttributes(),
                    study.getModalitiesInStudy(),
                    study.getNumberOfStudyRelatedSeries(),
                    study.getNumberOfStudyRelatedInstances(),
                    study.getRetrieveAETs(),
                    study.getAvailabilitySafe()));
        }
        return result;
    }

    /**
     * @ejb.interface-method
     * @ejb.transaction type="Required"
     */
    public List listSeriesOfStudy(int studyPk) throws FinderException {
        Collection c =
            studyHome.findByPrimaryKey(new Integer(studyPk)).getSeries();
        List result = new ArrayList(c.size());
        for (Iterator it = c.iterator(); it.hasNext();) {
            SeriesLocal series = (SeriesLocal) it.next();
            result.add(
                DTOFactory.newSeriesDTO(
                    series.getPk().intValue(),
                    series.getAttributes(),
                    series.getNumberOfSeriesRelatedInstances(),
                    series.getRetrieveAETs(),
                    series.getAvailabilitySafe()));
        }
        return result;
    }

    /**
     * @ejb.interface-method
     * @ejb.transaction type="Required"
     */
    public List listInstancesOfSeries(int seriesPk) throws FinderException {
        Collection c =
            seriesHome.findByPrimaryKey(new Integer(seriesPk)).getInstances();
        List result = new ArrayList(c.size());
        for (Iterator it = c.iterator(); it.hasNext();) {
            InstanceLocal inst = (InstanceLocal) it.next();
            result.add(
                DTOFactory.newInstanceDTO(
                    inst.getPk().intValue(),
                    inst.getAttributes(),
                    inst.getRetrieveAETs(),
                    inst.getFiles().size(),
                    inst.getAvailabilitySafe(),
                    inst.getCommitmentSafe()));
        }
        return result;
    }

}
