/* $Id: FolderCtrl.java 1171 2004-10-03 21:59:12Z gunterze $
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

import org.apache.log4j.Logger;

/**
 * 
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1171 $ $Date: 2004-10-04 05:59:12 +0800 (周一, 04 10月 2004) $
 * @since 28.01.2004
 */
public class FolderCtrl extends Dcm4JbossFormController {


	protected static Logger log = Logger.getLogger(FolderCtrl.class);
    
    public static final String FOLDER = "folder";
    public static final String MERGE = "merge";
    
    
    protected Object makeFormBean() {
        return FolderForm.getFolderForm(getCtx().getRequest());
    }

    protected String perform() throws Exception {
        return FOLDER;
    }

}
