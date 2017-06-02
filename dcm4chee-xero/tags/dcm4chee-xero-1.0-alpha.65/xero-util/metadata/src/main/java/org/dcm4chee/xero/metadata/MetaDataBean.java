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

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dcm4chee.xero.metadata.access.ValueList;

/**
 * This is a metadata information object that provides meta-data at a single
 * level. For example, it might provide StudySearch meta-data. In turn study
 * search might have StudySearch.Commands that has meta-data of it's own.
 * It is read-only once constructed, and thus is thread safe.
 * 
 * @author bwallace
 */
public class MetaDataBean extends AbstractMap<String, Object> {
   static Logger log = LoggerFactory.getLogger(MetaDataBean.class);
   
   /**
     * This object had better actually not be serialized however, it isn't clear
     * how to avoid that right now.
     */
   private static final long serialVersionUID = 4349072566971592532L;

   public static final String DEFAULT_SORT_KEY = "priority";

   private String childName;

   protected MetaDataBean() {
	  // Only here to make the default constructor protected.
   }

   protected MetaDataBean createChild() {
	  return new MetaDataBean();
   }

   /**
     * Create a meta-data object on the given path, with the given initial
     * meta-data providers.
     * 
     * @param path
     * @param theme
     */
   public MetaDataBean createChild(String pathIn) {
	  MetaDataBean newChild = createChild();
	  newChild.metaDataProviders = metaDataProviders;
	  newChild.valueProviders = valueProviders;
	  newChild.path = pathIn;
	  newChild.childName = pathIn.substring(pathIn.lastIndexOf('.') + 1);
	  return newChild;
   }

   /**
     * Create a meta-data provider on just the two default providers, a property
     * file, as evidenced by a Map<String,String>, and a MetaData annotation
     * reader.
     */
   public MetaDataBean(Map<String, ?> properties) {
	  this.path = "";
	  this.childName = "";
	  configureProviders(properties);
	  initAll();
   }
   
   /**
    * Initializes this and all child elements
    * TODO - make this safe even for self-nesting inherits (this is otherwise recursive indefinitely)
    */
   protected void initAll() {
	  log.debug("Initializing "+this.path);
	  this.initChildren();
	  if( children==null ) return;
	  for(String child : children.keySet()) {
		 MetaDataBean mdbChild = getChild(child);
		 mdbChild.initAll();
	  }
   }

   /**
     * Sets up the meta-data and value providers. It does this iteratively,
     * adding providers as long as a new provider has defined a new instance
     * (which causes the length of the providers list to change.)
     */
   protected void configureProviders(Map<String, ?> properties) {
	  PropertyProvider propProvider = new PropertyProvider(properties);
	  valueProviders = new ValueList<ValueProvider>(ValueProvider.class);
	  valueProviders.add(new NullValueProvider());
	  valueProviders.add(new InstanceValueProvider());
	  valueProviders.add(new FactoryValueProvider());
	  valueProviders.add(new ReferenceValueProvider());
	  List<ValueProvider> baseList = new ArrayList<ValueProvider>(valueProviders);

	  metaDataProviders = new ValueList<MetaDataProvider>(MetaDataProvider.class);
	  metaDataProviders.add(propProvider);
	  metaDataProviders.add(InheritProvider.getInheritProvider());

	  while (true) {
		 MetaDataBean metaProviderMeta = getChild("metaDataProvider");
		 MetaDataBean valueProviderMeta = getChild("valueProvider");

		 if (valueProviderMeta != null) {
			log.debug("Updating value provider.");
			// this could end up updating with the same values several times,
			// but it is hard to
			// know whether that will be the case or not, and meta-data should
			// be static.
			ValueList<ValueProvider> updatedValueProviders = new ValueList<ValueProvider>(ValueProvider.class, baseList);
			updatedValueProviders.setMetaData(valueProviderMeta);
			valueProviders.clear();
			valueProviders.addAll(updatedValueProviders);
		 }

		 // There are no children, so just return;
		 if (metaProviderMeta == null) {
			log.debug("No meta data providers configured.");
			return;
		 }

		 log.debug("Trying to add meta-data providers.");
		 ValueList<MetaDataProvider> newProviders = new ValueList<MetaDataProvider>(MetaDataProvider.class);
		 newProviders.add(propProvider);
		 newProviders.setMetaData(metaProviderMeta);
		 newProviders.add(InheritProvider.getInheritProvider());
		 boolean finalMetaDataProvider = (newProviders.size() <= metaDataProviders.size());
		 log.debug("Found " + (newProviders.size() - metaDataProviders.size()) + " new meta-data providers.");
		 // Use the most recent providers so that they reference the right
		 // meta-data.
		 metaDataProviders.clear();
		 metaDataProviders.addAll(newProviders);
		 if (finalMetaDataProvider) {
			return;
		 }
		 invalidate();
	  }
   }

