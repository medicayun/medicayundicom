/** 
 * The Navigation object defines the desired view/navigation positions relative to some named navigation information.
 * Then, the Navigation object is provided External information and uses that information to determine which
 * object is to be used/displayed in that navigation spot, and provides event notifications when a different External object
 * (or no object) applies to a given Navigation.
 *
 * Given the external information, it is possible to figure out if there is conversational or merged information to apply - conversational
 * information is local to the given window and does not inherit any values from the merged/external information, where as the 
 * merged information is a separate class that merges external and local changes to be applied to various objects.  Which merged object to use
 * can be computed from the external information.
 * 
 */
 function Navigation(level, offset) {
	// Listeners includes both explicit listeners, in implicitly, children of this object.
	this.children = new Object();
	if( level && level>0 ) {
		this.level = level;
		this.offset = offset;
		this.levelName = this.names[level];
		this.listeners = new Array();
	} else {
		this.merged = new Merged();
		this.conversation = new NavConversation();
		this.external = new StudyData();
		// The id is an indicator that the parent is valid and has a valid location.
		this.id = "results";
	}
	this.childType = this.childTypes[this.level];
	this.childIdVar = this.idVars[this.level];
};

/** A key is used to identify a particular element in the set, typically a UID.
 * Offsets from this follow the natural list order.
 */
Navigation.prototype.KEY="nav";

Navigation.prototype.isDebug = false;
if( Navigation.prototype.isDebug ) Navigation.prototype.debug = console.debug;
else Navigation.prototype.debug = function() {};

Navigation.prototype.names = ["result", "patient", "study", "series", "image"];

Navigation.RESULT = 0;
Navigation.PATIENT = 1;
Navigation.STUDY = 2;
Navigation.SERIES = 3;
Navigation.IMAGE = 4;
Navigation.OBJECT = 4;

Navigation.prototype.level =0;
Navigation.prototype.idVars = ["PatientIdentifier", "studyUID", "seriesUID", "Position"];

Navigation.prototype.childTypes = ["patient", "study", "series", "image"];

Navigation.prototype.IMAGE_QUERY_SIZE=64;
Navigation.prototype.QUERY_SIZE=16;

// Event types.
Navigation.prototype.NAVIGATION_CHANGED = 100;
Navigation.prototype.INVALID = 110;
Navigation.prototype.EXTERNAL_CHANGED = 120;
Navigation.prototype.EXTERNAL_UPDATED = "external";


Navigation.prototype.error = function(msg) {
	console.error(msg);
	throw new Error(msg);
};

/** 
 * Generates the URL that loads the external data for the given child Id (which maybe undefined to mean all/unknown child.)
 * Puts the new value into the return object if it isn't already
 * present, and returns that object.
 * TODO - strictly speaking, this is controller logic, NOT model logic so at some point there should be a setter
 * method that indicates how these queries are to be generated.  However, until such a time as that is actually needed
 * this method can be left here
 */
Navigation.prototype.generateQuery = function Navigation_generateQuery(child, ret) {
	// So we don't have any external data for the child object, but we do have it's id
	if(!ret) ret = new Object();
	var url, modifiers;
	
	var childId = child.id;
	modifiers = this.conversation.assignDefaultModifiers(this.external, childId);
	this.debug("Modifiers ",modifiers);
	if( this.level===3 ) {
		this.debug("Adding image level query at %d of size %d initial offset %d", childId, this.IMAGE_QUERY_SIZE, this.QUERY_SIZE);
		position = childId - this.QUERY_SIZE;
		position -= (position % this.IMAGE_QUERY_SIZE);
		position += this.QUERY_SIZE;
		url = IMAGE_QUERY+"&regroup=*&studyUID="+this.parent.getBaseId()+"&seriesUID="+this.getBaseId()+"&Count="+this.IMAGE_QUERY_SIZE+"&Position="+position;
		this.debug("Adding series extension query",url,"modifiers",modifiers); 
	} else if( this.level===2 ) {
		url = IMAGE_QUERY+"&regroup=*&studyUID="+this.getBaseId()+"&Position=0&Count="+this.QUERY_SIZE;
		this.debug("Adding image query to study uid %s because child with id %s and external children %s", this.id, childId, this.external.children);
	} else if( this.level===1 && modifiers!=null && modifiers[2]!=undefined) {
		// Fix the modifiers, and do an image level query.
		url = IMAGE_QUERY+"&regroup=*&studyUID="+child.getBaseId()+"&Position=0&Count="+this.QUERY_SIZE;
		this.debug("Adding updated image, with study query to %s because child with id %s and external children %s", this.id, childId, this.external.children);
	} else if( this.level===1 ) {
		// This happens when the user views a new study in the same patient, or an updated study in 
		// the same patient.
		url = STUDY_QUERY+"&gsps=*&ko=*&studyUID="+childId;
		this.debug("Adding new study query %s", url);
	} else {
		console.error("Unknown id/query level %d for child id %s", this.level, childId);
		return ret;
	}
	if( url ) ret[url] = modifiers;
	return ret;
};

