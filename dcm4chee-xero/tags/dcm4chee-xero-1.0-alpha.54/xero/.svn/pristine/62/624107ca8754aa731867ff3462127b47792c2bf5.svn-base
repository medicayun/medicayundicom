/**
 * This controller extends the based XeroController with 
 * methods dealing with laying out the overall patient display.
 */

XeroController.prototype.service = new ServiceRequest();

 /** Displays all studies for the selected patient */
XeroController.prototype.displayPatient = function XC_displayPatient(id,patientId,uid) {
	waitCursor();
	this.userEvent("DisplayPatient");
	console.info("Display patientID: "+patientId+", studyUID="+uid);
	console.time("Load Patient Data");
	
	var model = this.model;
	var searchLayout = model.idMap.SearchLayout;
	if(! searchLayout.search ) {
		console.info("Search not yet populated.  Performing basic query");
		this.performSearch();
	}
	
	var patient = searchLayout.search.find("patient","PatientIdentifier", patientId);
	if(!patient) {
		console.warn("Unable to find patient: "+patientId);
		defaultCursor();
		// TODO - use the actual selected objects to read the patient IDs
		return;
	}
	// Record which patient was displayed so that a redisplay works
	searchLayout.search.displayedPatient = patientId;
	var study = patient.study;
	var i,n=study.length;
	model.studies = new Object();
	var xmlSeries;
	console.info("Found %d studies.",n);
	
	var uids = this.selectManyStudies(patient,uid);

	if( ! model.navigate ) {
		model.navigate = new Navigation();
		model.navigate.conversation.mode="ImageMode";
		model.patientLayout.navigate = model.navigate;
		model.patientLayout.init();
	}
	model.patientLayout.errorMessage = null;
	
	var navigate = model.navigate;
	navigate.setNavigation(patientId);
	var navPat = navigate.getChild(0);
	navPat.setNavigation(uids,"study");
	navPat.setNavigation(uids[0],"nav0");
	// Only set it if it hasn't already been set.
    if( navPat.conversation.nav1Show==null ) navPat.conversation.nav1Show = (uids.length>1);
    
	if( uids.length>1 ) {
		navPat.setNavigation(uids[1],"nav1");
	} else {
		navPat.setNavigation(uids[0]+"&c=1","nav1"); 
	}
	var keyObjects = model.layout.findLayout("KeyObjects");
	navigate.conversation.keyObjectDefault = (keyObjects && keyObjects.isActive);
	
	var external = navigate.external;
	
	// From here to the end of merge external the system starts 
	// making XML requests that take however long the server takes to
	// respond.
	
	if( (!external) || (!external.children[patientId]) ) {
		external = new StudyData(uids, searchLayout.search.ae);
		this.debug("Setup external study data root on url %s", external.url);
	} else {
		// Need to ensure all the studies are available for the base information
		external.queryForStudies(uids, external.children[patientId].children, model.ae);
	}
	var useThis = this;

	var lazy2 = function(ext,url,str,e) {
		if( e ) {
			useThis.model.patientLayout.errorMessage = useThis.getError(e);
			console.warn("Study Object Query has error", e,useThis.model.patientLayout.errorMessage); 
			searchLayout.hasError = useThis.model.patientLayout.errorMessage;
			useThis.updateView("ReRenderError");
			return;
		}
		if( uids.length==1 ) {
			var navS0 = navPat.getChild(0,"nav0");
			if(! navS0.conversation.seriesLayout ) {
				if( navS0.getExtChildren().length>4 ) {
					navS0.setConversation("seriesLayout", "Layout4x2");
				}
			}  
		}

		model.layout.selectTab("Display");
		console.timeEnd("Load Patient Data");

		useThis.displayResize("displayPatient");
		defaultCursor();
	};
		
	var lazy = function(ext,url,str,e) {
		if( e ) {
			useThis.model.patientLayout.errorMessage = useThis.getError(e);
			// If there was supposed to be a tab switch, it will fail, so
			// display the error on the search layout. 
			searchLayout.hasError = useThis.model.patientLayout.errorMessage;
			console.warn("Initial Study Query URL error",e,useThis.model.patientLayout.errorMessage);
			useThis.updateView("ReRenderError");
			return;
		}
		useThis.mergeExternal(external, lazy2);
	};
	
	if( external.url ) external.request(this.service, lazy);
	else lazy(external);
};
	
