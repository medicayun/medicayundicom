/**
 * Tests the study data code at various levels.
 */

function studyDataTest() { 
	// Check to see that a patient/study/series object can be parsed - these are provided by the test
	// environment as seriesXml and image<N>Xml
	var studyData = new StudyData();
	studyData.setSeriesXml(seriesXml);
	
	// No customized data yet.
	studyData.setStudyCustomXml("<results />");
	
	// No positional data yet.
	studyData.setPositionXml("<results />");
	
	// Check to see that getting the position key on the patient level object 
	// returns the 0,1,2 etc study.  Note that at this point, there is
	// no assumption that multiple patients can be viewed simultaneously - they
	// are all treated as though they were a single patient if there is more than
	// one specified (this can happen if they are "linked" somehow.)
	var study0 = studyData.getStudy(studyData.DEFAULT_KEY, 0);
	console.assert(study0,"Must be able to find an offset study information.");
	var study1 = studyData.getStudy(studyData.DEFAULT_KEY, 1);
	console.assert(study1);
	console.assert(study1!=study0);
	var studyn = studyData.getStudy(studyData.DEFAULT_KEY,1000);
	console.assert(!studyn,"Must not define absent studies.");
	
	// Check to see the series level data can be fetched from the study level data.
	var series0 = study0.getSeries(study0.DEFAULT_KEY,0);
	console.assert(series0,"Must define a series.");
	
	// Check to see that image level data can be fetched - this will cause an internal
	// fetch to occur for the given study position.
	// No guarantee that getImage actually returns an image - it might return a 
	// GSPS or other type of object if the series was fetched with a non-image key. 
	var image0 = series0.getObject(series0.DEFAULT_KEY,0);
	var image64 = series0.getObject(series0.DEFAULT_KEY,64);
	console.assert(image0,"Image must be defined.");
	console.assert(image64,"Image 64 must be defined.");
	console.assert(image0.Position==0);
	console.assert(image64.Position==64);
	
	// Set the study position, see that it gets the right value.
	studyData.setPosition(studyData.DEFAULT_KEY,1);
	var study0b = studyData.getStudy(studyData.DEFAULT_KEY,0);
	console.assert(study0b==study1,"Study position must update the study positions.");

	// Get the over-ride object, set some values in it, test to see they are the right values,
	// and that the original over-rides haven't been touched.

	// Get the over-ride for the series level, and check to see that series level correctly
	// over-rides child level.

	// Check that "reset" operations - those that delete a specific set of values for both image and
	// series work.
};

studyDataTest();