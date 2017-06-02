<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:include href="common_dcm2hl7.xsl"/>
    <xsl:template match="/dicom">
        <xsl:call-template name="MSH">
            <xsl:with-param name="msgType" select="'ADT^A40'"/>
        </xsl:call-template>
        <xsl:call-template name="EVN" />
	<xsl:apply-templates select="/dicom" mode="dcm2PID" />
    </xsl:template>
</xsl:stylesheet>