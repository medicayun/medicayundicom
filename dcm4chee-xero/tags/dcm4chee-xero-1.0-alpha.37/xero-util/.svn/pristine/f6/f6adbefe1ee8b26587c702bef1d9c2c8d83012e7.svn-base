
function XmlModelTest() {
	console.info("Starting XmlModelTest");
	var txt = '<results><patient PatientName="Fred"><study StudyId="1" /><study StudyId="2" /></patient></results>';
	var domparser = new DOMParser();
	var xml = domparser.parseFromString(txt,"text/xml");
	console.assert(xml);
	console.assert(xml.documentElement);
	console.info("About to create the xml model.");
	var obj = new XmlModel();
	console.info("Done creating the model.");
	obj.parse(xml.documentElement);
	console.info("Done parsing the XML.");
	console.assert(obj.patient!==undefined,"results object must contain a patient.");
};


XmlModelTest();

