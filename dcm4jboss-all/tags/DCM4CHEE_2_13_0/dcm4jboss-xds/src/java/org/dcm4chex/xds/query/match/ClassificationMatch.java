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

package org.dcm4chex.xds.query.match;

import java.util.List;

public class ClassificationMatch {

    private String name;
    private StringBuffer sbMatch = new StringBuffer();
    private ClassificationMatch(List codes, String name, String urn) {
        this.name = name;
        if ( codes == null || codes.size() < 1) return;
        sbMatch.append("( ").append(name).append(".classifiedobject = doc.id AND ").append(name);
        sbMatch.append(".classificationScheme ='").append(urn).append("' AND ").append(name);
        sbMatch.append(".nodeRepresentation IN ('").append(codes.get(0));
        for (int i = 1; i < codes.size(); i++) {
            sbMatch.append("', '").append(codes.get(i));
        }
        sbMatch.append("')");
    }
    
    public static ClassificationMatch getClassCodeMatch(List l) {
        return getCodeMatch(l, "clCode", "urn:uuid:41a5887f-8865-4c09-adf7-e362475b143a");
    }
    public static ClassificationMatch getPSCodeMatch(List l) {
        return getCodeMatch(l, "psc", "urn:uuid:cccf5598-8b07-4b77-a05e-ae952c785ead");
    }
    public static ClassificationMatch getHFTCodeMatch(List l) {
        return getCodeMatch(l, "hftc", "urn:uuid:f33fb8ac-18af-42cc-ae0e-ed0b0bdb91e1");
    }
    public static ClassificationMatch getEVCodeMatch(List l) {
        return getCodeMatch(l, "ecl", "urn:uuid:2c6b8cb7-8b2a-4051-b291-b1ae6a575ef4");
    }
    
    private static ClassificationMatch getCodeMatch(List l, String name, String urn) {
        if ( l == null || l.size() < 1 ) return null;
        return new ClassificationMatch(l, name, urn);
    }
    
    public String getName() {
        return name;
    }
    public String toString() {
        return sbMatch.toString();
    }

}
