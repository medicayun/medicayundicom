/*
 *  Copyright (c) 2003 by TIANI MEDGRAPH AG                                  *
 *                                                                           *
 *  This file is part of dcm4che.                                            *
 *                                                                           *
 *  This library is free software; you can redistribute it and/or modify it  *
 *  under the terms of the GNU Lesser General Public License as published    *
 *  by the Free Software Foundation; either version 2 of the License, or     *
 *  (at your option) any later version.                                      *
 *                                                                           *
 *  This library is distributed in the hope that it will be useful, but      *
 *  WITHOUT ANY WARRANTY; without even the implied warranty of               *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        *
 *  Lesser General Public License for more details.                          *
 *                                                                           *
 *  You should have received a copy of the GNU Lesser General Public         *
 *  License along with this library; if not, write to the Free Software      *
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA  *
 */
package org.dcm4chex.arr.web;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.security.Principal;
import java.text.MessageFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.dcm4che.data.DcmParser;
import org.dcm4che.data.DcmParserFactory;
import org.dcm4che.dict.DictionaryFactory;
import org.dcm4che.dict.TagDictionary;
import org.dcm4che.util.Base64;
import org.dcm4che.util.HostNameUtils;
import org.dcm4chex.arr.ejb.session.QueryAuditRecordLocal;
import org.dcm4chex.arr.ejb.session.QueryAuditRecordLocalHome;
import org.dcm4chex.arr.ejb.session.StoreAuditRecordLocal;
import org.dcm4chex.arr.ejb.session.StoreAuditRecordLocalHome;
import org.jboss.security.SecurityAssociation;

/**
 *  <description>
 *
 * @web:servlet
 *  name="AuditRep"
 *  display-name="Audit Record Repository"
 *  load-on-startup="1"
 * @web:servlet-init-param
 *  name="LogUsage"
 *  value="false"
 * @web:servlet-mapping
 *  url-pattern="*.do"
 * @web.ejb-local-ref
 *  name="ejb/QueryAuditRecord"
 *  type="Session"
 *  home="org.dcm4chex.arr.ejb.session.QueryAuditRecordLocalHome"
 *  local="org.dcm4chex.arr.ejb.session.QueryAuditRecordLocal"
 *  link="dcm4jboss-arr-ejb.jar#QueryAuditRecord"
 * @web.ejb-local-ref
 *  name="ejb/StoreAuditRecord"
 *  type="Session"
 *  home="org.dcm4chex.arr.ejb.session.StoreAuditRecordLocalHome"
 *  local="org.dcm4chex.arr.ejb.session.StoreAuditRecordLocal"
 *  link="dcm4jboss-arr-ejb.jar#StoreAuditRecord"
 * 
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @author  <a href="mailto:joseph@tiani.com">joseph foraci</a>
 * @since  February 16, 2003
 * @version  $Revision: 4367 $
 */
public class AuditRepServlet extends HttpServlet
{

    // Constants -----------------------------------------------------
    private final static String LIST_TPL = "/WEB-INF/xslt/arr-list.xsl";
    private final static String VIEW_TPL = "/WEB-INF/xslt/arr-view.xsl";
    private final static String VIEW_DCM_TPL = "/WEB-INF/xslt/arr-viewdcm.xsl";
    private final static String LIST_PATH = "/arr-list.do";
    private final static String VIEW_PATH = "/arr-view.do";
    private final static String VIEW_B64_PATH = "/arr-viewdcm.do";
    private final static char[] XML_PREFIX = "<?xml version=\"1.0\"?>".toCharArray();
    private final static String SQL_DATETIME = "yyyy-MM-dd HH:mm:ss";
    private final static TagDictionary Dict =
            DictionaryFactory.getInstance().getDefaultTagDictionary();
    private static final String AUDIT_LOG_USED =
        "<IHEYr4>"
        + "<AuditLogUsed>"
        + "<Usage>Access</Usage>"
        + "<User><LocalUser>{0}</LocalUser></User>"
        + "</AuditLogUsed>"
        + "<Host>{1}</Host>"
        + "<TimeStamp>{2,date,yyyy-MM-dd'T'HH:mm:ss.SSS}</TimeStamp>"
        + "</IHEYr4>";

