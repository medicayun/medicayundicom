/**
 * Tests the study data/XML object tests
 */
$unittest()$
$jsonModel()$
$studyData()$
$layout/layout()$
$sortPatient()$

var studyDataTests = new UnitTest();
console.info("testGsps"); 

var vlutTxt = "$vlutImageJSON();format="js"$";
var sortTxt = "$sortStudyJSON();format="js"$";

studyDataTests.testGsps = function StudyDataTests_gspsTest() { 
	var sd = new StudyData();
	sd.parseString(vlutTxt);
	assertNotNull(sd,"vlut study data");
	var pat = sd.patient[0];
	var patId = sd.children["VLUT_Pnn"];
	assertNotNull(pat,"VLUT Patient");
	assertEquals(pat,patId,"Patient child and patient by id must be the same.");
	var study = pat.study[0];
	assertEquals(pat.study.length,1,"Only 1 study.");
	assertEquals(study.series.length,13,"Should have 12 image series and 1 gsps series");
	var gspsSeries = study.children["PR:1.2.276.0.7230010.3.200.4"];
	assertNotNull(gspsSeries,"GSPS series must exist.");
	assertEquals(gspsSeries.gsps.length,12,"Must have 12 GSPS VLUT children.");
};

studyDataTests.testSort_parent = function StudyDataTests_sortTest_parent()  {
    var sd = new StudyData();
    sd.parseString(sortTxt);
    
    sd.searchLayout = new Layout();
    sd.searchLayout.search = sd;
    
    var sortPat = new SortPatient(sd.searchLayout);
    var pat = sd.searchLayout.search.patient;
    
    console.info("Asserting pre-sort order.");
    
    assertEquals(pat[0].PatientName,"HEMITHORAX","(unsorted) PatientName[0] should match");
    assertEquals(pat[1].PatientName, "SPONDYLDISCITIS^C6-C7","(unsorted) PatientName[1] should match");
    assertEquals(pat[2].PatientName, "LUXATION","(unsorted) PatientName[2] should match");
    
    var column = new Object();
    column.level = "1";
    column.sortType = "lexicographic";
    
    // Ascending
    column.sort = "PatientName";
    sortPat.sortColumn (column);
    
    console.info("Sorted ascending.");
    
    assertEquals(pat[0].PatientName,"HEMITHORAX","(Ascending) PatientName[0] should match");
    assertEquals(pat[1].PatientName , "LUXATION","(Ascending) PatientName[1] should match");
    assertEquals(pat[2].PatientName , "SPONDYLDISCITIS^C6-C7","(Ascending) PatientName[2] should match");

    // Ascending
    column.sort = "PatientID";
    sortPat.sortColumn (column);

    console.info("Sorted descending.");
    
    assertEquals(pat[0].PatientID , "12", "(Ascending) PatientID[0] should match");
    assertEquals(pat[1].PatientID , "66", "(Ascending) PatientID[1] should match");
    assertEquals(pat[2].PatientID , "93", "(Ascending) PatientID[2] should match");

    // Descending
    column.sort = "PatientName";
    sortPat.sortColumn (column);
    
    assertEquals(pat[0].PatientName , "SPONDYLDISCITIS^C6-C7","(Descending) PatientName[0] should match");
    assertEquals(pat[1].PatientName , "LUXATION","(Descending) PatientName[1] should match");
    assertEquals(pat[2].PatientName , "HEMITHORAX","(Descending) PatientName[2] should match");
    
    
    // Descending
    column.sort = "PatientID";
    sortPat.sortColumn (column);
    
    assertEquals(pat[0].PatientID, "93", "(Descending) PatientID[0] should match");
    assertEquals(pat[1].PatientID , "66", "(Descending) PatientID[1] should match");
    assertEquals(pat[2].PatientID , "12", "(Descending) PatientID[2] should match");
};



