<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template match="/">
			<html>
			<head>
				<title><xsl:copy-of select="$page_title" /></title>
				<script language = "JavaScript" src= "dcm4che.js"/>
				<link rel="stylesheet" href="stylesheet.css" type="text/css"/>
			</head>
			<body>
				<table cellspacing="0" cellpadding="2" bgcolor="#eeeeee"  width="100%" border="1">
					<tr align="left" style="text-align: center">
						<td> <a href="default.jsp">Folder</a> </td>
						<td> <a href="ae.m">AE Management</a> </td>
					</tr>
				</table>			
			  <xsl:apply-templates select="model"/>
		  </body>
		</html>
	</xsl:template>
</xsl:stylesheet>
	