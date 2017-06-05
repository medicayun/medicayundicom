package org.dcm4chex.archive.web.maverick.shunt;

import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;

import org.infohazard.maverick.flow.ConfigException;
import org.infohazard.maverick.flow.NoSuitableModeException;
import org.infohazard.maverick.flow.Shunt;
import org.infohazard.maverick.flow.ShuntFactory;
import org.infohazard.maverick.flow.View;

import org.jdom.Element;

public class LanguageShuntFactory implements ShuntFactory {

    public Shunt createShunt() throws ConfigException {
        return new LanguageShunt();
    }

    public void init(Element arg0, ServletConfig arg1) throws ConfigException {
    }
    
    class LanguageShunt implements Shunt {

        private HashMap views = new HashMap();
        
        public void defineMode(String mode, View view) throws ConfigException {
            views.put(mode, view);
        }

        public View getView(HttpServletRequest req) throws NoSuitableModeException {
            ResourceBundle[] rb = (ResourceBundle[]) req.getSession().getAttribute("dcm4chee-web-messages");
            String mode = rb != null? rb[0].getString("view_mode") : 
                    Locale.getDefault().getLanguage();
            View view = (View) views.get(mode);
            if ( view == null )
                view = (View) views.get(null);
            return view;
        }
        
    }
    
}
