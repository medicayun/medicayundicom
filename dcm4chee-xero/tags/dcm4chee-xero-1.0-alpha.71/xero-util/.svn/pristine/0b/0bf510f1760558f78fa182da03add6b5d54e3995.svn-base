package org.dcm4chee.xero.template;

import java.util.Map;

import org.dcm4chee.xero.metadata.MetaDataBean;
import org.dcm4chee.xero.metadata.StaticMetaData;
import org.dcm4chee.xero.metadata.access.MapWithDefaults;
import org.testng.annotations.Test;
import org.antlr.stringtemplate.StringTemplate;


/**
 * Tests that the list of all templates has the correct set of child items.
 * @author bwallace
 *
 */
public class AllTemplatesMapTest {
	@SuppressWarnings("unchecked")
	@Test
	public void testAllTemplates() {
		  MetaDataBean root = StaticMetaData.getMetaData("util-test.metadata");
		  MetaDataBean mdb = root.getChild("model");
		  MapWithDefaults mwd = new MapWithDefaults(mdb);
		  Map<String,StringTemplate> templates = (Map<String,StringTemplate>) mwd.get("templates");
		  assert templates!=null;
		  assert templates.containsKey("simpleMacro");
		  assert templates.containsKey("allTemplatesMapTest");
	}

}
