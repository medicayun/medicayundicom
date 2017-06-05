/*
 *  Copyright (c) 2003 by TIANI MEDGRAPH AG                                  *
 *                                                                           *
 *  This file is part of dcm4che.                                            *
 *                                                                           *
 *  This library is free software; you can redistribute it and/or modify it  *
 *  under the terms of the GNU Lesser General Public License as published    *
 *  by the Free Software Foundation; either version 2 of the License, or     *
 *  (at your option) any later version.                                      *
 *                                                                           *
 *  This library is distributed in the hope that it will be useful, but      *
 *  WITHOUT ANY WARRANTY; without even the implied warranty of               *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        *
 *  Lesser General Public License for more details.                          *
 *                                                                           *
 *  You should have received a copy of the GNU Lesser General Public         *
 *  License along with this library; if not, write to the Free Software      *
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA  *
 */
package org.dcm4chex.arr.ejb.entity;
import java.sql.Timestamp;

import javax.ejb.CreateException;
import javax.ejb.EntityBean;
import javax.ejb.RemoveException;

import org.apache.log4j.Logger;

/**
 *  AuditRecord bean.
 *
 * @ejb:bean
 *  name="AuditRecord"
 * 	type="CMP"
 *  view-type="local"
 * 	primkey-field="pk"
 *  local-jndi-name="ejb/AuditRecord"
 * 
 * @ejb:transaction 
 *  type="Required"
 * 
 * @ejb.persistence
 *  table-name="audit_record"
 * 
 * @jboss.entity-command
 *  name="hsqldb-fetch-key"
 * 
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @author  <a href="mailto:joseph@tiani.com">joseph foraci</a>
 * @since  February 15, 2003
 * @version  $Revision: 4364 $ $Date: 2004-03-25 01:20:44 +0800 (周四, 25 3月 2004) $
 */
public abstract class AuditRecordBean implements EntityBean
{

    private final static Logger log = Logger.getLogger(AuditRecordBean.class);
 

    /**
     * @ejb:create-method
     * 
     * @param  type Description of the Parameter
     * @param  host Description of the Parameter
     * @param  ts Description of the Parameter
     * @param  xmldata Description of the Parameter
     * @return  Description of the Return Value
     * @exception  CreateException Description of the Exception
     */
    public Integer ejbCreate(String type, String host, Timestamp ts, String aet,
                             String userName, String patientName, String patientId,
                             String xmldata)
        throws CreateException
    {
        setType(type);
        setHost(host);
        setTimestamp(ts);
        setAet(aet);
        setUserName(userName);
        setPatientName(patientName);
        setPatientId(patientId);
        setXmlData(xmldata);
        return null;
    }


    /**
     *  Description of the Method
     *
     * @param  type Description of the Parameter
     * @param  host Description of the Parameter
     * @param  ts Description of the Parameter
     * @param  xmldata Description of the Parameter
     * @exception  CreateException Description of the Exception
     */
    public void ejbPostCreate(String type, String host, Timestamp ts, String xmldata)
        throws CreateException
    {
		if (log.isDebugEnabled()) {
			log.debug("create AuditRecord[pk=" + getPk() + "]");
		}
    }


    /**
     *  Description of the Method
     *
     * @exception  RemoveException Description of the Exception
     */
    public void ejbRemove()
        throws RemoveException
    {
        if (log.isDebugEnabled()) {
            log.debug("remove AuditRecord[pk=" + getPk() + "]");
        }
    }


	/**
	 * @ejb:pk-field
     * @ejb:persistence
     *  column-name="pk"
     * @jboss.persistence
     *  auto-increment="true"
	 */
	public abstract Integer getPk() ;
    
	public abstract void setPk(Integer pk) ;
    
    /**
     * @ejb:interface-method
     * @ejb:persistence
     *  column-name="msg_type"
     * 
     * @return  The type value
     */
    public abstract String getType();


    /**
     * @ejb:interface-method
     * 
     * @param  type The new type value
     */
    public abstract void setType(String type);


    /**
     * @ejb:interface-method
     * @ejb:persistence
     *  column-name="host_name"
     * 
     * @return  The host value
     */
    public abstract String getHost();


    /**
     * @param  host The new host value
     * 
     * @ejb:interface-method
     */
    public abstract void setHost(String host);


    /**
     * @ejb:interface-method
     * @ejb:persistence
     *  column-name="time_stamp"
     * 
     * @return  The timestamp value
     */
    public abstract Timestamp getTimestamp();


    /**
     * @param  ts The new timestamp value
     * @ejb:interface-method
     */
    public abstract void setTimestamp(Timestamp ts);


    /**
     * @return  The AET value
     * 
     * @ejb:interface-method
     * @ejb:persistence
     *  column-name="aet"
     */
    public abstract String getAet();


    /**
     * @param  aet The new AET value
     * 
     * @ejb:interface-method
     */
    public abstract void setAet(String aet);


    /**
     * @ejb:interface-method
     * @ejb:persistence
     *  column-name="user_name"
     * 
     * @return  The UserName value
     */
    public abstract String getUserName();


    /**
     * @param  userName The new UserName value
     * @ejb:interface-method
     */
    public abstract void setUserName(String userName);


    /**
     * @ejb:interface-method
     * @ejb:persistence
     *  column-name="patient_name"
     * 
     * @return  The PatientName value
     */
    public abstract String getPatientName();


    /**
     * @param  patientName The new PatientName value
     * 
     * @ejb:interface-method
     */
    public abstract void setPatientName(String patientName);


    /**
     * @ejb:interface-method
     * @ejb:persistence
     *  column-name="patient_id"
     * 
     * @return  The PatientId value
     */
    public abstract String getPatientId();


    /**
     * @param  patientId The new PatientId value
     * @ejb:interface-method
     */
    public abstract void setPatientId(String patientId);


    /**
     * @ejb:interface-method
     * @ejb:persistence
     *  column-name="xml_data"
     * 
     * @return  The xmlData value
     */
    public abstract String getXmlData();


    /**
     * @param  xmlData The new xmlData value
     * 
     * @ejb:interface-method
     */
    public abstract void setXmlData(String xmlData);
}

