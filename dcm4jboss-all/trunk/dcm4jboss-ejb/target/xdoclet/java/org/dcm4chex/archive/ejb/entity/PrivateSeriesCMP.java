/*
 * Generated by XDoclet - Do not edit!
 */
package org.dcm4chex.archive.ejb.entity;

/**
 * CMP layer for PrivateSeries.
 * @xdoclet-generated at ${TODAY}
 * @copyright The XDoclet Team
 * @author XDoclet
 * @version 2.19.0-SNAPSHOT
 */
public abstract class PrivateSeriesCMP
   extends org.dcm4chex.archive.ejb.entity.PrivateSeriesBean
   implements javax.ejb.EntityBean
{

   public void ejbLoad() 
   {
   }

   public void ejbStore() 
   {
   }

   public void ejbActivate() 
   {
   }

   public void ejbPassivate() 
   {

   }

   public void setEntityContext(javax.ejb.EntityContext ctx) 
   {
   }

   public void unsetEntityContext() 
   {
   }

   public void ejbRemove() throws javax.ejb.RemoveException
   {

   }

   public abstract java.lang.Long getPk() ;

   public abstract void setPk( java.lang.Long pk ) ;

   public abstract int getPrivateType() ;

   public abstract void setPrivateType( int privateType ) ;

   public abstract byte[] getEncodedAttributes() ;

   public abstract void setEncodedAttributes( byte[] encodedAttributes ) ;

   public abstract java.lang.String getSeriesIuid() ;

   public abstract void setSeriesIuid( java.lang.String seriesIuid ) ;

   public abstract java.lang.String getSourceAET() ;

   public abstract void setSourceAET( java.lang.String sourceAET ) ;

}
