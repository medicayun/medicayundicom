/** Handles the panning/zooming controls */

/**
 * Sets the zoom mode, and sets the region back to the original region if the zoom mode was
 * already scale to fit.
 */
 
XeroController.prototype.zoomReset = function (lay,e, preventUpdate) {
	if( !preventUpdate ) this.userEvent("zoomReset");
	this.zoomMode(lay,e,null,preventUpdate);
};
XeroController.prototype.shift_clickImage = XeroController.prototype.zoomReset;

/** Sets the scale to fit mode */
XeroController.prototype.scaleToFit = function (lay,e) {
	this.userEvent("scaleToFit");
	this.zoomMode(lay,e,"SCALE_TO_FIT");
};

/** Sets magnify mode */
XeroController.prototype.magnify = function(lay,e) {
	this.userEvent("magnify");
	this.zoomMode(lay,e,"MAGNIFY");
};

/** Sets true size mode */
XeroController.prototype.trueSize = function(lay,e) {
	this.userEvent("trueSize");
	this.zoomMode(lay,e,"TRUE_SIZE");
};

/** Toggles modes */
XeroController.prototype.sizeToggle = function(lay,e) {
	lay = this.findLayout(lay);
	var nav = this.getNavigate(lay);
	var size = nav.parent.merged.get("size");
    console.warn("Toggling size from",size);
	if( size=="MAGNIFY" ) size = "TRUE_SIZE";
	else if( size==="TRUE_SIZE" ) size = "SCALE_TO_FIT";
	else size = "MAGNIFY";
	this.zoomMode(lay,e,size);
};

/** Handles setting a given zoom mode, and re-setting the region */
XeroController.prototype.zoomMode = function(lay,e,mode,preventUpdate) {
	lay = this.findLayout(lay);
	var nav = this.getNavigate(lay);
	nav.setMerged("smallImage",true);
	console.info("Resetting region on",lay.id);
	// Reset the topLeft/bottomRight instead of setting the mode
	nav.parent.setMerged("size", mode);
	nav.parent.setMerged("topLeft");
	nav.parent.setMerged("bottomRight");
	nav.parent.setMerged("magnify");
	
	if( !preventUpdate ) this.waitUpdate("zoomMode",5);
};

/** 
 * Handles zooming in/out.  Delta is a +/- integer increment.  For Magnify mode, it means
 * change by that many powers of two, ignored for true size (which can't change magnification),
 * and scale to fit it changes the magnification by 10%
 */
