/**
 * Handles the window levelling.
 */
function WLController() {
};
WLController.prototype = new ImageMouseHandler();
WLController.prototype.isWLController = true;

XeroController.prototype.WindowLevel_dragImage = new WLController();

XeroController.prototype.ctrl_dragImage = XeroController.prototype.WindowLevel_dragImage;

/** Handle the window level reset operation */
XeroController.prototype.wlReset = function(lay,event, preventUpdate) {
	lay = this.findLayout(lay);
	if( !preventUpdate ) this.userEvent("wlReset");
	var nav = this.getNavigate(lay);
	nav=nav.parent;
	if( nav.merged.invert ) {
		nav.setMerged("invert");
	}
	if( nav.merged.windowWidth ) {
		// Only notify on the second set
		nav.merged.windowWidth = undefined;
		nav.setMerged("windowCenter");
	}
	if( !preventUpdate ) this.waitUpdate("mouseUp",2);
};

XeroController.prototype.ctrl_clickImage = XeroController.prototype.wlReset; 

/** Handle inverting the image */
XeroController.prototype.wlInvert = function(lay) {
	lay = this.findLayout(lay);
	this.userEvent("wlInvert");
	this.userEvent("logWLInvert");
	var nav = this.getNavigate(lay);
	var inv = nav.merged.get("invert"); 
	nav=nav.parent;
	nav.setMerged("invert",!inv);
	this.waitUpdate("mouseUp",2);
};


WLController.prototype.prepare = function(e) {
	if( !this.prepareBase(e) ) return false;
	this.baseUrl = this.getImageUrl(["rows", "columns", "windowWidth", "windowCenter","imageQuality"]);
	this.controller.userEvent("windowLevel");
	this.controller.userEvent("logWindowLevel");
	this.cnt = 0;
	this.totalCnt = 0;
	this.timeSum = 0;

	// Find the window level indicator
	var n = this.imageId.length;
	var wlIndicator = "wlIndicatorRightImageMenu"+this.imageId.substring(n-7,n-2);
	this.wlIndicator = document.getElementById(wlIndicator);
	console.warn("wlIndicator is",this.wlIndicator);
	this.wlIndicator.style.display="block";
	this.wlIndicator.innerHTML = "Loading Image Info";
	
	this.minPixel = this.layout.merged.get("minPixel");
	if( this.minPixel==undefined ) {
		this.waitCursor = true;
		waitCursor();
		
		var pos = this.layout.navigate.external.Position;
		var query = this.layout.navigate.parent.generateQuery(parseInt(pos));
		var squery;
		for(var i in query) squery = i;
		var mods = query[squery];
		query[squery] = null;
		
		var count = squery.indexOf("&Count");
		squery = squery.substring(0,count)+"&Count=1&Position="+pos+"&pixelInfo=true";
		this.debug("Need to compute min/max pixel value for window levelling using query",squery,"at position",pos);
		query = new Object();
		query[squery] = mods;
		this.controller.model.navigate.external.addQueries(this.controller.service,query,true);
	};

	return true;
};

/** 
 * On the start, find the related image object (should be the start target),
 * and grab the source URL and figure out the range of the window levelling and the 
 * initial window level.
 */
WLController.prototype.start = function WLC_start(e) {
	console.info("Starting wl controller.");
	// TODO - add a server call to fetch windowing information if not already available
	this.minPixel = this.layout.merged.get("minPixel");
	if( this.minPixel==null ) {
		console.info("Waiting for min/max pixel info.");
		return false;
	}
	this.origCenter = this.layout.merged.get("windowCenter");
	this.origWidth = this.layout.merged.get("windowWidth");
	this.maxPixel = this.layout.merged.get("maxPixel");
	this.minPixel = Number(this.minPixel);
	this.maxPixel = Number(this.maxPixel);
	this.pixelRange = this.maxPixel - this.minPixel;
	if( this.origCenter==undefined ) {
		this.origCenter = (this.minPixel + this.maxPixel)/2;
		this.origWidth = this.pixelRange;
	} else {
		this.origCenter = Number(this.origCenter);
		this.origWidth = Number(this.origWidth);
	};
	this.center = this.origCenter;
	this.width = this.origWidth;
	console.info("Initial c/w=",this.origCenter,",",this.origWidth,"min/max=", this.minPixel, this.maxPixel);
	return true;
}; 

