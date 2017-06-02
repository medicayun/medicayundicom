/**
 * The dynamic controller components are the parts that respond with interactive image presentation state
 * changes.
 * The dynamic events are:
 *    Window Level - while actually interacting
 *	  Panning
 *    Image Navigation within a series (TODO still)
 *	  Menu display/handling
 *
 * The non-dynamic events include things like rotation, flip, reset that 
 * get applied on image mouse down, but don't add any mouse listening controls.  These
 * types of events use the plug-in design for handling mouse down, but do not otherwise
 * handle ongoing mouse events.
 *
 * Use of shift/control/alt can modify the base events or cause an alternative event to
 * be used.
 * shift is used to modify the base action - making it faster or otherwise modifying it.
 * alt, ctrl are both used to choose different actions
 *
 * 
 */

/** Given the current mode, this looks for the mode handler to use for that mode/type.
 * The name is an _ separated name list, starting from the most general handler to the
 * most specific.  Each name is looked for as a function in this object, and if none is
 * found, then undefined is returned.
 * Typically, this will be something like:
 * ctrl_a_ImageArea
 * which is an image area key handler, for the a key with ctrl.
 * Or
 * WindowLevel_ctrl_left_ImageArea
 * meaning that it is an image area left item, with ctrl down, in window level mode.
 *
 * The mode handler name is returned as it could be used either as a function call or
 * as an object depending on the context.
 * This should NOT be used for interactive move/up events, but only for the initial,
 * triggerring event.  Also, if there is at text box or something equivalent, it is better
 * to have a dedicated handler that does not go through this logic, as it maybe a bit slow
 * for faster events.
 */
XeroController.prototype.findModeHandler = function(name) {
	if( this[name] ) return name;
	var under = name.indexOf("_");
	if( under>=0 ) return this.findModeHandler(name.substring(under+1));
};

/** Handles mouse wheel events in the image area */
XeroController.prototype.wheelMouse = function (id,e) {
	if(!e) e = window.event;
	var lay = this.model.layout.findLayout(id);
	var area = lay.area; 
	var handler = this.getShiftInfo(e);
	handler = handler+"wheel";
	if( area ) handler = this["getMode"+area](lay,e)+"_"+handler+area; 
	this.debug("Looking for handler for",handler);
	handler = this.findModeHandler(handler);
	this.debug("Found mouse click handler",handler);
	if( handler ) return this[handler](lay,e);
};

/** Gets the shift information */
XeroController.prototype.getShiftInfo = function(e) {
	var ret = "";
	if( e.ctrlKey ) ret = "ctrl_"+ret;
	if( e.shiftKey ) ret = "shift_"+ret;
	if( e.altKey ) ret = "alt_"+ret;
	if( e.metaKey ) ret = "meta_"+ret;
	return ret;
};

/** Handles the key press event - 
 * @TODO see if there is a reasonable way to hook up specific event objects instead of
 * the generalized ones.
 */
XeroController.prototype.keyPress = function(area, e) {
	if(!e) e = window.event;
	this.userEvent("keyPress",e.keyCode);
	var shift = this.getShiftInfo(e);	
	var handler= shift+"key"+e.keyCode;
	if( area ) handler = area+"_"+handler;
	//this.debug("Key press handler initial",handler);
	handler = this.findModeHandler(handler);
	//this.debug("Found handler",handler);
	if(!handler) return;
	return this[handler](e);
};

XeroController.prototype.dblClickTime=350;
XeroController.prototype.dblMoveDistance=2;

/**
 * Handles the double-click event for IE
 * This does an extra mouse down/mouse up to simulate the second
 * click.
 */
XeroController.prototype.doubleClick = function(id,e) {
	console.debug("doubleClick - IE specific behaviour.",id);
	this.mouseClickDrag(id);
	if( this.mousing ) {
		return this.mouseUp(e);
	} else {
		console.info("Not mousing - no dbl click handler found.");
	}
};

/**
 * Handles the mouse-down for an image area.
 *
 * There are 3 mouse button handlers for different types of mouse listening.
 * mouseClickDrag is a mouse-down handler that handles click/drag operations on
 * non-menu areas.  It can distinguish between single and double and click and drag
 * actions. 
 *
 * @see mouseMenu for mouse-down actions for a menu event
 * @see itemAction for actions on a specific button.
 * 
 */
