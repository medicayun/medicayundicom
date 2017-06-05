<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="xml" indent="yes"/>
    <xsl:include href="common.xsl"/>
    <xsl:template match="/hl7">
        <dataset>
            <attr tag="00080005" vr="CS">ISO_IR 100</attr>
            <xsl:apply-templates select="PID"/>
            <xsl:apply-templates select="PV1"/>
            <xsl:apply-templates select="ORC[1]"/>
            <xsl:apply-templates select="OBR[1]"/>
            <!-- Scheduled Procedure Step Sequence -->
            <attr tag="00400100" vr="SQ">
                <xsl:apply-templates select="ORC" mode="sps"/>
            </attr>
         </dataset>
    </xsl:template>
    <xsl:template match="PV1">
        <!-- Referring Physican Name
        <xsl:call-template name="cn2pnAttr">
            <xsl:with-param name="tag" select="'00080090'"/>
            <xsl:with-param name="cn" select="field[8]"/>
        </xsl:call-template>
        -->
        <xsl:call-template name="pregnancyStatus">
            <xsl:with-param name="ambulantStatus" select="field[15]/text()"/>
        </xsl:call-template>
        <!-- Admission ID, Issuer -->
        <xsl:call-template name="cx2attrs">
            <xsl:with-param name="idtag" select="'00380010'"/>
            <xsl:with-param name="istag" select="'00380011'"/>
            <xsl:with-param name="cx" select="field[19]"/>
        </xsl:call-template>
    </xsl:template>
    <xsl:template name="pregnancyStatus">
        <xsl:param name="ambulantStatus"/>
        <xsl:if test="$ambulantStatus = 'B6'">
            <attr tag="001021C0" vr="US">3</attr>
        </xsl:if>
    </xsl:template>
    <xsl:template match="ORC[1]">
        <xsl:call-template name="procedurePriority">
            <xsl:with-param name="priority" select="field[7]/component[5]"/>
        </xsl:call-template>
        <!-- Provide Requesting Physician also as Referring Physican Name -->
        <xsl:call-template name="cn2pnAttr">
            <xsl:with-param name="tag" select="'00080090'"/>
            <xsl:with-param name="cn" select="field[12]"/>
        </xsl:call-template>
        <!-- Requesting Physician -->
        <xsl:call-template name="cn2pnAttr">
            <xsl:with-param name="tag" select="'00321032'"/>
            <xsl:with-param name="cn" select="field[12]"/>
        </xsl:call-template>
    </xsl:template>
    <xsl:template name="procedurePriority">
        <xsl:param name="priority"/>
        <xsl:if test="$priority">
            <attr tag="00401003" vr="CS">
                <xsl:choose>
                    <xsl:when test="$priority = 'S'">STAT</xsl:when>
                    <xsl:when test="$priority = 'A' or $priority = 'P' or $priority = 'C' ">HIGH</xsl:when>
                    <xsl:when test="$priority = 'R'">ROUTINE</xsl:when>
                    <xsl:when test="$priority = 'T'">MEDIUM</xsl:when>
                </xsl:choose>
            </attr>
        </xsl:if>
    </xsl:template>
    <xsl:template match="OBR[1]">
        <!-- Placer Order Number -->
        <xsl:call-template name="dcmAttr">
            <xsl:with-param name="tag" select="'00402016'"/>
            <xsl:with-param name="vr" select="'LO'"/>
            <xsl:with-param name="val" select="field[2]/text()"/>
        </xsl:call-template>
        <!-- Filler Order Number -->
        <xsl:variable name="acc_no" select="field[3]/text()"/>
        <xsl:call-template name="dcmAttr">
            <xsl:with-param name="tag" select="'00402017'"/>
            <xsl:with-param name="vr" select="'LO'"/>
            <xsl:with-param name="val" select="$acc_no"/>
        </xsl:call-template>
        <!-- Accession Number -->
        <xsl:call-template name="dcmAttr">
            <xsl:with-param name="tag" select="'00080050'"/>
            <xsl:with-param name="vr" select="'SH'"/>
            <xsl:with-param name="val" select="$acc_no"/>
        </xsl:call-template>
        <!-- Patient State -->
        <xsl:call-template name="dcmAttr">
            <xsl:with-param name="tag" select="'00380500'"/>
            <xsl:with-param name="vr" select="'LO'"/>
            <xsl:with-param name="val" select="field[12]/text()"/>
        </xsl:call-template>
         <!-- Medical Alerts -->
        <xsl:call-template name="dcmAttr">
            <xsl:with-param name="tag" select="'00102000'"/>
            <xsl:with-param name="vr" select="'LO'"/>
            <xsl:with-param name="val" select="field[13]/text()"/>
        </xsl:call-template>
        <!-- Patient Transport Arrangements -->
        <xsl:call-template name="dcmAttr">
            <xsl:with-param name="tag" select="'00401004'"/>
            <xsl:with-param name="vr" select="'LO'"/>
            <xsl:with-param name="val" select="field[30]/text()"/>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="ORC" mode="sps">
        <item>
            <!-- Scheduled Station Name -->
            <xsl:call-template name="dcmAttr">
                <xsl:with-param name="tag" select="'00400010'"/>
                <xsl:with-param name="vr" select="'SH'"/>
                <xsl:with-param name="val" select="field[18]/text()"/>
            </xsl:call-template>
            <xsl:apply-templates select="following-sibling::OBR[1]" mode="sps"/>
        </item>
    </xsl:template>
    <xsl:template match="OBR" mode="sps">
        <!-- Scheduled Procedure Step Start Date/Time -->
        <xsl:call-template name="dcmAttrDATM">
            <xsl:with-param name="datag" select="'00400002'"/>
            <xsl:with-param name="tmtag" select="'00400003'"/>
            <xsl:with-param name="val" select="field[36]/text()"/>
        </xsl:call-template>
        <!-- Study Instance UID -->
        <xsl:variable name="suid" select="field[3]/component[2]"/>
        <xsl:call-template name="dcmAttr">
            <xsl:with-param name="tag" select="'0020000D'"/>
            <xsl:with-param name="vr" select="'UI'"/>
            <xsl:with-param name="val" select="$suid"/>
        </xsl:call-template>
        <xsl:variable name="spsid" select="substring-before(substring-after(substring-after(substring-after(substring-after(substring($suid,17),'.'),'.'),'.'),'.'),'.')"/>
        <!-- Requested Procedure ID -->
        <xsl:call-template name="dcmAttr">
            <xsl:with-param name="tag" select="'00401001'"/>
            <xsl:with-param name="vr" select="'SH'"/>
            <xsl:with-param name="val" select="$spsid"/>
        </xsl:call-template>
        <!-- Requested Procedure Description -->
        <xsl:call-template name="dcmAttr">
            <xsl:with-param name="tag" select="'00321060'"/>
            <xsl:with-param name="vr" select="'LO'"/>
            <xsl:with-param name="val" select="field[4]/component[1]"/>
        </xsl:call-template>
        <!-- Requested Procedure Code Sequence -->
        <xsl:call-template name="codeItem">
            <xsl:with-param name="sqtag" select="'00321064'"/>
            <xsl:with-param name="code" select="field[4]/text()"/>
            <xsl:with-param name="scheme" select="'orbis'"/>
            <xsl:with-param name="meaning" select="field[4]/component[1]"/>
        </xsl:call-template>
        <!-- Scheduled Performing Physican Name -->
        <xsl:call-template name="cn2pnAttr">
            <xsl:with-param name="tag" select="'00400006'"/>
            <xsl:with-param name="cn" select="field[34]"/>
            <xsl:with-param name="cn26" select="field[34]/subcomponent"/>
        </xsl:call-template>
        <!-- Scheduled Procedure Step Description -->
        <xsl:call-template name="dcmAttr">
            <xsl:with-param name="tag" select="'00400007'"/>
            <xsl:with-param name="vr" select="'LO'"/>
            <xsl:with-param name="val" select="field[4]/component[1]"/>
        </xsl:call-template>
        <!-- Scheduled Protocol Code Sequence -->
        <xsl:call-template name="codeItem">
            <xsl:with-param name="sqtag" select="'00400008'"/>
            <xsl:with-param name="code" select="field[4]/text()"/>
            <xsl:with-param name="scheme" select="'orbis'"/>
            <xsl:with-param name="meaning" select="field[4]/component[1]"/>
        </xsl:call-template>
        <!-- Scheduled Procedure Step ID -->
        <xsl:call-template name="dcmAttr">
            <xsl:with-param name="tag" select="'00400009'"/>
            <xsl:with-param name="vr" select="'SH'"/>
            <xsl:with-param name="val" select="$spsid"/>
        </xsl:call-template>
    </xsl:template>
</xsl:stylesheet>
