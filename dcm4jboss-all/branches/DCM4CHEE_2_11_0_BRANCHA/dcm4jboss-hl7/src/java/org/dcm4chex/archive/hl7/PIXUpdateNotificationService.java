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

package org.dcm4chex.archive.hl7;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;
import org.dcm4cheri.util.StringUtils;
import org.dcm4chex.archive.ejb.interfaces.PatientUpdateHome;
import org.dcm4chex.archive.exceptions.PatientMergedException;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.dcm4chex.archive.util.HomeFactoryException;
import org.dom4j.Document;
import org.xml.sax.ContentHandler;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision: 3397 $ $Date: 2007-06-21 13:58:05 +0800 (周四, 21 6月 2007) $
 * @since Jun 18, 2007
 */
public class PIXUpdateNotificationService extends AbstractHL7Service {
    
    private static final int ID = 0;
    private static final int ISSUER = 1;
    private List issuersOfOnlyOtherPatientIDs;

    public final String getIssuersOfOnlyOtherPatientIDs() {
        if (issuersOfOnlyOtherPatientIDs == null
                || issuersOfOnlyOtherPatientIDs.isEmpty()) {
            return "-";
        }
        Iterator iter = issuersOfOnlyOtherPatientIDs.iterator();
        StringBuffer sb = new StringBuffer((String) iter.next());
        while (iter.hasNext()) {
            sb.append(',').append((String) iter.next());
        }
        return sb.toString();
    }


    public final void setIssuersOfOnlyOtherPatientIDs(String s) {
        if (s.trim().equals("-")) {
            issuersOfOnlyOtherPatientIDs = null;
        } else {
            String[] a = StringUtils.split(s, ',');
            issuersOfOnlyOtherPatientIDs = new ArrayList(a.length);
            for (int i = 0; i < a.length; i++) {
                issuersOfOnlyOtherPatientIDs.add(a[i].trim());
            }
        }
    }


    public boolean process(MSH msh, Document msg, ContentHandler hl7out)
            throws HL7Exception {
        List pids;
        try {
            pids = new PID(msg).getPatientIDs();
        } catch (IllegalArgumentException e) {
            throw new HL7Exception("AR", e.getMessage());
        }
        try {
            PatientUpdateHome patUpdate = getPatientUpdateHome();
            for (int i = 0, n = pids.size(); i < n; ++i) {
                String[] pid = (String[]) pids.get(i);
                if (!issuersOfOnlyOtherPatientIDs.contains(pid[ISSUER])) {
                    Dataset ds = toDataset(pid);
                    DcmElement opids = ds.putSQ(Tags.OtherPatientIDSeq);
                    for (int j = 0, m = pids.size(); j < m; ++j) {
                        String[] opid = (String[]) pids.get(j);
                        if (opid != pid) {
                            opids.addItem(toDataset(opid));
                        }
                    }
                    patUpdate.create().updateOtherPatientIDsOrCreate(ds);
                }
            }
        } catch (PatientMergedException e) {
            throw new HL7Exception("AR", e.getMessage());
        } catch (Exception e) {
            throw new HL7Exception("AE", e.getMessage(), e);
        }
        return true;
    }


    private Dataset toDataset(String[] pid) {
        Dataset ds = DcmObjectFactory.getInstance().newDataset();
        ds.putLO(Tags.PatientID, pid[ID]);
        ds.putLO(Tags.IssuerOfPatientID, pid[ISSUER]);
        return ds;
    }

    private PatientUpdateHome getPatientUpdateHome()
            throws HomeFactoryException {
        return (PatientUpdateHome) EJBHomeFactory.getFactory().lookup(
                PatientUpdateHome.class, PatientUpdateHome.JNDI_NAME);
    }
}
