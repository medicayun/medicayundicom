/* $Id: PurgeFilesService.java 1119 2004-05-21 09:50:27Z gunterze $
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
package org.dcm4chex.archive.mbean;

import java.io.File;

import org.dcm4chex.archive.ejb.interfaces.FileDTO;
import org.dcm4chex.archive.ejb.interfaces.PurgeFile;
import org.dcm4chex.archive.ejb.interfaces.PurgeFileHome;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.jboss.system.ServiceMBeanSupport;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1119 $ $Date: 2004-05-21 17:50:27 +0800 (周五, 21 5月 2004) $
 * @since 20.02.2004
 */
public class PurgeFilesService extends ServiceMBeanSupport {

    private static final String LOCAL = "local";
    private String[] retrieveAETs;
    
    private static String null2local(String s) {        
        return s == null ? LOCAL : s;
    }

    private static String local2null(String s) {        
        return LOCAL.equals(s) ? null : s;
    }
    
    public String getEjbProviderURL() {
        return null2local(EJBHomeFactory.getEjbProviderURL());
    }        

    public void setEjbProviderURL(String ejbProviderURL) {
        EJBHomeFactory.setEjbProviderURL(local2null(ejbProviderURL));
    }

    public final String[] getRetrieveAETs() {
        return retrieveAETs;
    }

    public final void setRetrieveAETs(String[] aets) {
        this.retrieveAETs = aets;
    }

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
