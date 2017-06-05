package org.dcm4chex.archive.hsm.module.dicey;


import java.io.File;
import java.io.IOException;

import org.apache.log4j.*;

import org.dcm4chex.archive.hsm.module.dicey.DiceyFSModule;
import org.dcm4chex.archive.hsm.module.dicey.FileIOTimeOut;
import org.dcm4chex.archive.hsm.module.HSMException;
import org.dcm4chex.archive.hsm.module.HSMFileBasedModule;
import org.dcm4chex.archive.util.FileUtils;

public class DiceyFSModule extends HSMFileBasedModule {
    private static final Logger log = Logger.getLogger(DiceyFSModule.class);
    private File incomingDir;
    private File absIncomingDir;
    private int readTimeout;
    
    public final String getIncomingDir() {
        return incomingDir.getPath();
    }

    public final void setIncomingDir(String dir) {
        this.incomingDir = new File(dir);
        this.absIncomingDir = FileUtils.resolve(this.incomingDir);
    }
    
    public final int getReadTimeout() {
        return readTimeout;
    }

    public final void setReadTimeout(int to) {
    	 this.readTimeout = to;
    }
  
    @Override
    public File fetchHSMFile(String fsID, String filePath) throws HSMException {
            if (absIncomingDir.mkdirs()) {
                log.info("M-WRITE "+absIncomingDir);
            }
            File tarFile;
            File fileToFetch;
            try {
                tarFile = File.createTempFile("hsm_", ".tar", absIncomingDir);
            } catch (IOException x) {
                throw new HSMException("Failed to create temp file in "+absIncomingDir, x);
            }
            if (fsID.startsWith("tar:"))
                fsID=fsID.substring(4);
            fileToFetch = FileUtils.toFile(fsID, filePath);
            try {
                FileIOTimeOut.copy(fileToFetch, tarFile, readTimeout) ;
            } catch (IOException x) {
                throw new HSMException("Failed to retrieve "+fileToFetch, x);
            }
            return tarFile;
     }
}



