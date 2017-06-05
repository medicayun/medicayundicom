/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.cdw.nerocmd;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.dcm4chex.cdw.common.Executer;
import org.dcm4chex.cdw.common.ExecutionStatusInfo;
import org.dcm4chex.cdw.common.MediaCreationException;
import org.dcm4chex.cdw.common.MediaWriterServiceSupport;

/**
 * @author gunter.zeilinter@tiani.com
 * @version $Revision: 4207 $ $Date: 2004-10-30 02:20:02 +0800 (周六, 30 10月 2004) $
 * @since 22.06.2004
 *
 */
public class NeroCmdService extends MediaWriterServiceSupport {

    private boolean dvd = false;

    private String driveLetter;

    public final String getDriveLetter() {
        return driveLetter;
    }

    public final void setDriveLetter(String driveLetter) {
        this.driveLetter = driveLetter;
        super.setDriveLetterOrMountDirectory(driveLetter + ':');
    }
    
    public final boolean isDvd() {
        return dvd;
    }

    public final void setDvd(boolean dvd) {
        this.dvd = dvd;
    }

    public boolean checkDrive() throws MediaCreationException {
        String[] cmdarray = { executable, "-driveinfo", "--drivename", driveLetter};
        return super.check(cmdarray, "Device");
    }

    public boolean checkDisk() throws MediaCreationException {
        String[] cmdarray = { executable, "-cdinfo", "--drivename", driveLetter};
        return super.check(cmdarray, "writeable");
    }

    public boolean hasTOC() throws MediaCreationException {
        String[] cmdarray = { executable, "-cdinfo", "--drivename", driveLetter};
        return super.check(cmdarray, "not writeable");
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

    protected String[] makeBurnCmd(File isoImageFile) {
        ArrayList cmd = new ArrayList();
        cmd.add(executable);
        cmd.add("--write");
        cmd.add("--drivename");
        cmd.add(driveLetter);
        if (!simulate) cmd.add("--real");
        if (dvd) cmd.add("--dvd");
        cmd.add("--image");
        cmd.add(isoImageFile.getAbsolutePath());
        if (writeSpeed >= 0) {
	        cmd.add("--speed");
	        cmd.add(String.valueOf(writeSpeed));
        }
        if (!eject && !verify) cmd.add("--disable_eject");
        if (!multiSession) cmd.add("--close_session");
        return (String[]) cmd.toArray(new String[cmd.size()]);
    }

    protected String[] makeEjectCmd() {
        return new String[] { executable, "--eject", "--drivename", 
                driveLetter};
    }

    protected String[] makeLoadCmd() {
        return new String[] { executable, "--load", "--drivename",
                driveLetter};
    }
}