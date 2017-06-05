/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.hl7;

import org.dom4j.Document;
import org.xml.sax.ContentHandler;


/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1357 $ $Date: 2004-12-30 08:55:56 +0800 (周四, 30 12月 2004) $
 * @since 27.10.2004
 *
 */
public interface HL7Service {

    boolean process(MSH msh, Document msg, ContentHandler hl7out) throws HL7Exception;

}
