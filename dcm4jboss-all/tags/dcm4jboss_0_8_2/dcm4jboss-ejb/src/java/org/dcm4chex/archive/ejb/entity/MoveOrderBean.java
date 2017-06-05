/* $Id: MoveOrderBean.java 1149 2004-08-06 07:50:10Z gunterze $
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

import javax.ejb.CreateException;
import javax.ejb.EntityBean;
import javax.ejb.RemoveException;

import org.apache.log4j.Logger;
import org.dcm4chex.archive.ejb.interfaces.MoveOrderValue;

/**
 * 
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1149 $ $Date: 2004-08-06 15:50:10 +0800 (周五, 06 8月 2004) $
 * @since 08.12.2003
 * 
 * @ejb.bean
 *  name="MoveOrder"
 *  type="CMP"
 *  view-type="local"
 *  primkey-field="pk"
 *  local-jndi-name="ejb/MoveOrder"
 * 
 * @ejb.persistence
 *  table-name="move_order"
 * 
 * @jboss.entity-command
 *  name="hsqldb-fetch-key"
 * 
 * @ejb.finder
 *  signature="java.util.Collection findAll()"
 *  query="SELECT OBJECT(a) FROM MoveOrder AS a"
 *
 * @ejb.finder
 *  signature="java.util.Collection findBefore(java.sql.Timestamp scheduledTime)"
 *  query=""
 * @jboss.query
 *  signature="java.util.Collection findBefore(java.sql.Timestamp scheduledTime)"
 *  query="SELECT OBJECT(a) FROM MoveOrder AS a WHERE a.scheduledTime IS NOT NULL AND a.scheduledTime < ?1 ORDER BY a.priority DESC, a.scheduledTime LIMIT 1"
 *  strategy="on-load"
 *  page-size="1"
 *  eager-load-group="*"
 */
public abstract class MoveOrderBean implements EntityBean {

    private static final Logger log = Logger.getLogger(MoveOrderBean.class);

    /**
     * Auto-generated Primary Key
     *
     * @ejb.interface-method
     * @ejb.persistence
     *  column-name="pk"
     * @jboss.persistence
     *  auto-increment="true"
     */
    public abstract Integer getPk();

    public abstract void setPk(Integer pk);

    /**
     * Scheduled Time
     *
     * @ejb.persistence
     *  column-name="scheduled_time"
     * @ejb.interface-method
     */
    public abstract java.sql.Timestamp getScheduledTime();

    /**
     * @ejb.interface-method
     */
    public abstract void setScheduledTime(java.sql.Timestamp time);

    /**
     * Priority
     *
     * @ejb.persistence
     *  column-name="priority"
     * @ejb.interface-method
     */
    public abstract int getPriority();

    /**
     * @ejb.interface-method
     */
    public abstract void setPriority(int priority);

    /**
    * Retrieve AET
    *
    * @ejb.persistence
    *  column-name="retrieve_aet"
    * @ejb.interface-method
    */
    public abstract String getRetrieveAET();

    /**
     * @ejb.interface-method
     */
    public abstract void setRetrieveAET(String aets);

    /**
     * Move Destination
     *
     * @ejb.persistence
     *  column-name="move_dest"
     * @ejb.interface-method
     */
    public abstract String getMoveDestination();

    /**
     * @ejb.interface-method
     */
    public abstract void setMoveDestination(String destination);

    /**
     * Patient ID
     *
     * @ejb.persistence
     *  column-name="pat_id"
     * @ejb.interface-method
     */
    public abstract String getPatientId();

    /**
     * @ejb.interface-method
     */
    public abstract void setPatientId(String iuids);

    /**
     * Study Instance UIDs
     *
     * @ejb.persistence
     *  column-name="study_iuids"
     * @ejb.interface-method
     */
    public abstract String getStudyIuids();

    /**
     * @ejb.interface-method
     */
    public abstract void setStudyIuids(String iuids);

    /**
     * Series Instance UIDs
     *
     * @ejb.persistence
     *  column-name="series_iuids"
     * 
     * @ejb.interface-method
     */
    public abstract String getSeriesIuids();

    /**
     * @ejb.interface-method
     */
    public abstract void setSeriesIuids(String iuids);

    /**
     * SOP Instance UIDs
     *
     * @ejb.persistence
     *  column-name="sop_iuids"
     * @ejb.interface-method
     */
    public abstract String getSopIuids();

    /**
     * @ejb.interface-method
     */
    public abstract void setSopIuids(String iuids);

    /**
     * Failure Count
     *
     * @ejb.persistence
     *  column-name="failure_count"
     * @ejb.interface-method
     */
    public abstract int getFailureCount();

    /**
     * @ejb.interface-method
     */
    public abstract void setFailureCount(int count);

    /**
     * Failure Count
     *
     * @ejb.persistence
     *  column-name="failure_status"
     * @ejb.interface-method
     */
    public abstract int getFailureStatus();

    /**
     * @ejb.interface-method
     */
    public abstract void setFailureStatus(int count);

    /**
     * @ejb.interface-method
     * @jboss.method-attributes
     *   read-only=true
     */
    public MoveOrderValue getMoveOrderValue() {
        MoveOrderValue retval =
            new org.dcm4chex.archive.ejb.interfaces.MoveOrderValue();
        retval.setScheduledTime(getScheduledTime());
        retval.setPriority(getPriority());
        retval.setRetrieveAET(getRetrieveAET());
        retval.setMoveDestination(getMoveDestination());
        retval.setPatientId(getPatientId());
        retval.setStudyIuids(getStudyIuids());
        retval.setSeriesIuids(getSeriesIuids());
        retval.setSopIuids(getSopIuids());
        retval.setFailureCount(getFailureCount());
        retval.setFailureStatus(getFailureStatus());
        return retval;
    }

    /**
     * Create Move Order.
     *
     * @ejb.create-method
     */
    public Integer ejbCreate(MoveOrderValue val) throws CreateException {

        setScheduledTime(val.getScheduledTime());
        setPriority(val.getPriority());
        setRetrieveAET(val.getRetrieveAET());
        setMoveDestination(val.getMoveDestination());
        setPatientId(val.getPatientId());
        setStudyIuids(val.getStudyIuids());
        setSeriesIuids(val.getSeriesIuids());
        setSopIuids(val.getSopIuids());
        setFailureCount(val.getFailureCount());
        setFailureStatus(val.getFailureStatus());
        return null;
    }

    public void ejbPostCreate(MoveOrderValue val) throws CreateException {
        log.info("Created " + getMoveOrderValue());
    }

    public void ejbRemove() throws RemoveException {
        log.info("Deleting " + getMoveOrderValue());
    }

}
