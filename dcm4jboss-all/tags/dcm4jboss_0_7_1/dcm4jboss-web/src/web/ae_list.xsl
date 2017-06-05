<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
<!ENTITY nbsp "&#160;">
]>
<xsl:stylesheet version="1.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:internal="urn:my-internal-data">

   <xsl:variable name="page_title">AE List</xsl:variable>
   <xsl:include href  = "page.xsl" />

   <xsl:template match="model">
			<table border="1" cellspacing="0" cellpadding="0" width="100%"><tr><td>
				<table border="0">
					<tr>
							<td>AE Title</td>
							<td>Hostname</td>
							<td>Port</td>														
							<!--td>Cipher Suites</td-->
							<td>&nbsp;</td>
					</tr>
					<xsl:apply-templates select="AEs/item"/>
					<tr>
						<td>
							  <a href="aenew.m">Add New AE</a>						
						</td>
					</tr>
				</table>
			</td></tr></table>
   </xsl:template>

	<xsl:template match="item[@type='org.dcm4chex.archive.ejb.jdbc.AEData']">
		<tr bgcolor="#eeeeee">
	        <td title="AE Title" >
				<xsl:value-of select="title"/>
			</td>
	        <td title="Hostname" >
				<xsl:value-of select="hostName"/>
	        </td>
	        <td title="Port">
					<xsl:value-of select="port"/>
	        </td>
	        <!--td title="Cipher Suites">
					<xsl:value-of select="cipherSuites"/>
	        </td-->
			<td>
					<a href="aeedit.m?pk={pk}">Edit</a>
	        </td>
			<td>
					<a href="aedelete.m?pk={pk}" onclick="return confirm('Are you sure you want to delete?')">Delete</a>					
			</td>
		</tr>
	</xsl:template>
	   
</xsl:stylesheet>