    // Attributes ----------------------------------------------------
    private SAXTransformerFactory stf;
    private Templates listTpl;
    private Templates viewTpl;
    private Templates viewDcmTpl;
    private QueryAuditRecordLocal query = null;
    private StoreAuditRecordLocal store = null;
    private boolean logUsage = true;
    private Logger logger = Logger.getLogger(AuditRepServlet.class);
    
    // Constructors --------------------------------------------------

    // Methods -------------------------------------------------------
    /**
     *  Description of the Method
     *
     * @exception  UnavailableException Description of the Exception
     */
    public void init()
        throws UnavailableException
    {
        TransformerFactory transFact = TransformerFactory.newInstance();
        if (!transFact.getFeature(SAXTransformerFactory.FEATURE)) {
            log("SAXTransformerFactory is not supported");
            throw new UnavailableException(
                    "SAXTransformerFactory is not supported");
        }
        stf = (SAXTransformerFactory) transFact;
        try {
            ServletContext ctx = getServletContext();
            listTpl = stf.newTemplates(
                    new StreamSource(ctx.getResource(LIST_TPL).toExternalForm()));
            viewTpl = stf.newTemplates(
                    new StreamSource(ctx.getResource(VIEW_TPL).toExternalForm()));
            viewDcmTpl = stf.newTemplates(
                    new StreamSource(ctx.getResource(VIEW_DCM_TPL).toExternalForm()));
        } catch (TransformerConfigurationException tce) {
            log("Unable to compile stylesheet", tce);
            throw new UnavailableException("Unable to compile stylesheet");
        } catch (MalformedURLException mue) {
            log("Unable to locate stylesheet", mue);
            throw new UnavailableException("Unable to locate stylesheet");
        }
        String logparam = getInitParameter("LogUsage");
        if (logparam != null && "false".equalsIgnoreCase(logparam))
            logUsage = false;
    }

    public void destroy()
    {
    }

    public String getCurrentPrincipalName()
    {
        Principal p = SecurityAssociation.getPrincipal();
        return p != null
            ? p.getName()
            : System.getProperty("user.name");
    }
    
    private String buildAuditLogUsedAuditMessage(HttpServletRequest rq)
    {
        Object[] arguments = {
            getCurrentPrincipalName(),
            HostNameUtils.getLocalHostName(),
            new Date()
        };
        return MessageFormat.format(AUDIT_LOG_USED, arguments);
    }

    /**
     *  Description of the Method
     *
     * @param  rq Description of the Parameter
     * @param  rsp Description of the Parameter
     * @exception  IOException Description of the Exception
     * @exception  ServletException Description of the Exception
     */
    public void doPost(HttpServletRequest rq, HttpServletResponse rsp)
        throws IOException, ServletException
    {
        doGet(rq, rsp);
    }


