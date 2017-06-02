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

package org.dcm4chee.arr.seam.servlet;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.dcm4chee.arr.entities.AuditRecord;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Apr 14, 2007
 */
public class HTMLViewServlet extends XMLViewServlet {

    private static final long serialVersionUID = -3823698611577286637L;

    private static final String DETAILS_XSL = "arr-details.xsl";

    private Templates tpl;

    public void init() throws ServletException {
        super.init();
        try {
            SAXTransformerFactory tf = (SAXTransformerFactory)
                    TransformerFactory.newInstance();
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            StreamSource src = new StreamSource(
                    cl.getResource(DETAILS_XSL).toString());
            tpl = tf.newTemplates(src);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    protected void writeTo(AuditRecord rec, HttpServletResponse rsp)
            throws IOException, ServletException {
        rsp.setContentType("text/html; charset=UTF-8");
        ServletOutputStream out = rsp.getOutputStream();
        try {
            Transformer tr = tpl.newTransformer();
            StreamSource src = new StreamSource(
                    new ByteArrayInputStream(rec.getXmldata()));
            StreamResult rslt = new StreamResult(out);
            tr.transform(src, rslt);
        } catch (TransformerException e) {
            throw new ServletException(e);
        } finally {
            out.close();
        }
    }

}
