package org.dcm4chee.xdsib.retrieve.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.io.StopTagInputHandler;

public class DicomObjectDataSource implements DataSource {

    private DicomObject header;
    private DicomInputStream dis;
    
    public DicomObjectDataSource(DicomObject o) throws IOException {
        this(o, null);
    }
    public DicomObjectDataSource(DicomObject o, DicomInputStream dis ) throws IOException {
        header = o;
        this.dis = dis;
    }

    public String getContentType() {
        return "application/dicom";
    }

    public InputStream getInputStream() throws IOException {
        return new DicomObjectInputStream(header, dis);
    }

    public String getName() {
        return header == null ? "DICOM object" : header.getString(Tag.SOPInstanceUID);
    }

    public OutputStream getOutputStream() throws IOException {
        return null;
    }

}
