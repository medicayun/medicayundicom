/**
 * Contains the general controller for Xero.
 * This is the non-interactive controller for Xero - it handles the actions such as series
 * navigation that do not result in continuous, interactive updates to images etc.
 * There is an interactive controller as well that handles things like window levelling,
 * zoom/pan, and faster navigation while the user has the button down, or until some timeout
 * has completed and the user isn't navigating any longer.  At that point, the interactive
 * controller switches to this controller to complete the overall update.
 *
 * There are some common parts to the controller in a vertical slice:
 * 	Setup - configures the initial model for display
 *  Submit - stores information to the server
 * 	Restore Data - takes information from the server and sets up a local setting
 *  Restore View - takes stored data and sets up the model again for displaying that page/data
 *	Read Input - takes information from a form on the display page and sets up a model to
 *		correspond with it
 *
 * Horizontally, there are several components as well:
 *	Query Page - including search results, query form
 *	Display Page - including series tray, report view, series view, image view etc
 */

/** Creates a XeroController object, typically called controller */
function XeroController(model, ua, view) {
	this.model = model;
	this.view = view;

    this.renderListeners = new Array();
    	
	// TODO:  Move into a method on the model?
	if(_SARISSA_IS_IE) {
		this.model.IS_IE6 = (ua.indexOf("MSIE 6")>=0);
		this.model.HAS_SVG = false;
		this.model.HAS_VML = true;
	} else {
		this.model.HAS_SVG = true;
		this.model.HAS_VML = false;
	}
	this.model.IS_FIREFOX = _SARISSA_IS_MOZ;
	this.model.IS_MOBILE = (ua.indexOf("Mobile")>=0 || ua.indexOf("BlackBerry")>=0);
	
	if( this.isDebug ) this.debug = console.debug;
};

// The set of event listeners - define early so as to be available to push items into.
XeroController.prototype.userEventListeners = new Array();

/** Indicates that some user event has occurred */
XeroController.prototype.userEvent = function(eventName, eventDescription) {
	if( eventDescription==null ) eventDescription="";
	var i,n=this.userEventListeners.length;
	for(i=0; i<n; i++) {
		this.userEventListeners[i](this,eventName,eventDescription);
	}
};

/**
 * Converts the given style size (i.e. width or height) value to a pixel value.  For
 * a percent value, a pixel length must be specified.  An optional scale value can be
 * passed to scale the given value.
 */
XeroController.convertToBase = function(val,length, scale) {
	if( val=="" ) return 0;
	var last = val.charAt(val.length-1);
	if( last=='%' ) {
		return Math.floor(parseInt(val.substring(0,val.length-1))*length*scale/100);
	};
	if( last=="x" ) return parseInt(val.substring(0,val.length-2))*scale;
	// TODO - compute the 3.X from pixel spacing/sizes instead of a raw value.
	//  
	if( last=="m" ) 
	{
	    console.warn("Unable to accurately convert metric sizes to pixels.");
	    return parseInt(val.substring(0,val.length-2))*3.75*scale
	}
	console.warn("Unknown length type left,top coordinate:",val,length,"last character", last);
};

/**
 * Gets an adjusted zoom rectangle that is at least partially visible on screen.
 * 
 * For images that are zoomed in, the method ensures that the top and left image 
 * edges are not positioned greater than the image port's center.  Likewise, the 
 * bottom and right image edges will not be positioned less than the image port's 
 * center.
 * 
 * For images that are zoomed out, the method ensures that the image mid-point is 
 * never outside the original (zoom-to-fit) image rectangle.  The computation to 
 * ensure that the mid-point is never panned outside the image port was considered 
 * to be more complex and expensive than actually worth (i.e. just need to ensure 
 * that the image is partially in view).
 */
