/**
 * A URL Factory takes form data and a base URL and constructs a URL to query
 * It constructs the URL from the given form.  If the form does not exist currently,
 * it uses previously remembered values to construct the object.  It is assumed that the
 * form can be reconstructed from local values contained in the UrlFactory.
 */
function UrlFactory(base, formId) {
	this.base = base;
	this.formId = formId;
	this.url = base;
};

// TODO: Consider moving this to a CriteriaHandler class...

UrlFactory.prototype.debug = function() {};


/** Update the internal URL with the indicated queryStr */
UrlFactory.prototype.updateQueryStr = function(queryStr) {	
	this.url = this.base;
	if(!queryStr)
		return;

	if( this.url.indexOf("?")<0 ) 
		this.sep = "?";
	else
		this.sep = "&";
	
	this.url += this.sep;	
	this.url += queryStr;
};

/** Updates the local parameters with values from the form being currently displayed, if it can be found.
  * If not, then it just silently returns.
  */
UrlFactory.prototype.updateUrlFromForm = function() {
	var form = document.getElementById(this.formId);
	if( form!==undefined && form!==null ) {
		this.debug("Update url %s from form %s", this.url, this.formId);
		this.sep = "?";
		this.url = this.base;
		if( this.url.indexOf("?")>=0 ) this.sep = "&";
		this.updateUrlFromFormNode(form);
	}
	else {
		console.info("Not updating url %s as no form found named %s",this.url,this.formId);
	}
	
	return this.url;
};

// Update the URL based on the current contents of the query form.
UrlFactory.prototype.updateUrlFromFormNode = function(node) {
	// TODO:  Make this method return the URL rather than set it.
	if( node.nodeType!==1 ) return;
	var atts, name, val;
	var nname = node.nodeName;
	if( nname==="INPUT" || nname==="SELECT" || nname==="input" || nname==="select") {
		atts = node.attributes;
		name = atts.getNamedItem("name").nodeValue;
		val = node.value;
	};
	if( name ) {
		this.debug("Adding parameter %s=%s",name,val);
		this[name] = val;
		if( val && val!=="" ) {
			this.url = this.url + this.sep + name+"="+val;
			this.sep = "&";
		};
	};
	
	var children = node.childNodes;
	var i,n=children.length;
	for(i=0; i<n; i++) {
		this.updateUrlFromFormNode(children.item(i));
	};
};


// Update the form based on the content of the indicated URL.
// Form is not cleared before the URL is read.
UrlFactory.prototype.updateFormFromURL = function(url) {
	if(!url) return;
	
	// Read the criteria off the URL into a Map
	var criteria = this.readCriteriaFromURL(url);
	
	var form = document.getElementById(this.formId);
	if( !form || form.nodeType!==1 ) return;

	var inputNodes = form.getElementsByTagName("input");
	var i,n=inputNodes.length;
	for(i=0;i<n;i++) {
		var node = inputNodes[i];
		var name = node.getAttribute("name");
		
		var value = criteria[name];
		if(value) {
			node.setAttribute("value",value);
			console.info("Wrote attribute into form: "+name+"="+value);
		}
	}
};

/*
 * Parse through the URL and populate a map with the criteria 
*/
UrlFactory.prototype.readCriteriaFromURL = function(url) {
	var criteria = this; // not great but it keeps things consistent.
	
	var params = url.split("&");
	for(var idx = 0;idx<params.length;idx++) {
		var p = params[idx];
		var eqIdx = p.indexOf("=");
		if(eqIdx < 0) continue;
		
		var name = p.substring(0,eqIdx);
		var value = p.substring(eqIdx+1);
		
		criteria[name]=value;
	}
	return criteria;
};
