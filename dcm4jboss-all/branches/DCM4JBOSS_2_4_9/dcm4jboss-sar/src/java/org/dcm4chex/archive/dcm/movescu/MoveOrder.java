/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.dcm.movescu;

import java.io.Serializable;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1793 $ $Date: 2005-06-22 15:47:41 +0800 (周三, 22 6月 2005) $
 * @since 26.08.2004
 *
 */
public final class MoveOrder implements Serializable {

	private static final long serialVersionUID = 3617856386927702068L;

	public static final String QUEUE = "MoveScu";

    private String retrieveAET;

    private String moveDestination;

    private String patientId;

    private String[] studyIuids;

    private String[] seriesIuids;

    private String[] sopIuids;

    private int priority;

    private int failureCount;

    public MoveOrder(String retrieveAET, String moveDestination, int priority,
            String patientId, String[] studyIuids, String[] seriesIuids,
            String[] sopIuids) {
        this.priority = priority;
        this.retrieveAET = retrieveAET;
        this.moveDestination = moveDestination;
        this.patientId = patientId;
        this.studyIuids = studyIuids;
        this.seriesIuids = seriesIuids;
        this.sopIuids = sopIuids;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("MoveOrder[");
        if (sopIuids != null) {
            sb.append(sopIuids.length).append(" Instances, dest=");
        } else if (seriesIuids != null) {
            sb.append(seriesIuids.length).append(" Series, dest=");
        } else if (studyIuids != null) {
            sb.append(studyIuids.length).append(" Studies, dest=");
        } else {
            sb.append("1 Patient, dest=");
        }
        sb.append(moveDestination);
        sb.append(", qrscp=").append(retrieveAET);
        sb.append(", priority=").append(priority);
        sb.append(", failureCount=").append(failureCount);
        sb.append("]");
        return sb.toString();
    }

    public final String getQueryRetrieveLevel() {
        return sopIuids != null ? "IMAGE" : seriesIuids != null ? "SERIES"
                : studyIuids != null ? "STUDY" : "PATIENT";
    }

    public final int getPriority() {
        return priority;
    }

    public final void setPriority(int priority) {
        this.priority = priority;
    }

    public final int getFailureCount() {
        return failureCount;
    }

    public final void setFailureCount(int failureCount) {
        this.failureCount = failureCount;
    }

    public final String getMoveDestination() {
        return moveDestination;
    }

    public final void setMoveDestination(String moveDestination) {
        this.moveDestination = moveDestination;
    }

    public final String getPatientId() {
        return patientId;
    }

    public final void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public final String getRetrieveAET() {
        return retrieveAET;
    }

    public final void setRetrieveAET(String retrieveAET) {
        this.retrieveAET = retrieveAET;
    }

    public final String[] getSeriesIuids() {
        return seriesIuids;
    }

    public final void setSeriesIuids(String[] seriesIuids) {
        this.seriesIuids = seriesIuids;
    }

    public final String[] getSopIuids() {
        return sopIuids;
    }

    public final void setSopIuids(String[] sopIuids) {
        this.sopIuids = sopIuids;
    }

    public final String[] getStudyIuids() {
        return studyIuids;
    }

    public final void setStudyIuids(String[] studyIuids) {
        this.studyIuids = studyIuids;
    }

}