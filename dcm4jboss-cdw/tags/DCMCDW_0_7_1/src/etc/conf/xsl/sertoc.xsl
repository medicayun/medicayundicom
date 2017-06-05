<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:redirect="http://xml.apache.org/xalan/redirect" extension-element-prefixes="redirect" version="1.0">

<xsl:template match="record[@type='STUDY']" mode="toc">
	<xsl:variable name="stydir" select="substring(record/record/attr[@tag='(0004,1500)'],7,18)"/>
	<redirect:write select="concat($stydir,'TOC.HTM')">
		<html>
			<head>
				<title>Series</title>
				<link rel="stylesheet" href="../../STYLE.CSS" type="text/css"/>
			</head>
			<body link="#FF0000" alink="#FF0000" vlink="#FF0000">
				<table>
					<tr>
						<td nowrap="nowrap"><a href="INDEX.HTM" target="view">
							<xsl:call-template name="formatDateTime">
								<xsl:with-param name="date" select="attr[@tag='(0008,0020)']"/>
								<xsl:with-param name="time" select="attr[@tag='(0008,0030)']"/>
							</xsl:call-template></a><br/>
							Series:<br/>
							<xsl:for-each select="record[@type='SERIES']">
								<xsl:sort data-type="text" order="ascending" select="attr[@tag='(0008,0060)']"/>
								<xsl:sort data-type="number" order="ascending" select="attr[@tag='(0020,0011)']"/>
								<xsl:variable name="serdir" select="substring(record/attr[@tag='(0004,1500)'],25,9)"/>
								<a href="{concat($serdir,'INDEX.HTM')}" target="view">
									<xsl:value-of select="attr[@tag='(0008,0060)']"/>
									<xsl:text>-</xsl:text><xsl:value-of select="attr[@tag='(0020,0011)']"/>
								</a><br/>
							</xsl:for-each>
						</td>
					</tr>
				</table>
			</body>
		</html>
	</redirect:write>
</xsl:template>

</xsl:stylesheet>
