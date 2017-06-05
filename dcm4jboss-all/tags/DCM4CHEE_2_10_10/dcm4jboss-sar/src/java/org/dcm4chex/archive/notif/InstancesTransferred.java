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
 * Agfa-Gevaert Group.
 * Portions created by the Initial Developer are Copyright (C) 2003-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See @authors listed below.
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

package org.dcm4chex.archive.notif;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.dcm4che.net.Association;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision: 3053 $ $Date: 2007-01-08 18:14:49 +0800 (周一, 08 1月 2007) $
 * @since Jan 8, 2007
 */
public class InstancesTransferred {

    private final Association storeAssoc;
    private final Association moveAssoc;
    private final String patID;
    private final String patName;
    private final HashMap studies = new HashMap();

    public InstancesTransferred(Association storeAssoc, Association moveAssoc, 
            String patID, String patName) {
        this.storeAssoc = storeAssoc;
        this.moveAssoc = moveAssoc;
        this.patID = patID;
        this.patName = patName;
    }

    public final Association getMoveAssociation() {
        return moveAssoc;
    }

    public final Association getStoreAssociation() {
        return storeAssoc;
    }
    
    public final String getPatientID() {
        return patID;
    }

    public final String getPatientName() {
        return patName;
    }

    public void addInstance(String studyIUID, String sopCUID, String sopIUID) {
        HashMap cuids = (HashMap) studies.get(studyIUID);
        if (cuids == null) {
            studies.put(studyIUID, cuids = new HashMap());
        }
        ArrayList iuids = (ArrayList) cuids.get(sopCUID);
        if (iuids == null) {
            cuids.put(sopCUID, iuids = new ArrayList());
        }
        iuids.add(sopIUID);
    }
    
    public Map getStudies() {
        return studies;
    }
}
