package org.dcm4chee.web.common.login;

import java.util.PropertyResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginResources {

    protected static Logger log = LoggerFactory.getLogger(LoginResources.class);
    
    PropertyResourceBundle prb;
    
    public LoginResources() {
        try {
            prb = new PropertyResourceBundle(this.getClass().getResourceAsStream("locale/login_en.properties"));
        } catch (Exception e) {
            log.error("Error processing default locale for login page: ", e);
        }
    }

    public void setLocale(String locale) {
        try {
            prb = new PropertyResourceBundle(this.getClass().getResourceAsStream("locale/login_" + locale + ".properties"));
        } catch (Exception e) {
            log.error("Error processing locale " + locale + " for login page: ", e);
        }
    }
    
    public String getBrowser_title() {
        return prb.getString("login.browser_title");
    }
    
    public String getLoginLabel() {
        return prb.getString("login.loginLabel");
    }

    public String getUsername() {
        return prb.getString("login.username");
    }

    public String getPassword() {
        return prb.getString("login.password");
    }

    public String getSubmit() {
        return prb.getString("login.submit");
    }
    
    public String getReset() {
        return prb.getString("login.reset");
    }

    public String getLoginFailed() {
        return prb.getString("login.loginFailed");
    }
}
