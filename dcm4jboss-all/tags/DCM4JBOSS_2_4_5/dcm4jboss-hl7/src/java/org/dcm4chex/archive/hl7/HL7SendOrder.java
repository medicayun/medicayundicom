package org.dcm4chex.archive.hl7;

import java.io.Serializable;

public class HL7SendOrder implements Serializable {

	private static final long serialVersionUID = 3257003259104147767L;

	private final byte[] hl7msg;

	private final String receiving;

    private int failureCount;	

    private Exception exception;

	public HL7SendOrder(byte[] hl7msg, String receiving) {
		if (hl7msg == null)
			throw new NullPointerException();
		if (receiving == null)
			throw new NullPointerException();
		this.hl7msg = hl7msg;
		this.receiving = receiving;
	}
	
    public final byte[] getHL7Message() {
        return hl7msg;
    }

	public final String getReceiving() {
		return receiving;
	}

	public final int getFailureCount() {
        return failureCount;
    }

    public final void setFailureCount(int failureCount) {
        this.failureCount = failureCount;
    }
	

    public String toString() {
        return "HL7SendOrder[receiving=" + receiving
        		+ ", failureCount=" + failureCount 
                + ", exception=" + exception
                + "]";
    }

	public final Exception getException() {
		return exception;
	}

	public final void setException(Exception exception) {
		this.exception = exception;
	}

}
