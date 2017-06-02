/**
 * This class retrieves an JSON response from a web service with the XMLHttpRequest object.  
 * It then converts the object into a hierarchical representation where attributes 
 * are converted into simple attributes on the object, and nested objects are converted into a list
 * with the name of the nested object type.
 *  
 * The resulting structure can be customized if necessary, for example, to generate synthetic elements
 * based on the actual elements, or to auto-sort the children etc.  To do so, add a function customize_<local-name>
 *
 */
 
function JSONModel(urlSource) {
	this.urlSource = urlSource;
	if( this.isDebug ) this.debug = console.info;
	var useThis = this;
	
	this.callback = function() {
		useThis.parseData();	
	}
};

JSONModel.prototype.debug = function() {};
//JSONModel.prototype.isDebug = true;


/** Causes a request to be made to update the given object.  Currently creates synchronous requests
 * but eventually that should be changed to use asynchronous and to make the required requests up front
 * or to keep a set of continuations available to complete the request when the data is available.
 * The continuations idea is a good one, as it would allow parts of the view to be created and perhaps even 
 * displayed, but implementing it could be really hard.
 */
JSONModel.prototype.request = function JM_request(service, lazy) {
	if(!service)
		throw "Service not specified for data request";

	if( !this.url ) {
		if( !this.urlSource ) return;
		this.url = this.urlSource.url;
		if( !this.url ) {
			console.warn("No URL specified.");
			return;
		}
	}
	this.requestUrl(service,this.url,null,lazy);
};

/**
 * Makes the given request, along with any modifiers such as GSPS application.
 * If lazy is true, executes asynchronously  
 */
JSONModel.prototype.requestUrl = function JM_requestUrl(service, url, modifiers, lazy) {
	var request;
	if( modifiers ) {
		for(var key in modifiers) {
			url = url + modifiers[key]; 
		}
	}
	
	if( lazy ) {
		var useThis = this;
		var handleResponse = function(str,e) {
			if( str ) {
				var data = useThis.parseJSON(str);
				useThis.parse(data,modifiers);
			}
			useThis.url = null;
			if( typeof(lazy)==="function" ) {
				lazy(useThis,url, str, e);
			}
		};
		service.requestAsync(url,handleResponse);
		return;
	}
	
	var jsonStr = service.request(url);
	var data = this.parseJSON(jsonStr);
	this.parse(data,modifiers);
	this.url = null;
};


// Load the JSON script into a local object
JSONModel.prototype.parseJSON = function SR_parseJSON(jsonStr)
{	
	if(!jsonStr) return null;
	
    try {
    	return eval("("+jsonStr+")");
    } catch(e) {
    	console.error("Caught exception evaluating json:'"+jsonStr+"'");
    	throw e;
    }
};


/** Parses the JSON structure into the model */
JSONModel.prototype.parse = function JM_parse(data, modifiers) {
	for(var attribute in data) {
		// Assign children to the local model.
		if(attribute != "children")
			this[attribute] = data[attribute];
	};
	
	// Handle element children
	if(data.children)
	{
		var children = data.children;
		var i,n = children.length;
		var lst, name, child, xchild;
		
		for(i=0; i<n; i++) {
			child = children[i];
			
			// Create new nodes for each element
			name = child.tagName;
			xchild = this.getChild(name,child,modifiers,i);
			xchild._parent = this;
			xchild.parse(child,modifiers);
		};
	}

	var nodeType = data.tagName;
	if( this["customize_"+nodeType]!=null ) this["customize_"+nodeType]();	
};

/** Parses a String into JSON and thence into this object */
JSONModel.prototype.parseString = function XM_parse(jsonStr,modifiers) {
    var data = this.parseJSON(jsonStr);
	return this.parse(data,modifiers);
};

/** Gets a given child, specified by the given name.  May use other attributes from el to look it up. */
JSONModel.prototype.getChild = function JM_getChild(name,el,modifiers, i) {		
	var lst = this[name];
	var xchild = new JSONModel();
	if( !lst ) {
	  lst = new Array();
	  this[name] = lst;
	  xchild.xmlFirst = true;
	} else xchild.xmlFirst = false;
	lst.push(xchild);
	return xchild;
};

/** Finds the given element in the list child element, having attribute att with the given value 
 * TODO - construct an index instead, and use it consistently.
 */
JSONModel.prototype.find = function JM_find(childName, attrName, value) {
	if(value == null || !this[childName]) {
		console.warn("Aborting search for access "+this[childName]+"."+attrName+"="+value);
		//for(var child in this) console.info("Child: "+child);
		return null;
	}
	
	try
	{
		var lst = this[childName];
		var i,n=lst.length;
		for(i=0; i<n; i++) {
			if( lst[i][attrName]==value ) return lst[i];
		}
	}
	catch(e)
	{
		console.error("Error attempting to access "+childName+"."+attrName+"="+value);
		throw e;
	}
	
};

/** Gets an element from this or any parent of this. */
JSONModel.prototype.get = function (at) {
	var ret=this[at];
	if( ret!==undefined ) return ret;
	if( this._parent ) return this._parent.get(at);
};