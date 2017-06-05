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
 * Agfa-Gevaert Group.
 * Portions created by the Initial Developer are Copyright (C) 2003-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See @authors listed below.
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

package org.dcm4chex.archive.ejb.entity;

import java.sql.Timestamp;

import javax.ejb.CreateException;
import javax.ejb.EntityBean;
import javax.ejb.RemoveException;

import org.apache.log4j.Logger;
import org.dcm4chex.archive.ejb.interfaces.StudyLocal;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision: 3432 $ $Date: 2007-06-29 18:41:39 +0800 (周五, 29 6月 2007) $
 * @since Jun 29, 2007
 * 
 * @ejb.bean name="StudyPermission" type="CMP" view-type="local"
 *           local-jndi-name="ejb/StudyPermission" primkey-field="pk"
 * @ejb.persistence table-name="study_permission"
 * @ejb.transaction type="Required"
 * @jboss.entity-command name="hsqldb-fetch-key"
 * 
 */
public abstract class StudyPermissionBean implements EntityBean{

    private static final Logger log = Logger.getLogger(StudyPermissionBean.class);

    /**
     * Auto-generated Primary Key
     *
     * @ejb.interface-method
     * @ejb.pk-field
     * @ejb.persistence column-name="pk"
     * @jboss.persistence auto-increment="true"
     */
    public abstract Long getPk();
    public abstract void setPk(Long pk);
    
    /**
     * @ejb.interface-method
     * @ejb.persistence column-name="action"
     */
    public abstract String getAction();
    public abstract void setAction(String action);
    
    /**
     * @ejb.interface-method
     * @ejb.persistence column-name="valid_from"
     */
    public abstract java.sql.Timestamp getValidFrom();
    public abstract void setValidFrom(java.sql.Timestamp from);
    
    /**
     * @ejb.interface-method
     * @ejb.persistence column-name="valid_until"
     */
    public abstract java.sql.Timestamp getValidUntil();
    public abstract void setValidUntil(java.sql.Timestamp until);
    
    /**
     * @ejb.interface-method
     * @ejb.persistence column-name="roles"
     */
    public abstract String getRole();
    public abstract void setRole(String role);
    
    /**
     * @ejb.relation name="study-permission"
     *               role-name="permission-for-study"
     *               cascade-delete="yes"
     * @jboss.relation fk-column="study_fk" related-pk-field="pk"
     */
    public abstract StudyLocal getStudy();    

    /**
     * @ejb.interface-method
     */
    public abstract void setStudy(StudyLocal Study);

    /**
     * @ejb.create-method
     */
    public Long ejbCreate(String action, String role, Timestamp validFrom,
            Timestamp validUntil, StudyLocal study)
            throws CreateException {
        setAction(action);
        setRole(role);
        setValidFrom(validFrom);
        setValidUntil(validUntil);
       return null;
    }
    
    public void ejbPostCreate(String action, String role, Timestamp validFrom,
            Timestamp validUntil, StudyLocal study) throws CreateException {
        setStudy(study);
        log.info("Created " + prompt());
    }

    public void ejbRemove() throws RemoveException {
        log.info("Deleting " + prompt());
    }

    
    private String prompt() {
        return "StudyPermission[pk=" + getPk() 
        + ", study-iuid=" + studyIuid()
        + ", action=" + getAction()
        + ", role=" + getRole()
        + "]";
    }

    private String studyIuid() {
        StudyLocal study = getStudy();
        return study != null ? study.getStudyIuid() : "null";
    }
}
