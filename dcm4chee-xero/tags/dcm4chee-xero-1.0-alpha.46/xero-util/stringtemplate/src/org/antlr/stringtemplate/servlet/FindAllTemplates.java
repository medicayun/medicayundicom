package org.antlr.stringtemplate.servlet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.ASTExpr;
import org.antlr.stringtemplate.language.ActionEvaluatorTokenTypes;
import org.antlr.stringtemplate.language.ConditionalExpr;
import org.antlr.stringtemplate.language.StringRef;
import org.antlr.stringtemplate.language.StringTemplateAST;
import org.antlr.stringtemplate.language.StringTemplateToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import antlr.collections.AST;

/**
 * This class is used to find a list of all templates, starting with the given set of named templates.
 * 
 * @author bwallace
 */
public class FindAllTemplates {
   private static final Logger log = LoggerFactory.getLogger(FindAllTemplates.class);

   /** Recursively iterate through all the templates to find all possibly templates. */
   public static Map<String,StringTemplate> findAllTemplates(StringTemplateGroup stg, List<String> startingTemplates) {
	  Map<String,StringTemplate> ret = new HashMap<String,StringTemplate>();
	  for(String name : startingTemplates) {
		 addAllTemplates(stg, name,ret);
	  }
	  return ret;
   }
   
   /** Add the given template and all child templates to the template map */
   static void addAllTemplates(StringTemplateGroup stg, String name, Map<String,StringTemplate> templates) {
	  if( templates.containsKey(name) ) return;
	  log.debug("Adding template {}",name);
	  StringTemplate st = stg.getInstanceOf(name);
	  templates.put(name,st);
	  addAllTemplates(stg,st,templates);
   }
   
   static void addAllTemplates(StringTemplateGroup stg, StringTemplate st, Map<String,StringTemplate> templates) {
	  if( st==null || st.getChunks()==null ) return;
	  for(Object chunk : st.getChunks() ) {
		 if( chunk instanceof StringRef ) continue;
		 if( chunk instanceof ConditionalExpr ) {
			addAllTemplates(stg,(ConditionalExpr) chunk, templates);
			// This is in addition to the regular ASTExpr handle on the ast tree
		 }
		 ASTExpr astexpr = (ASTExpr) chunk;
		 AST ast = astexpr.getAST();
		 addAllTemplates(stg, ast, templates);
	  }
   }
   
   @SuppressWarnings("unchecked")
   static void addAllTemplates(StringTemplateGroup stg, ConditionalExpr ce, Map<String,StringTemplate> templates) {
	  StringTemplate st = ce.getSubtemplate();
	  addAllTemplates(stg,st,templates);
	  st = ce.getElseSubtemplate();
	  if( st!=null ) addAllTemplates(stg,st,templates);
	  List elseifs = ce.getElseIfSubtemplates();
	  if( elseifs==null ) return;
	  ConditionalExpr.ElseIfClauseData eic;
	  for(Object obj : elseifs ) {
		 eic = (ConditionalExpr.ElseIfClauseData) obj;
		 addAllTemplates(stg,eic.getSubtemplate(),templates);
	  }
   }
   
   static void addAllTemplates(StringTemplateGroup stg, AST ast, Map<String,StringTemplate> templates) {
	  if( ast==null ) return;
	  int astype = ast.getType();
	  if( astype==ActionEvaluatorTokenTypes.INCLUDE || astype==ActionEvaluatorTokenTypes.TEMPLATE) {
		 AST includeNameAst = ast.getFirstChild();
		 if( includeNameAst.getType()==ActionEvaluatorTokenTypes.ID ) {
			addAllTemplates(stg,includeNameAst.getText(),templates);
		 }
	  }
	  else if( astype==ActionEvaluatorTokenTypes.ANONYMOUS_TEMPLATE ) {
          StringTemplateAST stast = (StringTemplateAST) ast;
	      StringTemplate anonymous = stast.getStringTemplate(); 
	      if( anonymous==null ) {
	          String text = ast.getText();
              anonymous = new StringTemplate(stg,text);
              stast.setStringTemplate(anonymous);
	      }
          addAllTemplates(stg,anonymous,templates);
	  }
	  addAllTemplates(stg,ast.getFirstChild(),templates);
	  addAllTemplates(stg,ast.getNextSibling(),templates);
   }
   
}
