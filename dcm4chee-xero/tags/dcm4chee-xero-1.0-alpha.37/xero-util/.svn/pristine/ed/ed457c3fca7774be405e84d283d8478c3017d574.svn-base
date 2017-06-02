package org.dcm4chee.xero.metadata.access;

import org.dcm4chee.xero.metadata.MetaDataBean;
import org.dcm4chee.xero.metadata.StaticMetaData;
import org.dcm4chee.xero.metadata.access.MapWithDefaults;
import org.testng.annotations.Test;

/** Tests that the map with defaults works as expected. */
public class MapWithDefaultsTest {

	/** Test the createion explicitly on a meta-data node */
	@Test
	public void testMap() {
		  MetaDataBean root = StaticMetaData.getMetaData("access.metadata");
		  MetaDataBean mdb = root.getChild("model");
		  MapWithDefaults mwd = new MapWithDefaults(mdb);
		  assert mwd.get("testKey").equals("testKeyValue");
		  assert mwd.get("doesNotExist")==null;
	}
	
	/** Test implicit creation from a meta-data node */
	@Test
	public void testImplicitMap() {
		  MetaDataBean root = StaticMetaData.getMetaData("access.metadata");
		  MetaDataBean mdb = root.getChild("model");
		  MapWithDefaults mwd = (MapWithDefaults) mdb.getValue();
		  assert mwd.get("testKey").equals("testKeyValue");
		  assert mwd.get("doesNotExist")==null;
		  assert mwd.get("testFactory")==TestFactory.staticValue;
		  assert mdb.getValue()!=mwd;
		  MetaDataBean stat = root.getChild("static");
		  assert stat.getValue()==stat.getValue();
	}
	
	/** Tests that meta-data children that have no value are returned as meta-data objects */
	@Test
	public void testNoValueChild() {
		  MetaDataBean root = StaticMetaData.getMetaData("access.metadata");
		  MetaDataBean mdb = root.getChild("model");
		  MapWithDefaults mwd = (MapWithDefaults) mdb.getValue();
		  assert mwd.get("shared")!=null;
		  assert mwd.get("shared") instanceof MetaDataBean;
	}
}
