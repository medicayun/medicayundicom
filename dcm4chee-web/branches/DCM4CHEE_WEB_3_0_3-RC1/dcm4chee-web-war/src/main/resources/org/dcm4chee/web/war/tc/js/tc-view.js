function createTCViewDialog(settings) {
	try
	{
		var w = Wicket.Window.create(settings);
		w.resizing = function() {
			updateTCViewDialogLayout();
		};
		return w;
	}
	catch (e)
	{
		console.log(e);
	}
}

function updateTCViewDialog() {
	updateTCViewDialogToUseQueryUI();
	updateTCViewDialogLayout();
}

function updateTCViewDialogToUseQueryUI() {
	$('#tc-view-content').tabs({
		show: function(event, ui) {
			updateTCViewTabsLayout();
			checkToLoadThumbnails();
		}
	});
	$('#tc-view-editable-content').tabs({
		show: function(event, ui) {
			updateTCViewTabsLayout();
			checkToLoadThumbnails();
		}
	});
	$('#tc-view-images-tab-container').parent().addClass("tc-view-images-tab-container-parent");
	$('#tc-view button').each(function() {
		if (!$(this).hasClass('tc-keyword-chooser-btn'))
		{
			$(this).button();
		}
	});
}

function disableTCViewTabs(indices)
{
	$('#tc-view-content').tabs('option','disabled',indices);
	$('#tc-view-editable-content').tabs('option','disabled',indices);
}

function hideTCViewTab(index)
{
	$('#tc-view-content').tabs('remove', index);
	$('#tc-view-editable-content').tabs('remove', index);
}

function updateTCViewDialogLayout() {
	updateTCViewTabsLayout();
}

function updateTCViewTabsLayout() {
	//adapt height of individual tabs
	$('#tc-view-content').find('div[id^="tabs"]').each(function(index) {
		$(this).height(
				$('#tc-view-content').height()-
				$('#tc-view-content > ul').outerHeight()-
				($(this).outerHeight()-$(this).height()));
	});
	$('#tc-view-editable-content').find('div[id^="tabs"]').each(function(index) {
		$(this).height(
				$('#tc-view-editable-content').height()-
				$('#tc-view-editable-content > ul').outerHeight()-
				($(this).outerHeight()-$(this).height()));
	});
	
	//adapt height of individual elements
	$('#tc-view-diagnosis-text').height($('#tc-view-diagnosis-text').parent().innerHeight()-$('#tc-view-diagnosis-confirmed-chkbox').outerHeight()-13);
	$('#tc-view-bibliography-container').outerHeight(
		$('#tc-view-bibliography-container').parent().innerHeight()-
		$('#tc-view-bibliography-header').outerHeight() -
		$('#tc-view-bibliography-hr').outerHeight());
	
	//layout bibliography elements
	/*
	$('#tc-view-bibliography-container').find('.tc-view-bibliography-list').each(function(index) {
		//account for horizontal width of vertical scrollbar
		$(this).outerWidth($(this).parent().innerWidth()-24);
		
		var textarea = $(this).children('textarea');
		var btn = $(this).children('a');
		
		//textarea.outerWidth($(this).innerWidth());
		btn.css('top',textarea.position().top+4);
		btn.css('left',textarea.position().left+textarea.outerWidth()-btn.outerWidth()-4);
	});
	*/
}

function checkToLoadThumbnails()
{
	$('.tc-view-images-thumbnail').lazyload({
		event: "scrollstop",
		//effect: "fadeIn",
		threshold: "500", //px
		container: $('#tc-view-images-thumbnail-container')
	});
}

function checkToScrollToThumbnail(img)
{
	var container = $('#tc-view-images-thumbnail-container');
	var viewLeft = 0; //container.scrollTop();
	var viewRight = container.width()+viewLeft;
	var imgLeft = img.position().left;
	var imgRight = img.width() + imgLeft;
	var left = container.scrollLeft();
	
	if (imgLeft<viewLeft)
	{
		container.animate({scrollLeft:left+imgLeft-1});
	}
	else if (imgRight>viewRight)
	{
		container.animate({scrollLeft:left+imgLeft+img.width()+1-container.width()});
	}
}
