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
 * Portions created by the Initial Developer are Copyright (C) 2007
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
package org.dcm4chee.xero.metadata.jndi;

import java.util.HashMap;
import java.util.Map;

//import javax.naming.Context;
//import javax.naming.InitialContext;
//import javax.naming.NamingEnumeration;

import org.dcm4chee.xero.metadata.MetaDataBean;
//import org.jboss.ejb3.embedded.EJB3StandaloneBootstrap;
//import org.jboss.ejb3.embedded.EJB3StandaloneDeployer;
import org.testng.annotations.Test;

/** This class tests jndi meta-data information lookup.  
 * It currently does not test lists of JNDI objects and the like, and it probably
 * should do so.
 * @author bwallace
 */
public class JndiMetaDataTest {
	static Map<String, Object> prop = new HashMap<String, Object>();
	static {
		prop.put("metaDataProvider.jndi",
				"${org.dcm4chee.xero.metadata.jndi.JndiMetaDataProvider}");
		prop.put("metaDataProvider.jndi.priority", "5");
		prop.put("metaDataProvider.jndi.path", "metadata");
		prop.put("valueProvider.jndi",
				"${org.dcm4chee.xero.metadata.jndi.JndiValueProvider}");
		prop.put("testjndi", "jndi://metadata/TestBean/Local");
	}

	/*
	//@BeforeSuite
	public void startup() throws Exception {
	 	EJB3StandaloneBootstrap.boot(null);
		EJB3StandaloneBootstrap.scanClasspath();
		EJB3StandaloneDeployer deployer = new EJB3StandaloneDeployer();
		deployer.setKernel(EJB3StandaloneBootstrap.getKernel());
		deployer.create();
		
		Context context = new InitialContext();
		String loc = "/metadata";
		NamingEnumeration ne = context.list(loc);
		while(ne.hasMoreElements()) {
			Object val = ne.next();
			System.out.println("Found child value of "+loc +" "+val);
		}
	}
	*/

	static MetaDataBean mdb;

	/** This tests to ensure that JNDI objects can be correctly looked up, both
	 * as direct values, and as values from a value-provider.
	 */
	@Test(enabled=false)
	public void testJndiLookup() {
		if (mdb == null)
			mdb = new MetaDataBean(prop);
		assert mdb.getValue("testjndi")!=null;
		assert mdb.getValue("testjndi") instanceof TestLocal;
		assert mdb.getValue("TestBean")!=null;
		assert mdb.getValue("TestBean") instanceof TestLocal;
	}

	/** This tests that JNDI objects can correctly contribute to the meta-data */
	@Test(enabled=false)
	public void testJndiMetaDataProvider() {
		if (mdb == null)
			mdb = new MetaDataBean(prop);
		assert mdb.get("TestBean")!=null;
		assert mdb.getValue("TestBean")!=null;
		assert mdb.getChild("TestBean").getChild("jndiValue")!=null;
		assert mdb.getValue("TestBean.jndiValue")!=null;
		// This isn't get a property of the bean, but is rather getting
		// the meta-data associated with the bean - these don't have to agree,
		// although usually they should.
		assert mdb.getValue("TestBean.jndiValue").equals("jndiValue");
	}

}
