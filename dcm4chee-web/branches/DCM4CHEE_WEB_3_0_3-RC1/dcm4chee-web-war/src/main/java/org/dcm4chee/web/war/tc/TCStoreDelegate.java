package org.dcm4chee.web.war.tc;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4chee.web.common.delegate.BaseMBeanDelegate;
import org.slf4j.LoggerFactory;

/**
 * @author bernhard.ableitinger@gmail.com
 * @version $Revision$ $Date$
 * @since Jan 10, 2012
 */
public class TCStoreDelegate extends BaseMBeanDelegate 
{
    private static TCStoreDelegate instance;

    private TCStoreDelegate()
    {
        super();
    }
    
    public static synchronized TCStoreDelegate getInstance()
    {
        if (instance==null)
        {
            instance = new TCStoreDelegate();
        }
        return instance;
    }
    
    @Override
    public String getServiceNameCfgAttribute() 
    {
        return "tcStoreScuServiceName";
    }
    
    public boolean storeImmediately(DicomObject dataset) throws Exception
    {
        try
        {
            return (Boolean) server.invoke(serviceObjectName, "store", new Object[]{dataset}, new String[]{DicomObject.class.getName()});
        }
        catch (Exception e)
        {
            log.error("Failed to store dataset!", e);
            
            throw e;
        }
    }
    
    public void store(DicomObject dataset) throws Exception
    {
        try
        {
            LoggerFactory.getLogger(TCStoreDelegate.class).info("Storing dataset " + dataset.getString(Tag.SOPInstanceUID));
            server.invoke(serviceObjectName, "schedule", new Object[]{dataset}, new String[]{DicomObject.class.getName()});
        }
        catch (Exception e)
        {
            log.error("Failed to store dataset!", e);
        }
    }

}
