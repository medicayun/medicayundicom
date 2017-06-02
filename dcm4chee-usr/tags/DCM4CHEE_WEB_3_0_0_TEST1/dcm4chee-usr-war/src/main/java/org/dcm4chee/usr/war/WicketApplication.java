/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa-Gevaert AG.
 * Portions created by the Initial Developer are Copyright (C) 2008
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See listed authors below.
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package org.dcm4chee.usr.war;

import java.security.Principal;
import java.security.acl.Group;
import java.util.Enumeration;

import javax.security.auth.Subject;

import org.apache.wicket.Page;
import org.apache.wicket.authentication.AuthenticatedWebApplication;
import org.apache.wicket.authentication.AuthenticatedWebSession;
import org.apache.wicket.authorization.strategies.role.Roles;
import org.apache.wicket.markup.html.WebPage;
import org.dcm4chee.usr.war.common.AccessDeniedPage;
import org.dcm4chee.usr.war.common.InternalErrorPage;
import org.dcm4chee.usr.war.common.PageExpiredErrorPage;
import org.dcm4chee.usr.war.pages.LoginPage;
import org.dcm4chee.usr.war.pages.UserManagementMainPage;
import org.dcm4chee.usr.war.session.JaasWicketSession;

public class WicketApplication extends AuthenticatedWebApplication {

    private String securityDomainName;
    private String rolesGroupName;
    private String userRoleName;
    private String adminRoleName;

    private String hashEncoding;
    private String hashCharset;
    private String hashAlgorithm;

    public static WicketApplication get() {
        return (WicketApplication) AuthenticatedWebApplication.get();
    }

    public String getSecurityDomainName() {
        return securityDomainName;
    }

    public String getRolesGroupName() {
        return rolesGroupName;
    }

    public String getUserRoleName() {
        return userRoleName;
    }

    public String getAdminRoleName() {
        return adminRoleName;
    }

    public void setHashEncoding(String hashEncoding) {
        this.hashEncoding = hashEncoding;
    }

    public String getHashEncoding() {
        return hashEncoding;
    }

    public void setHashCharset(String hashCharset) {
        this.hashCharset = hashCharset;
    }

    public String getHashCharset() {
        return hashCharset;
    }

    public void setHashAlgorithm(String hashAlgorithm) {
        this.hashAlgorithm = hashAlgorithm;
    }

    public String getHashAlgorithm() {
        return hashAlgorithm;
    }

    @Override
    protected void init() {
        super.init();
        
        getApplicationSettings().setAccessDeniedPage(AccessDeniedPage.class);
        getApplicationSettings().setInternalErrorPage(InternalErrorPage.class);
        getApplicationSettings().setPageExpiredErrorPage(PageExpiredErrorPage.class);

        mountBookmarkablePage("/login", LoginPage.class);
        
        this.securityDomainName = getInitParameter("securityDomainName");
        this.rolesGroupName = getInitParameter("rolesGroupName");
        this.userRoleName = getInitParameter("userRoleName");
        this.adminRoleName = getInitParameter("adminRoleName");
    }

    public Roles getRoles(Subject subject) {
        Roles roles = new Roles();
        for (Principal p : subject.getPrincipals()) {
            if ((p instanceof Group) && (rolesGroupName.equals(p.getName()))) {
                Group g = (Group) p;
                Enumeration<? extends Principal> members = g.members();
                while (members.hasMoreElements()) {
                    Principal member = members.nextElement();
                    String name = member.getName();
                    if (userRoleName.equals(name))
                        roles.add(Roles.USER);
                    if (adminRoleName.equals(name))
                        roles.add(Roles.ADMIN);
                }
            }
        }
        return roles;
    }

    public boolean isUserOrAdminRole(String role) {
        return userRoleName.equals(role) || adminRoleName.equals(role);
    }

    @Override
    public Class<? extends Page> getHomePage() {
        return UserManagementMainPage.class;
    }

    @Override
    protected Class<? extends WebPage> getSignInPageClass() {
        return LoginPage.class;
    }

    @Override
    protected Class<? extends AuthenticatedWebSession> getWebSessionClass() {
        return JaasWicketSession.class;
    }
}
