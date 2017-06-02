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

package org.dcm4chex.webview;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;

/**
 * CustomLaunchProperties Implementation.
 * 
 * @author franz.willer@agfa.com
 * @version $Revision$ $Date$
 * @since 04.10.2006
 */
public class WebViewerProperties implements CustomLaunchProperties {

    /* (non-Javadoc)
     * @see org.dcm4chex.webview.CustomLaunchProperties#addCustomProperties(java.util.Properties, java.util.Map)
     */
    public void addCustomProperties(Properties p, Map queryResult) {
        if ( queryResult.isEmpty()) return;
        DicomObject dcm = (DicomObject)((Collection) queryResult.values().iterator().next()).iterator().next();
        String patientName = dcm.getString(Tag.PatientName);
        if ( patientName == null ) patientName ="";
     	String firstName = "";
     	String lastName = patientName;
     	int pos = patientName.indexOf('^');
     	if ( pos != -1 ) {
     		firstName=patientName.substring(pos+1);
     		lastName=patientName.substring(0,pos);
     	}
     	String sex = dcm.getString(Tag.PatientSex);
     	if ( sex == null ) sex ="";
        String birthDate = dcm.getString(Tag.PatientBirthDate);
     	if ( birthDate == null ) birthDate ="";
        String title = lastName+"("+sex+")";
        
        p.setProperty("title", title);
        p.setProperty("DB_FIRST_NAME", firstName);
        p.setProperty("DB_LAST_NAME", lastName);
        p.setProperty("DB_SEX", sex);
        p.setProperty("DB_BIRTH_DATE", birthDate == null ? "" : birthDate);
        if ( UID.KeyObjectSelectionDocument.equals( dcm.getString(Tag.SOPClassUID) ) ) {
            p.setProperty("KEYNOTE", dcm.getString(Tag.SOPInstanceUID));
        }
    }

    /* (non-Javadoc)
     * @see org.dcm4chex.webview.CustomLaunchProperties#getResultAttributes()
     */
    public int[][] getResultAttributes() {
        int[][] attrs = new int[3][];
        attrs[0] = new int[]{ Tag.PatientName }; 
        attrs[1] = new int[]{ Tag.PatientSex }; 
        attrs[2] = new int[]{ Tag.PatientBirthDate }; 
        return attrs;
    }

}
