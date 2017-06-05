<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:template name="patID">
        <xsl:param name="cx"/>
        <attr tag="00100020" vr="LO">
            <xsl:value-of select="$cx/text()"/>
        </attr>
        <attr tag="00100021" vr="LO">
            <xsl:value-of select="$cx/component[3]"/>
        </attr>
    </xsl:template>
    <xsl:template name="patName">
        <xsl:param name="xpn"/>
        <xsl:if test="$xpn/text()">
            <attr tag="00100010" vr="PN">
                <xsl:if test="$xpn != '&quot;&quot;'">
                    <xsl:call-template name="xpn2pn">
                        <xsl:with-param name="xpn" select="$xpn"/>
                    </xsl:call-template>
                </xsl:if>
            </attr>
        </xsl:if>
    </xsl:template>
    <xsl:template name="patBirthDate">
        <xsl:param name="ts"/>
        <xsl:if test="$ts/text()">
            <attr tag="00100030" vr="DA">
                <xsl:if test="$ts != '&quot;&quot;'">
                    <xsl:value-of select="substring($ts,1,8)"/>
                </xsl:if>
            </attr>
        </xsl:if>
    </xsl:template>
    <xsl:template name="patSex">
        <xsl:param name="is"/>
        <xsl:if test="$is/text()">
            <attr tag="00100040" vr="CS">
                <xsl:if test="$is != '&quot;&quot;'">
                    <xsl:value-of select="$is"/>
                </xsl:if>
            </attr>
        </xsl:if>
    </xsl:template>
    <xsl:template name="xpn2pn">
        <xsl:param name="xpn"/>
        <xsl:value-of select="$xpn/text()"/>
        <xsl:variable name="compCount" select="count($xpn/component)"/>
        <xsl:if test="$compCount &gt; 0">
            <xsl:text>^</xsl:text>
            <xsl:value-of select="$xpn/component[1]"/>
            <xsl:if test="$compCount &gt; 1">
                <xsl:text>^</xsl:text>
                <xsl:value-of select="$xpn/component[2]"/>
                <xsl:if test="$compCount &gt; 2">
                    <xsl:text>^</xsl:text>
                    <xsl:value-of select="$xpn/component[4]"/>
                    <xsl:text>^</xsl:text>
                    <xsl:value-of select="$xpn/component[3]"/>
                </xsl:if>
            </xsl:if>
        </xsl:if>
    </xsl:template>
</xsl:stylesheet>
