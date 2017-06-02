$unittest()$

function XeroModel() {
};

var xeroView = new Object();
var controller = new XeroController(new XeroModel(),"");

$zoomRectangle()$
$controller()$

var controllerTest = new UnitTest();
function ServiceRequest() {};

controllerTest.init = function() {
	this.origDocument = document;
	document = {
		"getElementById":function(id) {
			return this[id];
       }
	};
	
};

controllerTest.uninit = function() {
	document = this.origDocument;
};

XeroController.prototype.viewRenderedListener = function(lay) {
	lay.fired++;
};

function ViewRenderedLayout(children) {
	this.viewRenderedListeners = ["viewRenderedListener"];
	this.children = children;
	this.fired = 0;
};

controllerTest.test_fireViewRendered_unused = function() {
	var lay = new ViewRenderedLayout();
	controller.fireViewRendered(lay);
	$assertTrue(condition="!lay.fired",msg="The view rendered should not be fired when the layout isn't used.")$;
};

controllerTest.test_fireViewRendered_called = function() {
	var layChild = new ViewRenderedLayout();
	var layChild2 = {"used":true};
	var lay = new ViewRenderedLayout([layChild, layChild2]);
	lay.used = true;
	layChild.used = true;
	controller.fireViewRendered(lay);
	$assertTrue(condition="lay.fired==1",msg="The top view should have been fired")$;
	$assertTrue(condition="layChild.fired==1",msg="The first child should have been fired")$;
	$assertTrue(condition="!layChild2.fired",msg="The second child should not have been fired")$;
};

controllerTest.test_storeScrollPosition = function() {
    this.init();
	var lay = { "id" : "storeItem" };
	document[lay.id] = { "scrollTop": 34 };
	
	controller.storeScrollPosition(lay);

	this.uninit();
	
	$assertTrue(condition="lay.scrollTop==34",msg="Should have stored the scroll top.")$;
	
};

controllerTest.test_fixScrollPosition = function() {
    this.init();
	var lay = { "id" : "storeItem" };
	
	document[lay.id] = { "scrollTop": 25 };
	
	controller.fixScrollPosition(lay);

	$assertTrue(condition="document[lay.id].scrollTop==25",msg="Should have tried to fix the scroll top")$;

	lay.scrollTop = 50;
	controller.fixScrollPosition(lay);
	$assertTrue(condition="document[lay.id].scrollTop==50",msg="Should have fixed the scroll top.")$;

	this.uninit();	
	
};

controllerTest.testGetOnScreenZoomRectangle_ShouldNotAlterRectangle_WhenImageMidPtInView = function()
{
    var zoomRect = new ZoomRectangle([1,1], [9, 9]);
    var expectedZoomRect = zoomRect;
    var rows = 10;
    var cols = 13;
    
    var actualZoomRect = XeroController.getOnScreenZoomRectangle(zoomRect, rows, cols);
    
    $assertEquals(expected="expectedZoomRect", actual="actualZoomRect", msg="Zoom rectangle should not be adjusted.")$;
}

controllerTest.testGetOnScreenZoomRectangle_ShouldAlignImageLeftEdgeWithImagePortCenter_WhenImageZoomedInAndLeftEdgePastPortCenter = function()
{
    var zoomRect = new ZoomRectangle([-4, 5], [2, 9]);
    var expectedZoomRect = new ZoomRectangle([-3, 5], [3, 9]);
    var rows = 10;
    var cols = 13;
    
    var actualZoomRect = XeroController.getOnScreenZoomRectangle(zoomRect, rows, cols);
    $assertEquals(expected="expectedZoomRect", actual="actualZoomRect", msg="Zoom rectangle not adjusted correctly.")$;
}

controllerTest.testGetOnScreenZoomRectangle_ShouldAlignImageMidPtWithAreaRightEdge_WhenImageZoomedOutAndMidPtPastAreaRightEdge = function()
{
    var zoomRect = new ZoomRectangle([-15, -2], [-1, 12]);
    var expectedZoomRect = new ZoomRectangle([-8, -2], [6, 12]);
    var rows = 10;
    var cols = 13;
    
    var actualZoomRect = XeroController.getOnScreenZoomRectangle(zoomRect, rows, cols);
    $assertEquals(expected="expectedZoomRect", actual="actualZoomRect", msg="Zoom rectangle not adjusted correctly.")$;
}

