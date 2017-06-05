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