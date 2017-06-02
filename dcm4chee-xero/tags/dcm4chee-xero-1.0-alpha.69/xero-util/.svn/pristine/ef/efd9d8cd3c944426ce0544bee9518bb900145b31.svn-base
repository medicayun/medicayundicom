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
package org.dcm4chee.xero.rhino;

import java.util.Map;

import org.apache.struts.flow.sugar.SugarWrapFactory;
import org.dcm4chee.xero.metadata.MetaData;
import org.dcm4chee.xero.metadata.access.MapFactory;
import org.dcm4chee.xero.util.StringUtil;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A JavaScriptMapFactory is an object that knows how to export a JavaScript
 * execution environment into a MapWithDefaults. Execution context for the
 * JavaScript can be multi-layered. The parent layer is specified by name.
 * 
 * Additionally, the child script is executed with the current src model
 * available as JavaScriptMapFactory. This allows variables from other contexts
 * to be used.
 * 
 * The base context that is created includes by default the Struts Sugar access.
 * This can be turned off if required.
 * 
 * @author bwallace
 * 
 */
public class JavaScriptMapFactory implements MapFactory<JavaScriptObjectWrapper> {
	private static final Logger log = LoggerFactory.getLogger(JavaScriptMapFactory.class);

	boolean useSugarWrap = true;

	String parentScopeName;

	boolean modifiable = true;

	String script;
	Script mscript;

	/**
	 * The name of the script being executed - defaults to the path of the
	 * element
	 */
	String scriptName = "<script>";

	boolean compile = true;

	/** Declare a context factory that has the dynamic scope feature */
	public static final ContextFactory contextFactory = new ContextFactory() {
		@Override
		protected boolean hasFeature(Context cx, int featureIndex) {
			if (featureIndex == Context.FEATURE_DYNAMIC_SCOPE) {
				return true;
			}
			return super.hasFeature(cx, featureIndex);
		}
	};

	Map<String, String> imported;

	public static final SugarWrapFactory SUGAR_WRAP_FACTORY = new SugarWrapFactory();

	/**
	 * Creates a JavaScriptObjectWrapper containing a new scope in which the
	 * given script has been executed (if provided), and for which the parent
	 * scope has been set. Adds the sugar wrap if the setting is true.
	 */
	public JavaScriptObjectWrapper create(Map<String, Object> src) {
		Context cx = contextFactory.enterContext();
		if (useSugarWrap)
			cx.setWrapFactory(SUGAR_WRAP_FACTORY);
		Scriptable scope;
		if (parentScopeName != null) {
			Scriptable parentScope = getParentScope(src);
			scope = cx.newObject(parentScope);
			scope.setPrototype(parentScope);
		} else {
			scope = cx.initStandardObjects();
		}

		JavaScriptObjectWrapper ret = new ParentScripter(scope,script,scriptName);
		if (imported != null) {
			for (Map.Entry<String, String> me : imported.entrySet()) {
				Object value = src.get(me.getValue());
				ret.put(me.getKey(), value);
			}
		}

		try {
			if (compile) {
				synchronized (script) {
					if (mscript == null) {
						cx.setWrapFactory(SUGAR_WRAP_FACTORY);
						mscript = cx.compileString(script, scriptName, 1, null);
					}
				}
			}
			if (mscript != null) {
				mscript.exec(cx, scope);
			} else if (script != null) {
				cx.evaluateString(scope, script, scriptName, 1, null);
			}
		} catch (RhinoException e) {
			String errorScript = getNamedScript(src,e.sourceName());
			if( errorScript==null ) {
				log.warn("Unable to find script "+e.sourceName());
				throw e;
			}
			displayRhinoException(log, e, errorScript);
		}

		if (!modifiable)
			((ScriptableObject) scope).sealObject();
		return ret;
	}

	/** Displays the line number that is problematic in the rhino script */
	public static void displayRhinoException(Logger log, RhinoException e, String script) {
		int line = e.lineNumber();
		log.warn("Caught Rhino exception {}", e.getMessage(), line);
		String[] splits = StringUtil.split(script, '\n', true);
		for (int i = Math.max(0, line - 5), n = Math.min(splits.length, line + 5); i < n; i++) {
			log.warn("{}: {}", i + 1, splits[i]);
		}
		throw e;
	}

	/** Gets the parent context */
	protected Scriptable getParentScope(Map<String, Object> src) {
		Object ret = src.get(parentScopeName);
		if (ret instanceof JavaScriptObjectWrapper)
			return ((JavaScriptObjectWrapper) ret).scriptable;
		return (Scriptable) ret;
	}
	
	/**
	 * Gets the script text for the named script, or null if it can't be found.
	 */
	protected String getNamedScript(Map<String, Object> src, String namedScript) {
		if( namedScript.equals(this.scriptName) ) return script;
		Object parent = src.get(parentScopeName);
		if( parent instanceof ParentScripter ) {
			ParentScripter ps = (ParentScripter) parent;
			if( ps.scriptName.equals(namedScript) ) return ps.script;
		}
		return null;
	}

	public boolean isUseSugarWrap() {
		return useSugarWrap;
	}

	@MetaData(required = false)
	public void setUseSugarWrap(boolean useSugarWrap) {
		this.useSugarWrap = useSugarWrap;
	}

	public String getParentScopeName() {
		return parentScopeName;
	}

	@MetaData(required = false)
	public void setParentScopeName(String parentScopeName) {
		this.parentScopeName = parentScopeName;
	}

	public boolean isModifiable() {
		return modifiable;
	}

	@MetaData(required = false)
	public void setModifiable(boolean modifiable) {
		this.modifiable = modifiable;
	}

	public String getScript() {
		return script;
	}

	@MetaData(required = false)
	public void setScript(String script) {
		this.script = script;
	}

	@MetaData(value = "_path")
	public void setScriptName(String path) {
		this.scriptName = path;
	}

	/**
	 * Sets the values to be imported from the src map before executing the
	 * script. WARNING: The imported values MUST not depend on this object - that
	 * is, you can't successfully create a circular reference.
	 */
	@MetaData(required = false)
	public void setImported(Map<String, String> imported) {
		this.imported = imported;
	}
	
	static class ParentScripter extends JavaScriptObjectWrapper {
		public ParentScripter(Scriptable scriptable, String script, String scriptName) {
			super(scriptable);
			this.script = script;
			this.scriptName = scriptName;
      }
		public String script;
		public String scriptName;
	}
}
