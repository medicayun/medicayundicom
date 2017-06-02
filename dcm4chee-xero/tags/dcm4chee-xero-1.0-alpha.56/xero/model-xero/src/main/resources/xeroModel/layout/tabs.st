/**
 * Define the model information for a Layout that implements a tab.
 */
function TabsLayout(id, i18n) {
	var style = i18n ? i18n.TabStyle : "html/tabs";
	this.baseConstructor(id,style);
};
TabsLayout.prototype = new Layout("TabLayoutPrototype","html/tabs");
TabsLayout.prototype.baseConstructor = Layout;

/** The height of the header section */
TabsLayout.prototype.headerHeight = 22;
TabsLayout.TabMenu="tabMenu";

/** Adds the given tab/Layout  to the display */
TabsLayout.prototype.addTab = function TV_addTab(name,layout) {
	if(layout==null) {
		layout = new Layout();
		layout.tabInactive = true;
	}
	layout.tabName = name;
	if( this.activeTab ) {
		layout.used = false;
	} else {
		this.activeTab = layout;
		layout.used = true;
	}
	this.add(layout);
};

/** Removes a given tab */
TabsLayout.prototype.removeTab = function TV_removeTab(name) {
	var tab,i,n=this.layouts.length;
	var newArr = new Array();
	for(i=0; i<n; i++) {
		tab = this.layouts[i];
		if( tab.tabName!==name ) {
			newArr.push(tab);
		}
	}
	this.layouts = newArr;
};

/** Finds a tab */
TabsLayout.prototype.findTab = function TV_findTab(name) {
	var tab,i,n=this.layouts.length;
	for(i=0; i<n; i++) {
		tab = this.layouts[i];
		if( tab.tabName===name ) {
			return tab
		}
	}
};

/** Determines if we are using the headerless tab style */
TabsLayout.prototype.hasNoHeader = function() {
	return this.template == "html/noHeaderTabs";
};

/** Handle the resize operation for tabs */
TabsLayout.resize = function TL_resize(width, height, style) {
	if( this.hasNoHeader() ) {
		this.activeTab.resize(width,height,style);
	} else {
		var ht = height-this.headerHeight;
		this.style = "height:"+(ht/height)+"%;";
		this.activeTab.resize(width,ht,this.style);
	};
};

/** Selects the given tab.  Return the new tab if a change is made. */
TabsLayout.prototype.selectTab = function TV_selectTab(name) {
	var tab = this.findTab(name);
	if( !tab ) throw new Error("Tab "+name+" not found.");
	if( this.activeTab===tab ) return false;
	if( tab.tabInactive ) return false;
	if( this.activeTab ) {
		this.activeTab.hide();
		if( this.activeTab.usedListener ) this.activeTab.usedListener();
	}
	this.activeTab = tab;
	tab.used = true;
	if( tab.usedListener ) tab.usedListener();
	return this.activeTab;
};

/* This function can be used to activate/de-activate an array of objects */
function TabsLayoutArrayUsedListener() {
	var i,n=this.length;
	var wasUsed;
	if( this.used ) {
		for(i=0; i<n; i++) {
			if( this[i].used!=this.wasUsed ) {
				this[i].used = this[i].wasUsed;
				if( this[i].usedListener ) this[i].usedListener();
			}
		}
	} else {
		for(i=0; i<n; i++) {
			this[i].wasUsed = this[i].used;
			if( this[i].used ) {
				this[i].used = false;
				if( this[i].usedListener ) this[i].usedListener();
			}
		}
	}
};