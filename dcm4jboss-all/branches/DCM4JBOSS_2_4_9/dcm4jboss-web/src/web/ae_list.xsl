<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:internal="urn:my-internal-data">

<xsl:output method="html" indent="yes" encoding="ISO-8859-1"/>
<xsl:variable name="page_title">AE List</xsl:variable>
<xsl:include href="page.xsl"/>
<xsl:template match="model">

		<table width="70%" border="0" bordercolor="#ffffff" cellspacing="5" cellpadding="0">
		<tr>	<center>
			<td>
				<tr>
					<td width="25%"><h2>AE Title</h2></td>
					<td width="20%"><h2>Hostname</h2></td>
					<td width="15%"><h2>Port</h2></td>	
					<td width="15%"><h2>Cipher</h2></td>	
					<td colspan="2" width="10%" align="center"><a href="aenew.m"><img src="images/add_aet.gif" alt="add new AET" border="0"/></a></td>
				</tr>
					<xsl:apply-templates select="AEs/item">
						<xsl:sort data-type="text" order="ascending" select="title"/>
					</xsl:apply-templates>
			</td>	</center>
		</tr>
		</table>


</xsl:template>

	<xsl:template match="item[@type='org.dcm4chex.archive.ejb.jdbc.AEData']">
		<tr>
	        <td title="AE Title" valign="top" >
				<xsl:value-of select="title"/>
			</td>
	        <td title="Hostname" valign="top" >
				<xsl:value-of select="hostName"/>
	        </td>
	        <td title="Port" valign="top" >
					<xsl:value-of select="port"/>
	        </td>
	        <td title="Cipher" valign="top" >
	        	<xsl:for-each select="cipherSuites/item">
	        		<xsl:value-of select="."/><br/>
				</xsl:for-each>
	        </td>
			<td align="center" valign="top" >
				<a href="aeedit.m?pk={pk}">
					<img src="images/edit.gif" alt="edit" border="0"/>		
				</a>
	        </td>
			<td align="left" valign="top" >
					<a href="aedelete.m?pk={pk}" onclick="return confirm('Are you sure you want to delete?')">
					<img src="images/delete.gif" alt="delete" border="0"/>							
					</a>					
			</td>
			<td align="left" valign="top" >
					<a href="aeecho.m?aet={title}" >
					<xsl:attribute name="onclick" >return doEchoAET('<xsl:value-of select="title"/>')</xsl:attribute>
					<img src="images/echo.gif" alt="Check AET({title}) with echo." border="0"/>							
					</a>					
			</td>
		</tr>
	</xsl:template>
 
</xsl:stylesheet>


