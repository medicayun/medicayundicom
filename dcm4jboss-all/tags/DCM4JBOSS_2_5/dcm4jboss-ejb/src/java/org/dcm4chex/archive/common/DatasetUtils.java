/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmDecodeParam;
import org.dcm4che.data.DcmEncodeParam;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;
import org.dcm4cheri.util.StringUtils;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1664 $ $Date: 2005-04-02 22:55:27 +0800 (周六, 02 4月 2005) $
 * @since 04.02.2005
 *
 */

public class DatasetUtils {

	public static void putRetrieveAET(Dataset ds, String iAETs, String eAET) {
		if (iAETs != null) {
	        ds.putAE(Tags.RetrieveAET, 
	        		StringUtils.split(
	        				eAET != null ? iAETs + '\\' + eAET : iAETs, '\\'));
		} else {
			ds.putAE(Tags.RetrieveAET, eAET);
		}
	}

    private static final DcmObjectFactory dof = DcmObjectFactory.getInstance();

    public static Dataset fromByteArray(
        byte[] data,
        DcmDecodeParam decodeParam, Dataset ds) {
        ByteArrayInputStream bin = new ByteArrayInputStream(data);
        if (ds == null)
            ds = dof.newDataset();
        try {
            ds.readDataset(bin, decodeParam, -1);
        } catch (IOException e) {
            throw new IllegalArgumentException("" + e);
        }
        return ds;
    }

    public static byte[] toByteArray(Dataset ds, DcmEncodeParam encodeParam) {
        ByteArrayOutputStream bos =
            new ByteArrayOutputStream(ds.calcLength(encodeParam));
        try {
            ds.writeDataset(bos, encodeParam);
        } catch (IOException e) {
            throw new IllegalArgumentException("" + e);
        }
        return bos.toByteArray();
    }

    private static SAXParser getSAXParser() {
        try {
            return SAXParserFactory.newInstance().newSAXParser();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Dataset fromXML(InputSource is)
        throws SAXException, IOException {
        Dataset ds = dof.newDataset();
        getSAXParser().parse(is, ds.getSAXHandler2(null));
        return ds;
    }

    public static Dataset fromXML(InputStream is)
        throws SAXException, IOException {
        return fromXML(new InputSource(is));
    }

    public static Dataset fromXML(Reader r) throws SAXException, IOException {
        return fromXML(new InputSource(r));
    }

    public static Dataset fromXML(String s) throws SAXException, IOException {
        return fromXML(new StringReader(s));
    }

    private DatasetUtils() {} // no instance
}
