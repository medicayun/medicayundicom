<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="text"/>

<xsl:template match="/">
<xsl:text>datasource=</xsl:text>
<xsl:value-of select="jbosscmp-jdbc/defaults/datasource"/>
<xsl:text>
</xsl:text>
<xsl:text>datasource-mapping=</xsl:text>
<xsl:value-of select="jbosscmp-jdbc/defaults/datasource-mapping"/>
<xsl:text>
AddUserCmd=INSERT INTO users (user_id,passwd) VALUES(?,?)
UpdatePasswordForUserCmd=UPDATE users SET passwd=? WHERE user_id=?
RemoveUserCmd=DELETE FROM users WHERE user_id=?
AddRoleToUserCmd=INSERT INTO roles (user_id,roles) VALUES(?,?)
RemoveRoleFromUserCmd=DELETE FROM roles WHERE user_id=? AND roles=?
QueryUsersCmd=SELECT user_id FROM users
QueryPasswordForUserCmd=SELECT passwd FROM users WHERE user_id=?
QueryRolesForUserCmd=SELECT roles FROM roles WHERE user_id=?
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
<xsl:apply-templates select="jbosscmp-jdbc/enterprise-beans/entity[ejb-name = 'SeriesRequest']" mode="fk">
<xsl:with-param name="fk" select="'series_fk'"/>
</xsl:apply-templates>
<xsl:apply-templates select="jbosscmp-jdbc/enterprise-beans/entity[ejb-name = 'Instance']" mode="fk">
<xsl:with-param name="fk" select="'series_fk'"/>
</xsl:apply-templates>
<xsl:apply-templates select="jbosscmp-jdbc/enterprise-beans/entity[ejb-name = 'Instance']" mode="fk">
<xsl:with-param name="fk" select="'srcode_fk'"/>
</xsl:apply-templates>
<xsl:apply-templates select="jbosscmp-jdbc/enterprise-beans/entity[ejb-name = 'Instance']" mode="fk">
<xsl:with-param name="fk" select="'media_fk'"/>
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
<xsl:apply-templates select="jbosscmp-jdbc/enterprise-beans/entity[ejb-name = 'GPSPS']" mode="fk">
<xsl:with-param name="fk" select="'patient_fk'"/>
</xsl:apply-templates>
<xsl:apply-templates select="jbosscmp-jdbc/enterprise-beans/entity[ejb-name = 'GPSPS']" mode="fk">
<xsl:with-param name="fk" select="'code_fk'"/>
</xsl:apply-templates>
<xsl:apply-templates select="jbosscmp-jdbc/enterprise-beans/entity[ejb-name = 'GPSPSRequest']" mode="fk">
<xsl:with-param name="fk" select="'gpsps_fk'"/>
</xsl:apply-templates>
<xsl:apply-templates select="jbosscmp-jdbc/enterprise-beans/entity[ejb-name = 'GPSPSPerformer']" mode="fk">
<xsl:with-param name="fk" select="'gpsps_fk'"/>
</xsl:apply-templates>
<xsl:apply-templates select="jbosscmp-jdbc/enterprise-beans/entity[ejb-name = 'GPSPSPerformer']" mode="fk">
<xsl:with-param name="fk" select="'code_fk'"/>
</xsl:apply-templates>
<xsl:apply-templates select="jbosscmp-jdbc/enterprise-beans/entity[ejb-name = 'MPPS']" mode="fk">
<xsl:with-param name="fk" select="'patient_fk'"/>
</xsl:apply-templates>
<xsl:apply-templates select="jbosscmp-jdbc/enterprise-beans/entity[ejb-name = 'GPPPS']" mode="fk">
<xsl:with-param name="fk" select="'patient_fk'"/>
</xsl:apply-templates>
<xsl:apply-templates select="jbosscmp-jdbc/enterprise-beans/entity[ejb-name = 'HP']" mode="fk">
<xsl:with-param name="fk" select="'user_fk'"/>
</xsl:apply-templates>
<xsl:apply-templates select="jbosscmp-jdbc/enterprise-beans/entity[ejb-name = 'HPDefinition']" mode="fk">
<xsl:with-param name="fk" select="'hp_fk'"/>
</xsl:apply-templates>
<xsl:apply-templates select="jbosscmp-jdbc/enterprise-beans/entity[ejb-name = 'PrivateStudy']" mode="fk">
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
