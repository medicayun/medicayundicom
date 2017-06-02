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

import java.io.IOException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dcm4chee.arr.entities.AuditRecord;
import org.dcm4chee.arr.seam.ejb.AuditRecordAccessLocal;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Apr 14, 2007
 */
public class XMLViewServlet extends HttpServlet {

    private static final long serialVersionUID = 230759458252187964L;

    protected void doPost(HttpServletRequest rq, HttpServletResponse rsp)
            throws ServletException, IOException {
        doGet(rq, rsp);
    }

    protected void doGet(HttpServletRequest rq, HttpServletResponse rsp)
            throws ServletException, IOException {
        writeTo(findAuditRecord(Long.parseLong(rq.getParameter("pk"))), rsp);
    }

    protected void writeTo(AuditRecord rec, HttpServletResponse rsp)
            throws IOException, ServletException {
        rsp.setContentType("text/xml; charset=UTF-8");
        ServletOutputStream out = rsp.getOutputStream();
        try {
            out.write(rec.getXmldata());
        } finally {
            out.close();
        }
    }

    private AuditRecord findAuditRecord(long pk) throws ServletException {
        InitialContext jndiCtx = null;
        try {
            jndiCtx = new InitialContext();
            AuditRecordAccessLocal dao = (AuditRecordAccessLocal)
                    jndiCtx.lookup("java:comp/env/ejb/AuditRecordAccess");
            return dao.findAuditRecord(pk);
        } catch (Exception e) {
            throw new ServletException(e);
        } finally {
            if (jndiCtx != null) {
                try {
                    jndiCtx.close();
                } catch (NamingException ignore) {
                }
            }
        }
    }

}
