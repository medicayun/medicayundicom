/**
 * This controller manages the search form and results
 */
 
 
/** 
 * Perform a search based on the indicated queryStr.  If queryStr is undefined then the
 * current query is refreshed. 
 */
XeroController.prototype.performSearch = function XC_performSearch() {
    var model = this.model;
	var queryLayout = model.idMap.QueryLayout;
	
	var url = queryLayout.query.url;
	this.userEvent("SearchQuery",url);
	console.time("SearchQuery");
	var searchLayout = model.idMap.SearchLayout;
	searchLayout.search = new StudyData();
	searchLayout.emptyCriteria = (url === queryLayout.query.base);
	searchLayout.isntServer = !isServer;
	 
	if( model.ae ) url = url+model.ae;
	if( !searchLayout.emptyCriteria ) {
		try { 
			searchLayout.search.requestUrl(this.service,url);
			searchLayout.hasError = null;
		} catch(e) {
			console.warn("Caught exception",e,"while loading query results.");
			searchLayout.hasError = this.getError(e);
		}
		searchLayout.noResults = (searchLayout.search.patient==null); 
		if( searchLayout.search.tooManyResults ) {
			console.warn("Too many results found from query",url);
		}
	}
	console.timeEnd("SearchQuery");
};

/** Causes the study query to be made immediately on loading - so that everything is available.
 * Also hooks up the de-activate listener for QueryLayout
 */
function onLoadStudySearch() {
	console.info("onLoadStudySearch.");
	controller.userEvent("onLoadStudySearch", "Initialize study search.");
	
	if( model.idMap.SearchLayout.search ) return;

	// Update the client based on the URL that has been passed.
	var url = document.location.search.substring(1);
	if(!url) return;

	controller.updateClient(url);

	controller.findLayout("QueryLayout").hideListener = function() {
		controller.storeScrollPosition("SearchLayout");
	};
};

// Client side load event to update the query data
addLoadEvent(onLoadStudySearch);


/** Adds a multiple study selection to allow viewing a sub-set of the available studies. */
XeroController.prototype.manyStudy = function(id, pid, studyUID) {
    this.userEvent("ManyStudy");
	var search = this.model.idMap.SearchLayout.search;
	if( !(search && search.children) ) {
		this.performSearch();
		search = this.model.idMap.SearchLayout.search;
	}
	var pat = search.children[pid];
	if(!pat) {
		log.warn("Didn't find patient.");
		return;
	}
	var study = pat.children[studyUID];
	if( !study ) {
		log.warn("Didn't find study UID.",studyUID);
		return;
	};
	study.isSelected = !pat.children[studyUID].isSelected;
	console.info("Selected study",studyUID,study.isSelected); 
};

/** Finds the search results and causes them to be displayed */
XeroController.prototype.findAction = function XC_findAction(src) {
	this.userEvent("findAction");
	var queryLayout = this.model.idMap.QueryLayout;
	var queryUrl = queryLayout.query.updateUrlFromForm();
	this.updateFindTab();
};



/** Sort the selected columns */
XeroController.prototype.sortQuery = function SC_sortQuery(src,event,col) {
  var lay = model.idMap.SearchLayout
  this.userEvent("sortQuery", col);
  if(! lay.search ) {
	this.performSearch();
  };
  lay.sortPatient.sortColumn(col);
  this.updateView("findAction");	
};

/** Submit the find form on enter press */
XeroController.prototype.find_key13 = function(e) {
	this.findAction();
};

/** Clear the query criteria */
XeroController.prototype.clearQuery = function(id) {
  	this.userEvent("ClearQuery");
	var lay = this.findLayout(id);
	var q = lay.query;
	// TODO - figure out a better way to list all the query fields.
	q.AccessionNumber="";
	q.ModalitiesInStudy=null;
	q.PatientID="";
	q.StudyDateTime=null;
	q.PatientName="";
	lay.relayout = true;
	this.waitUpdate("clearQuery");
};

// Update the client based on the passed URL
XeroController.prototype.updateClient = function(url) {
	// Populate the 'Find' tab with search results
	var queryLayout = this.findLayout("QueryLayout");
	queryLayout.query.updateQueryStr(url);
	queryLayout.query.updateFormFromURL(url);
	this.performSearch();

	if(this.model.i18n.InitialTab == "Display")
	{
		// Show the 'Display' tab first.
		var patients = this.findLayout("SearchLayout").search.patient;
		if(patients && patients.length > 0)
		{
			var pid = patients[0].PatientIdentifier;
			
			console.info("Displaying studies for PatientIdentifer="+pid);
			this.displayPatient('SearchLayout',pid);
		}
	}
};

XeroController.prototype.updateFindTab = function() {
	this.userEvent("FindTab");
	// Invoke a query based on the updated URL
	this.performSearch();
	
	var sl = this.findLayout("SearchLayout");
	if( sl ) {
	   sl.scrollTop = 0;
	}
	this.updateView("findAction");	
};

