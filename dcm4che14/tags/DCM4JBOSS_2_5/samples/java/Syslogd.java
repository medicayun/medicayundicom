/*$Id: Syslogd.java 3598 2003-02-14 11:24:52Z jforaci $*/
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

import org.dcm4che.server.*;
import java.util.Date;

/**
 *
 * @author  gunter
 */
public class Syslogd {
    
    private static final ServerFactory sf = ServerFactory.getInstance();
    
    private static final String USAGE =
    "java -jar syslogd.jar port [out-file]";
    
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println(USAGE);
        }
        Syslogd syslogd = new Syslogd();
        syslogd.setPort(Integer.parseInt(args[0]));
        syslogd.start();
    }

    private SyslogService service = new SyslogService() {
        public void process(Date date, String host, String content) {
            System.out.println("date:" + date + "\nhost:" + host + "\ncontent:" + content);
        }
    };

    private UDPServer server = sf.newUDPServer(
        sf.newSyslogHandler(service));
    
    public int getPort() {
        return server.getPort();
    }
    
    public void setPort(int port) {
        server.setPort(port);
    }
    
    public void start() throws Exception {
        server.start();
    }
}
