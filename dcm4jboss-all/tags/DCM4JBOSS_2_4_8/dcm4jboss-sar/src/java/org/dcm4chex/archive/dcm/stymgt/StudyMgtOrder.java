package org.dcm4chex.archive.dcm.stymgt;

import java.io.Serializable;

import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;

public class StudyMgtOrder implements Serializable {
	
	private static final long serialVersionUID = 3258417226779603505L;

	private final String callingAET;

	private final String calledAET;
	
	private final int cmdField;
	
	private final int actionTypeID;

	private final String iuid;

	private final Dataset ds;

	private int failureCount;

	private Exception exception;

	public StudyMgtOrder(String callingAET, String calledAET,
			int cmdField, int actionID, String iuid, Dataset dataset){
		this.callingAET = callingAET;
		this.calledAET = calledAET;
		this.cmdField = cmdField;
		this.actionTypeID = actionID;
		this.iuid = iuid;
		this.ds = dataset;
	}

	public final String getCalledAET() {
		return calledAET;
	}

	public final String getCallingAET() {
		return callingAET;
	}
	
	public final int getActionTypeID() {
		return actionTypeID;
	}

	public final int getCommandField() {
		return cmdField;
	}

	public final String getSOPInstanceUID() {
		return iuid;
	}
	
	public final Dataset getDataset() {
		return ds;
	}
	
    public final int getFailureCount() {
        return failureCount;
    }

    public final void setFailureCount(int failureCount) {
        this.failureCount = failureCount;
    }

	public Exception getException() {
		return exception;
	}

	public void setException(Exception exception) {
		this.exception = exception;
	}

    public String toString() {
        return "StudyMgtOrder[" + cmdFieldAsString()
        		+ ", iuid=" + iuid
        		+ ", failureCount=" + failureCount 
                + ", exception=" + exception
                + "]";
    }

	private String cmdFieldAsString() {
		return commandAsString(cmdField, actionTypeID);		
	}
	
	public static String commandAsString(int cmdField, int actionTypeID) {
	      switch (cmdField) {
	         case Command.N_SET_RQ:
	            return "N_SET_RQ";
	         case Command.N_ACTION_RQ:
	            return "N_ACTION_RQ(" + actionTypeID + ")";
	         case Command.N_CREATE_RQ:
	            return "N_CREATE_RQ";
	         case Command.N_DELETE_RQ:
	            return "N_DELETE_RQ";
	      }
		  return Integer.toHexString(cmdField).toUpperCase();
	}
}
