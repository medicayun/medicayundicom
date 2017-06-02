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
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Bill Wallace, Agfa HealthCare Inc., 
 * Portions created by the Initial Developer are Copyright (C) 2008
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Bill Wallace <bill.wallace@agfa.com>
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
package org.dcm4chee.xero.metadata.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Captures the output of a servlet response item - the error code, output stream etc.  This is typically used
 * for unit testing, not for primary use, but extensions to this class might be more useable for primary use.
 * @author bwallace
 *
 */
public class CaptureSerlvetResponseItem implements HttpServletResponse {

	public int code = 0;
	public String errorMessage;
	
	/** Either a string writer or the byte output stream will be used, but not both, and sometimes neither if an error is sent. */
	CaptureServletOutputStream outputStream;
	PrintWriter pw;
	
	public CaptureSerlvetResponseItem(ServletResponseItem sri) throws IOException {
		this(new HttpServletRequestImpl(),sri);
	}
	
	/** Capture the servlet response immediately, being able to get the code, errorMessage, strin content etc */
	public CaptureSerlvetResponseItem(HttpServletRequest request, ServletResponseItem sri) throws IOException {
		sri.writeResponse(request,this);
	}
	
	/** Gets the string content */
	public String getStringContent() {
		if( outputStream==null ) return null;
		return outputStream.getByteArrayOutputStream().toString();
	}
	
	/** Gets the byte[] content */
	public byte[] getByteArrayContent() {
		if( outputStream==null ) return null;
		return outputStream.getByteArrayOutputStream().toByteArray();
	}

	public void addCookie(Cookie arg0) {
	}

	public void addDateHeader(String arg0, long arg1) {
	}

	public void addHeader(String arg0, String arg1) {
	}

	public void addIntHeader(String arg0, int arg1) {
	}

	public boolean containsHeader(String arg0) {
		return false;
	}

	public String encodeRedirectURL(String arg0) {
		return null;
	}

	public String encodeRedirectUrl(String arg0) {
		return null;
	}

	public String encodeURL(String arg0) {
		return null;
	}

	public String encodeUrl(String arg0) {
		return null;
	}

	public void sendError(int code) throws IOException {
		this.sendError(code,null);
	}

	public void sendError(int code, String errorMessage) throws IOException {
		this.code = code;
		this.errorMessage = errorMessage;
	}

	public void sendRedirect(String arg0) throws IOException {
	}

	public void setDateHeader(String arg0, long arg1) {
	}

	public void setHeader(String arg0, String arg1) {
	}

	public void setIntHeader(String arg0, int arg1) {
	}

	public void setStatus(int arg0) {
	}

	public void setStatus(int arg0, String arg1) {
	}

	public void flushBuffer() throws IOException {
	}

	public int getBufferSize() {
		return 0;
	}

	public String getCharacterEncoding() {
		return null;
	}

	public String getContentType() {
		return null;
	}

	public Locale getLocale() {
		return null;
	}

	public ServletOutputStream getOutputStream() throws IOException {
		if( outputStream==null ) {
			outputStream = new CaptureServletOutputStream();
		}
		return outputStream;
	}

	public PrintWriter getWriter() throws IOException {
		if( pw==null ) {
			pw = new PrintWriter(getOutputStream());
		}
		return pw;
	}

	public boolean isCommitted() {
		return false;
	}

	public void reset() {	
	}

	public void resetBuffer() {
	}

	public void setBufferSize(int arg0) {
	}

	public void setCharacterEncoding(String arg0) {
	}

	public void setContentLength(int arg0) {
	}

	public void setContentType(String arg0) {
	}

	public void setLocale(Locale arg0) {
	}
}
