/**
 * Various utilities for DOM nodes and HTML strings.  In particular, utility functions
 * exist for merging new node data (i.e. attributes and children differences) into an existing node.
 */

function DOMUtils(){};

/**
 * Updates an existing DOM element with an HTML string.
 * elem - the element or element id (i.e. a String) to update
 * html - the HTML string to update the element with; the html
 * is rooted by the element to update
 * returns the element that is updated
 */
DOMUtils.updateHtml = function(elem, html)
{
	var oldElem = typeof elem === "string" ? document.getElementById(elem) : elem;
	
	var domParser = new DOMParser();
    var wrappedHtml = DOMUtils.wrapHtml(html);	
	var htmlDoc = domParser.parseFromString(wrappedHtml, "application/xml");
	    
    var newElem = htmlDoc.documentElement.firstChild;
    DOMUtils.updateNodes(oldElem, newElem);
    
	return oldElem;    
};

/**
 * Updates an existing node with data from a new node.  The attributes,
 * value and children are all updated.
 * oldNode - the node to update
 * newNode - the new node data
 */
DOMUtils.updateNodes = function(oldNode, newNode)
{
    DOMUtils.updateAttributes(oldNode, newNode);

    DOMUtils.updateNodeValue(oldNode, newNode);
    
    // images are assumed to be leaf nodes, so there are no children to process
    if(!DOMUtils.isImageNode(oldNode))
    {
        var childNodeArr = DOMUtils.getChildNodesToUpdate(oldNode, newNode);
        
        DOMUtils.removeChildNodes(oldNode, childNodeArr);
        
        DOMUtils.insertNewChildNodes(oldNode, newNode, childNodeArr);
        
        DOMUtils.updateChildNodes(oldNode, newNode, childNodeArr);
    }    
};

/**
 * Determines if the given node is an image.
 */
DOMUtils.isImageNode = function(node)
{
    var nodeName = node.nodeName;
    return (nodeName == "image" || nodeName == "IMAGE");
}

/**
 * Updates an existing node's value with another node's value.
 * oldNode - the value of this node will be updated
 * newNode - the value of this node will be the new value
 */
DOMUtils.updateNodeValue = function(oldNode, newNode)
{
    if(oldNode.nodeValue != newNode.nodeValue)
    {
        oldNode.nodeValue = newNode.nodeValue;
    }
};

/**
 * Updates the children of oldNode that are specified in childNodeArr.
 * The children are updated with the corresponding child nodes of childNodeArr.
 * oldNode - the children of this node are updated
 * newNode - the Children of this node are used to update those in oldNode
 * childNodeArr - an Array of child nodes of oldNode that must be udpated; the
 * nodes in this array must be ordered such that the ith element in the array
 * corresponds to the ith child of newNode.
 */
DOMUtils.updateChildNodes = function(oldNode, newNode, childNodeArr)
{
    for(var i = 0; i < childNodeArr.length; i += 1)
    {
        var oldChild = childNodeArr[i];
        if(oldChild != null)
        {
            if(oldNode != oldChild.parentNode)
            {
                // oldChild exists elsewhere in the document, so now insert it as a child of oldNode.
                var refChild = DOMUtils.getNodeListItem(oldNode.childNodes, i);
                oldNode.insertBefore(oldChild, refChild);
            }
            
            var newChild = DOMUtils.getNodeListItem(newNode.childNodes, i);
            DOMUtils.updateNodes(oldChild, newChild);
        }
    }    
};

/**
 * Inserts new child nodes to oldNode.  The new child nodes are taken from
 * newNode and are the children that have a corresponding null entry in childNodeArr.
 * oldNode - the node to which the children are added
 * newNode - the node from which the new child nodes are taken
 * childNodeArr - an Array of child nodes of oldNode that must be udpated; the
 * nodes in this array must be ordered such that the ith element in the array
 * corresponds to the ith child of newNode; a null entry in the array indicates
 * that the corresponding child in newNode is a new child that must be inserted 
 * as a child of oldNode
 */
