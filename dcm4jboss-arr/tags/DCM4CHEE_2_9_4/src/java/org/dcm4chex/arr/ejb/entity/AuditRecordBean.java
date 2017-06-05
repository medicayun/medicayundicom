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
 * Joe Foraci <jforaci@users.sourceforge.net>
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
 * @version  $Revision: 4422 $ $Date: 2005-10-07 03:47:54 +0800 (周五, 07 10月 2005) $
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

