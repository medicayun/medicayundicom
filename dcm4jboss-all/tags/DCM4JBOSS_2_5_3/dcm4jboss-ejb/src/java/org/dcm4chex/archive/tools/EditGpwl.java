/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.tools;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.ResourceBundle;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.dcm4che.data.Dataset;
import org.dcm4chex.archive.common.DatasetUtils;
import org.dcm4chex.archive.ejb.interfaces.GPWLManager;
import org.dcm4chex.archive.ejb.interfaces.GPWLManagerHome;
import org.xml.sax.SAXException;

/**
 * @author gunter.zeilinger@tiani.com
 * @version Revision $Date: 2005-03-29 07:37:20 +0800 (周二, 29 3月 2005) $
 * @since 28.03.2005
 */

public class EditGpwl {

    private static ResourceBundle messages =
        ResourceBundle.getBundle(EditGpwl.class.getName());

    private final static LongOpt[] LONG_OPTS =
        new LongOpt[] {
            new LongOpt("url", LongOpt.REQUIRED_ARGUMENT, null, 'u'),
            new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h'),
            new LongOpt("version", LongOpt.NO_ARGUMENT, null, 'v'),
            };

    private String ejbProviderURL = "jnp://localhost:1099";

    public static void main(String[] args) {
         Getopt g = new Getopt("gpwlitem.jar", args, "af:r:u:hv", LONG_OPTS);

        EditGpwl gpwl = new EditGpwl();
        int c;
        int cmd = 0;
        String iuid = null;
        String fpath = null;
        while ((c = g.getopt()) != -1) {
            switch (c) {
                case 'a' :
                    cmd = c;
                    break;
                case 'r' :
                    cmd = c;
                    iuid = g.getOptarg();
                    break;
                case 'f' :
                    fpath = g.getOptarg();
                    break;
                case 'u' :
                    gpwl.ejbProviderURL = g.getOptarg();
                    break;
                case 'v' :
                    exit(messages.getString("version"), false);
                case 'h' :
                    exit(messages.getString("usage"), false);
                case '?' :
                    exit(null, true);
                    break;
            }
        }
        if (cmd == 0) {
            exit(messages.getString("missing"), true);
        }
        try {
            switch (cmd) {
                case 'a' :
                    iuid = gpwl.add(loadGPWLItem(fpath));
                    System.out.println(iuid);
                    break;
                case 'r' :
                    gpwl.remove(iuid);
                    }
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    private String add(Dataset ds) throws Exception {
        GPWLManager mgr = getGPWLManager();
        try {
            return mgr.addWorklistItem(ds);
        } finally {
            try {
                mgr.remove();
            } catch (Exception ignore) {
            }
        }
    }

    private Dataset remove(String uid) throws Exception {
        GPWLManager mgr = getGPWLManager();
        try {
            return mgr.removeWorklistItem(uid);
        } finally {
            try {
                mgr.remove();
            } catch (Exception ignore) {
            }
        }
    }

    private static Dataset loadGPWLItem(String fpath) throws SAXException, IOException {
        InputStream is = fpath == null ? System.in : new FileInputStream(fpath);
        try {
            return DatasetUtils.fromXML(is);            
        } finally {
            if (fpath != null) {
                try {
                    is.close();
                } catch (IOException ignore) {
                }
            }
        }
    }

    private Hashtable makeEnv() {
        Hashtable env = new Hashtable();
        env.put(
                "java.naming.factory.initial",
        "org.jnp.interfaces.NamingContextFactory");
        env.put(
                "java.naming.factory.url.pkgs",
        "org.jboss.naming:org.jnp.interfaces");
        env.put("java.naming.provider.url", ejbProviderURL);
        return env;
    }

    private GPWLManager getGPWLManager() throws Exception {
        Context ctx = new InitialContext(makeEnv());
        GPWLManagerHome home = (GPWLManagerHome) ctx.lookup(GPWLManagerHome.JNDI_NAME);
        ctx.close();
        return home.create();
    }
    
    private static void exit(String prompt, boolean error) {
        if (prompt != null) {
            System.err.println(prompt);
        }
        if (error) {
            System.err.println(messages.getString("usage"));
        }
        System.exit(1);
    }
}
