/*
 * Generated by XDoclet - Do not edit!
 */
package org.dcm4chex.archive.ejb.interfaces;

/**
 * Home interface for StudyPermissionManager.
 * @xdoclet-generated at ${TODAY}
 * @copyright The XDoclet Team
 * @author XDoclet
 * @version 2.19.0-SNAPSHOT
 */
public interface StudyPermissionManagerHome
   extends javax.ejb.EJBHome
{
   public static final String COMP_NAME="java:comp/env/ejb/StudyPermissionManager";
   public static final String JNDI_NAME="ejb/StudyPermissionManager";

   public org.dcm4chex.archive.ejb.interfaces.StudyPermissionManager create()
      throws javax.ejb.CreateException,java.rmi.RemoteException;

}
