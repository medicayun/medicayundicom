<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:fo="http://www.w3.org/1999/XSL/Format" version="1.0">
    <xsl:output method="xml" indent="yes" media-type="text/xml-fo"/>
    <xsl:include href="common.xsl"/>
    <!-- max number of listed patients on label-->
    <xsl:variable name="maxPatCount" select="3"/>
    <!-- max number of listed studies on label-->
    <xsl:variable name="maxStudyCount" select="3"/>
    <!-- overwritten by application with actual values -->
    <xsl:param name="writer" select="'CDRecord'"/>
    <xsl:param name="fsid" select="'12345678'"/>
    <xsl:param name="seqno" select="1"/>
    <xsl:param name="size" select="2"/>
    <xsl:param name="today" select="'20030515'"/>
    <!-- the stylesheet processing entry point -->
    <xsl:template match="/">
        <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
            <fo:layout-master-set>
                <!-- ajust page-size and margins according the label printer -->
                <fo:simple-page-master master-name="page" page-height="120mm" page-width="120mm"
                    margin-left="0mm" margin-right="0mm" margin-top="0mm" margin-bottom="0mm">
                    <fo:region-body>
                        <xsl:attribute name="background-image">url(label_bg.jpg)</xsl:attribute>
                        <xsl:attribute name="background-attachment">fixed</xsl:attribute>
                        <xsl:attribute name="background-repeat">no-repeat</xsl:attribute>
                    </fo:region-body>
                </fo:simple-page-master>
            </fo:layout-master-set>
            <fo:page-sequence master-reference="page">
                <fo:flow flow-name="xsl-region-body">
                    <fo:block-container position="absolute" top="3mm" left="35mm" right="85mm" bottom="20mm">
                        <xsl:apply-templates select="dicomdir" mode="top"/>
                    </fo:block-container>
                    <fo:block-container position="absolute" top="20mm" left="20mm" right="100mm" bottom="40mm">
                        <xsl:apply-templates select="dicomdir" mode="upper"/>
                    </fo:block-container>
                    <fo:block-container position="absolute" top="40mm" left="5mm" right="35mm" bottom="85mm">
                        <xsl:apply-templates select="dicomdir" mode="left"/>
                    </fo:block-container>
                    <fo:block-container position="absolute" top="40mm" left="85mm" right="115mm" bottom="85mm">
                        <xsl:apply-templates select="dicomdir" mode="right"/>
                    </fo:block-container>
                    <fo:block-container position="absolute" top="85mm" left="20mm" right="100mm" bottom="105mm">
                        <xsl:apply-templates select="dicomdir" mode="lower"/>
                    </fo:block-container>
                    <fo:block-container position="absolute" top="105mm" left="20mm" right="100mm" bottom="120mm">
                        <xsl:apply-templates select="dicomdir" mode="bottom"/>
                    </fo:block-container>
                </fo:flow>
            </fo:page-sequence>
        </fo:root>
    </xsl:template>
    <xsl:template match="dicomdir" mode="top">
        <xsl:variable name="label" select="attr[@tag='(2200,0002)']"/>
        <xsl:choose>
            <!-- if Label Text -->
            <xsl:when test="$label != ''">
                <fo:block text-align="center" font-family="Helvetica" font-weight="bold" font-size="12pt">
                    <xsl:value-of select="$label"/>
                </fo:block>
            </xsl:when>
            <xsl:otherwise>
                <!-- Include dcm4chee Logo -->
                <fo:block text-align="center">
                    <fo:external-graphic src="logo.jpg"/>
                </fo:block>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template match="dicomdir" mode="left">
        <!-- Include IHE Logo -->
        <fo:wrapper text-align="center" font-family="Helvetica" font-weight="bold" font-size="12pt">
            <fo:block padding-top="5mm">
                <fo:external-graphic src="ihe_logo.jpg"/>
            </fo:block>
            <fo:block>PDI - Demo</fo:block>
            <fo:block>IHE-E 2006</fo:block>
        </fo:wrapper>
    </xsl:template>
    <xsl:template match="dicomdir" mode="right">
        <fo:wrapper text-align="left" font-family="Helvetica" font-size="8pt">
            <!-- Creation Date and Institution -->
            <fo:block padding-top="10mm"> Created at <xsl:call-template name="formatDate">
                    <xsl:with-param name="date" select="$today"/>
                </xsl:call-template>
            </fo:block>
            <fo:block>by <fo:inline font-weight="bold">YOUR COMPANY NAME</fo:inline>
            </fo:block>
            <fo:block>STREET</fo:block>
            <fo:block>CITY</fo:block>
            <fo:block>COUNTRY</fo:block>
            <fo:block>http://your.company.url</fo:block>
        </fo:wrapper>
    </xsl:template>
    <xsl:template match="dicomdir" mode="upper">
        <!-- if Label Using Information Extracted From Instances -->
        <xsl:if test="attr[@tag='(2200,0001)']='YES'">
            <xsl:choose>
                <!-- if all studies belong to one patient -->
                <xsl:when test="count(record) = 1">
                    <fo:block padding-bottom="2mm" text-align="center" font-family="Helvetica"
                        font-weight="bold" font-size="12pt">
                        <xsl:text>Patient - Disk</xsl:text>
                    </fo:block>
                    <fo:table width="80mm" table-layout="fixed" font-family="Helvetica" font-size="8pt">
                        <fo:table-column column-width="25mm" column-number="1"/>
                        <fo:table-column column-width="55mm" column-number="2"/>
                        <fo:table-body font-family="Helvetica" font-size="10pt">
                            <fo:table-row>
                                <fo:table-cell padding="1pt" text-align="right">
                                    <fo:block>Patient Name:</fo:block>
                                </fo:table-cell>
                                <fo:table-cell padding="1pt" text-align="left">
                                    <fo:block font-weight="bold">
                                        <xsl:call-template name="formatPN">
                                            <xsl:with-param name="pn" select="record[1]/attr[@tag='(0010,0010)']"/>
                                        </xsl:call-template>
                                    </fo:block>
                                </fo:table-cell>
                            </fo:table-row>
                            <fo:table-row>
                                <fo:table-cell padding="1pt" text-align="right">
                                    <fo:block>Birth Date:</fo:block>
                                </fo:table-cell>
                                <fo:table-cell padding="1pt" text-align="left">
                                    <fo:block font-weight="bold">
                                        <xsl:call-template name="formatDate">
                                            <xsl:with-param name="date" select="record[1]/attr[@tag='(0010,0030)']"/>
                                        </xsl:call-template>
                                    </fo:block>
                                </fo:table-cell>
                            </fo:table-row>
                        </fo:table-body>
                    </fo:table>
                </xsl:when>
                <!-- if studies belong to several patients -->
                <xsl:otherwise>
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
                            <xsl:for-each select="record[record/record/record[@seqno=$seqno]]">
                                <xsl:sort data-type="text" order="ascending" select="attr[@tag='(0010,0010)']"/>
                                <xsl:if test="position() &lt;= $maxPatCount">
                                    <fo:table-row>
                                        <fo:table-cell padding="1pt" border-top="0.5pt solid black"
                                            border-left="0.5pt solid black" border-right="0.5pt solid black">
                                            <xsl:if test="position() = last()">
                                                <xsl:attribute name="border-bottom">0.5pt solid black</xsl:attribute>
                                            </xsl:if>
                                            <fo:block>
                                                <xsl:call-template name="formatPN">
                                                  <xsl:with-param name="pn" select="attr[@tag='(0010,0010)']"/>
                                                </xsl:call-template>
                                            </fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell padding="1pt" border-top="0.5pt solid black"
                                            border-left="0.5pt solid black" border-right="0.5pt solid black">
                                            <xsl:if test="position() = last()">
                                                <xsl:attribute name="border-bottom">0.5pt solid black</xsl:attribute>
                                            </xsl:if>
                                            <fo:block>
                                                <xsl:call-template name="formatDate">
                                                  <xsl:with-param name="date" select="attr[@tag='(0010,0030)']"/>
                                                </xsl:call-template>
                                            </fo:block>
                                        </fo:table-cell>
                                    </fo:table-row>
                                </xsl:if>
                            </xsl:for-each>
                        </fo:table-body>
                    </fo:table>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:if>
    </xsl:template>
    <xsl:template match="dicomdir" mode="lower">
        <!-- if Label Using Information Extracted From Instances -->
        <xsl:if test="attr[@tag='(2200,0001)']='YES'">
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
                    <xsl:for-each select="record/record[record/record[@seqno=$seqno]]">
                        <xsl:sort data-type="text" order="ascending" select="attr[@tag='(0008,0020)']"/>
                        <xsl:if test="position() &lt;= $maxStudyCount">
                            <fo:table-row>
                                <fo:table-cell padding="1pt" border-top="0.5pt solid black"
                                    border-left="0.5pt solid black" border-right="0.5pt solid black">
                                    <xsl:if test="position() = last()">
                                        <xsl:attribute name="border-bottom">0.5pt solid black</xsl:attribute>
                                    </xsl:if>
                                    <fo:block>
                                        <xsl:call-template name="formatDate">
                                            <xsl:with-param name="date" select="attr[@tag='(0008,0020)']"/>
                                        </xsl:call-template>
                                    </fo:block>
                                </fo:table-cell>
                                <fo:table-cell padding="1pt" border-top="0.5pt solid black"
                                    border-left="0.5pt solid black" border-right="0.5pt solid black">
                                    <xsl:if test="position() = last()">
                                        <xsl:attribute name="border-bottom">0.5pt solid black</xsl:attribute>
                                    </xsl:if>
                                    <fo:block>
                                        <xsl:value-of select="attr[@tag='(0008,0061)']"/>
                                    </fo:block>
                                </fo:table-cell>
                                <fo:table-cell padding="1pt" border-top="0.5pt solid black"
                                    border-left="0.5pt solid black" border-right="0.5pt solid black">
                                    <xsl:if test="position() = last()">
                                        <xsl:attribute name="border-bottom">0.5pt solid black</xsl:attribute>
                                    </xsl:if>
                                    <fo:block>
                                        <xsl:value-of select="attr[@tag='(0008,1030)']"/>
                                    </fo:block>
                                </fo:table-cell>
                            </fo:table-row>
                        </xsl:if>
                    </xsl:for-each>
                </fo:table-body>
            </fo:table>
        </xsl:if>
    </xsl:template>
    <xsl:template match="dicomdir" mode="bottom">
        <fo:wrapper text-align="center" font-family="Helvetica" font-size="10pt">
            <xsl:variable name="nonDICOM" select="attr[@tag='(2200,0008)']"/>
            <xsl:variable name="viewer" select="attr[@tag='(2200,0009)']"/>
            <fo:block>Content Type: <fo:inline font-weight="bold">
                    <xsl:choose>
                        <!-- With or without Web Content -->
                        <xsl:when test="$nonDICOM!='NO' and $seqno = 1">DICOM AND WEB</xsl:when>
                        <xsl:otherwise>DICOM ONLY</xsl:otherwise>
                    </xsl:choose>
                </fo:inline>
                <!-- if Include Display Application -->
                <xsl:if test="$viewer='YES' and $seqno = 1">
                    <fo:inline font-size="6pt">
                        <xsl:text> (+ DICOM Viewer)</xsl:text>
                    </fo:inline>
                </xsl:if>
            </fo:block>
            <!-- if File-set ID defined or several disks -->
            <xsl:if test="$fsid!='' or $size &gt; 1">
                <fo:block>
                    <xsl:text>Media</xsl:text>
                    <!-- if several disks -->
                    <xsl:if test="$size &gt; 1">[<xsl:value-of select="$seqno"/>/<xsl:value-of select="$size"/>]</xsl:if>
                    <!-- if File-set ID -->
                    <xsl:if test="$fsid!=''"> ID: <fo:inline font-weight="bold">
                            <xsl:value-of select="$fsid"/>
                        </fo:inline>
                    </xsl:if>
                </fo:block>
            </xsl:if>
        </fo:wrapper>
    </xsl:template>
</xsl:stylesheet>
