/** Contains the model for Xero.
  * The model has a few parts:
  * 1. Static layout/menu/prefs information - this can be hardcoded or read as XML or JSON from the server.
  * 2. Query - contains the information about what the current query parameters are.  Updated whenever the 
  *        user performs a new find operation.
  * 3. search results - the information from the XML file that defines the results for the search.
  * 4. Image display information - TODO still.
  */
function XeroModel(is_ie,i18n) {
	if( i18n ) this.i18n = i18n;
	 
	this.IS_IE = is_ie;
	
	this.init();
};

if( !console.info ) {
	// Ensure that the basic logging features are present.
	console.info = console.log;
	console.warn = console.log;
};

if( !console.time) {
	// Ensure that the timing functions are available.
    console.time = function(name)
        {
            this.timeMap[name] = (new Date()).getTime();
        };
    console.timeEnd = function(name) {
            if (name in this.timeMap)
            {
                var delta = (new Date()).getTime() - this.timeMap[name];
                this.info([name+ ":"+ delta+"ms"]);
                delete this.timeMap[name];
            }
        };
    console.timeMap = new Object();
};

XeroModel.prototype.version="$version$";

/** Shared data is the static data that is shared between server and client. */
XeroModel.prototype.shared = new Object();

$xeroModels:{ $(it)()$ }$