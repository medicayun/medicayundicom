/* $Id: CollapseSeriesCtrl.java 978 2004-02-18 02:35:05Z gunterze $
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

import org.infohazard.maverick.ctl.ThrowawayBean2;

/**
 * 
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 978 $ $Date: 2004-02-18 10:35:05 +0800 (周三, 18 2月 2004) $
 * @since 28.01.2004
 */
public class CollapseSeriesCtrl extends ThrowawayBean2 {

    private int patPk;
    private int studyPk;
    private int seriesPk;
    
    public final void setPatPk(int patPk)
    {
        this.patPk = patPk;
    }

    public final void setSeriesPk(int seriesPk)
    {
        this.seriesPk = seriesPk;
    }

    public final void setStudyPk(int studyPk)
    {
        this.studyPk = studyPk;
    }

    protected String perform() throws Exception {
        FolderForm folderForm = FolderForm.getFolderForm(getCtx().getRequest());
        folderForm.getSeriesByPk(patPk, studyPk, seriesPk).getInstances().clear();
        return SUCCESS;
    }

}
