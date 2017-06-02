$unittest()$
$layout/layout()$
$layout/imageLayout()$

/** A test object that pretends to be a "merged" object, and contains imageLayout as a child element for testing. */
function ILTest(width,height, presets) {
	this.imageLayout = new ImageLayout();
	this.imageLayout.merged = this;
	this.imageLayout.image = this;
	this.imageLayout.navigate = this;
	this.external = this;
	this.get = function(at) { return this[at]; };
	this.imageLayout.width = width;
	this.imageLayout.height = height;
	var key;
	for(key in presets) {
		this[key] = presets[key];
	};
};
ILTest.prototype.get = function ILTest_get(v) {
	return this[v];
};

imageLayoutTests = new UnitTest();

/** Tests the simple scale to fit */
imageLayoutTests.testScaleToFit = function ILT_scaleToFitTest() {
	var ilt = new ILTest(200,300, {"Rows":"512", "Columns":512});
	var il = ilt.imageLayout;
	il.updatePosition();
	assertEquals(il.scale,200/512,"Scale should match to the width.");
	assertEquals(il.translateX,0,"Should fit width exactly.");
	assertEquals(il.translateY,50,"Should translate 50 to center.");

	ilt = new ILTest(300,200, {"Rows":"512", "Columns":512});
	il = ilt.imageLayout;
	il.updatePosition();
	assertEquals(il.scale,200/512,"Scale should match to the width.");
	assertEquals(il.translateX,50,"Should translate 50 to center.");
	assertEquals(il.translateY,0,"Should fit height exactly.");

	ilt = new ILTest(600,700, {"Rows":"512", "Columns":512});
	il = ilt.imageLayout;
	il.updatePosition();
	$assertTrue(condition="il.scale==600/512",msg="Scale should be 600/512")$;
	$assertTrue(condition="il.translateX===0",msg="Should not translate to center.")$;
	$assertTrue(condition="il.translateY===50",msg="Should translate to center.")$;

};

/** Tests the simple magnify */
imageLayoutTests.testMagnify = function ILT_magnifyTest() {
	// First a test that is too large
	var ilt = new ILTest(200,300, {"Rows":"512", "Columns":512, "magnify":0.5, "size":"MAGNIFY"});
	var il = ilt.imageLayout;
	il.updatePosition();
	assertEquals(il.scale,0.5,"magnify scale");
	assertEquals(il.translateY,22,"Should translate 22 to center.");
	assertEquals(il.translateX,-28,"Should translate off edge.");

	// Now one that fits
	ilt.magnify = 0.25;
	il.updatePosition();
	assertEquals(il.scale,0.25,"magnify scale");
	assertEquals(il.translateX,36,"Should translate 36 to center.");
	assertEquals(il.translateY,86,"Should translate 86 to center.");
}

/** Tests the 3 rotations in combination with scale to fit - and a size that can be used to ensure correct scale is computed. */
imageLayoutTests.testRotation = function ILT_rotationTest() {
	var ilt = new ILTest(200,300, {"Columns":400, "Rows":900, "rotation":90});
	var il = ilt.imageLayout;
	il.updatePosition();
	assertEquals(il.scale,2/9,"Rotation scale");
	assertEquals(il.translateX,200,"rot90 translate X");
	assertFloat(il.translateY,(300-400*2/9)/2,"rot90 tranlate Y");

	ilt.rotation = 180;
	il.updatePosition();
	assertEquals(il.scale,3/9,"Rotation scale");
	assertFloat(il.translateX,(200+400*3/9)/2,"rot180 translate X");
	assertEquals(il.translateY,300,"rot180 tranlate Y");

	ilt.rotation = 270;
	il.updatePosition();
	assertEquals(il.scale,2/9,"Rotation scale");
	assertEquals(il.translateX,0,"rot270 translate X");
	assertFloat(il.translateY,(300+400*2/9)/2,"rot270 tranlate Y");
};

/** Tests the flip in all 4 rotations with scale to fit - and a size that can be used to ensure correct scale is computed. */
imageLayoutTests.testRotation = function ILT_rotationTest() {
	var ilt = new ILTest(200,300, {"Columns":400, "Rows":900, "rotation":0, "horizontalFlip":"TRUE"});
	var il = ilt.imageLayout;
	il.updatePosition();
	assertEquals(il.scale,3/9,"Rotation scale");
	assertFloat(il.translateX,(200+400*3/9)/2,"flip rot 0 translate X");
	assertFloat(il.translateY,0,"flip rot0 tranlate Y");
	
	ilt.rotation = 90;
	il.updatePosition();
	assertEquals(il.scale,2/9,"flip rot90 Rotation scale");
	assertFloat(il.translateX,0,"flip rot90 translate X");
	assertFloat(il.translateY,(300-400*2/9)/2,"flip rot90 tranlate Y");

	ilt.rotation = 180;
	il.updatePosition();
	assertEquals(il.scale,3/9,"flip rot180 Rotation scale");
	assertFloat(il.translateX,(200-400*3/9)/2,"flip rot180 translate X");
	assertEquals(il.translateY,300,"flip rot180 tranlate Y");

	ilt.rotation = 270;
	il.updatePosition();
	assertEquals(il.scale,2/9,"flip rot270 Rotation scale");
	assertEquals(il.translateX,200,"flip rot270 translate X");
	assertFloat(il.translateY,(300+400*2/9)/2,"flip rot270 tranlate Y");
};

imageLayoutTests.runUnitTests();