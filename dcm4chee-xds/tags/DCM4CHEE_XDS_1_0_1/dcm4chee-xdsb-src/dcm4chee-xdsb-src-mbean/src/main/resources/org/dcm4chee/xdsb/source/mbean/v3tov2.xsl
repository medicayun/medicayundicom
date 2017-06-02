<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:rim="urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.1"
    xmlns:rs="urn:oasis:names:tc:ebxml-regrep:registry:xsd:2.1"
    xmlns:lcm3="urn:oasis:names:tc:ebxml-regrep:xsd:lcm:3.0"
    xmlns:rs3="urn:oasis:names:tc:ebxml-regrep:xsd:rs:3.0"
    xmlns:rim3="urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0">
   
    <xsl:template match="/">
        <xsl:apply-templates select="rs3:*" mode="check"/>
    </xsl:template> 
    
    
    <xsl:template match="rs3:*" mode="check">
        <xsl:variable name="name" select="local-name()"/>
        <xsl:element name="{$name}" namespace="urn:oasis:names:tc:ebxml-regrep:registry:xsd:2.1">
            <xsl:copy-of select="@*" />
            <xsl:if test="$name='RegistryResponse' ">
                <xsl:variable name="len" select="string-length('urn:oasis:names:tc:ebxml-regrep:ResponseStatusType:')+1"/>
                <xsl:attribute name="status"><xsl:value-of select="substring(@status,$len)"/></xsl:attribute>
            </xsl:if>
            <xsl:apply-templates select="*|text()" mode="check"/>
        </xsl:element>
    </xsl:template> 
    
    
    
    <xsl:template match="rs:*" mode="changeNS">
        <xsl:variable name="ns">
            <xsl:value-of select="namespace-uri()"/>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="$ns='urn:oasis:names:tc:ebxml-regrep:xsd:rs:3.0'">
                <xsl:apply-templates select="." mode="setNS">
                    <xsl:with-param name="ns" select="'urn:oasis:names:tc:ebxml-regrep:registry:xsd:2.1'"/>
                </xsl:apply-templates>
            </xsl:when>
            <xsl:when test="$ns='urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0'">
                <xsl:apply-templates select="." mode="setNS">
                    <xsl:with-param name="ns"
                        select="'urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.1'"/>
                </xsl:apply-templates>
            </xsl:when>
            <xsl:when test="$ns='urn:oasis:names:tc:ebxml-regrep:xsd:lcm:3.0'">
                <xsl:apply-templates select="." mode="setNS">
                    <xsl:with-param name="ns"
                        select="'urn:oasis:names:tc:ebxml-regrep:registry:xsd:2.1'"/>
                </xsl:apply-templates>
            </xsl:when>
            <xsl:otherwise>
                <xsl:apply-templates select="." mode="setNS">
                    <xsl:with-param name="ns" select="$ns"/>
                </xsl:apply-templates>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template match="*" mode="setNS">
        <xsl:param name="ns" select="'urn:oasis:names:tc:ebxml-regrep:registry:xsd:2.1'"/>
        <xsl:element name="{local-name()}" namespace="{$ns}">
            <xsl:apply-templates select="@*|*|text()" mode="changeNS"/>
        </xsl:element>
    </xsl:template>
</xsl:stylesheet>
