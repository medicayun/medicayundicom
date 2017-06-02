/**
 * Contains the layout object definitions for the display page.
 * Two types of over-rides can be added to the display layout area - things that are defined in the
 * metadata can be directly added to for the specific version being rendered.  For things not in the
 * metadata, some or all methods can be over-ridden in another javascript file included at higher priority
 * than this one.
 */

function PatientLayout() {
	this.baseConstructor("PatientLayout");
};

PatientLayout.prototype = new Layout("patientLayout", "html/errorLayout");
PatientLayout.prototype.gridX = 2;
PatientLayout.prototype.gridY = 1;
// Add some initial size to handle the border.
PatientLayout.prototype.heightEdge = 0;
PatientLayout.prototype.widthEdge = 12;

/** Initialize the patient layout object - lazy, and only succeed if the navigate object is available.
 */
PatientLayout.prototype.init = function PatientLayout_init() {
	if( this.navigate==null ) return false;
	if( this.layouts && this.layouts.length>0 ) return true;
	var patientMenu = new PatientMenu();
	this.add(patientMenu, TabsLayout.TabMenu);
	
	for(var j=0; j<this.gridY; j++) {
		for(var i=0; i<this.gridX; i++) {
			if( i>0 ) {
				var spacer = new SpacerLayout(2,1);
				this.add(spacer);
			}
			var nth = j*this.gridX+i;
			var studyArea = new StudyAreaLayout(nth);
			this.add(studyArea);
		}
	}
	this.gridX = this.gridX*2-1;
	
	this.navigate.hook(this);

	return true;
};

/** Handles the compare study layout/event notifications */
function CompareStudyLay(suffix) {
	this.baseConstructor("CompareStudy"+suffix);
};

CompareStudyLay.prototype = new Layout("CompareStudy", "image/compareStudyLay");
CompareStudyLay.prototype.events=["/patient:0/conversation:nav0Show", "/patient:0/conversation:nav1Show"];
CompareStudyLay.prototype.icon="CloseIcon";
CompareStudyLay.prototype.tooltip="CompareSingleStudyTip";

CompareStudyLay.prototype.namedListener = function(nav,vname,val) {
	if(!(nav && nav.conversation) )return;
	var show0 = (nav.conversation.nav0Show!==false);
	var show1 = (nav.conversation.nav1Show!==false);
	this.debug("Updating CompareStudyLay shows:", show0,show1);
	if( show0===show1 ) {
		this.icon = "CloseIcon";
		this.tooltip = "CompareSingleStudyTip";
	} else {
		this.icon = "CompareStudyIcon";
		this.tooltip = "CompareStudyTip";
	}
	//this.debug("Icon set to", this.icon);
	this.relayout = true;
};

/** The menu for patient tab control */
function PatientMenu() {
	// The suffix is empty as there should only be 1 instance of this menu.
	var suffix = "";
	this.baseConstructor("PatientMenu");
	
$layout/addChildren(children=patientMenu.children)$
};

PatientMenu.prototype = new Layout("PatientMenuPrototype","image/patientMenu");
/** The listener for the patient information */
PatientMenu.prototype.navigateListener = function (navigate) {
	this.navigate = navigate;
	if( ! navigate ) return;
	this.patient = navigate.external;
};
PatientMenu.prototype.events=".";
PatientMenu.prototype.navigate="/patient:0";

/** Create the study layouts containing the study tray and the image area.  Not lazy. */
function StudyAreaLayout(nth) {
	var suffix = "S"+nth;
	this.baseConstructor("StudyArea"+suffix);
	this.nth = nth;
	this.navigate = "/patient:0/study:nav"+nth+",0";
	var studyTray = new StudyTrayLayout(suffix,nth);
	var even = ((nth %2)==0);
	if( studyTray.absWidth ) {
		this.add(studyTray,(even?Layout.WEST:Layout.EAST));
	} else {
		this.add(studyTray,Layout.NORTH);
	}
	
	this.add(new ReportArea(suffix));
	this.add(new SeriesAreas(suffix));
};

