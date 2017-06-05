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
package org.dcm4chex.arr.ejb.session;

import java.sql.Timestamp;
import java.util.Date;
import javax.ejb.CreateException;
import javax.ejb.EJBException;

import javax.ejb.SessionBean;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import org.dcm4chex.arr.ejb.entity.AuditRecordLocalHome;

/**
 *  StoreAuditRecord bean.
 *
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @author  <a href="mailto:joseph@tiani.com">joseph foraci</a>
 * @created  February 15, 2003
 * @version  $Revision: 4366 $ $Date: 2004-03-25 06:13:09 +0800 (周四, 25 3月 2004) $
 * @ejb:bean  type="Stateless" name="StoreAuditRecord" view-type="local"
 *      local-jndi-name="ejb/StoreAuditRecord"
 * @ejb:transaction  type="Required"
 * @ejb:transaction-type  type="Container"
 * @ejb:ejb-ref  ejb-name="AuditRecord" view-type="local" ref-name="ejb/AuditRecord"
 */
public abstract class StoreAuditRecordBean implements SessionBean
{
    private final static Logger log = Logger.getLogger(StoreAuditRecordBean.class);
    private AuditRecordLocalHome home = null;
    
    private AuditRecordLocalHome getAuditRecordHome() {
    	if (home == null) {
			try {
				Context ctx = new InitialContext();
				home = (AuditRecordLocalHome) ctx.lookup("java:comp/env/ejb/AuditRecord");
			} catch (NamingException e) {
				log.error("Failed lookup ns:", e);
				throw new EJBException(e);
			}
    	}
    	return home;
	}

    /**
     * @param  xmldata Description of the Parameter
     * @ejb:interface-method
     */
    public void store(String xmldata)
    {
        Timestamp ts = null;
        String type, host, aet, userName, patientName, patientId;
        
        type = host = aet = userName = patientName = patientId = null;
        // parse xmldata for audit log
        ArrMsgParser msgParser;
        try {
            msgParser = new ArrMsgParserImpl(false); //non-validating
        	int retcode = msgParser.parse(xmldata);
            //if fields are missing: type, host, etc can display null, but for
            // timestamp we should generate the current time
        	type = msgParser.getType();
        	host = msgParser.getHost();
            aet = msgParser.getAet();
            userName = msgParser.getUserName();
            patientName = msgParser.getPatientName();
            patientId = msgParser.getPatientId();
            Date date = msgParser.getTimeStamp();
            if (date == null)
                date = new Date();
        	ts = new Timestamp(date.getTime());
        }
        catch (ArrInputException e) {
            log.error("ArrInputException: " + e);
            if (e.getCause() != null)
                log.error("  Cause: " + e.getCause());
        }
        try {
            log.info("Store AuditRecord[type=" + type
                     + ", host=" + host
                     + ", timestamp=" + ts
                     + "]");
            getAuditRecordHome().create(type, host, ts, aet, userName, patientName, patientId, xmldata);
        } catch (CreateException e) {
            log.error("Failed to insert new AuditRecord[" + type + "] in DB:", e);
            throw new EJBException(e);
        }
    }
}

