<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
<!ENTITY nbsp " ">
]>
<!--
 $Id: folder.xsl 1108 2004-05-05 11:59:18Z gunterze $
 -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="html" indent="yes" encoding="ISO-8859-1"/>
<xsl:variable name="page_title">Patient List</xsl:variable>
<xsl:include href="page.xsl"/>

<xsl:template match="model">
	<form action="foldersubmit.m" method="post" name="myForm">
		<table border="0" cellspacing="0" cellpadding="0" width="100%">
			<td valign="top">
				<table border="0" height="30" cellspacing="0" cellpadding="0" width="100%">
					<td bgcolor="eeeeee" align="center">Displaying studies
						<b>
							<xsl:value-of select="offset + 1"/>
						</b>
							to
						<b>
							<xsl:choose>
								<xsl:when test="offset + limit &lt; total">
									<xsl:value-of select="offset + limit"/>
								</xsl:when>
								<xsl:otherwise>
									<xsl:value-of select="total"/>												
								</xsl:otherwise>
							</xsl:choose>
						</b>
							of
						<b>
							<xsl:value-of select="total"/>
						</b>matching studies.
 					</td>

					<td width="150" bgcolor="eeeeee">
					</td>
					<td width="40" bgcolor="eeeeee">
						<input type="image" value="Search" name="filter" src="images/search.gif" border="0"/>
					</td>
					<td width="40" bgcolor="eeeeee">
						<input type="image" value="Prev" name="prev" src="images/preview.gif" alt="preview" border="0">
							<xsl:if test="offset = 0">
                  <xsl:attribute name="disabled">disabled</xsl:attribute>
							</xsl:if>
						</input>
					</td>
					<td width="40" bgcolor="eeeeee">
						<input type="image" value="Next" name="next" src="images/next.gif" alt="next" border="0">
							<xsl:if test="offset + limit &gt;= total">
								<xsl:attribute name="disabled">disabled</xsl:attribute>
							</xsl:if>
						</input>
					</td>
					<td width="40" bgcolor="eeeeee">
						<input type="image" value="Merge" name="merge" src="images/merge.gif" alt="merge" border="0" onclick="return validateChecks(this.form.stickyPat, 'Patient', 2)">
							<xsl:if test="total &lt;= 0">
								<xsl:attribute name="disabled">disabled</xsl:attribute>
							</xsl:if>
						</input>
					</td>
					<td width="40" bgcolor="eeeeee">
						<input type="image" value="Del" name="del" src="images/loeschen.gif" alt="delete" border="0" onclick="return confirm('Are you sure you want to delete?')">
							<xsl:if test="total &lt;= 0">
								<xsl:attribute name="disabled">disabled</xsl:attribute>
							</xsl:if>
						</input>
					</td>
				</table>
				<table border="0" width="100%" cellpadding="0" cellspacing="0">
					<td bgcolor="eeeeee" class="label">Patient Name:
					</td>
					<td bgcolor="eeeeee">
						<input size="10" name="patientName" type="text" value="{patientName}"/>
      		</td>
					<td bgcolor="eeeeee" class="label">Patient ID:
					</td>
					<td bgcolor="eeeeee">
						<input size="10" name="patientID" type="text" value="{patientID}"/>
					</td>
					<td bgcolor="eeeeee" class="label">Study ID:
					</td>
      		<td bgcolor="eeeeee">
        		<input size="10" name="studyID" type="text" value="{studyID}"/>
      		</td>
					<td bgcolor="eeeeee" class="label">Study Date:
					</td>
      		<td bgcolor="eeeeee"> 
        		<input size="10" name="studyDateRange" type="text" value="{studyDateRange}"/>
      		</td>
      		<td bgcolor="eeeeee" class="label">Accession No.:
					</td>
      		<td bgcolor="eeeeee">
        		<input size="10" name="accessionNumber" type="text" value="{accessionNumber}"/>
      		</td>
      		<td bgcolor="eeeeee" class="label">Modality:
					</td>
      		<td bgcolor="eeeeee">
        		<input size="10" name="modality" type="text" value="{modality}"/>
      		</td>
				</table>
					<xsl:call-template name="overview"/>
   			<table border="0" cellpadding="0" cellspacing="0" width="100%">
					<tr>
						<td>
							<table border="0" cellpadding="0" cellspacing="0" width="100%">
								<tbody valign="top">
									<xsl:apply-templates select="patients/item"/>
								</tbody>
							</table>
						</td>
					</tr>
				</table>
			</td>
	</table>
	</form>
