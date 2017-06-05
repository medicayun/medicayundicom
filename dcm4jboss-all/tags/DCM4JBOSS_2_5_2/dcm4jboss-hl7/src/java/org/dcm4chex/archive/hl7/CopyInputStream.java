/**
 * 
 */
package org.dcm4chex.archive.hl7;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class CopyInputStream extends FilterInputStream {

	private OutputStream out;

	protected CopyInputStream(InputStream in, OutputStream out) {
		super(in);
		this.out = out;
	}
	
	public int read() throws IOException {
		int b = in.read();
		if (b != -1)
			out.write(b);
		return b;
	}

	public int read(byte[] b, int off, int len) throws IOException {
		int l = in.read(b, off, len);
		if (l != -1)
			out.write(b, off, l);
		return l;
	}
	
}