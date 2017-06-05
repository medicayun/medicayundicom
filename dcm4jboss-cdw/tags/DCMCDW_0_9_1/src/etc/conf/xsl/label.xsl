<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" version="1.0">
  <xsl:output method="xml" indent="yes" media-type="text/xml-fo"/>
<!-- max number of listed patients on label-->
  <xsl:param name="maxPatCount" select="4"/>
<!-- max number of listed studies on label-->
  <xsl:param name="maxStudyCount" select="5"/>
<!-- overwritten by application with actual values -->
  <xsl:param name="writer" select="'CDRecord'"/>
  <xsl:param name="fsid" select="'12345678'"/>
  <xsl:param name="seqno" select="2"/>
  <xsl:param name="size" select="3"/>
<!-- the stylesheet processing entry point -->
  <xsl:template match="/dicomdir">
    <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
      <fo:layout-master-set>
<!-- ajust page-size and margins according the label printer -->
        <fo:simple-page-master master-name="page" page-height="14cm" page-width="14cm" margin-left="1cm" margin-right="1cm" margin-top="1cm" margin-bottom="1cm">
          <fo:region-body>
            <xsl:attribute name="background-image">url(label_bg.jpg)</xsl:attribute>
            <xsl:attribute name="background-attachment">fixed</xsl:attribute>
            <xsl:attribute name="background-repeat">no-repeat</xsl:attribute>
          </fo:region-body>
        </fo:simple-page-master>
      </fo:layout-master-set>
      <fo:page-sequence master-reference="page">
        <fo:flow flow-name="xsl-region-body">
          <fo:block-container position="absolute" top="5mm" left="35mm" right="85mm" bottom="20mm">
            <xsl:variable name="labeltext" select="attr[@tag='(2200,0002)']"/>
            <xsl:choose>
<!-- if Label Text -->
              <xsl:when test="$labeltext != ''">
                <fo:block text-align="center" font-family="Helvetica" font-weight="bold" font-size="12pt">
                  <xsl:value-of select="$labeltext"/>
                </fo:block>
              </xsl:when>
              <xsl:otherwise>
<!-- Include TIANI Logo -->
                <fo:block text-align="center">
                  <fo:external-graphic src="tiani_logo.jpg"/>
                </fo:block>
              </xsl:otherwise>
            </xsl:choose>
          </fo:block-container>
          <fo:block-container position="absolute" top="50mm" left="5mm" right="40mm" bottom="80mm">
<!-- Include IHE Logo -->
            <fo:block text-align="center">
              <fo:external-graphic src="ihe_logo.jpg"/>
            </fo:block>
          </fo:block-container>
<!-- if Include Display Application -->
          <xsl:if test="attr[@tag='(2200,0009)']='YES'">
            <fo:block-container position="absolute" top="50mm" left="80mm" right="117mm" bottom="80mm">
              <xsl:call-template name="list-requirements"/>
            </fo:block-container>
          </xsl:if>
<!-- if Label Using Information Extracted From Instances -->
          <xsl:if test="attr[@tag='(2200,0001)']='YES'">
            <fo:block-container position="absolute" top="20mm" left="20mm" right="100mm" bottom="40mm">
              <xsl:choose>
<!-- if all studies belong to one patient -->
                <xsl:when test="count(record) = 1">
                  <xsl:apply-templates select="record" mode="list-patient"/>
                </xsl:when>
<!-- if studies belong to several patients -->
                <xsl:otherwise>
                  <xsl:apply-templates select="." mode="list-patients"/>
                </xsl:otherwise>
              </xsl:choose>
            </fo:block-container>
            <fo:block-container position="absolute" top="80mm" left="20mm" right="100mm" bottom="107mm">
              <xsl:apply-templates select="." mode="list-studies"/>
            </fo:block-container>
          </xsl:if>
          <fo:block-container position="absolute" top="107mm" left="20mm" right="100mm" bottom="120mm">
<!-- if File-set ID defined -->
            <xsl:if test="$fsid!=''">
              <fo:block text-align="center" font-family="Helvetica" font-size="12pt">
                <xsl:text>File-set ID: </xsl:text>
                <xsl:value-of select="$fsid"/>
              </fo:block>
            </xsl:if>