</xsl:template>

<xsl:template name="overview">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
	<table border="0" cellpadding="0" cellspacing="0" width="100%">
		<colgroup>
			<col width="5%"/>
			<col width="25%"/>
			<col width="15%"/>
			<col width="24%"/>
			<col width="5%"/>
			<col width="23%"/>
			<col width="3%"/>
		</colgroup>
		<tr bgcolor="eeeeee">
			<td bgcolor="cccccc">
				<font size="1">
					Patient:</font>
			</td>
			<td>
				<font size="1" color="ff0000">
					 Name:</font>
			</td>
    	<td>
				<font size="1" color="ff0000">
					ID:</font>
    	</td>
			<td>
				<font size="1" color="ff0000">
					Birthdate:</font>
			</td>
    	<td>
				<font size="1" color="ff0000">
					Sex:</font>
    	</td>
			<td>
			</td>
			<td bgcolor="cccccc">
			</td>
			</tr>
	</table>	
	
	<table border="0" cellspacing="0" cellpadding="0" width="100%">
		<colgroup>
			<col width="5%"/>
			<col width="12%"/>
			<col width="15%"/>
			<col width="8%"/>
			<col width="25%"/>
			<col width="14%"/>
			<col width="10%"/>
			<col width="4%"/>
			<col width="4%"/>
			<col width="3%"/>

</colgroup>
		<tr bgcolor="eeeeee">
			<td bgcolor="ccccff">
				<font size="1">
					Study:</font>
			</td>
			<td>
				<font size="1" color="ff0000">
					 Date/Time:</font>
			</td>
			<td>
				<font size="1" color="ff0000">
					ID:</font>
			</td>
			<td>
				<font size="1" color="ff0000">
					Mods:</font>
			</td>
			<td>
				<font size="1" color="ff0000">
					Description:</font>
			</td>
			<td>
				<font size="1" color="ff0000">
					Ref. Physican:</font>
			</td>
			<td>
				<font size="1" color="ff0000">
					Acc.Nr.:</font>
			</td>
			<td>
				<font size="1" color="ff0000">
					NoS:</font>
			</td>
			<td>
				<font size="1" color="ff0000">
					NoI:</font>
			</td>
			<td bgcolor="ccccff"> 
			</td>
		</tr>
	</table>
	
	<table border="0" cellspacing="0" cellpadding="0" width="100%">
		<colgroup>
			<col width="5%"/>
			<col width="15%"/>
			<col width="12%"/>
			<col width="8%"/>
			<col width="34%"/>
			<col width="19%"/>
			<col width="4%"/>
			<col width="3%"/>
		</colgroup>
		<tr bgcolor="eeeeee">
			<td bgcolor="ccffcc">
				<font size="1">
					Serie:</font>
			</td>
			<td>
				<font size="1" color="ff0000">
					 Date/Time:</font>
			</td>
			<td>
				<font size="1" color="ff0000">
					No:</font>
			</td>
			<td>
				<font size="1" color="ff0000">
					Mod:</font>			
			</td>
			<td>
				<font size="1" color="ff0000">
					Description/Body Part:</font>
			</td>
			<td>
				<font size="1" color="ff0000">
					Vendor/Model:</font>
			</td>
			<td>
				<font size="1" color="ff0000">
					NoI:</font>
			</td>
			<td bgcolor="ccffcc"> 
			</td>
		</tr>
	</table>
</table>
</xsl:template>