    /**
     *  Description of the Method
     *
     * @param  rq Description of the Parameter
     * @param  rsp Description of the Parameter
     * @exception  IOException Description of the Exception
     * @exception  ServletException Description of the Exception
     */
    public void doGet(HttpServletRequest rq, HttpServletResponse rsp)
        throws IOException, ServletException
    {
        final int MIN_START = 0;        //starting entry to display, zero-based
        final int MIN_PAGESIZE = -1;    //-1 displays all records at once, 0 displays none, x>0 displays at most x record entries
        int start = 0;
        int pageSize = 0;
        
        //parse start
        if (rq.getParameter("start") != null) {
            try { start = Math.max(MIN_START,Integer.parseInt(rq.getParameter("start"))); }
            catch (NumberFormatException nfe) {  }
        }
        //parse pagesize
        if (rq.getParameter("pagesize") != null) {
            try { pageSize = Math.max(MIN_PAGESIZE,Integer.parseInt(rq.getParameter("pagesize"))); }
            catch (NumberFormatException nfe) {  }
        }
        try {
            Templates tpl;
            String xml;
            String path = rq.getServletPath();
            if (VIEW_B64_PATH.equals(path)) {
                String b64 = rq.getParameter("base64");
                if (b64 != null)
                    showDatasetPage(rsp, b64, viewDcmTpl);
            }
            else {
                if (VIEW_PATH.equals(path)) {
                    int pk = Integer.parseInt(rq.getParameter("pk"));
                    xml = getQueryAuditRecord().getXmlData(pk);
                    //check if viewing raw xml
                    if (rq.getParameter("viewxml") != null)
                        tpl = null;
                    else
                        tpl = viewTpl;
                } else { // assume LIST_PATH.equals(path)
                    //check for empty or aggregated types to query
                    String[] types = rq.getParameterValues("type");
                    if (types != null && types.length == 1) {
                        if (types[0].length() == 0)
                            types = null;
                        else if (types[0].indexOf(',') != -1)
                            types = types[0].split(",");
                    }
                    //refresh hdr
                    if (rq.getParameter("update") != null)
                        rsp.setIntHeader("Refresh", 10);
                    //
                    String[] orderBy = rq.getParameterValues("orderby");
                    String[] orderDir = rq.getParameterValues("orderdir");
                    if (orderBy == null || orderDir == null
                        || orderBy.length != orderDir.length) {
                        orderBy = orderDir = null;
                    }
                    else { //loop through making sure values are legal, otherwise don't use ordering
                        for (int i = 0; i < orderBy.length; i++) {
                            if (!(orderBy[i].equalsIgnoreCase("type")
                                  || orderBy[i].equalsIgnoreCase("timestamp")
                                  || orderBy[i].equalsIgnoreCase("host"))) {
                                orderBy = orderDir = null;
                                break;
                            }
                            if (!(orderDir[i].length() == 0
                                  || orderDir[i].equalsIgnoreCase("ASC")
                                  || orderDir[i].equalsIgnoreCase("DESC"))) {
                                orderBy = orderDir = null;
                                break;
                            }
                        }
                    }
                    //query bean
                    xml = getQueryAuditRecord().query(
                        types,
                        maskNull(rq.getParameter("host")),
                        toSQLDate(maskNull(rq.getParameter("from"))),
                        toSQLDate(maskNull(rq.getParameter("to"))),
                        maskNull(rq.getParameter("aet")),
                        maskNull(rq.getParameter("username")),
                        maskNull(rq.getParameter("patientname")),
                        maskNull(rq.getParameter("patientid")),
                        start, pageSize,
                        orderBy, orderDir);
                    tpl = listTpl;
                    //accessed
                    if (logUsage)
                        getStoreAuditRecord().store(buildAuditLogUsedAuditMessage(rq));
                }
                showPage(rsp, xml, tpl);
            }
        } catch (Exception e) {
            showErrorPage(rsp, e);
        }
    }


    private void showDatasetPage(HttpServletResponse rsp, String base64Data, Templates tpl)
            throws IOException, TransformerConfigurationException
    {
        final int STOP_TAG = -1;
        try {
            byte[] bDcmDS = Base64.base64ToByteArray(base64Data);
            InputStream bIn = new ByteArrayInputStream(bDcmDS);
            SAXTransformerFactory sf = (SAXTransformerFactory)TransformerFactory.newInstance();
            TransformerHandler trans = sf.newTransformerHandler(viewDcmTpl);
            Result result = new StreamResult(rsp.getWriter());
            trans.setResult(result);
            DcmParserFactory pfact = DcmParserFactory.getInstance();
            DcmParser parser = pfact.newDcmParser(bIn);
            parser.setSAXHandler(trans, Dict);
            parser.parseDataset(parser.detectFileFormat().decodeParam, STOP_TAG);
        }
        catch (Exception e) { e.printStackTrace(); }
        finally {
            //try { out.close(); } catch (IOException ignore) {}
            //try { in.close(); } catch (IOException ignore) {}
        }
    }