studyDataTests.testSort_child = function StudyDataTests_sortTest_child()  {
    var sd = new StudyData();
    sd.parseString(sortTxt);
    sd.searchLayout = new Layout();
    sd.searchLayout.search = sd;
    
    var sortPat = new SortPatient(sd.searchLayout);
    
    var pat = sd.searchLayout.search.patient;
    
    assertEquals(pat[0].study.length,3,"There must be 3 studies for the first patient");
    assertEquals(pat[0].study[0].StudyDescription , "RX THORAX_Z", "(unsorted) StudyDescription [0,0] should match");
    assertEquals(pat[0].study[1].StudyDescription , "RX THORAX_A", "(unsorted) StudyDescription [0,1] should match");
    assertEquals(pat[0].study[2].StudyDescription , "RX THORAX_A", "(unsorted) StudyDescription [0,2] should match");

    assertEquals(pat[1].study[0].StudyDescription , "RX CWZ_T", "(unsorted) StudyDescription [1,0] should match");
    assertEquals(pat[1].study[1].StudyDescription , "RX SHOULDER", "(unsorted) StudyDescription [1,1] should match");
    assertEquals(pat[1].study[2].StudyDescription , "RX CDWZ_S", "(unsorted) StudyDescription [1,2] should match");
    
    assertEquals(pat[2].study[0].StudyDescription , "RX SHOULDER LEFT_K", "(unsorted) StudyDescription [2,0] should match");
    assertEquals(pat[2].study[1].StudyDescription , "RX SHOULDER LEFT_N", "(unsorted) StudyDescription [2,1] should match");
    assertEquals(pat[2].study[2].StudyDescription , "RX UPPERARM LEFT", "(unsorted) StudyDescription [2,2] should match");

    var column = new Object();
    column.level = "2";
    column.sortType = "lexicographic";
    
    // Ascending    
    column.sort = "StudyDescription";
    sortPat.sortColumn (column);
    
    assertEquals(pat.length,3, "Should be three patients");
    assertEquals(pat[0].study.length,3,"Should be three studies in the first patient");
    assertEquals(pat[0].study[0].StudyDescription , "RX CDWZ_S", "(ascending) StudyDescription [0,0] should match");
    assertEquals(pat[0].study[1].StudyDescription , "RX CWZ_T", "(ascending) StudyDescription [0,1] should match");
    assertEquals(pat[0].study[2].StudyDescription , "RX SHOULDER", "(ascending) StudyDescription [0,2] should match");

    assertEquals(pat[1].study[0].StudyDescription , "RX SHOULDER LEFT_K", "(ascending) StudyDescription [1,0] should match");
    assertEquals(pat[1].study[1].StudyDescription , "RX SHOULDER LEFT_N", "(ascending) StudyDescription [1,1] should match");
    assertEquals(pat[1].study[2].StudyDescription , "RX UPPERARM LEFT", "(ascending) StudyDescription [1,2] should match");

    assertEquals(pat[2].study[0].StudyDescription , "RX THORAX_A", "(ascending) StudyDescription [2,0] should match");
    assertEquals(pat[2].study[1].StudyDescription , "RX THORAX_A", "(ascending) StudyDescription [2,1] should match");
    assertEquals(pat[2].study[2].StudyDescription , "RX THORAX_Z", "(ascending) StudyDescription [2,2] should match");
    
    // Ascending
    column.sort = "StudyDateTime";
    sortPat.sortColumn (column);

    assertEquals(pat[0].study[0].StudyDateTime , "20011024112650.000000", "(ascending) StudyDateTime [0,0] should match");
    assertEquals(pat[0].study[1].StudyDateTime , "20011107095413.000000", "(ascending) StudyDateTime [0,1] should match");
    assertEquals(pat[0].study[2].StudyDateTime , "20011108085255.000000", "(ascending) StudyDateTime [0,2] should match");

    assertEquals(pat[1].study[0].StudyDateTime , "20020116122742.000000", "(ascending) StudyDateTime [1,0] should match");
    assertEquals(pat[1].study[1].StudyDateTime , "20020116133450.000000", "(ascending) StudyDateTime [1,1] should match");
    assertEquals(pat[1].study[2].StudyDateTime , "20020124143405.000000", "(ascending) StudyDateTime [1,2] should match");
    
    
    assertEquals(pat[2].study[0].StudyDateTime , "20020304181031.000000", "(ascending) StudyDateTime [2,0] should match");
    assertEquals(pat[2].study[1].StudyDateTime , "20020304181250.000000", "(ascending) StudyDateTime [2,1] should match");
    assertEquals(pat[2].study[2].StudyDateTime , "20020308091506.000000", "(ascending) StudyDateTime [2,2] should match");
        
    // Descending    
    column.sort = "StudyDescription";
    sortPat.sortColumn (column);
    
    assertEquals(pat[0].study[0].StudyDescription , "RX UPPERARM LEFT", "(descending) StudyDescription [0,0] should match");
    assertEquals(pat[0].study[1].StudyDescription , "RX SHOULDER LEFT_N", "(descending) StudyDescription [0,1] should match");
    assertEquals(pat[0].study[2].StudyDescription , "RX SHOULDER LEFT_K", "(descending) StudyDescription [0,2] should match");

    assertEquals(pat[1].study[0].StudyDescription , "RX THORAX_Z", "(descending) StudyDescription [1,0] should match");    
    assertEquals(pat[1].study[1].StudyDescription , "RX THORAX_A", "(descending) StudyDescription [1,1] should match");
    assertEquals(pat[1].study[2].StudyDescription , "RX THORAX_A", "(descending) StudyDescription [1,2] should match");
    
    assertEquals(pat[2].study[0].StudyDescription , "RX SHOULDER", "(descending) StudyDescription [2,0] should match");
    assertEquals(pat[2].study[1].StudyDescription , "RX CWZ_T", "(descending) StudyDescription [2,1] should match");
    assertEquals(pat[2].study[2].StudyDescription , "RX CDWZ_S", "(descending) StudyDescription [2,2] should match");
    
    
    // Descending
    column.sort = "StudyDateTime";
    sortPat.sortColumn (column);

    assertEquals(pat[0].study[0].StudyDateTime , "20020308091506.000000", "StudyDateTime [0,0] should match");
    assertEquals(pat[0].study[1].StudyDateTime , "20020304181250.000000", "StudyDateTime [0,1] should match");
    assertEquals(pat[0].study[2].StudyDateTime , "20020304181031.000000", "StudyDateTime [0,2] should match");

    assertEquals(pat[1].study[0].StudyDateTime , "20020124143405.000000", "StudyDateTime [1,0] should match");
    assertEquals(pat[1].study[1].StudyDateTime , "20020116133450.000000", "StudyDateTime [1,1] should match");
    assertEquals(pat[1].study[2].StudyDateTime , "20020116122742.000000", "StudyDateTime [1,2] should match");

    assertEquals(pat[2].study[0].StudyDateTime , "20011108085255.000000", "StudyDateTime [2,0] should match");
    assertEquals(pat[2].study[1].StudyDateTime , "20011107095413.000000", "StudyDateTime [2,1] should match");
    assertEquals(pat[2].study[2].StudyDateTime , "20011024112650.000000", "StudyDateTime [2,2] should match");
};

studyDataTests.runUnitTests();

 