/**
 * This section defines some special image layout handling
 */
function ImageLayout(id, nth) {
	this.baseConstructor(id);
	this.nth = nth;
	this.navigate = "image:"+nth;
};

ImageLayout.prototype = new Layout("baseImage","image/image");
ImageLayout.prototype.isDebug = false;

ImageLayout.prototype.baseConstructor = Layout;

// Scale no smaller than fetchSize when fetching,
// and fetch on fetchSize index boundaries 
ImageLayout.prototype.fetchSize = 256;
ImageLayout.prototype.MAGNIFY = "MAGNIFY";
ImageLayout.prototype.SCALE_TO_FIT = "SCALE_TO_FIT";
ImageLayout.prototype.TRUE_SIZE = "TRUE_SIZE";
ImageLayout.prototype.events = [".","../merged:windowWidth","../merged:windowCenter","../merged:invert","../merged:rotation","../merged:horizontalFlip","../merged:size", "../merged:topLeft","../merged:bottomRight"];
ImageLayout.prototype.area="Image";
ImageLayout.prototype.isDiffUpdate = true;


/** This defines the listeners for navigate changes to the layout */
ImageLayout.prototype.navigateListener = function ImageMergedListener(navigate) {
	this.debug("Image merged listener");
	this.navigate = navigate;
	if( navigate && this.image!==navigate.external ) {
		this.debug("Updating the merged setting:",this.id);
		this.relayout = true;
		this.image = navigate.external;
		this.merged = navigate.getMerged();
		if( this.image ) {
			this.Position1 = 1+parseInt(this.image.Position);
		}
		this.updatePosition();
		this.addImageUrl(true);
	}
};

/** Update the URLs if the window width changes */
ImageLayout.prototype.namedListener = function(nav,vname,val,prevVal) {
	if( prevVal===val && val===undefined ) return;
	this.debug("Updating all image URLs for",nav.external.seriesUID);
	this.relayout = true;
	if( vname==="horizontalFlip" || vname==="rotation" || vname==="size" || vname==="bottomRight" || vname==="topLeft") {
		this.updatePosition();
	}
	this.addImageUrl(true);
};

/** Adds an image URL to the set of URLs to display */
ImageLayout.prototype.addImageUrl = function AddImageUrl(clear) {
	if( clear ) {
	   this.imageError = null;
	   this.imageUrls = new Array();
	}
	if( (!this.image) || !(this.width) || !(this.height) ) return;
	if( this.navigate.merged.get("smallImage") ) {
		this.imageUrls.push(this.createSmallImage(XERO_QUERY));
	}
	this.imageUrls.push(this.createFullUrl(XERO_QUERY));
	this.preloadImage = new Image();
	var src = this.imageUrls[this.imageUrls.length-1].imageUrl;
	this.preloadImage.src = src;
	this.debug("Created a pre-load image for", src);
};

/** Listens for changes in size and re-calculates the URLs to use - LEAVING the old one in place
 * so that it continues to show until the new one is ready. (Leaving the old one is temporarily disabled
 * while bugs are worked out around re-sizing - this leaving should occur only if the old image has 
 * been fetched and is immediately visible - this change may cause some hiccups to occur for a while.)
 */
ImageLayout.prototype.resizeListener = function ImageResizeListener() {
	this.updatePosition();
	this.addImageUrl(true);
};
 
/** Updates the image position information
 */
ImageLayout.prototype.updatePosition = function ImageLayout_updatePosition() {
	if(!this.merged ) {
		return;
	}
	
	// Need to add layout & query information to make the image fit the displayed area.
	this.layoutType = this.merged.get("size");
	this.debug("Found layout type %s", this.layoutType);
	if(!this.layoutType ) this.layoutType = this.SCALE_TO_FIT;
	// Reset the positional information...
	this.scale = 1;
	this.scaleY = 1;
	this.translateX = 0;
	this.translateY = 0;
	this.rotation = undefined;
	this.flip = false;
	this.aspectPercent = undefined;
	this.ae = this.navigate.external.get("ae");
	
	var tl = this.merged.get("topLeft");
	if( tl ) {
	    // The points are all backwards and the first set are offset by 1 each as they are starting/ending points
		var br = this.merged.get("bottomRight");
		this.region = parsePoint(tl+","+br);
		this.region[0] = this.region[0]-1;
		this.region[1] = this.region[1]-1;
		this.debug("Using specified region %s", this.region);
	} else {
		this.region = [0,0,this.image.Columns, this.image.Rows];
	}
	
	this.debug("Region to display %s in %d,%d", this.region, this.width,this.height);
	if( !this.width ) return;
	
	this.computeSpatialTransform();
	this.computeScale();
	this.computeTranslate();
};

