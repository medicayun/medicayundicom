<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template match="/">
			<html>
			<head>
				<title><xsl:copy-of select="$page_title" /></title>
				<script language = "JavaScript" src= "dcm4che.js"/>
				<link rel="stylesheet" href="stylesheet.css" type="text/css"/>
			</head>
			<body onLoad="checkError('{model/errorCode}');checkPopup('{model/popupMsg}')" background="images/bg.jpg" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0" link="#FF0000" alink="#FF0000" vlink="#FF0000">
			<table width="100%" border="0" cellpadding="0" cellspacing="0">
  		<tr valign="middle" bgcolor="#eeeeee" style="center">
    	<td width="50" align="left"><img src="images/logo.jpg" alt="TIANI Medgraph AG" border="0"/></td>
    	<td width="100" align="center"><a href="default.jsp">Folder</a></td>
      <td width="120" align="center"><a href="ae.m">AE Management</a></td>
      <td width="120" align="center"><a href="mcm_console.m">Offline Storage</a></td>
      <td width="120" align="center"><a href="mwl_console.m">Worklist Console</a></td>
      <td width="120" align="center"><a href="mpps_console.m">MPPS Console</a></td>
      <td width="120" align="center"><a href="../dcm4jboss-arr">Audit Repository</a></td>
			<td width="40%"></td>
			  <xsl:apply-templates select="model"/>
	      </tr>
      </table>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>
	
