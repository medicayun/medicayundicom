<%--
	$Id: error.jsp 1875 2005-08-01 10:17:13Z javawilli $
	$Source$
--%>
<html>
<head>
	<title>FAILED: DCM Folder Login</title>
	<h1>DCM Folder Login failed!</h1>
</head>
<body>

	<form method="POST" action="j_security_check">
		<table width="100" border="0" cellpadding="2" cellspacing="2">
	  		<tr valign="middle" bgcolor="#eeeeee" style="center">
	  			<td>Name:</td>
	   			<td><input type="text" name="j_username"></td>
	   		</tr>
	  		<tr valign="middle" bgcolor="#eeeeee" style="center">
	  			<td>Password:</td>
	   			<td><input type="password" name="j_password"></td>
	   		</tr>
	  		<tr valign="middle" bgcolor="#eeeeee" style="center">
	   			<td colspan="2" align="center"><input type="submit" value="Log in"></td>
	   		</tr>
	</form>
</body>
</html>