XeroController.getOnScreenZoomRectangle = function(zoomRect, rows, cols)
{
    var onScreenZoomRect = new ZoomRectangle(zoomRect.topLeft, zoomRect.bottomRight);
    
	var halfCols = Math.floor(cols/2);
	
	var halfRectWidth = Math.floor(onScreenZoomRect.getWidth() / 2);
	if(onScreenZoomRect.topLeft[0] + halfRectWidth < 0)
	{
	    if(onScreenZoomRect.bottomRight[0] < halfCols)
	    {
	        var deltaX = -halfRectWidth - onScreenZoomRect.topLeft[0];
	        if(onScreenZoomRect.bottomRight[0] + deltaX < halfCols)
	        {
	            // zoomed-in; image left edge panned to greater than the image port mid-point 
	            onScreenZoomRect.topLeft[0] = -halfRectWidth;
	            onScreenZoomRect.bottomRight[0] += deltaX;
	        }
	        else
	        {
	            // zoomed-out; image mid-point panned past the right edge of the view area
	            deltaX = halfCols - onScreenZoomRect.bottomRight[0];
	            onScreenZoomRect.bottomRight[0] = halfCols;
	            onScreenZoomRect.topLeft[0] += deltaX;
	        }
	    }
	} 
	else if(onScreenZoomRect.topLeft[0] + halfRectWidth > cols)
	{
	    if(onScreenZoomRect.topLeft[0] > halfCols)
	    {
       	    var deltaX = cols - 1 - halfRectWidth - onScreenZoomRect.topLeft[0];
    	    if(onScreenZoomRect.topLeft[0] + deltaX > halfCols)
    	    {
	            // zoomed-in; image right edge panned to less than the image port mid-point 
    	        onScreenZoomRect.topLeft[0] = cols - 1 - halfRectWidth;
    	        onScreenZoomRect.bottomRight[0] += deltaX;
    	    }
    	    else
    	    {
	            // zoomed-out; image mid-point panned past the left edge of the view area
    	        deltaX = halfCols - onScreenZoomRect.topLeft[0];
    	        onScreenZoomRect.topLeft[0] = halfCols;
    	        onScreenZoomRect.bottomRight[0] += deltaX;
    	    }
	    }
	};
	
	var halfRows = Math.floor(rows/2);
	
	var halfRectHeight = Math.floor(onScreenZoomRect.getHeight() / 2);
	if(onScreenZoomRect.topLeft[1] + halfRectHeight < 0)
	{
	    if(onScreenZoomRect.bottomRight[1] < halfRows)
	    {
	        var deltaY = -halfRectHeight - onScreenZoomRect.topLeft[1];
	        if(onScreenZoomRect.bottomRight[1] + deltaY < halfRows)
	        {
	            // zoomed-in; image top edge panned to greater than the image port mid-point 
	            onScreenZoomRect.topLeft[1] = -halfRectHeight;
	            onScreenZoomRect.bottomRight[1] += deltaY;
	        }
	        else
	        {
	            // zoomed-out; image mid-point panned below the bottom edge of the view area
	            deltaY = halfRows - onScreenZoomRect.bottomRight[1];
	            onScreenZoomRect.bottomRight[1] = halfRows;
	            onScreenZoomRect.topLeft[1] += deltaY;
	        }
	    }
	} 
	else if(onScreenZoomRect.topLeft[1] + halfRectHeight > rows)
	{
	    if(onScreenZoomRect.topLeft[1] > halfRows)
	    {
       	    var deltaY = rows - 1 - halfRectHeight - onScreenZoomRect.topLeft[1];
    	    if(onScreenZoomRect.topLeft[1] + deltaY > halfRows)
    	    {
	            // zoomed-in; image bottom edge panned to less than the image port mid-point 
    	        onScreenZoomRect.topLeft[1] = rows - 1 - halfRectHeight;
    	        onScreenZoomRect.bottomRight[1] += deltaY;
    	    }
    	    else
    	    {
	            // zoomed-out; image mid-point panned above the top edge of the view area
    	        var deltaY = halfRows - onScreenZoomRect.topLeft[1];
    	        onScreenZoomRect.topLeft[1] = halfRows;
    	        onScreenZoomRect.bottomRight[1] += deltaY;
    	    }
	    }
	};
	
	return onScreenZoomRect;    
};    

$xeroControllers:{ $(it)()$ }$

XeroController.prototype.isDebug = false;
XeroController.prototype.debug = function() {};

