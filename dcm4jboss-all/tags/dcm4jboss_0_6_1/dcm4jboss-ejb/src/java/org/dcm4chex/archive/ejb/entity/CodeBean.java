/* $Id: CodeBean.java 929 2004-01-19 22:06:17Z gunterze $
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

/**
 * @ejb.bean
 *  name="Code"
 *  type="CMP"
 *  view-type="local"
 *  primkey-field="pk"
 *  local-jndi-name="ejb/Code"
 * 
 * @jboss.container-configuration
 *  name="Standard CMP 2.x EntityBean with cache invalidation"
 *  
 * @ejb.transaction 
 *  type="Required"
 * 
 * @ejb.persistence
 *  table-name="code"
 * 
 * @jboss.entity-command
 *  name="hsqldb-fetch-key"
 * 
 * @ejb.finder
 *  signature="Collection findAll()"
 *  query="SELECT OBJECT(a) FROM Code AS a"
 *  transaction-type="Supports"
 *
 * @ejb.finder
 *  signature="java.util.Collection findByValueAndDesignator(java.lang.String value, java.lang.String designator)"
 *  query="SELECT OBJECT(a) FROM Code AS a WHERE a.codeValue = ?1 AND a.codingSchemeDesignator = ?2"
 *  transaction-type="Supports"
 * 
 * @author <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 *
 */
public abstract class CodeBean implements EntityBean
{
    private static final Logger log = Logger.getLogger(CodeBean.class);
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
     * Code Value
     *
     * @ejb.interface-method
     * @ejb.persistence
     *  column-name="code_value"
     */
    public abstract String getCodeValue();

    public abstract void setCodeValue(String value);

    /**
     * Code Value
     *
     * @ejb.interface-method
     * @ejb.persistence
     *  column-name="code_designator"
     */
    public abstract String getCodingSchemeDesignator();

    public abstract void setCodingSchemeDesignator(String designator);

    /**
     * Code Value
     *
     * @ejb.interface-method
     * @ejb.persistence
     *  column-name="code_version"
     */
    public abstract String getCodingSchemeVersion();

    public abstract void setCodingSchemeVersion(String version);

    /**
     * Code Value
     *
     * @ejb.interface-method
     * @ejb.persistence
     *  column-name="code_meaning"
     */
    public abstract String getCodeMeaning();

    public abstract void setCodeMeaning(String meaning);

    /**
     * 
     * @ejb.interface-method
     */
    public String asString()
    {
        return prompt();
    }

    private String prompt()
    {
        return "Code[pk="
            + getPk()
            + ", value="
            + getCodeValue()
            + ", designator="
            + getCodingSchemeDesignator()
            + ", version="
            + getCodingSchemeVersion()
            + ", meaning="
            + getCodeMeaning()
            + "]";
    }

    /**
     * Create Media.
     *
     * @ejb.create-method
     */
    public Integer ejbCreate(
        String value,
        String designator,
        String version,
        String meaning)
        throws CreateException
    {
        setCodeValue(value);
        setCodingSchemeDesignator(designator);
        setCodingSchemeVersion(version);
        setCodeMeaning(meaning);
        return null;
    }

    public void ejbPostCreate(
        String value,
        String designator,
        String version,
        String meaning)
        throws CreateException
    {
        log.info("Created " + prompt());

    }

    public void ejbRemove() throws RemoveException
    {
        log.info("Deleting " + prompt());
    }
}
