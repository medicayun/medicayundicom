$unittest()$
$navConversation()$

var navConversationTests = new UnitTest();

navConversationTests.testCreateNavConversation = function() {
	var nc = new NavConversation();
};

/** Tests that the id/position information is correct for a simple child element with no modifications/changes */
navConversationTests.testSimpleChild = function() {
	var nc = new NavConversation();
	var c1 = nc.getChild("mypid");
	assertNotNull(c1,"c1");
	assertEquals(c1.id,"mypid","mypid", "base id");
	assertEquals(c1.getMergedId(),"mypid", "match id");
	assertEquals(c1.getExternalId(),"mypid", "external id");
	assertEquals(c1.queryModifier,"", "queryModifier");
};

/** Tests that the id/position information is correct for an extended child with all of presentation changes,
 * navigation changes and a copy of the basic object 
 */
navConversationTests.testComplexChild = function() {
	var nc = new NavConversation();
	var c1 = nc.getChild("mypid&c=1");
	var prMod = "&gsps=g";
	var koMod = "&koUID=2";
	c1.setQueryModifier("pr",prMod,true);
	c1.setQueryModifier("ko",koMod,false);
	assertNotNull(c1,"c1");
	assertEquals(c1.id,"mypid&c=1", "base id");
	assertEquals(c1.getMergedId(),"mypid&c=1&gsps=g", "match id");
	var ui = c1.getExternalId();
	if( (ui!="mypid"+prMod+koMod) && (ui!="mypid"+koMod+prMod) ) {
		fail("UrlId must be mypid + "+presMod+" + "+ queryMod +" in either order, but is "+ui);
	};	
};

navConversationTests.runUnitTests();