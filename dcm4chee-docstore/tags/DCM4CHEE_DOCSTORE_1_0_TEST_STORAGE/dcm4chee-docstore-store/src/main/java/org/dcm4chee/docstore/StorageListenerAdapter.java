package org.dcm4chee.docstore;

import org.dcm4chee.docstore.spi.DocumentStorage;

public class StorageListenerAdapter implements DocumentStorageListener {

    public void documentCommitted(BaseDocument doc) {
    }

    public void documentCreated(BaseDocument doc) {
    }

    public void documentDeleted(BaseDocument doc) {
    }

    public void documentRetrieved(BaseDocument doc) {
    }

    public void documentStored(BaseDocument doc) {
    }

    public void storageAvailabilityChanged(DocumentStorage docStore,
            Availability oldAvail, Availability newAvail) {
    }

}
