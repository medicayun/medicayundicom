<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
<!ENTITY nbsp "&#160;">
]>
<xsl:stylesheet version="1.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output method="html" indent="yes" encoding="ISO-8859-1" />
<xsl:variable name="page_title">Patient Merge</xsl:variable>
<xsl:include href  = "page.xsl" />

<xsl:template match="model">
	<form action="patientMerge.m" method="post">
		<table border="0" cellspacing="0" cellpadding="0" width="90%">
			<tr>
				<td>
					<center>Select dominant Patient
:</center>
	<center>
		<table border="0" width="500">
			<tr>
				<td class="label" title="Patient ID" >
				</td>
			</tr>	
			<tr>
				<th title="Patient ID" >Patient ID</th>
				<th title="Patient Name" >Patient Name</th>
				<th title="Patient Sex">Patient Sex</th>
				<th title="Birth Date">Birth Date</th>
				<th title="Patient ID" >Patient ID</th>
			</tr>	
				<xsl:apply-templates select="patients/item"/>
		  <tr>
        <td colspan="5" align="center">
      		<input type="submit" name="merge" value="Merge" onclick="return validateRadios(this.form.pk,'Patient')" />
	        <input type="submit" name="cancel" value="Cancel" />
    	  </td>
      </tr>
	  </table>
	</center>
       	</td>
			</tr>
		</table>
	</form>
</xsl:template>

<xsl:template match="item[@type='org.dcm4chex.archive.ejb.interfaces.PatientDTO']">
	<xsl:if test="/model/stickyPatients/item = pk">
		<center>
			<input type="hidden" name="toBeMerged" value="{pk}"/>
				<tr bgcolor="eeeeee">
	        <td title="Patient ID" >
						<xsl:value-of select="patientID"/>
					</td>
	        <td title="Patient Name" >
						<xsl:value-of select="patientName"/>
	        </td>
	        <td title="Patient Sex">
						<xsl:value-of select="patientSex"/>
	        </td>
	        <td title="Birth Date">
						<xsl:value-of select="patientBirthDate"/>
	        </td>
	        <td title="Patient ID" align="center">
						<input type="radio" name="pk" value="{pk}">
						</input>
					</td>
				</tr>
			</center>	
		</xsl:if>
	</xsl:template>
</xsl:stylesheet>