   /** Invalidates this meta data and all children */
   protected void invalidate() {
	  children.clear();
	  isInitialized = false;
   }

   ValueList<MetaDataProvider> metaDataProviders;

   ValueList<ValueProvider> valueProviders;

   transient Map<String, MetaDataBean> children;

   /** The value of this meta data bean. */
   Object instanceValue;

   /** The original, raw value of this object */
   Object rawValue;

   /** The converter to use for this object, if any. */
   ValueProvider instanceValueProvider;

   /**
     * The instance config is static configuration based on this meta-data,
     * defaulting to this meta data.
     */
   Object instanceConfig;

   boolean isInitialized = false;

   MetaDataBean parent, root;

   /** The path is the parent elements that lead up to this element. */
   String path = null;

   protected MetaDataProvider additionalMetaDataProvider;

   /**
     * Initializes the children with a complete list of all the children of the
     * current path For example, if the path is ExampleSearch.fred, and there is
     * a ExampleSearch.fred.bean, then it will put bean into children. There
     * maybe other types of initialization that also occurs allow meta-data to
     * be defined elsewhere than a theme - this would ONLY apply to the default
     * meta-data.
     */
   protected void initChildren() {
	  if (isInitialized)
		 return;
	  isInitialized = true;
	  for (MetaDataProvider mdp : metaDataProviders) {
		 // The overall meta-data providers use absolute paths.
		 addMetaDataFromProvider(mdp, getPath());
	  }
	  MetaDataBean ancestor = this;
	  String relativePath = "";
	  while (ancestor != null) {
		 if (ancestor.additionalMetaDataProvider != null) {
			addMetaDataFromProvider(ancestor.additionalMetaDataProvider, relativePath);
		 }
		 ancestor = ancestor.getParent();
		 if (ancestor != null) {
			int len = ancestor.getPath().length();
			if (len == 0)
			   len = -1; // Virtual dot.
			relativePath = getPath().substring(len + 1);
		 }
	  }
	  initValueProvider();
   }

   /** Adds the meta-data provided by mdp into this meta-data instance. */
   protected void addMetaDataFromProvider(MetaDataProvider mdp, String relativePath) {
	  int pathLen = relativePath.length();
	  // There is a virtual . before the start that has already been excluded,
	  // so empty pathLen is -1
	  if (pathLen == 0)
		 pathLen = -1;
	  Map<String, ?> addedMetaData = mdp.getMetaData(relativePath, this);
	  if (addedMetaData == null)
		 return;
	  for (Map.Entry<String, ?> me : addedMetaData.entrySet()) {
		 String key = me.getKey();
		 if (!key.startsWith(relativePath))
			continue;
		 if (key.length() == pathLen) {
			// This is a simple value, put it into the map
			Object value = me.getValue();
			if (value instanceof String) {
			   value = ((String) value).trim();
			}
			log.debug("Found a matching value for " + key + " on path " + relativePath + " value " + value);
			if (this.rawValue != null) {
			   log.debug("Second instance of key:" + key);
			   continue;
			}
			rawValue = value;
			continue;
		 } else if (pathLen < 0 || key.charAt(pathLen) == '.') {
			int nextDot = key.indexOf('.', pathLen + 1);
			if (nextDot < 0)
			   nextDot = key.length();
			String newChildName = key.substring(pathLen + 1, nextDot);
			if (newChildName.equals("") ) {
			   log.warn("Child meta-data name is empty:" + key);
			   continue;
			}
			// Only create the children once we know there is something
			// to it.
			if (children == null)
			   children = new HashMap<String, MetaDataBean>();
			MetaDataBean child = children.get(newChildName);
			if (child != null) {
			   continue;
			}
			log.debug("From mdb path " + getPath() + " creating child element " + newChildName);
			String childPath = path + (path.length() > 0 ? "." : "") + newChildName;
			child = createChild(childPath);
			child.setParent(this);
			children.put(newChildName, child);
		 }
	  }
   }

