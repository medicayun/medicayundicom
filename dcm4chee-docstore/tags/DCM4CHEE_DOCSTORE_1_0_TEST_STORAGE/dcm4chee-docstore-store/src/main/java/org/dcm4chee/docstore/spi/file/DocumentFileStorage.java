package org.dcm4chee.docstore.spi.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;

import org.apache.log4j.Logger;
import org.dcm4chee.docstore.Availability;
import org.dcm4chee.docstore.BaseDocument;
import org.dcm4chee.docstore.DocumentStore;
import org.dcm4chee.docstore.Feature;
import org.dcm4chee.docstore.spi.BaseDocumetStorage;
import org.dcm4chee.docstore.util.FileSystemInfo;
import org.jboss.system.server.ServerConfigLocator;

public class DocumentFileStorage extends BaseDocumetStorage {

    private static final String DEFAULT_MIME_FILENAME = "default.mime";
    public static final String STORAGE_TYPE = "SimpleFileStorage";
    private static final String DEFAULT_BASE_DIR = "store/docs";

    private int[] directoryTree = new int[]{347, 331, 317};

    private File baseDir;
    private Availability currentAvailabilty;
    private long minFree  = 10000000l;

    private boolean computeHash = true;
    private long lastCheck;
    private int checkIntervall = 600000;

    private static Logger log = Logger.getLogger( DocumentFileStorage.class.getName() );

    public DocumentFileStorage() {
        this(STORAGE_TYPE);
    }

    public DocumentFileStorage(String name) {
        super(name);
        this.addFeature( Feature.CREATE_EMPTY_DOCUMENT );
        this.addFeature( Feature.MULTI_MIME );
        this.addFeature(new Feature("SHA1", "Generation of SHA1 hash") );
    }

    public void init(String initString) {
        String basedir = null;
        String s = null;
        try {
            Properties p = this.readInitAsProperties(initString);
            basedir = p.getProperty("BASE_DIR");
            s = p.getProperty("minFree");
            if ( s != null ) {
                minFree = Long.parseLong(s);
            }
            s = p.getProperty("checkIntervall");
            if ( s != null ) {
                checkIntervall = Integer.parseInt(s);
            }
            s = p.getProperty("disableHash");
            if ( s != null && s.equalsIgnoreCase("true") ) {
                computeHash = false;
            }
            s = p.getProperty("dfCmdName", "dcm4chee.archive:service=dfcmd");
            FileSystemInfo.setDFCmdServiceName(s);
        } catch (IOException e) {
            log.error("Cant initialize DocumentFileStorage!", e);
            throw new IllegalArgumentException("Initialization of DocumentFileStorage failed! initString"+initString);
        } catch (NumberFormatException e) {
            log.warn("Illegal minFree value! ("+s+")! Use default:"+minFree,e);
        }
        setBaseDir(basedir == null ? DEFAULT_BASE_DIR : basedir);
        log.info("DocumentFileStorage initialized! :"+this);
    }

    public void setBaseDir(String dirName) {
        File dir = new File(dirName);
        if ( dir.isAbsolute() ) {
            baseDir = dir;
        } else {
            File serverHomeDir = ServerConfigLocator.locate().getServerHomeDir();
            baseDir = new File(serverHomeDir, dir.getPath());
        }
        if ( !baseDir.exists() ) {
            baseDir.mkdirs();
        }
    }

    public Availability getStorageAvailability() {
        log.debug("getStorageAvailability called! currentAvailabilty:"+currentAvailabilty);
        return currentAvailabilty == null || System.currentTimeMillis() - lastCheck > checkIntervall ? 
                checkAvailabilty() : currentAvailabilty;
    }

    public Availability checkAvailabilty() {
        log.debug("checkAvailabilty called! currentAvailabilty:"+currentAvailabilty);
        currentAvailabilty = FileSystemInfo.getFileSystemAvailability(baseDir, minFree);
        log.debug("checkAvailabilty done! currentAvailabilty:"+currentAvailabilty);
        lastCheck = System.currentTimeMillis();
        return currentAvailabilty;
    }

    public boolean deleteDocument(String docUID) {
        boolean b = false;
        File f = getDocumentPath(docUID);
        if ( f.exists() ) {
            b = deleteFile(f);
            purgeDocumentPath(f.getParentFile());
        }
        return b;
    }

    private boolean deleteFile(File f) {
        if ( f.isDirectory()) {
            File[] files = f.listFiles();
            for ( int i = 0; i < files.length ; i++ ) {
                deleteFile(files[i]);
            }
        }
        log.info("M-DELETE DocumentStorage file:"+f);
        return f.delete();
    }
    
