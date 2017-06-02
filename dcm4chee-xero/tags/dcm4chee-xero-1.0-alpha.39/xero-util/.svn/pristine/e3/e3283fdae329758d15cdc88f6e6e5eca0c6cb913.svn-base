package org.antlr.stringtemplate.servlet;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpSession;

/** Provides a map based interface the the HttpSession.
 * 
 * @author bwallace
 */
public class SessionMap extends AbstractMap<String,Object> {
   HttpSession httpSession;
   
   /** Create a map view onto an http session */
   public SessionMap(HttpSession httpSession) {
	  this.httpSession = httpSession;
   }
   
   @Override
   public Object get(Object key) {
	  	return httpSession.getAttribute((String) key);
   }
   
   @Override
   public Object put(String key, Object value) {
	  httpSession.setAttribute(key,value);
	  return null;
   }

   @Override
   public Set<Map.Entry<String,Object> > entrySet() {
	  throw new UnsupportedOperationException("entrySet not yet supported.");
   }
   
}
