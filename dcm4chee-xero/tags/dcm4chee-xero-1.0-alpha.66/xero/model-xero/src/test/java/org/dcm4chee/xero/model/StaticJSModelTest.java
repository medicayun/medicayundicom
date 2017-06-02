package org.dcm4chee.xero.model;

import java.util.List;
import java.util.Map;

import org.dcm4chee.xero.metadata.MetaDataBean;
import org.dcm4chee.xero.metadata.StaticMetaData;
import org.dcm4chee.xero.metadata.filter.FilterUtil;
import org.dcm4chee.xero.test.JSTemplate;
import org.testng.annotations.Test;

/**
 * This tests the plugins for the static data - ensuring that meta-data is plugged into the various 
 * model elements correctly.
 * The data MUST be plugged into the "static" model to be useable in that way.
 * 
 * @author bwallace
 *
 */
public class StaticJSModelTest {

	MetaDataBean mdb = StaticMetaData.getMetaData("test-model.metadata");
	MetaDataBean stat = mdb.getChild("static");
	JSTemplate jst = new JSTemplate(stat, "rhinoAccess","xeroModelTests", "xeroModel", "xeroTest");

	/**
	 * Tests that the scripts value is well defined and contains the value required for the script plugin test
	 */
	@Test
	public void valueScriptPluginTest() {
		assert stat!=null;
		
		Map<String,Object> model = jst.getModel();
		assert model!=null;
		
		List<?> scripts = (List<?>) model.get("scripts");
		assert scripts!=null;
		assert scripts.contains("js/sarissa.js");
	}
	
	/**
	 * Tests that scripts can be correctly plugged in.
	 * This runs all the tests in the staticPluginTest
	 */
	@Test
	public void staticPluginTest() {
		runTest("staticPluginTest");
	}
	
	@Test
	public void modelInterpretTest() {
		runTest("modelInterpretTest");
	}
	
	@Test
	public void test_overallMenuLayout_children_available() {
		Map<?,?> model = (Map<?,?>) stat.getValue();
		assert FilterUtil.getPath(model,"overallMenuLayout")!=null;
		List<?> children = (List<?>) FilterUtil.getPath(model,"overallMenuLayout.children");
		assert children.get(0)!=null;
	}
	
	@Test
	public void js_variable_accessible() {
		Map<?,?> model = (Map<?,?>) stat.getValue();
		//AutoStringTemplateGroup astg = (AutoStringTemplateGroup) stat.getChild("js_static").getValue("script");
		//System.out.println("script template="+astg.getTemplate());
		//System.out.println("script="+stat.getChild("js_static").getValue("script"));
		assert model.get("js_static")!=null;
		assert model.get("i")!=null;
		System.out.println("i="+model.get("i"));
		assert model.get("i").equals(3);
	}
	
	/** Uses the static meta-data as the model and then just calls the generic  
	 * runTest for JavaScript.
	 * @param jsname
	 */
	public void runTest(String jsname) {
		jst.runTest(jsname);
	}
	public void runTestD(String jsname) {
		jst.runTest(jsname,true);
	}
}