/** Gets the base ID that identifies this object level - without copy identifiers etc */
Navigation.prototype.getBaseId = function() {
   return this.conversation.baseId;
};

/** Gets the external id - this is the ID to query against at this level */
Navigation.prototype.getExternalId = function() {
	this.createConversation();
	return this.conversation.getExternalId();
};

/** 
 * Adds external data for the the study.
 * The external data is owned by this object, but the act of updating/reading the data is handled by the controller.
 * This function returns undefined to indicate all required external data is available, or it returns a set of URLs as the keys in an object.  (The values are
 * used to store information about what has been retrieved and how to merge it into the StudyData object).
 */  
Navigation.prototype.mergeExternal = function Navigation_mergeExternal(external,ret) {
	if( external==null && this.level==0 ) external = this.external;
	if( this.external!==external ) {
		this.external = external;
		if( this.merged ) {
			if( this.level>0 ) {
				this.merged = null;
				this.createMerged();
			} else {
				this.merged.external = external;
			}
		}
		this.fireChanged(this.EXTERNAL_CHANGED);
	} else {
		this.fire(this.EXTERNAL_UPDATED,external,external);
	}
	this.debug("Adding external data to %s at level %d", this.id,this.level);
	var child,childKey;
	var childExt,url,position,extId;
	for(childKey in this.children) {
		child = this.children[childKey];
		if(child.id==null) {
			child.invalidate(false);
		}
		// Null means no object there, whereas undefined means unknown, so if it is null, just continue.
		if( child.id===null ) continue;
		if( child.id!==undefined && external.children) {
			extId = child.getExternalId();
			this.debug("Looking for external id %s on base id %s",extId,child.id);
			childExt = external.children[extId];
			if( childExt ) {
				ret = child.mergeExternal(childExt,ret);
				continue;
			}
		}
		ret = this.generateQuery(child,ret);
		// No point continuing to look at series children as a single query is used to get ALL series children information at once.		
		if( this.level==2 ) break;
	}
	return ret;
}; 

/** Sets a given named navigation key object value. */
Navigation.prototype.setNavigation = function Navigation_setNavigation(value,key) {
	if(! key ) key = this.KEY;
	this.createConversation();
	if( this.conversation[key] == value ) {
		this.debug("setNavigation key is unchanged - continuing with doing anything.");
		return;
	}
	this.debug("setNavigation on %s is %s=%s", this.id, key, value);
	this.conversation[key] = value;

	this.fireChanged(this.NAVIGATION_CHANGED);
	this.debug("Finished fire changed - about to invalidate.");
	this.invalidate(true);
	this.debug("Finished invalidate.");
};

/** Invalidates the given key element, if there are any children depending on it. */
Navigation.prototype.invalidate = function Navigation_invalidate(isTop) {
	if( !isTop ) {
		this.debug("Invalidating child object - resetting ids at level",this.level);
		var oldId = this.id;
		// Compute the new id if we can
		this.id = this.lookupId();
		this.debug("Looked up id = %s", this.id);
		if( this.id === oldId ) {
			return;
		}
		this.debug("Changed data for old id",oldId,"is now",this.id);
		this.conversation = undefined;
		var msg = this.INVALID;
		this.external = undefined;
		this.merged = undefined;
		if( this.id!=null ) {
			this.createMerged();
			if( this.external ) msg = this.EXTERNAL_CHANGED;
		}	
		this.fireChanged(msg);
	};
	
	var childKey, child;
	for(childKey in this.children ) {
		child = this.children[childKey];
		child.invalidate(false);
	}
};

/** Notifies listeners of changes to the navigation information, specified by the given type. Also fires the named listener changes
 * for the new conversational/merged data.
 */
