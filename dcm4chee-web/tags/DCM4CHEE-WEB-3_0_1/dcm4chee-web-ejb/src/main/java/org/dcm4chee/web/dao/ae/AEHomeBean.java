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
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa-Gevaert AG.
 * Portions created by the Initial Developer are Copyright (C) 2008
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See listed authors below.
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
package org.dcm4chee.web.dao.ae;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.dcm4chee.archive.entity.AE;
import org.dcm4chee.usr.dao.UserAccess;
import org.dcm4chee.usr.util.JNDIUtils;
import org.jboss.annotation.ejb.LocalBinding;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Jan 5, 2008
 */
@Stateless
@LocalBinding (jndiBinding=AEHomeLocal.JNDI_NAME)
public class AEHomeBean implements AEHomeLocal {

    @PersistenceContext(unitName="dcm4chee-arc")
    private EntityManager em;

    @SuppressWarnings("unchecked")
    public List<String> listAETitles() {
        return em.createQuery("SELECT ae.title FROM AE ae ORDER BY ae.title")
                .getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<AE> findAll(String filter) {
        String filterQuery = filter == null ? "" : 
            "<NONE>".equals(filter) ? "WHERE aeGroup IS NULL" : "WHERE aeGroup = :filter";
        Query query = em.createQuery("FROM AE ae " + filterQuery + " ORDER BY ae.title, ae.aeGroup");
        if (filter != null && !"<NONE>".equals(filter)) query.setParameter("filter", filter);
        List<AE> l = query.getResultList();
        em.clear();
        return l;
    }

    public AE findByTitle(String title) {
        Query q = em.createNamedQuery("AE.findByTitle");
        q.setParameter("title", title);
        return  (AE) q.getSingleResult();
    }
    
    public AE updateOrCreateAET(AE ae){
        if ( ae.getPk() == -1) {
            em.persist(ae);
            return null;
        } else {
            AE oldAE = em.find(AE.class, ae.getPk());
            String oldAETitle = oldAE.getTitle();
            em.merge(ae);
            ((UserAccess) JNDIUtils.lookup(UserAccess.JNDI_NAME))
                .updateAETInAETGroups(oldAETitle, ae.getTitle());
            return oldAE;
        }
    }

    public void removeAET(long pk) {
        AE ae = em.find(AE.class, pk);
        em.remove(ae);
        ((UserAccess) JNDIUtils.lookup(UserAccess.JNDI_NAME))
            .removeAETFromAETGroups(ae.getTitle());
    }
    
    @SuppressWarnings("unchecked")
    public List<String> listAeTypes() {
        return em.createQuery("SELECT DISTINCT ae.aeGroup FROM AE ae ORDER BY ae.aeGroup").getResultList();
    }

}
