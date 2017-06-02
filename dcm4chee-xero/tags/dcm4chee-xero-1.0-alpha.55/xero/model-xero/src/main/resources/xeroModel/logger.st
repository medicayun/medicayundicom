/**
 * Server-side Logger instance that redirects to SLF4J and can be used in the place
 * of the firebug logging object.
 */
function Logger(name) {
	if(!name) name = "js.console";
	
	this.slf4j = org.slf4j.LoggerFactory.getLogger(name);
};

Logger.prototype.info = function(msg) { this.slf4j.info(msg); };
Logger.prototype.error = function(msg) { this.slf4j.error(msg); };
Logger.prototype.debug = function(msg) { this.slf4j.debug(msg); };
Logger.prototype.warn = function(msg) { this.slf4j.warn(msg); };