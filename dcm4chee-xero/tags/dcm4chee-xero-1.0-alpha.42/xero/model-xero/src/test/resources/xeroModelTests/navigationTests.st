$unittest()$
$merged()$
$navConversation()$
$studyData()$
$jsonModel()$
$navigation()$
function XeroModel() {};
$displayAndFindModel()$

navigationTests = new UnitTest();

navigationTests.testCreateNavigation = function() {
	var nav = new Navigation();
};

function CreateTestStudyData(patId, studyIds) {
	var sd = new StudyData();
};

var twoStudy = "$twoStudyJSON();format="js"$";
var prStudy = "$prStudyJSON();format="js"$";
var images0 = "$study0ImagesJSON();format="js"$";
var images1 = "$study1ImagesJSON();format="js"$";

var studyData = new StudyData();
studyData.parseString(twoStudy);
var patient = studyData.patient[0];
var patid = ""+patient.PatientIdentifier;
var study0 = patient.study[0];
var study1 = patient.study[1];
var stuid0 = ""+study0.studyUID;
var stuid1 = ""+study1.studyUID;
console.info("Study id 0 %s", stuid0);
console.info("Study id 1 %s", stuid1);

/** Sets the patient level navigation, optinally the study nav
 * uids, and optionally sets up the studyData to the given xmlText
 */
navigationTests.init = function(uids, xmlText) {
	this.nav = new Navigation();
	this.nav.setNavigation(patid);
	this.pat = this.nav.getChild(0);
	if(uids) {
		this.pat.setNavigation(uids,"study");
		console.info("Set navigation to %s", uids);
	}
	this.patS0 = this.pat.getChild(0);
	this.patS1 = this.pat.getChild(1);
	this.studyData = new StudyData();
	if( xmlText ) this.studyData.parseString(xmlText);
};

/** Tests that the navigation object will deliver the correct study query URLs
 */
navigationTests.testStudyQuery = function() {
	var nav = new Navigation();
	nav.setNavigation(patid);
	var patNav = nav.getChild(0);
	var uids = [stuid0, stuid1];	
	patNav.setNavigation(uids,"study");
	console.info("Set navigation to %s", uids);
	var c0 = patNav.getChild(0);
	var c1 = patNav.getChild(1);
	$assertTrue(condition="c0.id==stuid0")$;
	$assertTrue(condition="c1.id==stuid1")$;
	studyData = new StudyData();
	studyData.parseString(twoStudy);
	var urls = nav.mergeExternal(studyData);
	$assertTrue(condition="urls==null",msg="URLs must be null initially without anyone being interested in studies")$
	var se00 = c0.getChild(0);
	var se10 = c1.getChild(0);
	var im000 = se00.getChild(0);
	var im100 = se10.getChild(0);
	var im10n = se10.getChild(10000);
	urls = nav.mergeExternal(studyData);
	$assertTrue(condition="urls!=null",msg="Must require more URLs after adding studies to view.")$;
	var cnt = 0;
	for(key in urls) {
		console.info("Looking for URL %s", key);
		cnt++;
	}
	$assertTrue(condition="cnt==2",msg="Must have 2 studies to retrieve information for.")$;
	studyData.parseString(images0);
	studyData.parseString(images1);
	urls = nav.mergeExternal(studyData);
	$assertTrue(condition="urls==null",msg="Urls must be null after supplying all data.")$
	$assertTrue(condition="se00.id!=null")$
	$assertTrue(condition="se00.external!=null")$
	$assertTrue(condition="im000.id!=null")$
	$assertTrue(condition="im000.external!=null")$
};

/** 
 * Tests that the getExtChildren will:
 * 1. Use the external named child type if no other data is provided
 * 2. Use the conversational data in preference if it is available
 */
navigationTests.test_getExtChildren = function() {
	this.init([stuid0,stuid1], twoStudy);
	
	$assertTrue(condition="this.patS0.id==stuid0")$;
	$assertTrue(condition="this.patS1.id==stuid1")$;
	
	
	this.nav.mergeExternal(this.studyData);

	var topChild = this.nav.getExtChildren();
	$assertTrue(condition="topChild!=null",msg="getExtChildren must get the external data when none other provided.")$;
	
	var studyChildren = this.pat.getExtChildren();
	$assertEquals(expected="2", actual="studyChildren.length",msg="Must have 2 study children from results.")$;
	$assertEquals(expected="stuid0", actual="studyChildren[0]", msg="Must use the conversational data in preference.")$;
};

/**
 * Tests the getCustomKey gets the conversation key and the custom series key.
 * Ensures that study level computes the series list of items containing images,
 * not all items, and that otherwise the conversation object is returned if
 * available, or null/undefined.
 */
navigationTests.test_getCustomChildren = function() {
	this.init(null, images0);

    // Check that nothing is returned if a custom list isn't defined	
	var studyChildren = this.pat.getCustomChildren();
	$assertTrue(condition="studyChildren==null",msg="No custom study children until navigation is set.")$;

	this.pat.setNavigation([stuid0,stuid1],"study");
	this.nav.mergeExternal(this.studyData);

    // Check that a custom, specified list is returned.	
	studyChildren = this.pat.getCustomChildren(this.pat.childType);
	$assertTrue(condition="studyChildren!=null",msg="Once conversational study is set, should be available")$;
	
	// Check that the series which contain images are returned.
	var seriesChildren = this.patS0.getCustomChildren(this.patS0.childType);
		console.info("seriesChildren.length"+seriesChildren.length);
	$assertTrue(condition="seriesChildren.length===9",msg="Must have 9 series children.")$;
};
 
/**
 * Tests that the lookupId function finds the right id either as an offset into images,
 * an offset into other ids, or a fixed offset after a given item name.
 */
