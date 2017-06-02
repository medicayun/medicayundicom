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
 * Gunter Zeilinger, Huetteldorferstr. 24/10, 1150 Vienna/Austria/Europe.
 * Portions created by the Initial Developer are Copyright (C) 2002-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunterze@gmail.com>
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

package org.dcm4chee.arr.ejb;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision$ $Date$
 * @since Jun 13, 2006
 *
 */
@Entity
@Table(name = "part_obj")
public class ParticipantObject implements Serializable {

    private static final long serialVersionUID = 3794144950916480382L;

    private static Logger log = LoggerFactory.getLogger(ParticipantObject.class);
    
    private int pk;
    private AuditRecord auditRecord;
    private String objectID;
    private int objectType;
    private int objectRole;
    private int dataLifeCycle;
    private String objectSensitivity;
    private int objectIDTypeRFC;
    private Code objectIDType;
    private String objectName;
    
    @Id
    @GeneratedValue(generator = "hibseq")
    @GenericGenerator(name = "hibseq", strategy = "seqhilo", parameters = {
            @Parameter(name = "max_lo", value = "100"),
            @Parameter(name = "sequence", value = "part_obj_pk_seq") })
    @Column(name="pk")
    public int getPk() {
        return pk;
    }
    
    public void setPk(int pk) {
        this.pk = pk;
    }

    @ManyToOne
    @JoinColumn(name="audit_record_fk")
    public AuditRecord getAuditRecord() {
        return auditRecord;
    }

    public void setAuditRecord(AuditRecord auditRecord) {
        this.auditRecord = auditRecord;
    }

    @ManyToOne
    @JoinColumn(name="obj_id_type_fk")
    public Code getObjectIDType() {
        return objectIDType;
    }

    public void setObjectIDType(Code code) {
        this.objectIDType = code;
    }

    @Column(name="obj_id_type_rfc")
    public int getObjectIDTypeRFC() {
        return objectIDTypeRFC;
    }

    public void setObjectIDTypeRFC(int code) {
        this.objectIDTypeRFC = code;
    }

    @Column(name="name")
    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String name) {
        this.objectName = name;
    }

    @Column(name="obj_id")
    public String getObjectID() {
        return objectID;
    }

    public void setObjectID(String id) {
        this.objectID = id;
    }

    @Column(name="obj_type")
    public int getObjectType() {
        return objectType;
    }

    public void setObjectType(int code) {
        this.objectType = code;
    }

    @Column(name="obj_role")
    public int getObjectRole() {
        return objectRole;
    }

    public void setObjectRole(int code) {
        this.objectRole = code;
    }

    @Column(name="data_life_cycle")
    public int getDataLifeCycle() {
        return dataLifeCycle;
    }

    public void setDataLifeCycle(int code) {
        this.dataLifeCycle = code;
    }

    @Column(name="obj_sensitivity")
    public String getObjectSensitivity() {
        return objectSensitivity;
    }

    public void setObjectSensitivity(String sensitivity) {
        this.objectSensitivity = sensitivity;
    }

    @PostPersist
    public void postPersit() {
        if (log.isDebugEnabled())
            log.debug("Created " + this.toString());
    }

    @PostRemove
    public void postRemove() {
        if (log.isDebugEnabled())
            log.debug("Removed " + this.toString());
    }

    @Override
    public String toString() {
        return "[" + this.getClass().getSimpleName() + "[pk=" + pk + "]";
    }
}
