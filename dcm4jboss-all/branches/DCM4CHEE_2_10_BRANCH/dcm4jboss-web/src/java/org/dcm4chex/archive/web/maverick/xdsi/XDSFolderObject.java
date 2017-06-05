package org.dcm4chex.archive.web.maverick.xdsi;

import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.RegistryPackage;

public class XDSFolderObject implements XDSRegistryObject {
    private RegistryPackage rp;

    public XDSFolderObject( RegistryPackage rp ) {
        this.rp = rp;
    }

    public String getId() throws JAXRException {
        return rp.getKey().getId();
    }
    public String getName() throws JAXRException {
        return rp.getName().getValue();
    }
    public int getStatus() throws JAXRException {
        return rp.getStatus();
    }
    /**
     * Get status of document as String.
     * 
     * @return
     * @throws JAXRException
     */
    public String getStatusAsString() throws JAXRException {
        return XDSStatus.getStatusAsString(rp.getStatus());
    }
}
