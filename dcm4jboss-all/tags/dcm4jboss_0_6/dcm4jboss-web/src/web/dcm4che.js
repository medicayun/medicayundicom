function validateChecks(check,check_type,total)  //checks if at least <<total>> check have been checked
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