/** Indicate if the image load is complete */
ImageLayout.prototype.isComplete = function ImageLayout_isComplete() {
	if( this.preloadImage===undefined ) return true;
	if( this.preloadImage.complete ) {
		this.debug("Image %s is complete.", this.preloadImage.src);
		this.preloadImage = undefined;
		return true;
	};
	this.debug("Waiting for %s", this.preloadImage.src);
	return false;
};

/** This generates a small URL reference for the entire image area, but at a reduced resolution/quality */
ImageLayout.prototype.createSmallImage = function(base) {
	var ret = this.createBaseUrl(base);
	ret.imageId += "Small";
	ret.imageUrl = ret.imageUrl + "&imageQuality=0.4";
	var rows = this.image.Rows;
	while(rows>512) rows = rows/2;
	rows = Math.floor(rows);
	ret.rows = this.image.Rows;
	ret.cols = this.image.Columns;	
	ret.imageUrl = ret.imageUrl + "&rows="+rows;
	return ret;
};

/** Creates the base URL object */
ImageLayout.prototype.createBaseUrl = function (base) {
	var uids = "&studyUID="+this.image.get("studyUID")+"&seriesUID="+this.image.get("seriesUID");
	var ret = { "imageUrl" : base+uids+"&objectUID="+this.image.objectUID, "top":0, "left":0, "imageId":"im"+this.id};
	if( this.ae ) {
		ret.imageUrl = ret.imageUrl + "&ae="+this.ae;
	}
	if( this.image.frame ) {
		ret.imageUrl = ret.imageUrl + "&frameNumber="+this.image.frame;
	}
	var ww = this.merged.get("windowWidth");
	if( ww ) {
		var wc = this.merged.get("windowCenter");
		ret.imageUrl = ret.imageUrl + "&windowWidth="+ww+"&windowCenter="+wc;
	}
	var isinvert = this.merged.get("invert");
	if( isinvert && isinvert!=="FALSE" && isinvert!=="false" ) {
		this.debug("Invert is true.");
		// Changes from the natural presentation to the inverted presentation - this is an OVERRIDE
		// on the base value or base value + GSPS value, it isn't the base value itself.
		ret.imageUrl = ret.imageUrl+"&invert=true";
	};
	var puid = this.merged.get("gspsUID");
	if( puid ) {
		ret.imageUrl = ret.imageUrl + "&presentationUID="+puid;
	}
	return ret;
};

/** This generates a URL object that includes positioning/offset information 
 * as well as the actual URL string to display the image.
 * This is technically controller code, but it is so intimately connected with this model
 * details that it is better to keep it part of the model.  
 */
ImageLayout.prototype.createFullUrl = function ImageLayout_createFullUrl(base) {
	var ret = this.createBaseUrl(base);
	
	var intScale = 1;
	// Only fetch sizes down to 256 -otherwise fetch full size.
	var testScale = this.scale*2;
	for(ret.fetchScale=1; ret.fetchScale < this.image.Rows/this.fetchSize; ret.fetchScale *=2 ) {
		if( testScale >= intScale ) break;
		intScale = intScale/2;
	}
	
	this.debug("ImageLayout scale %f, fetchScale %f",this.scale, ret.fetchScale);

	var coords = [0,0, 0,this.height, this.width,0, this.width,this.height];
	
	// Figure out the region that needs to be fetched, in image coordinates.
	var xmin = this.image.Columns;
	var ymin = this.image.Rows;
	var xmax = 0;
	var ymax = 0;
	var i,n=coords.length;
	var pnt;
	for(i=0; i<n; i+=2) {
		pnt = this.toImage(coords,i);
		xmin = Math.min(xmin,pnt[0]);
		xmax = Math.max(xmax,pnt[0]);
		ymin = Math.min(ymin,pnt[1]);
		ymax = Math.max(ymax,pnt[1]);
	};
	xmin = Math.max(0,xmin) / ret.fetchScale;
	ymin = Math.max(0,ymin) / ret.fetchScale;
	xmax = Math.min(this.image.Columns,xmax) / ret.fetchScale;
	ymax = Math.min(this.image.Rows,ymax) / ret.fetchScale;
	
	// Now, figure out the size of that region in display coordinates
	xmin = (xmin - (xmin % this.fetchSize))*ret.fetchScale;
	ymin = (ymin - (ymin % this.fetchSize))*ret.fetchScale;
	xmax = (xmax + this.fetchSize - (xmax % this.fetchSize))*ret.fetchScale;
	ymax = (ymax + this.fetchSize - (ymax % this.fetchSize))*ret.fetchScale;
	xmax = Math.min(this.image.Columns,xmax);
	ymax = Math.min(this.image.Rows,ymax);

	ret.cols = xmax-xmin;
	ret.rows = ymax-ymin;
	ret.top = ymin;
	ret.left = xmin;
	if( xmin!==0 || ymin!==0 || xmax!==this.image.Columns || ymax!==this.image.Rows ) {
		this.debug("Fetching sub-region %d,%d,%d,%d", xmin,ymin,xmax,ymax);
		ret.imageUrl = ret.imageUrl + "&region="+xmin/this.image.Columns+","+ymin/this.image.Rows+","+xmax/this.image.Columns+","+ymax/this.image.Rows;
		ret.isSubregion = true;
	}
	if( ret.fetchScale!=1 ) {
		this.debug("Fetching at scale %d",ret.fetchScale);
		ret.imageUrl = ret.imageUrl + "&rows=" + Math.floor((ymax-ymin)/ret.fetchScale);
	}
	return ret;
};
 