Navigation.prototype.fireChanged = function Merge_fireChanged(type) {
	if( this.level===0 ) return;
	if( this.conversation && this.conversation.navigateListener ) 
		this.conversation.navigateListener(this,type);
	var v, l, i,n=this.listeners.length;
	for(i=0; i<n;i++) {
		this.listeners[i].navigateListener(this,type);
	}
	// It isn't clear whether to fire on external is null/empty - for now, don't fire
	if(! this.external ) return;
	if( this.namedListeners ) {
		for(i in this.namedListeners) {
			l = this.namedListeners[i];
			v = this.conversation[i];
			if(!v ) v = this.merged[i];
			var j,jn = l.length;
			for(j=0;j<jn;j++) l[j].namedListener(this,i,v);
		}
	}	
};

/** 
 * Looks up the id of this object, within the context of the parent object..
 * 
 * The ID is calculated from several values:
 *   1. The set of available, external entities - either those explicitly selected, or 
 *      those from the external data.  For example, a set of study UID's to display, 
 *      as defined in the conversation OR the external data.
 *   2. Either an offset defined in a key, or a specific value to show, 
 *      combined with a fixed offset.
 * The value is returned.
 * The offset can either be provided, or determined from this element.
 */
Navigation.prototype.lookupId = function Navigation_lookupId(offset) {
	if( !(this.parent && this.parent.id) ) return null;
	this.parent.createConversation();

	var key = this.KEY;
	if( offset==undefined ) offset = this.offset;
	var val = this.parent.conversation[key];
	var type = typeof(val);
	var external = this.parent.external;
	
	if(val==undefined ) {
		this.debug("Failed to find any navigation information for key %s at level %d id %s", key, this.level, this.id);
		if( this.level===4 ) type = "number";
		else type="string";
	}
	
	if( type==="number" ) {
		this.debug("Looking up offset %s from number %s", offset, val);
		if(! external ) return undefined;
		if(!val) val = 0;
		if( val+offset<0 ) return null;
		if(! external.Viewable ) return undefined;
		if( external.Viewable > val+offset ) return val+offset;
		return null;
	}
	else if( type==="string" ) {
		this.debug("Looking up relative position to %s offset %s external %s", val, offset, external);
		// Key type - one case is to avoid any searching at all, although it doesn't
		// allow you to find out much extra information in the child.
		if( offset===0 && val ) {
			this.debug("Returning 0 offset information directly.");
			return val;
		}
		var extChildren = this.parent.getExtChildren();
		if( !extChildren ) {
			this.debug("Found no children defined for %s under the name %s at level %d offset %s", this.id,this.childType, this.level, offset);
			return;
		}
		if( val ) {
			var posn = this.parent.findExternal(val);
			if( posn!==undefined ) {
				offset = offset + posn;
			} else {
				console.warn("Didn't find offset %d for %s at position %s",offset,key,posn);
				return undefined;
			}
		}
		if( extChildren.length > offset && offset>=0 ) {
			return this.getIdFrom(extChildren[offset]);
		}
		// Return null because there ARE children, but the offset is outside the bounds
		// so that means none available as opposed to unknown.
		return null;
	}
	else {
		this.error("Unknown key "+key+" type "+type);
	}
};

/** Gets the id from the given object for something at this level
 * If obj is a string or a number, then returns it directly, otherwise
 * uses the child id var to get the id.
 */
Navigation.prototype.getIdFrom = function(obj) {
	if( typeof(obj)==="object" ) return obj[this.parent.childIdVar];
	return obj;
};

/** Gets the external children list to be used.
 * Normally, this is just the simple children objects, but
 * the getCustomChildren call is also used to over-ride the external children
 * defaults.
 */
Navigation.prototype.getExtChildren = function Navigation_getExtChildren(key) {
    if( !key ) key = this.childType;
    if( !(this.external || this.conversation) ) return null;
    
	var ret = this.getCustomChildren(key);
	if( ret!=null || this.external==null) return ret;
	ret = this.external[key];
	return ret;
};

/**
 * This method is used to compute a custom list of children elements for the
 * given key - currently, that means the value defined in the conversation context,
 * or, for series children, the series elements containing at least one image.
 * Only returns a value if there is a conversation
 * context available (which can happen even without an external context).
 */
Navigation.prototype.getCustomChildren = function(key) {
	if( this.conversation==null ) return;
	var ret = this.conversation[key];
	if( ret!=null ) return ret;
	if( key!=="series" || !this.external) return;
	
	 
	// For the study level, the getExtChildren returns only series image children in the default list.
	// To explicitly use another type of list, use a different key type such as a key set.
	
	// The available series doesn't change unless the entire object is refreshed, once the
	// first set of series has been loaded.
	ret = new Array();
	var series = this.external.series;
	if( !series ) return; 
	var ser, i,n=series.length;
	for(i=0; i<n; i++) {
		ser = series[i];
		if( !ser.image ) continue;
		ret.push(ser);
	}
	this.conversation.series = ret;
	return ret;
};

