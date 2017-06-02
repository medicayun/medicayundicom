<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:template match="IHEYr4">
    <xsl:apply-templates mode="EventIdentification" select="*"/>
    <xsl:apply-templates mode="ActiveParticipant" select="*"/>
    <xsl:apply-templates mode="AuditSourceIdentification" select="*"/>
    <xsl:apply-templates mode="ParticipantObjectIdentification" select="*"/>
  </xsl:template>
  <!-- =========================================== -->
  <!-- EventIdentification                         -->
  <!-- =========================================== -->
  <xsl:template mode="EventIdentification" match="ActorConfig">
    <strong>Application Activity</strong>
    <xsl:apply-templates mode="EventTypeCode" select="*"/>    
  </xsl:template>
  <xsl:template mode="EventIdentification" match="ActorStartStop">
    <strong>Application Activity</strong>
    <xsl:apply-templates mode="EventTypeCode" select="*"/>
  </xsl:template>
  <xsl:template mode="EventIdentification" match="AuditLogUsed">
    <strong>Audit Log Used</strong>
  </xsl:template>
  <xsl:template mode="EventIdentification" match="BeginStoringInstances">
    <strong>Begin Transferring DICOM Instances</strong>
  </xsl:template>
  <xsl:template mode="EventIdentification" match="DICOMInstancesDeleted">
    <strong>DICOM Instances Accessed</strong>
  </xsl:template>
  <xsl:template mode="EventIdentification" match="DICOMInstancesUsed">
    <strong>DICOM Instances Accessed</strong>
  </xsl:template>
  <xsl:template mode="EventIdentification" match="DicomQuery">
    <strong>Query</strong>
  </xsl:template>
  <xsl:template mode="EventIdentification" match="Export">
    <strong>Export</strong>
  </xsl:template>
  <xsl:template mode="EventIdentification" match="Import">
    <strong>Import</strong>
  </xsl:template>
  <xsl:template mode="EventIdentification" match="InstancesSent">
    <strong>DICOM Instances Transferred</strong>
    <xsl:apply-templates mode="EventTypeCode" select="*"/>
  </xsl:template>
  <xsl:template mode="EventIdentification" match="InstancesStored">
    <strong>DICOM Instances Transferred</strong>
  </xsl:template>
  <xsl:template mode="EventIdentification" match="NetworkEntry">
    <strong>NetworkEntry</strong>
    <xsl:apply-templates mode="EventTypeCode" select="*"/>
  </xsl:template>
  <xsl:template mode="EventIdentification" match="OrderRecord">
    <strong>Order Record</strong>
  </xsl:template>
  <xsl:template mode="EventIdentification" match="PatientRecord">
    <strong>Patient Record</strong>
  </xsl:template>
  <xsl:template mode="EventIdentification" match="ProcedureRecord">
    <strong>Procedure Record</strong>
  </xsl:template>
  <xsl:template mode="EventIdentification" match="SecurityAlert">
    <strong>Security Alert</strong>
    <xsl:apply-templates mode="EventTypeCode" select="*"/>
  </xsl:template>
  <xsl:template mode="EventIdentification" match="StudyDeleted">
    <strong>DICOM Study Deleted</strong>
  </xsl:template>
  <xsl:template mode="EventIdentification" match="UserAuthenticated">
    <strong>User Authentication</strong>
    <xsl:apply-templates mode="EventTypeCode" select="*"/>
  </xsl:template>
  <xsl:template mode="EventIdentification" match="*"/>
  <!-- =========================================== -->
  <!-- EventIdentification / EventTypeCode         -->
  <!-- =========================================== -->
  <xsl:template mode="EventTypeCode" match="Action">
    <xsl:variable name="action" select="normalize-space(.)"/>
    <xsl:choose>
      <xsl:when test="$action='Failure'">(Login)</xsl:when>
      <xsl:otherwise>
        <xsl:text>(</xsl:text>
        <xsl:value-of select="$action"/>
        <xsl:text>)</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template mode="EventTypeCode" match="AlertType">
    <xsl:variable name="alert" select="normalize-space(.)"/>
    <xsl:choose>
      <xsl:when test="$alert='NodeAuthenticationFailure'">(Node Authentication)</xsl:when>
      <xsl:otherwise>
        <xsl:text>(</xsl:text>
        <xsl:value-of select="$alert"/>
        <xsl:text>)</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template mode="EventTypeCode" match="ApplicationAction">
    <xsl:text>(Application&#160;</xsl:text>
    <xsl:value-of select="normalize-space(.)"/>
    <xsl:text>)</xsl:text>
  </xsl:template>
  <xsl:template mode="EventTypeCode" match="ConfigType">
    <xsl:variable name="config" select="normalize-space(.)"/>
    <xsl:choose>
      <xsl:when test="$config='Networking'">(Network Configuration)</xsl:when>
      <xsl:otherwise>
        <xsl:text>(</xsl:text>
        <xsl:value-of select="$config"/>
        <xsl:text>&#160;Configuration)</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template mode="EventTypeCode" match="MachineAction">
    <xsl:text>(</xsl:text>
    <xsl:value-of select="normalize-space(.)"/>
    <xsl:text>)</xsl:text>
  </xsl:template>
  <xsl:template mode="EventTypeCode" match="*"/>
  <!-- =========================================== -->
  <!-- Active Participant                          -->
  <!-- =========================================== -->
  <xsl:template mode="ActiveParticipant" match="ActorConfig">
    <xsl:apply-templates mode="ActiveParticipant" select="*"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="ActorName">
    <xsl:text>,&#32;</xsl:text>
    <strong>User:&#160;</strong>
    <xsl:value-of select="normalize-space(.)"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="ActorStartStop">
    <xsl:apply-templates mode="ActiveParticipant" select="*"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="AET">
    <xsl:text>, AETITLE=</xsl:text>
    <xsl:value-of select="normalize-space(.)"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="AuditLogUsed">
    <xsl:apply-templates mode="ActiveParticipant" select="*"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="BeginStoringInstances">
    <xsl:apply-templates mode="ActiveParticipant" select="*"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="Destination">
    <xsl:apply-templates mode="ActiveParticipant" select="*"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="DICOMInstancesDeleted">
    <xsl:apply-templates mode="ActiveParticipant" select="*"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="DICOMInstancesUsed">
    <xsl:apply-templates mode="ActiveParticipant" select="*"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="DicomQuery">
    <xsl:apply-templates mode="ActiveParticipant" select="*"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="Export">
    <xsl:apply-templates mode="ActiveParticipant" select="*"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="Hname">
    <xsl:text>,&#32;</xsl:text>
    <xsl:value-of select="normalize-space(.)"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="Import">
    <xsl:apply-templates mode="ActiveParticipant" select="*"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="InstanceActionDescription">
    <xsl:apply-templates mode="ActiveParticipant" select="*"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="InstancesSent">
    <xsl:apply-templates mode="ActiveParticipant" select="*"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="InstancesStored">
    <xsl:text>,&#32;</xsl:text>
    <strong>Req.User:&#160;</strong>
    <xsl:apply-templates mode="ActiveParticipant" select="RemoteNode"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="IP">
    <xsl:text>node=</xsl:text>
    <xsl:value-of select="normalize-space(.)"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="LocalPrinter">
    <xsl:text>,&#32;</xsl:text>
    <strong>User</strong>
    <xsl:text>(Destination Media):&#160;</xsl:text>
    <xsl:value-of select="normalize-space(.)"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="LocalUser">
    <xsl:value-of select="normalize-space(.)"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="LocalUsername">
    <xsl:text>,&#32;</xsl:text>
    <strong>Req.User:&#160;</strong>
    <xsl:value-of select="normalize-space(.)"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="MediaID">
    <xsl:text>,&#32;</xsl:text>
    <strong>User:&#160;</strong>
    <xsl:value-of select="normalize-space(.)"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="NetworkEntry">
    <xsl:text>,&#32;</xsl:text>
    <strong>User:&#160;</strong>
    <xsl:value-of select="normalize-space(../Host)"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="Node">
    <xsl:text>,&#32;</xsl:text>
    <strong>User:&#160;</strong>
    <xsl:apply-templates mode="ActiveParticipant" select="*"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="OrderRecord">
    <xsl:apply-templates mode="ActiveParticipant" select="*"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="PatientRecord">
    <xsl:apply-templates mode="ActiveParticipant" select="*"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="ProcedureRecord">
    <xsl:apply-templates mode="ActiveParticipant" select="*"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="RemoteNode">
    <xsl:apply-templates mode="ActiveParticipant" select="*"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="Requestor">
    <xsl:text>,&#32;</xsl:text>
    <strong>Req.User:&#160;</strong>
    <xsl:apply-templates mode="ActiveParticipant" select="*"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="RNode">
    <xsl:text>,&#32;</xsl:text>
    <strong>User:&#160;</strong>
    <xsl:apply-templates mode="ActiveParticipant" select="*"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="SecurityAlert">
    <xsl:apply-templates mode="ActiveParticipant" select="*"/>
    <xsl:text>,&#32;</xsl:text>
    <strong>User:&#160;</strong>
    <xsl:value-of select="normalize-space(../Host)"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="StudyDeleted">
    <xsl:apply-templates mode="ActiveParticipant" select="*"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="User">
    <xsl:text>,&#32;</xsl:text>
    <strong>Req.User:&#160;</strong>
    <xsl:apply-templates mode="ActiveParticipant" select="*"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="UserAuthenticated">
    <xsl:apply-templates mode="ActiveParticipant" select="*"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="*"/>
  <!-- =========================================== -->
  <!-- Active Participant / RoleIDCode             -->
  <!-- =========================================== -->
  <!-- =========================================== -->
  <!-- AuditSourceIdentification                   -->
  <!-- =========================================== -->
  <xsl:template mode="AuditSourceIdentification" match="Host">
    <xsl:text>,&#32;</xsl:text>
    <strong>Audit&#160;Source:&#160;</strong>
    <xsl:value-of select="normalize-space(../Host)"/>
  </xsl:template>
  <xsl:template mode="AuditSourceIdentification" match="*"/>
  <!-- =========================================== -->
  <!-- ParticipantObjectIdentification             -->
  <!-- =========================================== -->
  <xsl:template mode="ParticipantObjectIdentification"
    match="BeginStoringInstances">
    <xsl:apply-templates mode="ParticipantObjectIdentification" select="*"/>
  </xsl:template>
  <xsl:template mode="ParticipantObjectIdentification"
    match="DICOMInstancesDeleted">
    <xsl:apply-templates mode="ParticipantObjectIdentification" select="*"/>
  </xsl:template>
  <xsl:template mode="ParticipantObjectIdentification"
    match="DICOMInstancesUsed">
    <xsl:apply-templates mode="ParticipantObjectIdentification" select="*"/>
  </xsl:template>
  <!--
  <xsl:template mode="ParticipantObjectIdentification" match="DicomQuery">
    <xsl:text>,&#32;</xsl:text>
    <strong>Report:&#160;</strong>
    <span title="SOP Class UID"><xsl:value-of select="CUID"/> </span>
  </xsl:template>
  -->
  <xsl:template mode="ParticipantObjectIdentification" match="Export">
    <xsl:apply-templates mode="ParticipantObjectIdentification" select="*"/>
  </xsl:template>
  <xsl:template mode="ParticipantObjectIdentification" match="Import">
    <xsl:apply-templates mode="ParticipantObjectIdentification" select="*"/>
  </xsl:template>
  <xsl:template mode="ParticipantObjectIdentification"
    match="InstanceActionDescription">
    <xsl:apply-templates mode="ParticipantObjectIdentification" select="*"/>
  </xsl:template>
  <xsl:template mode="ParticipantObjectIdentification" match="InstancesSent">
    <xsl:apply-templates mode="ParticipantObjectIdentification" select="*"/>
  </xsl:template>
  <xsl:template mode="ParticipantObjectIdentification" match="InstancesStored">
    <xsl:apply-templates mode="ParticipantObjectIdentification" select="*"/>
  </xsl:template>
  <xsl:template mode="ParticipantObjectIdentification" match="OrderRecord">
    <xsl:apply-templates mode="ParticipantObjectIdentification" select="*"/>
  </xsl:template>
  <xsl:template mode="ParticipantObjectIdentification" match="Patient">
    <xsl:text>,&#32;</xsl:text>
    <strong>Patient:&#160;</strong>
    <xsl:apply-templates mode="ParticipantObjectIdentification"
      select="PatientID"/>
    <xsl:apply-templates mode="ParticipantObjectIdentification"
      select="PatientName"/>
    <xsl:apply-templates mode="ParticipantObjectIdentification" select="SUID"/>
  </xsl:template>
  <xsl:template mode="ParticipantObjectIdentification" match="PatientID">
    <xsl:text>id=</xsl:text>
    <xsl:value-of select="normalize-space(.)"/>
  </xsl:template>
  <xsl:template mode="ParticipantObjectIdentification" match="PatientName">
    <xsl:text>,&#32;name=</xsl:text>
    <xsl:value-of select="normalize-space(.)"/>
  </xsl:template>
  <xsl:template mode="ParticipantObjectIdentification" match="PatientRecord">
    <xsl:apply-templates mode="ParticipantObjectIdentification" select="*"/>
  </xsl:template>
  <xsl:template mode="ParticipantObjectIdentification" match="ProcedureRecord">
    <xsl:apply-templates mode="ParticipantObjectIdentification" select="*"/>
  </xsl:template>
  <xsl:template mode="ParticipantObjectIdentification" match="SecurityAlert">
    <xsl:text>,&#32;</xsl:text>
    <strong>Security&#160;Resource:&#160;</strong>
    <xsl:value-of select="normalize-space(../Host)"/>
  </xsl:template>
  <xsl:template mode="ParticipantObjectIdentification" match="StudyDeleted">
    <xsl:apply-templates mode="ParticipantObjectIdentification" select="*"/>
  </xsl:template>
  <xsl:template mode="ParticipantObjectIdentification" match="AccessionNumber">
    <xsl:text>,&#32;</xsl:text>
    <strong title="Accession Number">Acc.No:&#160;</strong>
    <xsl:value-of select="normalize-space(.)"/>
  </xsl:template>
  <xsl:template mode="ParticipantObjectIdentification" match="SUID">
    <xsl:text>,&#32;</xsl:text>
    <strong>Study:&#160;</strong>
    <xsl:text>uid=</xsl:text>
    <xsl:value-of select="normalize-space(.)"/>
  </xsl:template>
  <xsl:template mode="ParticipantObjectIdentification" match="NumberOfInstances">
    <xsl:text>,&#32;</xsl:text>
    <strong title="Number of Instances">NoI:&#160;</strong>
    <xsl:value-of select="normalize-space(.)"/>
  </xsl:template>  <xsl:template mode="ParticipantObjectIdentification" match="*"/>
  <!-- =========================================== -->
  <!-- Discard everything else                     -->
  <!-- =========================================== -->
  <xsl:template match="*"/>
</xsl:stylesheet>
