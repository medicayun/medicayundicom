
package org.dcm4chee.xds.infoset.v21;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
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
 *       &lt;sequence>
 *         &lt;element ref="{urn:oasis:names:tc:ebxml-regrep:registry:xsd:2.1}RegistryError" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *       &lt;attribute name="highestSeverity" type="{urn:oasis:names:tc:ebxml-regrep:registry:xsd:2.1}ErrorType" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "registryError"
})
@XmlRootElement(name = "RegistryErrorList", namespace = "urn:oasis:names:tc:ebxml-regrep:registry:xsd:2.1")
public class RegistryErrorList {

    @XmlElement(name = "RegistryError", namespace = "urn:oasis:names:tc:ebxml-regrep:registry:xsd:2.1", required = true)
    protected List<RegistryError> registryError;
    @XmlAttribute
    protected ErrorType highestSeverity;

    /**
     * Gets the value of the registryError property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the registryError property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRegistryError().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link RegistryError }
     * 
     * 
     */
    public List<RegistryError> getRegistryError() {
        if (registryError == null) {
            registryError = new ArrayList<RegistryError>();
        }
        return this.registryError;
    }

    /**
     * Gets the value of the highestSeverity property.
     * 
     * @return
     *     possible object is
     *     {@link ErrorType }
     *     
     */
    public ErrorType getHighestSeverity() {
        return highestSeverity;
    }

    /**
     * Sets the value of the highestSeverity property.
     * 
     * @param value
     *     allowed object is
     *     {@link ErrorType }
     *     
     */
    public void setHighestSeverity(ErrorType value) {
        this.highestSeverity = value;
    }

}