   /**
     * This method initializes the value provider, after the valueInstance has
     * been set. It does this by reading through the value providers and seeing
     * if any of them provide a value for this instance.
     */
   protected void initValueProvider() {
	  if (instanceValue != null)
		 return;
	  if (rawValue == null)
		 return;
	  // This might get called recursively in some circumstances.
	  if (instanceValueProvider != null) {
		 instanceValue = rawValue;
		 return;
	  }
	  for (ValueProvider vp : valueProviders) {
		 instanceValue = vp.preConvertValue(this, rawValue);
		 if (instanceValue != null) {
			instanceValueProvider = vp;
			additionalMetaDataProvider = vp.getExtraMetaDataProvider(this, instanceValue, rawValue);
			if (additionalMetaDataProvider != null) {
			   // The relative path to this object is the root instance itself.
			   // This will only add children, it won't directly add values.
			   log.debug("Adding children meta-data providers to " + getPath());
			   addMetaDataFromProvider(additionalMetaDataProvider, "");
			}
			// We might need to inject this after the extra meta-data is
			// read in case it reads meta-data itself.
			inject(instanceValue);
			return;
		 }
	  }
	  instanceValue = rawValue;
	  // Provide a warning for $... when it isn't found.
	  if (instanceValue != null && (instanceValue instanceof String)) {
		 String sInstanceValue = (String) instanceValue;
		 if (sInstanceValue.trim().startsWith("$")) {
			log.warn("Meta-data starts with $ but no value provider converted this to a value:" + sInstanceValue + " at node "
				  + getPath());
		 }
	  }
   }

   /**
     * Returns any configuration information based on the local position of the
     * given object.
     * 
     * @return
     */
   public Object getValueConfig() {
	  if (instanceConfig != null)
		 return instanceConfig;
	  Object val = getValue();
	  if (instanceValueProvider instanceof PreConfigMetaData) {
		 instanceConfig = ((PreConfigMetaData<?>) instanceValueProvider).getConfigMetaData(this);
		 if (instanceConfig != null)
			return instanceConfig;
	  }
	  if (val instanceof PreConfigMetaData) {
		 instanceConfig = ((PreConfigMetaData<?>) val).getConfigMetaData(this);
		 if (instanceConfig != null)
			return instanceConfig;
	  }
	  // Eventually looking up the instance config provider as meta-data
	  // itself might
	  // be worthwhile.
	  instanceConfig = this;
	  return instanceConfig;
   }

   /**
     * Returns the "value" of this meta-data bean. This will be the directly
     * assigned value, eg for meta-data mdb with path mdb. and mdb=SomeString
     * mdb.X=othervalue then the value of mdb would be SomeString.
     * 
     * @return
     */
   public Object getValue() {
	  initChildren();
	  if (instanceValue == null)
		 return null;
	  if (instanceValueProvider != null) {
		 return instanceValueProvider.convertValue(this, instanceValue);
	  }
	  return instanceValue;
   }

   /**
     * Can be over-ridden to handle parsing of extended names, for example, for
     * JNDI lookup, or for Seam EL parsing.
     * 
     * @param result
     * @return
     */
   protected Object parseEL(String result) {
	  return result;
   }

   /**
     * Returns the value for a child meta-data object - just a convenience to
     * avoid extra null checks.
     */
   public Object getValue(String childMeta) {
	  MetaDataBean mdb = getForPath(childMeta);
	  if (mdb == null)
		 return null;
	  return mdb.getValue();
   }

   /** Sets the parent of the meta-data bean to the given value. */
   protected void setParent(MetaDataBean parent) {
	  this.parent = parent;
	  this.root = parent.getRoot();
   }

   /** Gets the parent of this object */
   public MetaDataBean getParent() {
	  return parent;
   }

   /** Gets a path containing value */
   public MetaDataBean getForPath(String subpath) {
	  if (subpath == null || subpath.length() == 0)
		 return this;
	  int nextDot = subpath.indexOf('.');
	  if (nextDot < 0)
		 return getChild(subpath);
	  MetaDataBean child = getChild(subpath.substring(0, nextDot));
	  if (child == null)
		 return null;
	  return child.getForPath(subpath.substring(nextDot + 1));
   }

   /** Get the root of the meta-data bean hierarchy. */
   protected MetaDataBean getRoot() {
	  if (this.root != null)
		 return root;
	  return this;
   }

