package org.dcm4chee.docstore.spi.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.management.ObjectName;

import org.dcm4chee.docstore.Availability;
import org.dcm4chee.docstore.BaseDocument;
import org.dcm4chee.docstore.spi.DocumentStorage;
import org.dcm4chex.rid.mbean.RIDService;
import org.jboss.system.server.ServerConfigLocator;
import org.apache.log4j.Logger;

public class DocumentFileStorage implements DocumentStorage {

	private static final String MIMEFILE_EXTENSION = ".mime";
	private static final String HASHFILE_EXTENSION = ".sha1";
	public static final String STORAGE_TYPE = "SimpleFileStorage";
	private static final String DEFAULT_BASE_DIR = "store/docs";
	
    private int[] directoryTree = new int[]{347, 331, 317};

	private File baseDir;
	private String name;
	
	private static final Set<String> facilities = new HashSet<String>();	
	static {
		facilities.add("SHA1");
	}
	
	private static Logger log = Logger.getLogger( RIDService.class.getName() );
			
	public DocumentFileStorage() {
		this(STORAGE_TYPE);
	}
	
	public DocumentFileStorage(String name) {
		this.name = name;
	}

	public void init(String initString) {
		setBaseDir(initString == null ? DEFAULT_BASE_DIR : "store/"+initString);
		log.info("DocumentFileStorage initialized! baseDir:"+baseDir);
	}
	
	public void setBaseDir(String dirName) {
		File dir = new File(dirName);
		if ( dir.isAbsolute() ) {
			baseDir = dir;
		} else {
			File serverHomeDir = ServerConfigLocator.locate().getServerHomeDir();
			baseDir = new File(serverHomeDir, dir.getPath());
		}
	}
	public boolean deleteDocument(String docUID) {
		File f = getDocumentFile(docUID);
		new File(f.getAbsolutePath()+MIMEFILE_EXTENSION).delete();
		if ( f.exists() ) {
			return f.delete();
		}
		return false;
	}

	public Availability getAvailabilty(String docUid) {
		File f = getDocumentFile(docUid);
		return f.exists() ? Availability.ONLINE : Availability.UNAVAILABLE;
	}

	public String getName() {
		return name;
	}

	public String getRetrieveURL(String docUid) {
		return null;
	}

	public ObjectName getMBeanServiceName() {
		return null;
	}

	public String getStorageType() {
		return STORAGE_TYPE;
	}

	public BaseDocument retrieveDocument(String docUid) throws IOException {
		File f = getDocumentFile(docUid);
		if ( f.exists() ) {
			byte[] ba = readFile(f);
			String mime = ba != null ? new String(ba) : null;
			return new BaseDocument(docUid, mime, new DataHandler(new FileDataSource(f)), Availability.ONLINE, f.length());
		} else {
			return null;
		}
	}

	public BaseDocument createDocument(String docUid, String mime) throws IOException { 
		File f = getDocumentFile(docUid);
		writeFile(new File(f.getAbsolutePath()+MIMEFILE_EXTENSION),mime.getBytes());
		return new BaseDocument(docUid, mime, 
				new DataHandler(new FileDataSource(f)),	Availability.UNAVAILABLE, f.length());
	}
	public BaseDocument storeDocument(String docUid, DataHandler dh) throws IOException {
		File f = getDocumentFile(docUid);
		log.debug("#### Document File:"+f);
		log.debug("#### Document File exist?:"+f.exists());
		try {
			byte[] digest = writeFile(f, dh);
			writeFile(new File(f.getAbsolutePath()+MIMEFILE_EXTENSION),dh.getContentType().getBytes());
			writeFile(new File(f.getAbsolutePath()+HASHFILE_EXTENSION), digest);
			return digest == null ? null : new BaseDocument(docUid, dh.getContentType(), 
					new DataHandler(new FileDataSource(f)),	Availability.ONLINE, f.length());
		} catch (NoSuchAlgorithmException x) {
			log.error("Store of document "+docUid+" failed! Can't calculate hash value!",x);
			throw new IOException("Store of document "+docUid+" failed! Unknown Hash Algorithm!");
		} finally {
			try {
				dh.getInputStream().close();
			} catch (IOException ignore) {
				log.warn("Error closing InputStream of DataHandler! Ignored",ignore );
			}
		}
	}
	
	private byte[] writeFile(File f, DataHandler dh) throws IOException, NoSuchAlgorithmException {
		MessageDigest md = null;
		DigestOutputStream dos = null;
		if ( !f.exists() ) {
			log.info("#### Write File:"+f);
			try {
				f.getParentFile().mkdirs();
				md = MessageDigest.getInstance("SHA1");
				FileOutputStream fos = new FileOutputStream(f);
				dos = new DigestOutputStream(fos, md);
				dh.writeTo(dos);
				log.info("#### File written:"+f+" exists:"+f.exists());
			} finally {
				if ( dos != null )
					try {
						dos.close();
					} catch (IOException ignore) {
						log.error("Ignored error during close!",ignore);
					}
					
			}
		}
		return md == null ? null : md.digest();
	}

	private void writeFile(File f, byte[] ba) throws IOException {
		FileOutputStream fos = null;
		try {
			f.getParentFile().mkdirs();
			fos = new FileOutputStream(f);
			fos.write(ba);
		} finally {
			if ( fos != null ) {
				try {
					fos.close();
				} catch (IOException ignore) {
					log.warn("Cant close FileOutputStream! ignored! reason:"+ignore);
				}
			}
		}
	}
	private byte[] readFile(File f) throws IOException {
		if ( !f.exists() )
			return null;
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(f);
			byte[] ba = new byte[fis.available()];
			fis.read(ba);
			return ba;
		} finally {
			if ( fis != null ) {
				try {
					fis.close();
				} catch (IOException ignore) {
					log.warn("Cant close FileInputStream! ignored! reason:"+ignore);
				}
			}
		}
	}
	
	private File getDocumentFile(String docUid) {
		return new File( baseDir, getFilename(docUid) );
	}

	public boolean commitDocument(String docUid) {
		return true;
	}

	public Set<String> getFacilities() {
		return facilities ;
	}

	public boolean matchFacilities(Set<String> facilities) {
		return this.facilities.containsAll(facilities);
	}

    private String getFilename(String uid) {
        if (directoryTree == null) 
        	return uid;
        StringBuffer sb = new StringBuffer();
        int hash = uid.hashCode();
        int modulo;
        for (int i = 0; i < directoryTree.length; i++) {
            if (directoryTree[i] == 0) {
                sb.append(Integer.toHexString(hash)).append(File.separatorChar);
            } else {
                modulo = hash % directoryTree[i];
                if (modulo < 0) {
                    modulo *= -1;
                }
                sb.append(modulo).append(File.separatorChar);
            }
        }
        sb.append(uid);
        return sb.toString();
    }

}
