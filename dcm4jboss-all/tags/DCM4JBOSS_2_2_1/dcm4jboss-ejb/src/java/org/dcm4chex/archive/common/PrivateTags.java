/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.common;


/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1559 $ $Date: 2005-02-25 23:20:31 +0800 (周五, 25 2月 2005) $
 * @since 06.10.2004
 *
 */
public class PrivateTags {
    public static final String CreatorID = "dcm4che/archive";
    public static final int PatientPk = 0x00430010;
    public static final int StudyPk = 0x00430011;
    public static final int SeriesPk = 0x00430012;
    public static final int InstancePk = 0x00430013;
    public static final int CallingAET = 0x00430014;
    public static final int CalledAET = 0x00430015;
}
