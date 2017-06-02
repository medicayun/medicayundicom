package org.antlr.stringtemplate.servlet;

import java.util.HashMap;

import org.antlr.stringtemplate.language.ActionEvaluatorTokenTypes;

/**
 * This names the template to use to handle the given object node type.
 * Unfortunately, nodes aren't sufficiently distinguishable by the view to always call the
 * right template -there isn't a choose method, so this creates one.
 * @author bwallace
 *
 */
@SuppressWarnings("serial")
public class NameJSTemplate extends HashMap<Object,String> {

   public NameJSTemplate() {
	  put(ActionEvaluatorTokenTypes.ANONYMOUS_TEMPLATE,"anonymousTemplate");
	  put(ActionEvaluatorTokenTypes.CONDITIONAL, "conditionalTemplate");
   }
   
   /** Gets the name of the template to use for the given object type. */
   @Override
   public String get(Object key) {
	  String ret = super.get(key);
	  if( ret!=null ) return ret;
	  return "ast";
   }
   
   @Override
   public boolean containsKey(Object key) {
	  return true;
   }
   
   public void put(int key, String value) {
	  this.put(Integer.toString(key),value);
   }
}
