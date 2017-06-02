/**
 * Handles a layout object.  A layout object is a templatable object that specifies what template to display it with
 * plus information on what/how to display it.
 */
function Layout(id, template) {
	if( !id ) id = "unspecified";
	this.idBase = id;
	this.id = id;
	if( template ) this.template = template;
	if( this.isDebug ) this.debug = console.info;
};

Layout.prototype.idCount = new Object();
Layout.prototype.isDebug = false;
Layout.EAST = "east";
Layout.WEST = "west";
Layout.NORTH = "north";
Layout.SOUTH = "south";
Layout.prototype.widthEdge = 0;
Layout.prototype.heightEdge = 0;
Layout.prototype.extraStyle = "";
Layout.prototype.baseConstructor = Layout;

Layout.prototype.debug = function() {};
Layout.prototype.template = "html/layout";
// Some things don't quite get setup right without an initial size, even though it is wrong.
Layout.prototype.setGrid = function Layout_setGrid(x,y) {
	this.gridX = x;
	this.gridY = y;
	if(! this.layouts) this.layouts = new Array();
};

function LayoutIdMap() {
	this.counter = new Object();
};

Layout.prototype.computeId = function Layout_computeId(idMap) {
	if(!idMap ) {
		idMap = new LayoutIdMap();
	}
	this.idMap = idMap;
	if( !idMap[this.id] ) {
		idMap[this.id] = this;
	} else if( idMap[this.id]===this ) {
	  // No-op
	} else {
		var cnt = idMap.counter[this.idBase];
		if(! cnt ) {
			idMap.counter[this.idBase]=1;
			cnt = 1;
		}
		this.id = this.idBase+"-"+cnt;
		idMap.counter[this.idBase] = 1+cnt;
		idMap[this.id] = this;
	}
	if( !this.children ) return;
	var i, n=this.children.length;
	for(i=0; i<n; i++) {
		this.children[i].computeId(idMap);
	};
};

/** Hides this object the hierarchy - sets the used to false, and calls any store methods */
Layout.prototype.hide = function(isChild) {
	if( !this.used ) return;
	if( !isChild ) {
		this.used = false;
	}
	if( this.hideListener ) {
		this.hideListener();
	}

	if( !this.children ) return;
	var i,n=this.children.length;
	for(i=0; i<n; i++) {
		this.children[i].hide(true);
	}
};

/** Shows the given object - does not recursively show parents, so parents must
 * also be visible for this to become visible - also tags it for relayout
 */
Layout.prototype.show = function() {
 	if( this.used ) return;
 	if( this.parent ) this.parent.relayout = true;
 	this.used = true;
 };

Layout.prototype.used = true;

/** Resizes the edge components */
Layout.prototype.resizeEdge = function Layout_resizeExtra() {
	if( !this.children ) return;
	var edge = this.north;
	var style;
	if( edge ) {
		style = "width: 100%; height: "+edge.absHeight+"px;";
		edge.resize(this.width, edge.absHeight,style);
	}
	edge = this.west;
	if( edge ) {
		style = "width: "+(edge.absWidth*100/this.width)+"%; height: "+((this.height-this.deltaHeight)/this.height)+"%;";
		edge.resize(this.width, edge.absHeight,style);
	}
	edge = this.east;
	if( edge ) {
		style = "width: "+(edge.absWidth*100/this.width-0.1)+"%; height: "+((this.height-this.deltaHeight)/this.height-0.1)+"%;";
		edge.resize(this.width, edge.absHeight,style);
	}
	edge = this.south;
	if( edge ) {
		style = "width: 100%; height: "+(edge.absHeight*100/this.height-0.1)+"%;";
		edge.resize(this.width, edge.absHeight,style);
	}
};

/**
 * Resizes the objects to display in the correct area
 */