StudyAreaLayout.prototype = new Layout();
StudyAreaLayout.prototype.gridX = 1;
StudyAreaLayout.prototype.gridY = 2;
StudyAreaLayout.prototype.displayClass="studyArea";
StudyAreaLayout.prototype.events= ["/patient:0/conversation:nav0Show","/patient:0/conversation:nav1Show","."];
StudyAreaLayout.prototype.compareUsed = true;
StudyAreaLayout.prototype.widthEdge=3;

/**
 * This defines the listeners for navigate changes to the layout.  Computes
 * quite a few fields relevant to display menus etc.
 * These computations probably should be moved to the individual sub-items, 
 * and some of them have been. 
 */
StudyAreaLayout.prototype.navigateListener = function (navigate) {
	this.navigate = navigate;  
	this.used = false;
	if( ! navigate ) return;
	this.study = navigate.external;
	if( !this.study ) return;
	this.used = true;
	this.patient = navigate.parent.external;
	var showKey = navigate.KEY+"Show";
	if( navigate.parent.conversation[showKey]===false ) {
		this.used = false;
		return;
	};
	
	var studies = navigate.parent.getExtChildren();
	if( !studies ){
		console.warn("Didn't find studies to navigate on.");
		return;
	}

        // The logic for the next/previous buttons is that there has to exist a next/previous
        // item OTHER than a displayed study - thus, determine what is next and whether it is
        // displayed before one can determine first/last.
	var uid = navigate.external.studyUID;
	var altKey = (navigate.KEY=="nav0" ? "nav1" : "nav0"); 
	var uidAlt = navigate.parent.conversation[altKey];
	var showAlt = (navigate.parent.conversation[altKey+"Show"]!==false);
	var posn = navigate.parent.findExternal(uid);
	var altPosn = navigate.parent.findExternal(uidAlt);
	if( posn===undefined ) {
		console.warn("Didn't find study uid in list:",uid);
		return;
	}
	this.debug("Found",studies.length,"Studies position",posn);
	this.isFirst = (posn==0 || (showAlt && posn==1 && altPosn==0));
	this.isLast = ((posn==studies.length-1) || (showAlt && posn==studies.length-2 && altPosn==studies.length-1));
};


/** Listen for changes in the compare study type */
StudyAreaLayout.prototype.namedListener = function StudyNavigateListener(nav,vname,val) {
	this.debug("show study changed",vname,"to",val);
	var key = "nav"+this.nth+"Show";
	// This object isn't responsible for modifying the display settings - that is done in the
	// navigate listener.
	if( vname!==key || val==null ) return;
	var nUsed = (val!==false && this.navigate.external!=null);
	this.debug("StudyLayout set show",key,"to",nUsed);
	if( nUsed!=this.used ) {
		this.used = nUsed;
		this.relayout = true;
	} 
};

/** Resize the study area - change the position of the report to left/top based on
  * available size.
  */
StudyAreaLayout.prototype.resizeListener = function() {
	if( this.width < 800 ) {
		this.gridX = 1;
		this.gridY = 2;
	} else {
		this.gridX = 2;
		this.gridY = 1;
	}
};

/** The report area displays a report study */
function ReportArea(suffix) {
	this.baseConstructor("ReportArea"+suffix);
};

ReportArea.prototype = new Layout("ReportArea","report/report");
ReportArea.prototype.events=["conversation:displayReport","conversation:studyLayout"];