/** Finds the layout specified by the given id, optionally looks for the parent layout of the
 * given patient through image level (1->4)
 */
XeroController.prototype.findLayout = function(id,lvl) {
	if( typeof(id)==="object" ) return id;
	
	if(!this.model.layout)
	{
	    return null;
	}
	
	var lay = this.model.layout.findLayout(id);
	if( !lvl ) return lay;
	while(lay) {
		if( lay && lay.navigate && lay.navigate.level===lvl ) return lay;
		lay = lay.parent;
	}
};

/** Updates the innerHtml of the element provided (el) with the provided HTML to update the
 * display area in IE.
 */
XeroController.prototype.innerHtml = function XC_innerHtml(el, html) {
	var oldEl = typeof el === "string" ? document.getElementById(el) : el;
	oldEl.outerHTML = html;
	return oldEl;
};

/** Non-IE browsers don't have a mouse wheel event to hook up to, so
 * use the DOM events to hookup the mouse events. */
XeroController.prototype.fixWheelMouse = function XC_fixWheelMouse(lay) {
	if( this.model.IS_IE ) return;
	
	if( lay.mousewheel ) {
	   hookEvent(lay.id, "mousewheel", lay.mousewheel);
	};
};


/** Gets the scroll node, or null if not found */
XeroController.prototype.getScrollNode = function(id) {
   var node;
   if( this.model.IS_FIREFOX ) {
      node = document.getElementById(id+"Body");
   } 
   if(!node ) {
      node = document.getElementById(id);
   }
   return node;
};

/**
 * Restore the scroll position - has special handling for Firefox to restore
 * the tbody scroll position instead of the top-level scroll position.
 */
XeroController.prototype.fixScrollPosition = function(id) {
   var lay = this.findLayout(id);
   if( lay.scrollTop==undefined ) return;
   var node = this.getScrollNode(lay.id);
   console.info("fixScrollPosition",id,"on",node,"to",lay.scrollTop);
   if( node ) node.scrollTop = lay.scrollTop;
};

/** Store the scroll position, so that it can be restored later */
XeroController.prototype.storeScrollPosition = function(id) {
   var lay = this.findLayout(id);
   var node = this.getScrollNode(lay.id);
   if( !node ) return;
   lay.scrollTop = node.scrollTop;
};

/** Replaces the node specified by the node id, with the given replacement text.
 * If isDiffUpdate is true, then this should be updated by a difference comparison
 * rather than a replacement.
 */
XeroController.prototype.replaceHtml = function XC_replaceHtml(node,text,isDiffUpdate) {
   var ret;
   console.info("Replacing html on node",node);
   if( this.model.IS_IE ) {
      if( isDiffUpdate ) {
   	     ret = DOMUtils.updateHtml(node,text);
      } else {
         ret = this.innerHtml(node,text);
       }
   }
   else
   {
      this.debug("fixMouseWheel");
      var ret = DOMUtils.updateHtml(node, text);
   }
   this.fireViewRendered(this.findLayout(node));
   return ret;
};

/** Fires the view rendered event recursively on this and child elements */
XeroController.prototype.fireViewRendered = function(lay) {
	if( !lay || !lay.used ) return;
	// Fire event
	var i,n;
	if( lay.viewRenderedListeners ) {
		n = lay.viewRenderedListeners.length;
		for(i=0;i<n; i++) {
			this[lay.viewRenderedListeners[i]](lay);
		};
	};
	
	// Recurse over the children
	if( !lay.children ) return;
	n=lay.children.length;
	for(i=0;i<n;i++) {
		this.fireViewRendered(lay.children[i]);
	};
};

/** Updates the view in its entirety.
 */
XeroController.prototype.updateView = function XC_action(actionName) {
    if( !this.view ) return;
    this.fireRenderEvent("renderStarted");
	console.time(actionName);
	var render = this.view.toString("xeroBody",this.model);
	if( render==null || render==="" ) {
		alert("Couldn't render view.");
		return;
	}
	window._render = render;
	
	//console.info("Render: %s",render);
	this.replaceHtml(this.model.layout.id, render);
	this.model.layout.clearRelayout();
	console.timeEnd(actionName);
    this.fireRenderEvent("renderCompleted");
};