<xsl:template match="item[@type='org.dcm4chex.archive.ejb.interfaces.PatientDTO']">
	<tr>
      <table width="100%" callpadding="1" callspacing="1" border="0">
		<colgroup>
			<col width="1%"/>
			<col width="29%"/>
			<col width="15%"/>
			<col width="25%"/>
			<col width="5%"/>
			<col width="20%"/>
			<col width="3%"/>
			<col width="2%"/>
		</colgroup>
		<xsl:variable name="rowspan" select="1+count(descendant::item)"/>
			<td align="right" bgcolor="cccccc" rowspan="{$rowspan}">
				<xsl:choose>
					<xsl:when test="$rowspan=1">
						<a title="Show Studies" href="expandPat.m?patPk={pk}">
						<img src="images/plus.gif" border="0" alt="+"/>
              </a>				
					</xsl:when>
					<xsl:otherwise>
							<a title="Hide Studies" href="collapsePat.m?patPk={pk}">							
							<img src="images/minus.gif" border="0" alt="-"/>
              </a>				
					</xsl:otherwise>
				</xsl:choose>
			</td>
      <td title="Patient Name">
				<strong>
            <xsl:value-of select="patientName"/>
				</strong>
      </td>
      <td title="Patient ID">
				<strong>
            <xsl:value-of select="patientID"/>
				</strong>
			</td>
      <td title="Birth Date">
				<strong>
            <xsl:value-of select="patientBirthDate"/>
				</strong>
      </td>
      <td title="Patient Sex">
				<strong>
            <xsl:value-of select="patientSex"/>
				</strong>
      </td>
      <td>
      </td>
			<td align="right">
			  <a href="patientEdit.m?pk={pk}">
            <font size="1">Edit</font>
          </a> 				  
			</td>
			<td align="right" bgcolor="cccccc">
				<input type="checkbox" name="stickyPat" value="{pk}">
					<xsl:if test="/model/stickyPatients/item = pk">
						<xsl:attribute name="checked"/>
					</xsl:if>
				</input>
			</td>
	</table>
</tr>
			<xsl:apply-templates select="studies/item">
				<xsl:sort data-type="text" order="ascending" select="studyDateTime"/>
			</xsl:apply-templates>
</xsl:template>

<xsl:template match="item[@type='org.dcm4chex.archive.ejb.interfaces.StudyDTO']">
<tr>
	<table width="100%" callpadding="0" callspacing="0" border="0">
		<xsl:variable name="rowspan" select="1+count(descendant::item)"/>
		<colgroup>
			<col width="2%"/>
			<col width="15%"/>
			<col width="15%"/>
			<col width="8%"/>
			<col width="25%"/>
			<col width="15%"/>
			<col width="10%"/>
			<col width="4%"/>
			<col width="4%"/>
			<col width="2%"/>
		</colgroup>
			<td align="right" bgcolor="ccccff" rowspan="{$rowspan}">
				<xsl:choose>
					<xsl:when test="$rowspan=1">
						<a title="Show Series" href="expandStudy.m?patPk={../../pk}&amp;studyPk={pk}">
						<img src="images/plus.gif" border="0" alt="+"/>
              </a>				
					</xsl:when>
					<xsl:otherwise>
						<a title="Hide Series" href="collapseStudy.m?patPk={../../pk}&amp;studyPk={pk}">							
						<img src="images/minus.gif" border="0" alt="-"/>
              </a>				
					</xsl:otherwise>
				</xsl:choose>
			</td>
      <td title="Study Date">
				<xsl:value-of select="studyDateTime"/>
			</td>
			<td title="Study ID">
				<xsl:value-of select="studyID"/>
			</td>
		 	<td title="Modalities">
				<xsl:value-of select="modalitiesInStudy"/>
			</td>
      <td title="Study Description">
				<xsl:value-of select="studyDescription"/>
			</td>
      <td title="Referring Physican">
				<xsl:value-of select="referringPhysician"/>
			</td>
			<td title="Accession Number">
				<xsl:value-of select="accessionNumber"/>
			</td>
      <td title="Number of Series">
				<xsl:value-of select="numberOfSeries"/>
			</td>
      <td title="Number of Instances">
				<xsl:value-of select="numberOfInstances"/>
			</td>
			<td align="right" bgcolor="ccccff">
				<input type="checkbox" name="stickyStudy" value="{pk}">
					<xsl:if test="/model/stickyStudies/item = pk">
						<xsl:attribute name="checked"/>
					</xsl:if>
				</input>
			</td>
	</table>
