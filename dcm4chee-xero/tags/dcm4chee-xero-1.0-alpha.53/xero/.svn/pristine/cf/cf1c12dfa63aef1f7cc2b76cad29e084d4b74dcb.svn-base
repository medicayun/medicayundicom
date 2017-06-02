/**
 * Contains the patient/study/series/image information that is merged between the presentation and external data.
 */

/** Creates a new merged overall object.
 */
function Merged(parent) {
	if( parent ) {
		this.level = parent.level+1;
		this.parent = parent;
	} else {
		this.level = 0;
		this.external = new Object();
	};
};

/** This gets the external object associated with this object */
Merged.prototype.getExternal = function Merged_getExternal() {
	return this.external;
};

/** 
 * Looks up the given child value if available, or creates it if it isn't available.
 */
Merged.prototype.getChild = function Merged_getChild(id) {
	if(!this.children ) this.children = new Object();
	var child = this.children[id];
	if(! child ) {
		child = new Merged(this);
		this.children[id] = child;
		child.id = id;
	}
	return child;
};



/**
 * Looks up the requested presentation value.  Looks first in the presentation layers at this level and higher, and if not
 * found there, then it looks in the external data.
 */
Merged.prototype.get = function Merged_get(key) {
	var val, parent = this;
	do {
		val = parent[key];
		if( val!==undefined ) return val;
		parent = parent.parent;
	} while(parent && parent.level > 2);
	
	parent = this;
	do {
		if( !parent.external ) {
			console.error("Parent has no external object at level %d on id %s", this.level,this.id);
		} else {
			val = parent.external[key];
			if( val!==undefined ) return val;
		}
		parent = parent.parent;
	} while(parent && parent.level > 2);  
};

