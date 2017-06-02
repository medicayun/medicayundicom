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
 * Portions created by the Initial Developer are Copyright (C) 2007
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
package org.dcm4chee.xero.metadata.filter;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is a filter return item where there is a primary item defined,
 * plus a set of string values attributes associated with this object, and
 * finally, a "query" string - something that defines this object.
 * 
 * @author bwallace
 * 
 */
public class FilterReturn<T> {
	private static Logger log = LoggerFactory.getLogger(FilterReturn.class.getName());
	private T value;

	private Map<String, Object> parameterMap = new HashMap<String, Object>();

	// Assume parameters each take 128 bytes.
	static long MAP_SIZE = 128;

	/** Creates a filter return instance */
	public FilterReturn(T initialValue) {
		setValue(initialValue);
	}

	/** Creates a filter return instance -used for cloning */
	protected FilterReturn(FilterReturn<T> fr) {
		this(fr.getValue());
		getParameterMap().putAll(fr.getParameterMap());
	}

	/** Copies a filter return instance */
	public FilterReturn<T> clone() {
		return new FilterReturn<T>(this);
	}

	/** Returns the primary value of this object */
	public T getValue() {
		return value;
	}

	/** Sets the primary value of this object */
	public void setValue(T value) {
		this.value = value;
	}

	/**
	 * Gets the parameter map. This is not a copy, and is directly modifiable to
	 * affect the internal parameters.
	 */
	public Map<String, Object> getParameterMap() {
		return parameterMap;
	}

	/** Gets a parameter value */
	public Object getParameter(String key) {
		return parameterMap.get(key);
	}

	/** Gets a double parameter, with a default */
	public double getParameter(String key, double def) {
		Object ovalue = getParameter(key);
		if( ovalue==null ) return def;
		if (ovalue instanceof Number)
			return ((Number) ovalue).doubleValue();
		if (ovalue instanceof String) {
			return Double.parseDouble((String) ovalue);
		}
		else {
			log.warn("Unknown type to convert "+ovalue.getClass().getName());
			return def;
		}
	}

	/** Gets a float parameter, with a default */
	public float getParameter(String key, float def) {
		Object ovalue = getParameter(key);
		if( ovalue==null ) return def;
		if (ovalue instanceof Number)
			return ((Number) ovalue).floatValue();
		if (ovalue instanceof String) {
			return Float.parseFloat((String) ovalue);
		}
		else {
			log.warn("Unknown type to convert "+ovalue.getClass().getName());
			return def;
		}
	}

	/** Sets a parameter value */
	public void setParameter(String key, Object value) {
		parameterMap.put(key, value);
	}

}
