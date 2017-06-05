package org.dcm4chex.archive.web.maverick.xdsi;

import javax.xml.registry.JAXRException;

public interface XDSRegistryObject {
    /**
     * Gets the universally unique ID (UUID) for this object.
     * @return
     * @throws JAXRException 
     */
    String getId() throws JAXRException;
    
    /**
     * Gets the user-friendly name of this object.
     * @return
     * @throws JAXRException 
     */
    String getName() throws JAXRException;
    
    /**
     * Gets the status of this object.
     * @return
     * @throws JAXRException 
     */
    int getStatus() throws JAXRException;
}
