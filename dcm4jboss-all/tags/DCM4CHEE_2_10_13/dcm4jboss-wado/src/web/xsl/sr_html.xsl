<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  
	<xsl:output method="html" indent="yes" media-type="text/html" encoding="UTF-8"/>
  <xsl:param name="wadoURL" select="'wado'"/>

		<!-- the stylesheet processing entry point -->
	<xsl:template match="/">
	  <xsl:apply-templates select="dataset"/>
	</xsl:template>

	<xsl:template match="dataset">
		<html>
		<head>
			<title>
			<xsl:value-of select="attr[@tag='0040A043']/item/attr[@tag='00080104']"/>
			</title>
		</head>
		<body>
		<font size="-1">
		By <xsl:value-of select="attr[@tag='00080080']"/>, Ref. Phys. <xsl:value-of select="attr[@tag='00080090']"/>
		</font>
		<br/>
		<table border="0">
		<tr><td>Patient Name:</td><td><xsl:value-of select="attr[@tag='00100010']"/>
</td></tr>
		<tr><td>Patient ID:</td><td><xsl:value-of select="attr[@tag='00100020']"/>
</td></tr>
		<tr><td>Patient Birthdate:</td><td><xsl:value-of select="attr[@tag='00100030']"/>
</td></tr>
		<tr><td>Patient Sex:</td><td><xsl:value-of select="attr[@tag='00100040']"/>
</td></tr>
		</table>
<hr/>
    
	  <xsl:apply-templates select="attr[@tag='0040A730']/item" mode="content"/>
	
		</body>
    </html>
	</xsl:template>

<!--
  Contentsequence output starts here
-->
	<xsl:template match="item" mode="content">

	<font size="+1"><xsl:value-of select="attr[@tag='0040A043']/item/attr[@tag='00080104']"/>
</font>
	<ul>
	<li>
	<xsl:choose>
	  <xsl:when test="attr[@tag='0040A040']='TEXT'">
		<xsl:value-of select="attr[@tag='0040A160']"/>
		</xsl:when>
	  
	  <xsl:when test="attr[@tag='0040A040']='IMAGE '">
		  <xsl:apply-templates select="attr[@tag='00081199']/item" mode="imageref"/>
		</xsl:when>
		
	  <xsl:when test="attr[@tag='0040A040']='CODE'">
		<xsl:value-of select="attr[@tag='0040A168']/item/attr[@tag='00080104']"/>
		</xsl:when>		

	  <xsl:when test="attr[@tag='0040A040']='PNAME '">
		<xsl:value-of select="attr[@tag='0040A123']"/>
		</xsl:when>		

	  <xsl:when test="attr[@tag='0040A040']='NUM '">
		<i>
		<xsl:value-of select="attr[@tag='0040A043']/item/attr[@tag='00080104']"/> Measurement not supported yet</i>
		</xsl:when>		
				
 
	  <xsl:when test="attr[@tag='0040A040']='CONTAINER '">
    <xsl:apply-templates select="attr[@tag='0040A730']/item" mode="content"/>
		</xsl:when>
		
	  <xsl:otherwise>
		<i><xsl:value-of select="attr[@tag='0040A040']"/> (Value Type not supported yet)</i>
	  </xsl:otherwise>
	</xsl:choose>
  </li>
	</ul>
	
	</xsl:template>
	
	<xsl:template match="item" mode="imageref">
		Image 
		<img>
			<xsl:attribute name="src"><xsl:value-of select="$wadoURL"/>?requestType=WADO&amp;studyUID=1&amp;seriesUID=1&amp;objectUID=<xsl:value-of select="attr[@tag='00081155']"/></xsl:attribute>
		</img>
<br/>
	</xsl:template>
	
</xsl:stylesheet>