    private String maskNull(String s)
    {
        return s != null ? s.trim() : "";
    }


    private String toSQLDate(String s)
    {
        int l = s.length();
        if (l == 0) {
            //log("Ignore zero-len date input");
            return "";
        }
        SimpleDateFormat df = new SimpleDateFormat(SQL_DATETIME);
        try {
            if (s.charAt(0) != '-') {
                // check
                ParsePosition pos = new ParsePosition(0);
                df.setLenient(false);
                if (df.parse(s,pos) == null)
                    return "";
                s = s.substring(0,pos.getIndex());
                log("query date = " + s);
                return s;
            }
            long ms = Integer.parseInt(s.substring(0, l - 1));
            switch (s.charAt(l - 1)) {
                case 'd':
                    ms *= 24;
                case 'h':
                    ms *= 60;
                case 'm':
                    ms *= 60;
                case 's':
                    ms *= 1000;
                    break;
                default:
                    throw new IllegalArgumentException();
            }
            ms += System.currentTimeMillis();
            return df.format(new Date(ms));
        } catch (Exception e) {
            log("Ignore invalid date input: " + s);
            return "";
        }
    }


    private void showPage(HttpServletResponse rsp, String xml, Templates tpl)
        throws Exception
    {
        if (tpl != null) {
            rsp.setContentType("text/html");
            //check for null char at end of xml that some implementations send
            if (xml.charAt(xml.length() - 1) == '\0')
                xml = xml.substring(0,xml.length()-1);
            //check for xml declaration at beginning
            Source xmlSource = null;
            if (xml.indexOf("<?xml ") == 0)
                xmlSource = new StreamSource(new PrefixStringReader(new char[0], xml));
            else
                xmlSource = new StreamSource(new PrefixStringReader(XML_PREFIX, xml));
            Result result = new StreamResult(rsp.getWriter());
            Transformer t = tpl.newTransformer();
            t.transform(xmlSource, result);
        }
        else {
            rsp.setContentType("text/xml");
            PrintWriter out = rsp.getWriter();
            out.print(xml);
            out.flush();
            out.close();
        }
    }


    private void showErrorPage(HttpServletResponse rsp, Throwable e)
        throws IOException
    {
        PrintWriter w = rsp.getWriter();
        w.println("<html><body><h1>An Error Has Occured</h1><pre>");
        e.printStackTrace(w);
        w.println("</pre></body></html>");
    }


    private QueryAuditRecordLocal getQueryAuditRecord()
    {
        if (query != null) {
            return query;
        }

        Context jndiCtx = null;
        try {
            jndiCtx = new InitialContext();
            QueryAuditRecordLocalHome home = (QueryAuditRecordLocalHome)
                    jndiCtx.lookup("java:comp/env/ejb/QueryAuditRecord");
            return (query = home.create());
        } catch (NamingException e) {
            log("Failed lookup ns:", e);
            throw new EJBException(e);
        } catch (CreateException e) {
            log("Failed create QueryAuditRecord EJB:", e);
            throw new EJBException(e);
        } finally {
            if (jndiCtx != null) {
                try {
                    jndiCtx.close();
                } catch (NamingException ignore) {}
            }
        }
    }
    
    private StoreAuditRecordLocal getStoreAuditRecord()
    {
        if (store != null) {
            return store;
        }
        Context jndiCtx = null;
        try {
            jndiCtx = new InitialContext();
            StoreAuditRecordLocalHome home = (StoreAuditRecordLocalHome)
                    jndiCtx.lookup("java:comp/env/ejb/StoreAuditRecord");
            return (store = home.create());
        } catch (CreateException e) {
            throw new EJBException(e);
        } catch (NamingException e) {
            throw new EJBException(e);
        } finally {
            if (jndiCtx != null) {
                try {
                    jndiCtx.close();
                } catch (NamingException ignore) {}
            }
        }
    }
}

