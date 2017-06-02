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
 * Bill Wallace, Agfa HealthCare Inc., 
 * Portions created by the Initial Developer are Copyright (C) 2008
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Bill Wallace <bill.wallace@agfa.com>
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
package org.dcm4chee.xero.controller;

import java.io.IOException;
import java.io.InputStream;
import java.security.AccessControlException;
import java.security.Principal;
import java.security.acl.Group;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;

import org.dcm4chee.xero.metadata.access.ResourceBundleMap;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.metadata.filter.FilterUtil;
import org.dcm4chee.xero.metadata.servlet.MetaDataServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sun.security.acl.PrincipalImpl;

/**
 * The Language/Theme Controller adds an i18n and theme object to the model
 * based on the selected parameters. It may also update the Locale object in the
 * base filter to be one of the available locales.
 * 
 * The theme defines the language choice, and then the language itself is
 * available.
 * 
 * @author bwallace
 * @param <Properties>
 * 
 */
public class LanguageThemeController<T> implements Filter<T> {

	private static Logger log = LoggerFactory
			.getLogger(LanguageThemeController.class);

	/** The theme key to get from params */
	public static final String THEME_KEY = "theme";
	
	/** The key to specify the resource bundle to use for the language */
	public static final String DEFAULT_I18N_KEY="defaultI18n";

	ConcurrentMap<String, Properties> themes = new ConcurrentHashMap<String, Properties>();
	ClassLoader cl = Thread.currentThread().getContextClassLoader();

	private static Principal EPR_USER_ROLE = new PrincipalImpl("EprUser");
	
	/** Adds browser identification information */
	public T filter(FilterItem<T> filterItem, Map<String, Object> params) {
		Map<String, Object> model = FilterUtil.getModel(params);

		Properties theme = null;

		String sTheme = FilterUtil.getString(params, THEME_KEY);
		if( sTheme!=null ) {
			theme = getTheme(sTheme);
			if (theme == null) {
				log.warn("Theme {} not found, using default theme.", sTheme);
			}
		}

		// Get a theme from the http servlet request
		HttpServletRequest req = (HttpServletRequest) params.get(MetaDataServlet.REQUEST);
		if( theme==null && req!=null ) {
			String serverName = req.getServerName();
			log.info("Server Name="+serverName);
			int dot = serverName.indexOf(".");
			if( dot>0 ) serverName = serverName.substring(0,dot);
			theme = getTheme(serverName);
			if( theme!=null ) {
				log.info("Using theme from request server name {}", serverName);
			}
		}
		
		if( theme==null ) theme = getTheme("theme");
		
		String themeName = (String) theme.get("theme");
		checkPermission(themeName, (HttpServletRequest) params.get(MetaDataServlet.REQUEST));
		log.info("Using theme {}", themeName);

		Locale locale = FilterUtil.getLocale(params);
		ResourceBundle rb = getI18N(theme, locale);
		log.info("Adding i18n and locale to the model.");
		model.put("i18n", new ResourceBundleMap(rb, theme));
		model.put("locale", locale);

		return filterItem.callNextFilter(params);
	}

   /** Returns the theme associated with the given string.
	 * If the theme contains theme.inherit then that theme will
	 * be used as the parent theme that provides default values.
	 */
	public Properties getTheme(String sTheme) {
		Properties theme = themes.get(sTheme);
		if (theme != null)
			return theme;
		try {
			InputStream is = cl.getResourceAsStream(sTheme+".properties");
			if (is == null)
				return null;
			theme = new Properties();
			theme.load(is);
			is.close();
			String sParent = theme.getProperty("theme.inherit");
			if( sParent!=null ) {
				Properties parent = getTheme(sParent);
				if( parent==null ) log.error("Parent theme "+sParent+" not found.");
				else log.info("Inheriting from parent {} into {}", sParent, sTheme);
				theme = new Properties();
				theme.putAll(parent);
				is = cl.getResourceAsStream(sTheme+".properties");
				theme.load(is);
				is.close();
			}
			themes.putIfAbsent(sTheme, theme);
		} catch (IOException e) {
			return null;
		}
		return theme;
	}

	/** Returns the language bundle for the given theme/locale */
	public ResourceBundle getI18N(Properties theme, Locale loc) {
		String i18nName = theme.getProperty(DEFAULT_I18N_KEY);
		if( i18nName!=null ) log.info("Using language bundle {}",i18nName);
		else {
			i18nName = "i18n";
			log.info("Using language bundle i18n.");
		}
		ResourceBundle ret = ResourceBundle.getBundle(i18nName,loc);
		return ret;
	}
	
   public void checkPermission(String theme,HttpServletRequest req )
      throws AccessControlException 
   {
      if(req!=null && req.isUserInRole("EprUser") && !"epr".equalsIgnoreCase(theme))
         throw new AccessControlException("The EprUser role can only view the epr theme");
   }

   /**
    * Check to see if we have permission to see this particular theme.
    * Specifically the EprUser is constrained to only be able to view
    * the 'epr' theme.  All other themes result in a SecurityException.
    */
   public void checkPermission( String theme, Subject s)
      throws AccessControlException  {
      if(s != null)
      {
         Set<Principal> principals = s.getPrincipals();
         for(Principal p : principals)
         {
            if(p.getName().equalsIgnoreCase("Roles"))
            {
               Group g = (Group)p;
               if(g.isMember(EPR_USER_ROLE) && !theme.equalsIgnoreCase("epr"))
                  throw new AccessControlException("The EprUser role can only view the epr theme");
            }
         }
      }
   }
}
