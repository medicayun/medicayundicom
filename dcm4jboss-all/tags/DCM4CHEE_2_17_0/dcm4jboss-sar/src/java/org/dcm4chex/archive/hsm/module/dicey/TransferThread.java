package org.dcm4chex.archive.hsm.module.dicey;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import org.apache.log4j.Logger;

public class TransferThread extends Thread {
    public File source;
    public File destination;
    public long lengthInBytes;
    public long chunckSizeInBytes;
    public boolean verbose;
    private static final Logger log = Logger.getLogger(TransferThread.class);

    public TransferThread(File inFile, File outFile, long length, int i) {
        this.source = inFile;
        this.destination = outFile;
        this.lengthInBytes = length;
        this.chunckSizeInBytes = i;
    }

    public void run() {
        log.debug("Opening streams");
        FileInputStream fileInputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            try {
                fileInputStream = new FileInputStream(source);
            } catch (FileNotFoundException e) {
                log.error("Source file not found:"+source);
                throw e;
            }
            try {
                fileOutputStream = new FileOutputStream(destination);
            } catch (FileNotFoundException e) {
                log.error("Opening destination file failed:"+destination);
                throw e;
            }
            log.debug("Opening channels");
            FileChannel inputChannel = fileInputStream.getChannel();
            FileChannel outputChannel = fileOutputStream.getChannel();
            long overallBytesTransfered = 0L;

            try {
                while (overallBytesTransfered < lengthInBytes) {
                    long bytesToTransfer = Math.min(chunckSizeInBytes,
                            lengthInBytes - overallBytesTransfered);
                    long bytesTransfered = 0;
                    log.debug("Transfer bytes: " + bytesToTransfer);
                    try {
                        bytesTransfered = inputChannel.transferTo(
                                overallBytesTransfered, bytesToTransfer,
                                outputChannel);
                    } catch (IOException e) {
                        log.error("Copy of chunks Failed");
                        throw e;
                    }
                    overallBytesTransfered += bytesTransfered;
                }
            } catch (IOException e) {
                log.error("Copy Failed");
                throw e;

            } finally {
                fileInputStream.close();
                fileOutputStream.close();
                inputChannel.close();
                outputChannel.close();
            }
        } catch (IOException e) {
            log.error("IOException:"+e.getMessage(), e);
        } finally {
            interrupt();
        }
    }
}