/** Handles the case where the image has an error on load */
XeroController.prototype.imageError = function(id) {
	var lay = this.findLayout(id);
	lay.errorMessage = "Unable to load image";
	lay.relayout = true;
	this.updateView("imageError");
}

/** Selects the isSelected studies from patient */
XeroController.prototype.selectManyStudies = function(patient,uid) {
	var study,studies = patient.study;
	var i,n=studies.length;
	var uids = new Array();
	if( uid ) uids.push(uid);
	for(i=0; i<n;i++) {
		study = studies[i];
		if( study.isSelected && study.studyUID!=uid) uids.push(study.studyUID);
	}
	if( uids.length>0 ) {
		this.debug("Found",uids.length,"seleted studies:",uids);
		return uids;
	}
	for(i=0; i<n; i++) {
		study = studies[i];
		uids.push(study.studyUID);	
	}
	return uids;
};

/** Returns the mode for the given layout */
XeroController.prototype.getModeImage = function(lay) {
	var mode = this.model.navigate.conversation.mode;
	return mode;
};
XeroController.prototype.getModeSeries = XeroController.prototype.getModeImage;

/** Gets the navigate object from this or a parent object */
XeroController.prototype.getNavigate = function(lay) {
	while(!lay.navigate) lay = lay.parent;
	if( !lay ) return;
	return lay.navigate;
};

/** Resizes and redisplays the entire display area */
XeroController.prototype.displayResize = function(op) {
	var size = getViewportSize();
	this.debug("Viewport size %d,%d",size[0],size[1]);
	this.model.patientLayout.resize(size[0]-8,size[1]-32, "width:100%; height:100%;");
	this.updateView(op);
};

/** Sets the current display mode - that is, the operation that controls click etc */
XeroController.prototype.displayMode = function(layId) {
	var lay = this.model.layout.findLayout(layId);
	var toMode = lay.i18n;
	var wasMode = this.getModeImage();
	this.userEvent("DisplayMode",toMode+" from "+wasMode);
	
	this.model.navigate.setConversation("mode", toMode);
	this.waitUpdate("displayMode");
};

/** Sets the current display mode - that is, the operation that controls click etc */
XeroController.prototype.seriesLayout = function(id, event, arg) {
	var lay = this.model.layout.findLayout(id);
	this.userEvent("SeriesLayout",lay.i18n);
	var nav = lay.navigate;
	if( nav==null ) nav = lay.parent.navigate;
	if( !arg ) arg = lay.i18n;
	nav.setConversation("seriesLayout", arg);
	if( nav.conversation.studyLayout === "ShowReport" ) {
		nav.setConversation("studyLayout", "ShowReportAndImages");
	}

	// Navigate backwards to ensure all positions are shown.
	this.fixSeriesLayoutNav(nav,arg);
	this.updateDisplayTab(true);
};

/** Given a study level navigate object, fix the series layout/position to
 * not extend beyond the end, for the given layout name (null/undefined mean
 * default layout).  Does not re-render.
 */
XeroController.prototype.fixSeriesLayoutNav = function(nav, layout) {
	var cnt = this.getSeriesLayoutCount(layout);
	if( cnt>1 ) {
		var posn;
		var navTo = 0;
		var c0 = nav.getChild(0);
		while(posn==null && cnt>0) {
			if( c0.lookupId(--cnt)!=null ) break;
			if( c0.lookupId(navTo)==null ) break;
			navTo--;
		}
		console.info("Trying to back-navigate to", navTo);
		if( navTo<0 ) nav.setNavigation(c0.lookupId(navTo));
	}
};

/** From the layout name, figure out how many objects could be shown. */
XeroController.prototype.getSeriesLayoutCount = function(layout) {
	if( layout==="Layout1x1" ) return 1;
	if( layout==="Layout2x2" || !layout) return 4;
	if( layout==="Layout4x2" ) return 8;
	return 2;
};

