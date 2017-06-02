/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Bill Wallace, Agfa HealthCare Inc., 
 * Portions created by the Initial Developer are Copyright (C) 2007
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Bill Wallace <bill.wallace@agfa.com>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */
/** Miscellaneous event handling helper code bits */

/** Returns the local name of the node */
function localName(node) {
	if( node.localName!==undefined && node.localName!==null ) return node.localName;
	if( node.baseName!==undefined ) return node.baseName;
	var tn = node.tagName;
	var i = tn.indexOf(":");
	if( i>=0 ) tn = ""+tn.substring(i+1);
	return tn;
};

/** Get the target of the event */
function target(e) {
	var targ;
	if( !e ) var e = window.event;
	if ( e.target ) targ = e.target;
	else if (e.srcElement) targ = e.srcElement;
	if( targ.nodeType == 3 ) // defeat Safari bug
	  targ = targ.parentNode;
	return targ;
}

/** Tells if this is a left mouse button.  Works in IE, Firefox, Konq, Opera, Safari.
 * @param evt to detect the mouse button on.
 * @return true if left mouse button.
 */
function isLeftMouse(evt) {
	if( evt.which ) return (evt.which<2);
	return evt.button < 2;
};

/** Tells if this is a right mouse button.  Works in IE, Firefox, Konq, Opera, Safari.
 * @param evt to detect the mouse button on.
 * @return true if left mouse button.
 */
function isRightMouse(evt) {
	if( evt.which ) return (evt.which==3);
	return event.button == 2;
};

/** Telss if any mouse button is down. */
function isMouseDown(e) {
	if( e.which ) return true;
	return e.button!=0;
};

/**
 * Finds the coordinates for a given event, multi-browser supported.
 * Returns array with the DOCUMENT based coordinates
 * @param (Event) e is the event to use.  Will use window.event for IE
 * @return [x,y] document based coordinates.
 * @author tarquinwj (public code in posted as demo)
 */
function docCoords(e) {
   if( !e ) { e = window.event; } if( !e || ( typeof( e.pageX ) != 'number' && typeof( e.clientX ) != 'number' ) ) { return [ 0, 0 ]; }
   if( typeof( e.pageX ) == 'number' ) { return [e.pageX,e.pageY];} else {
      var posX = e.clientX; var posY = e.clientY;
      if( !( ( window.navigator.userAgent.indexOf( 'Opera' ) + 1 ) || ( window.ScriptEngine && ScriptEngine().indexOf( 'InScript' ) + 1 ) || window.navigator.vendor == 'KDE' ) ) {
         if( document.documentElement && ( document.documentElement.scrollTop || document.documentElement.scrollLeft ) ) {
            posX += document.documentElement.scrollLeft; posY += document.documentElement.scrollTop;
         } else if( document.body && ( document.body.scrollTop || document.body.scrollLeft ) ) {
            posX += document.body.scrollLeft; posY += document.body.scrollTop;
         }
      }
      return [ posX, posY ];
   }
};


/**
 * Gets the top, left coordinates of the given element in document space.
 * An optional offset that will be added to the result can be supplied.
 * For example, to determine the document position that is 10 pixels to
 * the right and 5 pixels down from the top, left corner of the element e, invoke
 * this function as such:
 *      getElementDocCoords(e, [10, 5]); 
 */
function getElementDocCoords(elem, offset)
{
    var coords = new Array(2);
    
    coords[0] = offset ? offset[0] : 0;
    coords[1] = offset ? offset[1] : 0;
    
    var e = elem;
    while(e)
    {
        coords[0] += e.offsetLeft;
        coords[1] += e.offsetTop;
        
        e = e.offsetParent;
    }
    
    e = elem.parentNode;
    while(e && e != document.body)
    {
        if(e.scrollLeft)
        {
            coords[0] -= e.scrollLeft;
        }
        
        if(e.scrollTop)
        {
            coords[1] -= e.scrollTop;
        }
        
        e = e.parentNode;
    }
    
    return coords;
};

/** Strips the provided elements from the URL and returns the new URL */
function stripUrl(url, rem) {
	if( !url ) {
		alert("No URL found.");
		return undefined;
	}
	if(!rem) return url;
	var i,j;
    var search;
	if( (typeof rem)==='string') {
        search = '&'+rem+'=';
		i = url.indexOf(search);
		if( i<0 ) return url;
		j = url.indexOf('&',i+2);
		if( j<0 ) j = url.length;
		url = url.substring(0,i) + url.substring(j,url.length);
		return url;
	}
	else if( rem ) {
	   for(i=0; i<rem.length; i++) {
	   	  url = stripUrl(url,rem[i]);
	   }
	}
	return url;
};

/** Provides information about the frame width.  Defaults to 1024, 768 if nothing else found.*/
function getViewportSize() {
    var size = [1024, 768];
    if (typeof window.innerWidth != 'undefined')
    {
    	size = [ window.innerWidth, window.innerHeight ];
    }
    else if (typeof document.documentElement != 'undefined' &&
             typeof document.documentElement.clientWidth != 'undefined' &&
             document.documentElement.clientWidth != 0)
    {
     size = [ document.documentElement.clientWidth, document.documentElement.clientHeight ];
    }
    else
    {
     var body = document.getElementsByTagName('body')[0];
     size = [ body.clientWidth, body.clientHeight ];
    }

    return size;
};

/** Given an element, hookup the given callback (must be a function) to listen to that event */
function hookEvent(element, eventName, callback)
{
  if(typeof(element) == "string")
    element = document.getElementById(element);
  if(element == null)
    return;
  if(element.addEventListener)
  {
    if(eventName == 'mousewheel')
    {
      // This is idempotent so it doesn't matter if the same element is hooked up multiple times.
      element.addEventListener('DOMMouseScroll',
        callback, false); 
    } else {
    	element.addEventListener(eventName, callback, false);
    }
  }
  else if(element.attachEvent)
    element.attachEvent("on" + eventName, callback);
}

/** Unhook the given callback from the event. */
function unhookEvent(element, eventName, callback)
{
  if(typeof(element) == "string")
    element = document.getElementById(element);
  if(element == null)
    return;
  if(element.removeEventListener)
  {
    if(eventName == 'mousewheel')
    {
      element.removeEventListener('DOMMouseScroll',
        callback, false); 
    }
    element.removeEventListener(eventName, callback, false);
  }
  else if(element.detachEvent)
    element.detachEvent("on" + eventName, callback);
};

/** Cancels an events default behaviour */
function cancelEvent(e)
{
  e = e ? e : window.event;
  if( !e ) return false;
  if(e.stopPropagation)
    e.stopPropagation();
  if(e.preventDefault)
    e.preventDefault();
  e.cancelBubble = true;
  e.cancel = true;
  e.returnValue = false;
  return false;
};

/** Adds the given function to the onload event - this can handle things like auto-querying
 * or other things that need to happen when the window is loaded.
 */
function addLoadEvent(func) {
  var oldonload = window.onload;
  if (typeof window.onload != 'function') {
    window.onload = func;
  } else {
    window.onload = function() {
      if (oldonload) {
        oldonload();
      }
      func();
    }
  }
};


