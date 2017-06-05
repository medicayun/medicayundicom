/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.cdw.cdrecord;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;

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
public class CDRecordService extends AbstractMediaWriterService {

    private static final int MIN_RETRY_INTERVAL = 10;

    private static final int MIN_GRACE_TIME = 2;

    private static final String TAO = "tao";

    private static final String DAO = "dao";

    private static final String SAO = "sao";

    private static final String RAW = "raw";

    private static final String RAW96R = "raw96r";

    private static final String RAW96P = "raw96p";

    private static final String RAW16 = "raw16";

    private static final String[] WRITE_MODES = { TAO, DAO, SAO, RAW, RAW96R,
            RAW96P, RAW16};

    private static final String DATA = "data";

    private static final String MODE2 = "mode2";

    private static final String XA = "xa";

    private static final String XA1 = "xa1";

    private static final String XA2 = "xa2";

    private static final String XAMIX = "xamix";

    private static final String[] TRACK_TYPES = { DATA, MODE2, XA, XA1, XA2,
            XAMIX};

    private String executable = "cdrecord";

    private String device = "0,0,0";

    private String drivePath;

    private File driveDir;

    private int writeSpeed = 24;

    private String writeMode = "tao";

    private String trackType = "data";

    private boolean appendEnabled = false;

    private boolean multiSession = false;

    private boolean padding = false;

    private boolean simulate = false;

    private boolean eject = true;

    private int numberOfRetries = 0;

    private int retryInterval = 60;

    private boolean verify = false;

    private boolean mount = false;

    private boolean logEnabled = false;

    private int graceTime = MIN_GRACE_TIME;

    private int mountTime = 10;

    private int pauseTime = 10;

    private final File logFile;

    private final LabelPrintDelegate labelPrint = new LabelPrintDelegate(this);

    public CDRecordService() {
        File homedir = ServerConfigLocator.locate().getServerHomeDir();
        logFile = new File(homedir, "log" + File.separatorChar + "cdrecord.log");
    }

    public final ObjectName getLabelPrintName() {
        return labelPrint.getLabelPrintName();
    }

    public final void setLabelPrintName(ObjectName labelPrintName) {
        labelPrint.setLabelPrintName(labelPrintName);
    }

    public final String getDriveLetterOrMountDirectory() {
        return drivePath;
    }

