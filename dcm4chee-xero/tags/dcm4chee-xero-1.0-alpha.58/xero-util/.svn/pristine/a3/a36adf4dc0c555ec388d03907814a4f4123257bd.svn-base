package org.antlr.stringtemplate.servlet;

import java.util.HashMap;
import java.util.Map;

import org.antlr.stringtemplate.AttributeRenderer;
import org.antlr.stringtemplate.language.NewlineRef;
import org.antlr.stringtemplate.language.StringRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StringSafeRenderer implements AttributeRenderer {
   private static final Logger log = LoggerFactory.getLogger(StringSafeRenderer.class);
   
   String defaultFormat;
   
   public StringSafeRenderer() {
   }
   
   public StringSafeRenderer(String defaultFormat) {
	  this.defaultFormat = defaultFormat;
   }
   
   /** A common set of renderers that render to JS safe strings */
   public static Map<Class<?>,AttributeRenderer> RENDERERS = new HashMap<Class<?>,AttributeRenderer>();
   static {
	  StringSafeRenderer j = new StringSafeRenderer();
	  RENDERERS.put(String.class, j);
	  RENDERERS.put(StringRef.class, j);
	  RENDERERS.put(NewlineRef.class, j);
   }

   public static Map<Class<?>,AttributeRenderer> JS_RENDERERS = new HashMap<Class<?>,AttributeRenderer>();
   static {
	  StringSafeRenderer j = new StringSafeRenderer("js");
	  JS_RENDERERS.put(String.class, j);
	  JS_RENDERERS.put(StringRef.class, j);
	  JS_RENDERERS.put(NewlineRef.class, j);
   }

   public String toString(Object o) {
	  return toString(o,defaultFormat);
   }
   
   /** JavaScript safe version */
   public static String toJSString(Object o) {
	  String src = o.toString();
	  StringBuffer ret = new StringBuffer(src.length()+2);
	  for(int i=0, n=src.length(); i<n; i++) {
		 char ch = src.charAt(i);
		 switch(ch) {
		 case '\n':
			ret.append("\\n");
			break;
		 case '\r':
			break;
		 case '\\':
			ret.append("\\\\");
			break;
		 case '"':
			ret.append("\\\"");
			break;
		 case '\'':
			ret.append("\\'");
			break;
		 default:
			ret.append(ch);
		 }
	  }
	  return ret.toString();
   }

   /** Convert o into a string using the given format name. */
   public String toString(Object o, String formatName) {
	  if( formatName==null ) return o.toString();
	  if( formatName.equals("js") ) return toJSString(o);
	  if( formatName.equals("xml") ) return toXmlString(o);
	  if( formatName.equals("underline") ) return toUnderlineString(o);
	  log.warn("Unknown format name {}", formatName);
	  return o.toString();
   }
   
   /** Escape spaces to underlines so that the name can be used as a template name */
   public static String toUnderlineString(Object o) {
      String src = o.toString();
      StringBuffer ret = new StringBuffer(src.length());
      for(int i=0; i<src.length(); i++) {
         char ch = src.charAt(i);
         if( ch==0 || Character.isSpaceChar(ch) ) ret.append("_");
         else ret.append(ch);
      }
      return ret.toString();
   }

   /** Escape the XML relevant characters so that the string is XML safe. */
   public static String toXmlString(Object o) {
	  String src = o.toString();
	  boolean changed = false;
	  for(int i=0, n=src.length(); i<n; i++) {
		 char ch = src.charAt(i);
		 if( ch=='&' || ch=='<' || ch==0 ) {
			changed=true;
			break;
		 }
	  }
	  if( !changed ) return src;
	  StringBuffer ret = new StringBuffer(src.length()+2);
	  for(int i=0, n=src.length(); i<n; i++) {
		 char ch = src.charAt(i);
		 switch(ch) {
		 case '&':
			ret.append("&amp;");
			break;
		 case '<':
			ret.append("&lt;");
			break;
		 case 0:
		    break;
		 default:
			ret.append(ch);
		 }
	  }
	  return ret.toString();
   }

}
