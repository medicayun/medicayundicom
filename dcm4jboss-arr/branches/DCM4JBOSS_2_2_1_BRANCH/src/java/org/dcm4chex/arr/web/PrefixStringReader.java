/*
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
package org.dcm4chex.arr.web;

import java.io.FilterReader;
import java.io.IOException;
import java.io.StringReader;

/**
 *  <description>
 *
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @author  <a href="mailto:joseph@tiani.com">joseph foraci</a>
 * @created  February 16, 2003
 * @version  $Revision: 4364 $
 */
class PrefixStringReader extends FilterReader
{

    // Constants -----------------------------------------------------

    // Attributes ----------------------------------------------------
    private final char[] prefix;
    private int pos;


    // Constructors --------------------------------------------------
    /**
     *  Constructor for the PrefixStringReader object
     *
     * @param  prefix Description of the Parameter
     * @param  data Description of the Parameter
     */
    public PrefixStringReader(char[] prefix, String data)
    {
        super(new StringReader(data));
        this.prefix = prefix;
        pos = 0;
    }


    // Methods -------------------------------------------------------
    /**
     *  Description of the Method
     *
     * @return  Description of the Return Value
     * @exception  IOException Description of the Exception
     */
    public int read()
        throws IOException
    {
        if (pos >= prefix.length) {
            return in.read();
        }
        return (int) prefix[pos++];
    }


    /**
     *  Description of the Method
     *
     * @param  cbuf Description of the Parameter
     * @param  off Description of the Parameter
     * @param  len Description of the Parameter
     * @return  Description of the Return Value
     * @exception  IOException Description of the Exception
     */
    public int read(char[] cbuf, int off, int len)
        throws IOException
    {
        if (pos >= prefix.length) {
            return in.read(cbuf, off, len);
        }
        final int copyLen = Math.min(prefix.length - pos, len);
        System.arraycopy(prefix, pos, cbuf, off, copyLen);
        pos += copyLen;
        return copyLen;
    }
}

