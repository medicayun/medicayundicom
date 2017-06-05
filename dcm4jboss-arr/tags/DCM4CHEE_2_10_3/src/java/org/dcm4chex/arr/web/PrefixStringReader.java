/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), available at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * TIANI Medgraph AG.
 * Portions created by the Initial Developer are Copyright (C) 2003-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunter.zeilinger@tiani.com>
 * Joe Foraci <jforaci@users.sourceforge.net>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

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
 * @version  $Revision: 4422 $
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