   /**
     * Returns the internal children of this object, initializing it as
     * necessary.
     * 
     * @return
     */
   public Map<String, MetaDataBean> getChildren() {
	  initChildren();
	  return children;
   }

   /**
     * Return the meta-data associated with this object. If the meta-data is of
     * the form ${...} then lookup the object instead of returning the string.
     */
   public MetaDataBean getChild(String property) {
	  initChildren();
	  if (children == null)
		 return null;
	  return children.get(property);
   }
   
   /**
    * Returns the named element - special element "value" gets the value of this element, otherwise
    * return the child meta data bean value.
    */
   @Override
   public Object get(Object property) {
	   if( "value".equals(property) ) return getValue();
	   return getChild((String) property);
   }
   
   /** Find out if the given element is present */
   @Override
   public boolean containsKey(Object property) {
	   if( "value".equals(property) ) return true;
	   return getChildren().containsKey(property);
   }

   /**
     * Allow iteration over the meta-data elements. This only iterates over
     * direct children, not nested children, and does not include the value/keys or other customized entries.
     */
   @SuppressWarnings("unchecked")
   @Override
   public Set<Map.Entry<String, Object>> entrySet() {
	  // Hide the actual return types, as the return value is read only and MetaDataBean is a child of Object so it
	  // is safe to do.
	  Set ret = metaDataEntrySet();
	  return ret;
   }
   
   /** Return the entry set containing the children meta-data entries. */
   @SuppressWarnings("unchecked")
   public Set<Map.Entry<String,MetaDataBean>> metaDataEntrySet() {
		  initChildren();
		  if (children == null)
			 return Collections.EMPTY_MAP.entrySet();
		  return children.entrySet();
   }

   /**
     * Inject the components into the bean. If there are any.  Note that the
     * pre-configured values are auto-injected at the right time, but any
     * values the final conversion (eg factory: values and others) still need
     * injection.
     * 
     * @MetaData flagged items with required=true (that is the default), then
     *           throw an exception if the value isn't found. In any case, throw
     *           an exception if the value can't be converted correctly.
     */
   public void inject(Object obj) {
	  // There are a few reasons why we can get null and it is valid, so just ignore.
	  if( obj==null ) return;
	  Injector.getInjector(obj.getClass()).inject(this, obj);
	  if (obj instanceof MetaDataUser)
		   ((MetaDataUser) obj).setMetaData(this);
   }

   /**
     * Converts this object to a string
     */
   public String toString() {
	  Object ret = getValue();
	  if (ret == null)
		 return null;
	  return ret.toString();
   }

   /** This returns the full path of this object */
   public String getPath() {
	  return path;
   }

   /** This returns the name of this object within the immediate parent. */
   public String getChildName() {
	  return childName;
   }

   /**
     * This gets the list of children, sorted on the given parameter, having the
     * given child, with the provided default value.
     */
   public List<MetaDataBean> sorted(String havingKey, String sortKey, int defaultValue) {
	  initChildren();
	  List<MetaDataBean> ret = new ArrayList<MetaDataBean>();
	  for (Map.Entry<String, MetaDataBean> me : metaDataEntrySet()) {
		 if (havingKey != null && me.getValue().get(havingKey) == null)
			continue;
		 ret.add(me.getValue());
	  }
	  if (sortKey == null)
		 return ret;
	  Collections.sort(ret, new MetaDataComparator(sortKey, defaultValue));
	  return ret;
   }

   /** Just return the sorted child elements */
   public List<MetaDataBean> sorted() {
	  return sorted(null, DEFAULT_SORT_KEY, 0);
   }

   /**
     * This gets the list of children, sorted on the given parameter, having the
     * given child
     */
   public List<MetaDataBean> sorted(String havingKey) {
	  return sorted(havingKey, havingKey + "." + DEFAULT_SORT_KEY, 0);
   }

   /** Get an integer value - returns defaultValue if not found.  May throw
    * an exception if the value isn't convertable to an integer.
    * @param key
    * @param defaultValue
    * @return the value for key.
    */
	public int getIntValue(String key, int defaultValue) {
		Object v=getValue(key);
		if( v==null ) return defaultValue;
		if( v instanceof String ) return Integer.parseInt((String) v);
		if( v instanceof Number ) return ((Number) v).intValue();
		return defaultValue;
   }
	/** Gets a string value - uses toString if the base value isn't already a string */
	public String getStringValue(String key) {
		Object v=getValue(key);
		if( v==null ) return null;
		return v.toString();
	}
	
}