ReportArea.prototype.namedListener=function(nav,vname,val) {
	this.used = false;
	if( !(nav && nav.external) ) return;

	var studyLayout = nav.conversation.studyLayout;
	this.closeReportAreaUsed = (studyLayout==="ShowReportAndImages");
	// Check actual number of children - even if it is "and images" there may not
	// BE any images.
	if( this.closeReportAreaUsed ) {
		var extChildren = nav.getExtChildren();
		if( !(extChildren && extChildren.length>0) ) this.closeReportAreaUsed=false;
	}
	var displayReport = nav.conversation.displayReport;
	this.debug("Report area layout",studyLayout,"report",displayReport);
	
	if( !(studyLayout && displayReport && studyLayout!=="ShowImages") ) return;
	var reportExt = nav.external.findInstance(displayReport);
	if( reportExt==null ) {
		console.warn("Can't find report information.");
		return;
	}
	this.debug("Displaying report",val);
	this.used = true;
	this.src = "studyUID="+reportExt.get("studyUID")+"&amp;seriesUID="+reportExt.get("seriesUID")+"&amp;objectUID="+displayReport;
	var ae = reportExt.get("ae");
	if( ae ) this.src =this.src+"&amp;ae="+ae;
}; 

/** Handle the study tray layout */
function StudyTrayLayout(suffix,nth) {
	this.baseConstructor("StudyTray"+suffix);
	this.add(new StudyMenu(suffix,nth),"StudyMenu");
	$layout/addChildren(children=studyTrayLayout.children)$
};

StudyTrayLayout.prototype = new Layout("StudyTray", "image/studyTray");
StudyTrayLayout.prototype.absWidth = 135;
StudyTrayLayout.prototype.events=".";
StudyTrayLayout.prototype.navigateListener = StudyAreaLayout.prototype.navigateListener;

/** Creates the study menu top level item */
function StudyMenu(suffix,nth) {
	this.baseConstructor("StudyMenu"+suffix);
	this.nth = nth;
	this.key = "nav"+nth;
	this.StudyMenuItems = new StudyMenuItems(suffix,this.key);
	this.add(this.StudyMenuItems);
	this.baseLayouts = this.layouts;
};
StudyMenu.prototype = new Layout("StudyMenu","html/toolItem");
StudyMenu.prototype.events="merged:external";
StudyMenu.prototype.area="Menu";

/** Setup the child menu */
StudyMenu.prototype.namedListener = function(nav) {
	if( !(nav && nav.external) ) return;
	this.label = nav.external.StudyDescription;
	this.title = this.label;
	var pat = nav.parent.external;
	this.navigate = nav.parent;
	var navStudy = this.navigate.getExtChildren();
	this.StudyMenuItems.used = (navStudy.length>1);
	if(!this.StudyMenuItems.used) this.layouts = null;
		else this.layouts = this.baseLayouts;
	this.StudyMenuItems.items = new Array();
	var study, i,n=navStudy.length;
	this.debug("StudyMenu navigate listener level",nav.level,this.label,"on",n,"Child studies.");
	// TODO - move this code into navigate proper.
	for(i=0; i<n; i++) {
		if( i==this.nth ) continue;
		study = this.navigate.external.children[navStudy[i]];
		this.StudyMenuItems.items.push(study);
	}
};

/** Contains the list of items in the study menu */
function StudyMenuItems(suffix, key) {
	this.baseConstructor(this.id+suffix);
	this.key = key;
};
StudyMenuItems.prototype = new Layout("StudyMenuItems","html/childList");
StudyMenuItems.prototype.itemId = "studyUID";
StudyMenuItems.prototype.listTemplate="image/studyMenuItem";
StudyMenuItems.prototype.action="navToStudy";


/** Defines the X of N series layout information */
function XofNSeries(suffix) {
	this.baseConstructor("XofN"+suffix);
};

XofNSeries.prototype = new Layout("XofN","image/seriesNumber");
XofNSeries.prototype.events=["series:0","conversation:seriesLayout"];
XofNSeries.prototype.numSeries = 4;
XofNSeries.prototype.x=0;
XofNSeries.prototype.n=0;

