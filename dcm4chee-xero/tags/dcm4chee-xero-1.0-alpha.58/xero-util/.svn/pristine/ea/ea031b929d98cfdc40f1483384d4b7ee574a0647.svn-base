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
package org.dcm4chee.xero.metadata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** A factory instance provides a new, unique value on every request */
public class FactoryValueProvider implements ValueProvider {
   private static Logger log = LoggerFactory.getLogger(FactoryValueProvider.class);

   /**
    * Convert the class name into a class object so that we have the metadata
    * etc
    */
   public Object preConvertValue(MetaDataBean mdb, Object sourceValue) {
	  if (!(sourceValue instanceof String))
		 return null;
	  String sourceValueStr = ((String) sourceValue).trim();
	  if ((!sourceValueStr.startsWith("${factory:"))) {
		 return null;
	  }
	  if (!sourceValueStr.endsWith("}"))
		 return null;
	  String className;
	  className = sourceValueStr.substring(10, sourceValueStr.length() - 1);
	  try {
		 Class<?> clazz = Class.forName(className);
		 log.debug("Found factory class for meta-data value " + className);
		 return clazz;
	  } catch (ClassNotFoundException e) {
		 log.warn("No class found for '" + className +"'");
		 return null;
	  }
   }

   /** Get the extra meta-data associated with this object */
   public MetaDataProvider getExtraMetaDataProvider(MetaDataBean mdb, Object convertedValue, Object originalValue) {
	  return Injector.getInjector((Class<?>)convertedValue);
   }

   /**
    * Create an instance of the provided class name. TODO inject values as
    * appropriate (maybe - if not already done elsewhere)
    */
   public Object convertValue(MetaDataBean mdb, Object sourceValue) {
	  try {
		 Class<?> clazz = (Class<?>) sourceValue;
		 log.debug("Creating a new instance of {}",clazz);
		 Object instance = clazz.newInstance();
		 mdb.inject(instance);
		 return instance;
	  } catch (InstantiationException e) {
		 e.printStackTrace();
		 return null;
	  } catch (IllegalAccessException e) {
		 e.printStackTrace();
		 return null;
	  }
   }

}