/** Chooses between the compare study mode and single study mode */
XeroController.prototype.studyCompare = function(id) {
	var lay = this.findLayout(id);
	var nav = this.getNavigate(lay);
	
	var key = nav.KEY+"Show";
	var altKey = (key =="nav0Show" ? "nav1Show" : "nav0Show");
	var showAlt = nav.parent.conversation[altKey];
	//console.info("compare Show keys",key,altKey,"at level", nav.parent.level);
	//console.info("Values:", nav.parent.conversation[key],nav.parent.conversation[altKey]);
	
	if( showAlt===false ) {
		this.userEvent("studyCompare", "compare");
	
		// Display the compare mode again
		console.info("Showing study compare mode.");
		nav.parent.setConversation(altKey,true);
	} else {
		this.userEvent("studyCompare", "single");
		nav.parent.setConversation(key,false);
	}
	this.displayResize("studyCompare");
};

/**
 * Merge the external data into the navigate display data so that layouts can be seen.
 * If lazy is passed in, then call it when the values are returned/available. 
 */
XeroController.prototype.mergeExternal = function(external, lazy, i) {
	var navigate = this.model.navigate;
	if( i==null ) i = 3;
	if( !external ) external = navigate.external;
	
	// Have to allow 3 queries, top level, series and image.
	var queries = navigate.mergeExternal(external);
	if( !queries ) {
		if( lazy ) lazy(external);
		return;
	}
	if( i==0 ) {
		console.warn("Too many queries without getting all data...");
		if( lazy ) lazy(external,null,null,"Client Error: Too many queries.");
		return;
	}
	
	var lazy2;
	if( lazy ) {
		var cnt = 0;
		var useThis = this;
		for(var j in queries) cnt++;
		lazy2 = function(ext,url,str,e) {
			if( lazy2.error ) return;
			if( e ) {
				lazy(ext,url,str,e);
				lazy2.error = e;
			}
			cnt--;
			if( cnt<=0 ) useThis.mergeExternal(external, lazy, i-1);				
		};
	};
	try {
		external.addQueries(this.service, queries, lazy2);
	} catch(e) {
		this.model.patientLayout.errorMessage = this.getError(e);
		this.model.patientLayout.relayout=true;
	}
	
	if( !lazy ) this.mergeExternal(external,lazy,i-1);
};

XeroController.prototype.selectGsps = function XC_selectGsps(id, event, gsps) {
	console.info("You selected to apply %s to %s", gsps, id);
	this.userEvent("selectGsps",gsps);
	var lay = this.findLayout(id);
	var nav = this.getNavigate(lay);
	var curGsps = nav.conversation.getQueryModifier("pr");
	if( curGsps!=null && curGsps.substring(6)==gsps ) gsps = null;
	if( gsps==null || gsps==="" ) nav.conversation.setQueryModifier("pr");
	else nav.conversation.setQueryModifier("pr","&gsps="+gsps,true);
	nav.setConversation("gspsLabel",gsps);
	console.info("Conversation info set on",nav.conversation.id,"at level",
		nav.level,"external id now", nav.conversation.getExternalId());
	this.updateDisplayTab(true);
	console.info("Completed applying gsps %s", gsps);	
};

XeroController.prototype.selectKo = function (id, event, koUID) {
	var lay = this.model.layout.findLayout(id);
	var nav = this.getNavigate(lay);
	var curKo = nav.conversation.getQueryModifier("ko");
	if( curKo!=null && curKo.substring(7)==koUID ) koUID = null;
	if( koUID==null || koUID==="" ) {
		this.userEvent("selectKo", "unapply");
		var qm = nav.conversation.setQueryModifier("ko","&koUID=*");
		qm.infoOnly = true;
	} else {
		this.userEvent("selectKo", "apply");
		nav.conversation.setQueryModifier("ko","&koUID="+koUID);
	}
	nav.setConversation("koUID", koUID);
	console.info("Conversation info set on",nav.conversation.id,"at level",
		nav.level,"external id now", nav.conversation.getExternalId());
	// The merge external is required before the invalidate to match the next external data/children
	// up, while the invalidate is required to ensure that new children get the correct values from external.
	// This may require an extra external query AFTER the invalidate.
	this.mergeExternal();
	nav.invalidate(true);
	this.updateDisplayTab(true);
};