/** Updates the first/last indicators when the number of images changes */
XofNSeries.prototype.namedListener = function(nav,vname,val) {
	if( val==null ) {
		this.numSeries = 4;
		return;
	} 
	else if( val==="Layout1x1" ) this.numSeries = 1;
	else if( val==="Layout2x1" || val==="Layout1x2" ) this.numSeries = 2;
	else if( val==="Layout2x2" ) this.numSeries = 4;
	else if( val==="Layout4x2" ) this.numSeries = 8;
	this.isLast = (this.x+this.numSeries>this.n);
};

/** Set the current position and first/last indicators */
XofNSeries.prototype.navigateListener = function(nav) {
	this.used = false;
	if( !(nav && nav.external) ) return;
	this.series = nav.parent.getExtChildren();
	if( ! this.series ) return;
	this.n = this.series.length;
	if( this.numSeries > this.n ) this.numSeries = this.n;
	if( this.n<2 ) return;
	this.used = true;
	this.debug("Series UID navigated to", nav.external.seriesUID);
	this.relayout=true;
	for(this.x = 1;this.x<=this.series.length; this.x++) {
		if( nav.external.seriesUID == this.series[this.x-1].seriesUID ) break;
	}
	this.debug("xofn series x=",this.x);
	this.isFirst = (this.x===1);
	this.isLast = (this.x+this.numSeries>this.n);
};

/** Defines right-hand image menu */
function RightImageMenu(suffix) {
	this.baseConstructor("RightImageMenu"+suffix);
};

RightImageMenu.prototype = new Layout("RightImageMenu","image/rightImageMenu");


/** Defines the X of N series layout information */
function XofNImage(suffix) {
	this.baseConstructor("XofN"+suffix);
};

XofNImage.prototype = new Layout("XofN","image/imageNav");
XofNImage.prototype.events=".";
XofNImage.prototype.isFirst = false;
XofNImage.prototype.isLast = false;
XofNImage.prototype.x = 0;
XofNImage.prototype.n = 300;
XofNImage.prototype.SIZE = 10;

XofNImage.prototype.navigateListener = function(nav) {
	this.used = false;
	if(!(nav && nav.external)) return;
	this.x = 1+parseInt(nav.external.Position);
	this.n = parseInt(nav.parent.external.Viewable);
	var n = this.n-1;
	if( n===0 ) n = 1;
	this.beforeLine = ((this.x-1)*this.SIZE/n);
	this.afterLine = this.SIZE-this.beforeLine;
	this.isFirst = (this.x==1);
	this.isLast = (this.x>=this.n);
	this.used = (this.n>1);
	this.relayout = true;
};

/**
 * The series areas contains the individual series are display objects
 */
function SeriesAreas(suffix, nth) {
	this.baseConstructor("SeriesAreas"+suffix);
	this.nth = nth;
	var i,j, seriesSuffix, imageAreas;
	for(j=0; j<this.gridY; j++) {
		for(i=0; i<this.gridX; i++) {
			nth = j*this.gridX+i;
			seriesSuffix = suffix+"Se"+nth;
			imageAreas = new ImageAreasLayout(seriesSuffix, nth);
			this.add(imageAreas);
		};
	};
	this.closeSeriesAreas = new CloseSeriesAreas(suffix); 
	this.add(this.closeSeriesAreas);
};

SeriesAreas.prototype = new Layout("SeriesAreas");
SeriesAreas.prototype.gridX = 4;
SeriesAreas.prototype.gridY = 2;
SeriesAreas.prototype.events=["series:0","conversation:seriesLayout","conversation:studyLayout"];
SeriesAreas.prototype.displayClass="seriesAreas";
SeriesAreas.prototype.heightEdge = 7;

