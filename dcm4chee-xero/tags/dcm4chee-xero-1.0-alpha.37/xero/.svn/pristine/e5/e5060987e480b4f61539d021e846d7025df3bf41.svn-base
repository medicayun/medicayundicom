$unittest()$
$string()$
$domUtils()$
$boccMock()$

org.w3c.dom.element.prototype.id = "";

var model = new Object();
model.IS_IE = false;

var domUtilsTest = new UnitTest();

domUtilsTest.doTestUpdateHtml = function(elementId, originalHtml, replacementHtml, expectedHtml)
{
	var documentHtml = this.removeLineChars(originalHtml);
	
	var domParser = new DOMParser();
	
	document = domParser.parseFromString(documentHtml);
	
	var xmlSerializer = new XMLSerializer();

	var html = this.removeLineChars(replacementHtml);

	var updateHtmlResult = DOMUtils.updateHtml(elementId, html);
	
	var documentAsXMLString = xmlSerializer.serializeToString(document);
	var actual = this.removeLineChars(documentAsXMLString);
	
	var expected = this.removeLineChars(expectedHtml);
	
	$assertTrue(condition="updateHtmlResult!=false", msg="Not expecting importHtml to return false.")$;
	
	$assertEquals(expected="expected", actual="actual", msg="The html is not as expected.")$;            
}

domUtilsTest.testUpdateHtml_WhenAnAttributeValueIsChanged = function()
{
	var elementId = "A";

	var originalHtml = "$attributeValueChanged_Original_Html();format="js"$";
	
	var replacementHtml = "$attributeValueChanged_ReplacementNode_Html();format="js"$";

	var expectedHtml = "$attributeValueChanged_Expected_Html();format="js"$";

    this.doTestUpdateHtml(elementId, originalHtml, replacementHtml, expectedHtml);	
}

domUtilsTest.testUpdateHtml_WhenAnAttributeValueIsAdded = function()
{
	var elementId = "A";

	var originalHtml = "$attributeValueAdded_Original_Html();format="js"$";
	
	var replacementHtml = "$attributeValueAdded_ReplacementNode_Html();format="js"$";

	var expectedHtml = "$attributeValueAdded_Expected_Html();format="js"$";

    this.doTestUpdateHtml(elementId, originalHtml, replacementHtml, expectedHtml);		
}

domUtilsTest.testUpdateHtml_WhenAnAttributeValueIsRemoved = function()
{
	var elementId = "A";
	
	var originalHtml = "$attributeValueRemoved_Original_Html();format="js"$";
	
	var replacementHtml = "$attributeValueRemoved_ReplacementNode_Html();format="js"$";

	var expectedHtml = "$attributeValueRemoved_Expected_Html();format="js"$";
	
    this.doTestUpdateHtml(elementId, originalHtml, replacementHtml, expectedHtml);				
}

domUtilsTest.testUpdateHtml_WhenAChildIsRemoved = function()
{
	var elementId = "A";
	
	var originalHtml = "$childRemoved_Original_Html();format="js"$";
	
	var replacementHtml = "$childRemoved_ReplacementNode_Html();format="js"$";

	var expectedHtml = "$childRemoved_Expected_Html();format="js"$";
	
    this.doTestUpdateHtml(elementId, originalHtml, replacementHtml, expectedHtml);		
}

domUtilsTest.testUpdateHtml_WhenAChildIsMoved = function()
{
	var elementId = "A";
	
	var originalHtml = "$childMoved_Original_Html();format="js"$";
	
	var replacementHtml = "$childMoved_ReplacementNode_Html();format="js"$";

	var expectedHtml = "$childMoved_Expected_Html();format="js"$";
	
    this.doTestUpdateHtml(elementId, originalHtml, replacementHtml, expectedHtml);		
}

domUtilsTest.testUpdateHtml_WhenAChildIsAdded = function()
{
	var elementId = "A";
	
	var originalHtml = "$childAdded_Original_Html();format="js"$";
	
	var replacementHtml = "$childAdded_ReplacementNode_Html();format="js"$";

	var expectedHtml = "$childAdded_Expected_Html();format="js"$";
	
    this.doTestUpdateHtml(elementId, originalHtml, replacementHtml, expectedHtml);		
}

domUtilsTest.testUpdateHtml_WhenAChildIsChanged = function()
{
	var elementId = "A";
	
	var originalHtml = "$childChanged_Original_Html();format="js"$";
	
	var replacementHtml = "$childChanged_ReplacementNode_Html();format="js"$";

	var expectedHtml = "$childChanged_Expected_Html();format="js"$";
	
    this.doTestUpdateHtml(elementId, originalHtml, replacementHtml, expectedHtml);		
}

domUtilsTest.testUpdateHtml_WhenADocumentNodeChangesParent = function()
{
	var elementId = "A";
	
	var originalHtml = "$docNodeParentChanged_Original_Html();format="js"$";
	
	var replacementHtml = "$docNodeParentChanged_ReplacementNode_Html();format="js"$";

	var expectedHtml = "$docNodeParentChanged_Expected_Html();format="js"$";
	
    this.doTestUpdateHtml(elementId, originalHtml, replacementHtml, expectedHtml);		
}

