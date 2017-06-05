/* $Id: MWLItemBean.java 1145 2004-08-01 12:19:37Z gunterze $
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
package org.dcm4chex.archive.ejb.entity;

import java.rmi.RemoteException;
import java.text.SimpleDateFormat;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.RemoveException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmDecodeParam;
import org.dcm4che.dict.Tags;
import org.dcm4cheri.util.DatasetUtils;

/**
 * @ejb.bean
 *  name="MWLItem"
 *  type="CMP"
 *  view-type="local"
 *  primkey-field="pk"
 *  local-jndi-name="ejb/MWLItem"
 * 
 * @ejb.transaction 
 *  type="Required"
 * 
 * @ejb.persistence
 *  table-name="mwl_item"
 * 
 * @jboss.entity-command
 *  name="hsqldb-fetch-key"
 * 
 * @ejb.env-entry
 *  name="SpsIdPrefix"
 *  type="java.lang.String"
 *  value="" 
 *
 * @ejb.finder
 *  signature="Collection findAll()"
 *  query="SELECT OBJECT(a) FROM MWLItem AS a"
 *  transaction-type="Supports"
 * 
 * @ejb.finder
 *  signature="org.dcm4chex.archive.ejb.interfaces.MWLItemLocal findBySpsId(java.lang.String id)"
 *  query="SELECT OBJECT(a) FROM MWLItem AS a WHERE a.spsId = ?1"
 *  transaction-type="Supports"
 *
 * @author <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 *
 */
public abstract class MWLItemBean implements EntityBean {
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    private static final Logger log = Logger.getLogger(MWLItemBean.class);
    private String spsIdPrefix;

    public void setEntityContext(EntityContext arg0)
        throws EJBException, RemoteException {
        Context jndiCtx = null;
        try {
            jndiCtx = new InitialContext();
            spsIdPrefix = (String) jndiCtx.lookup("java:comp/env/SpsIdPrefix");
        } catch (NamingException e) {
            throw new EJBException(e);
        } finally {
            if (jndiCtx != null) {
                try {
                    jndiCtx.close();
                } catch (NamingException ignore) {}
            }
        }
    }

    /**
     * Auto-generated Primary Key
     *
     * @ejb.interface-method
     * @ejb.pk-field
     * @ejb.persistence
     *  column-name="pk"
     * @jboss.persistence
     *  auto-increment="true"
     *
     */
    public abstract Integer getPk();

    public abstract void setPk(Integer pk);

    /**
     * SPS ID
     *
     * @ejb.interface-method
     * @ejb.persistence
     *  column-name="sps_id"
     */
    public abstract String getSpsId();

    public abstract void setSpsId(String spsId);

    /**
     * SPS Start Datetime
     *
     * @ejb.interface-method
     * @ejb.persistence
     *  column-name="start_datetime"
     */
    public abstract java.sql.Timestamp getSpsStartDateTime();

    public abstract void setSpsStartDateTime(java.sql.Timestamp dateTime);

    /**
     * Station AET
     *
     * @ejb.interface-method
     * @ejb.persistence
     *  column-name="station_aet"
     */
    public abstract String getScheduledStationAET();

    public abstract void setScheduledStationAET(String aet);

    /**
     * Modality
     *
     * @ejb.interface-method
     * @ejb.persistence
     *  column-name="modality"
     */
    public abstract String getModality();

    public abstract void setModality(String md);

    /**
     * Performing Physician
     *
     * @ejb.interface-method
     * @ejb.persistence
     *  column-name="perf_physician"
     */
    public abstract String getPerformingPhysicianName();

    public abstract void setPerformingPhysicianName(String physician);

    /**
     * Requested Procedure ID
     *
     * @ejb.interface-method
     * @ejb.persistence
     *  column-name="req_proc_id"
     */
    public abstract String getRequestedProcedureId();

    public abstract void setRequestedProcedureId(String id);

