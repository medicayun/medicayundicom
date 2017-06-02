XeroModel.prototype.init = function()
{
    console.info("Called XeroModel.init function");
	var findLayout = new FindLayout("",this.ae);

	this.layout = new TabsLayout("XeroRootTab",this.i18n);
	this.layout.add(new XeroRootMenu(),"tabMenu");
	
	this.layout.addTab("Find", findLayout);
	this.patientLayout = new PatientLayout();
	this.layout.addTab("Display", this.patientLayout);
	
	// Make the idMap global - in case there end up being multiple display areas...
	this.idMap = new LayoutIdMap();
	this.layout.computeId(this.idMap);
	this.idMap.i18n = this.i18n;
	this.idMap.IS_IE = this.IS_IE;

	if( ! this.idMap.QueryLayout ) throw "Query Layout is null";
	 	    
};

/** The pieces from here to the end of this file are very ugly as they need to be replicated
 * server and client side - it would be far preferable to have a single definition that works
 * for both sides, and can generate computed/linked/nested data, as well as embed plugin information
 * from the server side and information about what to call to setup various bits of data.  The
 * actual setup should be lazy if at all possible.
 * See studyColumns.xml in view-xero for the server side instance.
 */
var XeroStudyColumns = {
	"template":"xeroStudyColumns",
	"header":[
		{"i18n":"PatientName", "var":"patientNameF",  "sort":"PatientName", "template":"query/patientNameSel","id":"patientNameColHead", "level":"1", "sortType":"lexicographic" },
		{"i18n":"PatientID", "var":"PatientID", "sort":"PatientID", "template":"html/displayFirst","id":"patientIdColHead", "level":"1", "sortType":"lexicographic" },
		{"i18n":"PatientBirthDate", "var":"birthDateF", "sort":"PatientBirthDate", "template":"html/displayFirst", "id":"birthDateColHead", "level":"1", "sortType":"lexicographic" },
		{"i18n":"PatientSex", "var":"PatientSex", "sort":"PatientSex", "template":"html/displayFirst", "id":"patientSexColHead", "level":"1", "sortType":"lexicographic" },
		{"i18n":"StudyDescription", "var":"StudyDescription", "sort":"StudyDescription", "template":"query/studyDescriptionSel","id":"studyDescColHead", "level":"2", "sortType":"lexicographic" },
		{"i18n":"Modality", "var":"ModalitiesInStudy", "sort":"ModalitiesInStudy", "template":"html/displayIt", "id":"modalityColHead", "level":"2", "sortType":"lexicographic" },
		{"i18n":"AccessionNumber", "var":"AccessionNumber", "sort":"AccessionNumber", "template":"html/displayIt", "id":"accessionNumberColHead", "level":"2", "sortType":"lexicographic" },
		{"i18n":"StudyDateTimeF", "var":"StudyDateF", "sort":"StudyDateTime", "template":"html/displayIt", "id":"studyDateTimeColHead", "level":"2", "sortType":"lexicographic" },
		//{"i18n":"InstanceAvailability", "var":"InstanceAvailability", "sort":"InstanceAvailability", "template":"html/displayIt", "id":"instanceAvailabilityColHead", "level":"2", "sortType":"numeric" },
		{"name":"# Objects", "var":"NumberOfStudyRelatedInstances", "sort":"NumberOfStudyRelatedInstances", "template":"html/displayIt", "id":"numObjectsColHead", "level":"2", "sortType":"numeric" }
	]
};

// TODO - remove this - it is a bit of test code being used temporarily.
var i=3;

var BASE_QUERY="/wado2/?v=$version$";
XeroModel.prototype.BASE_QUERY = BASE_QUERY;
var STUDY_QUERY=BASE_QUERY+"&requestType=STUDY&contentType=text/javascript";
var IMAGE_QUERY=BASE_QUERY+"&requestType=IMAGE&contentType=text/javascript";
var XERO_QUERY=BASE_QUERY+"&requestType=XERO";
var WADO_QUERY=BASE_QUERY+"&requestType=WADO";
XeroModel.prototype.WADO_QUERY=WADO_QUERY;
