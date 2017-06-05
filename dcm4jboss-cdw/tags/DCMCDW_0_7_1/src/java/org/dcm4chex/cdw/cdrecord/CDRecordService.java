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

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import org.dcm4chex.cdw.common.AbstractMediaWriterService;
import org.dcm4chex.cdw.common.Executer;
import org.dcm4chex.cdw.common.ExecutionStatusInfo;
import org.dcm4chex.cdw.common.MediaCreationException;
import org.dcm4chex.cdw.common.MediaCreationRequest;
import org.jboss.system.server.ServerConfigLocator;

/**
 * @author gunter.zeilinter@tiani.com
 * @version $Revision: 4167 $ $Date: 2004-08-16 07:15:37 +0800 (周一, 16 8月 2004) $
 * @since 22.06.2004
 *
 */
public class CDRecordService extends AbstractMediaWriterService {

    
private static final int MIN_GRACE_TIME = 2;
    private static final String TAO = "tao";

    private static final String DAO = "dao";

    private static final String SAO = "sao";

    private static final String RAW = "raw";

    private static final String RAW96R = "raw96r";

    private static final String RAW96P = "raw96p";

    private static final String RAW16 = "raw16";

    private static final String[] WRITE_MODES =
        { TAO, DAO, SAO, RAW, RAW96R, RAW96P, RAW16 };

    private static final String DATA = "data";

    private static final String MODE2 = "mode2";

    private static final String XA = "xa";

    private static final String XA1 = "xa1";

    private static final String XA2 = "xa2";

    private static final String XAMIX = "xamix";

    private static final String[] TRACK_TYPES =
        { DATA, MODE2, XA, XA1, XA2, XAMIX };

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
    
    private int graceTime = MIN_GRACE_TIME;

    private int pauseTime = 10;

    private final File logFile;
    
    protected ObjectName labelPrintName;

    public CDRecordService() {
        File homedir = ServerConfigLocator.locate().getServerHomeDir();
        logFile =
            new File(homedir, "log" + File.separatorChar + "cdrecord.log");
    }

    public final ObjectName getLabelPrintName() {
        return labelPrintName;
    }
    
    public final void setLabelPrintName(ObjectName labelPrintName) {
        this.labelPrintName = labelPrintName;
    }
    
    public final int getGraceTime() {
        return graceTime;
    }
    
    public final void setGraceTime(int graceTime) {
        if (graceTime < MIN_GRACE_TIME)
            throw new IllegalArgumentException("graceTime: " + graceTime);
        this.graceTime = graceTime;
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

    protected void handle(MediaCreationRequest rq, Dataset attrs)
        throws MediaCreationException, IOException {
        if (!checkDrive())
            throw new MediaCreationException(
                ExecutionStatusInfo.CHECK_MCD_SRV,
                "Drive Check failed");
        int tot = attrs.getInt(Tags.TotalNumberOfPiecesOfMediaCreated, 0);
        for(int i = 0; i < rq.getNumberOfCopies(); ++i, ++tot) {
            if (i > 0) {
	            if (rq.isCanceled()) {
	                log.info("" + rq + " was canceled");
	                return;
	            }
	            attrs.putUS(Tags.TotalNumberOfPiecesOfMediaCreated, tot);
	            rq.writeAttributes(attrs, log);                
            }
	        if (!checkDisk()) {
	            eject();
	            throw new MediaCreationException(
	                ExecutionStatusInfo.OUT_OF_SUPPLIES,
	                "No or Wrong Media");
	        }
	        if (!appendEnabled && hasTOC()) {
	            eject();
	            throw new MediaCreationException(
	                ExecutionStatusInfo.OUT_OF_SUPPLIES,
	                "Media not empty");
	        }
	        burn(rq.getIsoImageFile());
	        if (rq.getLabelFile() != null)
	            printLabel(rq);
	        try {
	            Thread.sleep(pauseTime * 1000L);
	        } catch (InterruptedException e) {
	            log.warn("Pause after burn was interrupted:", e);
	        }
        }
    }

    private void printLabel(MediaCreationRequest rq) throws MediaCreationException {
        try {
            server.invoke(labelPrintName, "print", new Object[] { rq },
                    new String[]{MediaCreationRequest.class.getName()});
        } catch (InstanceNotFoundException e) {
            throw new MediaCreationException(
                    ExecutionStatusInfo.PROC_FAILURE,
                    e);
        } catch (MBeanException e) {
            throw new MediaCreationException(
                    ExecutionStatusInfo.PROC_FAILURE,
                    e);
        } catch (ReflectionException e) {
            throw new MediaCreationException(
                    ExecutionStatusInfo.PROC_FAILURE,
                    e);
        }
    }

    public void burn(File isoImageFile) throws MediaCreationException {
        int exitCode;
        OutputStream logout = null;
        try {
            ArrayList cmd = new ArrayList();
            cmd.add("cdrecord");
            cmd.add("dev=" + device);
            cmd.add("speed=" + writeSpeed);
            cmd.add("gracetime=" + graceTime);
            if (simulate)
                cmd.add("-dummy");
            if (eject)
                cmd.add("-eject");
            if (!TAO.equals(writeMode))
                cmd.add("-" + writeMode);
            if (multiSession)
                cmd.add("-multi");
            cmd.add("-s");
            cmd.add(padding ? "-pad" : "-nopad");
            cmd.add("-" + trackType);
            cmd.add(isoImageFile.getAbsolutePath());
            String[] cmdarray = (String[]) cmd.toArray(new String[cmd.size()]);
            if (logEnabled)
                logout =
                    new BufferedOutputStream(new FileOutputStream(logFile));
            exitCode = new Executer(cmdarray, logout, null).waitFor();
        } catch (InterruptedException e) {
            throw new MediaCreationException(
                ExecutionStatusInfo.PROC_FAILURE,
                e);
        } catch (IOException e) {
            throw new MediaCreationException(
                ExecutionStatusInfo.PROC_FAILURE,
                e);
        } finally {
            if (logout != null)
                try {
                    logout.close();
                } catch (IOException ignore) {}
        }
        if (exitCode != 0) {
            throw new MediaCreationException(
                ExecutionStatusInfo.MCD_FAILURE,
                "cdrecord " + isoImageFile + " returns " + exitCode);
        }
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
        String[] cmdarray = { "cdrecord", "dev=" + device, "-eject" };
        try {
            int exit = new Executer(cmdarray, null, null).waitFor();
            if (exit != 0)
                log.warn("Failed to eject Media: exit(" + exit + ")");
        } catch (Exception e) {
            log.warn("Failed to eject Media:", e);
        }
    }

    private boolean cdrecord(String option, String match)
        throws MediaCreationException {
        String[] cmdarray = { "cdrecord", "dev=" + device, option };
        try {
            ByteArrayOutputStream stdout = new ByteArrayOutputStream();
            Executer ex = new Executer(cmdarray, stdout, null);
            int exit = ex.waitFor();
            String result = stdout.toString();
            if (log.isDebugEnabled())
                log.debug("cdrecord stdout: " + result);
            return exit == 0 && result.indexOf(match) != -1;
        } catch (InterruptedException e) {
            throw new MediaCreationException(
                ExecutionStatusInfo.PROC_FAILURE,
                e);
        } catch (IOException e) {
            throw new MediaCreationException(
                ExecutionStatusInfo.PROC_FAILURE,
                e);
        }
    }
}
