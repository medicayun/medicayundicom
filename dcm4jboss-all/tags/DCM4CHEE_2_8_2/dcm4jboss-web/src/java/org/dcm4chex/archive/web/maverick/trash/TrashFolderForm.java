/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), available at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * TIANI Medgraph AG.
 * Portions created by the Initial Developer are Copyright (C) 2003-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunter.zeilinger@tiani.com>
 * Franz Willer <franz.willer@gwi-ag.com>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package org.dcm4chex.archive.web.maverick.trash;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.dcm4chex.archive.web.maverick.BasicFolderForm;
import org.infohazard.maverick.flow.ControllerContext;

/**
 * @author franz.willer@gwi-ag.com
 * @version $Revision: 2274 $ $Date: 2006-02-14 18:41:42 +0800 (周二, 14 2月 2006) $
 * @since 19.12.2005
 */
public class TrashFolderForm extends BasicFolderForm {

    static final String FOLDER_ATTRNAME = "trashFolderFrom";

	protected static Logger log = Logger.getLogger(TrashFolderForm.class);
	
    static TrashFolderForm getTrashFolderForm(ControllerContext ctx) {
    	HttpServletRequest request = ctx.getRequest();
        TrashFolderForm form = (TrashFolderForm) request.getSession()
                .getAttribute(FOLDER_ATTRNAME);
        if (form == null) {
            form = new TrashFolderForm(request);
            request.getSession().setAttribute(FOLDER_ATTRNAME, form);
            try {
                int limit = Integer.parseInt( ctx.getServletConfig().getInitParameter("limitNrOfStudies") );
            	if ( limit > 0 ) {
            		form.setLimit( limit );
            	} else {
            		log.warn("Wrong servlet ini parameter 'limitNrOfStudies' ! Must be greater 0! Ignored");
            	}
            } catch (Exception x) {
        		log.warn("Wrong servlet ini parameter 'limitNrOfStudies' ! Must be an integer greater 0! Ignored");
            }
        }
        form.setErrorCode( NO_ERROR ); //reset error code
		form.setPopupMsg(null);
        
        return form;
    }
    
	private TrashFolderForm( HttpServletRequest request ) {
    	super(request);
    }
	
	public String getModelName() { return "TRASH"; }

	/* (non-Javadoc)
	 * @see org.dcm4chex.archive.web.maverick.BasicFormPagingModel#gotoCurrentPage()
	 */
	public void gotoCurrentPage() {
		//We doesnt need this method here. TrashFolderCtrl does not use performPrevious/performNext!
	}
	
}