XeroController.prototype.zoomChange = function(lay,e,delta,isZoomToMousePoint) {
    console.info("Zoom change", delta);
	if( !(delta && isFinite(delta)) ) {
		console.warn("Non-finite zoom change:",delta);
		return;
	}
	var nav = this.getNavigate(lay);
	nav.setMerged("smallImage",true);
	var size = nav.merged.get("size");
	if( size==="TRUE_SIZE" ) {
		console.warn("Can't change magnification in true size mode.");
		return;
	}
	if( size==="MAGNIFY" ) {
		var mag = nav.merged.get("magnify");
		if( !mag ) mag = 1.0;
		while(delta>0 && mag < 16) {
			mag = mag * 2;
			delta--;
		}
		var rows = nav.external.Rows;
		while(delta<0 && rows*mag>64) {
			mag = mag/2;
			delta++;
		}
		nav.parent.setMerge("magnify",mag);
	}
	else {
		// In IE, remove the full-resolution image (shape) to speed up zoom.
    	var small = document.getElementById("im" + lay.id + "SmallShape");
    	if(small)
    	{
    		var full = document.getElementById("im" + lay.id + "Shape");
    	    if(full)
    	    {
    	    	full.parentNode.removeChild(full);
    	    }
    	}
    	
		// Make sure we have an image level instance.
		if( !nav.external.Rows ) nav = nav.getChild(0);
		
		var tl = nav.merged.get("topLeft");
		var br = nav.merged.get("bottomRight");
		var rows = nav.external.Rows;
		var cols = nav.external.Columns;
		if(!tl) tl = [0,0];
		if(!br) br = [cols,rows];

    	var preZoomRect = new ZoomRectangle(tl, br);
    	
    	var zoomDelta = new Array(2);
	    zoomDelta[0] = Math.floor(preZoomRect.getWidth()*0.05*delta);
		zoomDelta[1] = Math.floor(preZoomRect.getHeight()*0.05*delta);

		if( (preZoomRect.getWidth() - 2 * zoomDelta[0] < 16) || (preZoomRect.getHeight( ) - 2 * zoomDelta[1] < 16) ) {
			console.warn("Too small a zoom.");
			return;
		}
		if( (preZoomRect.getWidth() - 2 * zoomDelta[0] > cols * 16) || (preZoomRect.getHeight() - 2 * zoomDelta[1] > rows * 16) ) {
			console.warn("Too large a zoom."); 
			return;
		}
		
		var zoomRect;
		if(isZoomToMousePoint)
		{
    	   var mousePtInDocCoords = docCoords(e);
    	
	       zoomRect = this.getZoomRectangle(mousePtInDocCoords, lay, preZoomRect, zoomDelta);
		}
		else
		{
		   zoomRect = this.calculateZoomRectangle(preZoomRect, zoomDelta);
		}
		
		if( !(isFinite(zoomRect.topLeft[0]) && isFinite(zoomRect.topLeft[1]) && isFinite(zoomRect.bottomRight[0]) && isFinite(zoomRect.bottomRight[1])) ) {
			console.warn("New region isn't finite:",zoomRect.topLeft,zoomRect.bottomRight);
		} else { 
			nav.parent.merged.topLeft = zoomRect.topLeft;
			nav.parent.setMerged("bottomRight", zoomRect.bottomRight, true);
		}
	}
	this.waitUpdate("zoomMode",5);
};

/**
 * Gets the rectangle defining the zoom for a given mouse position, zoom (x,y) delta and pre zoom (i.e. current) rectangle.
 */
XeroController.prototype.getZoomRectangle = function(mousePtInDocCoords, imageLayout, preZoomRect, zoomDelta)
{
    var vmlElem = document.getElementById("vml" + imageLayout.id);
    if(vmlElem)
    {
        return this.getVmlZoomRectangle(vmlElem, mousePtInDocCoords, imageLayout, preZoomRect, zoomDelta);
    }
    
    var svgElem = document.getElementById("tr" + imageLayout.id);
    if(svgElem)
    {
        return this.getSvgZoomRectangle(svgElem, mousePtInDocCoords, imageLayout, preZoomRect, zoomDelta);
    }
};

/**
 * Calculates the rectangle to define the zoom given the pre zoom rectangle, the zoom (x,y) delta and
 * pre and post zoom image space mouse points.
 */
XeroController.prototype.calculateZoomRectangle = function(preZoomRect, zoomDelta, preZoomMousePt, postZoomMousePt)
{
    var zoomOffset = [0, 0];
    if(preZoomMousePt && postZoomMousePt)
    {
        zoomOffset[0] = Math.round(preZoomMousePt[0] - postZoomMousePt[0]);
        zoomOffset[1] = Math.round(preZoomMousePt[1] - postZoomMousePt[1]);
    }
    
    var zoomRectangle = new ZoomRectangle();
    zoomRectangle.topLeft[0] = preZoomRect.topLeft[0] + zoomDelta[0] + zoomOffset[0];
    zoomRectangle.topLeft[1] = preZoomRect.topLeft[1] + zoomDelta[1] + zoomOffset[1];

    zoomRectangle.bottomRight[0] = preZoomRect.bottomRight[0] - zoomDelta[0] + zoomOffset[0];
    zoomRectangle.bottomRight[1] = preZoomRect.bottomRight[1] - zoomDelta[1] + zoomOffset[1];
    
    return zoomRectangle;
};

/**
 * Gets the zoom rectangle for SVG images.  The zoom rectangle is such that image point under the mouse cursor
 * stays constant before and after the zoom.
 */
