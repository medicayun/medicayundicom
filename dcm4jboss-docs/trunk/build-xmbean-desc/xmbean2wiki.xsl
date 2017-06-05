<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="text"/>
  
<xsl:template match="/mbean">
h2. Description

<xsl:value-of select="description"/>

h2. Attributes
<xsl:apply-templates select="attribute"/>

h2. Operations
<xsl:apply-templates select="operation"/>

h2. Notifications
<xsl:apply-templates select="notification"/>
</xsl:template>
  
<xsl:template match="attribute">

h4. [#<xsl:value-of select="name"/>] {anchor:<xsl:value-of select="name"/>}

<xsl:value-of select="description"/>
<xsl:apply-templates select="descriptors/value"/>
</xsl:template>

<xsl:template match="descriptors/value">
  
*Default Value:* {{<xsl:value-of select="@value"/>}}
</xsl:template>
  
<xsl:template match="operation">

h4. [#<xsl:value-of select="name"/>] {anchor:<xsl:value-of select="name"/>}
  
<xsl:value-of select="description"/>
</xsl:template>
 
<xsl:template match="notification">

h4. [#<xsl:value-of select="notification-type"/>] {anchor:<xsl:value-of select="notification-type"/>}

<xsl:value-of select="description"/>
</xsl:template>
  
</xsl:stylesheet>
