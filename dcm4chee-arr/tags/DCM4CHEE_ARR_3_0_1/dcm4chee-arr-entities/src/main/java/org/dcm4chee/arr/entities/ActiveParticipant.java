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
 * Portions created by the Initial Developer are Copyright (C) 2002-2005
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

package org.dcm4chee.arr.entities;

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

import org.apache.log4j.Logger;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Jan 23, 2007
 */
@Entity
@Table(name = "active_part")
public class ActiveParticipant implements Serializable {

    private static final long serialVersionUID = 513457139488147710L;

    private static final Logger log = Logger.getLogger(ActiveParticipant.class);
    
    private int pk;

    private AuditRecord auditRecord;

    private String userID;

    private String alternativeUserID;

    private String userName;

    private boolean userIsRequestor;

    private String networkAccessPointID;

    private int networkAccessPointType;

    private Code roleID;

    @Id
    @GeneratedValue(generator = "hibseq")
    @GenericGenerator(name = "hibseq", strategy = "seqhilo", parameters = {
            @Parameter(name = "max_lo", value = "100"),
            @Parameter(name = "sequence", value = "active_part_pk_seq") })
    @Column(name = "pk")
    public int getPk() {
        return pk;
    }

    public void setPk(int pk) {
        this.pk = pk;
    }

    @ManyToOne
    @JoinColumn(name = "audit_record_fk")
    public AuditRecord getAuditRecord() {
        return auditRecord;
    }

    public void setAuditRecord(AuditRecord auditRecord) {
        this.auditRecord = auditRecord;
    }

    @ManyToOne
    @JoinColumn(name = "role_id_fk")
    public Code getRoleID() {
        return roleID;
    }

    public void setRoleID(Code roleID) {
        this.roleID = roleID;
    }

    @Column(name = "user_id")
    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    @Column(name = "alt_user_id")
    public String getAlternativeUserID() {
        return alternativeUserID;
    }

    public void setAlternativeUserID(String alternativeUserID) {
        this.alternativeUserID = alternativeUserID;
    }

    @Column(name = "user_name")
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Column(name = "requestor")
    public boolean getUserIsRequestor() {
        return userIsRequestor;
    }

    public void setUserIsRequestor(boolean userIsRequestor) {
        this.userIsRequestor = userIsRequestor;
    }

    @Column(name = "net_access_pt_id")
    public String getNetworkAccessPointID() {
        return networkAccessPointID;
    }

    public void setNetworkAccessPointID(String id) {
        this.networkAccessPointID = id;
    }

    @Column(name = "net_access_pt_type")
    public int getNetworkAccessPointType() {
        return networkAccessPointType;
    }

    public void setNetworkAccessPointType(int code) {
        this.networkAccessPointType = code;
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

    
    public String toString() {
        return "ActiveParticipant[pk=" + pk + ", id=" + userID + "]";
    }
    
}
