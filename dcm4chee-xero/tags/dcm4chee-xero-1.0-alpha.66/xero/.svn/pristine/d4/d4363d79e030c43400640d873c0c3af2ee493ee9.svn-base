/**
 * This controller manages the report viewing and navigation.
 */
 
/** Causes the given report to be viewed - sets the layout to one of the viewable layouts
 *  if it isn't already in that mode, and changes the UID to view.
 */
XeroController.prototype.viewReport = function RC_viewReport(layId,event,uid) {
	var lay = this.findLayout(layId);
	console.info("Trying to view report", uid,"in",lay.id);

	var nav = lay.navigate;
	if(!nav) nav = lay.parent.navigate;
	if( !nav ) {
		console.warn("No navigation defined - can't navigate study/report.");
	 	return;
	};
	nav.setConversation("displayReport", uid);
	if( nav.conversation.studyLayout === "ShowImages" ) {
		nav.setConversation("studyLayout", "ShowReportAndImages");
	}

	this.displayResize("viewReport");
};

/** Closes the report area */
XeroController.prototype.closeArea = function (id,event,area) {
	var lay = this.findLayout(id);
	var nav = this.getNavigate(lay);
	var studyLayout = nav.conversation.studyLayout;
	console.info("Changing study layout from", studyLayout);
	var newLay = "ShowReportAndImages";
	if( studyLayout==="ShowImages" ) newLay = "ShowReport";
	else if( studyLayout==="ShowReport" || area==="Report") newLay = "ShowImages";
	else newLay = "ShowReport"; 
	nav.setConversation("studyLayout", newLay);

	this.displayResize(newLay);
};

/** View another object type */
XeroController.prototype.viewOther = function (id,event,uid) {
	var lay = this.findLayout(id);
	var nav = lay.navigate;
	var other = nav.external.findInstance(uid);
	if( !other ) {
	   console.warn("Unable to find other view object",uid);
	   return;
	}
	var studyUID = other.get("studyUID");
	var seriesUID = other.get("seriesUID");
	var ae = other.get("ae");
	var url = WADO_QUERY + "&studyUID="+studyUID+"&seriesUID="+seriesUID+"&objectUID="+uid;
	if( ae ) url = url+"&ae="+ae;
	console.info("Opening window",url);
	window.open(url);
};

