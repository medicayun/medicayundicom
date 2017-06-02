package org.dcm4chee.docstore;

import org.dcm4chee.docstore.spi.DocumentStorage;

public interface DocumentStorageListener {

    void documentStored(BaseDocument doc);
    void documentCreated(BaseDocument doc);
    void documentDeleted(BaseDocument doc);
    void documentRetrieved(BaseDocument doc);
    void documentCommitted(BaseDocument doc);
    void storageAvailabilityChanged(DocumentStorage docStore, Availability oldAvail, Availability newAvail);
}