    /**
     * Accession Number
     *
     * @ejb.interface-method
     * @ejb.persistence
     *  column-name="accession_no"
     */
    public abstract String getAccessionNumber();

    public abstract void setAccessionNumber(String no);

    /**
     * Patient ID
     *
     * @ejb.interface-method
     * @ejb.persistence
     *  column-name="pat_id"
     */
    public abstract String getPatientId();

    public abstract void setPatientId(String pid);

    /**
     * Patient Name
     *
     * @ejb.interface-method
     * @ejb.persistence
     *  column-name="pat_name"
     */
    public abstract String getPatientName();

    /**
     *
     * @param name
     */
    public abstract void setPatientName(String name);

    /**
     * MWL Item DICOM Attributes
     *
     * @ejb.persistence
     *  column-name="item_attrs"
     * 
     */
    public abstract byte[] getEncodedAttributes();

    public abstract void setEncodedAttributes(byte[] bytes);

    /**
     * Create MWLItem.
     *
     * @ejb.create-method
     */
    public Integer ejbCreate(Dataset ds) throws CreateException {
        setAttributes(ds);
        return null;
    }

    public void ejbPostCreate(Dataset ds) throws CreateException {
        if (getSpsId().length() == 0) {
            String id = spsIdPrefix + getPk();
            setSpsId(id);
            log.debug("generate SPS ID from pk - " + prompt());
            ds.getItem(Tags.SPSSeq).putCS(Tags.SPSID, id);
        }
        setEncodedAttributes(
            DatasetUtils.toByteArray(ds, DcmDecodeParam.EVR_LE));
        log.info("Created " + prompt());
    }

    public void ejbRemove() throws RemoveException {
        log.info("Deleting " + prompt());
    }

    /**
     * @ejb.interface-method
     */
    public Dataset getAttributes() {
        return DatasetUtils.fromByteArray(
            getEncodedAttributes(),
            DcmDecodeParam.EVR_LE);
    }

    /**
     * @ejb.interface-method
     */
    public void setAttributes(Dataset ds) {
        Dataset spsItem = ds.getItem(Tags.SPSSeq);
        if (spsItem == null) {
            throw new IllegalArgumentException("Missing Scheduled Procedure Step Sequence (0040,0100) Item");
        }
        setSpsId(spsItem.getString(Tags.SPSID, ""));
        setSpsStartDateTime(
            spsItem.getDateTime(Tags.SPSStartDate, Tags.SPSStartTime));
        setScheduledStationAET(spsItem.getString(Tags.ScheduledStationAET));
        setPerformingPhysicianName(
            spsItem.getString(Tags.PerformingPhysicianName));
        setModality(spsItem.getString(Tags.Modality));
        setRequestedProcedureId(ds.getString(Tags.RequestedProcedureID));
        setAccessionNumber(ds.getString(Tags.AccessionNumber));
        setPatientId(ds.getString(Tags.PatientID));
        setPatientName(ds.getString(Tags.PatientName));
    }

    /**
     * @ejb.interface-method
     */
    public void setSpsStartDateTime(java.util.Date date) {
        setSpsStartDateTime(date != null ? new java.sql.Timestamp(date.getTime()) : null);
    }

    /**
     * 
     * @ejb.interface-method
     */
    public String asString() {
        return prompt();
    }

    private String prompt() {
        java.sql.Timestamp spsDT = getSpsStartDateTime();
        return "MWLItem[pk="
            + getPk()
            + ", spsId="
            + getSpsId()
            + ", spsStartDateTime="
            + (spsDT != null ? new SimpleDateFormat(DATE_FORMAT).format(spsDT) : "")
            + ", stationAET="
            + getScheduledStationAET()
            + ", rqProcId="
            + getRequestedProcedureId()
            + ", modality="
            + getModality()
            + ", accessionNo="
            + getAccessionNumber()
            + ", patId="
            + getPatientId()
            + ", patName="
            + getPatientName()
            + "]";
    }

}
