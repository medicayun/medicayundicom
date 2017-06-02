package org.dcm4chee.web.war.tc.keywords;

import org.dcm4chee.web.war.tc.TCInput;

public interface TCKeywordInput extends TCInput 
{
    public TCKeyword getKeyword();

    public void resetKeyword();
    
    public void setExclusive(boolean exclusive);
    
    public boolean isExclusive();
}