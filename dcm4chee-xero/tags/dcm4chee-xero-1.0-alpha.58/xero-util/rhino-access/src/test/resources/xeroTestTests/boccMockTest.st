$unitTest()$
$boccMock()$

var boccMockTest = new UnitTest();

boccMockTest.testCreateMock_ShouldBeInitializedCorrectly_WhenAFunctionIsMocked = function()
{
    var functionToMock = function(arg0, arg1){ return 1000; };
    
    var mockFunc = BoccMock.createMock(functionToMock);
    
    $assertEquals(expected="0", actual="mockFunc.numCalls", msg="numCalls should initially be 0.")$;
    $assertEquals(expected="0", actual="mockFunc.numCallsExpected", msg="numCallsExpected should initially be 0.")$;
    $assertEquals(expected="null", actual="mockFunc.bodyCallback", msg="bodyCallback should initially be null.")$;
    $assertEquals(expected="undefined", actual="mockFunc.returns", msg="returns should initially be undefined.")$;
    $assertEquals(expected="0", actual="mockFunc.callArgs.length", msg="callArgs should initially be length 0.")$;
}

boccMockTest.testCreateMock_ShouldKeepTrackOfTheNumberOfCalls_WhenAFunctionIsMocked = function()
{
    var functionToMock = function(arg0, arg1){ return 1000; };
    
    var mockFunc = BoccMock.createMock(functionToMock);
    
    mockFunc.numCallsExpected = 1;
    
    mockFunc("hello", 27);
    
    $assertEquals(expected="1", actual="mockFunc.numCalls", msg="numCalls should be 1.")$;
    
}

boccMockTest.testCreateMock_ShouldAllowACallbackForAFunctionBody_WhenAFunctionIsMocked = function()
{
    var arg0Expected = "hello";
    var arg1Expected = 27;
    
    var functionToMock = function(arg0, arg1){ return 1000; };
    
    var mockFunc = BoccMock.createMock(functionToMock);
    
    // Specify both a returns and a bodyCallabck and notice that the bodyCallback takes precedence.
    mockFunc.returns = -1;
    mockFunc.bodyCallback = function(arg0, arg1) {
        $assertEquals(expected="arg0Expected", actual="arg0", msg="Parameters not passed to callback.")$;
        $assertEquals(expected="arg1Expected", actual="arg1", msg="Parameters not passed to callback.")$;
        return 2000;    
    }
    
    var result = mockFunc(arg0Expected, arg1Expected);
    
    $assertEquals(expected="2000", actual="result", msg="The callback was not invoked.")$;
}

boccMockTest.testCreateMock_ShouldAllowAReturnValueToBeSpecified_WhenAFunctionIsMocked = function()
{
    var arg0Expected = "hello";
    var arg1Expected = 27;
    
    var functionToMock = function(arg0, arg1){ return 1000; };
    
    var mockFunc = BoccMock.createMock(functionToMock);
    mockFunc.returns = 2000;
    
    var result = mockFunc(arg0Expected, arg1Expected);
    
    $assertEquals(expected="2000", actual="result", msg="The callback was not invoked.")$;
};

boccMockTest.testCreateMock_ShouldPersistArgumentsPassedToMock_WhenAFunctionIsMocked = function()
{
    var functionToMock = function(arg0, arg1){ return 1000; };
    
    var mockFunc = BoccMock.createMock(functionToMock);
    
    var aArg = "a";
    var bArg = "b";
    
    mockFunc(aArg, 1);
    mockFunc(bArg, 2);
    
    $assertEquals(expected="2", actual="mockFunc.callArgs.length", msg="The call arguments array should have 2 elements")$;
    
    $assertEquals(expected="aArg", actual="mockFunc.callArgs[0][0]", msg="call 0, arg 0 not as expected.")$;
    $assertEquals(expected="1", actual="mockFunc.callArgs[0][1]", msg="call 0, arg 1 not as expected.")$;
    $assertEquals(expected="bArg", actual="mockFunc.callArgs[1][0]", msg="call 1, arg 0 not as expected.")$;
    $assertEquals(expected="2", actual="mockFunc.callArgs[1][1]", msg="call 1, arg 1 not as expected.")$;
    
};

boccMockTest.testVerify_ShouldVerifyTheExpectedNumberOfCallsToAFunction = function()
{
    var functionToMock = function(arg0, arg1){ return 1000; };
    
    var mockFunc = BoccMock.createMock(functionToMock);
    mockFunc.numCallsExpected = 2;
    
    mockFunc();
    mockFunc();
    
    BoccMock.verify(mockFunc);
};

boccMockTest.testVerify_ShouldThrowAnException_WhenAFunctionIsNotCalledAnExpectedNumberOfTimes = function()
{
    var functionToMock = function(arg0, arg1){ return 1000; };
    
    var mockFunc = BoccMock.createMock(functionToMock);
    mockFunc.numCallsExpected = 2;
    
    mockFunc();
    
    BoccMock.verify(mockFunc);
};