controllerTest.testGetOnScreenZoomRectangle_ShouldAlignImageRightEdgeWithImagePortCenter_WhenImageZoomedInAndRightEdgePastPortCenter = function()
{
    var zoomRect = new ZoomRectangle([13, -1], [18, 5]);
    var expectedZoomRect = new ZoomRectangle([9, -1], [14, 5]);
    var rows = 10;
    var cols = 13;
    
    var actualZoomRect = XeroController.getOnScreenZoomRectangle(zoomRect, rows, cols);
    $assertEquals(expected="expectedZoomRect", actual="actualZoomRect", msg="Zoom rectangle not adjusted correctly.")$;
}

controllerTest.testGetOnScreenZoomRectangle_ShouldAlignImageMidPtWithAreaLeftEdge_WhenImageZoomedOutAndMidPtPastAreaLeftEdge = function()
{
    var zoomRect = new ZoomRectangle([9, 0], [30, 20]);
    var expectedZoomRect = new ZoomRectangle([6, 0], [27, 20]);
    var rows = 10;
    var cols = 13;
    
    var actualZoomRect = XeroController.getOnScreenZoomRectangle(zoomRect, rows, cols);
    $assertEquals(expected="expectedZoomRect", actual="actualZoomRect", msg="Zoom rectangle not adjusted correctly.")$;
};

controllerTest.testGetOnScreenZoomRectangle_ShouldAlignImageTopEdgeWithImagePortCenter_WhenImageZoomedInAndTopEdgeBelowPortCenter = function()
{
    var zoomRect = new ZoomRectangle([4, -4], [7, 2]);
    var expectedZoomRect = new ZoomRectangle([4, -3], [7, 3]);
    var rows = 10;
    var cols = 13;
    
    var actualZoomRect = XeroController.getOnScreenZoomRectangle(zoomRect, rows, cols);
    $assertEquals(expected="expectedZoomRect", actual="actualZoomRect", msg="Zoom rectangle not adjusted correctly.")$;
}

controllerTest.testGetOnScreenZoomRectangle_ShouldAlignImageMidPtWithAreaBottomEdge_WhenImageZoomedOutAndMidPtBelowAreaBottomEdge = function()
{
    var zoomRect = new ZoomRectangle([-10, -10], [23, 3]);
    var expectedZoomRect = new ZoomRectangle([-10, -8], [23, 5]);
    var rows = 10;
    var cols = 13;
    
    var actualZoomRect = XeroController.getOnScreenZoomRectangle(zoomRect, rows, cols);
    $assertEquals(expected="expectedZoomRect", actual="actualZoomRect", msg="Zoom rectangle not adjusted correctly.")$;
}

controllerTest.testGetOnScreenZoomRectangle_ShouldAlignImageBottomEdgeWithImagePortCenter_WhenImageZoomedInAndBottomEdgeAbovePortCenter = function()
{
    var zoomRect = new ZoomRectangle([0, 8], [4, 13]);
    var expectedZoomRect = new ZoomRectangle([0, 6], [4, 11]);
    var rows = 10;
    var cols = 13;
    
    var actualZoomRect = XeroController.getOnScreenZoomRectangle(zoomRect, rows, cols);
    $assertEquals(expected="expectedZoomRect", actual="actualZoomRect", msg="Zoom rectangle not adjusted correctly.")$;
}

controllerTest.testGetOnScreenZoomRectangle_ShouldAlignImageMidPtWithAreaTopEdge_WhenImageZoomedOutAndMidPtAboveAreaTopEdge = function()
{
    var zoomRect = new ZoomRectangle([2, 6], [18, 25]);
    var expectedZoomRect = new ZoomRectangle([2, 5], [18, 24]);
    var rows = 10;
    var cols = 13;
    
    var actualZoomRect = XeroController.getOnScreenZoomRectangle(zoomRect, rows, cols);
    $assertEquals(expected="expectedZoomRect", actual="actualZoomRect", msg="Zoom rectangle not adjusted correctly.")$;
};
