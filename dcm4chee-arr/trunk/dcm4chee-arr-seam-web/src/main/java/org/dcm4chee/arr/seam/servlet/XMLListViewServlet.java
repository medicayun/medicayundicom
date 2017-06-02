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
 * Agfa HealthCare.
 * Portions created by the Initial Developer are Copyright (C) 2010
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
import java.util.List;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dcm4chee.arr.seam.ejb.AuditRecordQueryLocal;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date:: xxxx-xx-xx $
 * @since Jan 29, 2010
 */
public class XMLListViewServlet extends HttpServlet {

    private static final long serialVersionUID = -8356280446373908115L;

    protected void doPost(HttpServletRequest rq, HttpServletResponse rsp)
            throws ServletException, IOException {
        doGet(rq, rsp);
    }

    protected void doGet(HttpServletRequest rq, HttpServletResponse rsp)
            throws ServletException, IOException {
        List<byte[]> list;
        try {
            list = query(rq);
        } catch (Exception e) {
            Throwable cause = e.getCause();
            if (cause instanceof IllegalArgumentException)
                rsp.sendError(HttpServletResponse.SC_BAD_REQUEST, cause.getMessage());
            else
                rsp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            return;
        }
        writeTo(list, rsp);
    }

    private List<byte[]> query(HttpServletRequest rq) throws Exception {
        InitialContext jndiCtx = null;
        try {
            jndiCtx = new InitialContext();
            AuditRecordQueryLocal dao = (AuditRecordQueryLocal) jndiCtx
                    .lookup("java:comp/env/ejb/AuditRecordQuery");
            return dao.findRecords(rq);
        } finally {
            if (jndiCtx != null)
                try { jndiCtx.close(); } catch (NamingException ignore) {}
        }
    }

    protected void writeTo(List<byte[]> list, HttpServletResponse rsp)
            throws IOException, ServletException {
        rsp.setContentType("text/xml; charset=UTF-8");
        ServletOutputStream out = rsp.getOutputStream();
        try {
           out.println("<AuditMessages>");
           for (byte[] xmldata : list) {
                writeTo(xmldata, out);
                out.println();
           }
           out.println("</AuditMessages>");
        } finally {
            out.close();
        }
    }

    private void writeTo(byte[] xmldata, ServletOutputStream out)
            throws IOException {
        int off = 0;
        while (!isAuditMessage(xmldata,off) && !isIHEYr4(xmldata,off))
            off++;
        out.write(xmldata, off , xmldata.length - off);
    }

    private static boolean isAuditMessage(byte[] buf, int off) {
        return buf[off+0] == '<'
            && buf[off+1] == 'A'
            && buf[off+2] == 'u'
            && buf[off+3] == 'd'
            && buf[off+4] == 'i'
            && buf[off+5] == 't'
            && buf[off+6] == 'M'
            && buf[off+7] == 'e'
            && buf[off+8] == 's'
            && buf[off+9] == 's'
            && buf[off+10] == 'a'
            && buf[off+11] == 'g'
            && buf[off+12] == 'e';
    }

    private static boolean isIHEYr4(byte[] buf, int off) {
        return buf[off+0] == '<'
            && buf[off+1] == 'I'
            && buf[off+2] == 'H'
            && buf[off+3] == 'E'
            && buf[off+4] == 'Y'
            && buf[off+5] == 'r'
            && buf[off+6] == '4';
    }
}
