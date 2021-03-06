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
import java.util.Collection;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import org.dcm4chex.archive.ejb.interfaces.PatientLocal;
import org.dcm4chex.archive.ejb.interfaces.PatientLocalHome;

/**
 * 
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 2010 $ $Date: 2005-10-07 03:55:27 +0800 (周五, 07 10月 2005) $
 * @since 14.01.2004
 * 
 * @ejb.bean
 *  name="PatientUpdate"
 *  type="Stateless"
 *  view-type="remote"
 *  jndi-name="ejb/PatientUpdate"
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
 */
public abstract class PatientUpdateBean implements SessionBean {

    private PatientLocalHome patHome;

    public void setSessionContext(SessionContext arg0) throws EJBException,
            RemoteException {
        Context jndiCtx = null;
        try {
            jndiCtx = new InitialContext();
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
    }

    /**
     * @ejb.interface-method
     */
    public void mergePatient(Dataset dominant, Dataset prior) {
        PatientLocal dominantPat = updateOrCreate(dominant);
        PatientLocal priorPat= updateOrCreate(prior);
        dominantPat.getStudies().addAll(priorPat.getStudies());
        dominantPat.getMpps().addAll(priorPat.getMpps());
        dominantPat.getMwlItems().addAll(priorPat.getMwlItems());
        dominantPat.getGsps().addAll(priorPat.getGsps());
        priorPat.setMergedWith(dominantPat);
        dominantPat.updateDerivedFields();
    }

    /**
     * @ejb.interface-method
     */
    public void updatePatient(Dataset attrs) {
        updateOrCreate(attrs);
    }

    private PatientLocal updateOrCreate(Dataset ds) {
        try {
            String pid = ds.getString(Tags.PatientID);
            String issuer = ds.getString(Tags.IssuerOfPatientID);
            Collection c = issuer == null ? patHome.findByPatientId(pid)
                    : patHome.findByPatientIdWithIssuer(pid, issuer);
            if (c.isEmpty()) { return patHome.create(ds); }
            if (c.size() > 1) { throw new FinderException("Patient ID[id="
                    + pid + ",issuer=" + issuer + " ambiguous"); }
            PatientLocal pat = (PatientLocal) c.iterator().next();
            update(pat, ds);
            return pat;
        } catch (FinderException e) {
            throw new EJBException(e);
        } catch (CreateException e) {
            throw new EJBException(e);
        }
    }

    private void update(PatientLocal pat, Dataset attrs) {
        Dataset ds = pat.getAttributes(false);
        ds.putAll(attrs);
        pat.setAttributes(ds);
    }

    /**
     * @ejb.interface-method
     */
    public void deletePatient(Dataset ds) throws RemoteException {
        try {
            String pid = ds.getString(Tags.PatientID);
            String issuer = ds.getString(Tags.IssuerOfPatientID);
            Collection c = issuer == null ? patHome.findByPatientId(pid)
                    : patHome.findByPatientIdWithIssuer(pid, issuer);
            if (c.isEmpty()) { throw new FinderException("Patient not found! PID:"+pid+",issuer:"+issuer); }
            if (c.size() > 1) { throw new FinderException("Patient ID[id="
                    + pid + ",issuer=" + issuer + " ambiguous"); }
            PatientLocal pat = (PatientLocal) c.iterator().next();
            patHome.remove(pat.getPk());
        } catch (EJBException e) {
            throw new RemoteException(e.getMessage());
        } catch (RemoveException e) {
            throw new RemoteException(e.getMessage());
        } catch (FinderException e) {
        throw new RemoteException(e.getMessage(),e);
    }
    }
    
}