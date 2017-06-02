package org.dcm4chee.web.war;

import org.apache.wicket.Page;
import org.apache.wicket.authentication.AuthenticatedWebSession;
import org.dcm4chee.web.common.base.BaseWicketApplication;

/**
 * Application object for your web application. If you want to run this
 * application without deploying, run the Start class.
 * 
 * @see wicket.myproject.Start#main(String[])
 */
public class WicketApplication extends BaseWicketApplication {
    
    @Override
    public Class<? extends Page> getHomePage() {
        return MainPage.class;
    }

    @Override
    protected Class<? extends AuthenticatedWebSession> getWebSessionClass() {
        return WicketSession.class;
    }
}
