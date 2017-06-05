/* $Id: FolderCtrl.java 943 2004-02-01 14:21:46Z gunterze $
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
package org.dcm4chex.archive.web.maverick;

import java.util.Arrays;
import java.util.Set;

import org.dcm4chex.archive.ejb.interfaces.ContentManager;
import org.dcm4chex.archive.ejb.interfaces.ContentManagerHome;
import org.dcm4chex.archive.ejb.interfaces.StudyFilterDTO;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.infohazard.maverick.ctl.ThrowawayFormBeanUser;

/**
 * 
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 943 $ $Date: 2004-02-01 22:21:46 +0800 (周日, 01 2月 2004) $
 * @since 28.01.2004
 */
public class FolderCtrl extends ThrowawayFormBeanUser {

    static final String FOLDER = "folder";
    static final String FOLDER_ATTRNAME = "folderFrom";

    protected Object makeFormBean() {
        FolderForm form =
            (FolderForm) getCtx().getRequest().getSession().getAttribute(
                FOLDER_ATTRNAME);
        if (form == null) {
            form = new FolderForm();
            getCtx().getRequest().getSession().setAttribute(FOLDER_ATTRNAME, form);
        }
        return form;
    }

    protected String perform() throws Exception {
        try {
            FolderForm folderForm = (FolderForm) getForm();
            setSticky(folderForm.getStickyPatients(), "stickyPat");
            setSticky(folderForm.getStickyStudies(), "stickyStudy");
            setSticky(folderForm.getStickySeries(), "stickySeries");
            setSticky(folderForm.getStickyInstances(), "stickyInst");
            int cmd = folderForm.cmd();
            switch (cmd) {
                case FolderForm.NEW_QUERY :
                    return query(true);
                case FolderForm.QUERY_AGAIN :
                    return query(false);
                case FolderForm.DELETE :
                    return FOLDER;
                case FolderForm.MERGE :
                    return FOLDER;
                case FolderForm.MOVE :
                    return FOLDER;
            }
            throw new RuntimeException("cmd=" + cmd);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private String query(boolean newQuery) throws Exception {
        ContentManagerHome home =
            (ContentManagerHome) EJBHomeFactory.getFactory().lookup(
                ContentManagerHome.class,
                ContentManagerHome.JNDI_NAME);
        ContentManager cm = home.create();
        try {
            FolderForm folderForm = (FolderForm) getForm();
            StudyFilterDTO filter = folderForm.getStudyFilter();
            if (newQuery) {
                folderForm.setTotal(cm.countStudies(filter));
            }
            folderForm.updatePatients(
                cm.listPatients(
                    filter,
                    folderForm.getOffset(),
                    folderForm.getLimit()));
        } finally {
            try {
                cm.remove();
            } catch (Exception e) {
            }
        }
        return FOLDER;
    }

    private void setSticky(Set stickySet, String attr) {
        stickySet.clear();
        String[] newValue = getCtx().getRequest().getParameterValues(attr);
        if (newValue != null) {
            stickySet.addAll(Arrays.asList(newValue));
        }
    }
}
