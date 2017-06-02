package org.dcm4che.client;

import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.ImageOutputStreamImpl;

/**
 * @author jforaci
 */
public class DataSourceImageOutputStream
	extends ImageOutputStreamImpl
	implements ImageOutputStream
{
    OutputStream out;

	public DataSourceImageOutputStream(Object output)
    {
		super();
        if (!(output instanceof OutputStream))
            throw new UnsupportedOperationException(
                "Only java.io.OutputStream is supported");
        out = (OutputStream)output;
	}

	public void write(int b) throws IOException
    {
        out.write(b);
	}

	public void write(byte[] b, int off, int len) throws IOException
    {
        out.write(b, off, len);
	}

	public int read() throws IOException
    {
		throw new UnsupportedOperationException(
            "reading from an outputstream is not supported");
	}

	public int read(byte[] b, int off, int len) throws IOException
    {
        throw new UnsupportedOperationException(
            "reading from an outputstream is not supported");
	}
}