domUtilsTest.testUpdateHtml_WhenAGrandChildIsAdded = function()
{
	var elementId = "A";
	
	var originalHtml = "$grandChildAdded_Original_Html();format="js"$";
	
	var replacementHtml = "$grandChildAdded_ReplacementNode_Html();format="js"$";

	var expectedHtml = "$grandChildAdded_Expected_Html();format="js"$";
	
    this.doTestUpdateHtml(elementId, originalHtml, replacementHtml, expectedHtml);		
}

domUtilsTest.testUpdateHtml_WhenTheFirstChildHasNoNodeId = function()
{
	var elementId = "A";
	
	var originalHtml = "$noChildIdForFirstChild_Original_Html();format="js"$";
	
	var replacementHtml = "$noChildIdForFirstChild_ReplacementNode_Html();format="js"$";

	var expectedHtml = "$noChildIdForFirstChild_Expected_Html();format="js"$";
	
    this.doTestUpdateHtml(elementId, originalHtml, replacementHtml, expectedHtml);		
}

domUtilsTest.testUpdateHtml_WhenAChildWithNoNodeIdOccursAfterANodeIdMatch = function()
{
	var elementId = "A";
	
	var originalHtml = "$noChildIdAfterIdMatch_Original_Html();format="js"$";
	
	var replacementHtml = "$noChildIdAfterIdMatch_ReplacementNode_Html();format="js"$";

	var expectedHtml = "$noChildIdAfterIdMatch_Expected_Html();format="js"$";
	
    this.doTestUpdateHtml(elementId, originalHtml, replacementHtml, expectedHtml);		
}

domUtilsTest.testUpdateHtml_WhenAChildWithNoNodeIdOccursAfterANodeInsertion = function()
{
	var elementId = "A";
	
	var originalHtml = "$noChildIdAfterNodeInsertion_Original_Html();format="js"$";
	
	var replacementHtml = "$noChildIdAfterNodeInsertion_ReplacementNode_Html();format="js"$";

	var expectedHtml = "$noChildIdAfterNodeInsertion_Expected_Html();format="js"$";
	
    this.doTestUpdateHtml(elementId, originalHtml, replacementHtml, expectedHtml);		
}

domUtilsTest.testUpdateHtml_WhenAChildWithNoNodeIdOccursAfterANodeRemoval = function()
{
	var elementId = "A";
	
	var originalHtml = "$noChildIdAfterNodeRemoval_Original_Html();format="js"$";
	
	var replacementHtml = "$noChildIdAfterNodeRemoval_ReplacementNode_Html();format="js"$";

	var expectedHtml = "$noChildIdAfterNodeRemoval_Expected_Html();format="js"$";
	
    this.doTestUpdateHtml(elementId, originalHtml, replacementHtml, expectedHtml);		
}

domUtilsTest.testUpdateHtml_WhenANodeValueIsChanged = function()
{
	var elementId = "A";
	
	var originalHtml = "$nodeValueChanged_Original_Html();format="js"$";
	
	var replacementHtml = "$nodeValueChanged_ReplacementNode_Html();format="js"$";

	var expectedHtml = "$nodeValueChanged_Expected_Html();format="js"$";
	
    this.doTestUpdateHtml(elementId, originalHtml, replacementHtml, expectedHtml);		
}

domUtilsTest.testUpdateHtml_ShouldMatchIdLessChildrenUsingNodeType_WhenIdLessChildrenArePresent = function()
{
	var elementId = "A";
	
	var originalHtml = "$noChildIdMatchUsingNodeType_Original_Html();format="js"$";
	
	var replacementHtml = "$noChildIdMatchUsingNodeType_ReplacementNode_Html();format="js"$";

	var expectedHtml = "$noChildIdMatchUsingNodeType_Expected_Html();format="js"$";
	
    this.doTestUpdateHtml(elementId, originalHtml, replacementHtml, expectedHtml);		
}

domUtilsTest.testGetChildNodesToUpdate_ShouldForceTableElementsToBeInsertedAnew = function()
{
    // oldNode should not be touched, so we set it to null 
    var oldNode = null;
    
    // mock out the childNodes of newNode
    var childNode = new Object();
    childNode.nodeName = "table";
    
    var newNode = new Object();
    newNode.childNodes = new Object();
    newNode.childNodes.length = 1;
    
    newNode.childNodes.item = BoccMock.createMockFunction();
    newNode.childNodes.item.returns = childNode;
    
    var childNodesToUpdate = DOMUtils.getChildNodesToUpdate(oldNode, newNode);
    
    $assertEquals(expected="null", actual="childNodesToUpdate[0]", msg="Table elements should always be inserted anew for performance reasons - artf18477.")$;
}

