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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Injector knows how to inject meta-data attributes into a class.
 * 
 * @author bwallace
 */
public class Injector implements MetaDataProvider {
   static final Logger log = LoggerFactory.getLogger(Injector.class);

   static final Map<Class<?>, Injector> classMap = new HashMap<Class<?>, Injector>();

   protected Class<?> clazz;

   protected Map<Method, String> metaDataItems = new HashMap<Method, String>();

   Map<String, String> outjectMetaData;

   /**
    * Create an injector by reading the meta-data attributes and figuring out
    * what name to use for each attribute.
    * 
    * @param clazz
    */
   private Injector(Class<?> clazz) {
	  this.clazz = clazz;
	  Method[] methods = clazz.getMethods();
	  log.debug("Injector examining class " + clazz);
	  for (Method m : methods) {
		 log.debug("Injector method " + m);
		 MetaData md = m.getAnnotation(MetaData.class);
		 if (md == null)
			continue;
		 String name = md.value();
		 boolean isSetter = m.getParameterTypes().length==1;
		 boolean isStaticGet = ((m.getModifiers() & Modifier.STATIC) == Modifier.STATIC) && (m.getParameterTypes().length==0);
		 if (name == null || name.length() == 0) {
			name = m.getName();
			if ((name.startsWith("set") || name.startsWith("get")) && name.length() > 3) {
			   name = "" + Character.toLowerCase(name.charAt(3)) + name.substring(4);
			}
		 }
		 if( isSetter ) {
			 metaDataItems.put(m, name);
		 }
		 if (md.out().length() > 0) {
			log.info("Adding outjecting meta-data " + name + "=" + md.out());
			if (outjectMetaData == null)
			   outjectMetaData = new HashMap<String, String>();
			outjectMetaData.put(name, md.out());
		 } else if (isStaticGet) {
			String value;
			try {
			   value = m.invoke(null).toString();
			   log.info("Adding static get meta-data " + name + "=" + value);
			   if (outjectMetaData == null)
				  outjectMetaData = new HashMap<String, String>();
			   outjectMetaData.put(name, value);
			} catch (IllegalArgumentException e) {
			   e.printStackTrace();
			} catch (IllegalAccessException e) {
			   e.printStackTrace();
			} catch (InvocationTargetException e) {
			   e.printStackTrace();
			}
		 }
	  }
   }

   public synchronized static Injector getInjector(Class<?> clazz) {
	  if (classMap.containsKey(clazz))
		 return classMap.get(clazz);
	  Injector injector = new Injector(clazz);
	  classMap.put(clazz, injector);
	  return injector;
   }

   public void inject(MetaDataBean mdb, Object bean) {
	  if (bean == null)
		 return;
	  for (Map.Entry<Method, String> ent : metaDataItems.entrySet()) {
		 String key = ent.getValue();
		 Object value;
		 if( key.equals("_childName") ) value=mdb.getChildName();
		 if( key.equals("_path") ) value=mdb.getPath();
		 else value = mdb.getValue(key);
		 try {
			Method m = ent.getKey();
			Class<?>[] argTypes = m.getParameterTypes();
			if (argTypes.length != 1) {
			   throw new IllegalArgumentException("Wrong number of arguments for method:" + m);
			}
			Class<?> argType = argTypes[0];
			if (value == null) {
			   MetaData md = m.getAnnotation(MetaData.class);
			   if (md.required())
				  throw new NullPointerException("No value found for " + key + " on " + mdb.getPath() + " into "+clazz);
			   continue;
			}
			if (!argType.isInstance(value)) {
			   String strValue = value.toString();
			   if (argType == String.class) {
				  value = strValue;
			   } else if (argType == Integer.class || argType == Integer.TYPE) {
				  value = new Integer(strValue);
			   } else if (argType == Boolean.TYPE || argType== Boolean.class) {
			   	value = "true".equalsIgnoreCase(strValue);
			   } else {
				  throw new IllegalArgumentException("Invalid type for " + m + " value " + strValue);
			   }
			}
			ent.getKey().invoke(bean, new Object[] { value });
		 } catch (RuntimeException e) {
			throw e;
		 } catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		 }
	  }
   }

   /**
    * Returns the meta-data associated with this class declaration. Note that
    * path in this case is a relative path, not an absolute one, so in fact it
    * can be completely ignored and just return the child elements.
    */
   public Map<String, ?> getMetaData(String path, MetaDataBean existingMetaData) {
	  return outjectMetaData;
   }

}