/** Just disable the series area if there are no available series */
SeriesAreas.prototype.navigateListener = function(nav) {
	this.used = (nav!=null && nav.external!=null);
	this.hasSeries = this.used;
	if( this.used && nav.parent.conversation ) {
	   var studyLayout = nav.parent.conversation.studyLayout;
	   if( studyLayout=="ShowReport" ) this.used = false;
	}
	this.debug("SeriesAreas navList used,hasSeries=",this.used,this.hasSeries);
};

/** Handle the series layout settings grid X/Y wise. */
SeriesAreas.prototype.namedListener = function(nav,vname,val) {
	this.debug("SeriesAreas",vname,val);
	var studyLayout = nav.conversation.studyLayout;;
	if( studyLayout==="ShowReport" ) {
		this.debug("ShowReport only - not displaying series areas.");
		this.used = false;
		return;
	};
	this.used = this.hasSeries;
	this.debug("SeriesArea listener",vname,"used",this.used);
	if( vname==="studyLayout" ) {
		this.closeSeriesAreas.used = (val=="ShowReportAndImages"); 
		return;
	}
	
	if( val==null ) val = "Layout2x2";

	this.gridX = 2;
	this.gridY = 2;
	if( val=="Layout2x2" ) return;
	if( val=="Layout2x1" || val=="Layout1x1") this.gridY = 1;
	if( val=="Layout1x2" || val=="Layout1x1" ) this.gridX = 1;
	if( val=="Layout4x2" ) this.gridX = 4;
};

function CloseSeriesAreas(suffix) {
	this.baseConstructor("CloseSeriesAreas"+suffix);
};
CloseSeriesAreas.prototype = new Layout("CloseSeriesAreas","image/closeSeriesAreas");
CloseSeriesAreas.prototype.absHeight = 0;
CloseSeriesAreas.prototype.absWidth = 0;
CloseSeriesAreas.prototype.arg = "Image";
CloseSeriesAreas.prototype.action = "closeArea";

/** Create an images areas object */
function ImageAreasLayout(suffix, nth) {
	this.baseConstructor("ImageAreas"+suffix);
	this.navigate = "series:"+nth;
	this.nth = nth;

	this.used = false;
	this.origGridX = this.gridX;
	this.origGridY = this.gridY;
	var useThis = this;

	this.mousewheel = function(evt) {
		useThis.debug("Mouse wheel image area called.");
		return controller.wheelMouse(useThis,evt);
	};
	
	var i,j,layout, imageSuffix;
	for(j=0; j<this.gridY; j++) {
		for(i=0; i<this.gridX; i++) {
			var nth = j*this.gridX+i;
			imageSuffix = suffix+"I"+nth;
			layout = new ImageLayout("ImageLay"+imageSuffix,nth);
			layout.x = i;
			layout.y = j;
			this.add(layout);
			layout.add(new ImageMenu(suffix),"imageMenu");
			layout.add(new XofNImage(suffix),"bottomImageMenu");
			layout.add(new RightImageMenu(suffix),"rightImageMenu");
		};
	};
};

ImageAreasLayout.prototype = new Layout();
ImageAreasLayout.prototype.gridX = 2;
ImageAreasLayout.prototype.gridY = 2;
ImageAreasLayout.prototype.onmousewheel="controller.wheelMouse";
ImageAreasLayout.prototype.viewRenderedListeners = ["fixWheelMouse"];
ImageAreasLayout.prototype.events=[".","../conversation:seriesLayout"];
ImageAreasLayout.prototype.area="Series";

/** Defines the image area navigate listener to reset the grid size and
 * enable/disable child elements as appropriate.
 */