XeroController.prototype.getSvgZoomRectangle = function(svgElem, mousePtInDocCoords, imageLayout, preZoomRect, zoomDelta)
{
    var postZoomCoordSize = new Array(2);
    postZoomCoordSize[0] = preZoomRect.getWidth() - 2 * zoomDelta[0];
    postZoomCoordSize[1] = preZoomRect.getHeight() - 2 * zoomDelta[1];
    
    var scale = new Array(2);
    scale[0] = postZoomCoordSize[0] / preZoomRect.getWidth();
    scale[1] = postZoomCoordSize[1] / preZoomRect.getHeight();
    
    var toDocMatrix = svgElem.parentNode.getScreenCTM();
    
    if(imageLayout.rotation)
    {
        toDocMatrix = toDocMatrix.rotate(-imageLayout.rotation);
    }
    
    if(imageLayout.flip)
    {
        toDocMatrix = toDocMatrix.flipX();
    }
    
    var toImageMatrix = toDocMatrix.inverse();
    
    var toImageTranslation = [toImageMatrix.e, toImageMatrix.f];
    if(imageLayout.rotation && imageLayout.rotation == 90 || imageLayout.rotation == 270)
    {
        if(!imageLayout.flip)
        {
            toImageTranslation[0] = -toImageMatrix.e;
            toImageTranslation[1] = -toImageMatrix.f;
        }
    }
        
    var mousePtInImage = new Array(2);
    mousePtInImage[0] = toImageMatrix.a * mousePtInDocCoords[0] + toImageMatrix.b * mousePtInDocCoords[1] + toImageTranslation[0];    		  
    mousePtInImage[1] = toImageMatrix.c * mousePtInDocCoords[0] + toImageMatrix.d * mousePtInDocCoords[1] + toImageTranslation[1];

    var postZoomTopLeft = [preZoomRect.topLeft[0] + zoomDelta[0], preZoomRect.topLeft[1] + zoomDelta[1]];

    var svgOrigin = new Array(2);
    svgOrigin[0] = toDocMatrix.a * preZoomRect.topLeft[0] + toDocMatrix.b * preZoomRect.topLeft[1] + toDocMatrix.e;
    svgOrigin[1] = toDocMatrix.c * preZoomRect.topLeft[0] + toDocMatrix.d * preZoomRect.topLeft[1] + toDocMatrix.f;

    var toZoomedImageMatrix = new Object();
    toZoomedImageMatrix.a = scale[0] * toImageMatrix.a;
    toZoomedImageMatrix.b = scale[0] * toImageMatrix.b;
    toZoomedImageMatrix.c = scale[1] * toImageMatrix.c;
    toZoomedImageMatrix.d = scale[1] * toImageMatrix.d;
            
    toZoomedImageMatrix.e = postZoomTopLeft[0] - (toZoomedImageMatrix.a * svgOrigin[0] + toZoomedImageMatrix.b * svgOrigin[1]);            
    toZoomedImageMatrix.f = postZoomTopLeft[1] - (toZoomedImageMatrix.c * svgOrigin[0] + toZoomedImageMatrix.d * svgOrigin[1]);
                
    var mousePtInZoomedImage = new Array(2);
    mousePtInZoomedImage[0] = toZoomedImageMatrix.a * mousePtInDocCoords[0] + toZoomedImageMatrix.b * mousePtInDocCoords[1] + toZoomedImageMatrix.e;    		  
    mousePtInZoomedImage[1] = toZoomedImageMatrix.c * mousePtInDocCoords[0] + toZoomedImageMatrix.d * mousePtInDocCoords[1] + toZoomedImageMatrix.f;
    
    return this.calculateZoomRectangle(preZoomRect, zoomDelta, mousePtInImage, mousePtInZoomedImage);
};

/**
 * Gets the zoom rectangle for VML images.  The zoom rectangle is such that image point under the mouse cursor
 * stays constant before and after the zoom.
 */
