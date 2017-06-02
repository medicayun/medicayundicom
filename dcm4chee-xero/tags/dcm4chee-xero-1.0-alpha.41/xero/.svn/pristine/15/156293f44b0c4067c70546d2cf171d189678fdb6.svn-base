$unittest()$

var staticPluginTest = new UnitTest();
var scriptXml = "$model/scripts(version="1.0-alpha5"); format="js"$";

staticPluginTest.testScriptPlugins = function() {
   console.info("%s",scriptXml);
	var i = scriptXml.indexOf("src=\"js/sarissa.js?v=1.0-alpha5&amp;theme=\"");
	console.info("%s",i);
	$assertTrue(condition="i>0",msg="Scripts generation must include sarissa.js.")$;
};
