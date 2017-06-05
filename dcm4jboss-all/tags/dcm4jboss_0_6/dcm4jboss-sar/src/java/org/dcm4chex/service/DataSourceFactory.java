/* $Id: DataSourceFactory.java 900 2003-12-18 00:42:53Z gunterze $
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

package org.dcm4chex.service;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.dcm4chex.service.util.ConfigurationException;
import org.jboss.logging.Logger;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision: 900 $ $Date: 2003-12-18 08:42:53 +0800 (周四, 18 12月 2003) $
 * @since 22.11.2003
 */
class DataSourceFactory
{

    private final Logger log;
    private String jndiName;
    private DataSource datasource;

    public DataSourceFactory(Logger log)
    {
        this.log = log;
    }

    public String getJNDIName()
    {
        return jndiName;
    }

    public void setJNDIName(String jndiName)
    {
        this.jndiName = jndiName;
        
    }

    public DataSource getDataSource() throws ConfigurationException
    {
        if (datasource == null) {
            try {
                Context jndiCtx = new InitialContext();
                try {
                    datasource = (DataSource) jndiCtx.lookup(jndiName);
                } finally {
                    try {
                        jndiCtx.close();
                    } catch (NamingException ignore) {} 
                }
            } catch (NamingException ne) {
                throw new ConfigurationException("Failed to access Data Source: " + jndiName, ne);
            } 
        }
        return datasource;
    }
}