DOMUtils.insertNewChildNodes = function(oldNode, newNode, childNodeArr)
{
    for(var i = 0; i < childNodeArr.length; i += 1)
    {
        var childNode = childNodeArr[i];
        if(childNode == null)
        {
            var newChild = DOMUtils.getNodeListItem(newNode.childNodes, i);
            var importedNewChild = document.importNode(newChild, true);
            if(importedNewChild)  // in IE the imported node is sometimes undefined
            {
                var refChild = DOMUtils.getNodeListItem(oldNode.childNodes, i);
                if(refChild != null)
                {
                    oldNode.insertBefore(importedNewChild, refChild);
                }
                else
                {
                    oldNode.appendChild(importedNewChild);
                }
            }
        }
    }
};

/**
 * Gets an array of child nodes of oldNode that must be updated by child nodes of
 * newNode.  The array returned has a length equal to the number of child nodes in
 * newNode.  Null entries in the array indicate that the corresponding child in
 * newNode has to be inserted as a new child of oldNode.
 * returns the array of child nodes of oldNode that must be updated by child nodes of
 * newNode
 */
DOMUtils.getChildNodesToUpdate = function(oldNode, newNode)
{
    var childNodeArr = new Array(0);
    childNodeArr.lastNodeMatchIndex = -1;
        
    for(var i = 0; i < newNode.childNodes.length; i += 1)
    {
        var newChild = DOMUtils.getNodeListItem(newNode.childNodes, i);
        
        if(DOMUtils.isTableNode(newChild))
        {
            childNodeArr[i] = null;
        }
        else
        {
            var childId = DOMUtils.getElementId(newChild);
            if(childId != null && childId != "")
            {
                var oldChild = DOMUtils.getChildById(oldNode, childId);
                
                if(oldChild != null)
                {
                    childNodeArr[i] = oldChild;
                }
                else
                {
                    var nodeFromDoc = document.getElementById(childId);
                    
                    childNodeArr[i] = nodeFromDoc;
                }
            }
            else
            {
                childNodeArr[i] = DOMUtils.getIdLessChildMatch(oldNode, childNodeArr, newChild);
            }
            
            if(childNodeArr[i] != null)
            {
                childNodeArr.lastNodeMatchIndex = i;
            }
        }
        
    }
    
    return childNodeArr;
};

/**
 * Determines if the given node is a table.
 */
DOMUtils.isTableNode = function(node)
{
    var nodeName = node.nodeName;
    return (nodeName == "TABLE" || nodeName == "table");
}

/**
 * Gets the child node of oldNode that should be matched with newChildNode.
 * The matched node is one that can be updated with newChildNode.
 * It is assumed that newChildNode has no element id and as such the matching
 * child of oldNode will also not have an id.  
 * The node returned is a subsequent sibling of the last non-null element of childNodeArr
 * (or of the first child of oldNode if every element of childNodeArr is empty)
 * that has no element id and the same node type and name as newChildNode.  Null is returned 
 * if no such child is found.
 * 
 * oldNode - the children of this node are searched for a match
 * childNodeArr - an array of child nodes of oldNode that have already been matched
 * to be updated
 * newChildNode - a match of this node is returned
 */
DOMUtils.getIdLessChildMatch = function(oldNode, childNodeArr, newChildNode)
{
    var candidateNode = null;    
    if(childNodeArr.length == 0)
    {
        candidateNode = oldNode.firstChild;
    }
    else if(childNodeArr.lastNodeMatchIndex == -1)
    {
        candidateNode = oldNode.firstChild;
    }
    else
    {
        candidateNode = childNodeArr[childNodeArr.lastNodeMatchIndex].nextSibling;        
    }
            
    for( ; ; candidateNode = candidateNode.nextSibling)
    {
        if(candidateNode == null)
        {
            return null;
        }
        
        var candidateNodeId = DOMUtils.getElementId(candidateNode);
        if(candidateNodeId)
        {
            continue;
        }
        
        if(candidateNode.nodeType != newChildNode.nodeType)
        {
            continue;
        }

        var newChildNodeNameInLowerCase = newChildNode.nodeName.toLowerCase();
        var candidateNodeNameInLowerCase = candidateNode.nodeName.toLowerCase();
        if(candidateNodeNameInLowerCase != newChildNodeNameInLowerCase)
        {
            if(model.IS_IE)
            {
                // IE strips away the v: from node names like v:group and v:image once the node has been impoted.
                // So just compare the string names following the colon.
                var indexOfColon = newChildNodeNameInLowerCase.indexOf(":");
                if(indexOfColon != -1 && newChildNodeNameInLowerCase.substring(indexOfColon + 1) != candidateNodeNameInLowerCase)
                {
                    continue;
                }
            }
            else
            {
                continue;
            }
        }
        
        return candidateNode;        
    }
    
};

