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

package org.dcm4chex.archive.hl7;

import java.util.Arrays;
import java.util.List;

import javax.management.ObjectName;

import org.dcm4cheri.util.StringUtils;
import org.jboss.system.ServiceMBeanSupport;

public class PIXQueryService extends ServiceMBeanSupport {

    private ObjectName hl7SendServiceName;
    private String pixQueryName;
    private String pixManager;
    private String mockResponse;

    public final ObjectName getHL7SendServiceName() {
        return hl7SendServiceName;
    }

    public final void setHL7SendServiceName(ObjectName name) {
        this.hl7SendServiceName = name;
    }

    public final String getPIXManager() {
        return pixManager;
    }

    public final void setPIXManager(String pixManager) {
        this.pixManager = pixManager;
    }

    public final String getPIXQueryName() {
        return pixQueryName;
    }

    public final void setPIXQueryName(String pixQueryName) {
        this.pixQueryName = pixQueryName;
    }

    public final String getMockResponse() {
        return mockResponse == null ? "-" : mockResponse;
    }

    public final void setMockResponse(String mockResponse) {
        String trim = mockResponse.trim();
        this.mockResponse = "-".equals(trim) ? null : trim;
    }

    public String showCorrespondingPIDs(String patientID, String issuer) {
        try {
            return queryCorrespondingPIDs(patientID, issuer).toString();
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    public List queryCorrespondingPIDs(String patientID, String issuer)
            throws Exception {
        return queryCorrespondingPIDs(patientID, issuer, null);
    }

    public List queryCorrespondingPIDs(String patientID, String issuer,
            String[] domains) throws Exception {
        return mockResponse == null
                    ? (List) server.invoke(hl7SendServiceName, "sendQBP_Q23",
                            new Object[] {
                                    pixManager,
                                    pixQueryName,
                                    patientID,
                                    issuer,
                                    domains  },
                            new String[] {
                                    String.class.getName(),
                                    String.class.getName(),
                                    String.class.getName(),
                                    String.class.getName(),
                                    String[].class.getName(),
                        })
                    : Arrays.asList(StringUtils.split(mockResponse, '|'));
    }

}
