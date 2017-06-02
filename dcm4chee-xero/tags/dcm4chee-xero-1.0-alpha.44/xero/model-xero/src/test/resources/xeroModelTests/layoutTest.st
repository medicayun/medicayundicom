$unittest()$
$boccMock()$
$layout/layout()$

var layoutTest = new UnitTest();

layoutTest.init = function() {
	this.lay = new Layout("lay");
	this.lay0 = new Layout("lay0");
	this.lay.add(this.lay0);
	this.lay1 = new Layout("lay1");
	this.lay.add(this.lay1);
	this.lay2 = new Layout("lay2");
	this.lay2.used = false;
	this.lay.add(this.lay2);
};

layoutTest.test_hide_setsUsedFalse = function() {
	this.init();
	$assertEquals(expected="true", actual="this.lay.used",message="Initial used should be true.")$;
	$assertEquals(expected="true", actual="this.lay0.used",message="lay0 used should be true.")$;
	this.lay.hide();
	$assertEquals(expected="false", actual="this.lay.used",message="Initial used should be true.")$;	
	$assertEquals(expected="true", actual="this.lay0.used",message="lay0 should still be true")$;
};

layoutTest.test_call_hideListener = function() {
	this.init();
	this.lay.hideListener = BoccMock.createMockFunction(1);
	this.lay1.hideListener = BoccMock.createMockFunction(1);
	this.lay2.hideListener = BoccMock.createMockFunction(0);
	$assertEquals(expected="true", actual="this.lay.used",message="Initial used should be true.")$;
	this.lay.hide();
	BoccMock.verify(this.lay.hideListener);
	BoccMock.verify(this.lay1.hideListener);
	BoccMock.verify(this.lay2.hideListener);
};