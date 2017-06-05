<?xml version="1.0" encoding="UTF-8"?>

<!-- <?xml version="1.0" encoding="iso-8859-1"?> -->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <xsl:variable name="StudyInstanceUID" select="dataset/attr[@tag='0020000D']"/>

	<xsl:output method="xml" indent="yes" media-type="text/xml" omit-xml-declaration="no"/>

		<!-- the stylesheet processing entry point -->
	<xsl:template match="/">
		<xsl:processing-instruction name="xml-stylesheet">
			<xsl:text>href="xsl/sr_html.xsl" type="text/xsl"</xsl:text>
		</xsl:processing-instruction><xsl:copy-of select="dataset"/>
	</xsl:template>

</xsl:stylesheet>