XeroController.prototype.getVmlZoomRectangle = function(vmlElem, mousePtInDocCoords, imageLayout, preZoomRect, zoomDelta)
{
    var vmlParentOrigin = getElementDocCoords(vmlElem.offsetParent);

    var vmlParentCenter = new Array(2);
    vmlParentCenter[0] = vmlParentOrigin[0] + (vmlElem.offsetParent.offsetWidth / 2);
    vmlParentCenter[1] = vmlParentOrigin[1] + (vmlElem.offsetParent.offsetHeight / 2);
    
    var preZoomCoordSize = [preZoomRect.getWidth(), preZoomRect.getHeight()];
    
    var vmlElemSize = new Array(2);
    vmlElemSize[0] = XeroController.convertToBase(vmlElem.style.width, vmlElem.offsetParent.offsetWidth, 1);
    vmlElemSize[1] = XeroController.convertToBase(vmlElem.style.height, vmlElem.offsetParent.offsetHeight, 1);
    
    var preZoomMousePtInImage = this.getDocPtInVmlImage(mousePtInDocCoords, vmlElem, imageLayout, preZoomRect.topLeft, vmlElemSize, preZoomCoordSize, vmlParentOrigin, vmlParentCenter);
    
    var postZoomImageTopLeft = new Array(2);
    postZoomImageTopLeft[0] = preZoomRect.topLeft[0] + zoomDelta[0];
    postZoomImageTopLeft[1] = preZoomRect.topLeft[1] + zoomDelta[1];
    
    var postZoomCoordSize = new Array(2);
    postZoomCoordSize[0] = preZoomCoordSize[0] - 2 * zoomDelta[0];
    postZoomCoordSize[1] = preZoomCoordSize[1] - 2 * zoomDelta[1];
    
    var postZoomMousePtInImage = this.getDocPtInVmlImage(mousePtInDocCoords, vmlElem, imageLayout, postZoomImageTopLeft, vmlElemSize, postZoomCoordSize, vmlParentOrigin, vmlParentCenter);
    
    return this.calculateZoomRectangle(preZoomRect, zoomDelta, preZoomMousePtInImage, postZoomMousePtInImage);
};

/**
 * Gets the VML image space coordinates of the given document space point.
 */
XeroController.prototype.getDocPtInVmlImage = function(docPt, vmlElem, imageLayout, imageTopLeft, vmlElemSize, coordSize, parentOrigin, parentCenter)
{
    var scale = new Array(2);
    scale[0] = coordSize[0] / vmlElemSize[0];
    scale[1] = coordSize[1] / vmlElemSize[1];

    var vmlTopLeft = new Array(2);
    vmlTopLeft[0] = XeroController.convertToBase(imageLayout.translateXPercent + "%", vmlElem.offsetParent.offsetWidth, 1);    
    vmlTopLeft[1] = XeroController.convertToBase(imageLayout.translateYPercent + "%", vmlElem.offsetParent.offsetHeight, 1);

    var vmlOrigin = new Array(2);        
    vmlOrigin[0] = parentOrigin[0] + vmlTopLeft[0] - (imageTopLeft[0] / scale[0]);
    vmlOrigin[1] = parentOrigin[1] + vmlTopLeft[1] - (imageTopLeft[1] / scale[1]);
    
    var docPtRelToParentCenter = new Array(2);
    docPtRelToParentCenter[0] = docPt[0] - parentCenter[0];
    docPtRelToParentCenter[1] = docPt[1] - parentCenter[1];
    
    if(imageLayout.flip)
    {
        docPtRelToParentCenter[0] = -docPtRelToParentCenter[0];
    }

    var imagePt = new Array(2);
    if(imageLayout.rotation)
    {
        if(imageLayout.rotation == 90)
        {
            imagePt[0] = docPtRelToParentCenter[1];
            imagePt[1] = -docPtRelToParentCenter[0];
        }
        else if(imageLayout.rotation == 180)
        {
            imagePt[0] = -docPtRelToParentCenter[0];
            imagePt[1] = -docPtRelToParentCenter[1];
        }
        else if(imageLayout.rotation == 270)
        {
            imagePt[0] = -docPtRelToParentCenter[1];
            imagePt[1] = docPtRelToParentCenter[0];
        }
        else
        {
            var radians = imageLayout.rotation*Math.PI/180;
	        var cs = Math.cos(radians);
	        var sn = Math.sin(radians);
	        
	        imagePt[0] = cs * docPtRelToParentCenter[0] + sn * docPtRelToParentCenter[1];
	        imagePt[1] = -sn * docPtRelToParentCenter[0] + cs * docPtRelToParentCenter[1];
        }
    }
    else
    {
        imagePt[0] = docPtRelToParentCenter[0];
        imagePt[1] = docPtRelToParentCenter[1];
     };
    
    imagePt[0] += parentCenter[0];
    imagePt[1] += parentCenter[1];
    
    imagePt[0] -= vmlOrigin[0];
    imagePt[1] -= vmlOrigin[1];
    
    imagePt[0] *= scale[0];
    imagePt[1] *= scale[1];
    
    return imagePt;
};

