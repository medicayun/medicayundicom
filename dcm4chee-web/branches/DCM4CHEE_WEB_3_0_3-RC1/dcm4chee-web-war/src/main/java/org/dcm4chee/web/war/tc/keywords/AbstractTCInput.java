package org.dcm4chee.web.war.tc.keywords;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.panel.Panel;
import org.dcm4chee.web.war.tc.TCInput;

public abstract class AbstractTCInput extends Panel implements TCInput 
{

    private List<ValueChangeListener> changeListener;
    
    public AbstractTCInput(final String id)
    {
        super(id);
    }
    
    @Override
    public Panel getComponent() 
    {
        return this;
    }

    protected void fireValueChanged()
    {
        if (changeListener!=null)
        {
            for (ValueChangeListener l : changeListener)
            {
                l.valueChanged(getValue());
            }
        }
    }

    @Override
    public void removeChangeListener(ValueChangeListener l) {
        if (changeListener!=null)
        {
            changeListener.remove(l);
        }
    }

    @Override
    public void addChangeListener(ValueChangeListener l) {
        if (changeListener==null)
        {
            changeListener = new ArrayList<ValueChangeListener>();
        }
        if (!changeListener.contains(l))
        {
            changeListener.add(l);
        }
    }
}
