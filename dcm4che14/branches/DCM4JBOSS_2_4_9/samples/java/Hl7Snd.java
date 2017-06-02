/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2002 by TIANI MEDGRAPH AG                                  *
 *                                                                           *
 *  This file is part of dcm4che.                                            *
 *                                                                           *
 *  This library is free software; you can redistribute it and/or modify it  *
 *  under the terms of the GNU Lesser General Public License as published    *
 *  by the Free Software Foundation; either version 2 of the License, or     *
 *  (at your option) any later version.                                      *
 *                                                                           *
 *  This library is distributed in the hope that it will be useful, but      *
 *  WITHOUT ANY WARRANTY; without even the implied warranty of               *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        *
 *  Lesser General Public License for more details.                          *
 *                                                                           *
 *  You should have received a copy of the GNU Lesser General Public         *
 *  License along with this library; if not, write to the Free Software      *
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA  *
 *                                                                           *
 *****************************************************************************/

import org.apache.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.text.MessageFormat;
import java.util.Locale;

import org.dcm4che.hl7.HL7Exception;
import org.dcm4che.hl7.HL7Factory;
import org.dcm4che.hl7.HL7Message;
import org.dcm4che.util.MLLP_URL;
import org.dcm4che.util.MLLPInputStream;
import org.dcm4che.util.MLLPOutputStream;
import org.dcm4che.util.SSLContextAdapter;

import java.util.ResourceBundle;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

/**
 * <description>
 *
 * @see <related>
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @version $Revision: 3901 $ $Date: 2005-07-12 21:51:32 +0800 (周二, 12 7月 2005) $
 * @since August 22, 2002
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>yyyymmdd author:</b>
 * <ul>
 * <li> explicit fix description (no line numbers but methods) go
 *            beyond the cvs commit message
 * </ul>
 */
public class Hl7Snd {
    
    // Constants -----------------------------------------------------
    private static final LongOpt[] LONG_OPTS = new LongOpt[] {
        new LongOpt("ack-timeout", LongOpt.REQUIRED_ARGUMENT, null, 2),
        new LongOpt("tls-key", LongOpt.REQUIRED_ARGUMENT, null, 2),
        new LongOpt("tls-key-passwd", LongOpt.REQUIRED_ARGUMENT, null, 2),
        new LongOpt("tls-cacerts", LongOpt.REQUIRED_ARGUMENT, null, 2),
        new LongOpt("tls-cacerts-passwd", LongOpt.REQUIRED_ARGUMENT, null, 2),
        new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h'),
        new LongOpt("version", LongOpt.NO_ARGUMENT, null, 'v'),
    };
    
    // Variables -----------------------------------------------------
    private static final Logger log = Logger.getLogger(Hl7Snd.class);
    private static final HL7Factory hl7Fact = HL7Factory.getInstance();
    private static ResourceBundle messages = 
        ResourceBundle.getBundle("Hl7Snd", Locale.getDefault());
    
    private MLLP_URL url;
    private int ackTimeout = 0;
    private SSLContextAdapter tls = null;
    private String[] cipherSuites = null;
    
    public static void main(String args[]) throws Exception {
        Getopt g = new Getopt("hl7snd", args, "", LONG_OPTS);
        
        Configuration cfg = new Configuration(
            Hl7Snd.class.getResource("hl7snd.cfg"));
        int c;
        while ((c = g.getopt()) != -1) {
            switch (c) {
                case 2:
                    cfg.put(LONG_OPTS[g.getLongind()].getName(), g.getOptarg());
                    break;
                case 'v':
                    exit(messages.getString("version"), false);
                case 'h':
                    exit(messages.getString("usage"), false);
                case '?':
                    exit(null, true);
                    break;
            }
        }
        int optind = g.getOptind();
        int argc = args.length - optind;
        if (argc < 2) {
            exit(messages.getString("missing"), true);
        }
        //      listConfig(cfg);
        try {
            Hl7Snd hl7snd = new Hl7Snd(cfg, new MLLP_URL(args[optind]), argc);
            hl7snd.execute(args, optind+1);
        } catch (IllegalArgumentException e) {
            exit(e.getMessage(), true);
        }
    }
    
