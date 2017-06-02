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
package org.dcm4chee.xero.metadata.access;

import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.dcm4chee.xero.metadata.MetaData;
import org.dcm4chee.xero.metadata.MetaDataBean;
import org.dcm4chee.xero.metadata.MetaDataUser;

/**
 * A MapWithDefaults is a map of values, where it can ask some pre-configured meta-data about what
 * defaults it should use coming from the meta-data bean.
 * This is closely related to a lazy map that knows how to create objects from a factory if needed.
 * 
 * @author bwallace
 *
 */
@SuppressWarnings("serial")
public class MapWithDefaults extends LazyMap implements MetaDataUser {
   MetaDataBean mdb;
   boolean wasEager = false;
   
   public MapWithDefaults() {   
   }
   
   public MapWithDefaults(MetaDataBean mdb) {
	  super();
	  this.mdb = mdb;
   }
   
   public MapWithDefaults(MetaDataBean mdb, Map<String,Object> lazy) {
	  super(lazy);
	  this.mdb = mdb;
   }

   /** Get the lazy object from the meta-data object associated with this map. */
   public Object getLazy(Object key) {
     MetaDataBean child = mdb.getChild((String) key);
     if( child==null ) return super.getLazy(key);
	  Object v = child.getValue();
	  if( v!=null ) return v;
	  return child;
   }
   
   /** Does an eager load of all values - this can be useful for configuration where runtime safety is important. */
   public void eager() {
	   if( wasEager ) return;
	   this.addAllLazy();
	   wasEager = true;
   }

   /** Adds all the lazy information to the map */
   public boolean addAllLazy() {
	   if( super.addAllLazy() ) return true;
	   for(Map.Entry<String,MetaDataBean> me : mdb.metaDataEntrySet()) {
		   this.get(me.getKey());
	   }
	   return false;
   }


   /** Ensure that all values are in the entry set if it is used. */
   @Override
   public Set<Entry<String, Object>> entrySet() {
   	if(!wasEager) eager();
	   return super.entrySet();
   }

	/** Sets the meta data to use. */
   public void setMetaData(MetaDataBean metaDataBean) {
	   this.mdb = metaDataBean;
   }
   
   /** Sets the lazy map */
   @MetaData(required=false)
   public void setLazy(Map<String,Object> lazy) {
   	this.lazy = lazy;
   }
}