<!-- if several disks -->
            <xsl:if test="$size &gt; 1">
              <fo:block text-align="center" font-family="Helvetica" font-size="12pt">
                <xsl:text>Disk #</xsl:text>
                <xsl:value-of select="$seqno"/>
                <xsl:text> of </xsl:text>
                <xsl:value-of select="$size"/>
              </fo:block>
            </xsl:if>
          </fo:block-container>
        </fo:flow>
      </fo:page-sequence>
    </fo:root>
  </xsl:template>
  <xsl:template match="record" mode="list-patient">
    <fo:block padding-bottom="2mm" text-align="center" font-family="Helvetica" font-weight="bold" font-size="12pt">
      <xsl:text>Patient - Disk</xsl:text>
    </fo:block>
    <fo:list-block font-family="Helvetica">
      <fo:list-item>
        <fo:list-item-label text-align="right" end-indent="52mm">
          <fo:block font-size="10pt">Patient Name:</fo:block>
        </fo:list-item-label>
        <fo:list-item-body start-indent="30mm">
          <fo:block font-size="12pt">
            <xsl:call-template name="formatPN">
              <xsl:with-param name="pn" select="attr[@tag='(0010,0010)']"/>
            </xsl:call-template>
          </fo:block>
        </fo:list-item-body>
      </fo:list-item>
      <fo:list-item>
        <fo:list-item-label text-align="right" end-indent="52mm">
          <fo:block font-size="10pt">Birth Date:</fo:block>
        </fo:list-item-label>
        <fo:list-item-body start-indent="30mm">
          <fo:block font-size="12pt">
            <xsl:call-template name="formatDate">
              <xsl:with-param name="date" select="attr[@tag='(0010,0030)']"/>
            </xsl:call-template>
          </fo:block>
        </fo:list-item-body>
      </fo:list-item>
    </fo:list-block>
  </xsl:template>
  <xsl:template match="dicomdir" mode="list-patients">
    <fo:table width="80mm" table-layout="fixed" font-family="Helvetica" font-size="8pt">
      <fo:table-column column-width="62mm" column-number="1"/>
      <fo:table-column column-width="18mm" column-number="2"/>
      <fo:table-header>
        <fo:table-row>
          <fo:table-cell padding="1pt" border="0.5pt solid black">
            <fo:block font-weight="bold">Patient Name</fo:block>
          </fo:table-cell>
          <fo:table-cell padding="1pt" border="0.5pt solid black">
            <fo:block font-weight="bold">Birth Date</fo:block>
          </fo:table-cell>
        </fo:table-row>
      </fo:table-header>
      <fo:table-body>
        <xsl:apply-templates select="record">
          <xsl:sort data-type="text" order="ascending" select="attr[@tag='(0010,0010)']"/>
        </xsl:apply-templates>
      </fo:table-body>
    </fo:table>
  </xsl:template>
  <xsl:template match="record[@type='PATIENT']">
    <xsl:choose>
      <xsl:when test="position()&lt;$maxPatCount or last()=$maxPatCount">
        <fo:table-row>
          <fo:table-cell padding="1pt" border="0.5pt solid black">
            <fo:block>
              <xsl:call-template name="formatPN">
                <xsl:with-param name="pn" select="attr[@tag='(0010,0010)']"/>
              </xsl:call-template>
            </fo:block>
          </fo:table-cell>
          <fo:table-cell padding="1pt" border="0.5pt solid black">
            <fo:block>
              <xsl:call-template name="formatDate">
                <xsl:with-param name="date" select="attr[@tag='(0010,0030)']"/>
              </xsl:call-template>
            </fo:block>
          </fo:table-cell>
        </fo:table-row>
      </xsl:when>
      <xsl:when test="position()= $maxPatCount">
        <fo:table-row>
          <fo:table-cell border-left="0.5pt solid black" border-right="0.5pt solid black">
            <fo:block text-align="center">:</fo:block>
          </fo:table-cell>
          <fo:table-cell border-left="0.5pt solid black" border-right="0.5pt solid black">
            <fo:block text-align="center">:</fo:block>
          </fo:table-cell>
        </fo:table-row>
      </xsl:when>
    </xsl:choose>
  </xsl:template>
  <xsl:template name="list-requirements">
    <fo:block padding-bottom="2mm" font-family="Helvetica" font-size="9pt">System Requirements:</fo:block>
    <fo:list-block font-family="Helvetica" font-size="8pt">
      <fo:list-item>
        <fo:list-item-label>
          <fo:block>Processor:</fo:block>
        </fo:list-item-label>
        <fo:list-item-body start-indent="15mm">
          <fo:block>P3/500MHz</fo:block>
        </fo:list-item-body>
      </fo:list-item>
      <fo:list-item>
        <fo:list-item-label>
          <fo:block>Memory:</fo:block>
        </fo:list-item-label>
        <fo:list-item-body start-indent="15mm">
          <fo:block>min 256MB</fo:block>
        </fo:list-item-body>
      </fo:list-item>
      <fo:list-item>
        <fo:list-item-label>
          <fo:block>OS:</fo:block>
        </fo:list-item-label>
        <fo:list-item-body start-indent="15mm">
          <fo:block>WinNT/2k/XP/Me</fo:block>
        </fo:list-item-body>
      </fo:list-item>
    </fo:list-block>
  </xsl:template>
  <xsl:template match="dicomdir" mode="list-studies">
    <fo:table width="80mm" table-layout="fixed" font-family="Helvetica" font-size="8pt">
      <fo:table-column column-width="18mm" column-number="1"/>
      <fo:table-column column-width="17mm" column-number="2"/>
      <fo:table-column column-width="45mm" column-number="3"/>
      <fo:table-header>
        <fo:table-row>
          <fo:table-cell padding="1pt" border="0.5pt solid black">
            <fo:block font-weight="bold">Study Date</fo:block>
          </fo:table-cell>
          <fo:table-cell padding="1pt" border="0.5pt solid black">
            <fo:block font-weight="bold">Modality</fo:block>
          </fo:table-cell>
          <fo:table-cell padding="1pt" border="0.5pt solid black">
            <fo:block font-weight="bold">Study Description</fo:block>
          </fo:table-cell>
        </fo:table-row>
      </fo:table-header>
      <fo:table-body>
        <xsl:apply-templates select="record/record">
          <xsl:sort data-type="text" order="ascending" select="attr[@tag='(0008,0020)']"/>
        </xsl:apply-templates>
      </fo:table-body>
    </fo:table>
  </xsl:template>
  <xsl:template match="record[@type='STUDY']">
    <xsl:choose>
      <xsl:when test="position()&lt;$maxStudyCount or last()=$maxStudyCount">
        <fo:table-row>
          <fo:table-cell padding="1pt" border="0.5pt solid black">
            <fo:block>
              <xsl:call-template name="formatDate">
                <xsl:with-param name="date" select="attr[@tag='(0008,0020)']"/>
              </xsl:call-template>
            </fo:block>
          </fo:table-cell>
          <fo:table-cell padding="1pt" border="0.5pt solid black">
            <fo:block>
              <xsl:value-of select="attr[@tag='(0008,0061)']"/>
            </fo:block>
          </fo:table-cell>
          <fo:table-cell padding="1pt" border="0.5pt solid black">
            <fo:block>
              <xsl:value-of select="attr[@tag='(0008,1030)']"/>
            </fo:block>
          </fo:table-cell>
        </fo:table-row>
      </xsl:when>
      <xsl:when test="position()=$maxStudyCount">
        <fo:table-row>
          <fo:table-cell border-left="0.5pt solid black" border-right="0.5pt solid black">
            <fo:block text-align="center">:</fo:block>
          </fo:table-cell>
          <fo:table-cell border-left="0.5pt solid black" border-right="0.5pt solid black">
            <fo:block text-align="center">:</fo:block>
          </fo:table-cell>
          <fo:table-cell border-left="0.5pt solid black" border-right="0.5pt solid black">
            <fo:block text-align="center">:</fo:block>
          </fo:table-cell>
        </fo:table-row>
      </xsl:when>
    </xsl:choose>
  </xsl:template>
  <xsl:template name="formatPN">
    <xsl:param name="pn"/>
    <xsl:value-of select="translate($pn,'^',',')"/>
  </xsl:template>
  <xsl:template name="formatDate">
    <xsl:param name="date"/>
    <xsl:variable name="len" select="string-length($date)"/>
    <xsl:choose>
      <xsl:when test="$len = 10">
        <xsl:value-of select="$date"/>
      </xsl:when>
      <xsl:when test="$len = 8">
        <xsl:value-of select="substring($date,1,4)"/>
        <xsl:text>-</xsl:text>
        <xsl:value-of select="substring($date,5,2)"/>
        <xsl:text>-</xsl:text>
        <xsl:value-of select="substring($date,7)"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>????-??-??</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
</xsl:stylesheet>
