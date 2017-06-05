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

import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXResult;

import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import org.dcm4chex.archive.ejb.interfaces.PatientUpdate;
import org.dcm4chex.archive.ejb.interfaces.PatientUpdateHome;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.dcm4chex.archive.util.HomeFactoryException;
import org.dom4j.Document;
import org.dom4j.io.DocumentSource;
import org.xml.sax.ContentHandler;

/**
 * @author gunter.zeilinger@tiani.com
 * @version Revision $Date: 2005-10-07 03:55:27 +0800 (周五, 07 10月 2005) $
 * @since 26.12.2004
 */

public class ADTService extends AbstractHL7Service {

	private String pidStylesheetURL = "resource:xsl/hl7/pid2dcm.xsl";

    private String mrgStylesheetURL = "resource:xsl/hl7/mrg2dcm.xsl";
    
	private boolean ignoreDeleteErrors;

	public final String getMrgStylesheetURL() {
		return mrgStylesheetURL;
	}

	public final void setMrgStylesheetURL(String mrgStylesheetURL) {
		this.mrgStylesheetURL = mrgStylesheetURL;
		reloadStylesheets();
	}

	public final String getPidStylesheetURL() {
		return pidStylesheetURL;
	}

	public final void setPidStylesheetURL(String pidStylesheetURL) {
		reloadStylesheets();
		this.pidStylesheetURL = pidStylesheetURL;
	}

	/**
	 * @return Returns the ignoreNotFound.
	 */
	public boolean isIgnoreDeleteErrors() {
		return ignoreDeleteErrors;
	}
	/**
	 * @param ignoreNotFound The ignoreNotFound to set.
	 */
	public void setIgnoreDeleteErrors(boolean ignore) {
		this.ignoreDeleteErrors = ignore;
	}
	
	public boolean process(MSH msh, Document msg, ContentHandler hl7out)
            throws HL7Exception {
        Dataset pat = dof.newDataset();
        try {
            Transformer t = getTemplates(pidStylesheetURL).newTransformer();
            t.transform(new DocumentSource(msg), new SAXResult(pat
                    .getSAXHandler2(null)));
			final String pid = pat.getString(Tags.PatientID);
			if (pid == null)
				throw new HL7Exception("AR", 
						"Missing required PID-3: Patient ID (Internal ID)");
			final String pname = pat.getString(Tags.PatientName);
			if (pname == null)
				throw new HL7Exception("AR", 
						"Missing required PID-5: Patient Name");
            PatientUpdate update = getPatientUpdateHome().create();
            try {
                if (isMerge(msh)) {
                    Dataset mrg = dof.newDataset();
                    Transformer t2 = getTemplates(mrgStylesheetURL)
                            .newTransformer();
                    t2.transform(new DocumentSource(msg), new SAXResult(mrg
                            .getSAXHandler2(null)));
					final String opid = mrg.getString(Tags.PatientID);
					if (opid == null)
						throw new HL7Exception("AR",
								"Missing required MRG-1: Prior Patient ID - Internal");
					final String opname = mrg.getString(Tags.PatientName);
                    log.info("Merge Patient " + opname + ", PID:" + opid
                            + " with "+ pname + ", PID:" + pid);
                    update.mergePatient(pat, mrg);
                } else if ( isDelete(msh) ) {
                	log.info("Delete Patient "+pat.getString( Tags.PatientName )
							+ ", PID:"+pat.getString( Tags.PatientID ));
                	try {
                		update.deletePatient(pat);
                	} catch ( Exception x ) {
                		if ( ! ignoreDeleteErrors ) {
                			throw x;
                		}
                	}
                } else {
                    log.info("Update Patient Info of " + pname + ", PID:" + pid);
                    update.updatePatient(pat);					
                }
            } finally {
                try {
                    update.remove();
	            } catch (Exception ignore) {
	            }
            }
        } catch (HL7Exception e) {
            throw e;
        } catch (Exception e) {
            throw new HL7Exception("AE", e.getMessage(), e);
        }
        return true;
    }

	private boolean isMerge(MSH msh) {
        return "A40".equals(msh.triggerEvent);
    }

    private boolean isDelete(MSH msh) {
        return "A23".equals(msh.triggerEvent);
    }

    private PatientUpdateHome getPatientUpdateHome()
            throws HomeFactoryException {
        return (PatientUpdateHome) EJBHomeFactory.getFactory().lookup(
                PatientUpdateHome.class, PatientUpdateHome.JNDI_NAME);
    }
}