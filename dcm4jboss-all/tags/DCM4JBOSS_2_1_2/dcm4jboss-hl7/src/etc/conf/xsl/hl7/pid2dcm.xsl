<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="xml" indent="yes"/>
    <xsl:include href="common.xsl"/>
    <xsl:template match="/hl7">
        <dataset>
            <xsl:apply-templates select="PID"/>
        </dataset>
    </xsl:template>
    <xsl:template match="PID">
        <xsl:call-template name="patName">
            <xsl:with-param name="xpn" select="field[5]"/>
        </xsl:call-template>
        <xsl:call-template name="patID">
            <xsl:with-param name="cx" select="field[3]"/>
        </xsl:call-template>
        <xsl:call-template name="patBirthDate">
            <xsl:with-param name="ts" select="field[7]"/>
        </xsl:call-template>
        <xsl:call-template name="patSex">
            <xsl:with-param name="is" select="field[8]"/>
        </xsl:call-template>
    </xsl:template>
</xsl:stylesheet>