domUtilsTest.testSetStyle_ShouldClearTheCSSTextField = function()
{
    var elem = new Object();
    elem.style = new Object();
    elem.style.cssText = "top:10;left:4";
    
    var emptyStr = "";
    DOMUtils.setStyle(elem, "");
    
    $assertEquals(expected="emptyStr", actual="elem.style.cssText", msg="The cssText field must be cleared.")$;    
};

domUtilsTest.testSetStyle_ShouldClearTheFlipField = function()
{
    var elem = new Object();
    elem.style = new Object();
    elem.style.flip = "x"
    
    var emptyStr = "";
    DOMUtils.setStyle(elem, "");
    
    $assertEquals(expected="emptyStr", actual="elem.style.flip", msg="The flip field must be cleared.")$;    
};

domUtilsTest.testSetStyle_ShouldParseTheStyleAndAssignTheStyleValuesAsFields = function()
{
    var elem = new Object();
    elem.style = new Object();
    
    var topVal = "15px";
    var rotVal = "270";
    DOMUtils.setStyle(elem, "top:15px;rotation:270");
    
    $assertEquals(expected="topVal", actual="elem.style.top", msg="The top field must be set.")$;    
    $assertEquals(expected="rotVal", actual="elem.style.rotation", msg="The rotation field must be set.")$;    
};

domUtilsTest.testSetStyle_ShouldParseAndEvaluateExpressionStyleValues = function()
{
    var elem = new Object();
    elem.style = new Object();
    
    var rotVal = "270";
    DOMUtils.setStyle(elem, "rotation:expression(180 + 90)");
    
    $assertEquals(expected="rotVal", actual="elem.style.rotation", msg="The rotation field must be set.")$;    
};

domUtilsTest.testSetStyle_ShouldTrimSubElements = function()
{
    var elem = new Object();
    elem.style = new Object();
    
    var topVal = "15px";
    var rotVal = "270";
    var customVal = "2 2";
    DOMUtils.setStyle(elem, " top:  15px  ; rotation : 270 ; custom: 2 2  ");
    
    $assertEquals(expected="topVal", actual="elem.style.top", msg="The top field must be set.")$;    
    $assertEquals(expected="rotVal", actual="elem.style.rotation", msg="The rotation field must be set.")$;    
    $assertEquals(expected="customVal", actual="elem.style.custom", msg="The custom field must be set.")$;    
};

domUtilsTest.testSetStyle_ShouldGracefullyHandleSemiColonPrefixedAndSuffixedStyles = function()
{
    var elem = new Object();
    elem.style = new Object();
    
    var topVal = "15px";
    var rotVal = "270";
    DOMUtils.setStyle(elem, ";top:15px;rotation:270;");
    
    $assertEquals(expected="topVal", actual="elem.style.top", msg="The top field must be set.")$;    
    $assertEquals(expected="rotVal", actual="elem.style.rotation", msg="The rotation field must be set.")$;    
};

domUtilsTest.testSetStyle_ShouldConvertToJavaScriptStyleAttributeName = function()
{
    var elem = new Object();
    elem.style = new Object();
    
    var marginLeftVal = "15px";
    DOMUtils.setStyle(elem, "margin-left:15px");
    
    $assertEquals(expected="marginLeftVal", actual="elem.style.marginLeft", msg="The marginLeft field must be set.")$;    
};

domUtilsTest.testGetJSCSSAttrName_ShouldHandleNonHyphenatedAttributes = function()
{
    var expected = "top";
    
    var actual = DOMUtils.getJSCSSAttrName("top");
    
    $assertEquals(expected="expected", actual="actual", msg="The top CSS attribute name is top in JavaScript.")$;
}

domUtilsTest.testGetJSCSSAttrName_ShouldHandleHyphenatedAttributes = function()
{
    var expected = "borderBottomWidth";
    
    var actual = DOMUtils.getJSCSSAttrName("border-bottom-width");
    
    $assertEquals(expected="expected", actual="actual", msg="The border-bottom-width CSS attribute name is borderBottomWidth in JavaScript.")$;
}

domUtilsTest.testGetJSCSSAttrName_ShouldHandleCSSFloat = function()
{
    var expected = "cssFloat";
    
    var actual = DOMUtils.getJSCSSAttrName("float");
    
    $assertEquals(expected="expected", actual="actual", msg="The float CSS attribute name is cssFloat in JavaScript.")$;
}

domUtilsTest.removeLineChars = function(str)
{
	var strMinusLineChars = str.replace(/\r\n|\n/g, "");
	return strMinusLineChars;    
}

DOMUtils.wrapHtml = function(html)
{
	return "$dtd();format="js"$" + '<div xmlns="http://www.w3.org/1999/xhtml" xmlns:svg="http://www.w3.org/2000/svg" xmlns:v="urn:schemas-microsoft-com:vml" xmlns:xlink="http://www.w3.org/1999/xlink">' + html + "</div>";
}

DOMUtils.getNodeListItem = function(nodeList, index)
{
    return nodeList.item(index);
}

domUtilsTest.runUnitTests();