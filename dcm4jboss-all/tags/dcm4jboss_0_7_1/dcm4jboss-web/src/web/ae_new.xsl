<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
<!ENTITY nbsp "&#160;">
]>
<xsl:stylesheet version="1.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:internal="urn:my-internal-data">

   <xsl:variable name="page_title">New AE</xsl:variable>
   <xsl:include href  = "page.xsl" />

   <xsl:template match="model/AE">
			<form action="aenewsubmit.m" method="post">
			<table border="1" cellspacing="0" cellpadding="0" width="100%"><tr><td>
				<table border="0">
					<tr>
						<td bgcolor="#eeeeee">AE Title</td>
				        <td title="AE Title" >
			                <input size="25" name="title" type="text" value=""/>
						</td>
					</tr>
					<tr>
						<td bgcolor="#eeeeee">Hostname</td>
				        <td title="Hostname" >
			                <input size="25" name="hostName" type="text" value=""/>
				        </td>
					</tr>
					<tr>				        
						<td bgcolor="#eeeeee">Port</td>														
				        <td title="Port">
			                <input size="25" name="port" type="text" value=""/>
				        </td>
					</tr>
				                <input name="cipherSuites" type="hidden" value=""/>
					<!--tr>
						<td bgcolor="#eeeeee">Cipher Suites</td>
				        <td title="Cipher Suites">
				                <input size="35" name="cipherSuites" type="text" value=""/>
				        </td>
					</tr-->
					<tr>
						<td>
							  <input type="submit" name="new" value="Create"/>
			                  <input type="submit" name="cancel" value="Cancel" />						
			             </td>
					</tr>
				</table>
			</td></tr></table>
			</form>
   </xsl:template>
</xsl:stylesheet>


