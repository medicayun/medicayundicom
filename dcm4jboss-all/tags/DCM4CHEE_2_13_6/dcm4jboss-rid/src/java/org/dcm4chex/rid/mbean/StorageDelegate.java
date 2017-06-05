package org.dcm4chex.rid.mbean;

import java.io.File;
import java.io.IOException;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;

import org.apache.log4j.Logger;
import org.dcm4chee.docstore.BaseDocument;
import org.dcm4chee.docstore.DocumentStore;

public class StorageDelegate {

	private static StorageDelegate singleton = new StorageDelegate();

	private static Logger log = Logger.getLogger( StorageDelegate.class.getName() );
	
	private DocumentStore store = DocumentStore.getInstance("RID");
	private StorageDelegate() {
		
	}
	
	public static StorageDelegate getInstance() {
		return singleton;
	}
	
	public BaseDocument getDocument(String docUid, String mime) {
		return store.getDocument(docUid, mime);
	}
	public BaseDocument createDocument(String docUid, String mime) throws IOException {
		return store.createDocument(docUid, mime);
	}
	
	public boolean removeDocument(String docUid) {
		return store.deleteDocument(docUid);
	}

}
