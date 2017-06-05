/* $Id: ContentEditBean.java 989 2004-02-19 00:27:35Z gunterze $
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.ejb.EJBException;
import javax.ejb.FinderException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.dcm4che.data.Dataset;
import org.dcm4chex.archive.ejb.interfaces.DTO2Dataset;
import org.dcm4chex.archive.ejb.interfaces.PatientDTO;
import org.dcm4chex.archive.ejb.interfaces.PatientLocal;
import org.dcm4chex.archive.ejb.interfaces.PatientLocalHome;
import org.dcm4chex.archive.ejb.interfaces.SeriesLocalHome;
import org.dcm4chex.archive.ejb.interfaces.StudyLocalHome;

/**
 * 
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 989 $ $Date: 2004-02-19 08:27:35 +0800 (周四, 19 2月 2004) $
 * @since 14.01.2004
 * 
 * @ejb.bean
 *  name="ContentEdit"
 *  type="Stateless"
 *  view-type="remote"
 *  jndi-name="ejb/ContentEdit"
 * 
 * @ejb.transaction-type 
 *  type="Container"
 * 
 * @ejb.transaction 
 *  type="Required"
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
public abstract class ContentEditBean implements SessionBean {

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
    public void updatePatient(PatientDTO to_update) {

        try {
            boolean modified = false;
            PatientLocal patientLocal =
                patHome.findByPrimaryKey(new Integer(to_update.getPk()));

            if (!equals(to_update.getPatientName(),
                patientLocal.getPatientName())) {
                patientLocal.setPatientName(to_update.getPatientName());
                modified = true;
            }

            if (!equals(to_update.getPatientSex(),
                patientLocal.getPatientSex())) {
                patientLocal.setPatientSex(to_update.getPatientSex());
                modified = true;
            }
            Date date_to_update = null;
            if (to_update.getPatientBirthDate() != null) {
                try {
                    date_to_update =
                        new SimpleDateFormat(PatientDTO.DATE_FORMAT).parse(
                            to_update.getPatientBirthDate());
                    if (!equals(date_to_update,
                        patientLocal.getPatientBirthDate())) {
                        patientLocal.setPatientBirthDate(date_to_update);
                        modified = true;
                    }
                } catch (ParseException e) {
                } //do nothing
            } else if (patientLocal.getPatientBirthDate() != null) {
                patientLocal.setPatientBirthDate(null);
                modified = true;
            }

            if (modified = true) {
                Dataset oldPat = patientLocal.getAttributes();
                DTO2Dataset.updtateDataset(oldPat, to_update);
                patientLocal.setAttributes(oldPat);
            }
        } catch (FinderException e) {
            throw new EJBException(e);
        }
    }

    private boolean equals(Object a, Object b) {
        return a == null ? b == null : a.equals(b);
    }
}
