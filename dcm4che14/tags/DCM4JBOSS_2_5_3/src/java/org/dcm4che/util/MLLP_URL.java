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
 */
package org.dcm4che.util;

/**
 * @author     <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @since    May, 2002
 * @version    $Revision: 3616 $ $Date: 2003-03-17 07:38:03 +0800 (周一, 17 3月 2003) $
 */
public final class MLLP_URL extends Object
{
    // Constants -----------------------------------------------------

    // Attributes ----------------------------------------------------
    private MLLP_Protocol protocol;
    private String host;
    private int port = 0;

    // Constructors --------------------------------------------------
    /**
     *Constructor for the MLLP_URL object
     *
     * @param  spec  Description of the Parameter
     */
    public MLLP_URL(String spec)
    {
        parse(spec.trim());
    }


    /**
     *Constructor for the MLLP_URL object
     *
     * @param  protocol  Description of the Parameter
     * @param  host      Description of the Parameter
     * @param  port      Description of the Parameter
     */
    public MLLP_URL(String protocol, String host, int port)
    {
        this.protocol = MLLP_Protocol.valueOf(protocol);
        this.host = host;
        this.port = port;
    }

    // Public --------------------------------------------------------
    /**
     *  Gets the protocol attribute of the MLLP_URL object
     *
     * @return    The protocol value
     */
    public final String getProtocol()
    {
        return protocol.toString();
    }


    /**
     *  Gets the tLS attribute of the MLLP_URL object
     *
     * @return    The tLS value
     */
    public final boolean isTLS()
    {
        return protocol.isTLS();
    }


    /**
     *  Gets the cipherSuites attribute of the MLLP_URL object
     *
     * @return    The cipherSuites value
     */
    public final String[] getCipherSuites()
    {
        return protocol.getCipherSuites();
    }


    /**
     *  Gets the host attribute of the MLLP_URL object
     *
     * @return    The host value
     */
    public final String getHost()
    {
        return host;
    }


    /**
     *  Gets the port attribute of the MLLP_URL object
     *
     * @return    The port value
     */
    public final int getPort()
    {
        return port;
    }


    /**
     *  Description of the Method
     *
     * @return    Description of the Return Value
     */
    public String toString()
    {
        return protocol.toString() + "://" + host + ':' + port;
    }

    // Private -------------------------------------------------------

    private void parse(String s)
    {
        int delimPos1 = s.indexOf("://");
        if (delimPos1 == -1) {
            throw new IllegalArgumentException(s);
        }
        int delimPos2 = s.indexOf(':', delimPos1 + 3);
        if (delimPos2 == -1) {
            throw new IllegalArgumentException(s);
        }
        protocol = MLLP_Protocol.valueOf(s.substring(0, delimPos1));
        host = s.substring(delimPos1 + 3, delimPos2);
        port = Integer.parseInt(s.substring(delimPos2 + 1));
    }
}

