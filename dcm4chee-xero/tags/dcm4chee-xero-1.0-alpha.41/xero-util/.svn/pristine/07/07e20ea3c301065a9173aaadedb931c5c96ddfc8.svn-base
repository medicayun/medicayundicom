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
package org.dcm4chee.xero.template;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.servlet.StringTemplatePrintWriter;
import org.dcm4chee.xero.metadata.servlet.ServletResponseItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class renders a string template to the given ServletResponse
 * 
 * @author bwallace
 * 
 */
public class StringTemplateResponseItem implements ServletResponseItem {
	private static final Logger log = LoggerFactory
			.getLogger(StringTemplateResponseItem.class);
	StringTemplate st;
	StringTemplateGroup stg;
	String errorView;
	// Defaults to HTML type response - this returns text/html for IE and
	// text/xml otherwise
	String contentType = "xhtml";
	String characterEncoding = "UTF-8";

	public StringTemplateResponseItem(StringTemplate st) {
		this(st, null, null);
	}

	/**
	 * Creates a string template response, where if an error occurs, the
	 * exception is added to a new model and the errorView is looked up and the
	 * view rendered.
	 * 
	 * @param st
	 * @param stg
	 * @param errorView
	 */
	public StringTemplateResponseItem(StringTemplate st,
			StringTemplateGroup stg, String errorView) {
		this.st = st;
		this.stg = stg;
		this.errorView = errorView;
	}

	/** Writes the string template string to the given http response */
	public void writeResponse(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		long start = System.nanoTime();
		String templateName = st.getName();
		if (contentType.equals("xhtml")) {
			if (req.getHeader("USER-AGENT").indexOf("MSIE") >= 0) {
				resp.setContentType("text/html");
			} else {
				resp.setContentType("text/xml");
			}
		} else {
			resp.setContentType(contentType);
		}
		resp.setCharacterEncoding(characterEncoding);
		PrintWriter pw = resp.getWriter();
		StringTemplatePrintWriter stpw = new StringTemplatePrintWriter(pw);
		st.write(stpw);
		pw.close();
		long end = System.nanoTime();
		log.info("Templating/writing {} took {} ms", templateName,
				(end - start) / (1e6));
	}

	public StringTemplateGroup getStg() {
		return stg;
	}

	public void setStg(StringTemplateGroup stg) {
		this.stg = stg;
	}

	public String getErrorView() {
		return errorView;
	}

	public void setErrorView(String errorView) {
		this.errorView = errorView;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getCharacterEncoding() {
		return characterEncoding;
	}

	public void setCharacterEncoding(String characterEncoding) {
		this.characterEncoding = characterEncoding;
	}

	public StringTemplate getSt() {
		return st;
	}
}