/**
 * Removes the child nodes of node that are not contained in nodesToKeepArr.
 * node - the children of this node that are not in nodesToKeepArr are removed.
 * nodesToKeepArr - an Array of nodes that must not be deleted
 */
DOMUtils.removeChildNodes = function(node, nodesToKeepArr)
{
   var childNodesToRemoveArr = new Array(0);    
   var nodeChildren = node.childNodes;
   for(var i = 0; i < nodeChildren.length; i += 1)
   {
       child = DOMUtils.getNodeListItem(nodeChildren, i);
       if(!DOMUtils.isNodeInArray(child, nodesToKeepArr))
       {
           childNodesToRemoveArr[childNodesToRemoveArr.length] = child;
       }
   }    
   
   for(var i = 0; i < childNodesToRemoveArr.length; i += 1)
   {
       node.removeChild(childNodesToRemoveArr[i]);
   }
};

/**
 * Determines if the given node is in nodeArr.
 */
DOMUtils.isNodeInArray = function(node, nodeArr)
{
   for(var i = 0; i < nodeArr.length; i += 1)
   {
       if(nodeArr[i] === node)
       {
           return true;
       }
   }
   return false;    
};

/**
 * Gets the child of elem with the given id.
 */
DOMUtils.getChildById = function(elem, id)
{
    var elemChildren = elem.childNodes;
    for(var i = 0; i < elemChildren.length; i += 1)
    {
        var child = DOMUtils.getNodeListItem(elemChildren, i);
        
        var childId = DOMUtils.getElementId(child);
        
        if(childId != null && childId == id)
        {
            return child;
        }
    }
    
    return null;
};

/**
 * Updates the attributes of oldElem with those of newElem.
 * It is assumed that the two elements have the same id.
 */
DOMUtils.updateAttributes = function(oldElem, newElem)
{
    DOMUtils.removeMissingAttributes(oldElem, newElem);
    
    DOMUtils.updateModifiedAttributes(oldElem, newElem);
};

/**
 * Removes attributes in oldElem that do not exist in newElem.
 */
DOMUtils.removeMissingAttributes = function(oldElem, newElem)
{
    var oldAttrs = oldElem.attributes;
    
    if(oldAttrs != null)
    {
        var oldAttrNamesToRemove = new Array(0);
        
        for(var i = 0; i < oldAttrs.length; i += 1)
        {
            var oldAttrName = oldAttrs.item(i).name;
            if(!DOMUtils.hasAttribute(newElem, oldAttrName) && oldElem.getAttribute(oldAttrName))
            {
                oldAttrNamesToRemove[oldAttrNamesToRemove.length] = oldAttrName;
            }
        }
        
        for(var i = 0; i < oldAttrNamesToRemove.length; i += 1)
        {
            oldElem.removeAttribute(oldAttrNamesToRemove[i]);
        }	    
    }    
};

/**
 * Updates oldElem to have the attributes in newElem.
 */
DOMUtils.updateModifiedAttributes = function(oldElem, newElem)
{
    var newAttrs = newElem.attributes;
    	
    if(newAttrs != null)
    {
        for(var i = 0; i < newAttrs.length; i += 1)
        {
            var attrName = newAttrs.item(i).name;
            var attrNameInLowerCase = attrName.toLowerCase();
            var newAttrValue = newAttrs.item(i).value;
            
            if(model.IS_IE && attrNameInLowerCase == "class")
            {
                DOMUtils.setClassName(oldElem, newAttrValue);
            }
            else if(attrNameInLowerCase == "style")
            {
                DOMUtils.setStyle(oldElem, newAttrValue);
            } 
            else if(oldElem.getAttribute(attrName) instanceof Function)
            {
//                console.debug("ignoring function attribute", attrName);
//                var funcAttrValue = new Function(newAttrValue);
//                oldElem.setAttribute(newAttrs.item(i).name, funcAttrValue);                
            }
            else if(attrName.indexOf("xlink:") == 0)
            {
                // xlink attributes in opera belong to a particular name space.
                DOMUtils.setAttributeNS(oldElem, "http://www.w3.org/1999/xlink", attrName, newAttrValue);
            }
            else 
            {
                DOMUtils.setAttribute(oldElem, attrName, newAttrValue);
            }
        }
    }    
};

