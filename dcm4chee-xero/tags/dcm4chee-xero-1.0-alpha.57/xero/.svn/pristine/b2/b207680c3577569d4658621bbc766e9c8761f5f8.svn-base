/**
 * The StudyData object is an extension of JSONModel that
 * adds support for merging data at various levels, and creating a "children" index that
 * indexes the available children by the appropriate key.
 * The default constructor uses the uids to query for the study level information, and requests that gsps and key object defaults
 * are included.
 */

function StudyData(uids,ae) {
	if( uids ) this.queryForStudies(uids,null,ae);
};

StudyData.prototype = new JSONModel();
StudyData.prototype.level = 0;
StudyData.prototype.idVars = {"patient":"PatientIdentifier", "study":"studyUID", "series":"seriesUID", "image":"Position", "gsps":"objectUID", "report":"objectUID", "keyObject":"objectUID", "objectRef":"objectUID"};

/** For for additional studies - whichever ones are missing */
StudyData.prototype.queryForStudies = function(uids, studies, ae) {
	var i,n=uids.length;
	var uidsBack;
	for(i=0; i<n; i++) {
		if( studies && studies[uids[i]] ) continue;
		if( uidsBack ) uidsBack = uidsBack+"\\\\";
		else uidsBack = "";
		uidsBack = uidsBack + uids[i];
	}
	if(!uidsBack ) return;
	this.url = STUDY_QUERY+"&gsps=*&ko=*&studyUID="+uidsBack;
	if( ae ) this.url = this.url + "&ae="+ae;
};

/** Finds a given object instance child by UID */
StudyData.prototype.findInstance = function(uid) {
	if( this.level>4 ) return;
	if( this.level==4 ) {
		if( this.objectUID===uid ) return this;
	} else if( this.level<4 ) {
		var i;
		for(i in this.children) {
			var child = this.children[i];
			var ret = child.findInstance(uid);
			if( ret ) return ret;
		}
	}
};

/** Gets a StudyData child, re-using the existing one of the right id if present, and indexing them
 * in a "children" element.
 */
StudyData.prototype.getChild = function StudyData_getChild(name,el,modifiers,i) {
	var lst = this[name];
	var isFirst = false;
	var modifier;
	// We need the modifier for the child level, not this level
	if( modifiers ) modifier = modifiers[this.level+1];
	if(! lst ) {
	  lst = new Array();
	  this.children = new Object();
	  this[name] = lst;
	  isFirst = true;
	}
	
	var childId = this.idVars[name];
	// Detect a non-DICOM image object child, and just use id for those.
	if( name=="image" && el["objectUID"]==null ) this.childId = "id";
	if( !childId ) childId = "id";
	this.debug("Choose childID %s for level %d on child %s", childId,this.level,name); 

	var id = el[childId];
	if( id==null ) id = ""+i;
	if( modifier ) id = id+modifier;
	this.debug("Found child %s=%s modifier %s modifiers %s tagName %s", childId,id, modifier, modifiers,el["tagName"]);
	var xchild = this.children[id];
	if( !xchild ) {
		xchild = new StudyData();
		this.children[id] = xchild;
		xchild.level = this.level+1;
		xchild.xmlFirst = isFirst;
		lst.push(xchild);
	}
	return xchild;
};

/**
 * Makes the requested queries for additional information to merge.  
 */
StudyData.prototype.addQueries = function StudyData_addQueries(service, queries, lazy) {
	var key;
	for(key in queries) {
		this.debug("Adding information from %s",key);
		this.requestUrl(service, key, queries[key],lazy);
	}
};

/** Adds a customization for the use node to indicate the type of node it is - this gets around the
 * templating problem of not being able to distinguish different classes of objects.
 */
StudyData.prototype.customize_use = function StudyData_customize_use() {
    var clazz = this["class"];
    if( clazz==null || clazz=="Overlay" ) this.isIMAGEREL = true;
    else this.isDISPLAYREL = true;
};

/** Adds a customization for the path - sets the filled value appropriately. */
StudyData.prototype.customize_path = function StudyData_customize_path() {
	this.filled = 't';
	if( this.fill=="none" ) this.filled='f';
};

