package org.dcm4chee.xero.metadata.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * This is a simple implementation of an HttpServletRequest that just has a user agent and set of 
 * headers.  Useful for filters that access the request in order to do some language or other validation
 * but where the original request isn't the correct one to use.
 * @author bwallace
 *
 */
public class HttpServletRequestImpl implements HttpServletRequest {
	
	Map<String,Object> headers = new HashMap<String,Object>();
	private StringBuffer requestUrl;
	private String queryString;
	
	/** Create a HttpServletRequestImpl */
	public HttpServletRequestImpl() {
		headers.put("USER-AGENT", "HttpServletRequestImpl");
	}

	public String getAuthType() {
		return null;
	}

	public String getContextPath() {
		return null;
	}

	public Cookie[] getCookies() {
		return null;
	}

	public long getDateHeader(String arg0) {
		return 0;
	}

	public String getHeader(String key) {
		return (String) headers.get(key);
	}

	public Enumeration<String> getHeaderNames() {
		return null;
	}

	public Enumeration<String> getHeaders(String arg0) {
		return null;
	}

	public int getIntHeader(String arg0) {
		return 0;
	}

	public String getMethod() {
		return null;
	}

	public String getPathInfo() {
		return null;
	}

	public String getPathTranslated() {
		return null;
	}
	
	public void setQueryString(String queryString) {
		this.queryString = queryString;
	}

	public String getQueryString() {
		return queryString;
	}

	public String getRemoteUser() {
		return null;
	}

	public String getRequestURI() {
		return null;
	}

	/** Sets the request URL - does not set the URI, path etc, only sets the URL */
	public void setRequestURL(String url) {
		this.requestUrl = new StringBuffer(url);
	}
	
	public StringBuffer getRequestURL() {
		return requestUrl;
	}

	public String getRequestedSessionId() {
		return null;
	}

	public String getServletPath() {
		return null;
	}

	public HttpSession getSession() {
		return null;
	}

	public HttpSession getSession(boolean arg0) {
		return null;
	}

	public Principal getUserPrincipal() {
		return null;
	}

	public boolean isRequestedSessionIdFromCookie() {
		return false;
	}

	public boolean isRequestedSessionIdFromURL() {
		return false;
	}

	public boolean isRequestedSessionIdFromUrl() {
		return false;
	}

	public boolean isRequestedSessionIdValid() {
		return false;
	}

	public boolean isUserInRole(String arg0) {
		return false;
	}

	public Object getAttribute(String arg0) {
		return null;
	}

	public Enumeration<String> getAttributeNames() {
		return null;
	}

	public String getCharacterEncoding() {
		return null;
	}

	public int getContentLength() {
		return 0;
	}

	public String getContentType() {
		return null;
	}

	public ServletInputStream getInputStream() throws IOException {
		return null;
	}

	public String getLocalAddr() {
		return null;
	}

	public String getLocalName() {
		return null;
	}

	public int getLocalPort() {
		return 80;
	}

	public Locale getLocale() {
		return null;
	}

	public Enumeration<Locale> getLocales() {
		return null;
	}

	public String getParameter(String arg0) {
		return null;
	}

	public Map<String,String[]> getParameterMap() {
		return null;
	}

	public Enumeration<String> getParameterNames() {
		return null;
	}

	public String[] getParameterValues(String arg0) {
		return null;
	}

	public String getProtocol() {
		return null;
	}

	public BufferedReader getReader() throws IOException {
		return null;
	}

	public String getRealPath(String arg0) {
		return null;
	}

	public String getRemoteAddr() {
		return null;
	}

	public String getRemoteHost() {
		return null;
	}

	public int getRemotePort() {
		return 0;
	}

	public RequestDispatcher getRequestDispatcher(String arg0) {
		return null;
	}

	public String getScheme() {
		return null;
	}

	public String getServerName() {
		return null;
	}

	public int getServerPort() {
		return 0;
	}

	public boolean isSecure() {
		return false;
	}

	public void removeAttribute(String arg0) {
	}

	public void setAttribute(String arg0, Object arg1) {
	}

	public void setCharacterEncoding(String arg0)
			throws UnsupportedEncodingException {
	}

}
