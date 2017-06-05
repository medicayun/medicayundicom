package org.dcm4chex.archive.hsm.module.dicey;


import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

public class FileIOTimeOut {
    private static final Logger log = Logger.getLogger(FileIOTimeOut.class);
    private static final int CHUNKSIZE = 1024 * 1024 * 4; // 4MB chunks

    public static void copy(File source, File destination, int timeOut)
    throws IOException {
        try {
            TransferThread ioThread = new TransferThread(source, destination, CHUNKSIZE);			
            ioThread.start();
            log.debug("Thread started");
            int MAX_SECONDS = timeOut; // Max number of seconds
            int counter = 0; // current second
            long totalSize = 0L;
            totalSize = destination.length();


            while ( ! ioThread.stop || ioThread.getState() != Thread.State.TERMINATED ) { // if not
                // already
                // terminated
                log.debug("Timeout seconds: " + counter);
                if (counter >= MAX_SECONDS) {
                    // Output filesize did not change for n seconds, therefore
                    // interrupt thread
                    log.debug("TimeOut reached");	
                    ioThread.inputChannel.close();
                    ioThread.outputChannel.close();
                    ioThread.stop = true;
                    ioThread.interrupt();				

                    throw new IOException("TimeOut reached");
                } else {
                    try {
                        Thread.sleep(1000);
                        if (destination.length() > totalSize) {
                            totalSize = destination.length();
                            counter = 0;
                            log.debug("Size:" + totalSize);
                        } else
                            log.debug("Size not changed:" + totalSize);
                    } catch (InterruptedException e) {
                        throw new IOException("Thread Aborted");
                    }
                }
                counter++;
            }
        } catch (Exception e) {
            throw new IOException("FileCopy Not successfull",e);
        }
    }

}
