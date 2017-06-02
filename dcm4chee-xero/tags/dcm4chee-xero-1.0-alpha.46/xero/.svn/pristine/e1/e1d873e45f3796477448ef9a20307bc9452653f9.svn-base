package org.dcm4chee.xero.controller;

import static org.easymock.classextension.EasyMock.*;

import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.Principal;
import java.security.acl.Group;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;

import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;

import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.metadata.filter.FilterUtil;
import org.dcm4chee.xero.metadata.servlet.MetaDataServlet;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import sun.security.acl.GroupImpl;
import sun.security.acl.PrincipalImpl;

import com.sun.security.auth.UserPrincipal;

public class LanguageThemeControllerTest {

	LanguageThemeController<Object> ltc = new LanguageThemeController<Object>();
	FilterItem<Object> filterItem;
	Map<String,Object> params, model;
	
	@SuppressWarnings("unchecked")
	@BeforeMethod
	public void init() {
		filterItem = createMock(FilterItem.class);
		params = new HashMap<String,Object>();
		model = new HashMap<String,Object>();
		params.put("model", model);
		expect(filterItem.callNextFilter(params)).andReturn(null);
		replay(filterItem);
	}
	
	@Test
	public void test_theme_and_language_default() {
		params.put("locale", Locale.CANADA);
		ltc.filter(filterItem, params);
		verify(filterItem);
		Map<String,Object> i18n = FilterUtil.getMap(params,"model.i18n");
		assert i18n!=null;
		assert i18n.get("themeName").equals("Default Theme");
		assert i18n.get("Language").equals("English");
	}
	
	@Test
	public void test_theme_and_language_custom() {
		params.put("theme", "custom");
		params.put("locale", Locale.CANADA_FRENCH);
		ltc.filter(filterItem, params);
		verify(filterItem);
		Map<String,Object> i18n = FilterUtil.getMap(params,"model.i18n");
		assert i18n!=null;
		assert i18n.get("themeName").equals("Custom Theme");
		assert i18n.get("Language").equals("Français");
	}
	
	  @Test(expectedExceptions=SecurityException.class)
	  public void checkPermission_Subject_EprUser_RestrictedToEPRTheme() {
	     Subject s = createSubjectWithRole("EprUser");
	     String theme = "custom";
	     ltc.checkPermission(theme,s);
	  }
	  
     @Test
     public void checkPermission_Subject_EprUser_AllowedToUseEPRTheme() {
        Subject s = createSubjectWithRole("EprUser");
        String theme = "epr";
        ltc.checkPermission(theme,s);
     }
	  
	  @Test
     public void checkPermission_NullSubject_IndicatesUnrestrictedVM() {
        String theme = "custom";
        ltc.checkPermission(theme, (Subject)null);
     }
	  
	  @Test
     public void checkPermission_NullRequest_IndicatesUnrestrictedVM() {
        String theme = "custom";
        ltc.checkPermission(theme, (HttpServletRequest)null);
	  }
	  
	  @Test
	  public void checkPermission_ServletRequest_EprUser_AllowedToUseEPRTheme()
	  {
        HttpServletRequest r = createMock(HttpServletRequest.class);
        expect(r.isUserInRole("EprUser")).andStubReturn(true);
        replay(r);
        String theme = "epr";
        ltc.checkPermission(theme,r);
	  }
	  
     @Test(expectedExceptions=SecurityException.class)
     public void checkPermission_ServletRequest_EprUser_RestrictedToEprTheme()
     {
        HttpServletRequest r = createMock(HttpServletRequest.class);
        expect(r.isUserInRole("EprUser")).andStubReturn(true);
        replay(r);
        String theme = "other";
        ltc.checkPermission(theme,r);
     }
	  
	  private Subject createSubjectWithRole(String role)
	  {
        Group g = new GroupImpl("Roles");
	     g.addMember(new PrincipalImpl(role));
	     Subject s = new Subject(false,new HashSet<Principal>(),new HashSet<Object>(), new HashSet<Object>());
        s.getPrincipals().add(g);
        return s;
	  }
	  
	  
}
