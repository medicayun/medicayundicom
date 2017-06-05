/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.cdw.nerocmd;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import javax.jms.JMSException;
import javax.management.ObjectName;

import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import org.dcm4chex.cdw.common.AbstractMediaWriterService;
import org.dcm4chex.cdw.common.Executer;
import org.dcm4chex.cdw.common.ExecutionStatus;
import org.dcm4chex.cdw.common.ExecutionStatusInfo;
import org.dcm4chex.cdw.common.FileUtils;
import org.dcm4chex.cdw.common.JMSDelegate;
import org.dcm4chex.cdw.common.LabelPrintDelegate;
import org.dcm4chex.cdw.common.MediaCreationException;
import org.dcm4chex.cdw.common.MediaCreationRequest;
import org.jboss.system.server.ServerConfigLocator;

/**
 * @author gunter.zeilinter@tiani.com
 * @version $Revision: 4189 $ $Date: 2004-09-26 04:36:31 +0800 (周日, 26 9月 2004) $
 * @since 22.06.2004
 *
 */
public class NeroCmdService extends AbstractMediaWriterService {

    private static final int MIN_RETRY_INTERVAL = 10;

    private String executable = "nerocmd";

    private String driveLetter;

    private File driveDir;

    private boolean dvd = false;

    private int writeSpeed = 24;

    private boolean autoLoad = false;

    private boolean appendEnabled = false;

    private boolean closeSession = true;

    private boolean simulate = false;

    private boolean eject = true;

    private int numberOfRetries = 0;

    private int retryInterval = 60;

    private boolean verify = false;
    
    private boolean logEnabled = false;

    private int pauseTime = 10;

    private int mountTime = 10;

    private final File logFile;

    private final LabelPrintDelegate labelPrint = new LabelPrintDelegate(this);

    public NeroCmdService() {
        File homedir = ServerConfigLocator.locate().getServerHomeDir();
        logFile = new File(homedir, "log" + File.separatorChar + "nerocmd.log");
    }

    public final ObjectName getLabelPrintName() {
        return labelPrint.getLabelPrintName();
    }

    public final void setLabelPrintName(ObjectName labelPrintName) {
        labelPrint.setLabelPrintName(labelPrintName);
    }

    public final String getDriveLetter() {
        return driveLetter;
    }

    public final void setDriveLetter(String drivePath) {
        this.driveLetter = drivePath;
        this.driveDir = new File(drivePath + ':');
    }

    public final int getNumberOfRetries() {
        return numberOfRetries;
    }

    public final void setNumberOfRetries(int numberOfRetries) {
        if (numberOfRetries < 0)
                throw new IllegalArgumentException("numberOfRetries:"
                        + numberOfRetries);
        this.numberOfRetries = numberOfRetries;
    }

    public final int getRetryInterval() {
        return retryInterval;
    }

    public final void setRetryInterval(int retryInterval) {
        if (retryInterval < MIN_RETRY_INTERVAL)
                throw new IllegalArgumentException("retryInterval:"
                        + retryInterval);
        this.retryInterval = retryInterval;
    }

    public final boolean isAutoLoad() {
        return autoLoad;
    }

    public final void setAutoLoad(boolean autoLoad) {
        this.autoLoad = autoLoad;
    }

    public final boolean isVerify() {
        return verify;
    }

    public final void setVerify(boolean verify) {
        this.verify = verify;
    }

    public String getKeepLabelFiles() {
        return labelPrint.getKeepLabelFiles();
    }

    public void setKeepLabelFiles(String s) {
        labelPrint.setKeepLabelFiles(s);
    }

    public boolean isPrintLabel() {
        return labelPrint.isPrintLabel();
    }

    public void setPrintLabel(boolean printLabel) {
        labelPrint.setPrintLabel(printLabel);
    }
    
    public final int getPauseTime() {
        return pauseTime;
    }

    public final void setPauseTime(int pauseTime) {
        if (pauseTime < 0)
                throw new IllegalArgumentException("pauseTime: " + pauseTime);
        this.pauseTime = pauseTime;
    }

