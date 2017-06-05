<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="text"/>

<xsl:template match="/">
<xsl:text>datasource-mapping=</xsl:text>
<xsl:value-of select="jbosscmp-jdbc/defaults/datasource-mapping"/>
<xsl:text>
</xsl:text>
<xsl:apply-templates select="jbosscmp-jdbc/enterprise-beans/entity[ejb-name = 'Patient']" mode="fk">
<xsl:with-param name="fk" select="'merge_fk'"/>
</xsl:apply-templates>
<xsl:apply-templates select="jbosscmp-jdbc/enterprise-beans/entity[ejb-name = 'Study']" mode="fk">
<xsl:with-param name="fk" select="'patient_fk'"/>
</xsl:apply-templates>
<xsl:apply-templates select="jbosscmp-jdbc/enterprise-beans/entity[ejb-name = 'Series']" mode="fk">
<xsl:with-param name="fk" select="'study_fk'"/>
</xsl:apply-templates>
<xsl:apply-templates select="jbosscmp-jdbc/enterprise-beans/entity[ejb-name = 'Instance']" mode="fk">
<xsl:with-param name="fk" select="'series_fk'"/>
</xsl:apply-templates>
<xsl:apply-templates select="jbosscmp-jdbc/enterprise-beans/entity[ejb-name = 'Instance']" mode="fk">
<xsl:with-param name="fk" select="'srcode_fk'"/>
</xsl:apply-templates>
<xsl:apply-templates select="jbosscmp-jdbc/enterprise-beans/entity[ejb-name = 'File']" mode="fk">
<xsl:with-param name="fk" select="'instance_fk'"/>
</xsl:apply-templates>
<xsl:apply-templates select="jbosscmp-jdbc/enterprise-beans/entity[ejb-name = 'File']" mode="fk">
<xsl:with-param name="fk" select="'filesystem_fk'"/>
</xsl:apply-templates>
<xsl:apply-templates select="jbosscmp-jdbc/enterprise-beans/entity[ejb-name = 'MWLItem']" mode="fk">
<xsl:with-param name="fk" select="'patient_fk'"/>
</xsl:apply-templates>
<xsl:apply-templates select="jbosscmp-jdbc/enterprise-beans/entity[ejb-name = 'MPPS']" mode="fk">
<xsl:with-param name="fk" select="'patient_fk'"/>
</xsl:apply-templates>
<xsl:apply-templates select="jbosscmp-jdbc/enterprise-beans/entity"/>
</xsl:template>

<xsl:template match="entity" mode="fk">
<xsl:param name="fk"/>
<xsl:value-of select="ejb-name"/>
<xsl:text>.</xsl:text>
<xsl:value-of select="$fk"/>
<xsl:text>=</xsl:text>
<xsl:value-of select="table-name"/>
<xsl:text>.</xsl:text>
<xsl:value-of select="$fk"/>
<xsl:text>
</xsl:text>
</xsl:template>

<xsl:template match="entity">
<xsl:value-of select="ejb-name"/>
<xsl:text>=</xsl:text>
<xsl:value-of select="table-name"/>
<xsl:text>
</xsl:text>
<xsl:apply-templates select="cmp-field"/>
</xsl:template>

<xsl:template match="cmp-field">
<xsl:value-of select="../ejb-name"/>
<xsl:text>.</xsl:text>
<xsl:value-of select="field-name"/>
<xsl:text>=</xsl:text>
<xsl:value-of select="../table-name"/>
<xsl:text>.</xsl:text>
<xsl:value-of select="column-name"/>
<xsl:text>
</xsl:text>
</xsl:template>
</xsl:stylesheet>
