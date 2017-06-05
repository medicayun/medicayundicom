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

import javax.management.ObjectName;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXResult;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.dict.Tags;
import org.dcm4che.util.UIDGenerator;
import org.dcm4chex.archive.config.DicomPriority;
import org.dom4j.Document;
import org.dom4j.io.DocumentSource;
import org.xml.sax.ContentHandler;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision: 2250 $ $Date: 2006-01-30 22:38:42 +0800 (周一, 30 1月 2006) $
 * @since Jan 29, 2006
 *
 */
public class ORUService extends AbstractHL7Service
{

    private String stylesheetURL = "resource:xsl/hl7/oru2sr.xsl";
    private ObjectName exportManagerName;
    private int storePriority = 0;

    public String getStylesheetURL() {
        return stylesheetURL;
    }

    public void setStylesheetURL(String stylesheetURL) {
        this.stylesheetURL = stylesheetURL;
        reloadStylesheets();
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
            Dataset sr = dof.newDataset();
            Transformer t = getTemplates(stylesheetURL).newTransformer();
            t.transform(new DocumentSource(msg), new SAXResult(
                    sr.getSAXHandler2(null)));
            addIUIDs(sr);
            storeSR(sr);
        }
        catch (Exception e)
        {
            throw new HL7Exception("AE", e.getMessage(), e);
        }      
        return true;
    }

    private void addIUIDs(Dataset sr)
    {
        UIDGenerator uidgen = UIDGenerator.getInstance();
        sr.putUI(Tags.SeriesInstanceUID, uidgen.createUID());
        sr.putUI(Tags.SOPInstanceUID, uidgen.createUID());
        String cuid = sr.getString(Tags.SOPClassUID);
        DcmElement identicalDocumentsSeq = sr.get(Tags.IdenticalDocumentsSeq);
        for (int i = 0, n = identicalDocumentsSeq.vm(); i < n; i++)
        {
            Dataset studyItem = identicalDocumentsSeq.getItem(i);
            Dataset seriesItem = studyItem.putSQ(Tags.RefSeriesSeq).addNewItem();
            seriesItem.putUI(Tags.SeriesInstanceUID, uidgen.createUID());
            Dataset sopItem = seriesItem.putSQ(Tags.RefSOPSeq).addNewItem();
            sopItem.putUI(Tags.RefSOPInstanceUID, uidgen.createUID());
            sopItem.putUI(Tags.RefSOPClassUID, cuid);
        }
    }

    private void storeSR(Dataset sr) throws Exception
    {
        server.invoke(exportManagerName, "storeExportSelection",
                new Object[]{sr, new Integer(storePriority)},
                new String[]{Dataset.class.getName(), int.class.getName()});        
    }
    
}