/** Adds a customization for the path - sets the filled value appropriately. */
StudyData.prototype.customize_text = function StudyData_customize_path() {
	this.fontsize = parseInt(this["font-size"]);
	this.iefontsize = this.fontsize*0.65;
	this.xc = parseInt(this.x);
	this.yc = parseInt(this.y)+this.fontsize/2;
	this.xc2 = this.xc + parseInt(this.textLength);	
	this.yc2 = this.yc+1;
	if( this.transform ) {
		var rot = this.transform.indexOf("rotate(");
		if( rot<0 ) return;
		rot = this.transform.substring(rot+7,this.transform.indexOf(",",rot+7));
		rot = parseInt(rot);
		if( rot==0 ) return;
		if( rot==90 ) {
			this.xc = this.xc + this.fontsize/2;
			this.yc = parseInt(this.y);
			this.xc2 = this.xc+1;
			this.yc2 = this.yc + parseInt(this.textLength);
		}
		else if( rot==180 ) {
			this.xc2 = this.xc - parseInt(this.textLength);
		}
		else if( rot==270 ) {
			this.xc = this.xc + this.fontsize/2;
			this.yc = parseInt(this.y);
			this.xc2 = this.xc-1;
			this.yc2 = this.yc - parseInt(this.textLength);
		}
	}
};

/**
 * Adds a customization for the svg node, registering all the g children at the study level in "svg" for easy
 * access from use nodes.  The id used includes the href # sign prefix so it is a complete href equivalent.
 */
StudyData.prototype.customize_svg = function StudyData_customize_svg() {
	var study = this._parent._parent._parent;
	if(! study.svg ) study.svg = new Object();
	var svg = study.svg;
	var key;
	for(key in this.children) {
		svg["#"+key] = this.children[key];
	};
};


/** Adds a customization for key objects, to store their UID's consistently */
StudyData.prototype.customize_keyObject = function() {
	if( this.p ) {
		this.keyText = new Array();
		for(var i=0; this.p[i]; i++) this.keyText.push(this.p[i].TEXT);
	}
	
	var allKo = this._parent._parent.allKo;
	if(! allKo ) {
		allKo = new Object();
		this._parent._parent.allKo=allKo;
	};
	var val = allKo[this.objectUID];
	if( val===this ) return;
	allKo[this.objectUID] = this;
	for(var i in val) {
		console.info("Adding key object to key objects list of image.");
		val[i].keyObjects.push(this);
	}
};

/** Adds a customizations for images that have key objects matching them up to their object */
StudyData.prototype.customize_image = function() {
	if(!this.koUID) return;
	if(this.keyObjects) return;
	this.keyObjects = new Array();
	var allKo = this._parent._parent.allKo;
	if(!allKo) {
		allKo = new Object();
		this._parent._parent.allKo = allKo;
	}
	var cur, uid, koUID = this.koUID;
	do {
		cur = koUID.indexOf(",");
		if( cur>0 ) {
			uid = koUID.substring(0,cur);
			koUID = koUID.substring(cur+1);
		} else {
			uid = koUID;
			koUID = null;
		}
		console.info("Adding key object",uid,"to image",this.objectUID);
		var v = allKo[uid];
		if( v==null ) {
			v = new Array();
			allKo[uid] = v;
		};
		if( v.length>=0 ) {
			console.info("image customize - adding image child",this);
			v.push(this);
		} else {
			console.info("image customize - adding key object child directly",v);
			this.keyObjects.push(v);
		}
		last = cur;
	} while(koUID);
};

/** Add text for no study description and flags for nearline/offline */
StudyData.prototype.customize_study = function() {
	if( !this.StudyDescription ) this.StudyDescription="No Study Description";
	if( this.InstanceAvailability==="NEARLINE") this.isNearline = true;
	else if( this.InstanceAvailability==="OFFLINE") this.isNearline = true;
	else this.isOnline = true;
};

/** Computes the length of the series description  */
StudyData.prototype.customize_series = function() {
	if(! this.SeriesDescription ) this.SeriesDescription="";
	this.SeriesDescLength = Math.min(5+this.SeriesDescription.length,35);
};

StudyData.prototype.customize_patient = function() {
	this.manyStudy = (this.study!=null && this.study.length>2);
};