/** Redisplays the display tab, resizing and merging external information etc */
XeroController.prototype.updateDisplayTab = function XC_updateDisplayTab(forceComplete) {
	var sameTab = true;
	
	this.mergeExternal();
	console.info("mergeExternal completed.");

	var size = getViewportSize();
	this.debug("Viewport size %d,%d",size[0],size[1]);
	this.model.patientLayout.resize(size[0]-8,size[1]-32, "width:100%; height:100%;");

	if( sameTab  ) {
	    // If the top level patient layout is completely redone, it means all changes will get incorporated,
	    // but doing it in a waitUpdate means that the update will be delayed until changes are available, fixing
	    // many flickering issues.
	 	if( forceComplete ) {
 			this.model.idMap.PatientLayout.relayout = true;
 		}
		this.waitUpdate("displayTabPartial");
	}
	else {
		this.updateView("displayTab");
	}
};

/** Displays the specified (backslashes) set of study UIDs */
XeroController.prototype.displayStudy = function XC_displayStudy(src,patientId, studyUid) {
	this.debug("Display study UID %s",studyUid);
	this.displayPatient(src, patientId,studyUid);
};

/** Navigates a given study next/previous - skips next/prev if they are
 * shown in the alt screen.
 */
XeroController.prototype.navStudy = function(id,event,dir) {
	this.userEvent("navStudy", dir);
	var lay = this.findLayout(id);
	var nav = this.getNavigate(lay);
	var uid = nav.lookupId(dir);
	
	var altKey = (nav.KEY=="nav0" ? "nav1" : "nav0");
	var altVal = nav.parent.conversation[altKey];
	var altShow = nav.parent.conversation[altKey+"Show"];
	if( altShow!==false && uid===altVal ) {
		console.info("Skipping alternate viewed image.");
		uid = nav.lookupId(dir*2);
	}
	
	if(uid==null ) {
		console.warn("Didn't find a study to navigate to.");
		return;
	}
	nav.parent.setNavigation(uid,nav.KEY);
	this.mergeExternal();
	this.displayResize();
};

/** Navigates to a given study for a given study slot */
XeroController.prototype.navToStudy = function(id,event,uid) {
	var lay = this.findLayout(id);
	var nav = this.getNavigate(lay);
	if( !(nav && nav.external) ) return;
	
	var altKey = (lay.key=="nav0" ? "nav1" : "nav0");
	var altVal = nav.conversation[altKey];
	if( altVal===uid ) {
		uid = uid + "&c=1";
		this.userEvent("navToStudy","sameStudy");
	} else {
		this.userEvent("navToStudy");
	}
	nav.setNavigation(uid,lay.key);
	this.mergeExternal();
	this.displayResize();
};

/** Navigates at the series level to a specific series */
XeroController.prototype.navToSer = function XC_NavToSer(id,event,uid) {
	this.userEvent("navToSeries");
	var lay = this.model.patientLayout.findLayout(id);
	var navigate = lay.navigate;
	// TODO - change to use a series level instance
	navigate.setNavigation(uid);
	console.info("setNavigation completed.");
	this.updateDisplayTab();	
};

/** Navigates at the series level by dir +/- forward/backwards */
XeroController.prototype.navSer = function XC_navSer(id,event,dir) {
	this.userEvent("navSeries",dir);
	var lay = this.model.layout.findLayout(id);
	console.info("You are trying to navigate %s in dir %d found lay %s", id,dir,lay.id);
	var nav = lay.navigate;
	if(!nav) nav = lay.parent.navigate
	if( typeof(dir)==="string" ) dir = parseInt(dir);
	if(!isFinite(dir)) {
		console.warn("Not a finite series direction:",dir);
		return;
	}
	dir = Math.floor(dir);
	// TODO - this should operate as a child of the series it belongs to,
	// then the getChild would not be required.
	var dirSeries = nav.getChild(0).lookupId(dir);
	if(!dirSeries ) {
		console.warn("Didn't find a next/previous series to navigate to.");
		return;
	}
	var sl = nav.conversation.seriesLayout;
	var cnt = this.getSeriesLayoutCount(sl);
	if( dir>0 && cnt>1) {
		var dirTest = nav.getChild(0).lookupId(cnt);
		if( !dirTest ) {
			console.info("Not able to navigate past series end points.");
			return;
		} 
	}
	console.info("You are trying to navigate to %s", dirSeries);
	nav.setNavigation(dirSeries);
	this.updateDisplayTab();	
};

