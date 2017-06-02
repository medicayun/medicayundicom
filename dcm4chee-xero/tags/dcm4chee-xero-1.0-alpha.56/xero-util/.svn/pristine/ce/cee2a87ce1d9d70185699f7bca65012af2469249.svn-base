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
package org.dcm4chee.xero.metadata;

/**
 * A value provider takes a static instance value, and figures out the actual, return
 * value to use.  This can be a lookup in JNDI, or in Seam Contexts or other such
 * places.
 * @author bwallace
 *
 */
public interface ValueProvider {
	/** Converts values from the source type to the destination type. 
	 * This is typically used for things like Seam expresison lookup.  This only needs
	 * to handle a preConvertValue output. 
	 */
	Object convertValue(MetaDataBean mdb, Object sourceValue);
	
	/** Pre-converts the value - this sets things up like expression parsing so as to allow fast lookup later. 
	 * Should default to just replying with the sourceValue.  
	 * @return non-null converted value is this object handles sourceValue.
	 */
	Object preConvertValue(MetaDataBean mdb, Object sourceValue);
	
	/**
	 * Add in any meta-data appropriate for this object.  This is added
	 * after all other meta-data has been added.
	 */
	MetaDataProvider getExtraMetaDataProvider(MetaDataBean mdb, Object convertedValue, Object originalValue);
}