/**
 * Sets the attribute with name attrName to have value attrValue
 * in elem.  The attribute is only set if elem does not already
 * have the same value for the attribute.
 */
DOMUtils.setAttribute = function(elem, attrName, attrValue)
{
    var oldAttrValue = elem.getAttribute(attrName);
    if(oldAttrValue != attrValue)
    {
        elem.setAttribute(attrName, attrValue);
    }    
};

/**
 * Sets an attribute on elem with the given namespace (ns).   attrName is the attribute name
 * while attrValue is the value to be assigned to the attribute for elem.  No value is set
 * if the given element already has the given attribute value.
 */
DOMUtils.setAttributeNS = function(elem, ns, attrName, attrValue)
{
    var oldAttrValue = elem.getAttributeNS(ns, attrName);
    if(oldAttrValue != attrValue)
    {
        elem.setAttributeNS(ns, attrName, attrValue);
    }                    
};

/**
 * Sets the class (name) attribute on the given element.  The class name is only
 * updated if it indeed changed.
 */
DOMUtils.setClassName = function(elem, className)
{
    if(elem.className != className)
    {
        elem.className = className;
    }    
};

/**
 * Sets the style attribute on the given element.  The style attribute is always
 * updated because there is no good mechanism to determine if it indeed changed.
 */
DOMUtils.setStyle = function(elem, style)
{
    // In IE flip has to be explicitly cleared.  Note this has to be done before
    // we clear the cssText or else it will not get cleared properly.
    if(elem.style.flip)
    {
        elem.style.flip = "";
    }

    // Clear the current style by setting the cssText property to the empty string.
    elem.style.cssText = "";

    // Split up the style value into key-value pairs.
    var pairs = style.split(";");
    for(var i = 0; i < pairs.length; i += 1)
    {
        var keyAndVal = pairs[i].split(":");
        if(keyAndVal.length === 2)
        {
            var val = keyAndVal[1].trim();
            if(val.indexOf("expression") == 0)
            {
               val = eval(val.substring(11, val.length - 1));
            }
                
            var key = DOMUtils.getJSCSSAttrName(keyAndVal[0].trim());
            elem.style[key] = val;
         }
     }
};

/**
 * Gets the JavaScript CSS attribute name that is equivalent to the given CSS attribute
 * name as it appears in HTML.
 */
DOMUtils.getJSCSSAttrName = function(attrName)
{
    if(attrName=="float")
    {
        return "cssFloat";
    }
    else
    {
        var splitAttrName = attrName.split("-");
        if(splitAttrName.length == 1)
        {
            return splitAttrName[0];
        }
        else
        {
            for(var i = 1; i < splitAttrName.length; i += 1)
            {
                splitAttrName[i] = splitAttrName[i].charAt(0).toUpperCase() + splitAttrName[i].substring(1);
            }
            return splitAttrName.join("");
        };
    };
};

/**
 * Determines if the given node has an attribute with the given name.
 */
DOMUtils.hasAttribute = function(node, attrName)
{
    if(node.hasAttribute)
    {
        return node.hasAttribute(attrName);
    }
    else if(node.attributes)
    {    
        var attrs = node.attributes;
        
        for(var i = 0; i < attrs.length; i += 1)
        {
            var attr = attrs.item(i);
            if(attr.name == attrName)
            {
                return true;
            }
        }
    }
    return false;
};

/**
 * Gets the id of the given element.
 */
DOMUtils.getElementId = function(elem)
{
    if(elem.id !== undefined)
    {
        return elem.id;
    }
       
    if(!elem.attributes)
    {
        return null;
    }
    
    var nodeId = elem.getAttribute("id");
    
    if(nodeId)
    {
        return nodeId;
    }
    
    nodeId = elem.getAttribute("ID");
    
    return nodeId;
};

/**
 * Gets the item at the given index of nodeList.
 */
DOMUtils.getNodeListItem = function(nodeList, index)
{
    if(index < nodeList.length)
    {
       return nodeList[index];
    }
    else
    {
        return null;
    }
};

/**
 * Wraps the given string of html such that it can be parsed by a DOMParser.
 */
DOMUtils.wrapHtml = function(html)
{
	return '<div xmlns="http://www.w3.org/1999/xhtml" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:svg="http://www.w3.org/2000/svg" xmlns:v="urn:schemas-microsoft-com:vml">' + html + "</div>";
};