ImageAreasLayout.prototype.navigateListener = function(navigate) {
	this.navigate = navigate;
	if(!navigate) {
		this.external=undefined;
		return;
	}
	if( navigate.external===this.series ) {
		return;
	}
	this.series = navigate.external;
	this.relayout = true;
	var wasUsed = this.used;
	this.namedListener(navigate,"seriesLayout",(navigate.parent.conversation? navigate.parent.conversation.seriesLayout : null));
	if( wasUsed!==this.used ) this.parent.relayout =true;
	if( !this.used ) return;
	var n,imageCount = navigate.external.Viewable;
	if( imageCount<=1 ) {
		this.gridX = 1;
		this.gridY = 1;
	} else {
		this.gridX = this.origGridX;
		this.gridY = this.origGridY;
		n = this.gridX * this.gridY;
		while(n>imageCount) {
			if( this.gridX>1 && this.gridY>1 ) {
				n = (this.gridX-1)*(this.gridY-1);
				if( n < imageCount ) {
					// Try removing the bottom row as well
					n = this.gridX * (this.gridY-1);
					if( n>=imageCount ) this.gridY--;
					break;
				}
				this.gridX = this.gridX-1;
				this.gridY = this.gridY-1;
			} else if( this.gridX>1 ) {
				this.gridX = imageCount;
				break;
			} else if( this.gridY>1 ) {
				this.gridY = imageCount;
				break;
			} else break;
		}
	}
	n=this.layouts.length;
	var i;
	for(i=0; i<n; i++) {
		this.layouts[i].used = (i < this.gridX * this.gridY);
	}
};

ImageAreasLayout.prototype.namedListener = function(nav,vname,val) {
	if( !this.series ) {
		this.used = false;
		return;
	}
	if( !val ) val = "Layout2x2";
	var maxSeries = 100;
	if( val==="Layout1x1" ) maxSeries = 1;
	else if( val=="Layout2x1" || val=="Layout1x2") maxSeries = 2;
	else if( val=="Layout2x2" ) maxSeries = 4;
	else if( val=="Layout4x2" ) maxSeries = 8;
	this.used = (this.nth < maxSeries);
	this.debug("ImageAreas used",this.used,"nth",this.nth,"max series",maxSeries,val);
}; 

/**
 * Generic display mode change - these are major modes for window level and zoom/pan,
 * plus others in the future. 
 */
function DisplayModeLay(suffix,i18n) {
	if( !i18n ) {
		i18n = suffix;
		suffix = "";
	}
	var id = i18n+suffix;
	this.baseConstructor(id);
	this.i18n = i18n;
	this.icon = i18n+"Icon";
	this.isActive = ( this.i18n==this.BASE_MODE);
};
DisplayModeLay.prototype = new Layout("DisplayModeLay","html/toolItem");
DisplayModeLay.prototype.events="/conversation:mode";
DisplayModeLay.prototype.action="displayMode";
DisplayModeLay.prototype.BASE_MODE = "ImageMode";

/** A general mode listener - activates/de-actives given modes, plus promoting child to parent mode */
DisplayModeLay.prototype.namedListener = function (navigate, varName, newVal) {
	var wasActive = this.isActive;
	if( !newVal ) newVal = this.BASE_MODE;
	this.isActive = (newVal===this.i18n);
	if( wasActive!==this.isActive ) this.relayout = true;
};


/** Handle the Image menu layout.  Knows about activating/de-activating mode specific
 * buttons.
 */
function ImageMenu(suffix) {
	this.baseConstructor("ImageMenu"+suffix);
$layout/addChildren(children=imageMenu.children)$
	this.namedListener();	
};

ImageMenu.prototype = new Layout("ImageMenu", "image/imageMenu");
ImageMenu.prototype.displayClass="imageMenu hoverMenu menu";
ImageMenu.prototype.ulClass="leftmenu";
ImageMenu.prototype.isActive = true;
ImageMenu.prototype.showLabel = false;
ImageMenu.prototype.events="/conversation:mode";

ImageMenu.prototype.namedListener = function(navigate,varName,newVal) {
	if( !newVal ) newVal = DisplayModeLay.prototype.BASE_MODE;
	var i, child;
	for(i in this.children ) {
		child = this.children[i];
		if( !child.mode ) continue;
		child.used = (newVal===child.mode);
	};
	this.relayout = true;
};