XeroController.prototype.mouseClickDrag = function(id,e) {
	if( this.mousing ) return this.mousing.mouseDown(id,e);
	if( this.hideMenu(id,e) ) return;
	if(!e) e = window.event;
	var lay = this.findLayout(id);
	var area = lay.area; 
	var handler = this.getShiftInfo(e);
	var clickHandler = handler;
	var but = "";
	if( isRightMouse(e) ) but = "Right";
	else if( !isLeftMouse(e) ) but = "Mid";
	var dbl = (this.singleClickMousing && this.singleClickMousing.singleClickTime()!=0);
	console.debug("Button pressed:",(but==""?"left":but),"is double",dbl);
	if( dbl ) {
		handler = handler+"dblDrag"+but;
		clickHandler = clickHandler + "dbl"+but;
		console.debug("Looking for double click handler",clickHandler);
		this.singleClickMousing = null;
	} else {
		handler = handler+"drag"+but;
		clickHandler = clickHandler + "click"+but;
	};
	if( area ) {
		var mode = "";
		if( this["getMode"+area] ) mode = this["getMode"+area](lay,e)+"_";
		handler = mode+handler+area;
		clickHandler = mode+clickHandler+area;
	} 
	handler = this.findModeHandler(handler);
	clickHandler = this.findModeHandler(clickHandler);
	if(!(handler || clickHandler)) {
		console.debug("No click handlers found.");
		return;
	}
	console.debug("Found mouse handler",handler,"click",clickHandler);
	
	var mousing;
	if( handler ) {
		mousing = this[handler];
	} else {
		mousing = new MouseHandler();
	};

	var mouseDom = mousing.getMouseDom(target(e));
	if( !mouseDom ) {
		console.warn("No mouse DOM area to hook mouse events to for",handler);
		return;
	}
	this.debug("Found mouse DOM area",mouseDom.id);
	this.mousing = mousing;
	mousing.clickHandler = clickHandler;
	mousing.lay = lay;
	mousing.controller = this;
	mousing.mouseDom = mouseDom;
	mousing.dblClick = dbl;
	
	if( ! mousing.prepare(e) ) {
		console.warn("Mouse prepare for",handler,"returned false, not mousing.");
		return;
	}
	this.debug("Area prepared - now about to hook listeners to", mouseDom.id);
	
	hookEvent(mouseDom,"mouseout", XC_callMouseOut);
	hookEvent(mouseDom,"mouseup", XC_callMouseUp);
	if(mousing.mousemove) hookEvent(mouseDom, "mousemove", XC_callMouseMove);
	
	this.debug("Hooked events, returning and awaiting the next call at",this.mousing.startTime);
	return cancelEvent(e);
};

/** Call the Xero controller mouse move. */
function XC_callMouseMove(e) {
	return controller.mouseMove(e);
};
/** Call the Xero controller mouse move. */
function XC_callMouseOut(e) {
	return controller.mouseOut(e);
};
/** Call the Xero controller mouse move. */
function XC_callMouseUp(e) {
	return controller.mouseUp(e);
};

/** Handle the mouse movement events */
XeroController.prototype.mouseMove = function(e) {
	if( !e ) e = window.event;
	if( !this.mousing ) {
		unhookEvent(target(e),"mousemove", XC_callMouseMove);
		return;
	};
	// Only start sending events to the child element once the double click
	// time/distance is exceeded so that no updates are done before that.
	// The return value will be true on started once it is ready.
	if( !this.mousing.isDrag(e) ) return;
	if( !this.mousing.started ) {
		this.mousing.started = this.mousing.start();
		if( !this.mousing.started ) return;
	} 
	this.mousing.mousemove(e);
};

