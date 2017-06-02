
function setPositionRelativeToParent(parentElementId, elementId)
{
  var obj = document.getElementById(elementId);

  if (obj)
  {
	  var parent = document.getElementById(parentElementId);
	  
	  var x=y=0;  
	  if (parent.offsetParent) 
	  {
	     x=parent.offsetLeft;
	     y=parent.offsetTop+parent.offsetHeight+1;
	     while (parent=parent.offsetParent) 
	     {
	         x+=parent.offsetLeft;
	         y+=parent.offsetTop;
	     }
	  }
	  
	  obj.style.top = new String(y+"px");
	  obj.style.left = new String(x+"px");
  }
}


function checkLastOnClickInParent(e, parentId)
{
	if (parentId)
	{
		var parent = document.getElementById(parentId);

		if (parent)
		{
			var child = getLastOnClickEventSource(e);
	
			while (child)
			{
				if (child.parentNode==parent)
				{
					return true;
				}
				
				child = child.parentNode;
			}
		}
	}
	
	return false;
}


function getLastOnClickEventSource(e)
{
	var el;
	
	if (!e)
	{
		e = window.event;
	}
	
	if (e.type=='click')
	{
		if (e.target)
	    {
			el = e.target;
	    }
			
		else if (e.srcElement)
	    {
			el = e.srcElement;
	    }
	}
	
	return el;
}
