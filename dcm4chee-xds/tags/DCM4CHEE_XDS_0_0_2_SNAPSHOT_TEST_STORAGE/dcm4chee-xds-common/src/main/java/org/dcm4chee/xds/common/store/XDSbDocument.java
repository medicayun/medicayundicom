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

package org.dcm4chee.xds.common.store;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.dcm4chee.xds.common.UUID;
import org.dcm4chee.xds.common.infoset.ExternalIdentifierType;
import org.dcm4chee.xds.common.infoset.ExtrinsicObjectType;
import org.dcm4chee.xds.common.infoset.SlotType1;
import org.dcm4chee.xds.common.infoset.ValueListType;
import org.dcm4chee.xds.common.utils.InfoSetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class XDSbDocument extends XDSDocument {

	private ExtrinsicObjectType extrinsicObj;
	private Map slotByName;
	private Map extIdentifiers;
	
	private Logger log = LoggerFactory.getLogger(XDSbDocument.class);
			
	public XDSbDocument(ExtrinsicObjectType extrObj, XDSDocumentWriter xdsDocWriter) {
		super(xdsDocWriter);
		extrinsicObj = extrObj;
	}
	
	public String getDocumentUID() {
		return getExternalIdentifierValue(UUID.XDSDocumentEntry_uniqueId);
	}
	
	public String getMimeType() {
		return extrinsicObj.getMimeType();
	}
	public String getPatientID() {
		return getExternalIdentifierValue(UUID.XDSDocumentEntry_patientId);
	}
	public List<String> getSlotValues(String slotName) {
		try {
			SlotType1 slot = (SlotType1)getSlots().get(slotName);
			if (slot == null) return null;
			ValueListType vl = slot.getValueList();
			return vl == null ? null : vl.getValue();
		} catch (JAXBException x) {
			log.error("GetValue of Slot "+slotName+" failed! Return null",x);
			return null;
		}
	}
	public String getSlotValue(String slotName) {
		List<String> l = getSlotValues(slotName);
		return l.size() > 0 ? l.get(0) : null;
	}
	
	public String getExternalIdentifierValue(String urn) {
		try {
			ExternalIdentifierType ei = (ExternalIdentifierType) getExternalIdentifiers().get(urn);
			return ei == null ? null : ei.getValue();
		} catch (JAXBException x) {
			log.error("getExternalIdentifierValue for "+urn+" failed! Return null",x);
			return null;
		}
	}
	
	public Map getSlots() throws JAXBException {
		if (slotByName == null) {
			slotByName = InfoSetUtil.getSlotsFromRegistryObject(extrinsicObj);
		}
		return slotByName;
	}
	public Map getExternalIdentifiers() throws JAXBException {
		ExternalIdentifierType ei;
		if (extIdentifiers == null) {
			extIdentifiers = new HashMap();
			List l = extrinsicObj.getExternalIdentifier();
			for ( Iterator iter = l.iterator() ; iter.hasNext();){
				ei = (ExternalIdentifierType) iter.next();
				extIdentifiers.put(ei.getIdentificationScheme(), ei);
			}
		}
		return extIdentifiers;
	}
}