    public final void setDriveLetterOrMountDirectory(String drivePath) {
        boolean mount = drivePath.charAt(0) == '/';
        if (!mount && drivePath.charAt(1) != ':')
                throw new IllegalArgumentException("drivePath: " + drivePath);
        this.drivePath = drivePath;
        this.driveDir = new File(drivePath);
        this.mount = mount;
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

    public final int getGraceTime() {
        return graceTime;
    }

    public final void setGraceTime(int graceTime) {
        if (graceTime < MIN_GRACE_TIME)
                throw new IllegalArgumentException("graceTime: " + graceTime);
        this.graceTime = graceTime;
    }

    public final int getMountTime() {
        return mountTime;
    }

    public final void setMountTime(int mountTime) {
        this.mountTime = mountTime;
    }

    public final int getPauseTime() {
        return pauseTime;
    }

    public final void setPauseTime(int pauseTime) {
        if (pauseTime < 0)
                throw new IllegalArgumentException("pauseTime: " + pauseTime);
        this.pauseTime = pauseTime;
    }

    public final boolean isPadding() {
        return padding;
    }

    public final void setPadding(boolean padding) {
        this.padding = padding;
    }

    public final boolean isAppendEnabled() {
        return appendEnabled;
    }

    public final void setAppendEnabled(boolean appendEnabled) {
        this.appendEnabled = appendEnabled;
    }

    public final boolean isMultiSession() {
        return multiSession;
    }

    public final void setMultiSession(boolean multiSession) {
        this.multiSession = multiSession;
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

    public final String getTrackType() {
        return trackType;
    }

    public final void setTrackType(String trackType) {
        if (Arrays.asList(TRACK_TYPES).indexOf(trackType) == -1)
                throw new IllegalArgumentException("trackType:" + trackType);
        this.trackType = trackType;
    }

    public final String getWriteMode() {
        return writeMode;
    }

    public final void setWriteMode(String writeMode) {
        if (Arrays.asList(WRITE_MODES).indexOf(writeMode) == -1)
                throw new IllegalArgumentException("writeMode:" + writeMode);
        this.writeMode = writeMode;
    }

    public final int getWriteSpeed() {
        return writeSpeed;
    }

    public final void setWriteSpeed(int writeSpeed) {
        if (writeSpeed < 0)
                throw new IllegalArgumentException("writeSpeed:" + writeSpeed);
        this.writeSpeed = writeSpeed;
    }

    public final String getDevice() {
        return device;
    }

    public final void setDevice(String device) {
        this.device = device;
    }

    public final String getExecutable() {
        return executable;
    }

    public final void setExecutable(String command) {
        this.executable = command;
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
                    if (!hasTOC())
                            // load media
                            throw new MediaCreationException(
                                    ExecutionStatusInfo.PROC_FAILURE,
                                    "Verification failed!");
                    if (!mount) { // On Windows wait for automount
                        if (mountTime > 0) {
                            try {
                                Thread.sleep(mountTime * 1000L);
                            } catch (InterruptedException e) {
                                log.warn("Mount Time was interrupted:", e);
                            }
                        }
                    } else {
                        mount();
                    }
                    try {
                        log.info("Verifying " + rq);
                        verify(rq.getFilesetDir());
                        log.info("Verified " + rq);
                    } finally {
                        if (mount) umount();
                    }
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
            cmd.add("dev=" + device);
            cmd.add("speed=" + writeSpeed);
            cmd.add("gracetime=" + graceTime);
            if (multiSession) cmd.add("-multi");
            if (simulate) cmd.add("-dummy");
            // eject for verify on windows
            if (verify ? !mount : eject) cmd.add("-eject");
            if (!TAO.equals(writeMode)) cmd.add("-" + writeMode);
            cmd.add("-s");
            cmd.add(padding ? "-pad" : "-nopad");
            cmd.add("-" + trackType);
            cmd.add(isoImageFile.getAbsolutePath());
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
                ExecutionStatusInfo.MCD_FAILURE, "cdrecord " + isoImageFile
                        + " returns " + exitCode); }
    }

    public boolean checkDrive() throws MediaCreationException {
        return cdrecord("-checkdrive", "TAO");
    }

    public boolean checkDisk() throws MediaCreationException {
        return cdrecord("-atip", "Disk");
    }

    public boolean hasTOC() throws MediaCreationException {
        return cdrecord("-toc", "track");
    }

    public void eject() {
        String[] cmdarray = { executable, "dev=" + device, "-eject"};
        try {
            int exit = new Executer(cmdarray, null, null).waitFor();
            if (exit != 0)
                    log.warn("Failed to eject Media: exit(" + exit + ")");
        } catch (Exception e) {
            log.warn("Failed to eject Media:", e);
        }
    }

    public void mount() throws MediaCreationException {
        String[] cmdArray = { "mount", drivePath};
        int exit = 0;
        try {
            exit = new Executer(cmdArray).waitFor();
        } catch (Exception e) {
            throw new MediaCreationException(ExecutionStatusInfo.PROC_FAILURE,
                    "mount " + drivePath + " failed:", e);
        }
        if (exit != 0)
                throw new MediaCreationException(
                        ExecutionStatusInfo.PROC_FAILURE, "mount " + drivePath
                                + " failed: exit(" + exit + ")");
    }

    public boolean umount() {
        String[] cmdArray = { "umount", "-l", drivePath};
        try {
            int exit = new Executer(cmdArray).waitFor();
            if (exit == 0) return true;
            log.warn("umount -l " + drivePath + " failed: exit(" + exit + ")");
        } catch (Exception e) {
            log.warn("umount -l " + drivePath + " failed:", e);
        }
        return false;
    }

    private boolean cdrecord(String option, String match)
            throws MediaCreationException {
        String[] cmdarray = { executable, "dev=" + device, option};
        try {
            ByteArrayOutputStream stdout = new ByteArrayOutputStream();
            Executer ex = new Executer(cmdarray, stdout, null);
            int exit = ex.waitFor();
            String result = stdout.toString();
            if (log.isDebugEnabled()) log.debug("cdrecord stdout: " + result);
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