/** Finds the position in the external data having the given id value.  Uses the
 * specified set of child values, if available
 */
Navigation.prototype.findExternal = function Navigation_findExternal(val,key) {
	var extChildren = this.getExtChildren(key);
	var v,i,n=extChildren.length;
	for(i=0; i<n; i++) {
		v = extChildren[i];
		if( v == val || v[this.childIdVar]==val ) return i;
	}
}

/** Creates the conversation context for this object - does not setup the merged context, as there might be changes to the conversation context
 * before the merged context is retrieved.
 */
Navigation.prototype.createConversation = function Navigation_createConversation() {
	if( this.conversation ) return;
	if( this.id==null ) this.error("This navigate object at level "+this.level+" doesn't yet have an ID - set the parent navigation/information first");
	this.parent.createConversation();
	this.conversation = this.parent.conversation.getChild(this.id);
};

/** Creates the merged and external contexts as appropriate.  The external context will be 
 * replaced at various levels by a real external context.
 */
Navigation.prototype.createMerged = function Navigation_createMerged() {
	if( this.merged ) return;
	this.createConversation();
	this.parent.createMerged();
	var mid = this.conversation.getMergedId();
	if( mid==null ) this.error("Unable to get merged id for "+this.id);
	this.merged = this.parent.merged.getChild(mid);
	this.createExternal();
	this.merged.external = this.external;
};

/** Creates/sets up the external object, if it can be found, returning true if available.  id, merged and conversation must be setup already.
 */
Navigation.prototype.createExternal = function Navigation_createExternal() {
	if( this.external ) return true;
	if( !this.parent.createExternal() ) return false;
	if( !this.parent.external.children ) this.parent.external.children = new Object();
	var eid = this.getExternalId();
	this.external = this.parent.external.children[eid];
	return this.external!=undefined;
};

/** Gets the merged context for this object, creating one if necessary.  Returns undefined
 * if the id isn't set, and null if there isn't yet a merged object.
 */
Navigation.prototype.getMerged = function Navigation_getMerged() {
	if(this.id==null) return undefined;
	if(this.external==null ) return null;
	this.createConversation();
	return this.merged;
}

/** Assigns results information to the various levels.
 */

/** Gets the given navigate child element.  The object will be disconnected from this navigate object
 * if on the next apply, no child elements are found reflecting that child information.
 */
Navigation.prototype.getChild = function Navigation_getChild(offset,key) {
	if(!key ) key = this.KEY;

	var ckey = key+":"+offset;
	var child =this.children[ckey];
	if( ! child ) {
		child = new Navigation(this.level+1, offset);
		if( child.KEY!=key ) child.KEY = key;
		child.parent = this;
		this.children[ckey] = child;
		// This will try to find information for this child element, at a minimum the id.
		child.invalidate(false);
	}
	return child;
};

/**
 * Indicate that a given object is a listener for this level of information - when a new assignment
 * is made, the values in the child object will be changed.  Multiple objects can listen for the same
 * position/offset.  The given function is assigned ot obj as navigateListener and called.  A given
 * object can't have 2 or more functions without some other means of combining them.
 * @return the child object that is being listened to.
 * @deprecated - try to use hook instead for all types of listeners
 */
Navigation.prototype.addChildListener = function Navigation_addChildListener(obj, listenerF, offset, key) {
	return this.getChild(offset,key).addListener(obj,listenerF);
};

Navigation.prototype.addListener = function Navigation_addListener(obj,listenerF) {
	obj.navigateListener = listenerF;
	this.listeners.push(obj);
	obj.navigateListener(this);
	return this;
};

/** Hook listeners up to this object.  The top level object should be used to hook generally,
 * and then if specific nested items need hooking, they can be added separately.
 * navigate strings in the nested hierarchy will be replaced with the navigate object they
 * refer to.
 */
Navigation.prototype.hook = function Navigation_hook(obj, dot) {
	if( !dot ) dot = this;
	//console.info("Hook",dot.level,dot.id);
	if( obj.navigate ) {
		//console.info("Obj has navigate - changing dot level.");		
		if( typeof( obj.navigate )==="string" ) {
			var oldDot = dot;
			dot = this.getPath(obj.navigate,dot);
			if(!dot.hook ) {
				throw "Unknown navigate path "+dot[1]+" at level "+oldDot.level;
			}
		} else {
			dot = obj.navigate;
		}
	}
	//console.info("Adding any child hook events.");
	var olst = obj.events;
	var i,n;
	if( typeof(olst)==="string" ) {
		this.hookEvent(obj,olst,dot);
	} else if( olst!=null ) {
		n=olst.length;
		for(i=0; i<n; i++) {
			this.hookEvent(obj,olst[i],dot);
		}
	}
	//console.info("Recursing over children.");
	if(! obj.children ) return;
	n = obj.children.length;
	for(i=0; i<n; i++) {
		this.hook(obj.children[i],dot);
	}
};

