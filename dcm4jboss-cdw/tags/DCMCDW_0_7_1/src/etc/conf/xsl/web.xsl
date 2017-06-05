<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:redirect="http://xml.apache.org/xalan/redirect" extension-element-prefixes="redirect" version="1.0">
<xsl:output method="xml" encoding="ISO-8859-1" indent="yes" doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN" doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"/>
<xsl:include href="common.xsl"/>
<xsl:include href="pattoc.xsl"/>
<xsl:include href="home.xsl"/>

<xsl:template match="/">
	<xsl:variable name="pagetitle">
	DICOM Media <xsl:value-of select="dicomdir/attr[@tag='(0004,1130)']"/>
</xsl:variable>
	<html>
		<head>
			<title>
				<xsl:value-of select="$pagetitle"/>
			</title>
		</head>
		<frameset cols="20%,80%">
			<frameset rows="35%,35%,30%">
				<frame src="TOC.HTM" name="patlist"/>
				<frame src="SELECT.HTM" name="stylist"/>
				<frame src="SELECT.HTM" name="serlist"/>
			</frameset>
			<frame src="HOME.HTM" name="view"/>
		</frameset>
	</html>
	<xsl:apply-templates select="dicomdir" mode="toc"/>
	<xsl:apply-templates select="dicomdir" mode="home">
		<xsl:with-param name="pagetitle" select="$pagetitle"/>
	</xsl:apply-templates>
</xsl:template>

</xsl:stylesheet>