/** Handle the mouse up events */
XeroController.prototype.mouseUp = function(e) {
	if( !this.mousing ) return true;
	if( !e ) e = window.event;
	if( this.mousing.isDrag(e) ) {
		console.debug("Drag, not click.");
		this.mousing.end(e);
	} else {
		var clickTime = this.mousing.singleClickTime();
		// Browsers can't usually delay less than 25 ms
		if( clickTime>25 ) {
			this.singleClickMousing = this.mousing;
			console.debug("Waiting for",clickTime,"to determine double click.");
			window.setTimeout(runSingleClickMousing, clickTime);
		} else {
			console.debug("Click, not drag - todo, check if double click.");	
			this.mousing.runClickEvent();
		}
	}
	unhookEvent(this.mousing.mouseDom,"mousemove", XC_callMouseMove);
	unhookEvent(this.mousing.mouseDom,"mouseout", XC_callMouseOut);
	unhookEvent(this.mousing.mouseDom,"mouseup", XC_callMouseUp);
	this.mousing = null;
	return cancelEvent(e);
};

function runSingleClickMousing() {
	if( ! controller.singleClickMousing ) return;
	controller.singleClickMousing.runClickEvent();
	controller.singleClickMousing = undefined;
	controller.waitUpdate("singleClickMousing",2); 
};

/** Handle mouse out events - currently only handles image area events 
 * @TODO fix this to be more general
 */
XeroController.prototype.mouseOut = function(e) {
	if( !this.mousing ) return;
	if( !e ) e = window.event;
	var tg = this.mousing.mouseDom;
	var reltg = (e.relatedTarget) ? e.relatedTarget : e.toElement;
	while (reltg!=null && reltg !== tg && reltg.nodeName != 'BODY') 
		reltg= reltg.parentNode
	if (reltg===tg) return;
	return this.mouseUp(e);
};


/** 
 * Define the base functions for a mouse handler.  Mouse handlers
 * those those that handle on-going mouse move listening.  These are NOT the handlers
 * that handle simple click type events, or mouse wheel events.
 * The expected handling of a mouse event is that 
 * 1. The start event is called (if no start is present, then this isn't a mouse handler)
 *     This can update the screen, or cause fetches to be started, but MUST not re-draw
 *     existing objects.
 */
function MouseHandler() {
};

MouseHandler.prototype.debug = function() {};

MouseHandler.prototype.prepare = function (e) {
	this.startTime = (new Date()).getTime();
	this.startPosn = docCoords(e);
	this.startScreen = [e.screenX,e.screenY];
	this.debug("Start position",this.startPosn,"at",this.startTime);
	this.drag = undefined;
	return this.clickHandler!=null;
};

MouseHandler.prototype.mhPrepare = MouseHandler.prototype.prepare; 

MouseHandler.prototype.start = function MouseHandler_start(event) {
	this.debug("Image mouse down target id %s imageDisplay %s button %d which %d at ", this.target, this.downTarget.id, event.button, event.which, this.startPosn);
	return true;
};

/** Just use the object clicked for the mouse dom area. */
MouseHandler.prototype.getMouseDom = function(src) { return src; };

MouseHandler.prototype.end = function MouseHandler_showMouseMoveEnd(event) {
	var posn = docCoords(event);
	var delta = [posn[0]-this.startPosn[0],posn[1]-this.startPosn[1]];
	this.debug("Show Mouse Move End button %d which %d at %s", event.button, event.which, delta);
};

/** Runs the click event instead of the drag event */
MouseHandler.prototype.runClickEvent = function(e) {
	// Click event may not be defined.
	if( !this.clickHandler ) return;
	this.controller[this.clickHandler](this.lay,e);
	this.clickHandler = undefined;
};

/** Handles another mouse-down event - default is to do nothing */
MouseHandler.prototype.mouseDown = function(lay,e) {};

/** Determines if the object was a drag */
MouseHandler.prototype.isDrag = function(e) {
	if(this.drag!==undefined ) return this.drag;
	var cposn = docCoords(e);
	var delta = Math.abs(cposn[0]-this.startPosn[0])+Math.abs(cposn[1]-this.startPosn[1]);
	console.debug("Delta",delta);
	if( delta>this.controller.dblMoveDistance ) {
		this.drag = true;
		return true;
	};
	return false;
};

/** Determines how much time is left before this is considered a single click */
MouseHandler.prototype.singleClickTime = function() {
	if( this.dblClick ) return 0;
	var ret =  this.controller.dblClickTime - (new Date()).getTime() + this.startTime;
	console.debug("singleClickTime:",ret);
	if( ret<0 ) return 0;
	return ret;
};

/** Handle an image-area mouse event - this defines things that affect the image */
function ImageMouseHandler() {
};
ImageMouseHandler.prototype = new MouseHandler();

