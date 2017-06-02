/*
 * Sets the position of an element (i.e popup) relative to another element (i.e parent)
 * For both, you need to specify the points that should be aligned. 
 * Further you are able to specify an additional offset that is going 
 * to be added to the calculated position.
 */
function setPositionRelativeToParent(parentId, elementId, parentAlign, elementAlign, offsetX, offsetY)
{
	var options = {
		of: $("[id='"+parentId+"']"),
		my: elementAlign,
		at: parentAlign,
		offset: offsetX + ' ' + offsetY,
		collision: 'fit fit'
	};
	
	$("[id='"+elementId+"']").position(options);
}

function selectTC(elementId, cssClassSelected, cssClassEvenUnselected, cssClassOddUnselected)
{
	$(".tc-content-table tr").each(function(index) {
		itemId = $(this).attr('id');
		
		if (itemId)
		{
			$(this).removeClass();
			$(this).removeAttr('selected');
			
			if (elementId==itemId)
			{
				$(this).addClass(cssClassSelected);
				$(this).attr('selected','selected');
			}
			else if (index%2==0)
			{
				$(this).addClass(cssClassEvenUnselected);
			}
			else
			{
				$(this).addClass(cssClassOddUnselected);
			}
		}
	});
}

function setPopupResizeable(popupId)
{
	$("[id='"+popupId+"']").resizable();
}

var nPopupsToHideOnOutsideClick = 0;

function hidePopup(popupId)
{
	hidePopup(popupId, null);
}

function hidePopup(popupId, hideOnOutsideClickCallbackUrl)
{	
	$("[id='" + popupId + "']").fadeOut('slow', function() {
		$("[id='" + popupId + "']").css({'left':'0','top':'0'});
	});
		
	if (hideOnOutsideClickCallbackUrl!=null)
	{
		nPopupsToHideOnOutsideClick--;
		if (nPopupsToHideOnOutsideClick<=0)
		{
			$(document).off('click', handlePopupOutsideClick);
			nPopupsToHideOnOutsideClick = 0;
		}
	}
}

function showPopup(popupId)
{
	showPopup(popupId, null);
}

function showPopup(popupId, hideOnOutsideClickCallbackUrl)
{
	$("[id='" + popupId + "']").fadeIn('slow');

	if (hideOnOutsideClickCallbackUrl!=null)	
	{		
		if (nPopupsToHideOnOutsideClick==0)
		{
			$(document).on('click', {url:hideOnOutsideClickCallbackUrl}, handlePopupOutsideClick);
		}
		
		nPopupsToHideOnOutsideClick++;
	}
}

function isPopupShown(popupId)
{
	return $("[id='"+popupId+"']").css('display')!='none';
}

function handlePopupOutsideClick(event)
{
	if (shouldHandlePopupOutsideClick(event))
	{
		wicketAjaxGet(event.data.url,function(){},function(){});
	}
}

function shouldHandlePopupOutsideClick(event)
{
	var mouseOverPopup = false;
	$('.tc-popup').each(function() {
		var popupId = $(this).attr('id');
		if (popupId!=null) {
			if (isPopupShown(popupId)) {
				var popupPosition = $(this).offset();
				var popupWidth = $(this).width();
				var popupHeight = $(this).height();

				if (popupPosition!=null && 
						popupWidth!=null && popupHeight!=null)
				{
					if (event.pageX>popupPosition.left-1 && event.pageX<popupPosition.left+popupWidth+1 &&
						event.pageY>popupPosition.top-1 && event.pageY<popupPosition.top+popupHeight+1)
					{
						mouseOverPopup=true;
						return false;
					}
				}
			}
		}
	});
	
	return !mouseOverPopup;
}

function shouldHandlePopupMouseOut(event, popupId)
{
	var popupPosition = null;
	var popupWidth = null;
	var popupHeight = null;
	var popup = $("[id='"+popupId+"']");
	
	popup.each(function() {
		popupPosition = $(this).offset();
		popupWidth = $(this).width();
		popupHeight = $(this).height();
	});

	if (popupId!=null &&
			popupPosition!=null && 
			popupWidth!=null && popupHeight!=null)
	{
		if (event.pageX<popupPosition.left+1 || event.pageX>popupPosition.left+popupWidth-1 ||
			event.pageY<popupPosition.top+1 || event.pageY>popupPosition.top+popupHeight-1)
		{
			if (!popup.hasClass('ui-resizable-resizing'))
			{
				return true;
			}
		}
	}
	
	return false;
}