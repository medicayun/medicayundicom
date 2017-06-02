package org.dcm4chee.xero.model;

import java.util.Map;

import org.dcm4chee.xero.metadata.MetaDataBean;
import org.dcm4chee.xero.metadata.StaticMetaData;
import org.dcm4chee.xero.metadata.filter.FilterUtil;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** Tests that the dynamic javascript model objects are available/accessible */
public class DynamicJSModelTest {

	MetaDataBean mdb = StaticMetaData.getMetaData("test-model.metadata");
	MetaDataBean modelMdb = mdb.getChild("model");
	Map<?,?> model;
	
	@BeforeMethod
	public void init() {
		model = (Map<?, ?>) modelMdb.getValue();
	}

	@Test
	public void test_js_is_available() {
		Object jso = model.get("js");
		assert jso!=null;
		assert jso instanceof Map;
	}
	
	@Test
	public void test_js_layout_isAvailable() {
		Object layoutO = model.get("layout");
		assert layoutO!=null;
		Map<?,?> layout = (Map<?,?>)layoutO;
		assert layout.get("template").equals("html/tabs");
	}

	@Test
	public void test_js_other_layouts_available() {
		assert FilterUtil.getPath(model,"queryLayout.template").equals("query/query");
		assert FilterUtil.getPath(model,"searchLayout.template").equals("query/studyTable");
		assert FilterUtil.getPath(model,"findLayout.template").equals("html/layout");
	}
	
}
