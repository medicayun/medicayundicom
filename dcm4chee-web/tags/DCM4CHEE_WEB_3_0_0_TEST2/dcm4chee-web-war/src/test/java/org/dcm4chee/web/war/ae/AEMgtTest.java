package org.dcm4chee.web.war.ae;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Locale;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import org.apache.wicket.authorization.strategies.role.IRoleCheckingStrategy;
import org.apache.wicket.authorization.strategies.role.RoleAuthorizationStrategy;
import org.apache.wicket.authorization.strategies.role.Roles;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.dcm4chee.archive.entity.AE;
import org.dcm4chee.archive.entity.FileSystem;
import org.dcm4chee.web.dao.ae.AEHomeBean;
import org.dcm4chee.web.war.WicketApplication;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bm.testsuite.BaseSessionBeanFixture;

public class AEMgtTest extends BaseSessionBeanFixture<AEHomeBean>
{
    private WicketApplication testApplicaton;
    private WicketTester wicketTester;
    private ArrayList<AE> aeList = new ArrayList<AE>(5);

    private static Logger log = LoggerFactory.getLogger(AEMgtTest.class);
    
    private static final Class<?>[] usedBeans = {FileSystem.class, AE.class};

    public AEMgtTest() throws Exception {
        super(AEHomeBean.class, usedBeans);
    }
    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        testApplicaton = new WicketApplication();
        wicketTester = new WicketTester(testApplicaton);
        aeList.add(getTestAE("AE_TEST", "localhost", 11112));
        //aeList.add(getTestAE("AE_FAILED", "localhost", 12222));
        //aeList.add(getTestAE("AE_TEST2", "localhost", 11113));
        //aeList.add(getTestAE("AE_TEST3", "localhost", 11114));
    }
    
    @Test
    public void testOpenAEMgt() {
        wicketTester.startPage(AETestPage.class);
        wicketTester.assertRenderedPage(AETestPage.class);
        wicketTester.assertComponent("aemgt:ae_panel", AEListPanel.class);
    }
    @Test
    public void testEditAERequiredFields() {
        wicketTester.getApplication().getSecuritySettings().setAuthorizationStrategy(
                new RoleAuthorizationStrategy(new UserRolesAuthorizer("WebAdmin")));
        wicketTester.startPage(AETestPage.class);
        wicketTester.getComponentFromLastRenderedPage("aemgt:ae_panel").getSession().setLocale(new Locale("en"));
        wicketTester.clickLink("aemgt:ae_panel:newAET");
        wicketTester.assertComponent("aemgt:ae_panel", EditAETPanel.class);
        FormTester formTester = wicketTester.newFormTester("aemgt:ae_panel:form");
        formTester.setValue("title", "");
        formTester.setValue("hostName", "");
        formTester.setValue("port", "0");
        formTester.submit();
        String[] expectedErrors = new String[]{"Field 'title' is required.",
                "Field 'hostName' is required.",
                "0 is not between 1 and 65535."};
        wicketTester.assertErrorMessages(expectedErrors);
    }
    
    @Test
    public void testEditAEValidators() {
        wicketTester.getApplication().getSecuritySettings().setAuthorizationStrategy(
                new RoleAuthorizationStrategy(new UserRolesAuthorizer("WebAdmin")));
        wicketTester.startPage(AETestPage.class);
        wicketTester.getComponentFromLastRenderedPage("aemgt:ae_panel").getSession().setLocale(new Locale("en"));
        wicketTester.clickLink("aemgt:ae_panel:newAET");
        FormTester formTester = wicketTester.newFormTester("aemgt:ae_panel:form");
        formTester.setValue("title", "AE_TEST_TO_LONGLONG");
        formTester.setValue("hostName", "");
        formTester.setValue("port", "100000000");
        formTester.setValue("wadoURL", "http://127.0.0.1.1/wado");
        formTester.submit();
        wicketTester.assertComponent("aemgt:ae_panel", EditAETPanel.class);
        String[] expectedErrors = new String[]{"'AE_TEST_TO_LONGLONG' is not between 1 and 16 characters long.",
              "Field 'hostName' is required.",
              "100000000 is not between 1 and 65535.",
              "'http://127.0.0.1.1/wado' is not a valid URL."};
        wicketTester.assertErrorMessages(expectedErrors);
    }
    
    @Test
    public void testNewAE() {
        wicketTester.getApplication().getSecuritySettings().setAuthorizationStrategy(
                new RoleAuthorizationStrategy(new UserRolesAuthorizer("WebAdmin")));
        wicketTester.startPage(AETestPage.class);
        for ( AE ae : aeList ) {
            wicketTester.clickLink("aemgt:ae_panel:newAET");
            wicketTester.assertComponent("aemgt:ae_panel", EditAETPanel.class);
            FormTester formTester = wicketTester.newFormTester("aemgt:ae_panel:form");
            formTester.setValue("title", ae.getTitle());
            formTester.setValue("hostName", ae.getHostName());
            formTester.setValue("port", String.valueOf(ae.getPort()));
            getEntityManager().getTransaction().begin();
            formTester.submit("submit");
            getEntityManager().getTransaction().commit();
            wicketTester.assertNoErrorMessage();
            wicketTester.assertComponent("aemgt:ae_panel", AEListPanel.class);
        }
        wicketTester.assertListView("aemgt:ae_panel:list", aeList);
    }

    @Test
    public void testOpenEchoFromList() {
        initDummyMBean();
        wicketTester.startPage(AETestPage.class);
        wicketTester.assertListView("aemgt:ae_panel:list", aeList);
        wicketTester.clickLink("aemgt:ae_panel:list:0:echo");
        wicketTester.assertNoErrorMessage();
        wicketTester.assertComponent("aemgt:ae_panel:echoPanel:content", DicomEchoWindow.DicomEchoPanel.class);        
    }

    private AE getTestAE(String title, String host, int port) {
        AE ae = new AE();
        ae.setTitle(title);
        ae.setHostName(host);
        ae.setPort(port);
        return ae;
    }
    
    @Test
    public void testUnauthorizedEdit() {
        wicketTester.getApplication().getSecuritySettings().setAuthorizationStrategy(
                new RoleAuthorizationStrategy(new UserRolesAuthorizer("dummy")));
        wicketTester.startPage(AETestPage.class);
        wicketTester.getComponentFromLastRenderedPage("aemgt:ae_panel").getSession().setLocale(new Locale("en"));
        wicketTester.assertInvisible("aemgt:ae_panel:newAET");
        wicketTester.assertInvisible("aemgt:ae_panel:list:0:editAET");
        wicketTester.assertInvisible("aemgt:ae_panel:list:0:removeAET");
    }
    
    private void initDummyMBean() {
        MBeanServer mbServer = MBeanServerFactory.createMBeanServer();
        try {
            mbServer.createMBean("org.dcm4chee.web.wicket.ae.DummyEchoMBean", 
                    new ObjectName("dcm4chee.archive:service=ECHOService"));
        } catch (Exception ignore) {log.error("Can't create DummyEchoMBean!",ignore);}        
    }
    
    private static final class UserRolesAuthorizer implements IRoleCheckingStrategy, Serializable {
            private static final long serialVersionUID = 1L;

            private final Roles roles;

            public UserRolesAuthorizer(String roles) {
                    this.roles = new Roles(roles);
            }

            public boolean hasAnyRole(Roles roles) {
                    return this.roles.hasAnyRole(roles);
            }
    }
    
}
