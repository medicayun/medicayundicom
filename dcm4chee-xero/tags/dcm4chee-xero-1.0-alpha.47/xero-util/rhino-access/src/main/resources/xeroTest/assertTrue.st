$! Defines an assertion that is inline and can record the assertion results etc !$
if(!($condition$)) {
	var message = "";

	$if(msg)$
		message += "$msg;format="js"$  ";
	$endif$
	
	message += "Expected $condition;format="js"$ to be true."
	
	throw new java.lang.AssertionError(message);	
}