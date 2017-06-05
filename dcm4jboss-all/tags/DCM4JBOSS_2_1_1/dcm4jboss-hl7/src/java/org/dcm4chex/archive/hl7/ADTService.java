/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
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
 * @version Revision $Date: 2005-02-07 17:05:58 +0800 (周一, 07 2月 2005) $
 * @since 26.12.2004
 */

public class ADTService extends AbstractHL7Service {

    private static final String PID2DCM_XSL_URL = "resource:xsl/hl7/pid2dcm.xsl";

    private static final String MRG2DCM_XSL_URL = "resource:xsl/hl7/mrg2dcm.xsl";

    public boolean process(MSH msh, Document msg, ContentHandler hl7out)
            throws HL7Exception {
        Dataset pat = dof.newDataset();
        try {
            Transformer t = getTemplates(PID2DCM_XSL_URL).newTransformer();
            t.transform(new DocumentSource(msg), new SAXResult(pat
                    .getSAXHandler2(null)));
            PatientUpdate update = getPatientUpdateHome().create();
            try {
                if (isUpdate(msh)) {
                    log.info("Update Patient Info of " 
                            + pat.getString(Tags.PatientName)
                            + ", PID:" + pat.getString(Tags.PatientID));
                    update.updatePatient(pat);

                } else if (isMerge(msh)) {
                    Dataset mrg = dof.newDataset();
                    Transformer t2 = getTemplates(MRG2DCM_XSL_URL)
                            .newTransformer();
                    t2.transform(new DocumentSource(msg), new SAXResult(mrg
                            .getSAXHandler2(null)));
                    log.info("Merge Patient " 
                            + mrg.getString(Tags.PatientName)
                            + ", PID:" + mrg.getString(Tags.PatientID)
                            + " with "+ pat.getString(Tags.PatientName)
                            + ", PID:" + pat.getString(Tags.PatientID));
                    update.mergePatient(pat, mrg);
                }
            } finally {
                try {
                    update.remove();
	            } catch (Exception ignore) {
	            }
            }
        } catch (Exception e) {
            throw new HL7Exception.AE(e.getMessage(), e);
        }
        return true;
    }

    private boolean isUpdate(MSH msh) {
        return "A08".equals(msh.triggerEvent);
    }

    private boolean isMerge(MSH msh) {
        return "A40".equals(msh.triggerEvent);
    }

    private PatientUpdateHome getPatientUpdateHome()
            throws HomeFactoryException {
        return (PatientUpdateHome) EJBHomeFactory.getFactory().lookup(
                PatientUpdateHome.class, PatientUpdateHome.JNDI_NAME);
    }
}