/** Navigates at the image level - can be called against either the imageToolbar or imageArea */
XeroController.prototype.navImg = function XC_navImg(id,event,dir) {
	this.userEvent("navImage",dir);
	var lay = this.findLayout(id);
	console.info("You are trying to navigate",id,"in dir",dir,"found lay", id,dir,lay.id);
	var navigate = this.getNavigate(lay);
	if( navigate.level==Navigation.SERIES ) {
		console.warn("Using series level navigation to navigate image - should use image level nav instead.");
		navigate = navigate.getChild(0);
	}
	var navSer = navigate.parent;
	if( dir==="first" ) {
		var dirImg = 0;
	} else if(dir==="last") {
		var dirImg = navSer.external.Viewable-1; 
	} else {
		if( typeof(dir)==="string" ) dir = parseInt(dir);
		if( !isFinite(dir) ) {
			console.warn("Not a finite direction/number:",dir);
			return;
		}
		// Sometimes fractions can occur
		dir = Math.floor(dir);
		var dirImg = navigate.lookupId(dir);
	};
	if( dirImg==null ) {
		console.warn("Didn't find a next/previous image to navigate to dir",dir,dirImg,"level",navigate.level);
		return;
	}
	console.info("You are trying to image navigate", navigate.KEY,"to", dirImg);
	navSer.setNavigation(dirImg,navigate.KEY);
	
	this.updateDisplayTab();
};

/** Navigates the series using the mouse wheel */
XeroController.prototype.wheelSeries = function(id,e) {
	this.userEvent("wheelSeries");
	id = this.model.patientLayout.findLayout(id);
	console.info("Wheel mouse navigation called on %s", id.id);
	var wheelData = e.detail ? e.detail : e.wheelDelta/-40;
	if( e.shiftKey ) wheelData = (wheelData * id.gridX * id.gridY)*10;
	else wheelData = (wheelData * id.gridX * id.gridY)/3;
	wheelData = Math.floor(-wheelData);
	console.info("Trying to navigate %d", wheelData);
	this.navImg(id,e,wheelData);
	console.info("Wheel isComplete=%s",id.isComplete());
	return cancelEvent(e);
};

/** Changes the series mode to 1x1 mode, or to 2x2 if in 1x1, AND navigates to the current
 * series.
 */
XeroController.prototype.dblImage = function(id,e) {
	this.userEvent("dblImage");
	var lay =this.findLayout(id);
	var nav = this.getNavigate(lay);
	var nth = nav.parent.offset;
	nav = nav.parent.parent;
	var mode = nav.conversation.seriesLayout;
	var navTo;
	if( mode==="Layout1x1" ) {
	  navTo = nav.conversation.dblSeries;
	  nav.conversation.dblSeries = undefined;
	  if( nav.conversation.wasSeriesLayout ) {
	  	mode = nav.conversation.wasSeriesLayout;
	  	nav.conversation.wasSeriesLayout = undefined;
	  	nav.setConversation("dblSeries");
	  } else {
	  	mode = undefined;
	  	this.fixSeriesLayoutNav(nav);
	  }
	} else {
	  nav.conversation.wasSeriesLayout = mode;
	  mode="Layout1x1";
	  nav.setConversation("dblSeries", nav.getChild(0).lookupId(0));
	  navTo = nav.getChild(0).lookupId(nth);
	}
	nav.setConversation("seriesLayout", mode);
	if( navTo ) {
		nav.setNavigation(navTo);		
	}
	this.updateDisplayTab(true);
};

/** Changes the image layout */
XeroController.prototype.setImgLay = function XC_setImgLay(id,w,h) {
	this.userEvent("setImgLay", w+"x"+h);
	id = this.findLayout(id);
	if( id.imageArea ) id = id.imageArea;
	id.origGridX = w;
	id.origGridY = h;
	// Make an artificial change to re-layout the image area.
	id.series = null;
	id.navigateListener(id.navigate);
	id.resize(id.width, id.height, id.style);
	this.updateView("displayPatient");
};


