package org.dcm4chee.xero.metadata.servlet;

import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Figures out what source to use for various URI requests TODO High priority -
 * validate the path being requested to ensure it is locally valid and safe to
 * resolve. 
 */
public class UrlUriResolver implements URIResolver {
   private static Logger log = LoggerFactory.getLogger(UrlUriResolver.class);

   public static final String URIRESOLVER = "_URIResolver";

   HttpServletRequest request;
   HttpServletResponse response;

   ServletContext servletContext;

   public UrlUriResolver(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) {
	  this.request = request;
	  this.servletContext = servletContext;
	  this.response = response;
   }

   public Source resolve(String href, String base) throws TransformerException {
	  log.debug("Trying to resolve {}",href);
	  int firstSlash = href.indexOf('/');
	  RequestDispatcher dispatcher;
	  if (firstSlash != 0) {
		 log.debug("Resolving against relative request dispatcher {}",href);
		 dispatcher = request.getRequestDispatcher(href);
	  } else {
		 int secondSlash = href.indexOf('/', 1);
		 if (secondSlash < 0) {
			log.debug("Resolving a root module href: {}",href);
			ServletContext altContext = servletContext.getContext("/");
			dispatcher = altContext.getRequestDispatcher(href.substring(firstSlash));
		 } else {
			String relative = href.substring(secondSlash);
			String contextPath = href.substring(0, secondSlash);
			log.debug("Absolute path Context path={} relative url={}",contextPath, relative);
			if (contextPath.equals(request.getContextPath())) {
			   dispatcher = request.getRequestDispatcher(relative);
			} else {
			   ServletContext altContext = servletContext.getContext(contextPath);
			   if( altContext==null ) {
				  log.debug("Using root context for full path.");
				  relative = href;
				  altContext = servletContext.getContext("/");
			   }
			   dispatcher = altContext.getRequestDispatcher(relative);
			}
		 }
	  }

	  try {
		 CaptureHttpServletResponse captureResponse = new CaptureHttpServletResponse(response);
		 IncludeHttpServletRequest includeRequest = new IncludeHttpServletRequest(request, href);
		 dispatcher.include(includeRequest, captureResponse);
		 String resolvedResponse = captureResponse.getResponseString();
		 log.debug("Resolved response for {}  is {}", href, resolvedResponse);
		 StringReader reader = new StringReader(resolvedResponse);
		 return new StreamSource(reader);
	  } catch (IOException e) {
		 throw new RuntimeException("IO Exception on URL:" + href, e);
	  } catch (ServletException e) {
		 e.printStackTrace();
		 throw new RuntimeException("Caught servlet exception " + e, e);
	  }
   }
}

/**
 * This class removes all request attributes, replacing them with ones parsed
 * from the new URL
 */
class IncludeHttpServletRequest extends HttpServletRequestWrapper {
   private static final Logger log = LoggerFactory.getLogger(IncludeHttpServletRequest.class);

   private Map<String, String[]> parameterMap = new HashMap<String, String[]>();

   private String queryString = "";

   private String requestURI;

   /**
     * Add all the parameters from href into the parameter map, instead of the
     * original values
     */
   public IncludeHttpServletRequest(HttpServletRequest request, String href) {
	  super(request);
	  int qPos = href.indexOf("?");
	  requestURI = href;
	  if (qPos == -1)
		 return;
	  queryString = href.substring(qPos + 1);
	  log.debug("Include http servlet query string={}", queryString);
	  requestURI = href.substring(0, qPos);
	  if (href.length() == 0)
		 return;
	  String[] args = queryString.split("&");
	  for (String arg : args) {
		 int keyEnd = arg.indexOf('=');
		 if (keyEnd <= 0)
			continue;
		 String key = arg.substring(0, keyEnd);
		 String value = arg.substring(keyEnd + 1);
		 if (value.equals(""))
			continue;
		 value = value.replace("%2F", "/");
		 String[] vals = parameterMap.get(key);
		 if (vals == null) {
			log.debug("Putting parameter map info {} value {}", key, value);
			parameterMap.put(key, new String[] { value });
		 } else {
			log.debug("Extending parameter map info {} value '{}'",key,value);
			String[] newVals = new String[vals.length + 1];
			System.arraycopy(vals, 0, newVals, 0, vals.length);
			parameterMap.put(key, newVals);
		 }
	  }
   }

