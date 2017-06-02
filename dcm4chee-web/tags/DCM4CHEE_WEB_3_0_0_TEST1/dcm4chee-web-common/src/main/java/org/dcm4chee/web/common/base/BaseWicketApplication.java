package org.dcm4chee.web.common.base;

import org.apache.wicket.Page;
import org.apache.wicket.authentication.AuthenticatedWebApplication;
import org.apache.wicket.authentication.AuthenticatedWebSession;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.settings.IExceptionSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application object for your web application. If you want to run this
 * application without deploying, run the Start class.
 * 
 * @see wicket.myproject.Start#main(String[])
 */
public class BaseWicketApplication extends AuthenticatedWebApplication {

    private Class<? extends Page> homePage;
    private Class<? extends WebPage> signinPage;
    private Class<? extends Page> accessDeniedPage;
    private Class<? extends Page> pageExpiredPage;
    private Class<? extends Page> internalErrorPage;

    private final static Logger log = LoggerFactory.getLogger(BaseWicketApplication.class);
    
    public BaseWicketApplication() {
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void init() {
        super.init();
        homePage = getPageClass(getInitParameter("homePageClass"), null);
        signinPage = (Class<? extends WebPage>) getPageClass(getInitParameter("signinPageClass"), LoginPage.class);
        accessDeniedPage = (Class<? extends Page>) getPageClass(getInitParameter("accessDeniedPage"), signinPage);
        pageExpiredPage = (Class<? extends Page>) getPageClass(getInitParameter("pageExpiredPage"), signinPage);
        internalErrorPage = getPageClass(getInitParameter("internalErrorPageClass"), null);
        getApplicationSettings().setAccessDeniedPage(accessDeniedPage);
        getApplicationSettings().setPageExpiredErrorPage(pageExpiredPage);
        if ( internalErrorPage != null ) {
            getApplicationSettings().setInternalErrorPage(internalErrorPage);
            mountBookmarkablePage("/internalError", internalErrorPage);
            this.getExceptionSettings().setUnexpectedExceptionDisplay(IExceptionSettings.SHOW_INTERNAL_ERROR_PAGE);
        }
        mountBookmarkablePage("/login", signinPage);
        if (pageExpiredPage != signinPage)
            mountBookmarkablePage("/expired", pageExpiredPage);
        if (accessDeniedPage != signinPage)
            mountBookmarkablePage("/denied", accessDeniedPage);
    }

    @SuppressWarnings("unchecked")
    private Class<? extends Page> getPageClass(String className, Class<? extends Page> def) {
        Class<?> clazz = null;
        if ( className != null ) {
            try {
                clazz = (Class<? extends Page>) Class.forName(className);
            } catch (Throwable t) {
                log.error("Could not get Class "+className+"! use default:"+def, t);
            }
        }
        return (Class<? extends Page>) (clazz == null ? def : clazz);
    }

    @Override
    public Class<? extends Page> getHomePage() {
        if (homePage == null) {
            throw new RuntimeException("No HomePage is set!"+
               " You have to set init-param 'homePageClass' in web.xml "+
               "or subclass BaseWicketApplication and override getHomePage()!");
        }
        return homePage;
    }

    @Override
    protected Class<? extends WebPage> getSignInPageClass() {
        return signinPage;
    }

    @Override
    protected Class<? extends AuthenticatedWebSession> getWebSessionClass() {
        return JaasWicketSession.class;
    }
}
