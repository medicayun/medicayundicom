/*                                                                           *
 *  Copyright (c) 2002, 2003 by TIANI MEDGRAPH AG                            *
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
import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import org.apache.log4j.Logger;
import org.dcm4che.hl7.HL7Exception;
import org.dcm4che.hl7.HL7Factory;
import org.dcm4che.hl7.HL7Message;
import org.dcm4che.hl7.HL7Service;
import org.dcm4che.server.HL7Handler;
import org.dcm4che.server.Server;
import org.dcm4che.server.ServerFactory;
import org.dcm4che.util.MLLP_Protocol;
import org.dcm4che.util.SSLContextAdapter;

/**
 * <description>
 *
 * @author     <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @created    September 7, 2002
 * @version    $Revision: 3646 $ $Date: 2003-05-10 06:12:03 +0800 (周六, 10 5月 2003) $
 */
public class Hl7Rcv implements HL7Service
{

    // Constants -----------------------------------------------------
    private final static LongOpt[] LONG_OPTS = new LongOpt[]{
            new LongOpt("max-clients", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("so-timeout", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("ack-delay", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("receiving-apps", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("sending-apps", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("mllp-tls", LongOpt.NO_ARGUMENT, null, 4),
            new LongOpt("mllp-tls.nodes", LongOpt.NO_ARGUMENT, null, 4),
            new LongOpt("mllp-tls.3des", LongOpt.NO_ARGUMENT, null, 4),
            new LongOpt("tls-key", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("tls-key-passwd", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("tls-cacerts", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("tls-cacerts-passwd", LongOpt.REQUIRED_ARGUMENT, null, 2),
            new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h'),
            new LongOpt("version", LongOpt.NO_ARGUMENT, null, 'v'),
            };

    private static ResourceBundle messages = ResourceBundle.getBundle(
            "Hl7Rcv", Locale.getDefault());

    // Variables -----------------------------------------------------
    final static Logger log = Logger.getLogger(Hl7Rcv.class);
    final static ServerFactory sf = ServerFactory.getInstance();
    final static HL7Factory hl7f = HL7Factory.getInstance();

    private final HL7Handler handler = sf.newHL7Handler();
    private final Server server = sf.newServer(handler);
    private int ackDelay = 0;

    private SSLContextAdapter tls = null;
    private MLLP_Protocol protocol = MLLP_Protocol.MLLP;

    // Constructors --------------------------------------------------
    Hl7Rcv(Configuration cfg)
    {
        initServer(cfg);
        initTLS(cfg);
    }

    private void initServer(Configuration cfg)
    {
        server.setPort(
                Integer.parseInt(cfg.getProperty("port")));
        server.setMaxClients(
                Integer.parseInt(cfg.getProperty("max-clients", "10")));
        handler.setSoTimeout(
                Integer.parseInt(cfg.getProperty("so-timeout", "0")));
        handler.setReceivingApps(cfg.tokenize(
                cfg.getProperty("receiving-apps", null, "<any>", null)));
        handler.setSendingApps(cfg.tokenize(
                cfg.getProperty("sending-apps", null, "<any>", null)));
        ackDelay = Integer.parseInt(cfg.getProperty("ack-delay", "0"));
        handler.putService("ADT", this);
        handler.putService("ORM", this);
        handler.putService("ORU", this);
        
    }


    private void initTLS(Configuration cfg)
    {
        try {
            this.protocol = MLLP_Protocol.valueOf(
                    cfg.getProperty("protocol", "mllp"));
            if (!protocol.isTLS()) {
                return;
            }

            tls = SSLContextAdapter.getInstance();
            char[] keypasswd = cfg.getProperty("tls-key-passwd", "passwd").toCharArray();
            tls.setKey(
                    tls.loadKeyStore(
                    Hl7Rcv.class.getResource(cfg.getProperty("tls-key", "identity.p12")),
                    keypasswd),
                    keypasswd);
            tls.setTrust(tls.loadKeyStore(
                    Hl7Rcv.class.getResource(cfg.getProperty("tls-cacerts", "cacerts.jks")),
                    cfg.getProperty("tls-cacerts-passwd", "passwd").toCharArray()));
            this.server.setServerSocketFactory(
                    tls.getServerSocketFactory(protocol.getCipherSuites()));
        } catch (Exception ex) {
            throw new RuntimeException("Could not initalize TLS configuration: ", ex);
        }
    }

    // Methods -------------------------------------------------------
    /**
     *  Description of the Method
     *
     * @exception  IOException  Description of the Exception
     */
    public void start()
        throws IOException
    {
        server.start();
    }


    /**
     *  Description of the Method
     *
     * @param  msg               Description of the Parameter
     * @return                   Description of the Return Value
     * @exception  HL7Exception  Description of the Exception
     */
    public byte[] execute(byte[] msg)
        throws HL7Exception
    {
        HL7Message hl7 = hl7f.parse(msg);
        if (log.isDebugEnabled()) {
            log.debug("Received:\n" + hl7.toVerboseString());
        }
        if (ackDelay > 0) {
            try {
                Thread.sleep(ackDelay);
            } catch (InterruptedException e) {}
        }
        byte[] ack = hl7.header().makeACK_AA();
        if (log.isDebugEnabled()) {
            log.debug("Send:\n" + hl7f.parse(ack).toVerboseString());
        }
        return ack;
    }


    /**
     *  Description of the Method
     *
     * @param  args           Description of the Parameter
     * @exception  Exception  Description of the Exception
     */
    public static void main(String args[])
        throws Exception
    {
        Getopt g = new Getopt("hl7rcv", args, "", LONG_OPTS);

        Configuration cfg = new Configuration(
                Hl7Rcv.class.getResource("hl7rcv.cfg"));
        int c;
        while ((c = g.getopt()) != -1) {
            switch (c) {
                case 2:
                    cfg.put(LONG_OPTS[g.getLongind()].getName(), g.getOptarg());
                    break;
                case 4:
                    cfg.put("protocol", LONG_OPTS[g.getLongind()].getName());
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
        switch (args.length - optind) {
            case 0:
                exit(messages.getString("missing"), true);
            case 1:
                cfg.put("port", args[optind]);
                break;
            default:
                exit(messages.getString("many"), true);
        }
        listConfig(cfg);
        try {
            new Hl7Rcv(cfg).start();
        } catch (IllegalArgumentException e) {
            exit(e.getMessage(), true);
        }
    }


    private static void listConfig(Configuration cfg)
    {
        StringBuffer msg = new StringBuffer();
        msg.append(messages.getString("cfg"));
        msg.append("\n\tprotocol=").append(cfg.getProperty("protocol"));
        for (int i = 0, n = LONG_OPTS.length - 2; i < n; ++i) {
            String opt = LONG_OPTS[i].getName();
            String val = cfg.getProperty(opt);
            if (val != null) {
                msg.append("\n\t").append(opt).append("=").append(val);
            }
        }
        log.info(msg.toString());
    }


    private static void exit(String prompt, boolean error)
    {
        if (prompt != null) {
            System.err.println(prompt);
        }
        if (error) {
            System.err.println(messages.getString("try"));
        }
        System.exit(1);
    }

}