    public final boolean isAppendEnabled() {
        return appendEnabled;
    }

    public final void setAppendEnabled(boolean appendEnabled) {
        this.appendEnabled = appendEnabled;
    }

    public final boolean isCloseSession() {
        return closeSession;
    }

    public final void setCloseSession(boolean closeSession) {
        this.closeSession = closeSession;
    }

    public final boolean isSimulate() {
        return simulate;
    }

    public final void setSimulate(boolean simulate) {
        this.simulate = simulate;
    }

    public final boolean isEject() {
        return eject;
    }

    public final void setEject(boolean eject) {
        this.eject = eject;
    }

    public final int getWriteSpeed() {
        return writeSpeed;
    }

    public final void setWriteSpeed(int writeSpeed) {
        if (writeSpeed < 0)
                throw new IllegalArgumentException("writeSpeed:" + writeSpeed);
        this.writeSpeed = writeSpeed;
    }

    public final String getExecutable() {
        return executable;
    }

    public final void setExecutable(String executable) {
        this.executable = executable;
    }

    public final boolean isDvd() {
        return dvd;
    }

    public final void setDvd(boolean dvd) {
        this.dvd = dvd;
    }

    public final int getMountTime() {
        return mountTime;
    }

    public final void setMountTime(int mountTime) {
        this.mountTime = mountTime;
    }

    public final boolean isLogEnabled() {
        return logEnabled;
    }

    public final void setLogEnabled(boolean logEnabled) {
        this.logEnabled = logEnabled;
    }

    protected void handle(MediaCreationRequest rq, Dataset attrs)
            throws MediaCreationException, IOException {
        if (!checkDrive())
                throw new MediaCreationException(
                        ExecutionStatusInfo.CHECK_MCD_SRV, "Drive Check failed");
        int tot = attrs.getInt(Tags.TotalNumberOfPiecesOfMediaCreated, 0);
        int remaining = rq.getRemainingCopies();
        while (remaining > 0) {
            if (autoLoad)
                load();
            if (!checkDisk()) {
                eject();
                retry(rq, attrs, "No or Wrong Media");
                return;
            }
            if (!appendEnabled && hasTOC()) {
                eject();
                retry(rq, attrs, "Media not empty");
                return;
            }
            try {
                log.info("Burning " + rq);
                burn(rq.getIsoImageFile());
                log.info("Burned " + rq);
                if (verify) {
                    load();
                    if (mountTime > 0) {
                        try {
                            Thread.sleep(mountTime * 1000L);
                        } catch (InterruptedException e) {
                            log.warn("Mount Time was interrupted:", e);
                        }
                    }
                    log.info("Verifying " + rq);
                    verify(rq.getFilesetDir());
                    log.info("Verified " + rq);
                    if (eject) eject();
                }
            } catch (MediaCreationException e) {
                eject();
                throw e;
            }
            
            labelPrint.print(rq);
            
            if (rq.isCanceled()) {
                log.info("" + rq + " was canceled");
                return;
            }
            attrs.putUS(Tags.TotalNumberOfPiecesOfMediaCreated, ++tot);
            rq.setRemainingCopies(--remaining);
            if (remaining > 0) rq.writeAttributes(attrs, log);
            try {
                Thread.sleep(pauseTime * 1000L);
            } catch (InterruptedException e) {
                log.warn("Pause before next burn was interrupted:", e);
            }
        }
    }

    private void retry(MediaCreationRequest rq, Dataset attrs, String msg)
            throws MediaCreationException, IOException {
        if (rq.getRetries() >= numberOfRetries)
                throw new MediaCreationException(
                        ExecutionStatusInfo.OUT_OF_SUPPLIES, msg);
        log.warn(msg + " - retry in " + retryInterval + "s.");
        rq.setRetries(rq.getRetries() + 1);
        attrs.putCS(Tags.ExecutionStatus, ExecutionStatus.PENDING);
        attrs.putCS(Tags.ExecutionStatusInfo,
                ExecutionStatusInfo.OUT_OF_SUPPLIES);
        rq.writeAttributes(attrs, log);
        try {
            JMSDelegate.getInstance(rq.getMediaWriterName()).queue(log,
                    rq,
                    System.currentTimeMillis() + retryInterval * 1000L);
        } catch (JMSException e) {
            throw new MediaCreationException(ExecutionStatusInfo.PROC_FAILURE,
                    e);
        }

    }