    private static void exit(String prompt, boolean error) {
        if (prompt != null)
            System.err.println(prompt);
        if (error)
            System.err.println(messages.getString("try"));
        System.exit(1);
    }

    // Constructors --------------------------------------------------
    Hl7Snd(Configuration cfg, MLLP_URL url, int argc) {
        this.url = url;
        this.ackTimeout = Integer.parseInt(cfg.getProperty("ack-timeout", "0"));
        initTLS(cfg);
    }
    
    // Methods -------------------------------------------------------
    public void execute(String[] args, int offset) {
        long t1 = System.currentTimeMillis();
        int count = 0;
        Socket s = null;
        MLLPInputStream in = null;
        MLLPOutputStream out = null;
        try {
            s = newSocket(url.getHost(), url.getPort());
            s.setSoTimeout(ackTimeout);
            in = new MLLPInputStream(
                new BufferedInputStream(s.getInputStream()));
            out = new MLLPOutputStream(
                new BufferedOutputStream(s.getOutputStream()));
            for (int i = offset; i < args.length; ++i) {
                count += send(new File(args[i]), in, out);
            }
        } catch (Exception e) {
            log.error("Could not send all messages: ", e);
        } finally {
            if (out != null) {
                try { out.close(); } catch (IOException ignore) {}
            }
            if (in != null) {
                try { in.close(); } catch (IOException ignore) {}
            }
            if (s != null) {
                try { s.close(); } catch (IOException ignore) {}
            }
        }
        long dt = System.currentTimeMillis() - t1;
        log.info(
            MessageFormat.format(messages.getString("sendDone"),
            new Object[]{
                new Integer(count),
                new Long(dt),
            }));
    }
    
    private int send(File file, MLLPInputStream in, MLLPOutputStream out)
    throws IOException {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            int count = 0;
            for (int i = 0; i < files.length; ++i) {
                count += send(files[i], in, out);
            }
            return count;
        }
        FileInputStream fin = null;
        byte[] msg;
        try {
            fin = new FileInputStream(file);
            msg = new byte[(int) file.length()];
            fin.read(msg);
        } catch (IOException e) {
            log.warn("Could not read " + file);
            return 0;
        } finally {
            if (fin != null) {
                try { fin.close(); } catch (IOException ignore) {}
            }
        }
        if (log.isInfoEnabled()) {
            try {
                HL7Message hl7 = hl7Fact.parse(msg);
                log.info("Send: " + hl7);
                if (log.isDebugEnabled()) {
                    log.debug(hl7.toVerboseString());
                }
            } catch (HL7Exception e) {
                log.warn("Could not parse HL7 message load from " + file, e);
            }
        }
        out.writeMessage(msg);
        out.flush();
        msg = in.readMessage();
        if (log.isInfoEnabled()) {
            try {
                HL7Message hl7 = hl7Fact.parse(msg);
                log.info("Received: " + hl7);
                if (log.isDebugEnabled()) {
                    log.debug(hl7.toVerboseString());
                }
            } catch (HL7Exception e) {
                log.warn("Could not parse HL7 message received from " + url, e);
            }
        }
        return 1;
    }

    private Socket newSocket(String host, int port)
    throws IOException, GeneralSecurityException {
        if (cipherSuites != null) {
            return tls.getSocketFactory(cipherSuites).createSocket(host, port);
        } else {
            return new Socket(host, port);
        }
    }

    private void initTLS(Configuration cfg) {
        try {
            cipherSuites = url.getCipherSuites();
            if (cipherSuites == null) {
                return;
            }
            tls = SSLContextAdapter.getInstance();
            char[] keypasswd = 
                cfg.getProperty("tls-key-passwd","secret").toCharArray();
            tls.setKey(
                tls.loadKeyStore(
                    Hl7Snd.class.getResource(
                        cfg.getProperty("tls-key","identity.p12")),
                    keypasswd),
                keypasswd);
            tls.setTrust(tls.loadKeyStore(
                Hl7Snd.class.getResource(
                    cfg.getProperty("tls-cacerts", "cacerts.jks")),
                cfg.getProperty("tls-cacerts-passwd", "secret").toCharArray()));
            tls.init();
        } catch (Exception ex) {
           throw new RuntimeException("Could not initalize TLS configuration: ", ex);
        }
    }
}
