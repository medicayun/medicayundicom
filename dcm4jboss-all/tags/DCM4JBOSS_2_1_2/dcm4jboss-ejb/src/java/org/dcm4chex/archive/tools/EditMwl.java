/* $Id: EditMwl.java 1489 2005-02-04 17:46:16Z gunterze $
 * Copyright (c) 2002,2003 by TIANI MEDGRAPH AG
 *
 * This file is part of dcm4che.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.dcm4chex.archive.tools;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.ResourceBundle;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.dcm4che.data.Dataset;
import org.dcm4chex.archive.common.DatasetUtils;
import org.dcm4chex.archive.ejb.interfaces.MWLManager;
import org.dcm4chex.archive.ejb.interfaces.MWLManagerHome;
import org.xml.sax.SAXException;

import gnu.getopt.LongOpt;
import gnu.getopt.Getopt;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1489 $ $Date: 2005-02-05 01:46:16 +0800 (周六, 05 2月 2005) $
 * @since 17.02.2004
 */
public final class EditMwl {

    private static ResourceBundle messages =
        ResourceBundle.getBundle(EditMwl.class.getName());

    private final static LongOpt[] LONG_OPTS =
        new LongOpt[] {
            new LongOpt("url", LongOpt.REQUIRED_ARGUMENT, null, 'u'),
            new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h'),
            new LongOpt("version", LongOpt.NO_ARGUMENT, null, 'v'),
            };

    private String ejbProviderURL = "jnp://localhost:1099";

    public static void main(String[] args) {
         Getopt g = new Getopt("mwlitem.jar", args, "af:r:u:hv", LONG_OPTS);

        EditMwl mwl = new EditMwl();
        int c;
        int cmd = 0;
        String spsId = null;
        String fpath = null;
        while ((c = g.getopt()) != -1) {
            switch (c) {
                case 'a' :
                    cmd = c;
                    break;
                case 'r' :
                    cmd = c;
                    spsId = g.getOptarg();
                    break;
                case 'f' :
                    fpath = g.getOptarg();
                    break;
                case 'u' :
                    mwl.ejbProviderURL = g.getOptarg();
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
                    spsId = mwl.add(loadMWLItem(fpath));
                    System.out.println(spsId);
                    break;
                case 'r' :
                    mwl.remove(spsId);
                    }
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * @param dataset
     * @return
     */
    private String add(Dataset ds) throws Exception {
        MWLManager mm = getMWLManager();
        try {
            return mm.addWorklistItem(ds);
        } finally {
            try {
                mm.remove();
            } catch (Exception ignore) {
            }
        }
    }

    /**
     * @param arg
     */
    private Dataset remove(String spsid) throws Exception {
        MWLManager mm = getMWLManager();
        try {
            return mm.removeWorklistItem(spsid);
        } finally {
            try {
                mm.remove();
            } catch (Exception ignore) {
            }
        }
    }

    private static Dataset loadMWLItem(String fpath) throws SAXException, IOException {
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

    private MWLManager getMWLManager() throws Exception {
	    Context ctx = new InitialContext(makeEnv());
	    MWLManagerHome home = (MWLManagerHome) ctx.lookup(MWLManagerHome.JNDI_NAME);
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