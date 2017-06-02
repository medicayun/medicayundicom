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
package org.dcm4chex.xds;

import org.apache.log4j.Logger;
import org.dcm4chex.xds.mbean.XDSService;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * @author franz.willer@gwi-ag.com
 * @version $Revision: 5473 $ $Date: 2007-11-21 09:37:11 +0100 (Mi, 21 Nov 2007) $
 * @since Mar 10, 2006
 */
public class XDSDocumentMetadata {
    public static final String NS_URN_RIM_2_1 = "urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.1";
    public static final String NS_URN_REGISTRY_2_1 = "urn:oasis:names:tc:ebxml-regrep:registry:xsd:2.1";

    private final Element metadata;
    private Node firstSlot;//used to insert slot for hash, UUID and URI on correct position

    private NamedNodeMap attributes;
    private String patientID;
    private Node uidNode;

    private final Document doc;

    private static Logger log = Logger.getLogger(XDSService.class.getName());

    public XDSDocumentMetadata( Element metadata) {
        this.metadata = metadata;
        doc = metadata.getOwnerDocument();
        init();
    }

    private void init() {
        attributes = metadata.getAttributes();
        firstSlot = null;
        NodeList nl = metadata.getChildNodes();
        Node child;
        String name, scheme;
        NamedNodeMap attrs;
        for ( int i = 0, len = nl.getLength() ; i < len ; i++ ){
            child = nl.item(i);
            if ( child.getNodeType() == Node.ELEMENT_NODE ) {
                if ( "ExternalIdentifier".equals(child.getLocalName())) {
                    attrs = child.getAttributes();
                    scheme = getAttributeValue(attrs,"identificationScheme");
                    if ( UUID.XDSDocumentEntry_uniqueId.equals(scheme) ) {
                        uidNode = attrs.getNamedItem("value");
                    } else if ( UUID.XDSDocumentEntry_patientId.equals(scheme) ) {
                        patientID = getAttributeValue(attrs,"value");
                    }
                } else if (firstSlot == null && "Slot".equals(child.getLocalName())) {
                    firstSlot = child;
                }
            }
        }
        removeSlot("hash");
        removeSlot("size");
        removeSlot("URI");
    }

    public String getContentID() {
        return getAttributeValue(attributes,"id");
    }
    public String getMimeType() {
        return getAttributeValue(attributes,"mimeType");
    }
    public String getObjectType() {
        return getAttributeValue(attributes,"objectType");
    }

    public String getUniqueID() {
        return uidNode.getNodeValue();
    }
    /**
     * @param uid
     */
    public void setUniqueID(String uid) {
        uidNode.setNodeValue(uid);

    }

    public String getPatientID() {
        return patientID;
    }

    /**
     * @param hash
     */
    public void setURI(String[] uri, String prefix) {
        setSlot( "URI", uri, prefix);
    }
    /**
     * @param hash
     */
    public void setHash(String hash, String prefix) {
        setSlot("hash", hash, prefix);
    }

    /**
     * @param fileSize
     */
    public void setSize(long fileSize, String prefix) {
        setSlot("size", String.valueOf(fileSize), prefix);
    }

    private void setSlot(String name, String value, String prefix) {
        setSlot(name, new String[]{value}, prefix);
    }
    private void setSlot(String name, String[] values, String prefix) {
        Element slot = doc.createElementNS(NS_URN_RIM_2_1,"Slot");
        if ( prefix != null ) slot.setPrefix(prefix);
        Element valueList = doc.createElementNS(NS_URN_RIM_2_1,"ValueList");
        if ( prefix != null ) valueList.setPrefix(prefix);
        for ( int i = 0 ; i < values.length ; i++ ) {
            Element valueElement = doc.createElementNS(NS_URN_RIM_2_1,"Value");
            if ( prefix != null ) valueElement.setPrefix(prefix);
            Text valueElementText = doc.createTextNode(values[i]);
            valueList.appendChild(valueElement);
            valueElement.appendChild(valueElementText);
        }
        slot.setAttribute("name", name);
        slot.appendChild(valueList);
        metadata.insertBefore(slot, firstSlot);
    }
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("XDSDocumentMetadata: id:").append(getContentID()).append(" patientID:").append(getPatientID()).append(" uuid:").append(getUniqueID());
        sb.append(" MimeType:").append(getMimeType());
        return sb.toString();
    }

    /**
     * @param attributes2
     * @param string
     * @return
     */
    private String getAttributeValue(NamedNodeMap attributes, String name) {
        Node item = attributes.getNamedItem(name);
        return item == null ? null: item.getNodeValue();
    }



    /**
     * @return Returns the metadata.
     */
    public Node getMetadata() {
        return metadata;
    }

    /**
     * @param string
     * @param string2
     * @param object
     */
    public void removeSlot(String name) {
        NodeList nl = metadata.getChildNodes();
        Node node;
        Attr attr;
        for ( int i=0,l=nl.getLength(); i<l ; i++) {
            node = (Node)nl.item(i);
            if ( node.getNodeType() == Node.ELEMENT_NODE ) {
                if ( "Slot".equals(node.getLocalName() ) ) {
                    attr = ((Element) node).getAttributeNode("name");
                    if ( name.equals(attr.getNodeValue()) ) {
                        metadata.removeChild(node);
                        log.warn("Slot "+name+" removed from XDSDocumentEntry!" );
                    }
                }
            }
        }
    }

}
