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

package org.dcm4chee.arr.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Jan 23, 2007
 */
@Entity
@Table(name = "audit_record")
public class AuditRecord implements Serializable {

    private static final long serialVersionUID = -4138396955398529612L;

    private static final Logger log = 
            LoggerFactory.getLogger(AuditRecord.class);

    private int pk;

    private Code eventID;

    private String eventAction;

    private int eventOutcome;

    private Date eventDateTime;

    private Date receiveDateTime;

    private Code eventType;

    private String enterpriseSiteID;

    private String sourceID;

    private int sourceType;

    private Collection<ActiveParticipant> activeParticipant;

    private Collection<ParticipantObject> participantObject;

    private boolean iheYr4;

    private byte[] xmldata;

    @Id
    @GeneratedValue(generator = "hibseq")
    @GenericGenerator(name = "hibseq", strategy = "seqhilo", parameters = {
            @Parameter(name = "max_lo", value = "100"),
            @Parameter(name = "sequence", value = "audit_record_pk_seq") })
    @Column(name = "pk")
    public int getPk() {
        return pk;
    }

    public void setPk(int pk) {
        this.pk = pk;
    }

    @ManyToOne
    @JoinColumn(name = "event_id_fk")
    public Code getEventID() {
        return eventID;
    }

    public void setEventID(Code eventID) {
        this.eventID = eventID;
    }

    @ManyToOne
    @JoinColumn(name = "event_type_fk")
    public Code getEventType() {
        return eventType;
    }

    public void setEventType(Code eventTypeCode) {
        this.eventType = eventTypeCode;
    }

    @Column(name = "site_id")
    public String getEnterpriseSiteID() {
        return enterpriseSiteID;
    }

    public void setEnterpriseSiteID(String auditEnterpriseSiteID) {
        this.enterpriseSiteID = auditEnterpriseSiteID;
    }

    @Column(name = "source_id")
    public String getSourceID() {
        return sourceID;
    }

    public void setSourceID(String auditSourceID) {
        this.sourceID = auditSourceID;
    }

    @Column(name = "source_type")
    public int getSourceType() {
        return sourceType;
    }

    public void setSourceType(int typeCode) {
        this.sourceType = typeCode;
    }

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "auditRecord")
    public Collection<ActiveParticipant> getActiveParticipant() {
        return activeParticipant;
    }

    public void setActiveParticipant(Collection<ActiveParticipant> c) {
        this.activeParticipant = c;
    }

    public void addActiveParticipant(ActiveParticipant ap) {
        if (activeParticipant == null) {
            activeParticipant = new ArrayList<ActiveParticipant>(3);
        }
        activeParticipant.add(ap);
        ap.setAuditRecord(this);
    }

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "auditRecord")
    public Collection<ParticipantObject> getParticipantObject() {
        return participantObject;
    }

    public void setParticipantObject(Collection<ParticipantObject> c) {
        this.participantObject = c;
    }

    public void addParticipantObject(ParticipantObject po) {
        if (participantObject == null) {
            participantObject = new ArrayList<ParticipantObject>(3);
        }
        participantObject.add(po);
        po.setAuditRecord(this);
    }

    @Column(name = "event_action")
    public String getEventAction() {
        return eventAction;
    }

    public void setEventAction(String eventActionCode) {
        this.eventAction = eventActionCode;
    }

    @Column(name = "event_outcome")
    public int getEventOutcome() {
        return eventOutcome;
    }

    public void setEventOutcome(int eventOutcomeIndicator) {
        this.eventOutcome = eventOutcomeIndicator;
    }

    @Basic
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "event_date_time")
    @Index(name = "ar_event_date_time")
    public Date getEventDateTime() {
        return eventDateTime;
    }

    public void setEventDateTime(Date dt) {
        this.eventDateTime = dt;
    }

    @Basic
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "receive_date_time")
    @Index(name = "ar_receive_date_ti")
    public Date getReceiveDateTime() {
        return receiveDateTime;
    }

    public void setReceiveDateTime(Date dt) {
        this.receiveDateTime = dt;
    }

    @Column(name = "iheyr4")
    public boolean isIHEYr4() {
        return iheYr4;
    }

    public void setIHEYr4(boolean iheYr4) {
        this.iheYr4 = iheYr4;
    }

    @Lob
    @Column(name = "xmldata", length = 262144)
    public byte[] getXmldata() {
        return xmldata;
    }

    public void setXmldata(byte[] xmldata) {
        this.xmldata = xmldata;
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
        return "AuditRecord[pk=" + pk + ", eventDateTime=" + eventDateTime
                + ", receiveDateTime=" + receiveDateTime + ", sourceID="
                + sourceID + "]";
    }
}