XeroController.prototype.Zoom_wheelSeries = function(id,e) {
	var lay = this.findLayout(id, Navigation.IMAGE).children[0];
	
	var wheelData = e.detail ? (-e.detail) : e.wheelDelta/40;
	if(! e.shiftKey ) wheelData = wheelData/3;
	else wheelData = wheelData*3;
	this.userEvent("zoomWheel",wheelData);
	
	this.zoomChange(lay,e,wheelData,true);
};
XeroController.prototype.Zoom_shift_wheelSeries = XeroController.prototype.Zoom_wheelSeries;

XeroController.prototype.zoomIn = function(id,e) {
	this.userEvent("zoomIn");
	this.userEvent("logZoomIn");
	var lay = this.findLayout(id,Navigation.IMAGE);
	this.zoomChange(lay,e,1,false);
};
XeroController.prototype.zoomOut = function(id,e) {
	this.userEvent("zoomOut");
	this.userEvent("logZoomOut");
	var lay = this.findLayout(id,Navigation.IMAGE);
	this.zoomChange(lay,e,-1,false);
};

/** Handles panning the image - causes the image to be moved around on screen.  
 * At the end of the pan, updates the region of the image 
 */
function PanController() {
};
PanController.prototype = new ImageMouseHandler();

XeroController.prototype.Zoom_dragImage = new PanController();
XeroController.prototype.dragImage = XeroController.prototype.Zoom_dragImage;
XeroController.prototype.shift_dragImage = XeroController.prototype.Zoom_dragImage;
XeroController.prototype.WindowLevel_dblDragImage = XeroController.prototype.Zoom_dragImage; 

PanController.prototype.prepare= function(e) {
	this.prepareBase(e);
	
	this.controller.userEvent("panStart");
	this.gtr = document.getElementById("tr"+this.lay.id);
	if(this.gtr) return this.prepareSvg();
	this.vml = document.getElementById("vml"+this.lay.id);
	if( this.vml ) return this.prepareVml();
	console.warn("Can't find any object to pan?");
	return false;
}; 


/** Compute the scale for VML (IE) */
PanController.prototype.prepareVml = function() {
		console.info("Found a VML element",this.vml.id,"to zoom/pan");
		console.info("Left,Top is", this.vml.style.left,this.vml.style.top);
		console.info("VML coord origin:", this.vml.coordorigin.x, this.vml.coordorigin.y);		
		var width = this.vml.parentNode.offsetWidth;
		var height = this.vml.parentNode.offsetHeight;
		this.finalScaleX = this.vml.coordsize.x/XeroController.convertToBase(this.vml.style.width, width, 1);
		this.finalScaleY = this.vml.coordsize.y/XeroController.convertToBase(this.vml.style.height,height,1);

	    this.baseX = this.vml.coordorigin.x;
	    this.baseY = this.vml.coordorigin.y;
	    console.info("prepare baseX,Y=",this.baseX, this.baseY);
        this.scaleX = -this.finalScaleX;
        this.scaleY = -this.finalScaleY;
	    
		return true;
};