/**
 * Compute the translation so that the center of the display area is co-incident with the overall area.
 */
ImageLayout.prototype.computeTranslate = function ImageLayout_computeTranslate() {
	// dispX,Y is the display point we want co-incident with the image point.
	// Remember that the image area is translated first, then scaled, then rotated
	var dispX = this.width/2;
	var dispY = this.height/2;
	this.debug("Half x,y=%f,%f  region %s", dispX, dispY, this.region);
	var imgTL = this.toDisplay(this.region,0);
	var imgBR = this.toDisplay(this.region,2);
	this.debug("imgTL %s imgBR %s", imgTL, imgBR);
	
	var imgX = (imgBR[0]-imgTL[0])/2+imgTL[0];
	var imgY = (imgBR[1]-imgTL[1])/2+imgTL[1];
	this.debug("imgX,Y = %d,%d", imgX, imgY);
	this.translateX = dispX-imgX;
	this.translateY = dispY-imgY;

	if( this.translateX > -1 && this.translateX < 1 ) this.translateX = 0;
	if( this.translateY > -1 && this.translateY < 1 ) this.translateY = 0;

	// The XPercent is used for IE, and is the display percentage for the flipped/rotated top hand corner
	this.translateXPercent = Math.floor(50*(this.width-Math.abs(imgBR[0]-imgTL[0]))/this.width);
	this.translateYPercent = Math.floor(50*(this.height-Math.abs(imgBR[1]-imgTL[1]))/this.height);

	this.debug("ImageLayout translation %d,%d", this.translateX, this.translateY);
};

/** 
 * Computes the spatial transformation.
 */
ImageLayout.prototype.computeSpatialTransform = function ImageLayout_computeSpatialTransform() {
	this.rotation = this.merged.get("rotation");
	var flip = this.merged.get("horizontalFlip");
	if( flip && flip!=="FALSE" && flip!=="false" ) this.flip = true;
	this.debug("Flip %s rotation %d", this.flip, this.rotation); 
	this.flipRotate = (this.flip || this.rotation);
};

/** Converts a display relative coordinate to an image relative one. */
ImageLayout.prototype.toImage = function ImageLayout_toImage(xy,offset) {
	if(offset===undefined) offset=0;
	var xd = xy[offset];
	var yd = xy[offset+1];
	if( this.translateX ) {
		xd = xd-this.translateX;
	}
	if( this.translateY ) {
		yd = yd-this.translateY;
	}
	if( this.flip )	xd = -xd;
	if( this.rotation ) {
		var xp,yp;
		if( this.rotation==90 ) {
			xp = yd;
			yp = -xd;
		} else if(this.rotation==180) {
			xp = -xd;
			yp = -yd;
		} else if(this.rotation==270) {
			xp = -yd
			yp = xd
		} else {
			var radians = 2*Math.PI-this.rotation*Math.PI/180;
			var cs = Math.cos(radians);
			var sn = Math.sin(radians);
			var xp = xd*cs-yd*sn;
			var yp = yd*cs+xd*sn;
		};
		xd = xp;
		yd = yp;
	};
	
	if( this.scale ) {
		xd = xd / this.scale;
		yd = yd / this.scaleY;
	}
	return [xd,yd];
}; 

/**
 * Computes the display relative coordinate from the image relative pair
 */
