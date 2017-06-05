<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="html"
		doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"
		doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"/>
	
	<xsl:template match="/">
		<html>
			<head>
				<title>DICOM dataset</title>
				<link rel="stylesheet" type="text/css" href="arr-style.css"/>
			</head>
			<body>
				<table class="dcmds">
					<xsl:apply-templates select="dataset"/>
				</table>
			</body>
		</html>
	</xsl:template>
	
	<xsl:template match="dataset">
        <table class="dcmds">
            <tr class="head"><td>Group</td><td>Element</td><td>Name</td><td>VR</td>
                <td>Length</td><td>VM</td><td>Value</td></tr>
                <xsl:apply-templates select="elm"/>
        </table>
	</xsl:template>
    
    <xsl:template match="seq">
        <td><xsl:value-of select="@len"/></td><td> -- </td>
        <td>
            <table class="dcmds">
                <tr class="head"><td><i>Items</i></td></tr>
                <xsl:apply-templates select="item"/>
            </table>
        </td>
    </xsl:template>
    
    <xsl:template match="item">
        <tr>
            <td>
                <table class="dcmds">
                    <tr class="head"><td>Group</td><td>Element</td><td>Name</td><td>VR</td>
                        <td>Length</td><td>VM</td><td>Value</td></tr>
                            <xsl:apply-templates select="elm"/>
                </table>
            </td>
        </tr>
    </xsl:template>
	
    <xsl:template match="elm">
        <xsl:variable name="pos" select="(position() mod 2)"/>
        <tr class="row{$pos}">
            <td><xsl:value-of select="substring(@tag,1,4)"/></td><td><xsl:value-of select="substring(@tag,5,4)"/></td>
            <td><xsl:value-of select="@name"/></td><td><xsl:value-of select="@vr"/></td>
            <xsl:apply-templates select="seq"/>
            <xsl:apply-templates select="val"/>
        </tr>
    </xsl:template>
    
    <xsl:template match="val">
        <td><xsl:value-of select="@len"/></td>
        <td><xsl:value-of select="@vm"/></td>
        <td><xsl:value-of select="@data"/></td>
    </xsl:template>
</xsl:stylesheet>