Layout.prototype.resize = function Layout_resize(width,height,style) {
	this.debug("resize",this.id,"to",width,height,"style",style);
	var changed = this.resizeListener && ((this.width!=Math.floor(width)) || (this.height!=Math.floor(height)));
	this.width = Math.floor(width);
	this.height = Math.floor(height);
	
	this.style = style+this.extraStyle;
	if( changed ) {
		this.resizeListener();
	}
	this.resizeEdge();
	if( ! this.gridX ) return;
	if( ! this.gridY ) return;
	
	var usedLay = this.getUsedLayouts();
	var absWidth = this.sumAbsWidth(usedLay);
	var absHeight = this.sumAbsHeight(usedLay);
	var sumRelWidth = this.sumRelWidth(usedLay);
	var sumRelHeight = this.sumRelHeight(usedLay);
	this.debug("sum rel x,y=%f,%f on %s,%d,%d", sumRelWidth, sumRelHeight, this.template,this.gridX, this.gridY);
	width = width-absWidth-this.widthEdge;
	height = height-absHeight-this.heightEdge;
	
	var i,j;
	var subWidth, subHeight;
	var layout;
	for(i=0; i<this.gridX; i++) {
		for(j=0; j<this.gridY; j++) {
			if( i+j*this.gridX>=usedLay.length ) break;
			layout = usedLay[i+j*this.gridX];
			subWidth = layout.absWidth;
			if( subWidth==null ) {
				subWidth = layout.relWidth;
				if( !subWidth ) subWidth = 100;
				subWidth = subWidth*width/sumRelWidth;
				if( sumRelWidth===0 ) {
					console.warn("Setting sub-width to infinity!");
				}
			}
			// Always do width relative - it messes up the display otherwise.
			if( this.idMap.IS_IE ) {
				style = "width:"+Math.floor(subWidth*997/this.width)/10+"%;";
			} else {
				style = "width:"+Math.floor(subWidth*1000/this.width)/10+"%;";
			}
			
			subHeight = layout.absHeight;
			if( subHeight==null ) {
				this.debug("Didn't find subHeight - computing it from height %d and sumRelHeight %d",height, sumRelHeight);
				subHeight = layout.relHeight;
				if( !subHeight ) subHeight = 100;
				subHeight = subHeight * height/sumRelHeight;
				if( this.idMap.IS_IE ) {
					style = style+"height:"+Math.floor(subHeight*998/this.height)/10+"%;";
				} else {
					style = style+"height:"+Math.floor(subHeight*1000/this.height)/10+"%;";
				}
			} else {
				style = style+"height:"+subHeight+"px;";
			}
			layout.resize(subWidth, subHeight, style);
		};
	};
};

/** Checks to see if the layout information is complete */
Layout.prototype.isComplete = function Layout_isComplete() {
	if( !this.children ) return true;
	var lay,i,n=this.children.length;
	for(i=0;i<n;i++){
		lay = this.children[i];
		if( !lay.used ) continue;
		if( !lay.isComplete() ) return false;
	};
	return true;
};

/** Clears an relayout flags that have been set for this object and all child objects */
Layout.prototype.clearRelayout = function Layout_clearRelayout() {
	this.relayout = false;
	if(! this.children ) return;
	var i,n=this.children.length;
	for(i=0; i<n; i++) {
		this.children[i].clearRelayout();
	}
};

/** Return only layouts that are being used - allows them to disappear or otherwise
 * not be shown.
 */
Layout.prototype.getUsedLayouts = function Layout_getUsedLayouts() {
	var i,n=this.layouts.length;
	var ret = new Array();
	for(i=0; i<n; i++) {
		if( this.layouts[i].used ) ret.push(this.layouts[i]);
	}
	return ret;
};

/** Finds the given layout object.  Can be passed a layout object directly or the id of one. */
Layout.prototype.findLayout = function findLayout(id) {
	if( typeof(id)!=="string" ) return id;
	return this.idMap[id];
}; 

