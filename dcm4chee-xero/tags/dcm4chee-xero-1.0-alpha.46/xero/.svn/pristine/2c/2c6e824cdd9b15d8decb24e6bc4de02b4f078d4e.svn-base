/*
Request from the client to a JSON based web service.
*/

function ServiceRequest() {
};


// Return the document that is indicated by the url
ServiceRequest.prototype.request = function SR_request(url) {
	// TODO: Implement as a relative path
	// TODO: Add version / ae fixes.
	
	console.info("Requesting JSON data "+url);
		
	var jsonStr = this.requestDocumentByURL(url);
	return jsonStr;
};

// Request the document from the server 
ServiceRequest.prototype.requestDocumentByURL = function SR_requestDocumentByURL(url)
{
	// TODO: Add a way to return errors or status values.
	var request = new XMLHttpRequest();
	request.open("GET",url,false);
	request.send(null);
	if( request.readyState!=4 ) {
		console.warn("Request was not responded to correctly:"+request.readyState);
		throw request;
	}
	if( request.status!=200 ) {
		console.warn("Server had error on request:"+request.status);
		throw request;
	}
	if( request.responseText.indexOf('id="LoginPage"')>0 ) {
		throw request;
	}
	return request.responseText;
}


// Return the document that is indicated by the url asynchronously.
ServiceRequest.prototype.requestAsync = function (url, callback) {
	console.info("Requesting JSON data "+url);
		
	var request = new XMLHttpRequest();
	request.onreadystatechange = function() {
		if( request.readyState!=4 ) return;
		if( request.status!=200 ) {
			return callback(null,request);
		}
		if( request.responseText.indexOf('id="LoginPage"')>0 ) {
			return callback(null,request);
		}
		return callback(request.responseText);
	};
	
	request.open("GET",url,true);
	request.send(null);
};

