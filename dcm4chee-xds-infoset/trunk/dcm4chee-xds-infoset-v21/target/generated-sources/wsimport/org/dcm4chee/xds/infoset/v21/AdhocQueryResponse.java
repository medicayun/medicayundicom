
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
 *         &lt;element ref="{urn:oasis:names:tc:ebxml-regrep:query:xsd:2.1}SQLQueryResult"/>
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
    "sqlQueryResult"
})
@XmlRootElement(name = "AdhocQueryResponse", namespace = "urn:oasis:names:tc:ebxml-regrep:query:xsd:2.1")
public class AdhocQueryResponse {

    @XmlElement(name = "SQLQueryResult", namespace = "urn:oasis:names:tc:ebxml-regrep:query:xsd:2.1")
    protected RegistryObjectListType sqlQueryResult;

    /**
     * Gets the value of the sqlQueryResult property.
     * 
     * @return
     *     possible object is
     *     {@link RegistryObjectListType }
     *     
     */
    public RegistryObjectListType getSQLQueryResult() {
        return sqlQueryResult;
    }

    /**
     * Sets the value of the sqlQueryResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link RegistryObjectListType }
     *     
     */
    public void setSQLQueryResult(RegistryObjectListType value) {
        this.sqlQueryResult = value;
    }

}
