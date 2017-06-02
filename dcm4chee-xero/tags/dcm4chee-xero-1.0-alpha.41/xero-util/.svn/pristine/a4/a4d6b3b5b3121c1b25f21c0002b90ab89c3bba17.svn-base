package org.antlr.stringtemplate.servlet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.js.NameJSTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This servlet takes a stringtemplate object and uses it as a DATA structure
 * and applies the given, named template.
 * 
 * @author bwallace
 * 
 */
@SuppressWarnings("serial")
public class JSTemplateServlet extends StringTemplateServlet {
   private static final Logger log = LoggerFactory.getLogger(JSTemplateServlet.class);
   protected Map<String, StringTemplate> templateData;
   
   long lastLookup;

   List<String> startTemplates;

   /** The name of the template group that provides the given templates */
   String dataName;

   /** The name of the templates variable to be created.  If null, then don't declare the variable, but
    * render as JSON data.
    */
   String jsName;
   /**
    * The stgData group contains the OTHER template to be rendered into
    * JavaScript by stg. This data should also refresh, but it doesn't appear to
    * do so right now.
    */
   protected StringTemplateGroup stgData;

   public JSTemplateServlet() {
	  this.templates = "jstemplate";
	  this.contentType = "text/javascript";
	  this.view = "jstemplate";
   }

   /**
    * Creates the model to use for the given request. By default just returns
    * the parameter map.
    */
   protected synchronized Map<String, Object> createModel(HttpServletRequest req, HttpServletResponse resp) {
	  Map<String, Object> ret = new HashMap<String, Object>();
	  ret.put("templatesName", jsName);
	  ret.put("nameJSTemplate", new NameJSTemplate());
	  long now = System.currentTimeMillis();
	  if( (now-lastLookup)/1000 > refreshIntervalInSeconds ) {
		  log.info("Reloading template data.");
		  templateData = FindAllTemplates.findAllTemplates(stgData, startTemplates);
		  lastLookup = now;
	  }
	  ret.put("templates", templateData);
	  return ret;
   }

   /**
    * Initialize the string template groups to be used to render the JavaScript
    * text
    */
   public void init(ServletConfig config) throws ServletException {
	  super.init(config);
	  stg.setAttributeRenderers(StringSafeRenderer.JS_RENDERERS);

	  String test = config.getInitParameter("dataName");
	  if (test != null)
		 dataName = test;
	  assert dataName != null;

	  stgData = createStringTemplateGroup(dataName);

	  startTemplates = new ArrayList<String>(1);
	  test = config.getInitParameter("startTemplates");
	  if (test != null) {
		 String[] splits = test.split(",");
		 for (String split : splits)
			startTemplates.add(split);
	  }
	  assert startTemplates != null;
	  templateData = FindAllTemplates.findAllTemplates(stgData, startTemplates);
	  lastLookup = System.currentTimeMillis();
	  
	  jsName = config.getInitParameter("jsName");
   }

}
