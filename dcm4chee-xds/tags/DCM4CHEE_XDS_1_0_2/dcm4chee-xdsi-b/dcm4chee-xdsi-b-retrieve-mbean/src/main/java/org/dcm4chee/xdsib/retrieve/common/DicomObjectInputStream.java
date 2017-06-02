package org.dcm4chee.xdsib.retrieve.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import org.dcm4che2.data.VR;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.io.DicomOutputStream;
import org.dcm4che2.io.StopTagInputHandler;

public class DicomObjectInputStream extends InputStream {

    private static final int BUF_SIZE = 8192;
    private InputStream[] cascade;
    private int idx = 0;
    boolean eof;
    
    public DicomObjectInputStream(DicomObject o, DicomInputStream dis) throws IOException {
        if (dis == null && o == null)
            throw new IllegalArgumentException("Either DicomObject header or InputStream must NOT be null!");
        DicomObject header;
        if ( dis != null && o != null) {
            dis.setHandler(new StopTagInputHandler(Tag.PixelData));
            header = dis.readDicomObject();
            if (o != null) 
                o.copyTo(header);
            cascade = new InputStream[]{getInputStreamForDicomObject(header, dis), dis};
        } else if ( dis == null ) {
            checkTS(o);
            cascade = new InputStream[]{getInputStreamForDicomObject(o, null)};
        } else {
            cascade = new InputStream[]{dis};
        }
    }

    private InputStream getInputStreamForDicomObject(DicomObject header, DicomInputStream dis)
            throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(BUF_SIZE);
        DicomOutputStream dos = new DicomOutputStream(baos);
        dos.writeDicomFile(header);
        if ( dis != null && dis.available() > 0) {
            dos.writeHeader(dis.tag(), dis.vr(), dis.valueLength());
        }
        return new ByteArrayInputStream(baos.toByteArray());
    }

    private void checkTS(DicomObject header) {
        String tsuid = header.getString(Tag.TransferSyntaxUID);
        if ( tsuid == null ) {
            if ( header.containsValue(Tag.PixelData) )
                throw new IllegalArgumentException("Missing (0002,0010) Transfer Syntax UID");
            header.putString(Tag.TransferSyntaxUID, VR.UI, UID.ExplicitVRLittleEndian);            
        }
    }

    @Override
    public int read() throws IOException {
        if (eof) return -1;
        int b = cascade[idx].read();
        if ( b != -1 ) return b;
        idx++;
        if ( idx == cascade.length ) {
            eof = true;
        }
        return read();
    }
    
    @Override
    public int available() throws IOException {
        int avail = 0;
        for ( InputStream is : cascade ) {
            avail += is.available();
        }
        return avail;
    }
    
    
    @Override
    public void close() throws IOException {
        for ( InputStream is : cascade ) {
            is.close();
        }
    }    
}
