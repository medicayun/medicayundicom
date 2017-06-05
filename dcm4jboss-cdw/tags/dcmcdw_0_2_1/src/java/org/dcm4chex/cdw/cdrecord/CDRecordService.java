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

import org.dcm4che.data.Dataset;
import org.dcm4chex.cdw.common.AbstractMediaWriterService;
import org.dcm4chex.cdw.common.Executer;
import org.dcm4chex.cdw.common.ExecutionStatusInfo;
import org.dcm4chex.cdw.common.MediaCreationException;
import org.dcm4chex.cdw.common.MediaCreationRequest;
import org.jboss.system.server.ServerConfigLocator;

/**
 * @author gunter.zeilinter@tiani.com
 * @version $Revision: 4142 $ $Date: 2004-07-09 05:40:43 +0800 (周五, 09 7月 2004) $
 * @since 22.06.2004
 *
 */
public class CDRecordService extends AbstractMediaWriterService {

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

    private String device = "0,0";

    private int writeSpeed = 24;

    private String writeMode = "tao";

    private String trackType = "data";

    private boolean padding = false;

    private boolean multiSession = false;

    private boolean appendEnabled = false;

    private boolean simulate = false;

    private boolean eject = true;

    private boolean logEnabled = false;

    private final File logFile;

    public CDRecordService() {
        File homedir = ServerConfigLocator.locate().getServerHomeDir();
        logFile = new File(homedir, "log" + File.separatorChar + "cdrecord.log");
    }

    public final boolean isPadding() {
        return padding;
    }

    public final void setPadding(boolean padding) {
        this.padding = padding;
    }

    public final boolean isMultiSession() {
        return multiSession;
    }

    public final void setMultiSession(boolean multiSession) {
        this.multiSession = multiSession;
    }

    public final boolean isAppendEnabled() {
        return appendEnabled;
    }

    public final void setAppendEnabled(boolean appendEnabled) {
        this.appendEnabled = appendEnabled;
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

    public final boolean isLogEnabled() {
        return logEnabled;
    }

    public final void setLogEnabled(boolean logEnabled) {
        this.logEnabled = logEnabled;
    }

    protected void handle(MediaCreationRequest r, Dataset attrs)
            throws MediaCreationException {
        if (!checkDrive())
                throw new MediaCreationException(
                        ExecutionStatusInfo.CHECK_MCD_SRV, "Drive Check failed");
        if (!checkDisk())
                throw new MediaCreationException(
                        ExecutionStatusInfo.OUT_OF_SUPPLIES,
                        "No or Wrong Media");
        if (hasTOC() && !appendEnabled)
                throw new MediaCreationException(
                        ExecutionStatusInfo.OUT_OF_SUPPLIES, "Media not empty");
        burn(r.getIsoImageFile());
    }

    public void burn(File isoImageFile) throws MediaCreationException {
        int exitCode;
        OutputStream logout = null;
        try {
            ArrayList cmd = new ArrayList();
            cmd.add("cdrecord");
            cmd.add("dev=" + device);
            cmd.add("speed=" + writeSpeed);
            if (simulate) cmd.add("-dummy");
            if (eject) cmd.add("-eject");
            if (!TAO.equals(writeMode)) cmd.add("-" + writeMode);
            if (multiSession) cmd.add("-multi");
            cmd.add("-s");
            cmd.add(padding ? "-pad" : "-nopad");
            cmd.add("-" + trackType);
            cmd.add(isoImageFile.getAbsolutePath());
            String[] cmdarray = (String[]) cmd.toArray(new String[cmd.size()]);
            if (logEnabled)
                logout = new BufferedOutputStream(new FileOutputStream(logFile));
            exitCode = new Executer(log, cmdarray, logout, null).waitFor();
        } catch (InterruptedException e) {
            throw new MediaCreationException(ExecutionStatusInfo.PROC_FAILURE,
                    e);
        } catch (IOException e) {
            throw new MediaCreationException(ExecutionStatusInfo.PROC_FAILURE,
                    e);
        } finally {
            if (logout != null)
                try { logout.close(); } catch (IOException ignore) {}
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

    private boolean cdrecord(String option, String match)
            throws MediaCreationException {
        String[] cmdarray = { "cdrecord", "dev=" + device, option};
        try {
            ByteArrayOutputStream stdout = new ByteArrayOutputStream();
            Executer ex = new Executer(log, cmdarray, stdout, null);
            int exit = ex.waitFor();
            if (exit != 0)
                    throw new MediaCreationException(
                            ExecutionStatusInfo.PROC_FAILURE, ex.cmd()
                                    + " failed with exit status:" + exit);
            return stdout.toString().indexOf(match) != -1;
        } catch (InterruptedException e) {
            throw new MediaCreationException(ExecutionStatusInfo.PROC_FAILURE,
                    e);
        } catch (IOException e) {
            throw new MediaCreationException(ExecutionStatusInfo.PROC_FAILURE,
                    e);
        }
    }

}
