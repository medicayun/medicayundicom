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
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa-Gevaert AG.
 * Portions created by the Initial Developer are Copyright (C) 2008
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See listed authors below.
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

package org.dcm4chee.web.war.folder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.dcm4chee.web.common.util.GroupedChoices;
import org.dcm4chee.web.dao.folder.StudyListFilter;
import org.dcm4chee.web.war.config.delegate.WebCfgDelegate;
import org.dcm4chee.web.war.folder.model.PatientModel;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Jan 14, 2009
 */
public class ViewPort implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private int offset = 0;
    private int total = 0;

    private final StudyListFilter filter = new StudyListFilter(
            GroupedChoices.get(WebCfgDelegate.getInstance().getSourceAetsPropertiesFilename()).getAllGroups());

    private List<PatientModel> patients = new ArrayList<PatientModel>();
    
    public ViewPort() {
        clear();
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public StudyListFilter getFilter() {
        return filter;
    }

    public List<PatientModel> getPatients() {
        return patients;
    }
    
    public void clear() {
        offset = total = 0;
        filter.clear();
        patients.clear();
    }
  
    public List<String> getSourceAetChoices(List<String> availableChoices) {
        return GroupedChoices.get(WebCfgDelegate.getInstance().getSourceAetsPropertiesFilename())
            .getChoices(availableChoices);
    }
}