/** Computes the scale translation for an SVG pan */
PanController.prototype.prepareSvg = function() {
		console.info("Found an SVG g transform object", this.gtr.id);
		this.baseTransform = this.gtr.attributes.getNamedItem("transform").value;
		console.info("Found base transform", this.baseTransform);
		var ctm = this.gtr.parentNode.getScreenCTM();
		// This object only has scale factors, not rotation.
		this.scaleX = 1/ctm.a;
		this.scaleY = 1/ctm.d;
		console.info("Scale x,y=",this.scaleX, this.scaleY);
		return true;
};

/** Start active panning */
PanController.prototype.start = function(e) {
	return true;
};

/** Handle the pan movement - update the transform for SVG, and the style for VML */
PanController.prototype.mousemove = function(e) {
	var posn = docCoords(e);
	if( this.gtr ) this.mousemoveSvg(e,posn);
	else if( this.vml ) this.mousemoveVml(e,posn);
};

/** Compute the amount the mouse has moved since the start, and apply it the the 
 * VML style left/top position.
 */
PanController.prototype.mousemoveVml = function(e,posn) {
	var dx = posn[0]-this.startPosn[0] ;
	var dy = posn[1]-this.startPosn[1] ;
	this.dx = dx;
	this.dy = dy;
	dx = dx*this.scaleX+this.baseX;
	dy = dy*this.scaleY+this.baseY;
//	console.info("dx,dy=",dx,",",dy,"on", this.vml.id);
//	console.info("vml coordorigin ", this.vml.coordorigin.x, this.vml.coordorigin.y);
	this.vml.coordorigin.x = dx;
	this.vml.coordorigin.y = dy;
};

/** Compute the amount the mouse has moved since the start, and apply it to the
 * SVG transform value.
 */
PanController.prototype.mousemoveSvg = function(e,posn) {
	this.dx = Math.floor((posn[0]-this.startPosn[0])*this.scaleX);
	this.dy = Math.floor((posn[1]-this.startPosn[1])*this.scaleY);
	var transform = "translate("+this.dx+","+this.dy+") "+this.baseTransform;
	if( this.transform==transform ) return;
	this.e = e;
	this.transform = transform;
	//console.info("Pan delta",this.dx,",",this.dy,"transform",this.transform);
	this.gtr.attributes.getNamedItem("transform").value = this.transform;
};


/** Return the sum of two points, the first point as a comma-separate string, the
 * second directly specified as two values.
 */
PanController.prototype.sumPoint = function(p1,dx,dy) {
	p1 = parsePoint(p1);
	return [p1[0]+dx,p1[1]+dy];
};

/** Handle the end of the pan movement - compute the topLeft, bottomRight based on the
 * dx,dy coordinates as set in the mousemove functions, and then rerender.  THis should
 * result in the image  */
PanController.prototype.end = function(e) {
	this.controller.userEvent("panEnd", "dx="+this.dx+" dy="+this.dy);
	var tl = this.lay.navigate.merged.get("topLeft");
	var br =this.lay.navigate.merged.get("bottomRight");
	if( !tl ) tl = "0,0";
	if( !br ) br = this.lay.navigate.external.Columns+","+this.lay.navigate.external.Rows;
	var dx = this.dx;
	var dy = this.dy;
	if( this.vml ) {
		dx = Math.floor(dx*this.finalScaleX);
		dy = Math.floor(dy*this.finalScaleY);
		console.info("VML dx,dy=",dx,dy);
	}
	
	// Convert dx,dy into IMAGE coordinate changes
	// Apply dx,dy to tl, br	
	var rows = this.lay.navigate.external.Rows;
	var cols = this.lay.navigate.external.Columns;
	
	var zoomRect = new ZoomRectangle(this.sumPoint(tl,-dx,-dy), this.sumPoint(br,-dx,-dy));
    zoomRect = XeroController.getOnScreenZoomRectangle(zoomRect, rows, cols);
		
	// The pre-load image should not be created for this, so just call the notify on the br
	this.lay.navigate.parent.merged.topLeft=zoomRect.topLeft;
	this.lay.navigate.parent.setMerged("bottomRight", zoomRect.bottomRight, true);

	this.controller.waitUpdate("pan",5);
};