package org.dcm4chee.web.war;


import java.net.URL;

import org.apache.wicket.Page;
import org.apache.wicket.Session;
import org.apache.wicket.authorization.strategies.role.Roles;
import org.apache.wicket.protocol.http.MockServletContext;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.dcm4chee.archive.entity.AE;
import org.dcm4chee.archive.entity.Code;
import org.dcm4chee.archive.entity.File;
import org.dcm4chee.archive.entity.FileSystem;
import org.dcm4chee.archive.entity.GPPPS;
import org.dcm4chee.archive.entity.GPSPS;
import org.dcm4chee.archive.entity.GPSPSPerformer;
import org.dcm4chee.archive.entity.GPSPSRequest;
import org.dcm4chee.archive.entity.Instance;
import org.dcm4chee.archive.entity.MPPS;
import org.dcm4chee.archive.entity.MWLItem;
import org.dcm4chee.archive.entity.Media;
import org.dcm4chee.archive.entity.OtherPatientID;
import org.dcm4chee.archive.entity.Patient;
import org.dcm4chee.archive.entity.RequestAttributes;
import org.dcm4chee.archive.entity.Series;
import org.dcm4chee.archive.entity.Study;
import org.dcm4chee.archive.entity.StudyOnFileSystem;
import org.dcm4chee.archive.entity.VerifyingObserver;
import org.dcm4chee.usr.entity.Role;
import org.dcm4chee.usr.entity.User;
import org.dcm4chee.web.common.base.LoginPage;
import org.dcm4chee.web.dao.folder.StudyListBean;
import org.junit.Test;

import com.bm.testsuite.BaseSessionBeanFixture;

public class WicketApplicationTest extends BaseSessionBeanFixture<StudyListBean>
{
    private static final String USER = "user";
    private static final String ADMIN = "admin";
    private WicketApplication testApplicaton;
    private WicketTester wicketTester;

    private static final Class<?>[] usedBeans = {Patient.class, Study.class, Series.class, Instance.class,
        File.class, FileSystem.class, StudyOnFileSystem.class, VerifyingObserver.class,
        Media.class, MPPS.class, GPSPS.class, GPPPS.class, GPSPSRequest.class, GPSPSPerformer.class,
        MWLItem.class,  
        OtherPatientID.class, AE.class, RequestAttributes.class, Code.class, User.class, Role.class};
    public WicketApplicationTest() throws Exception {
        super(StudyListBean.class, usedBeans);
    }
    @Override
    public void setUp() throws Exception {
        super.setUp();
        testApplicaton = new WicketApplication();
        wicketTester = new WicketTester(testApplicaton);
        MockServletContext ctx =(MockServletContext)wicketTester.getApplication().getServletContext();
        ctx.addInitParameter("securityDomainName", "dcm4chee");
        ctx.addInitParameter("rolesGroupName", "Roles");
        URL url = this.getClass().getResource("/wicket.login.file");
        //Set login configuration file! '=' means: overwrite other login configuration given in Java security properties file.
        System.setProperty("java.security.auth.login.config", "="+url.getPath()); 
    }
    
    @Test
    public void testShouldAuthChallenge() {
        wicketTester.startPage(MainPage.class);
        wicketTester.assertRenderedPage(LoginPage.class);
    }

    @Test
    public void testAdminLoginShouldAllow() {
        checkLogin(ADMIN, ADMIN, MainPage.class);
    }
    @Test
    public void testAdminLoginShouldFail() {
        checkLogin(ADMIN, "admon", LoginPage.class);
    }
    @Test
    public void testUserLoginShouldAllow() {
        checkLogin(USER, USER, MainPage.class);
    }
    @Test
    public void testUserLoginShouldFail() {
        checkLogin(USER, "wrong", LoginPage.class);
    }
    @Test
    public void testUnknownLoginShouldFail() {
        checkLogin("unknown", "unknown", LoginPage.class);
    }

    @Test
    public void testAdminRoles() {
         checkRoles(ADMIN, new String[]{"WebUser","WebAdmin","Doctor","JBossAdmin","AuditLogUser"});
    }
    @Test
    public void testUserRoles() {
        checkRoles( USER, new String[]{"WebUser","AuditLogUser"});
    }
    @Test
    public void testDocRoles() {
        checkRoles( "doc", new String[]{"WebUser","Doctor","AuditLogUser"});
    }
    @Test
    public void testGuestRoles() {
        checkRoles( "guest", new String[]{"WebUser"});
    }

    private void checkLogin(String user, String passwd, Class<? extends Page> pageClass) {
        wicketTester.startPage(MainPage.class);
        FormTester formTester = wicketTester.newFormTester("signInPanel:signInForm");
        formTester.setValue("username", user);
        formTester.setValue("password", passwd);
        formTester.submit();
        wicketTester.assertRenderedPage(pageClass);
    }

    private void checkRoles(String user, String[] roles) {
        Roles r = null;
        try {
            checkLogin(user, user, MainPage.class);
            Session session = Session.get();
            assertNotNull("Wicket Session is null", session);
            assertEquals("Wrong Class of Wicket Session!", WicketSession.class, session.getClass());
            r = ((WicketSession) session).getRoles();
            assertEquals("Wrong number of roles!",roles.length, r.size());
            for ( String role : roles) {
                assertTrue("Missing role:"+role, r.hasRole(role));
            }
        } catch (Throwable t) {
            fail(user+"("+r+"): "+t.getMessage());
            t.printStackTrace();
        }
    }
}
