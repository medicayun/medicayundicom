package org.dcm4chee.xero.template;

import org.antlr.stringtemplate.StringTemplate;
import org.dcm4chee.xero.metadata.servlet.CaptureSerlvetResponseItem;
import org.testng.annotations.Test;

/**
 * Tests that a StringTemplateResponseItem renders the correct response.
 * @author bwallace
 *
 */
public class StringTemplateResponseItemTest {

	@Test
	public void testStringTemplateResponseItem() throws Exception {
		AutoStringTemplateGroup stg = new AutoStringTemplateGroup();
		stg.setGroupNames("utilTestTemplates");
		StringTemplate st = stg.getInstanceOf("simpleMacro");
		assert st!=null;
		StringTemplateResponseItem stri = new StringTemplateResponseItem(st);
		CaptureSerlvetResponseItem csri = new CaptureSerlvetResponseItem(stri);
		assert csri.getStringContent().equals("Contents of simple macro.");
	}
}
