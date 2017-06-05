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

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import javax.management.ObjectName;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXResult;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;
import org.dcm4che.util.Base64;
import org.dcm4che.util.UIDGenerator;
import org.dcm4chex.archive.config.DicomPriority;
import org.dcm4chex.archive.ejb.jdbc.QueryCmd;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.DocumentSource;
import org.xml.sax.ContentHandler;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision: 2687 $ $Date: 2006-08-17 23:52:10 +0800 (周四, 17 8月 2006) $
 * @since Jan 29, 2006
 *
 */
public class ORUService extends AbstractHL7Service
{
    private String oru2srXSL = "resource:dcm4chee-hl7/oru2sr.xsl";
    private String oru2pdfXSL = "resource:dcm4chee-hl7/oru2pdf.xsl";
    private ObjectName exportManagerName;
    private int storePriority = 0;

    public String getSRStylesheetURL() {
        return oru2srXSL;
    }

    public void setSRStylesheetURL(String stylesheetURL) {
        this.oru2srXSL = stylesheetURL;
        reloadStylesheets();
    }

    public final String getPDFStylesheetURL() {
        return oru2pdfXSL;
    }

    public final void setPDFStylesheetURL(String oru2pdfXSL) {
        this.oru2pdfXSL = oru2pdfXSL;
    }

    public final ObjectName getExportManagerName()
    {
        return exportManagerName;
    }

    public final void setExportManagerName(ObjectName exportManagerName)
    {
        this.exportManagerName = exportManagerName;
    }

    public final String getStorePriority() {
        return DicomPriority.toString(storePriority);
    }

    public final void setStorePriority(String storePriority) {
        this.storePriority  = DicomPriority.toCode(storePriority);
    }
    
    public boolean process(MSH msh, Document msg, ContentHandler hl7out)
    throws HL7Exception
    {
        try
        {
            Dataset doc = dof.newDataset();
            byte[] pdf = getPDF(msg);
            if (pdf != null) {
                Transformer t = getTemplates(oru2pdfXSL).newTransformer();
                t.transform(new DocumentSource(msg), new SAXResult(
                        doc.getSAXHandler2(null)));
                doc.putOB(Tags.EncapsulatedDocument, pdf);
                storeSR(doc);
            } else {
                Transformer t = getTemplates(oru2srXSL).newTransformer();
                t.transform(new DocumentSource(msg), new SAXResult(
                        doc.getSAXHandler2(null)));
                addIUIDs(doc);
                storeSR(doc);
            }
        }
        catch (Exception e)
        {
            throw new HL7Exception("AE", e.getMessage(), e);
        }      
        return true;
    }

    static String toString(Object el) {
        return el != null ? ((Element) el).getText() : "";
    }
    
    private byte[] getPDF(Document msg) {
        List obxs = msg.getRootElement().elements("OBX");
        for (Iterator iter = obxs.iterator(); iter.hasNext();) {
            Element obx = (Element) iter.next();
            List fds = obx.elements();
            if ("ED".equals(toString(fds.get(1)))) {
                List cmps = ((Element) fds.get(4)).elements();
                if ("PDF".equals(toString(cmps.get(1)))) {
                    String s = toString(cmps.remove(3));
                    return Base64.base64ToByteArray(s);
                }
            }
        }
        // hl7/OBX[field[2]='ED']/field[5]/component[4]
        // TODO Auto-generated method stub
        return null;
    }

    private void addIUIDs(Dataset sr) {
        UIDGenerator uidgen = UIDGenerator.getInstance();
        if (!sr.containsValue(Tags.StudyInstanceUID)) {
            if (!addSUIDs(sr)) {
                sr.putUI(Tags.StudyInstanceUID, uidgen.createUID());
            }
        }
        sr.putUI(Tags.SeriesInstanceUID, uidgen.createUID());
        sr.putUI(Tags.SOPInstanceUID, uidgen.createUID());
        String cuid = sr.getString(Tags.SOPClassUID);
        DcmElement identicalDocumentsSeq = sr.get(Tags.IdenticalDocumentsSeq);
        for (int i = 0, n = identicalDocumentsSeq.countItems(); i < n; i++)
        {
            Dataset studyItem = identicalDocumentsSeq.getItem(i);
            Dataset seriesItem = studyItem.putSQ(Tags.RefSeriesSeq).addNewItem();
            seriesItem.putUI(Tags.SeriesInstanceUID, uidgen.createUID());
            Dataset sopItem = seriesItem.putSQ(Tags.RefSOPSeq).addNewItem();
            sopItem.putUI(Tags.RefSOPInstanceUID, uidgen.createUID());
            sopItem.putUI(Tags.RefSOPClassUID, cuid);
        }
    }

    private boolean addSUIDs(Dataset sr) {
        String accno = sr.getString(Tags.AccessionNumber);
        if (accno == null) {
            log.warn("Missing Accession Number in ORU - store report in new Study");
            return false;
        }
        Dataset keys = DcmObjectFactory.getInstance().newDataset();
        keys.putSH(Tags.AccessionNumber, accno);
        keys.putUI(Tags.StudyInstanceUID);
        QueryCmd query = null;
        try {
            query = QueryCmd.createStudyQuery(keys, false, true);
            query.execute();
            if (!query.next()) {
                log.warn("No Study with given Accession Number: " 
                        + accno + " - store report in new Study");
                return false;
            }
            copyStudyInstanceUID(query, sr);
            if (query.next()) {
                DcmElement sq = sr.putSQ(Tags.IdenticalDocumentsSeq);
                do {
                    copyStudyInstanceUID(query, sq.addNewItem());
                } while (query.next());
            }
            return true;
        } catch (SQLException e) {
            log.error("Query DB for Studies with Accession Number " + accno
                    + " failed - store report in new Study", e);
            sr.putSQ(Tags.IdenticalDocumentsSeq);
            return false;
        } finally {
            if (query != null)
                query.close();
        }
    }

    private void copyStudyInstanceUID(QueryCmd query, Dataset sr) 
        throws SQLException {
        sr.putUI(Tags.StudyInstanceUID, 
                query.getDataset().getString(Tags.StudyInstanceUID));
    }

    private void storeSR(Dataset sr) throws Exception {
        server.invoke(exportManagerName, "storeExportSelection",
                new Object[]{sr, new Integer(storePriority)},
                new String[]{Dataset.class.getName(), int.class.getName()});        
    }
    
}
