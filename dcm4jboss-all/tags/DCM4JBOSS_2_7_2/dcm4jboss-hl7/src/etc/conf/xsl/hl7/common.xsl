<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:template name="cx2attrs">
        <xsl:param name="idtag"/>
        <xsl:param name="istag"/>
        <xsl:param name="cx"/>
        <attr tag="{$idtag}" vr="LO">
            <xsl:value-of select="$cx/text()"/>
        </attr>
        <attr tag="{$istag}" vr="LO">
            <xsl:value-of select="$cx/component[3]"/>
        </attr>
    </xsl:template>
    <xsl:template name="ei2attr">
        <xsl:param name="tag"/>
        <xsl:param name="ei"/>
        <attr tag="{$tag}" vr="LO">
            <xsl:value-of select="$ei/text()"/>
            <xsl:text>^</xsl:text>
            <xsl:value-of select="$ei/component[1]"/>
        </attr>
    </xsl:template>
    <xsl:template name="dcmAttrDA">
        <xsl:param name="tag"/>
        <xsl:param name="val"/>
        <xsl:if test="$val">
            <attr tag="{$tag}" vr="DA">
                <xsl:if test="$val != '&quot;&quot;'">
                    <xsl:value-of select="substring($val,1,8)"/>
                </xsl:if>
            </attr>
        </xsl:if>
    </xsl:template>
    <xsl:template name="dcmAttrDATM">
        <xsl:param name="datag"/>
        <xsl:param name="tmtag"/>
        <xsl:param name="val"/>
        <xsl:if test="$val">
            <attr tag="{$datag}" vr="DA">
                <xsl:if test="$val != '&quot;&quot;'">
                    <xsl:value-of select="substring($val,1,8)"/>
                </xsl:if>
            </attr>
            <attr tag="{$tmtag}" vr="TM">
                <xsl:if test="$val != '&quot;&quot;'">
                    <xsl:value-of select="substring($val,9)"/>
                </xsl:if>
            </attr>
        </xsl:if>
    </xsl:template>
    <xsl:template name="dcmAttr">
        <xsl:param name="tag"/>
        <xsl:param name="vr"/>
        <xsl:param name="val"/>
        <xsl:if test="$val">
            <attr tag="{$tag}" vr="{$vr}">
                <xsl:if test="$val != '&quot;&quot;'">
                    <xsl:value-of select="$val"/>
                </xsl:if>
            </attr>
        </xsl:if>
    </xsl:template>
    <xsl:template name="xpn2pnAttr">
        <xsl:param name="tag"/>
        <xsl:param name="xpn"/>
        <xsl:param name="xpn25" select="$xpn/component"/>
        <xsl:call-template name="pnAttr">
            <xsl:with-param name="tag" select="$tag"/>
            <xsl:with-param name="val" select="$xpn/text()"/>
            <xsl:with-param name="fn" select="$xpn/text()"/>
            <xsl:with-param name="gn" select="$xpn25[1]/text()"/>
            <xsl:with-param name="mn" select="$xpn25[2]/text()"/>
            <xsl:with-param name="ns" select="$xpn25[3]/text()"/>
            <xsl:with-param name="np" select="$xpn25[4]/text()"/>
        </xsl:call-template>
    </xsl:template>
    <xsl:template name="cn2pnAttr">
        <xsl:param name="tag"/>
        <xsl:param name="cn"/>
        <xsl:param name="cn26" select="$cn/component"/>
        <xsl:call-template name="pnAttr">
            <xsl:with-param name="tag" select="$tag"/>
            <xsl:with-param name="val" select="$cn/text()"/>
            <xsl:with-param name="fn" select="$cn26[1]/text()"/>
            <xsl:with-param name="gn" select="$cn26[2]/text()"/>
            <xsl:with-param name="mn" select="$cn26[3]/text()"/>
            <xsl:with-param name="ns" select="$cn26[4]/text()"/>
            <xsl:with-param name="np" select="$cn26[5]/text()"/>
        </xsl:call-template>
    </xsl:template>
    <xsl:template name="pnAttr">
        <xsl:param name="tag"/>
        <xsl:param name="val"/>
        <xsl:param name="fn"/>
        <xsl:param name="gn"/>
        <xsl:param name="mn"/>
        <xsl:param name="np"/>
        <xsl:param name="ns"/>
        <xsl:if test="$val">
            <attr tag="{$tag}" vr="PN">
                <xsl:if test="$val != '&quot;&quot;'">
                    <xsl:value-of select="$fn"/>
                    <xsl:if test="$gn or $mn or $np or $ns">
                        <xsl:text>^</xsl:text>
                        <xsl:value-of select="$gn"/>
                        <xsl:if test="$mn or $np or $ns">
                            <xsl:text>^</xsl:text>
                            <xsl:value-of select="$mn"/>
                            <xsl:if test="$np or $ns">
                                <xsl:text>^</xsl:text>
                                <xsl:value-of select="$np"/>
                                <xsl:if test="$ns">
                                    <xsl:text>^</xsl:text>
                                    <xsl:value-of select="$ns"/>
                                </xsl:if>
                            </xsl:if>
                        </xsl:if>
                    </xsl:if>
                </xsl:if>
            </attr>
        </xsl:if>
    </xsl:template>
    <xsl:template name="codeItem">
        <xsl:param name="sqtag"/>
        <xsl:param name="code"/>
        <xsl:param name="scheme"/>
        <xsl:param name="meaning"/>
        <attr tag="{$sqtag}" vr="SQ">
            <item>
                <!-- Code Value -->
                <xsl:call-template name="dcmAttr">
                    <xsl:with-param name="tag" select="'00080100'"/>
                    <xsl:with-param name="vr" select="'SH'"/>
                    <xsl:with-param name="val" select="$code"/>
                </xsl:call-template>
                <!-- Coding Scheme Designator -->
                <xsl:call-template name="dcmAttr">
                    <xsl:with-param name="tag" select="'00080102'"/>
                    <xsl:with-param name="vr" select="'SH'"/>
                    <xsl:with-param name="val" select="$scheme"/>
                </xsl:call-template>
                <!-- Code Meaning -->
                <xsl:call-template name="dcmAttr">
                    <xsl:with-param name="tag" select="'00080104'"/>
                    <xsl:with-param name="vr" select="'LO'"/>
                    <xsl:with-param name="val" select="$meaning"/>
                </xsl:call-template>
            </item>
        </attr>
    </xsl:template>
    <xsl:template match="PID">
        <!-- Patient Name -->
        <xsl:call-template name="xpn2pnAttr">
            <xsl:with-param name="tag" select="'00100010'"/>
            <xsl:with-param name="xpn" select="field[5]"/>
        </xsl:call-template>
        <!-- Patient ID -->
        <xsl:call-template name="cx2attrs">
            <xsl:with-param name="idtag" select="'00100020'"/>
            <xsl:with-param name="istag" select="'00100021'"/>
            <xsl:with-param name="cx" select="field[3]"/>
        </xsl:call-template>
        <!-- Patient Birth Date -->
        <xsl:call-template name="dcmAttrDA">
            <xsl:with-param name="tag" select="'00100030'"/>
            <xsl:with-param name="val" select="field[7]/text()"/>
        </xsl:call-template>
        <!-- Patient Sex -->
        <xsl:call-template name="dcmAttr">
            <xsl:with-param name="tag" select="'00100040'"/>
            <xsl:with-param name="vr" select="'CS'"/>
            <xsl:with-param name="val" select="field[8]/text()"/>
        </xsl:call-template>
    </xsl:template>
</xsl:stylesheet>
