<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:redirect="http://xml.apache.org/xalan/redirect" extension-element-prefixes="redirect" version="1.0">
<xsl:include href="stytoc.xsl"/>

<xsl:template match="dicomdir" mode="toc">
	<redirect:write select="'TOC.HTM'">
		<html>
			<head>
				<title>Patients</title>
				<link rel="stylesheet" href="STYLE.CSS" type="text/css"/>
			</head>
			<body link="#FF0000" alink="#FF0000" vlink="#FF0000">
				<table>
					<tr>
						<td nowrap="nowrap">
							Patients:<br/>
							<xsl:for-each select="record[@type='PATIENT']">
								<xsl:sort data-type="text" order="ascending" select="attr[@tag='(0010,0010)']"/>
								<xsl:variable name="patdir" select="substring(record/record/record/attr[@tag='(0004,1500)'],7,9)"/>
								<a href="{concat($patdir,'TOC.HTM')}" target="stylist">
									<xsl:call-template name="formatPN">
										<xsl:with-param name="pn" select="attr[@tag='(0010,0010)']"/>
									</xsl:call-template>
								</a><br/>
							</xsl:for-each>
						</td>
					</tr>
				</table>
			</body>
		</html>
	</redirect:write>
	<xsl:apply-templates select="record[@type='PATIENT']" mode="toc"/>
</xsl:template>

</xsl:stylesheet>
