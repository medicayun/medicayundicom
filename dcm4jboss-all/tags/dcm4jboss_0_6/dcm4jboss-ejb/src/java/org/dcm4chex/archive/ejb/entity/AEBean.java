/* $Id: AEBean.java 1023 2004-03-01 13:59:36Z umbertoc $
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

import org.apache.log4j.Logger;

/**
 * Application Entity bean.
 *
 * @author <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 * 
 * @ejb.bean
 *  name="AE"
 *  type="CMP"
 *  view-type="local"
 *  primkey-field="pk"
 *  local-jndi-name="ejb/AE"
 * 
 * @ejb.transaction 
 *  type="Required"
 * 
 * @ejb.persistence
 *  table-name="ae"
 * 
 * @jboss.entity-command
 *  name="hsqldb-fetch-key"
 * 
 * @ejb.finder
 *  signature="Collection findAll()"
 *  query="SELECT OBJECT(a) FROM AE AS a"
 *  transaction-type="Supports"
 * @jboss.query
 *  signature="Collection findAll()"
 *  strategy="on-find"
 *  eager-load-group="*"
*
 * @ejb.finder
 *  signature="org.dcm4chex.archive.ejb.interfaces.AELocal findByAET(java.lang.String aet)"
 *  query="SELECT OBJECT(a) FROM AE AS a WHERE a.title = ?1"
 *  transaction-type="Supports"
 * 
 */
public abstract class AEBean implements EntityBean
{

    private static final Logger log = Logger.getLogger(AEBean.class);

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

    /**
     * Application Entity Title
     *
     * @ejb.interface-method
     * @ejb.persistence
     *  column-name="aet"
     */
    public abstract String getTitle();
    
    /**
     * @ejb.interface-method
     */
    public abstract void setTitle(String title);

    /**
     * @ejb.interface-method
     * @ejb.persistence
     *  column-name="hostname"
     */
    public abstract String getHostName();
    
    /**
     * @ejb.interface-method
     */
    public abstract void setHostName(String name);

    /**
     * @ejb.interface-method
     * @ejb.persistence
     *  column-name="port"
     */
    public abstract int getPort();
    
    /**
     * @ejb.interface-method
     */
    public abstract void setPort(int port);

    /**
     * @ejb.interface-method
     * @ejb.persistence
     *  column-name="cipher_suites"
     */
    public abstract String getCipherSuites();

    /**
     * @ejb.interface-method
     */
    public abstract void setCipherSuites(String cipherSuites);

    /**
     * @ejb.create-method
     */
    public Integer ejbCreate(
        String title,
        String hostname,
        int port,
        String cipherSuites)
        throws CreateException
    {
        if (log.isDebugEnabled())
        {
            log.debug("create AEBean(" + title + ")");
        }
        setTitle(title);
        setHostName(hostname);
        setPort(port);
        setCipherSuites(cipherSuites);
        return null;
    }

    public void ejbPostCreate(
        String title,
        String host,
        int port,
        String cipherSuites)
        throws CreateException
    {}

    /**
     * @ejb.interface-method
     */
    public String asString()
    {
        StringBuffer sb = new StringBuffer(64);
        sb.append(getProtocol()).append("://")
          .append(getTitle()).append('@')
          .append(getHostName()).append(':').append(getPort());
        return sb.toString();
    }

    private String getProtocol() {
        String cipherSuites = getCipherSuites();
        if (cipherSuites == null || cipherSuites.length() == 0) {
            return "dicom";
        }
        if ("SSL_RSA_WITH_NULL_SHA".equals(cipherSuites)) {
            return "dicom-tls.nodes";
        }
        if ("SSL_RSA_WITH_3DES_EDE_CBC_SHA".equals(cipherSuites)) {
            return "dicom-tls.3des/";
        }
        return "dicom-tls";
    }
}
