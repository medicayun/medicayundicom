/*                                                                           *
 *  Copyright (c) 2002, 2003 by TIANI MEDGRAPH AG                            *
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
 */
package org.dcm4cheri.auditlog;

import java.util.Iterator;
import java.util.LinkedHashSet;

import org.dcm4che.auditlog.Destination;
import org.dcm4che.auditlog.MediaDescription;
import org.dcm4che.auditlog.Patient;

/**
 * @author     <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @since      March 16, 2003
 * @version    $Revision: 3835 $ $Date: 2004-12-03 22:12:29 +0800 (周五, 03 12月 2004) $
 */
class MediaDescriptionImpl implements MediaDescription
{

    // Variables -----------------------------------------------------
    private String mediaID;
    private String mediaType;
    private LinkedHashSet suids = new LinkedHashSet(3);
    private LinkedHashSet pats = new LinkedHashSet(3);
    private Destination dest;

    // Constructors --------------------------------------------------
    /**
     *Constructor for the MediaDescriptionImpl object
     *
     * @param  patient  Description of the Parameter
     */
    public MediaDescriptionImpl(Patient patient)
    {
        addPatient(patient);
    }

    // Methods -------------------------------------------------------
    /**
     *  Sets the mediaID attribute of the MediaDescriptionImpl object
     *
     * @param  mediaID  The new mediaID value
     */
    public void setMediaID(String mediaID)
    {
        this.mediaID = mediaID;
    }


    /**
     *  Sets the mediaType attribute of the MediaDescriptionImpl object
     *
     * @param  mediaType  The new mediaType value
     */
    public void setMediaType(String mediaType)
    {
        this.mediaType = mediaType;
    }


    /**
     *  Sets the destination attribute of the MediaDescriptionImpl object
     *
     * @param  dest  The new destination value
     */
    public void setDestination(Destination dest)
    {
        this.dest = dest;
    }


    /**
     *  Adds a feature to the Patient attribute of the MediaDescriptionImpl object
     *
     * @param  patient  The feature to be added to the Patient attribute
     */
    public final void addPatient(Patient patient)
    {
        pats.add(patient);
    }


    /**
     *  Adds a feature to the StudyInstanceUID attribute of the MediaDescriptionImpl object
     *
     * @param  suid  The feature to be added to the StudyInstanceUID attribute
     */
    public final void addStudyInstanceUID(String suid)
    {
        suids.add(suid);
    }


    /**
     *  Description of the Method
     *
     * @param  sb  Description of the Parameter
     */
    public void writeTo(StringBuffer sb)
    {
        if (mediaID != null) {
            sb.append("<MediaID><![CDATA[")
                    .append(mediaID)
                    .append("]]></MediaID>");
        }
        if (mediaType != null) {
            sb.append("<MediaType><![CDATA[")
                    .append(mediaType)
                    .append("]]></MediaType>");
        }
        for (Iterator it = suids.iterator(); it.hasNext(); ) {
            sb.append("<SUID>")
                    .append(it.next())
                    .append("</SUID>");
        }
        for (Iterator it = pats.iterator(); it.hasNext(); ) {
            Patient pat = (Patient) it.next();
            pat.writeTo(sb);
        }
        if (dest != null) {
            dest.writeTo(sb);
        }
    }
}

