
package org.dcm4chee.xds.infoset.v21;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice>
 *         &lt;element ref="{urn:oasis:names:tc:ebxml-regrep:registry:xsd:2.1}SubmitObjectsRequest"/>
 *         &lt;element ref="{urn:oasis:names:tc:ebxml-regrep:query:xsd:2.1}AdhocQueryRequest"/>
 *         &lt;element ref="{urn:oasis:names:tc:ebxml-regrep:registry:xsd:2.1}RegistryResponse"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "submitObjectsRequest",
    "adhocQueryRequest",
    "registryResponse"
})
@XmlRootElement(name = "RootElement", namespace = "urn:oasis:names:tc:ebxml-regrep:registry:xsd:2.1")
public class RootElement {

    @XmlElement(name = "SubmitObjectsRequest", namespace = "urn:oasis:names:tc:ebxml-regrep:registry:xsd:2.1")
    protected SubmitObjectsRequest submitObjectsRequest;
    @XmlElement(name = "AdhocQueryRequest", namespace = "urn:oasis:names:tc:ebxml-regrep:query:xsd:2.1")
    protected AdhocQueryRequest adhocQueryRequest;
    @XmlElement(name = "RegistryResponse", namespace = "urn:oasis:names:tc:ebxml-regrep:registry:xsd:2.1")
    protected RegistryResponse registryResponse;

    /**
     * Gets the value of the submitObjectsRequest property.
     * 
     * @return
     *     possible object is
     *     {@link SubmitObjectsRequest }
     *     
     */
    public SubmitObjectsRequest getSubmitObjectsRequest() {
        return submitObjectsRequest;
    }

    /**
     * Sets the value of the submitObjectsRequest property.
     * 
     * @param value
     *     allowed object is
     *     {@link SubmitObjectsRequest }
     *     
     */
    public void setSubmitObjectsRequest(SubmitObjectsRequest value) {
        this.submitObjectsRequest = value;
    }

    /**
     * Gets the value of the adhocQueryRequest property.
     * 
     * @return
     *     possible object is
     *     {@link AdhocQueryRequest }
     *     
     */
    public AdhocQueryRequest getAdhocQueryRequest() {
        return adhocQueryRequest;
    }

    /**
     * Sets the value of the adhocQueryRequest property.
     * 
     * @param value
     *     allowed object is
     *     {@link AdhocQueryRequest }
     *     
     */
    public void setAdhocQueryRequest(AdhocQueryRequest value) {
        this.adhocQueryRequest = value;
    }

    /**
     * Gets the value of the registryResponse property.
     * 
     * @return
     *     possible object is
     *     {@link RegistryResponse }
     *     
     */
    public RegistryResponse getRegistryResponse() {
        return registryResponse;
    }

    /**
     * Sets the value of the registryResponse property.
     * 
     * @param value
     *     allowed object is
     *     {@link RegistryResponse }
     *     
     */
    public void setRegistryResponse(RegistryResponse value) {
        this.registryResponse = value;
    }

}