/** Get the area to link the mouse listeners to */
ImageMouseHandler.prototype.getMouseDom = function(targ) { return targ; };

/** Called before start is called to start loadings information/background images etc
 * @return true to indicate to handle this click.
 */
ImageMouseHandler.prototype.prepare = function (e) {
	this.mhPrepare(e);
	this.downTarget = this.getTarget(e);
	this.image = null;
	this.started = false;

	this.image = this.getImage();
	if( !this.image ) {
		console.warn("No image found, not mousing.");
		return false;
	}
	if( !this.layout ) {
		console.warn("No layout found, not mousing.");
		return false;
	}	
	return true;
};

/** Have an extra copy of this to allow calling the parent method */ 
ImageMouseHandler.prototype.prepareBase = ImageMouseHandler.prototype.prepare;


/** 
 * Finds the image display parent of the given object.  This is a top-level funciton that is
 * re-used.
 */
ImageMouseHandler.prototype.getMouseDom = function (src) {
	var pl = document.getElementById("PatientLayout");
	if( pl ) {
		console.debug("Using patient layout for mouse dom.");
		return pl;
	}
	var clazz = src.className;
	var prevChild = src;
	if( clazz && clazz.baseVal ) clazz = clazz.baseVal;
	while( src && clazz!="imageDisplay" && src.nodeName!=="BODY" && src.nodeName!=="body") {
		this.debug("findImageTarget", src.id, src.nodeName, clazz);
		prevChild = src;
		src = src.parentNode;
		clazz = src.className;
		if( clazz && clazz.baseVal ) clazz = clazz.baseVal;
		// Safari and Opera don't provide easy access to svg class.
		if( clazz==="image" ) {
			console.debug("Returning previous child - not really a good test.");
			return prevChild;
		}
	} 
	if( clazz==="imageDisplay" ) {
		this.debug("findImageTarget Found", src.id, src.nodeName, clazz);
		return src;
	}
	return undefined;
}; 




/** Finds the (primary) image object */
ImageMouseHandler.prototype.getImage = function() {
	var id = this.downTarget.id;
	var imageLayPos = id.indexOf("ImageLay");
	if( imageLayPos==-1 ) return;
	var layId = this.downTarget.id.substring(imageLayPos);
	this.imageId = "im"+layId;
	var ret = document.getElementById(this.imageId);
	if(!ret) { 
		console.warn("Looked for image id",this.imageId,"but not found, based on",this.downTarget.id);
		return;
	}
	this.layout = this.controller.findLayout(layId);
	return ret;
};

/** Finds the url for the image object */
ImageMouseHandler.prototype.getImageUrl = function MouseHandler_getImageUrl(strip) {
	if(!this.image) this.image = this.getImage();
	var url = this.image.getAttribute("src");
	if( url ) return stripUrl(url,strip);
	url = this.image.getAttributeNS("http://www.w3.org/1999/xlink","href");
	if( url ) return stripUrl(url,strip);
	console.warn("Unable to find URL for %s",this.image.id);
};

/** Sets the url for the image object */
ImageMouseHandler.prototype.setImageUrl = function MouseHandler_setImageUrl(url) {
	var origUrl = this.image.getAttribute("src");
	if( origUrl ) {
		this.image.setAttribute("src", url);
		return;
	}
	this.image.setAttributeNS("http://www.w3.org/1999/xlink","href",url);
};

ImageMouseHandler.prototype.getTarget = function(e)
{
    var targ = target(e);
    if(targ.nodeName == "shape")
    {
        // In IE images are wrapped in shape elements.
        var imagedata = targ.getElementsByTagName("imagedata");
        if(imagedata.length == 1)
        {
            targ = imagedata[0];
        }
    }
    
    return targ;
};

/** Calls the 3 base resets */
XeroController.prototype.allReset = function(lay,e) {
	this.userEvent("allReset");
	this.wlReset(lay,e,true);
	this.zoomReset(lay,e,true);
	this.spatReset(lay,e,true);
	this.waitUpdate("mouseUp",2);
};

/** Defines the individual dynamic controllers */
$dynamic/windowLevel()$
$dynamic/spat()$
$dynamic/zoom()$
$dynamic/menu()$