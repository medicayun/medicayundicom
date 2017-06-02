<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:rim2="urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.1"
    xmlns:rs2="urn:oasis:names:tc:ebxml-regrep:registry:xsd:2.1"
    xmlns:lcm="urn:oasis:names:tc:ebxml-regrep:xsd:lcm:3.0"
    xmlns:rs="urn:oasis:names:tc:ebxml-regrep:xsd:rs:3.0"
    xmlns:rim="urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0">
    
    <xsl:output method="xml" indent="yes" version="1.0" />
    
    <xsl:template match="/rs2:SubmitObjectsRequest">
        <xsl:element name="{local-name()}" namespace="urn:oasis:names:tc:ebxml-regrep:xsd:lcm:3.0">
            <xsl:copy-of select="@*" />
            <xsl:apply-templates select="rim2:LeafRegistryObjectList"/>
        </xsl:element>
    </xsl:template> 
    <xsl:template match="rim2:LeafRegistryObjectList">
        <xsl:element name="RegistryObjectList" namespace="urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0">
            <xsl:copy-of select="@*" />
            <xsl:apply-templates select="rim2:*" mode="addId"/>
       </xsl:element>
    </xsl:template> 
    
    <xsl:template match="rim2:*" mode="addId">
        <xsl:variable name="name" select="local-name()"/>
        <xsl:variable name="prefix" select="substring($name,0,4)"/>
        <xsl:element name="{$name}" namespace="urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0">
            <xsl:copy-of select="@*" />
            <xsl:if test="$name='Classification' or $name='Association' or $name='ExternalIdentifier' ">
                <xsl:if test="not(@id)">
                    <xsl:attribute name="id"><xsl:value-of select="concat($prefix,'_',position())"/></xsl:attribute>
                </xsl:if>
            </xsl:if>
            <xsl:if test="$name='ExternalIdentifier' ">
                <xsl:attribute name="registryObject"><xsl:value-of select="../@id"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$name='Association' ">
                <xsl:variable name="type" select="@associationType"/>
                <xsl:choose>
	                <xsl:when test="$type='HasMember'">
	                    <xsl:attribute name="associationType"><xsl:value-of select="concat('urn:oasis:names:tc:ebxml-regrep:AssociationType:',$type)"/></xsl:attribute>
	                </xsl:when>
	                <xsl:otherwise>
                        <xsl:attribute name="associationType"><xsl:value-of select="concat('urn:ihe:iti:2007:AssociationType:',$type)"/></xsl:attribute>
	                </xsl:otherwise>
	            </xsl:choose>
            </xsl:if>
            <xsl:apply-templates select="*|text()" mode="addId"/>
        </xsl:element>
    </xsl:template> 

</xsl:stylesheet>