/** Performs a delayed update */
XeroController.prototype.waitUpdate = function XC_waitUpdate(actionName, cnt, notFirst) {
    if( !notFirst ) this.fireRenderEvent("renderStarted");
	if( cnt===undefined ) cnt = 7;
	var missing = this.renderComplete([this.model.layout]);
	if( missing===0 ) {
        this.fireRenderEvent("renderCompleted");
	    return 0;
	}
	if(cnt===0 || missing<0) {
		this.debug("Rendering with cnt=%d with %d missing", cnt, missing);
		this.updateLayouts(actionName);
        this.fireRenderEvent("renderCompleted");
		return;
	}
	this.debug("Waiting till everything is complete - cnt=%d", cnt);
	var usethis = this;
	window.setTimeout( function() {
		usethis.waitUpdate(actionName,cnt-1,true);
	}, 200);
};

/** Renders all components that have relayout set and are complete.  Returns a count of required items. */
XeroController.prototype.renderComplete = function XC_renderComplete(layouts,incomplete) {
	if(! layouts ) return 0;
	var render, origLay,lay,i,n=layouts.length;
	var ret = 0;
	for(i=0; i<n; i++) {
		lay = layouts[i];
		if( !lay.used ) continue;
		if( lay.relayout ) {
			if( incomplete || lay.isComplete() ) {
				this.debug("Re-rendering %s template %s",lay.id, lay.template);
				origLay = this.model.layout;
				this.model.layout = lay;
				console.time("rerender");
				render = this.view.toString(lay.template,this.model);
				console.timeEnd("rerender");
				this.model.layout = origLay;
				var result = this.replaceHtml(lay.id,render, lay.isDiffUpdate);
				lay.clearRelayout();
				if( result===false ) {
					return -100000;
				} 
			}
			else {
				ret++;
			} 
		}
		else {
			ret += this.renderComplete(lay.children,incomplete);
		}
		if( ret<0 ) return ret;
	}
	return ret;
};


/** Updates the layout components individually, only as required. */
XeroController.prototype.updateLayouts = function XC_updateLayouts(actionName,layouts) {
	if( layouts===undefined ) {
		console.time(actionName);
		if(! this.model.layout.layouts ) {
			console.warn("Layout is null - can't update layout.");
			return;
		}
		this.updateLayouts(actionName,this.model.layout.layouts);
		console.timeEnd(actionName);
		return;
	}

	// Rendering everything, even stuff not yet complete.
	this.renderComplete(this.model.layout.layouts,true);	 
};

/** Switch to the given tab - switching to the Display tab
 * has the behaviour of:
 * 1. If no query results are available, perform a search and display all 
 *    results for the first patient.
 * 2. If current query results are available, then display the first patient,
 *    that has a selected study, or the first patient in the list.
 * 3. If query results are out of date, re-query and display all results for the
 *    first patient. 
 * The re-query display of the selected/ordered patient depends on selection
 * and ordering being preserved durign a performSearch re-query.
 */
XeroController.prototype.switchTab = function XC_switchTab(src,tabName) {
	console.info("Switch tab to", tabName);
	if( !this.model.layout.selectTab(tabName) ) return;
	if( tabName=="Display" ) {
		var urlFactory = this.findLayout("QueryLayout").query;
		var origUrl = urlFactory.url;
		var newUrl = urlFactory.updateUrlFromForm();
		var searchLayout = this.findLayout("SearchLayout");
		if( origUrl!==newUrl || !searchLayout.search) {
			console.info("The search isn't up to date - requerying.");
			this.performSearch();
		}
		if( searchLayout.search.displayedPatient ) {
			this.updateView("switchTab");
			return;
		}
		var pid = this.selectPatientToDisplay(searchLayout.search);
		if( pid==null ) {
			this.model.layout.selectTab("Find");
			this.updateView("switchTab");
			return;
		}
		this.displayPatient(src,pid);				
	} else {
		this.updateView("switchTab");
	}
};

