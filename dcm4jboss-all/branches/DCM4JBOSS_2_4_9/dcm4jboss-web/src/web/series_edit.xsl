<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:internal="urn:my-internal-data" version="1.0">
  <xsl:output method="html" indent="yes" encoding="ISO-8859-1"/>
  <xsl:variable name="page_title">Series Edit</xsl:variable>
  <xsl:include href="page.xsl"/>
  <xsl:template match="model">
    <form action="seriesUpdate.m" method="post">
      <input name="patPk" type="hidden" value="{patPk}"/>
      <input name="studyPk" type="hidden" value="{studyPk}"/>
      <input name="seriesPk" type="hidden" value="{seriesPk}"/>
      <table border="1" cellspacing="0" cellpadding="0" width="100%">
        <tr>
          <td>
            <table border="0">
              <xsl:apply-templates select="patient"/>
              <xsl:apply-templates select="study"/>
              <xsl:apply-templates select="series"/>
            </table>
          </td>
        </tr>
      </table>
    </form>
  </xsl:template>
  <xsl:template match="patient">
    <tr>
      <td class="label" bgcolor="#eeeeee">Patient ID:</td>
      <td>
        <xsl:value-of select="patientID"/>
      </td>
    </tr>
    <tr>
      <td class="label" bgcolor="#eeeeee">Issuer of Patient ID:</td>
      <td>
        <xsl:value-of select="issuerOfPatientID"/>
      </td>
    </tr>
    <tr>
      <td class="label" bgcolor="#eeeeee">Patient Name:</td>
      <td>
        <xsl:value-of select="patientName"/>
      </td>
    </tr>
  </xsl:template>
  <xsl:template match="study">
    <tr>
      <td class="label" bgcolor="#eeeeee">Study ID:</td>
      <td>
        <xsl:value-of select="studyID"/>
      </td>
    </tr>
    <tr>
      <td class="label" bgcolor="#eeeeee">Accession Number:</td>
      <td>
        <xsl:value-of select="accessionNumber"/>
      </td>
    </tr>
    <tr>
      <td class="label" bgcolor="#eeeeee">Study Date/Time:</td>
      <td>
        <xsl:value-of select="studyDateTime"/>
      </td>
    </tr>
    <tr>
      <td class="label" bgcolor="#eeeeee">Study Description:</td>
      <td>
        <xsl:value-of select="studyDescription"/>
      </td>
    </tr>
  </xsl:template>
  <xsl:template match="series">
    <tr>
      <td class="label" bgcolor="#eeeeee">Series Instance UID:</td>
      <td>
        <xsl:value-of select="seriesIUID"/>
      </td>
    </tr>
    <tr>
      <td class="label" bgcolor="#eeeeee">Series Number:</td>
      <td>
        <input size="16" name="seriesNumber" type="text" value="{seriesNumber}"/>
      </td>
    </tr>
    <tr>
      <td class="label" bgcolor="#eeeeee">Modality:</td>
      <td>
        <input size="16" name="modality" type="text" value="{modality}"/>
      </td>
    </tr>
    <tr>
      <td class="label" bgcolor="#eeeeee">Body Part Examined:</td>
      <td>
        <input size="16" name="bodyPartExamined" type="text" value="{bodyPartExamined}"/>
      </td>
    </tr>
    <tr>
      <td class="label" bgcolor="#eeeeee">Laterality:</td>
      <td>
        <input size="16" name="laterality" type="text" value="{laterality}"/>
      </td>
    </tr>
    <tr>
      <td class="label" bgcolor="#eeeeee">Series Description:</td>
      <td>
        <input size="64" name="seriesDescription" type="text" value="{seriesDescription}"/>
      </td>
    </tr>
    <tr>
      <td class="label" bgcolor="#eeeeee">Date/Time [yyyy/mm/dd hh:mm:ss]:</td>
      <td>
        <input size="20" name="seriesDateTime" type="text" value="{seriesDateTime}"/>
      </td>
    </tr>
    <tr>
      <td class="label" bgcolor="#eeeeee">Number of Instances:</td>
      <td>
        <xsl:value-of select="numberOfInstances"/>
      </td>
    </tr>
    <tr>
      <td class="label" bgcolor="#eeeeee">Availability:</td>
      <td>
        <xsl:value-of select="availability"/>
      </td>
    </tr>
    <tr>
      <td class="label" bgcolor="#eeeeee">Retrieve AETs:</td>
      <td>
        <xsl:value-of select="retrieveAETs"/>
      </td>
    </tr>
    <tr>
      <td align="left">
	    <xsl:choose>
		 	<xsl:when test="../seriesPk = -1">
				<input type="submit" name="submit" value="Add Series" onclick="return checkNotEmpty(this.form.modality.value, 'Modality')" />									
			</xsl:when>
			<xsl:otherwise>
				<input type="submit" name="submit" value="Update" onclick="return checkNotEmpty(this.form.modality.value, 'Modality')" />
			</xsl:otherwise>
	    </xsl:choose>
        <input type="submit" name="cancel" value="Cancel"/>
      </td>
    </tr>
  </xsl:template>
</xsl:stylesheet>
