// ***** BEGIN LICENSE BLOCK *****
// Version: MPL 1.1/GPL 2.0/LGPL 2.1
// 
// The contents of this file are subject to the Mozilla Public License Version 
// 1.1 (the "License"); you may not use this file except in compliance with 
// the License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
// 
// Software distributed under the License is distributed on an "AS IS" basis,
// WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
// for the specific language governing rights and limitations under the
// License.
// 
// The Original Code is part of dcm4che, an implementation of DICOM(TM) in Java(TM), hosted at http://sourceforge.net/projects/dcm4che
//  
// The Initial Developer of the Original Code is Agfa Healthcare.
// Portions created by the Initial Developer are Copyright (C) 2009 the Initial Developer. All Rights Reserved.
// 
// Contributor(s):
// Andrew Cowan <andrew.cowan@agfa.com>
// 
// Alternatively, the contents of this file may be used under the terms of
// either the GNU General Public License Version 2 or later (the "GPL"), or
// the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
// in which case the provisions of the GPL or the LGPL are applicable instead
// of those above. If you wish to allow use of your version of this file only
// under the terms of either the GPL or the LGPL, and not to allow others to
// use your version of this file under the terms of the MPL, indicate your
// decision by deleting the provisions above and replace them with the notice
// and other provisions required by the GPL or the LGPL. If you do not delete
// the provisions above, a recipient may use your version of this file under
// the terms of any one of the MPL, the GPL or the LGPL.
// 
// ***** END LICENSE BLOCK *****
package org.dcm4chee.xero.servlet;

import static org.easymock.EasyMock.*;
import static org.testng.Assert.*;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.MDC;
import org.dcm4chee.xero.servlet.LoggingDiagnosticContextFilter;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Andrew Cowan (andrew.cowan@agfa.com)
 */
public class LoggingDiagnosticContextFilterTest
{
   private LoggingDiagnosticContextFilter filter;
   private FilterChain chain;
   
   @BeforeMethod
   public void setup()
   {
      filter = new LoggingDiagnosticContextFilter();
      chain = createNiceMock(FilterChain.class);
   }
   
   /**
    * Test method for {@link org.dcm4chee.xero.servlet.LoggingDiagnosticContextFilter#filter(org.dcm4chee.xero.metadata.filter.FilterItem, java.util.Map)}.
    * @throws ServletException 
    * @throws IOException 
    */
   @Test
   public void doFilter_MustReadHostFromServletRequest() throws IOException, ServletException
   {
      String expectedHost = "marlin";
      ServletRequest req = createNiceMock(HttpServletRequest.class);
      ServletResponse res = createNiceMock(HttpServletResponse.class);
      expect(req.getRemoteHost()).andStubReturn(expectedHost);
      replay(req);
      filter.doFilter(req,res,chain);
      assertEquals(MDC.get("remote.host"),expectedHost);
   }
   
   @Test
   public void doFilter_MustReadRemoteAddressFromServletRequest() throws IOException, ServletException
   {
      String expectedAddress = "10.237.229.49";
      ServletRequest req = createNiceMock(HttpServletRequest.class);
      ServletResponse res = createNiceMock(HttpServletResponse.class);
      expect(req.getRemoteAddr()).andStubReturn(expectedAddress);
      replay(req);
      filter.doFilter(req,res,chain);
      assertEquals(MDC.get("remote.address"),expectedAddress);
   }

   @Test
   public void doFilter_MustReadRemoteUserFromServletRequest() throws ServletException, IOException
   {
      String expectedUser = "Andrew";
      HttpServletRequest req = createNiceMock(HttpServletRequest.class);
      ServletResponse res = createNiceMock(HttpServletResponse.class);
      expect(req.getRemoteUser()).andStubReturn(expectedUser);
      replay(req);
      filter.doFilter(req,res,chain);
      assertEquals(MDC.get("remote.user"),expectedUser);
   }
   
   @Test
   public void doFilter_MustAddSessionIDToMDC() throws IOException, ServletException
   {
      String expectedSessionID = "xxxxxxxx5555xxxxxx";
      HttpSession session = createNiceMock(HttpSession.class);
      expect(session.getId()).andStubReturn(expectedSessionID);
      HttpServletRequest req = createNiceMock(HttpServletRequest.class);
      ServletResponse res = createNiceMock(HttpServletResponse.class);
      expect(req.getSession(false)).andStubReturn(session);
      replay(req,session);
      filter.doFilter(req,res,chain);
      assertEquals(MDC.get("session.id"),expectedSessionID);
   }
   
   @Test
   public void doFilter_MustInvokeNextFilterInChain() throws IOException, ServletException
   {
      HttpServletRequest req = createNiceMock(HttpServletRequest.class);
      ServletResponse res = createNiceMock(HttpServletResponse.class);
      chain.doFilter(isA(ServletRequest.class), isA(ServletResponse.class));
      replay(chain,req,res);
      filter.doFilter(req,res,chain);
      verify(chain);
   }
   
   @Test(enabled=false)
   public void scratch()
   {

      String SamplerData = "JSESSIONID=ABAA1C9C9279955117902C7645C9B042; $Path";
      Pattern p = Pattern.compile("JSESSIONID=(.+?);");
      Matcher m = p.matcher(SamplerData);
      String sessionID = m.group(0);
      System.out.println("The session ID is "+sessionID);
   }
}