    private void purgeDocumentPath(File dir) {
        if (dir == null || dir.equals(baseDir))
            return;
        File[] files = dir.listFiles();
        if (files != null && files.length == 0) {
            dir.delete();
            purgeDocumentPath(dir.getParentFile());
        }
    }

    public Availability getAvailabilty(String docUid) {
        File f = getDocumentPath(docUid);
        return f.exists() ? Availability.ONLINE : Availability.UNAVAILABLE;
    }

    public String getRetrieveURL(String docUid) {
        return null;
    }

    public String getStorageType() {
        return STORAGE_TYPE;
    }

    public BaseDocument retrieveDocument(String docUid) throws IOException {
        return retrieveDocument(docUid, null);
    }

    public BaseDocument retrieveDocument(String docUid, String mime) throws IOException {
        File docPath = getDocumentPath(docUid);
        BaseDocument doc = null;
        if ( docPath.exists() ) {
            File f = getDocumentFile(docPath, mime);
            log.debug("docFile:"+f+" exists:"+f.exists());
            if ( f.exists() ){
                doc = new BaseDocument(docUid, mime, new DataHandler(new FileDataSource(f)), 
                    Availability.ONLINE, docPath.length(), this);
                notifyRetrieved(doc);
            }
        }
        return doc;
    }

    public BaseDocument createDocument(String docUid, String mime) throws IOException { 
        File docPath = getDocumentPath(docUid);
        File defaultMime = getMimeFile(docPath);
        if ( ! defaultMime.exists()) {
            writeFile(getMimeFile(docPath),mime.getBytes());
        }
        File docFile = this.getDocumentFile(docPath, mime);
        log.debug("M-CREATE: Empty document file created:"+docFile);
        BaseDocument doc = new BaseDocument(docUid, mime, 
                new DataHandler(new FileDataSource(docFile)), Availability.UNAVAILABLE, docFile.length(), this);
        notifyCreated(doc);
        return doc;
    }

    public BaseDocument storeDocument(String docUid, DataHandler dh) throws IOException {
        File docPath = getDocumentPath(docUid);
        log.debug("#### Document Path:"+docPath);
        log.debug("#### Document Path exist?:"+docPath.exists());
        try {
            File docFile = this.getDocumentFile(docPath, dh.getContentType());
            log.debug("#### Document File:"+docFile);
            log.debug("#### Document File exist?:"+docFile.exists());
            if ( docFile.exists() )
                return null;
            byte[] digest = writeFile(docFile, dh);
            File mimeFile = getMimeFile(docPath);
            if ( !mimeFile.exists() ) {
                writeFile(mimeFile,dh.getContentType().getBytes());
            }
            BaseDocument doc = new BaseDocument(docUid, dh.getContentType(), 
                    new DataHandler(new FileDataSource(docFile)), Availability.ONLINE, docFile.length(), this);
            doc.setHash(DocumentStore.toHexString(digest));
            notifyStored(doc);
            return doc;
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
        OutputStream out = null;
        if ( !f.exists() ) {
            log.debug("#### Write File:"+f);
            try {
                f.getParentFile().mkdirs();
                FileOutputStream fos = new FileOutputStream(f);
                if (computeHash) {
                    md = MessageDigest.getInstance("SHA1");
                    out = new DigestOutputStream(fos, md);
                } else {
                    log.debug("SHA1 feature is disabled!");
                    out = fos;
                }
                dh.writeTo(out);
                log.debug("#### File written:"+f+" exists:"+f.exists());
            } finally {
                if ( out != null )
                    try {
                        out.close();
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

    private File getDocumentPath(String docUid) {
        if ( baseDir == null ) {
            setBaseDir(DEFAULT_BASE_DIR);
            log.warn("DocumentFileStorage not initialized! Set default Base Directory:"+baseDir);
        }
        log.debug("getDocumentPath for "+docUid+" for DocumentStorage "+this.getName()+". baseDir:"+baseDir);
        return new File( baseDir, getFilepath(docUid) );
    }

    private File getDocumentFile(File docPath, String mime) throws IOException {
        if ( mime == null ) {
            byte[] ba = readFile(getMimeFile(docPath));
            mime = ba != null ? new String(ba) : "unknown_mime";
        }
        mime = mime.replace('/', '_');
        return new File(docPath, URLEncoder.encode(mime, "UTF-8"));
    }

    private File getMimeFile(File docPath) {
        return new File(docPath.getAbsolutePath(), DEFAULT_MIME_FILENAME);
    }

    public boolean commitDocument(String docUid) {
        return true;
    }

    private String getFilepath(String uid) {
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

    public String toString() {
        return super.toString()+" baseDir:"+baseDir+"(minFree:"+this.minFree+") "+
            this.getStorageAvailability();
    }
}
