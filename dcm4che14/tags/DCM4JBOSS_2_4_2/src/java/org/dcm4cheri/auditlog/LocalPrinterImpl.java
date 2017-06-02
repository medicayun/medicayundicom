/*                                                                           *
 *  Copyright (c) 2003 by TIANI MEDGRAPH AG                                  *
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
package org.dcm4cheri.auditlog;

import org.dcm4che.auditlog.Destination;

/**
 * @author     <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @created    March 16, 2003
 * @version    $Revision: 3835 $ $Date: 2004-12-03 22:12:29 +0800 (周五, 03 12月 2004) $
 */
class LocalPrinterImpl implements Destination
{

    // Constants -----------------------------------------------------

    // Variables -----------------------------------------------------
    private String name;

    // Constructors --------------------------------------------------
    /**
     *Constructor for the LocalPrinterImpl object
     *
     * @param  name  Description of the Parameter
     */
    public LocalPrinterImpl(String name)
    {
        this.name = name;
    }


    /**
     *  Description of the Method
     *
     * @param  sb  Description of the Parameter
     */
    public void writeTo(StringBuffer sb)
    {
        sb.append("<Destination><LocalPrinter><![CDATA[")
                .append(name)
                .append("]]></LocalPrinter></Destination>");
    }

}