</tr>
	<xsl:apply-templates select="series/item">
		<xsl:sort data-type="number" order="ascending" select="seriesNumber"/>
	</xsl:apply-templates>
</xsl:template>

<xsl:template match="item[@type='org.dcm4chex.archive.ejb.interfaces.SeriesDTO']">
	<tr>
<table width="100%" callpadding="0" callspacing="0" border="0">	  
		<colgroup>
			<col width="3%"/>
			<col width="17%"/>
			<col width="12%"/>
			<col width="8%"/>
			<col width="34%"/>
			<col width="20%"/>
			<col width="4%"/>
			<col width="2%"/>
		</colgroup>
		<xsl:variable name="rowspan" select="1+count(descendant::item)"/>
		  <td align="right" bgcolor="ccffcc" rowspan="{$rowspan}">
				<xsl:choose>
					<xsl:when test="$rowspan=1">
		  			<a title="Show Instances" href="expandSeries.m?patPk={../../../../pk}&amp;studyPk={../../pk}&amp;seriesPk={pk}">
						<img src="images/plus.gif" border="0" alt="+"/>
              </a>				
					</xsl:when>
					<xsl:otherwise>
		  			<a title="Hide Instances" href="collapseSeries.m?patPk={../../../../pk}&amp;studyPk={../../pk}&amp;seriesPk={pk}">
						<img src="images/minus.gif" border="0" alt="-"/>
              </a>				
					</xsl:otherwise>
				</xsl:choose>
			</td>
			<td title="Series Date">
				<xsl:value-of select="seriesDateTime"/>
			</td>
			<td title="Series Number">
			<xsl:value-of select="seriesNumber"/>
			</td>
      <td title="Modality">
				<xsl:value-of select="modality"/>
			</td>
      <td title="Series Description / Body Part">
    		<xsl:value-of select="seriesDescription"/>
				\ <xsl:value-of select="bodyPartExamined"/>
      </td>
			<td title="Modality Vendors / Modelname">
    		<xsl:value-of select="manufacturer"/>
				\ <xsl:value-of select="manufacturerModelName"/>
      </td>
			<td title="Number of Instances">
				<xsl:value-of select="numberOfInstances"/>
			</td>
			<td align="right" bgcolor="ccffcc">
				<input type="checkbox" name="stickySeries" value="{pk}">
					<xsl:if test="/model/stickySeries/item = pk">
						<xsl:attribute name="checked"/>
					</xsl:if>
				</input>
			</td>
      </table>
	</tr>
		<xsl:apply-templates select="instances/item">
			<xsl:sort data-type="number" order="ascending" select="instanceNumber"/>
		</xsl:apply-templates>
</xsl:template>

<xsl:template match="item[@type='org.dcm4chex.archive.ejb.interfaces.ImageDTO']">
	<tr>