navigationTests.test_lookupId = function() {
	var nav = new Navigation();
	nav.setNavigation(patid);
	var patNav = nav.getChild(0);
	var uids = [stuid0];	
	var s0 = patNav.getChild(0);
	var s0se0 = s0.getChild(0);
	var s0se0i0 = s0se0.getChild(0)
	
	studyData = new StudyData();
	studyData.parseString(images0);
	nav.mergeExternal(studyData);
	console.info("Set navigation to %s", uids);

	console.info("s0.id=",s0.id);
	console.info("s0se0.id=",s0se0.id);
	console.info("s0se0i0.id=",s0se0i0.id);
	
	var seuid = "1.3.51.0.7.99.2160121055.1018.1012301819.1";
	$assertTrue(condition="patNav.id==patid",msg="Patient navigation lookup id is incorrect.")$;
	$assertTrue(condition="s0.id==stuid0",msg="Study UID in navigation object is incorrect.")$;
	$assertTrue(condition="s0se0.id==seuid",msg="Series UID in navigation is incorrect.")$;
};
 
/**
 * Tests that the modified query is used when there is a gspsLabel to apply. 
 */
navigationTests.testModifiedQuery = function() {
	var nav = new Navigation();
	nav.setNavigation(patid);
	var patNav = nav.getChild(0);
	var uids = [stuid0];	
	patNav.setNavigation(uids,"study");
	console.info("Set navigation to %s", uids);
	var c0 = patNav.getChild(0);
	assertEquals(c0.id,stuid0,"c0.id");
	studyData = new StudyData();
	studyData.parseString(prStudy);
	var urls = nav.mergeExternal(studyData);
	$assertTrue(condition="urls==null",msg="URLS must be null since no request has been made below that level")$;
	var se00 = c0.getChild(0);
	var im000 = se00.getChild(0);
	urls = nav.mergeExternal(studyData);
	$assertTrue(condition="urls!=null",msg="URLS must not be null now.")$;
	var cnt = 0;
	var modifier;
	for(key in urls) {
		console.info("Looking for URL %s", key);
		modifier = urls[key];
		$assertTrue(condition="modifier!==key",msg="Modifier and key must not be the same object.")$;
		cnt++;
	}
	$assertTrue(condition="cnt===1",msg="Must have 1 url requested for pr test.")$;

	studyData.parseString(images0,modifier);
	console.info("testModifiedQuery merging study data external information.");
	urls = nav.mergeExternal(studyData);
	if( urls ) {
		for(var i in urls) {
			console.info("i=",i,"=",urls[i]);
		}
	}
	$assertTrue(condition="urls==null",msg="Urls must be null after supplying all data.")$;
	$assertTrue(condition="se00.id!=null",msg="se00.id should be defined")$;
	$assertTrue(condition="se00.external!=null",msg="se00.external should be available")$;
};

/** All of the event setup uses a set of listeners that just records the responses */
function TestEventListener(events) {
	this.events = events;
	this.navigateEvent = new Array();
	this.namedEvent = new Array();
	this.children = new Array();
};
TestEventListener.prototype.navigateListener = function (navigate) {
	this.navigateEvent.push(navigate);
};
TestEventListener.prototype.namedListener = function (navigate, varname, newVal, oldVal) {
	this.namedEvent.push({"navigate":navigate,"varname":varname, "oldVal":oldVal,"newVal":newVal});
};
TestEventListener.prototype.clear = function() {
	this.navigateEvent = new Array();
	this.namedEvent = new Array();
	var i,n=this.children.length;
	for(i=0; i<n;i++) {
		this.children[i].clear();
	};
};

var rootEvent = new TestEventListener([".", "/conversation:mode"]);
rootEvent.navigate = "patient:0";
var studyEvent = new TestEventListener([".", "../conversation:showReport", "/patient:0/merged:windowLevel"]);
studyEvent.navigate = "study:0";
rootEvent.children.push(studyEvent);

/** Tests navigation level changes */
navigationTests.test_setNavigation_expectEvent = function () {
	var nav = new Navigation();
	nav.hook(rootEvent);
	nav.setNavigation(patid);
	studyData = new StudyData();
	studyData.parseString(prStudy);
	var urls = nav.mergeExternal(studyData);
	$assertTrue(condition="rootEvent.navigateEvent.length>0")$;
	$assertTrue(condition="studyEvent.navigateEvent.length>0")$;
	rootEvent.clear();
};

/** Tests conversational (non-nav) events/changes */
navigationTests.test_setConversation_expectEvent = function test_setConversation_expectEvent() {
	var nav = new Navigation();
	nav.hook(rootEvent);
	nav.setNavigation(patid);
	studyData = new StudyData();
	studyData.parseString(prStudy);
	nav.mergeExternal(studyData);
	rootEvent.clear();
	
	nav.setConversation("mode", "wl");
	$assertTrue(condition="rootEvent.namedEvent.length>0")$;
	rootEvent.clear();
	console.info("About to set showReport to true.");
	nav.getChild(0).setConversation("showReport", "true");
	$assertTrue(condition="studyEvent.namedEvent.length>0")$;
	rootEvent.clear();
};

/** Tests merged events/changes */
navigationTests.test_setMerged_expectEvent = function () {
	var nav = new Navigation();
	nav.hook(rootEvent);
	nav.setNavigation(patid);
	studyData = new StudyData();
	studyData.parseString(prStudy);
	nav.mergeExternal(studyData);
	rootEvent.navigateEvent = new Array();
	studyEvent.navigateEvent = new Array();
	rootEvent.clear();
	
	nav.getChild(0).setMerged("windowLevel", 129);
	$assertTrue(condition="studyEvent.namedEvent.length>0")$;
	rootEvent.clear();
};

