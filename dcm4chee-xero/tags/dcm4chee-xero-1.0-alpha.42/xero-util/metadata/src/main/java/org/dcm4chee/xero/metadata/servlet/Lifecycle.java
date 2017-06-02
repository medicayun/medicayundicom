package org.dcm4chee.xero.metadata.servlet;

/** Defines calls to manage a lifecylce - start, stop */
public interface Lifecycle {
    
    /** The overall start/stop of a service */
    public void start(String name);
    public void stop(String name);
}