ImageLayout.prototype.toDisplay = function ImageLayout_toDisplay(xy,offset) {
	if(offset===undefined) offset=0;
	var xd = xy[offset];
	var yd = xy[offset+1];
	if( this.scale ) {
		xd = xd * this.scale;
		yd = yd * this.scaleY;
	}
	if( this.rotation ) {
		var xp,yp;
		if( this.rotation==90 ) {
			xp = -yd
			yp = xd
		} else if(this.rotation==180) {
			xp = -xd;
			yp = -yd;
		} else if(this.rotation==270) {
			xp = yd;
			yp = -xd;
		} else {
			var radians = this.rotation*Math.PI/180;
			var cs = Math.cos(radians);
			var sn = Math.sin(radians);
			var xp = xd*cs-yd*sn;
			var yp = yd*cs+xd*sn;
		};
		xd = xp;
		yd = yp;
	};
	if( this.flip )	xd = -xd;
	if( this.translateX ) {
		xd = xd+this.translateX;
	}
	if( this.translateY ) {
		yd = yd+this.translateY;
	}
	this.debug("toDisplay %f,%f to %f,%f", xy[offset],xy[offset+1],xd,yd);
	return [xd,yd];
}; 

/** Compute the scale to display the image at so that it all appears (or
 * for SCALE_TO_FIT, MAGNIFY or TRUE_SIZE, whatever scale is appropriate.)
 */
ImageLayout.prototype.computeScale = function ImageLayout_computeScale() {
	this.aspect = this.merged.get("aspect");
	if(this.aspect) {
		this.scaleY = this.aspect;
		this.aspectPercent = 100*this.scaleY;
	} 
	var imgTL = this.toDisplay(this.region,0);
	var imgBR = this.toDisplay(this.region,2);
	
	// View box is computed post-flip/rotate, and pre-scale
	// It sort of belongs as part of scale computation as the viewer itself computes the scale based on the same information as is here.
	this.coordX = Math.min(imgTL[0],imgBR[0]);
	this.coordY = Math.min(imgTL[1], imgBR[1]);
	this.coordWidth = Math.abs(imgBR[0]-imgTL[0]);
	this.coordHeight = Math.abs(imgBR[1]-imgTL[1]);
	this.debug("viewBox=%s %s %s %s", this.coordX, this.coordY, this.coordWidth, this.coordHeight);

	var imageX = Math.abs(imgBR[0]-imgTL[0]);
	var imageY = Math.abs(imgBR[1]-imgTL[1]);

	if( this.layoutType=="TRUE_SIZE") {
		var spx = this.merged.get("spacingX");
		if( !spx ) {
			console.warn("No spacing for true size -unable to apply true size.");
			this.layoutType = "SCALE_TO_FIT";
		}
	};

	if( this.layoutType=="TRUE_SIZE") {
		var spx = parseFloat(spx);
		var dpi = screen.logicalXDPI;
		if( !dpi ) dpi = screen.deviceXDPI;
		if( !dpi ) {
			dpi = 0.294;
			console.info("Using %f mm/pixel screen size - may not correspond to true screen size at fetch resolution.",dpi);
		}
		else {
			dpi = 25.4/dpi;
			this.debug("Using %f mm/pixel screen size as provided by screen.", dpi);
		}
		this.scale = spx/dpi;

		this.sameSize = true;
		this.negHalfRealWidth = Math.floor(-imageX*spx/2)+"mm";
		this.negHalfRealHeight = Math.floor(-imageY*spx/2)+"mm";
		this.realWidth = Math.floor(imageX*spx)+"mm";
		this.realHeight = Math.floor(imageY*spx)+"mm";		
	} else if( this.layoutType=="MAGNIFY" ) {
		var mag = this.merged.get("magnify");
		if( !mag ) mag = "1.0";
		this.scale = parseFloat(mag);
		this.sameSize = true;
		this.negHalfRealWidth = Math.floor(-imageX*this.scale/2)+"px";
		this.negHalfRealHeight = Math.floor(-imageY*this.scale/2)+"px";
		this.realWidth = Math.floor(imageX*this.scale)+"px";
		this.realHeight = Math.floor(imageY*this.scale)+"px";
	} else {
		this.sameSize = false;
		this.debug("Setting scale to fit type %s", this.layoutType);
		this.debug("imgTL = %s imgBR= %s region %s", imgTL, imgBR, this.region);
	
		var scaleX = this.width/imageX;
		var scaleY = this.height/imageY;
		this.debug("scale %f,%f", scaleX,scaleY);
		if( scaleX > scaleY ) scaleX = scaleY;
		this.scale = scaleX;
	}
	this.scaleY = this.scale;
	if( this.aspect ) this.scaleY = this.scale * this.aspect;
	this.debug("Computed scale %f,%f w,h=%f,%f",this.scale,this.scaleY);
	if( this.sameSize ) {
		this.debug("same size x,y=%s,%s w,h=%s,%s", this.negHalfRealWidth, this.negHalfRealHeight, this.realWidth, this.realHeight);
	}
	return this.scale;	
};