/** When switching tabs, selects a patient that has selected studies, OR
 * selects the first patient.
 */
XeroController.prototype.selectPatientToDisplay = function(results) {
	if( !(results && results.patient && results.patient.length) ) return;
	var n = results.patient.length;
	for(var i=0; i<n; i++) {
		var patient = results.patient[i];
		if( !patient.study ) continue;
		var m = patient.study.length;
		for(var j=0; j<m; j++) {
			var study = patient.study[j];
			if( study.isSelected ) {
				return patient.PatientIdentifier;
			}
		}
	};
	return results.patient[0].PatientIdentifier;
};

/** Switch a button */
XeroController.prototype.pushButton = function XC_pushButton(layId) {
	var lay = this.findLayout(layId);
	console.info("Pushed button", lay.id);
	if(!lay ) {
		console.warn("No layout named",layId,"found.");
		return;
	};
	lay.isActive = ! lay.isActive;
	lay.relayout = true;
	this.waitUpdate("pushButton");
};

/** Rotates to the given child item */
XeroController.prototype.rotateButton = function XC_rotateButton(layId,arg) {
	var lay = this.findLayout(layId);
	if(!lay ) {
		console.warn("No layout named",layId,"found.");
		return;
	};
	var par, chd;
	console.info("Rotating button",lay.id,"arg",arg,"in",lay.i18n);
	if( lay.children ) {
		par = lay;
		chd = lay.findRotate(arg,0);
	} else {
		par = lay.parent;
		chd = lay;
	};
	var lst = par.findRotate(par.i18n);
	if( lst ) lst.isActive = false;
	chd.isActive = true;
	par.copyFrom(chd);
	// Choose the next child type
	var rot = par.findRotate(par.i18n,1);
	console.info("Found rotate",rot.i18n);
	par.arg = rot.i18n;
	par.relayout = true;
	this.waitUpdate("rotateButton");
};

/**
 * Adds the given renderListener to the list of listeners.  A renderListener
 * is an object with two zero-argument methods:  renderStarted; renderCompleted.
 * @param renderListener the render listener
 */
XeroController.prototype.addRenderListener = function(renderListener)
{
  this.renderListeners.push(renderListener);
};

/**
 * Removes the given renderListener to the list of listeners.
 * @param renderListener the render listener
 */
XeroController.prototype.removeRenderListener = function(renderListener)
{
  for(i = this.renderListeners.length - 1; i >= 0; i -= 1)
  {
      if(this.renderListeners[i] === renderListener)
      {
          this.renderListeners.splice(i,1);
      }
  }
};

/**
 * Fires the render event with the given name.  The name is the method
 * of the render listener object to be invoked.
 * @param eventName the render listener event name; either "renderStarted" or
 *                  "renderCompleted"
 */
XeroController.prototype.fireRenderEvent = function(eventName)
{
  for(i = 0; i < this.renderListeners.length; i += 1)
  {
      this.renderListeners[i][eventName]();
  }    
}

/** Sets the cursor to the busy cursor */
function waitCursor() {
	var tab = document.getElementById("XeroRootTab");
	if( !tab ) return; 
	tab.className="wait";
	if( document.body ) document.body.style.cursor = 'wait';
}

/** Sets the cursor to the default cursor */
function defaultCursor() {
	var tab = document.getElementById("XeroRootTab");
	if( !tab ) return; 
	tab.className="";
	if( document.body ) document.body.style.cursor = 'default';
}

/** Gets the error condition associated with the given request */
XeroController.prototype.getError = function(e) {
	this.lastError = e;
$if(IS_SERVER)$
	return "PACS Server Unavailable";
$else$
	if( e.status===undefined ) return "Xero Server Unavailable";
	if( typeof(e)==="string" ) return e; 
	if( e.status===502 ) return "PACS Server Unavailable.";
	if( e.status===500 ) return "Internal Xero Error - please report this error to your support desk.";
	if( e.status===200 ) return "You have been logged out - please select the logout button and login again.";
	if( e.name==="SyntaxError" ) return "Problem parsing Xero server response.";
	return "Unknown server error:"+e.status;
$endif$
}