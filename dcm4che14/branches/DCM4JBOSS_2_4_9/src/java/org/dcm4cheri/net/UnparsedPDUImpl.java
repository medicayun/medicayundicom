/*$Id: UnparsedPDUImpl.java 3886 2005-04-07 11:42:58Z javawilli $*/
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

package org.dcm4cheri.net;

import java.io.*;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
final class UnparsedPDUImpl {
    
    static final long MAX_LENGTH = 1048576L; // 1 MB
    private final byte[] buf;
    private final int type;
    private final int len;
    
    /** Creates a new instance of RawPDU */
    public UnparsedPDUImpl(InputStream in, byte[] buf) throws IOException {
        if (buf == null || buf.length < 6) {
            buf = new byte[10];
        }
        readFully(in, buf, 0, 6);
        this.type = buf[0] & 0xFF;
        this.len = ((buf[2] & 0xff) << 24)
                | ((buf[3] & 0xff) << 16)
                | ((buf[4] & 0xff) << 8)
                | ((buf[5] & 0xff) << 0);
        if ((len & 0xFFFFFFFF) > MAX_LENGTH) {
            skipFully(in, len & 0xFFFFFFFFL);
            this.buf = null;
            return;
        }
        if (buf.length < 6 + len) {
            this.buf = new byte[6 + len];
            System.arraycopy(buf, 0, this.buf, 0, 6);
        } else {
            this.buf = buf;
        }
        readFully(in, this.buf, 6, len);
    }

    public final int type() {
        return type;
    }

    public final int length() {
        return len;
    }

    public final byte[] buffer() {
        return buf;
    }
    
    public String toString() {
        return "PDU[type=" + type
                + ", length=" + (len & 0xFFFFFFFFL)
                + "]";
    }

    static void skipFully(InputStream in, long len)
            throws IOException {
	long n = 0;
	while (n < len) {
	    long count = in.skip(len - n);
	    if (count <= 0)
	    	throw new EOFException();
	    n += count;
	}
    }

    static void readFully(InputStream in, byte b[], int off, int len)
            throws IOException {
	int n = 0;
	while (n < len) {
	    int count = in.read(b, n + off, len - n);
	    if (count < 0)
		throw new EOFException();
	    n += count;
	}
    }
}
