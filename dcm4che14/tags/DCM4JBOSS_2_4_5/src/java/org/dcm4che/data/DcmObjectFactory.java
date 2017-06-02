/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4che.data;

import org.dcm4che.Implementation;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version Revision $Date: 2005-03-01 20:56:59 +0800 (周二, 01 3月 2005) $
 */
public abstract class DcmObjectFactory {

    private static DcmObjectFactory instance = (DcmObjectFactory)
            Implementation.findFactory("dcm4che.data.DcmObjectFactory");

    public static DcmObjectFactory getInstance() {
        return instance;
    }

    public abstract Command newCommand();

    public abstract Dataset newDataset();

    public abstract FileMetaInfo newFileMetaInfo();

    public abstract FileMetaInfo newFileMetaInfo(String sopClassUID,
            String sopInstanceUID, String transferSyntaxUID);

    public abstract FileMetaInfo newFileMetaInfo(Dataset ds,
            String transferSyntaxUID) throws DcmValueException;

    public abstract PersonName newPersonName(String s);

}
