<%--
	$Id: login.jsp 3384 2007-06-13 12:27:53Z javawilli $
	$Source$
	
	Autologin for WebViewer:
	To allow external Webviewer access without login you can use request parameter to achieve autologin.
	1) accNr (Accession number) not null! 
	2) loginuser (user name)
	3) passwd4user (password for username in Base64)
	Be aware that this critical infos are visible in browser window!
	Therefore this user should only have WebUser role.
--%>
<%@ page import='java.net.InetAddress' %>
<html>
<head>
  <meta name="viewport" content="initial-scale=1.0"/> 
  <title>Login</title>
  <link href="style.css" rel="stylesheet" type="text/css">
</head>

<%
	String nodeInfo = System.getProperty("dcm4che.archive.nodename", InetAddress.getLocalHost().getHostName() );
    String agent = request.getHeader("USER-AGENT");
%>
<body onload="self.focus();document.login.j_username.focus()">
<% boolean isMobile = (agent.indexOf("Mobile")>=0 || agent.indexOf("BlackBerry")>=0);
   if( !isMobile ) { %>
<table border="0" cellspacing="0" cellpadding="0" width="100%">
 <tr>
  <td><img src="/dcm4chee-web/white48.jpg" width="100%" height="5px"></td>
 </tr>
 <tr>
  <td background="/dcm4chee-web/white48.jpg">
    <img src="/dcm4chee-web/white48.jpg" width="10px" height="24px"><img src="/dcm4chee-web/logo.gif" alt="DCM4CHEE"> Xero
  </td>
 </tr>
 <tr>
  <td><img src="/dcm4chee-web/line.jpg" width="100%" height="20px" alt="line"></td>
 </tr>
</table>
<% } %>
<center>
<h1>User Login at <%= nodeInfo %></h1>
<br>

<form name="login" method="POST" action="j_security_check">
<p>This is a Stringtemplate based version of Xero that is replacing the Xslt based Xero. 
The GUI is different in appearance and is being worked on for useability and appearance.
If you want the version with W/L etc support, try <a href="/xslt">XSLT Xero</a></p>
<table>
	<tr valign="middle">
	  <td><div class="text">Name:</div></td>
	  <td><input class="textfield" type="text" name="j_username" value="user"/></td>
	</tr>
	<tr valign="middle">
	  <td><div class="text">Password:</div></td>
	  <td><input class="textfield" type="password" name="j_password" value="user"/></td>
	</tr>
	<tr><td>&nbsp;</td></tr>
	<tr valign="middle">
	  <td>&nbsp;</td>
	  <td align="center"><input class="button" type="submit" value="Log in"></td>
	</tr>
</table>
</center>
<% if( !isMobile ) { %>
<a href="http://www.dcm4che.org/confluence/display/ee2/Xero">Xero WIKI</a><br />
<a href="http://www.dcm4che.org/jira/browse/XER">Xero Bug Reports</a><br />
<a href="http://sourceforge.net/project/showfiles.php?group_id=37982&package_id=236929">Xero Download</a><br />
<h2>Known Issues</h2>
<ul>
<li>This version has no interactive zoom/pan or CINE, or markup/text annotations</li>
<li>This version does support IE 6,7 Firefox 2,3  Opera 9   Safari 3</li>
<li>This version has:</li>
<li>Query and multi-study display, unlike the previous version.</li>
<li>GSPS selection/display per-study.</li>
<li>Report viewing (some bugs here still)</li>
<li>IHE Image Consistency tests: DISA, SPAT, VLUT, MLUT, PLUT, XLUT all pass in Firefox (true size works IF your monitor is the same as mine :-)</li>
<li>Missing features from the previous version are being worked on.</li> 
</ul>
<% } %>

</form>
</body>
</html>