    private void verify(File fsDir) throws MediaCreationException {
        try {
            if (!FileUtils.verify(driveDir, fsDir, log)) { throw new MediaCreationException(
                    ExecutionStatusInfo.PROC_FAILURE, "Verification failed!"); }
        } catch (IOException e) {
            throw new MediaCreationException(ExecutionStatusInfo.PROC_FAILURE,
                    "Verification failed:", e);
        }
    }

    public void burn(File isoImageFile) throws MediaCreationException {
        int exitCode;
        OutputStream logout = null;
        try {
            ArrayList cmd = new ArrayList();
            cmd.add(executable);
            cmd.add("--write");
            cmd.add("--drivename");
            cmd.add(driveLetter);
            if (!simulate) cmd.add("--real");
            if (dvd) cmd.add("--dvd");
            cmd.add("--image");
            cmd.add(isoImageFile.getAbsolutePath());
            cmd.add("--speed");
            cmd.add(String.valueOf(writeSpeed));
            if (!eject && !verify) cmd.add("--disable_eject");
            if (closeSession) cmd.add("--close_session");
            String[] cmdarray = (String[]) cmd.toArray(new String[cmd.size()]);
            if (logEnabled)
                    logout = new BufferedOutputStream(new FileOutputStream(
                            logFile));
            exitCode = new Executer(cmdarray, logout, null).waitFor();
        } catch (InterruptedException e) {
            throw new MediaCreationException(ExecutionStatusInfo.PROC_FAILURE,
                    e);
        } catch (IOException e) {
            throw new MediaCreationException(ExecutionStatusInfo.PROC_FAILURE,
                    e);
        } finally {
            if (logout != null) try {
                logout.close();
            } catch (IOException ignore) {
            }
        }
        if (exitCode != 0) { throw new MediaCreationException(
                ExecutionStatusInfo.MCD_FAILURE, "nerocmd " + isoImageFile
                        + " returns " + exitCode); }
    }

    public boolean checkDrive() throws MediaCreationException {
        return nerocmd("--driveinfo", "Device");
    }

    public boolean checkDisk() throws MediaCreationException {
        return nerocmd("--cdinfo", "writeable");
    }

    public boolean hasTOC() throws MediaCreationException {
        return nerocmd("--cdinfo", "not writeable");
    }

    public void eject() {
        String[] cmdarray = { executable, "--eject", "--drivename", driveLetter};
        try {
            int exit = new Executer(cmdarray, null, null).waitFor();
            if (exit != 0)
                    log.warn("Failed to eject Media: exit(" + exit + ")");
        } catch (Exception e) {
            log.warn("Failed to eject Media:", e);
        }
    }

    public void load() {
        String[] cmdarray = { executable, "--load", "--drivename", driveLetter};
        try {
            int exit = new Executer(cmdarray, null, null).waitFor();
            if (exit != 0)
                    log.warn("Failed to load Media: exit(" + exit + ")");
        } catch (Exception e) {
            log.warn("Failed to load Media:", e);
        }
    }

    private boolean nerocmd(String option, String match)
            throws MediaCreationException {
        String[] cmdarray = { executable, option, "--drivename", driveLetter};
        try {
            ByteArrayOutputStream stdout = new ByteArrayOutputStream();
            Executer ex = new Executer(cmdarray, stdout, null);
            int exit = ex.waitFor();
            String result = stdout.toString();
            if (log.isDebugEnabled())
                    log.debug(executable + " stdout: " + result);
            return exit == 0 && result.indexOf(match) != -1;
        } catch (InterruptedException e) {
            throw new MediaCreationException(ExecutionStatusInfo.PROC_FAILURE,
                    e);
        } catch (IOException e) {
            throw new MediaCreationException(ExecutionStatusInfo.PROC_FAILURE,
                    e);
        }
    }
}