function validateChecks(check,check_type,total)  //checks if at least <<total>> checks have been checked
{
	var checked_num=0;
	for (var i = 0; i < check.length; i++)
	{
		if (check[i].checked)
			checked_num++;
	}
	
	if (checked_num >= total)
		return true;
	
	var word = "checkbox";
	if (total>1)
		word = "checkboxes";
		
	alert('Please check at least ' + total + ' (' + check_type + ') ' + word);
	return false;
}

function validateRadios(radios, radio_type)  //checks if a radio button have been selected
{
	for (var i = 0; i < radios.length; i++)
	{
		if (radios[i].checked)
			return true;
	}
	alert('Please select a ' + radio_type);
	return false;
}

function checkNotEmpty( field,field_type ) //checks if a field value is not null and not empty
{
	if ( field.length < 1 ) {
		alert('Field ' + field_type + ' is empty!' );
		return false;
	}
	return true;
}

function checkError( errCode )
{
	if ( errCode != 'OK' && errCode != '' ) {
		var msg = 'Error: ';
    	if ( errCode == 'moveError' ) 
    		msg = msg + 'Unexpected error during move operation!'
    	else if ( errCode == 'parseError_date' ) 
    		msg = msg + ' Wrong date format! Use yyyy/mm/dd.'
    	else if ( errCode == 'parseError_time' ) 
    		msg = msg + ' Wrong time format! Use hh:mm:ss.'
    	else if ( errCode == 'parseError_datetime' ) 
    		msg = msg + ' Wrong date/time format! Use yyyy/mm/dd hh:mm:ss.'
    	else if ( errCode == 'moveError_noSelection' ) 
    		msg = msg + ' Nothing selected! Please select a destination and one or more sources.'
    	else if ( errCode == 'moveError_toManyDest' ) 
    		msg = msg + 'Please select only one destination!'
    	else if ( errCode == 'moveError_noSource' ) 
    		msg = msg + 'Please select at least one source!'
    	else if ( errCode == 'moveError_unselectSeries' ) 
    		msg = msg + 'Please check that all series and instances are unselected.'
    	else if ( errCode == 'moveError_unselectInstances' ) 
    		msg = msg + 'Please check that all instances are unselected.'
    	else if ( errCode == 'moveError_samePatient' ) 
    		msg = msg + 'Move studies to the same patient is not usefull.'
    	else if ( errCode == 'moveError_sameStudy' ) 
    		msg = msg + 'Move series to the same study is not usefull.'
    	else if ( errCode == 'moveError_sameSeries' ) 
    		msg = msg + 'Move instances to the same series is not usefull.'
    	else if ( errCode == 'moveError_diffPatient' ) 
    		msg = msg + 'Not allowed! move series is only allowed between studies of the same patient.'
    	else if ( errCode == 'moveError_diffStudy' ) 
    		msg = msg + 'Not allowed! move instances is only allowed between series of the same study.'
    	else if ( errCode == 'moveError_diffStudyParent' ) 
    		msg = msg + 'Not allowed! You can only move studies from one patient to another patient.'
    	else if ( errCode == 'moveError_diffSeriesParent' ) 
    		msg = msg + 'Not allowed! You can only move series from one study to another study.'
    	else if ( errCode == 'moveError_diffInstanceParent' ) 
    		msg = msg + 'Not allowed! You can only move instances from one series to another series.'
    	else if ( errCode == 'MEDIA_DELETE_FAILED' ) 
    		msg = msg + 'Delete media failed!'
    	else if ( errCode == 'deleteError_mwlEntry' ) 
    		msg = msg + 'Delete worklist entry failed!'
		alert(msg);
	}
}

function checkPopup( popupMsg )
{
	if ( popupMsg != '' ) {
		alert( popupMsg );
	}
}

function selectCipher()
{
	selection = document.ae_edit.cipherSelect.options[document.ae_edit.cipherSelect.selectedIndex ].value;
	if ( selection != '--' ) {
		document.ae_edit.cipherSuites.value=document.ae_edit.cipherSelect.options[document.ae_edit.cipherSelect.selectedIndex ].value;
	}
}

function doEchoAET(aet)  //open echo window
{
	newwindow = window.open('aeecho.m?aet='+aet, 'ECHO', 'toolbar=no,menubar=no, scrollbars=no, width=500,height=200');
	newwindow.focus();
	return false;
}

function doEcho(form)  //open echo window
{
	var query = 'title='+form.title.value+'&hostName='+form.hostName.value+'&port='+form.port.value;
	query = query + '&cipher1='+selectValue(form.cipher1)+'&cipher2='+selectValue(form.cipher2)+'&cipher3='+selectValue(form.cipher3)
	newwindow = window.open('aeecho.m?'+query, 'ECHO', 'toolbar=no,menubar=no, scrollbars=no, width=500,height=200');
	newwindow.focus();
	return false;
}

function selectValue(sObj) {
    with (sObj) return options[selectedIndex].value;
  }
  
function openWin( winName, url ) {
//alert('open new window '+winName+' with URL:'+url);
	newwindow = window.open(url, winName, 'toolbar=yes,menubar=yes,scrollbars=yes');
	newwindow.focus();
	return false;
}  
  