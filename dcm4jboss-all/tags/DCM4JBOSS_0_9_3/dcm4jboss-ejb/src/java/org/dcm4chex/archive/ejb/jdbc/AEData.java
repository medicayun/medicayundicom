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

package org.dcm4chex.archive.ejb.jdbc;

import java.io.Serializable;
import java.util.StringTokenizer;

/**
 * <description>
 *
 * @see <related>
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @version $Revision: 1026 $ $Date: 2004-03-04 02:22:28 +0800 (周四, 04 3月 2004) $
 * @since July 23, 2002
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>yyyymmdd author:</b>
 * <ul>
 * <li> explicit fix description (no line numbers but methods) go
 *            beyond the cvs commit message
 * </ul>
 */
public class AEData implements Serializable {

    // Constants -----------------------------------------------------
    static final long serialVersionUID = 9128665077590256461L;
    static final String[] EMPTY_STRING_ARRAY = {
    };

    // Variables -----------------------------------------------------
    private final int pk;
    private final String title;
    private final String hostname;
    private final int port;
    private final String cipherSuites;

    // Constructors --------------------------------------------------
    public AEData(
        int pk,
        String title,
        String hostname,
        int port,
        String cipherSuites) {
        this.pk = pk;
        this.title = title;
        this.hostname = hostname;
        this.port = port;
        this.cipherSuites = cipherSuites;
    }

    /**
     * @return pk
     */
    public final int getPk() {
        return pk;
    }

    /** Getter for property title.
     * @return Value of property title.
     */
    public java.lang.String getTitle() {
        return title;
    }

    /** Getter for property host.
     * @return Value of property host.
     */
    public java.lang.String getHostName() {
        return hostname;
    }

    /** Getter for property port.
     * @return Value of property port.
     */
    public int getPort() {
        return port;
    }

    /** Getter for property cipherSuites.
     * @return Value of property cipherSuites.
     */
    public java.lang.String[] getCipherSuites() {
        if (cipherSuites == null || cipherSuites.length() == 0) {
            return EMPTY_STRING_ARRAY;
        }
        StringTokenizer stk = new StringTokenizer(cipherSuites, " ,");
        String[] retval = new String[stk.countTokens()];
        for (int i = 0; i < retval.length; ++i) {
            retval[i] = stk.nextToken();
        }
        return retval;
    }

    /** Getter for property cipherSuites.
     * @return Value of property cipherSuites.
     */
    public java.lang.String getCipherSuitesAsString() {
        return cipherSuites;
    }

    public boolean isTLS() {
        return cipherSuites != null && cipherSuites.length() != 0;
    }

    public String toString() {
        return (isTLS() ? "dicom-tls://" : "dicom://")
            + title
            + '@'
            + hostname
            + ':'
            + port;
    }
}
