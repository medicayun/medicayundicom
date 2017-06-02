
package org.dcm4chee.xds.infoset.v21;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for LeafRegistryObjectListType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="LeafRegistryObjectListType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded" minOccurs="0">
 *         &lt;element ref="{urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.1}ObjectRef"/>
 *         &lt;element ref="{urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.1}Association"/>
 *         &lt;element ref="{urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.1}Classification"/>
 *         &lt;element ref="{urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.1}ExternalIdentifier"/>
 *         &lt;element ref="{urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.1}ExtrinsicObject"/>
 *         &lt;element ref="{urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.1}RegistryPackage"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LeafRegistryObjectListType", propOrder = {
    "objectRefOrAssociationOrClassification"
})
@XmlSeeAlso({
    RegistryObjectListType.class
})
public class LeafRegistryObjectListType {

    @XmlElements({
        @XmlElement(name = "Association", type = AssociationType1 .class),
        @XmlElement(name = "ExternalIdentifier", type = ExternalIdentifierType.class),
        @XmlElement(name = "RegistryPackage", type = RegistryPackageType.class),
        @XmlElement(name = "ExtrinsicObject", type = ExtrinsicObjectType.class),
        @XmlElement(name = "Classification", type = ClassificationType.class),
        @XmlElement(name = "ObjectRef", type = ObjectRefType.class)
    })
    protected List<Object> objectRefOrAssociationOrClassification;

    /**
     * Gets the value of the objectRefOrAssociationOrClassification property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the objectRefOrAssociationOrClassification property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getObjectRefOrAssociationOrClassification().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AssociationType1 }
     * {@link ExternalIdentifierType }
     * {@link RegistryPackageType }
     * {@link ExtrinsicObjectType }
     * {@link ClassificationType }
     * {@link ObjectRefType }
     * 
     * 
     */
    public List<Object> getObjectRefOrAssociationOrClassification() {
        if (objectRefOrAssociationOrClassification == null) {
            objectRefOrAssociationOrClassification = new ArrayList<Object>();
        }
        return this.objectRefOrAssociationOrClassification;
    }

}
