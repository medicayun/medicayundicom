package org.dcm4chex.archive.web.maverick.xdsi;

import javax.xml.registry.JAXRException;

public class XDSAssociation implements XDSRegistryObject {

    private XDSRegistryObject source;
    private XDSRegistryObject target;
    private String type;
    private int status;

    public XDSAssociation(XDSRegistryObject source, XDSRegistryObject target, String type, int status) {
        this.source = source;
        this.target = target;
        this.type = type;
        this.status = status;
    }
    public String getId() throws JAXRException {
        return null;
    }

    public String getName() throws JAXRException {
        return null;
    }

    /**
     * Get status of Association as int.
     * 
     * @return
     * @throws JAXRException
     */
    public int getStatus() throws JAXRException {
        return status;
    }
    /**
     * Get status of Association as String.
     * 
     * @return
     * @throws JAXRException
     */
    public String getStatusAsString() throws JAXRException {
        return XDSStatus.getStatusAsString(status);
    }

    public XDSRegistryObject getSource() {
        return source;
    }

    public XDSRegistryObject getTarget() {
        return target;
    }

    public String getType() {
        return type;
    }
}
