/** Contains static methods and helpers shared between the server and client side */

/** Parses a point into some number of numbers as an array.  
 * @param {String} point
 */
function parsePoint(point) {
	if( !point.substring ) return point;
	var spoint = ""+point;
	var xy = spoint.match(/[\-0-9.]+/g);
	for(var i=0; i<xy.length; i++) {
		xy[i] = parseInt(xy[i]);
	};	
	return xy;
};

