/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.cdw.cdrecord;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import org.dcm4chex.cdw.common.MediaCreationException;
import org.dcm4chex.cdw.common.MediaWriterServiceSupport;

/**
 * @author gunter.zeilinter@tiani.com
 * @version $Revision: 4207 $ $Date: 2004-10-30 02:20:02 +0800 (周六, 30 10月 2004) $
 * @since 22.06.2004
 *
 */
public class CDRecordService extends MediaWriterServiceSupport {

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

    private String device = "0,0,0";

    private String writeMode = "tao";

    private String trackType = "data";

    private boolean padding = false;

    private int graceTime = MIN_GRACE_TIME;

    public final int getGraceTime() {
        return graceTime;
    }

    public final void setGraceTime(int graceTime) {
        if (graceTime < MIN_GRACE_TIME)
                throw new IllegalArgumentException("graceTime: " + graceTime);
        this.graceTime = graceTime;
    }
    public final boolean isPadding() {
        return padding;
    }

    public final void setPadding(boolean padding) {
        this.padding = padding;
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

    public final String getDevice() {
        return device;
    }

    public final void setDevice(String device) {
        this.device = device;
    }

    public boolean checkDrive() throws MediaCreationException {
        String[] cmdarray = { executable, "dev=" + device, "-checkdrive"};
        return super.check(cmdarray, "TAO");
    }

    public boolean checkDisk() throws MediaCreationException {
        String[] cmdarray = { executable, "dev=" + device, "-atip"};
        return super.check(cmdarray, "Disk");
    }

    public boolean hasTOC() throws MediaCreationException {
        String[] cmdarray = { executable, "dev=" + device, "-toc"};
        return super.check(cmdarray, "track");
    }
    
    protected String[] makeBurnCmd(File isoImageFile) {
        ArrayList cmd = new ArrayList();
        cmd.add(executable);
        cmd.add("dev=" + device);
        if (writeSpeed >= 0) cmd.add("speed=" + writeSpeed);
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
        return (String[]) cmd.toArray(new String[cmd.size()]);
    }

    protected String[] makeLoadCmd() {
        return new String[]{ executable, "dev=" + device, "-load"};
    }

    protected String[] makeEjectCmd() {
        return new String[]{ executable, "dev=" + device, "-eject"};
    }
}