/** Sum all of the widths of the absolute width items */
Layout.prototype.sumAbsWidth = function Layout_sumAbsWidth(usedLay) {
	var ret = 0;
	var layout,i,n=Math.min(this.gridX,usedLay.length);
	for(i=0; i<n; i++) {
		layout = usedLay[i];
		if( layout.absWidth ) ret = ret+layout.absWidth;
	}
	return ret;
};
Layout.prototype.sumAbsHeight = function Layout_sumAbsHeight(usedLay) {
	var ret = 0;
	var layout,y;
	for(var i=0; i<usedLay.length; i+=this.gridX) {
		layout = usedLay[i];
		if( layout.absHeight ) ret = ret+layout.absHeight;
	}
	return ret;
};

/** Sum the relative width information, using 100 for any missing item values */
Layout.prototype.sumRelWidth = function Layout_sumRelWidth(usedLay) {
	var ret = 0;
	var layout,i,n=Math.min(this.gridX,usedLay.length);
	for(i=0; i<n; i++) {
		layout = usedLay[i];
		if( layout.relWidth ) {
			ret = ret+layout.relWidth;
		} else if( layout.absWidth==null ) {
			ret = ret+100;
		}
	};
	return ret;
};
Layout.prototype.sumRelHeight = function Layout_sumRelHeight(usedLay) {
	var ret = 0;
	var layout,i;
	for(i=0; i<usedLay.length; i+=this.gridX) {
		layout = usedLay[i];
		if( layout.relHeight ) {
			ret = ret+layout.relHeight;
		} else if( layout.absHeight==null ) {
			ret = ret+100;
		}
	};
	return ret;
};

/** Add a named child - assumes already set in layouts. */
Layout.prototype.addNamed = function Layout_addNamed(child,name) {
	if( name===Layout.EAST || name===Layout.WEST) {
		this.widthEdge = this.widthEdge+child.absWidth;
	} else if( name===Layout.NORTH || name===Layout.SOUTH ) {
		this.heightEdge = this.heightEdge+child.absHeight;
	}
};

/** Add another child object */
Layout.prototype.add = function Layout_add(child,name) {
	if( !child ) throw "Child being added to "+this.id+" is undefined.";
	if( name ) {
		this[name] = child;
		this.addNamed(child,name);
	} else {
		if( !this.layouts ) this.layouts =new Array();
		this.layouts.push(child);
	}
	if( this.children==null ) this.children = new Array();
	this.children.push(child);
	child.parent = this;
	if( this.idMap ) child.computeId(this.idMap);
};

/** Initialize/create the child elements - no op in default class.
 * @return false if unable to initialize yet, and true if initialized successfully. 
 */
Layout.prototype.init = function Layout_init() {
	return true;
};


/** Some exemplar layout types */
function LabelLayout(id, i18n) {
	this.baseConstructor(id);
	this.label = i18n;
	if( i18n && i18n!="" ) {
		this.i18n = i18n;
		this.icon = i18n + "Icon";
	}
};

LabelLayout.prototype = new Layout("Label", "html/toolItem");
LabelLayout.prototype.i18n = null;

/** Finds the given item, offset by dir, in the lst, or in the children elements. */
Layout.prototype.findRotate = function (i18n, dir, lst) {
	if( dir==null ) dir = 0;
	if(!lst ) lst = this.children;
	var i,n=lst.length;
	for(i=0; i<n; i++) {
		if( lst[i].i18n==i18n ) {
			i = (i+n + dir) % n;
			return lst[i];
		}		
	};
	return null;
};

/** Copies relevant attributes from another layout - should be over-ridden if
 * more than just the i18n, icon, label are to be copied.
 */
Layout.prototype.copyFrom = function(lay) {
	this.i18n = lay.i18n;
	this.label = lay.label;
	this.icon = lay.icon;
};

/** A spacer layout */
function SpacerLayout(width,height) {
	this.baseConstructor("spacer");
	this.absWidth = width;
	this.absHeight = height;
};

SpacerLayout.prototype = new Layout("SpacerLayout");