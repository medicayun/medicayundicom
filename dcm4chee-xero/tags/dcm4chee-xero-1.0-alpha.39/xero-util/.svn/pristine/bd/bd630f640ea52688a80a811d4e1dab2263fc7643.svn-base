package org.antlr.stringtemplate.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import org.antlr.stringtemplate.StringTemplateWriter;

public class StringTemplatePrintWriter implements StringTemplateWriter {
   PrintWriter pw;
   
   public StringTemplatePrintWriter(PrintWriter pw) {
	  this.pw = pw;
   }

   public void popAnchorPoint() {
   }

   public String popIndentation() {
	  return "";
   }

   public void pushAnchorPoint() {
   }

   public void pushIndentation(String indent) {
   }

   public void setLineWidth(int lineWidth) {
   }

   public int write(String str) throws IOException {
	  pw.print(str);
	  return str.length();
   }

   public int write(String str, String wrap) throws IOException {
	  pw.print(str);
	  return str.length();
   }

   public int writeSeparator(String str) throws IOException {
	  pw.print(str);
	  return str.length();
   }

   public int writeWrapSeparator(String wrap) throws IOException {
	  return 0;
   }

}