<table width="100%" callpadding="0" callspacing="0" border="0">
		<colgroup>
			<col width="4%"/>
			<col width="10%"/>
			<col width="3%"/>
			<col width="21%"/>
			<col width="25%"/>
			<col width="5%"/>
			<col width="30%"/>
			<col width="2%"/>
		</colgroup>
		<td bgcolor="ffffcc">
		</td>
		<td title="Content Datetime">
			<xsl:value-of select="contentDateTime"/>
		</td>
		<td title="Instance Number">
	   		<xsl:value-of select="instanceNumber"/>
    </td>
    <td title="ImageType">
			<xsl:value-of select="imageType"/>
		</td>
    <td title="Pixel Matrix">
	    	<xsl:value-of select="photometricInterpretation"/>
				 
    		<xsl:value-of select="rows"/>x<xsl:value-of select="columns"/>x<xsl:value-of select="numberOfFrames"/>
				 
    		<xsl:value-of select="bitsAllocated"/>bits
    </td>
		<td title="Number of Files">
			<xsl:value-of select="numberOfFiles"/>
		</td>
	  <td title="Retrieve AETs">
			<xsl:value-of select="retrieveAETs"/>
    </td>
		<td align="right" bgcolor="ffffcc">
			<input type="checkbox" name="stickyInst" value="{pk}">
				<xsl:if test="/model/stickyInstances/item = pk">
					<xsl:attribute name="checked"/>
				</xsl:if>
			</input>
		</td>
      </table>
	</tr>
</xsl:template>

<xsl:template match="item[@type='org.dcm4chex.archive.ejb.interfaces.PresentationStateDTO']">
	<tr>
<table width="100%" callpadding="0" callspacing="0" border="0">	
		<colgroup>
			<col width="4%"/>
			<col width="15%"/>
			<col width="6%"/>
			<col width="20%"/>
			<col width="5%"/>
			<col width="5%"/>
			<col width="5%"/>
			<col width="38%"/>
			<col width="2%"/>
		</colgroup>
		<td bgcolor="ffffcc">
		</td>
		<td title="Creation Datetime">
      <xsl:value-of select="presentationCreationDateTime"/>
		</td>
		<td title="Instance Number">
			<xsl:value-of select="instanceNumber"/>
    </td>
    <td title="Presentation Description">
      <xsl:value-of select="presentationDescription"/>
		</td>
		<td title="Presentation Label">
    	<xsl:value-of select="presentationLabel"/>
		</td>
		<td title="Number of Referenced Images">
      -&gt;<xsl:value-of select="numberOfReferencedImages"/>
		</td>
		<td title="Number of Files">
      <xsl:value-of select="numberOfFiles"/>
		</td>
		<td title="Retrieve AETs">
      <xsl:value-of select="retrieveAETs"/>
    </td>
		<td align="right" bgcolor="ffffcc">
			<input type="checkbox" name="stickyInst" value="{pk}">
				<xsl:if test="/model/stickyInstances/item = pk">
					<xsl:attribute name="checked"/>
				</xsl:if>
			</input>
		</td>
      </table>
	</tr>
</xsl:template>

<xsl:template match="item[@type='org.dcm4chex.archive.ejb.interfaces.StructuredReportDTO']">
	<tr>
<table width="100%" callpadding="1" callspacing="0" border="0">		 
		<colgroup>
			<col width="4%"/>
			<col width="15%"/>
			<col width="6%"/>
			<col width="15%"/>
			<col width="15%"/>
			<col width="5%"/>
			<col width="38%"/>
			<col width="2%"/>
		</colgroup>
 
		<td bgcolor="ffffcc">
		</td>
		<td title="Content Datetime">
    	<xsl:value-of select="contentDateTime"/>
		</td>
		<td title="Instance Number">
  		<xsl:value-of select="instanceNumber"/>
    </td>
    <td title="Document Title">
  		<xsl:value-of select="documentTitle"/>
		</td>
		<td title="Document Status">
      <xsl:value-of select="completionFlag"/>/<xsl:value-of select="verificationFlag"/>
    </td>
		<td title="Number of Files">
			<xsl:value-of select="numberOfFiles"/>
		</td>
	  <td title="Retrieve AETs">
			<xsl:value-of select="retrieveAETs"/>
    </td>
		<td align="right" bgcolor="ffffcc">
			<input type="checkbox" name="stickyInst" value="{pk}">
				<xsl:if test="/model/stickyInstances/item = pk">
					<xsl:attribute name="checked"/>
				</xsl:if>
			</input>
		</td>
</table>
	</tr>
</xsl:template>
</xsl:stylesheet>
