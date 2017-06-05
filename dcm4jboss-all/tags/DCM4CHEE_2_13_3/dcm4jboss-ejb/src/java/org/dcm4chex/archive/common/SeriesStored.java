/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), available at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * TIANI Medgraph AG.
 * Portions created by the Initial Developer are Copyright (C) 2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunter.zeilinger@tiani.com>
 * Franz Willer <franz.willer@gwi-ag.com>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package org.dcm4chex.archive.common;

import java.io.Serializable;

import javax.management.Notification;
import javax.management.NotificationFilter;

import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 5709 $ $Date: 2008-01-17 17:38:43 +0800 (周四, 17 1月 2008) $
 * @since Nov 7, 2005
 */
public class SeriesStored implements Serializable {

    private static final long serialVersionUID = -3119250046425862896L;

    public static final NotificationFilter NOTIF_FILTER =
            new NotificationFilter() {		
        private static final long serialVersionUID = 7625954422409724162L;

        public boolean isNotificationEnabled(Notification notif) {
            return SeriesStored.class.getName().equals(notif.getType());
        }
    };
    
    private final Dataset patAttrs;
    private final Dataset studyAttrs;
    private final Dataset seriesAttrs;
    private final Dataset ian;
    private final int numI;

    public SeriesStored(Dataset patient, Dataset study, Dataset series,
            Dataset ian) {
        if (patient == null) {
            throw new NullPointerException("patient");
        }
        if (study == null) {
            throw new NullPointerException("study");
        }
        if (series == null) {
            throw new NullPointerException("series");
        }
        if (ian == null) {
            throw new NullPointerException("ian");
        }
        this.patAttrs = patient;
        this.studyAttrs = study;
        this.seriesAttrs = series;
        this.ian = ian;
        this.numI = ian.getItem(Tags.RefSeriesSeq).get(Tags.RefSOPSeq)
                .countItems();
    }

    public String toString() {
        return "SeriesStored[calling=" + getCallingAET()
                + ", modality=" + getModality()
                + ", numOfInst=" + numI
                + ", study-iuid=" + getStudyInstanceUID()
                + ", series-iuid=" + getSeriesInstanceUID() + "]";
    }

    public final Dataset getPatientAttrs() {
        return patAttrs;
    }

    public final Dataset getStudyAttrs() {
        return studyAttrs;
    }

    public final Dataset getSeriesAttrs() {
        return seriesAttrs;
    }

    public final Dataset getIAN() {
        return ian;
    }

    public final String getCallingAET() {
        seriesAttrs.setPrivateCreatorID(PrivateTags.CreatorID);
        return seriesAttrs.getString(PrivateTags.CallingAET);
    }

    public String getRetrieveAET() {
        return seriesAttrs.getString(Tags.RetrieveAET);
    }

    public String getPatientID() {
        return patAttrs.getString(Tags.PatientID);
    }

    public String getPatientName() {
        return patAttrs.getString(Tags.PatientName);
    }

    public String getStudyInstanceUID() {
        return studyAttrs.getString(Tags.StudyInstanceUID);
    }

    public String getAccessionNumber() {
        return studyAttrs.getString(Tags.AccessionNumber);
    }

    public String getStudyID() {
        return studyAttrs.getString(Tags.StudyID);
    }

    public String getSeriesInstanceUID() {
        return seriesAttrs.getString(Tags.SeriesInstanceUID);
    }

    public String getModality() {
        return seriesAttrs.getString(Tags.Modality);
    }

    public int getNumberOfInstances() {
        return numI;
    }

}