/** Update the displayed window level, at least if the image isn't currently fetching */
WLController.prototype.mousemove = function WLC_mousemove(e) {
	var dx = e.screenX-this.startScreen[0];
	var dy = e.screenY-this.startScreen[1];
	var center = Math.floor(this.origCenter - (dy*this.origWidth/512));
	var width = Math.floor(this.origWidth - (dx*this.origWidth/512));
	if( center >= this.minPixel && center < this.maxPixel ) {
		this.center = center;
	}
	if( width>10 && width < 2*this.pixelRange ) {
		this.width = width;
	}
	if( !this.displayCount ) {
		this.displayCount = 20;
		console.info("dx,dy=%d,%d c/w=%d,%d", dx,dy,this.center,this.width);
	} else this.displayCount--;
	this.updateImage();
};

WLController.prototype.qualityLevel = 0;
WLController.prototype.qualityOpts = ["&rows=512&imageQuality=0.6","&rows=512&imageQuality=0.4", "&rows=512&imageQuality=0.3",
	"&rows=256&imageQuality=0.75","&rows=256&imageQuality=0.6", "&rows=256&imageQuality=0.4", "&rows=256&imageQuality=0.2"];

/** This causes the image to be updated if it is complete, otherwise it just continues */
WLController.prototype.updateImage = function WLC_updateImage() {
	if( !this.loadImage ) {
		if( this.width == this.loadWidth && this.center==this.loadCenter ) return;
		if( this.delayTime ) {
			if( this.delayTime<(new Date()).getTime() ) this.delayTime = undefined;
			else return;
		};
		this.loadStartWidth = this.width;
		this.loadStartCenter = this.center;
		this.tries = 0;
		var url = this.baseUrl + this.qualityOpts[this.qualityLevel] + "&windowWidth="+this.width+"&windowCenter="+this.center;
		//console.info("Loading %s", url);
		this.loadImage = new Image();
		this.loadImage.src = url;
		this.loadStart = (new Date()).getTime();
	}

	if( this.loadImage.complete || this.tries > 10 ) {
		if( this.waitCursor ) {
			defaultCursor();
			this.waitCursor = false;
		}
		this.loadWidth = this.loadStartWidth;
		this.loadCenter = this.loadStartCenter;
		this.setImageUrl(this.loadImage.src);
		this.wlIndicator.innerHTML = "W/C:"+this.loadWidth+"/"+this.loadCenter;
		this.loadImage = null;
		this.cnt++;
		this.totalCnt++;
		var now = (new Date()).getTime();
		this.timeSum = this.timeSum + now-this.loadStart;
		var avg = this.timeSum/this.cnt;
		var len = (avg>0 ? 1000/avg : "infinite");
		if((this.totalCnt % 10)==0) console.info("Average frame time",avg,"ms/frame rate",len,"frame/s","now",(now % 100000));
		if( this.cnt > 20 ) {
			this.cnt--;
			this.timeSum = Math.floor(this.timeSum*0.95);
		}
		if( avg > 120 ) { 
			this.qualityLevel++;
			if( this.qualityLevel>=this.qualityOpts.length ) {
				this.qualityLevel--;
			} else {
				console.info("Dropping quality level (higher is lower quality):",this.qualityLevel,"options",this.qualityOpts[this.qualityLevel]);
				this.timeSum = 0;
				this.cnt = 0;
			}
		} else if( avg < 80  ) {
			if( this.qualityLevel>0 ) {
				console.info("Increasing quality level to", this.qualityLevel);
				this.qualityLevel--;
			} else this.delayTime = (new Date()).getTime()+64;			
		}
	} else {
		this.tries++;
	}
};

/** Update the window level in the merged context and re-display the screen once the new image is fetched */
WLController.prototype.end = function WLC_end(event) {
	this.controller.userEvent("wlEnd", this.totalCnt);
	if( this.loadWidth && (this.loadWidth!=this.origWidth || this.loadCenter!=this.origCenter) ) {
		console.debug("Setting WL c/w=%d,%d", this.loadCenter, this.loadWidth);
		this.layout.navigate.parent.setMerged("windowCenter", this.loadCenter);
		this.layout.navigate.parent.setMerged("windowWidth", this.loadWidth);
	}

	// Clear the values.
	this.wlIndicator.style.display="none";
	this.wlIndicator=null;
	defaultCursor();
	this.loadImage = null;
	this.image = undefined;
	this.layout = undefined;
	this.baseUrl = undefined;
	this.controller.waitUpdate("mouseUp",5);
};

/** Sets the default mode to window levelling */
XeroController.prototype.imageMouseHandler = new WLController();

