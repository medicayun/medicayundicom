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
	 <html><body background="images/bg.jpg" cellpadding="0" cellspacing="0" border="0">
	 	<center><form action="aenewsubmit.m" method="post">
			<table border="0" cellspacing="0" cellpadding="0" width="35%"><tr><td>
				<center><table border="0">
					<tr>
						<td width="50">AE Title</td>
				        <td title="AE Title" >
			                <input size="25" name="title" type="text" value=""/>
						</td>
					</tr>
					<tr>
						<td width="50">Hostname</td>
				        <td title="Hostname" >
			                <input size="25" name="hostName" type="text" value=""/>
				        </td>
					</tr>
					<tr>				        
						<td width="50">Port</td>														
				        <td title="Port">
			                <input size="25" name="port" type="text" value=""/>
				        </td>
					</tr>
				  <!--              <input name="cipherSuites" type="hidden" value=""/>
					tr>
						<td bgcolor="#eeeeee">Cipher Suites</td>
				        <td title="Cipher Suites">
				                <input size="35" name="cipherSuites" type="text" value=""/>
				        </td>
					</tr-->
					<tr>
						<td colspan="2">
									<center><input type="submit" name="new" value="Create"/>
			                  <input type="submit" name="cancel" value="Cancel" /></center>
			             </td>
					</tr>
				</table>
</center>
			</td></tr></table>
			</form>
</center>
			</body></html>
   </xsl:template>
</xsl:stylesheet>


