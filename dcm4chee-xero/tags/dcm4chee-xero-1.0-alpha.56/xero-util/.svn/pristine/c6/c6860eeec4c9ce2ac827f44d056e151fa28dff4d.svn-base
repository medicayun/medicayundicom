package org.dcm4chee.xero.metadata.access;

import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

import org.testng.annotations.Test;

/** Checks to see that resource bundle maps return the correct keys */
public class ResourceBundleMapTest {

	ClassLoader cl = Thread.currentThread().getContextClassLoader();
	ResourceBundle rb = ResourceBundle.getBundle("i18n",new Locale("en"), cl);
	static final Properties lazy = new Properties();
	ResourceBundleMap rbm = new ResourceBundleMap(rb,lazy);
	
	static {
		lazy.put("name.icon", "Icon Name");
	};
	
	@Test
	public void test_get_returns_value() {
		assert rbm.get("name.label").equals("Name");
		assert rbm.get("name.icon").equals("Icon Name"); 
	}
	
	@Test
	public void test_iteration() {
		int count = 0;
		for(Map.Entry<String,Object> me : rbm.entrySet() ) {
			count++;
			assert me!=null;
		}
		System.out.println("Count="+count);
		assert count==4;
	}
	
}
