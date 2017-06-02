$unittest()$
$string()$

var stringTest = new UnitTest();

stringTest.testTrim_ShouldNotAlterString_WhenTheStringHasNoLeadingOrTrailingWhitespace = function()
{
    var str = "The string."
    var expected = str;
    var actual = str.trim();
    $assertEquals(expected="expected", actual="actual", msg="The string should not be altered.")$;
};

stringTest.testTrim_ShouldTrimLeadingWhitespace = function()
{
    var str = " \n\tThe string."
    var expected = "The string.";
    var actual = str.trim();
    $assertEquals(expected="expected", actual="actual", msg="The leading whitespace must be removed.")$;
};

stringTest.testTrim_ShouldTrimTrailingWhitespace = function()
{
    var str = "The string.\t \n"
    var expected = "The string.";
    var actual = str.trim();
    $assertEquals(expected="expected", actual="actual", msg="The trailing whitespace must be removed.")$;
};

stringTest.testTrim_ShouldTrimLeadingAndTrailingWhitespace = function()
{
    var str = "\n\n \t The string.\t \r\n  "
    var expected = "The string.";
    var actual = str.trim();
    $assertEquals(expected="expected", actual="actual", msg="Both the trailing and leading whitespace must be removed.")$;
};