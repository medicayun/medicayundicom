<?xml version="1.0" encoding="UTF-8"?>
<!-- 
Sample SubmissionSet XSL. 
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
	xmlns:rim="urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.1"
	xmlns:rs="urn:oasis:names:tc:ebxml-regrep:registry:xsd:2.1"
>
  <xsl:output method="xml" indent="yes"/>

  <!-- Handles SubmitObjectsRequest Root element -->
  <xsl:template match="/rs:SubmitObjectsRequest">
	<xsl:apply-templates select="rim:LeafRegistryObjectList/rim:ExtrinsicObject"/>
  </xsl:template>

  <!-- Handles LeafRegistryObjectListRoot element -->
  <xsl:template match="/rim:LeafRegistryObjectList">
	<xsl:apply-templates select="rim:ExtrinsicObject"/>
  </xsl:template>

  <xsl:template match="rim:ExtrinsicObject">
	<!-- <xsl:apply-templates select="rim:Slot[@name='sourcePatientId']" /> --><!-- source patient id handled with sourcePatientInfo -->
	<xsl:apply-templates select="rim:Slot[@name='sourcePatientInfo']/rim:ValueList/rim:Value" mode="patInfo"/>
	<xsl:apply-templates select="rim:ExternalIdentifier[@identificationScheme='urn:uuid:58a6f841-87b3-4a3e-92fd-a8ffeff98427']"/>
	<xsl:apply-templates select="rim:ExternalIdentifier[@identificationScheme='urn:uuid:2e82c1f6-a085-4c72-9da3-8640a32e42ab']" mode="docUID"/>
	<xsl:apply-templates select="rim:Slot[@name='creationTime']" />
	<xsl:apply-templates select="rim:Classification[@classificationScheme='urn:uuid:f0306f51-975f-434e-a61c-c59651d33983']" mode="docTitle"/>
 	    
      <!--MIME Type of Encapsulated Document-->
	<attr tag="00420012" vr="LO" ><xsl:value-of select="@mimeType" /></attr>
	
  </xsl:template>

  <xsl:template match="rim:Slot[@name='sourcePatientId']" >
	<!--patientID and Issuer of PatientID from sourcePatientId -->
	<xsl:call-template name="pidAndIssuer">
	    <xsl:with-param name="pidAndIssuer" select="rim:ValueList/rim:Value"/>
	</xsl:call-template>
  </xsl:template>
  
  <xsl:template match="rim:Slot[@name='creationTime']" >
    <xsl:variable name="date" select="rim:ValueList/rim:Value"/>
	<!-- Study date -->
	<attr tag="00080020" vr="DA" ><xsl:value-of select="$date" /></attr>
	<!-- Series date -->
	<attr tag="00080021" vr="DA" ><xsl:value-of select="$date" /></attr>
	<!-- content date -->
	<attr tag="00080023" vr="DA" ><xsl:value-of select="$date" /></attr>
  </xsl:template>
    
  <xsl:template match="rim:Value" mode="patInfo">
         <xsl:variable name="pid_segment" select="substring-before(.,'|')"/>
         <xsl:variable name="pid_value" select="substring-after(.,'|')"/>
         <xsl:choose>
            <xsl:when test="$pid_segment='PID-3'">
                <xsl:call-template name="pidAndIssuer">
                    <xsl:with-param name="pidAndIssuer" select="$pid_value"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="$pid_segment='PID-5'">
    	    <!--Patients Name-->
	    <attr tag="00100010" vr="PN" ><xsl:value-of select="$pid_value" /></attr>
            </xsl:when>
            <xsl:when test="$pid_segment='PID-7'">
    	    <!--Patients Birthdate-->
	    <attr tag="00100030" vr="DA" ><xsl:value-of select="$pid_value" /></attr>
            </xsl:when>
            <xsl:when test="$pid_segment='PID-8'">
    	    <!--Patients Sex-->
	    <attr tag="00100040" vr="CS" ><xsl:value-of select="$pid_value" /></attr>
            </xsl:when>
         </xsl:choose>        
</xsl:template>

  <xsl:template match="rim:ExternalIdentifier[@identificationScheme='urn:uuid:58a6f841-87b3-4a3e-92fd-a8ffeff98427']" >
	<!--Other Patient's ID sequence-->
	<attr tag="00101002" vr="SQ" >
		<item>
             <xsl:call-template name="pidAndIssuer">
                 <xsl:with-param name="pidAndIssuer" select="@value"/>
             </xsl:call-template>
        </item>
	</attr>
  </xsl:template>
    
  <xsl:template match="rim:ExternalIdentifier[@identificationScheme='urn:uuid:2e82c1f6-a085-4c72-9da3-8640a32e42ab']" mode="docUID" >
	<!-- SOP Instance UID -->
	<attr tag="00080018" vr="UI" ><xsl:value-of select="@value" /></attr>
  </xsl:template>

  <xsl:template match="rim:Classification[@classificationScheme='urn:uuid:f0306f51-975f-434e-a61c-c59651d33983']" mode="docTitle" >
	<!--Concept Name Code Sequence-->
    <attr tag="0040A043" vr="SQ">
      <item>
        <!--Code Value-->
        <attr tag="00080100" vr="SH"><xsl:value-of select="@nodeRepresentation" /></attr>
        <!--Coding Scheme Designator-->
        <attr tag="00080102" vr="SH"><xsl:value-of select="rim:Slot[@name='codingScheme']/rim:ValueList/rim:Value" /></attr>
        <!--Code Meaning-->
        <attr tag="00080104" vr="LO"><xsl:value-of select="rim:Name/rim:LocalizedString/@value" /></attr>
      </item>
    </attr>
  </xsl:template>

  <xsl:template name="pidAndIssuer">
        <xsl:param name="pidAndIssuer" />
	  <xsl:variable name="pid" select="substring-before($pidAndIssuer,'^')" />
        <xsl:variable name="issuer" select="substring-after(substring-after(substring-after($pidAndIssuer,'^'),'^'),'^')"/> 
        <xsl:choose>
            <xsl:when test="$issuer">
    	          	<!--Patient ID-->
	    		<attr tag="00100020" vr="LO" ><xsl:value-of select="$pid" /></attr>
    	    		<!--Issuer of Patient ID-->
	    		<attr tag="00100021" vr="LO" ><xsl:value-of select="$issuer" /></attr>
            </xsl:when>
            <xsl:otherwise>
                <attr tag="00100020" vr="LO" ><xsl:value-of select="$pid"/></attr>
            </xsl:otherwise>
        </xsl:choose>             
    </xsl:template>

</xsl:stylesheet>
