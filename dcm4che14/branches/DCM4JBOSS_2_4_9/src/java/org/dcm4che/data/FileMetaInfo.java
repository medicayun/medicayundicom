/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4che.data;

import org.dcm4che.dict.TagDictionary;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import javax.imageio.stream.ImageOutputStream;
import org.xml.sax.ContentHandler;

/** Defines behavior of <code>DataSet</code> container objects.
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 * @see "DICOM Part 10: Media Storage and File Format for Media Interchange,
 * 7.1 File Meta Information"
 */
public interface FileMetaInfo extends DcmObject {

    public byte[] getPreamble();

    public String getMediaStorageSOPClassUID();

    public String getMediaStorageSOPInstanceUID();

    public String getTransferSyntaxUID();

    public String getImplementationClassUID();

    public String getImplementationVersionName();

    public void write(DcmHandler handler) throws IOException;

    public void write(OutputStream out) throws IOException;

    public void write(ImageOutputStream out) throws IOException;

    public void write(ContentHandler ch, TagDictionary dict) throws IOException;

    public void write2(ContentHandler ch, TagDictionary dict,
            int[] excludeTags, int excludeValueLengthLimit, File basedir)
            throws IOException;

    public void read(InputStream in) throws IOException;
}