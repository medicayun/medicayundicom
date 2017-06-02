package org.dcm4chee.web.war.tc;

import java.io.Serializable;

import org.apache.wicket.Component;
import org.dcm4chee.web.war.tc.TCObject.ITextOrCode;

public interface TCInput extends Serializable {
    
    public ITextOrCode getValue();

    public void resetValue();

    public Component getComponent();

    public void addChangeListener(ValueChangeListener l);
    
    public void removeChangeListener(ValueChangeListener l);
    
    public static interface ValueChangeListener extends Serializable
    {
        public void valueChanged(ITextOrCode value);
    }
}