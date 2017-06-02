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
 * Java(TM), available at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * TIANI Medgraph AG.
 * Portions created by the Initial Developer are Copyright (C) 2003-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunter.zeilinger@tiani.com>
 * Franz Willer <franz.willer@gwi-ag.com>
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
package org.dcm4chee.xds.docstore.spi;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import sun.misc.Service;

public class XDSDocumentStorageRegistry {
	
	private Map<String, XDSDocumentStorageProviderSPI> docStorages = new HashMap<String, XDSDocumentStorageProviderSPI>();
	
	public XDSDocumentStorageRegistry() {
		init();
	}
	
	public void init() {
		Iterator iter = Service.providers(XDSDocumentStorageProviderSPI.class);
		XDSDocumentStorageProviderSPI sp;
		while (iter.hasNext()) {
			sp = (XDSDocumentStorageProviderSPI)iter.next();
			docStorages.put(sp.getName(), sp);
		}
	}
	
	public XDSDocumentStorage getXDSDocumentStorage(String name) {
		XDSDocumentStorageProviderSPI sp = docStorages.get(name);
		return sp==null ? null : sp.getDocumentStorage();
	}
	
	public Set<String> getAllXDSDocumentStorageProviderNames() {
		return docStorages.keySet();
	}
}
