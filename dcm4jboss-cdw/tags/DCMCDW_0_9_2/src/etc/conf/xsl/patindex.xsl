<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:redirect="http://xml.apache.org/xalan/redirect" extension-element-prefixes="redirect" version="1.0">
<xsl:include href="styindex.xsl"/>

<xsl:template match="record[@type='PATIENT']" mode="index">
	<xsl:param name="pagetitle"/>
	<xsl:variable name="patdir" select="substring(record/record/record/attr[@tag='(0004,1500)'],7,9)"/>
	<redirect:write select="concat($patdir,'INDEX.HTM')">
		<html>
			<head>
				<title>
					<xsl:value-of select="$pagetitle"/>
				</title>
				<link rel="stylesheet" href="../STYLE.CSS" type="text/css"/>
			</head>
			<body background="../BACKGRND.JPG" link="#FF0000" alink="#FF0000" vlink="#FF0000">
				<table width="100%">
					<tr>
						<td><a href="../HOME.HTM"><b>Home</b></a>&#160;&gt;&#160;<b>Patient</b><br/><a href="../INDEX.HTM" target="_top">FRAMES</a>&#160;&#160;<a href="INDEX.HTM" target="_top">NO FRAMES</a></td>
						<td align="right">
							<a href="http://www.tiani.com" target="_blank">
								<img src="../TIANI.GIF" border="0"/>
							</a>
						</td>
					</tr>
				</table>
				<table border="0" cellspacing="1" cellpadding="1">
					<tr align="left">
						<th>Patient Name</th>
						<th>ID</th>
						<th>Birth Date</th>
						<th>Sex</th>
						<th>Studies</th>
						<th>Files</th>
					</tr>
					<tr>
						<td>
							<xsl:call-template name="formatPN">
								<xsl:with-param name="pn" select="attr[@tag='(0010,0010)']"/>
							</xsl:call-template>
						</td>
						<td>
							<xsl:value-of select="attr[@tag='(0010,0020)']"/>
						</td>
						<td>
							<xsl:call-template name="formatDate">
								<xsl:with-param name="date" select="attr[@tag='(0010,0030)']"/>
							</xsl:call-template>
						</td>
						<td align="center">
							<xsl:value-of select="attr[@tag='(0010,0040)']"/>
						</td>
						<td align="center">
							<xsl:value-of select="count(record)"/>
						</td>
						<td align="center">
							<xsl:value-of select="count(record/record/record)"/>
						</td>
					</tr>
				</table>
				<table border="0" cellspacing="1" cellpadding="1">
					<tr align="left">
						<th>Study Date/Time</th>
						<th>ID</th>
						<th>Description</th>
						<th>Ref.Physican</th>
						<th>Series</th>
						<th>Files</th>
					</tr>
					<xsl:for-each select="record[@type='STUDY']">
						<xsl:sort data-type="text" order="ascending" select="attr[@tag='(0010,0010)']"/>
						<xsl:variable name="stydir" select="substring(record/record/attr[@tag='(0004,1500)'],16,9)"/>
						<tr>
							<td>
								<a href="{concat($stydir,'INDEX.HTM')}">
									<xsl:call-template name="formatDateTime">
										<xsl:with-param name="date" select="attr[@tag='(0008,0020)']"/>
										<xsl:with-param name="time" select="attr[@tag='(0008,0030)']"/>
									</xsl:call-template>
								</a>
							</td>
							<td>
								<xsl:value-of select="attr[@tag='(0020,0010)']"/>
							</td>
							<td>
								<xsl:value-of select="attr[@tag='(0008,1030)']"/>
							</td>
							<td>
								<xsl:call-template name="formatPN">
									<xsl:with-param name="pn" select="attr[@tag='(0008,0090)']"/>
								</xsl:call-template>
							</td>
							<td align="center">
								<xsl:value-of select="count(record)"/>
							</td>
							<td align="center">
								<xsl:value-of select="count(record/record)"/>
							</td>
						</tr>
					</xsl:for-each>
				</table>
			</body>
		</html>
	</redirect:write>
	<xsl:apply-templates select="record[@type='STUDY']" mode="index">
		<xsl:with-param name="pagetitle" select="$pagetitle"/>
	</xsl:apply-templates>
</xsl:template>

</xsl:stylesheet>
