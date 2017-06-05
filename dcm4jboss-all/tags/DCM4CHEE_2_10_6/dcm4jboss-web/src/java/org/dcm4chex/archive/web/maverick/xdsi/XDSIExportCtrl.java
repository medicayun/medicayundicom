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

package org.dcm4chex.archive.web.maverick.xdsi;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.dcm4chex.archive.web.maverick.Dcm4cheeFormController;
import org.dcm4chex.archive.web.maverick.FolderForm;

/**
 * @author franz.willer@gwi-ag.com
 * @version $Revision: 2717 $ $Date: 2006-08-28 19:52:34 +0800 (周一, 28 8月 2006) $
 */
public class XDSIExportCtrl extends Dcm4cheeFormController {

    private XDSIExportDelegate delegate = null;
	private static final String CANCEL = "cancel";
	private static final String XDSI_EXPORT = "xdsi_export";
	private static final String XDSI_DELEGATE_ATTR_NAME = "xdsiDelegate";
    protected Object makeFormBean() {
    	HttpServletRequest rq = getCtx().getRequest();
    	XDSIModel model = XDSIModel.getModel(rq);
    	delegate = (XDSIExportDelegate) rq.getSession().getAttribute(XDSI_DELEGATE_ATTR_NAME);
    	if ( delegate == null) { 
    		delegate = new XDSIExportDelegate();
    		try {
	    		delegate.init(getCtx());
	    		rq.getSession().setAttribute(XDSI_DELEGATE_ATTR_NAME,delegate);
	    		clear(model, true);
    		} catch ( Exception x) {
    			throw new NullPointerException("Cant create XDSIModel or XDSIExport delegate!");
    		}
    	}
    	return model;
    }
    protected String perform() {
    	XDSIModel model = (XDSIModel) getForm();
        try {
        	HttpServletRequest rq = getCtx().getRequest();
        	if ( rq.getParameter("docUID") != null ) {
    			Set set = new HashSet();
    			set.add(rq.getParameter("docUID"));
    			model.setInstances(set);
    			model.setPdfExport(true);
        	}
        	if ( model.getNumberOfInstances() < 1) {
    			FolderForm.setExternalPopupMsg(this.getCtx(),"Nothing selected for export! Please select at least one patient, study, series or instance");
    			return CANCEL;
        	}
        	model.setErrorCode("OK");
        	model.setPopupMsg(null);
        	if ( rq.getParameter("cancel") != null || rq.getParameter("cancel.x") != null ) {
        		return CANCEL;
        	}
        	if ( rq.getParameter("clear") != null || rq.getParameter("clear.x") != null ) {
        		clear(model, true);
        		return XDSI_EXPORT;
        	}
        	if ( rq.getParameter("addEventCode") != null || rq.getParameter("redraw.x") != null ) {
        		model.addSelectedEventCode();
        		return XDSI_EXPORT;
        	}
        	if ( rq.getParameter("delEventCode") != null || rq.getParameter("delEventCode.x") != null ) {
        		model.removeSelectedEventCode();
        		return XDSI_EXPORT;
        	}
        	if ( rq.getParameter("deselectAllEventCodes") != null || rq.getParameter("deselectAllEventCodes.x") != null ) {
        		model.deselectAllEventCodes();
        		return XDSI_EXPORT;
        	}

            if ( rq.getParameter("export") != null || rq.getParameter("export.x") != null ) {
	        	if ( ! delegate.exportXDSI(model) ) {
	        		model.setPopupMsg("XDS-I Export failed!");
	        		return XDSI_EXPORT;
	        	}
	    		clear(model, false);
	    		FolderForm.setExternalPopupMsg(getCtx(), "XDS-I Export done!");
	        	return SUCCESS;//export done
        	}
            return XDSI_EXPORT;//Show selection page for authorRole, ... selection
        } catch (Exception x) {
        	model.setPopupMsg("Error:"+x.getMessage());
        	return ERROR;
        }
    }
    
    private void clear(XDSIModel model, boolean reload) {
		model.clear();
		model.setMetadataProperties(delegate.joinMetadataProperties(new Properties()));
		if ( reload ) {
			model.setDocTitles(delegate.getConfiguredDocTitles());
			model.setAuthorRoles(delegate.getConfiguredAuthorRoles());
			model.setEventCodes(delegate.getConfiguredEventCodes());
			model.setClassCodes(delegate.getConfiguredClassCodes());
			model.setContentTypeCodes( delegate.getConfiguredContentTypeCodes());
			model.setHealthCareFacilityTypeCodes( delegate.getConfiguredHealthCareFacilityTypeCodes());
		}    	
    }

}