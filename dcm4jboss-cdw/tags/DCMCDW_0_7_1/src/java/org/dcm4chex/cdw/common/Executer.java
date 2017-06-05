/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.cdw.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;
import org.dcm4cheri.util.StringUtils;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 4146 $ $Date: 2004-07-12 06:29:23 +0800 (周一, 12 7月 2004) $
 * @since 08.07.2004
 */
public class Executer {

    static final Logger log = Logger.getLogger(Executer.class);

    private final String cmd;

    private final Process child;
    
    private final Thread stdoutReader;

    private final Thread stderrReader;
    
    public Executer(String[] cmdarray) throws IOException {
        this(cmdarray, null, null);
    }
    
    public Executer(String[] cmdarray, OutputStream stdout, OutputStream stderr) throws IOException {
        this.cmd = StringUtils.toString(cmdarray, ' ');
        if (log.isDebugEnabled())
            log.debug("invoke: " + cmd);
        this.child = Runtime.getRuntime().exec(cmdarray);
        this.stdoutReader = startCopy(child.getInputStream(), stdout);
        this.stderrReader = startCopy(child.getErrorStream(), stderr);
    }

    public final String cmd() {
        return cmd;
    }

    public int waitFor() throws InterruptedException {
        stdoutReader.join();
        stderrReader.join();
        int exit = child.waitFor();
        if (log.isDebugEnabled())
            log.debug("exit(" + exit + "): " + cmd);
        return exit;
    }

    private Thread startCopy(final InputStream in, final OutputStream out) {
        Thread t = new Thread(new Runnable() {

            public void run() {
                try {
                    int len;
                    byte[] buf = new byte[512];
                    while ((len = in.read(buf)) != -1)
                        if (out != null) out.write(buf, 0, len);
                } catch (IOException e) {
                    log.warn("i/o error reading stdout/stderr of " + cmd, e);
                }
            }
        });
        t.start();
        return t;
    }
}
