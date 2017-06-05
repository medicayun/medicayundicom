/* $Id: PurgeFileBean.java 1117 2004-05-21 09:41:30Z gunterze $
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
package org.dcm4chex.archive.ejb.session;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import javax.ejb.EJBException;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.dcm4cheri.util.StringUtils;
import org.dcm4chex.archive.ejb.interfaces.FileDTO;
import org.dcm4chex.archive.ejb.interfaces.FileLocal;
import org.dcm4chex.archive.ejb.interfaces.FileLocalHome;

/**
 * 
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1117 $ $Date: 2004-05-21 17:41:30 +0800 (周五, 21 5月 2004) $
 * @since 14.01.2004
 * 
 * @ejb.bean
 *  name="PurgeFile"
 *  type="Stateless"
 *  view-type="remote"
 *  jndi-name="ejb/PurgeFile"
 * 
 * @ejb.transaction-type 
 *  type="Container"
 * 
 * @ejb.transaction 
 *  type="Required"
 * 
 * @ejb.ejb-ref
 *  ejb-name="File" 
 *  view-type="local"
 *  ref-name="ejb/File" 
 * 
 */
public abstract class PurgeFileBean implements SessionBean {

    private FileLocalHome fileHome;

    public void setSessionContext(SessionContext arg0)
        throws EJBException, RemoteException {
        Context jndiCtx = null;
        try {
            jndiCtx = new InitialContext();
            fileHome = (FileLocalHome) jndiCtx.lookup("java:comp/env/ejb/File");
        } catch (NamingException e) {
            throw new EJBException(e);
        } finally {
            if (jndiCtx != null) {
                try {
                    jndiCtx.close();
                } catch (NamingException ignore) {
                }
            }
        }
    }

    public void unsetSessionContext() {
        fileHome = null;
    }

    /**
     * @ejb.interface-method
     */
    public void deleteFile(int file_pk)
        throws RemoteException, EJBException, RemoveException {
        fileHome.remove(new Integer(file_pk));
    }

    /**
     * @ejb.interface-method
     */
    public FileDTO[] findDereferencedFiles(String[] retrieveAETs)
        throws FinderException {
        Collection c = fileHome.findDereferenced();
        if (c.isEmpty()) {
            return new FileDTO[0];
        }
        Collection retrieveAETList = Arrays.asList(retrieveAETs);
        Collection retval = new ArrayList(c.size());
        for (Iterator it = c.iterator(); it.hasNext();) {
            FileLocal file = (FileLocal) it.next();
            HashSet set =
                new HashSet(
                    Arrays.asList(
                        StringUtils.split(file.getRetrieveAETs(), '\\')));
            set.retainAll(retrieveAETList);
            if (!set.isEmpty()) {
                retval.add(file.getFileDTO());
            }
        }
        return (FileDTO[]) retval.toArray(new FileDTO[retval.size()]);
    }
}
