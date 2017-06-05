<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:internal="urn:my-internal-data">
<xsl:output method="html" indent="yes" encoding="ISO-8859-1"/>

   <xsl:template match="/">
	 <html>
	    <body>
	    	<center>
	    		<b>You have logged out from DCM Folder:</b> 
	    		<p>
	    		<a href="foldersubmit.m?filter=">Login</a>
	    		</p>
	    	</center>
		</body>
	 </html>
   </xsl:template>

   
</xsl:stylesheet>

