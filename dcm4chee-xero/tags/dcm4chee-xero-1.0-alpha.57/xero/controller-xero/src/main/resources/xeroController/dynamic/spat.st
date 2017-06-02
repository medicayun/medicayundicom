/** Handles the spatial transformations - rotate, flip, spatReset */

/** Handle right rotating the image */
XeroController.prototype.rotateRight = function(lay) {
	this.userEvent("rotateRight");
	this.userEvent("logRotateRight");
	lay = this.findLayout(lay);
	this.rotateSeries(lay,90);
};
/** Handle left rotating the image */
XeroController.prototype.rotateLeft = function(lay) {
	this.userEvent("rotateLeft");
	this.userEvent("logRotateLeft");
	lay = this.findLayout(lay);
	this.rotateSeries(lay,270);
};

XeroController.prototype.rotateSeries = function(lay,angle) {
	console.info("Rotate series - trying to rotate the image",angle,"layout",lay.id);
	var nav = this.getNavigate(lay);
	var rot = nav.merged.get("rotation");
	var flip = nav.merged.get("horizontalFlip");
	flip = (flip && flip!=="FALSE" && flip!=="false");
	if(!rot) rot = 0;
	nav=nav.parent;
	if( flip ) rot = (rot+360-angle) % 360;
	else rot = (rot+angle) % 360;
	nav.setMerged("rotation",rot);
	this.waitUpdate("rotate",2);
};

/** Handle horizontal flipping the image */
XeroController.prototype.flipHorizontal = function(lay) {
	lay = this.findLayout(lay);
	this.userEvent("flipHorizontal");
	this.userEvent("logFlipHorizontal");
	var nav = this.getNavigate(lay);
	var flip = nav.merged.get("horizontalFlip");
	nav=nav.parent;
	flip = !flip;
	nav.setMerged("horizontalFlip",flip);
	this.waitUpdate("mouseUp",1);
};

/** Handle the spatial transform reset operation */
XeroController.prototype.spatReset = function(lay,e,preventUpdate) {
	lay = this.findLayout(lay);
	if( !preventUpdate ) this.userEvent("spatReset");
	var nav = this.getNavigate(lay);
	nav=nav.parent;
	nav.setMerged("horizontalFlip");
	nav.setMerged("rotation");
	if(! preventUpdate ) this.waitUpdate("mouseUp",2);
};

XeroController.prototype.alt_clickImage = XeroController.prototype.rotateRight;