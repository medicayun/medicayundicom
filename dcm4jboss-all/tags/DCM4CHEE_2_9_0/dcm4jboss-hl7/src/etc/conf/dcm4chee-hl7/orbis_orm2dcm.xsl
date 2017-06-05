<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output method="xml" indent="yes"/>
  <xsl:include href="common.xsl"/>
  <!--  root -->
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
  <!-- PV1 -->
  <xsl:template match="PV1">
    <!-- HL7:Referring Doctor -> DICOM:Referring Physican Name 
    (may replace HL7:Ordering Provider -> DICOM Referring Physican Name mapping)
    <xsl:call-template name="cn2pnAttr">
    <xsl:with-param name="tag" select="'00080090'"/>
    <xsl:with-param name="cn" select="field[8]"/>
    </xsl:call-template>
    -->
    <!-- HL7:Ambulatory Status -> DICOM:Pregnancy Status -->
    <xsl:if test="field[15]/text() = 'B6'">
      <attr tag="001021C0" vr="US">3</attr>
    </xsl:if>
    <!-- HL7:Visit Number -> DICOM:Admission ID + Issuer -->
    <attr tag="00380010" vr="LO">
      <xsl:value-of select="field[19]/text()"/>
    </attr>
    <attr tag="00380011" vr="LO">
      <xsl:value-of select="field[19]/component[3]/text()"/>
    </attr>
  </xsl:template>
  <!-- ORC[1] -->
  <xsl:template match="ORC[1]">
    <!-- HL7:Ordering Provider -> DICOM Referring Physican Name -->
    <xsl:call-template name="cn2pnAttr">
      <xsl:with-param name="tag" select="'00080090'"/>
      <xsl:with-param name="cn" select="field[12]"/>
    </xsl:call-template>
    <!-- HL7:Ordering Provider -> DICOM Requesting Physician -->
    <xsl:call-template name="cn2pnAttr">
      <xsl:with-param name="tag" select="'00321032'"/>
      <xsl:with-param name="cn" select="field[12]"/>
    </xsl:call-template>
    <!-- HL7:Quantity/Timing -> DICOM:Requested Procedure Priority -->
    <xsl:variable name="prior" select="field[7]/component[5]/text()"/>
    <xsl:if test="$prior">
      <attr tag="00401003" vr="CS">
        <xsl:choose>
          <xsl:when test="$prior = 'S'">STAT</xsl:when>
          <xsl:when test="$prior = 'A' or $prior = 'P' or $prior = 'C' ">HIGH</xsl:when>
          <xsl:when test="$prior = 'R'">ROUTINE</xsl:when>
          <xsl:when test="$prior = 'T'">MEDIUM</xsl:when>
        </xsl:choose>
      </attr>
    </xsl:if>
  </xsl:template>
  <!--  OBR[1] -->
  <xsl:template match="OBR[1]">
    <xsl:variable name="ordno" select="field[3]/text()"/>
    <!-- HL7:Filler Order Number -> DICOM:Placer Order Number -->
    <attr tag="00402016" vr="LO">
      <xsl:value-of select="$ordno"/>
    </attr>
    <!-- HL7:Filler Order Number -> DICOM:Filler Order Number -->
    <attr tag="00402017" vr="LO">
      <xsl:value-of select="$ordno"/>
    </attr>
     <!-- HL7:Filler Order Number -> DICOM:Accession Number -->
    <attr tag="00080050" vr="SH">
      <xsl:value-of select="$ordno"/>
    </attr>
    <!--  HL7:Danger Code -> DICOM:Patient State -->
    <attr tag="00380500" vr="LO">
      <xsl:value-of select="field[12]/text()"/>
    </attr>
    <!--  HL7:Relevant Clinical Info -> DICOM:Medical Alerts -->
    <attr tag="00102000" vr="LO">
      <xsl:value-of select="field[13]/text()"/>
    </attr>
    <!-- HL7:Transportation Mode  -> DICOM:Patient Transport Arrangements -->
    <attr tag="00401004" vr="LO">
      <xsl:value-of select="field[30]/text()"/>
    </attr>
  </xsl:template>
  <!-- ORC - sps -->
  <xsl:template match="ORC" mode="sps">
    <item>
      <!-- HL7:Entering Device -> DICOM:Scheduled Station Name -->
      <attr tag="00400010" vr="SH">
        <xsl:value-of select="field[18]/text()"/>
      </attr>
      <!-- HL7:Entering Device -> DICOM:Modality -->
      <attr tag="00080060" vr="CS">
        <xsl:value-of select="field[18]/component[1]/text()"/>
      </attr>
      <xsl:apply-templates select="following-sibling::OBR[1]" mode="sps"/>
    </item>
  </xsl:template>
  <!-- OBR - sps  -->
  <xsl:template match="OBR" mode="sps">
    <!-- HL7:Scheduled Date/Time -> DICOM:Scheduled Procedure Step Start Date/Time -->
    <xsl:variable name="dt" select="field[36]/text()"/>
    <xsl:if test="string-length($dt) >= 10">
      <attr tag="00400002" vr="DA">
          <xsl:value-of select="substring($dt,1,8)"/>
      </attr>
      <attr tag="00400003" vr="TM">
          <xsl:value-of select="substring($dt,9)"/>
      </attr>
    </xsl:if>
    <!-- HL7:Filer Order Number -> DICOM:Study Instance UID -->
    <xsl:variable name="suid" select="field[3]/component[2]/text()"/>
    <attr tag="0020000D" vr="UI">
      <xsl:value-of select="$suid"/>
    </attr>
    <!-- Study Instance UID -> DICOM:Requested Procedure -->
    <xsl:variable name="id" select="substring-before(substring-after(substring-after(substring-after(substring-after(substring($suid,17),'.'),'.'),'.'),'.'),'.')"/>
    <attr tag="00401001" vr="SH">
      <xsl:value-of select="$id"/>
    </attr>
    <!-- Study Instance UID -> DICOM:Scheduled Procedure Step ID -->
    <attr tag="00400009" vr="SH">
      <xsl:value-of select="$id"/>
    </attr>
    <!-- HL7:Universal Service Identifier.2 -> DICOM:Requested Procedure Description -->
    <xsl:variable name="desc" select="field[4]/component[1]/text()"/>    
    <attr tag="00321060" vr="LO">
      <xsl:value-of select="$desc"/>
    </attr>
    <!-- HL7:Universal Service Identifier -> DICOM:Requested Procedure Code -->
    <xsl:call-template name="codeItem">
      <xsl:with-param name="sqtag" select="'00321064'"/>
      <xsl:with-param name="code" select="field[4]/text()"/>
      <xsl:with-param name="scheme" select="'99ORBIS'"/>
      <xsl:with-param name="meaning" select="$desc"/>
    </xsl:call-template>
    <!-- HL7:Universal Service Identifier.2 -> DICOM:Scheduled Procedure Step Description -->
    <attr tag="00400007" vr="LO">
      <xsl:value-of select="$desc"/>
    </attr>
    <!-- HL7:Universal Service Identifier -> DICOM:Scheduled Protocol Code -->
    <xsl:call-template name="codeItem">
      <xsl:with-param name="sqtag" select="'00400008'"/>
      <xsl:with-param name="code" select="field[4]/text()"/>
      <xsl:with-param name="scheme" select="'99ORBIS'"/>
      <xsl:with-param name="meaning" select="$desc"/>
    </xsl:call-template>
    <!-- HL7:Technician -> Scheduled Performing Physican Name -->
    <xsl:call-template name="cn2pnAttr">
      <xsl:with-param name="tag" select="'00400006'"/>
      <xsl:with-param name="cn" select="field[34]"/>
      <xsl:with-param name="cn26" select="field[34]/subcomponent"/>
    </xsl:call-template>
  </xsl:template> 
</xsl:stylesheet>
