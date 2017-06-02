
package org.dcm4chee.xds.infoset.v21;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;choice minOccurs="0">
 *           &lt;element ref="{urn:oasis:names:tc:ebxml-regrep:query:xsd:2.1}AdhocQueryResponse"/>
 *         &lt;/choice>
 *         &lt;element ref="{urn:oasis:names:tc:ebxml-regrep:registry:xsd:2.1}RegistryErrorList" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="status" use="required">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *             &lt;enumeration value="Success"/>
 *             &lt;enumeration value="Failure"/>
 *             &lt;enumeration value="Unavailable"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "adhocQueryResponse",
    "registryErrorList"
})
@XmlRootElement(name = "RegistryResponse", namespace = "urn:oasis:names:tc:ebxml-regrep:registry:xsd:2.1")
public class RegistryResponse {

    @XmlElement(name = "AdhocQueryResponse", namespace = "urn:oasis:names:tc:ebxml-regrep:query:xsd:2.1")
    protected AdhocQueryResponse adhocQueryResponse;
    @XmlElement(name = "RegistryErrorList", namespace = "urn:oasis:names:tc:ebxml-regrep:registry:xsd:2.1")
    protected RegistryErrorList registryErrorList;
    @XmlAttribute(required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String status;

    /**
     * Gets the value of the adhocQueryResponse property.
     * 
     * @return
     *     possible object is
     *     {@link AdhocQueryResponse }
     *     
     */
    public AdhocQueryResponse getAdhocQueryResponse() {
        return adhocQueryResponse;
    }

    /**
     * Sets the value of the adhocQueryResponse property.
     * 
     * @param value
     *     allowed object is
     *     {@link AdhocQueryResponse }
     *     
     */
    public void setAdhocQueryResponse(AdhocQueryResponse value) {
        this.adhocQueryResponse = value;
    }

    /**
     * Gets the value of the registryErrorList property.
     * 
     * @return
     *     possible object is
     *     {@link RegistryErrorList }
     *     
     */
    public RegistryErrorList getRegistryErrorList() {
        return registryErrorList;
    }

    /**
     * Sets the value of the registryErrorList property.
     * 
     * @param value
     *     allowed object is
     *     {@link RegistryErrorList }
     *     
     */
    public void setRegistryErrorList(RegistryErrorList value) {
        this.registryErrorList = value;
    }

    /**
     * Gets the value of the status property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the value of the status property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStatus(String value) {
        this.status = value;
    }

}