/** Hooks the specific item if relevant */
Navigation.prototype.hookEvent = function Navigation_hookEvent(obj,evt,dot) {
	var path = this.getPath(evt,dot);
	//console.info("hookEvent",path);
	if( path.hookEvent ) {
	   //console.info("Hooking a navigate event.");
	   // This is a navigate event - not an array
	   path.listeners.push(obj);
	   return;
	}
	var typ = path[1];
	var arg = path[2];
	var dot = path[0];

	//console.info("Hooking a named listener",typ,arg,dot);
	if( typ=="conversation" || typ=="merged") {
		if( dot.namedListeners==null ) dot.namedListeners = new Object();
		if(! dot.namedListeners[arg] ) dot.namedListeners[arg] = new Array();
		dot.namedListeners[arg].push(obj);
	} else {
		throw "Unknown event to hook in Navigation: "+evt+" type="+typ;
	}
};

/** Gets the path specified by evt - returning the navigation object if the
 * path ends that way, or a quadruple [path, type, arg for type, rest of path]
 */
Navigation.prototype.getPath = function Navigation_getPath(evt,dot) {
	if( evt==="" || evt==="." ) {
		return dot;
	}
	
	var cln = evt.indexOf(":");
	var slash = evt.indexOf("/");
	if( slash==0 ) {
		evt = evt.substring(1);
		cln--;
		slash = evt.indexOf("/");
		dot = this;
	}
	if( slash<0 ) slash = evt.length;
	if( cln<0 || cln>slash ) cln = slash;
	var typ = evt.substring(0,cln);
	var arg = evt.substring(cln+1,slash);
	if( cln===slash ) arg = null; 
	var rest = "";
	if( slash < evt.length ) rest = evt.substring(slash+1);
	if( typ=="." ) {
		return this.getPath(rest,dot);
	} else if( typ=="..") {
	  return this.getPath(rest, dot.parent);
	} else if( typ==dot.childType ) {
		var comma=arg.indexOf(",");
		var key;
		if( comma>0 ) {
			key = arg.substring(0,comma);
			arg = arg.substring(comma+1);
		}
		var offset = parseInt(arg);
		return this.getPath(rest, dot.getChild(offset,key));
	} else {
		return [dot,typ,arg,rest];
	}
};

/** Sets a conversational level item - typically things like navigational data or mode/settings */
Navigation.prototype.setConversation = function(key,value,internalChange) {
	this.createConversation();
	var oldValue = this.conversation[key]; 
	if( oldValue===value && !internalChange ) {
		return;
	}
	this.conversation[key] = value;
	
	this.fire(key,value,oldValue);
};

/** Fire any named listeners */
Navigation.prototype.fire = function(key,value,oldValue) {
	if(! this.namedListeners ) return;
	var l = this.namedListeners[key];
	if(!l ) return;
	var i,n=l.length
	for(i=0; i<n;i++) {
	   l[i].namedListener(this,key,value,oldValue);
	};
};

/** Sets a merged level item - typically things like window level, flip/rotate etc */
Navigation.prototype.setMerged = function(key,value,internalChange) {
	this.createMerged();
	var oldValue = this.merged[key];
	this.debug("setMerged",key,"old",oldValue,"new value",value); 
	if( oldValue===value && !internalChange ) return;
	this.merged[key] = value;
	this.fire(key,value,oldValue);
};

/**
 * Remove ALL listeners - used when the listeners are being destroyed/cleared and recreated.
 * Only removes listeners from this level and below.  This also removed all child Navigation objects.
 * Any previously created/used navigate objects are no longer valid. 
 */
Navigation.prototype.removeAllListeners = function Navigation_removeAllListeners() {
	if( this.level>0 ) {
		var i,n=this.listeners.length;
		for(i=0; i<n; i++) {
			this.listeners[i].navigateListener(undefined);
		}
		this.listeners = new Array();
		this.namedListeners = null;
	}
	var childKey, child;
	for(childKey in this.children ) {
		child = this.children[childKey];
		child.removeAllListeners();
	}
	this.children = new Object();
}; 