   @Override
   public String getQueryString() {
	  return queryString;
   }

   @Override
   public String getRequestURI() {
	  return requestURI;
   }

   @Override
   public String getParameter(String param) {
	  String[] val = parameterMap.get(param);
	  if (val == null || val.length == 0) {
		 log.debug("No value for {}",param);
		 return null;
	  }
	  log.debug("Returning parameter for {}={}",param,val[0]);
	  return val[0];
   }

   @Override
   public Map<String,String[]> getParameterMap() {
	  return parameterMap;
   }

   @Override
   public Enumeration<String> getParameterNames() {
	  throw new UnsupportedOperationException();
   }

   @Override
   public String[] getParameterValues(String arg0) {
	  return parameterMap.get(arg0);
   }

}

/**
 * Converts a regular output stream into a servlet output stream.
 * 
 * @author bwallace
 * 
 */
class ServletOutputStreamConverter extends ServletOutputStream {
   private OutputStream outputStream;

   public ServletOutputStreamConverter(OutputStream os) {
	  outputStream = os;
   }

   @Override
   public void write(int b) throws IOException {
	  outputStream.write(b);
   }

   @Override
   public void close() throws IOException {
	  super.close();
	  outputStream.close();
   }

   @Override
   public void flush() throws IOException {
	  super.flush();
	  outputStream.flush();
   }

   @Override
   public void write(byte[] b, int off, int len) throws IOException {
	  outputStream.write(b, off, len);
   }

   @Override
   public void write(byte[] b) throws IOException {
	  outputStream.write(b);
   }

}

/** This class just captures the output from the given http request */
class CaptureHttpServletResponse extends HttpServletResponseWrapper {
   
   public CaptureHttpServletResponse(HttpServletResponse response) {
	  super(response);
   }

   private ByteArrayOutputStream outputStream;

   private ServletOutputStreamConverter servletOutputStream;

   private CharArrayWriter writer;

   private PrintWriter printWriter;

   /** Return the string response sent from the servlet */
   public String getResponseString() {
	  if (writer != null) {
		 return writer.toString();
	  }
	  if (outputStream != null) {
		 try {
			return outputStream.toString("UTF-8");
		 } catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Should be able to decode UTF-8");
		 }
	  }
	  return "";
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

   public void sendError(int arg0) throws IOException {
   }

   public void sendError(int arg0, String arg1) throws IOException {
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
	  return "UTF-8";
   }

   public String getContentType() {
	  return null;
   }

   public Locale getLocale() {
	  return null;
   }

   public ServletOutputStream getOutputStream() throws IOException {
	  if (servletOutputStream == null) {
		 if (printWriter != null)
			throw new IOException("Can't use both output stream and writer.");
		 outputStream = new ByteArrayOutputStream();
		 servletOutputStream = new ServletOutputStreamConverter(outputStream);
	  }
	  return servletOutputStream;
   }

   public PrintWriter getWriter() throws IOException {
	  if (printWriter == null) {
		 if (servletOutputStream != null)
			throw new IOException("Can't use both writer and output stream.");
		 writer = new CharArrayWriter();
		 printWriter = new PrintWriter(writer);
	  }
	  return printWriter;
   }

   public boolean isCommitted() {
	  return false;
   }

   public void reset() {
	  throw new UnsupportedOperationException();
   }

   public void resetBuffer() {
	  throw new UnsupportedOperationException();
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
