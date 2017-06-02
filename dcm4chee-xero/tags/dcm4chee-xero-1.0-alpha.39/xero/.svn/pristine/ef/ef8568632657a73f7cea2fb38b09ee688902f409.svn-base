/** Contains the server side javascript for the model */
$rhino()$

// Setup a SLF4J Logger bridge for logging
$logger()$
var console = new Logger("js.console");
var isServer = true;

// HACK!  Read in from the commandline options
getViewportSize = function() {
	return [ 600, 400 ];
};

// HACK!  We don't actually preload images in the server!
function Image(){};

$sharedStatic()$

$model()$

// Server-side data request method:  uses URIResolver to bind the required data.
ServiceRequest.prototype.requestDocumentByURL = function SR_requestDocumentByURL(url)
{
	var source = this.resolver.resolve(url,null);
	var reader = new java.io.BufferedReader(source.getReader());
	
	var document = new java.lang.StringBuilder();
	while(true)
	{
		var line = reader.readLine();
		if(line == null) break;
		document.append(line);
	}
	return document.toString();
};

/** The non-asynchronous request.  Server side, ALL requests are
 * made synchronously as there isn't any timing/notification services
 * on the server side, and the page likely hasn't been displayed yet to be able
 * to show anything.  Otherwise, this duplicates the behaviour of the client side
 * in terms of calling the callback mechanism.
 */
ServiceRequest.prototype.requestAsync = function (url, callback) {
    try {
		var doc = this.requestDocumentByURL(url);
		callback(url);
	} catch(e) {
		callback(null,e);
	}
};


$if(HAS_CONTROLLER)$
/**************************************************************************************************************
 * Server controller include
 **************************************************************************************************************
$controller(IS_SERVER="true")$

// Update the client UI using the indicated URL
XeroController.prototype.updateClientWithResolver = function(url,resolver)
{
	console.info("Controller.updateClient() invoked");
	
	// Assign the resolver to the current service request.
	this.service.resolver = resolver;
	this.updateClient(url);
};

$else$

function XeroController() {};
	
$endif$

