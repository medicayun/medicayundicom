/* $Id: PurgeFilesService.java 1004 2004-02-21 22:32:43Z gunterze $
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

import java.io.File;

import org.dcm4chex.archive.ejb.interfaces.FileDTO;
import org.dcm4chex.archive.ejb.interfaces.PurgeFile;
import org.dcm4chex.archive.ejb.interfaces.PurgeFileHome;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.jboss.system.ServiceMBeanSupport;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1004 $ $Date: 2004-02-22 06:32:43 +0800 (周日, 22 2月 2004) $
 * @since 20.02.2004
 * 
 * @jmx.mbean extends="org.jboss.system.ServiceMBean"
 */
public class PurgeFilesService
    extends ServiceMBeanSupport
    implements org.dcm4chex.service.PurgeFilesServiceMBean {

    private String retrieveAETs;
    
    /**
     * @jmx.managed-attribute
     */
    public String getEjbProviderURL() {
        return EJBHomeFactory.getEjbProviderURL();
    }

    /**
     * @jmx.managed-attribute
     */
    public void setEjbProviderURL(String ejbProviderURL) {
        EJBHomeFactory.setEjbProviderURL(ejbProviderURL);
    }

    /**
     * @jmx.managed-attribute
     */
    public final String getRetrieveAETs() {
        return retrieveAETs;
    }

    /**
     * @jmx.managed-attribute
     */
    public final void setRetrieveAETs(String aets) {
        if (aets == null || aets.length() == 0) {
            throw new IllegalArgumentException();
        }
        this.retrieveAETs = aets;
    }

    /**
     * @jmx.managed-operation
     */
    public void run() {
        PurgeFile purgeFile;
        try {
            PurgeFileHome home =
                (PurgeFileHome) EJBHomeFactory.getFactory().lookup(
                    PurgeFileHome.class,
                    PurgeFileHome.JNDI_NAME);
            purgeFile = home.create();
        } catch (Exception e) {
            log.error("Failed to connect EJB service", e);
            return;
        }
        try {
            FileDTO[] toDelete;
            try {
                toDelete = purgeFile.findDereferencedFiles(retrieveAETs);
            } catch (Exception e) {
                log.warn("Failed to query dereferenced files:", e);
                return;
            }
            for (int i = 0; i < toDelete.length; i++) {
                if (delete(toDelete[i].toFile())) {
                    try {
                        purgeFile.deleteFile(toDelete[i].getPk());
                    } catch (Exception e) {
                        log.warn("Failed to remove entry from list of dereferenced files:", e);
                    }
                }
            }
        } finally {
            try {
                purgeFile.remove();
            } catch (Exception ignore) {
            }
        }
    }

    private boolean delete(File file) {
        log.info("M-DELETE file: " + file);
        if (!file.exists()) {
            log.warn("File: " + file + " was already deleted");
            return true;
        }
        if (!file.delete()) {
            log.warn("Failed to delete file: " + file);
            return false;                    
        }
        return true;
    }
}
