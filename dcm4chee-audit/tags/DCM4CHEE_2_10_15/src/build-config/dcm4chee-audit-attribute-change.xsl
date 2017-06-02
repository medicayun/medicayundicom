<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output method="xml" omit-xml-declaration="yes" indent="yes"/>
  <xsl:param name="mbean"/>
  <xsl:template match="/mbean">
    <mbean name="{$mbean}">
      <xsl:apply-templates select="attribute[@access!='read-only']"/>
    </mbean>
  </xsl:template>
  <xsl:template match="attribute">
    <attribute name="{name}"/>
  </xsl:template>
</xsl:stylesheet>
