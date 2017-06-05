<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="xml" indent="yes"/>
    <xsl:include href="common.xsl"/>
    <xsl:template match="/hl7">
        <dataset>
            <xsl:apply-templates select="MRG"/>
        </dataset>
    </xsl:template>    
    <xsl:template match="MRG">
        <xsl:call-template name="patName">
            <xsl:with-param name="xpn" select="field[7]"/>
        </xsl:call-template>
        <xsl:call-template name="patID">
            <xsl:with-param name="cx" select="field[1]"/>
        </xsl:call-template>
    </xsl:template>
</xsl:stylesheet>
