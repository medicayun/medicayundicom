<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
<!ENTITY nbsp "&#160;">
]>
<!--
	$Id: folder.xsl 981 2004-02-18 14:51:16Z umbertoc $
-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:output method="html" indent="yes"
	encoding="ISO-8859-1"
	/>
	
	<xsl:template match="/">
			<html>
			<head>
				<title>Study Navigator</title>
				<link rel="stylesheet" href="stylesheet.css" type="text/css"/>
				<script language="JavaScript">window.name="folder";</script>				
			</head>
			<body>
			  <xsl:apply-templates select="model"/>
		  </body>
		</html>
	</xsl:template>
	
	<xsl:template match="model">
			<form action="foldersubmit.m" method="post">
			<table border="0" cellspacing="0" cellpadding="0" width="100%">
				<tr>
					<td valign="top" width="1%">
						<xsl:apply-templates select="studyFilter"/>
					</td>
					<td valign="top">
						<table border="0" cellspacing="0" cellpadding="0" width="100%">
							<tr>
								<td align="center">Displaying studies
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
									</b>
									matching studies.
								</td>
								<td align="right">
								<input type="submit" name="prev" value="Prev">
								<xsl:if test="offset = 0">
									<xsl:attribute name="disabled">disabled</xsl:attribute>
								</xsl:if>
								</input>
								<input type="submit" name="next" value="Next">
								<xsl:if test="offset + limit &gt;= total">
									<xsl:attribute name="disabled">disabled</xsl:attribute>
								</xsl:if>
								</input>
							  <input type="submit" name="send" value="Send" disabled="disabled"/>
							  <input type="submit" name="move" value="Move" disabled="disabled"/>
							  <input type="submit" name="merge" value="Merge" disabled="disabled"/>
							  <input type="submit" name="del" value="Del" disabled="disabled"/>
								</td>
							</tr>
						</table>
						<table bgcolor="#bbbbbb" border="0" cellpadding="0" cellspacing="0" width="100%">
							<tr>
								<td>
									<table border="0" cellpadding="3" cellspacing="1" width="100%">
										<tbody valign="top">
											<xsl:apply-templates select="patients/item"/>
										</tbody>
									</table>
								</td>
							</tr>
						</table>
					</td>
				</tr>
			</table>
		</form>
	</xsl:template>

	<xsl:template match="studyFilter">
	<table bgcolor="#eeeeee" border="0" width="100%">
		<tr>
			<td>
				<b>Filter:</b>
			</td>
			<td align="right">
				<input type="submit" name="filter" value="Search"/>
			</td>
		</tr>
	</table>
	<table bgcolor="#eeeeee" border="0" width="100%">
		<tr>
          <td class="label">Patient ID:</td>
        </tr>
		<tr>
          <td>
            <input size="15" name="patientID" type="text" value="{patientID}"/>
          </td>
        </tr>
		<tr>
          <td class="label">Patient Name:</td>
        </tr>
		<tr>
          <td>
            <input size="15" name="patientName" type="text" value="{patientName}"/>
          </td>
        </tr>
		<tr>
          <td class="label">Study ID:</td>
        </tr>
		<tr>
          <td>
            <input size="15" name="studyID" type="text" value="{studyID}"/>
          </td>
        </tr>
		<tr>
          <td class="label">Study Date:</td>
        </tr>
		<tr>
          <td>
            <input size="15" name="studyDateRange" type="text" value="{studyDateRange}"/>
          </td>
        </tr>
		<tr>
          <td class="label">Accession No.:</td>
        </tr>
		<tr>
          <td>
            <input size="15" name="accessionNumber" type="text" value="{accessionNumber}"/>
          </td>
        </tr>
		<tr>
          <td class="label">Modality:</td>
        </tr>
		<tr>
          <td>
            <input size="15" name="modality" type="text" value="{modality}"/>
          </td>
        </tr>
  </table>
	</xsl:template>

	<xsl:template match="item[@type='org.dcm4chex.archive.ejb.interfaces.PatientDTO']">
			<tr bgcolor="#eeeeee">
				<xsl:variable name="rowspan" select="1+count(descendant::item)"/>
				<td width="1%" rowspan="{$rowspan}">
				<xsl:choose>
					<xsl:when test="$rowspan=1">
						<a title="Show Studies" href="expandPat.m?patPk={pk}">
						<img src="images/icons/collapsed.gif" width="16" height="16" border="0" alt="+"/></a>				
					</xsl:when>
					<xsl:otherwise>
						<a title="Hide Studies" href="collapsePat.m?patPk={pk}">							
						<img src="images/icons/expanded.gif" width="16" height="16" border="0" alt="-"/></a>				
					</xsl:otherwise>
				</xsl:choose>
				</td>
        <td colspan="3" title="Patient ID" >
					<xsl:value-of select="patientID"/>
				</td>
        <td colspan="2" title="Patient Name" >
					<xsl:value-of select="patientName"/>
        </td>
        <td width="1%" title="Patient Sex">
					<xsl:value-of select="patientSex"/>
        </td>
        <td width="1%" colspan="3" title="Birth Date">
					<xsl:value-of select="patientBirthDate"/>
        </td>
 				<td>
				  <a href="patientEdit.m?pk={pk}" >Edit</a> 				  
        </td>
				<td>
					<input type="checkbox" name="stickyPat" value="{pk}">
						<xsl:if test="/model/stickyPatients/item = pk">
							<xsl:attribute name="checked"/>
						</xsl:if>
					</input>
			</td>
			</tr>
			<xsl:apply-templates select="studies/item">
				<xsl:sort data-type="text" order="ascending" select="studyDateTime"/>
			</xsl:apply-templates>
	</xsl:template>

	<xsl:template match="item[@type='org.dcm4chex.archive.ejb.interfaces.StudyDTO']">
		<tr bgcolor="#ffffff">
			<xsl:variable name="rowspan" select="1+count(descendant::item)"/>
		  <td width="1%" rowspan="{$rowspan}">
				<xsl:choose>
					<xsl:when test="$rowspan=1">
						<a title="Show Series" href="expandStudy.m?patPk={../../pk}&amp;studyPk={pk}">
						<img src="images/icons/collapsed.gif" width="16" height="16" border="0" alt="+"/></a>				
					</xsl:when>
					<xsl:otherwise>
						<a title="Hide Series" href="collapseStudy.m?patPk={../../pk}&amp;studyPk={pk}">							
						<img src="images/icons/expanded.gif" width="16" height="16" border="0" alt="-"/></a>				
					</xsl:otherwise>
				</xsl:choose>
			</td>
      <td colspan="2" title="Study ID">
        <xsl:value-of select="studyID"/>
			</td>
      <td width="1%" title="Study Date">
        <xsl:value-of select="studyDateTime"/>
			</td>
      <td colspan="1" title="Study Description">
        <xsl:value-of select="studyDescription"/>
			</td>
			<td width="1%" title="Accession Number">
        <xsl:value-of select="accessionNumber"/>
			</td>
		 	<td width="1%"  title="Modalities">
        <xsl:value-of select="modalitiesInStudy"/>
			</td>
      <td width="1%" title="Number of Series\Instances">
      	<xsl:value-of select="numberOfSeries"/>\<xsl:value-of select="numberOfInstances"/>
			</td>
		  <td width="1%" title="Retrieve AETs">
        <xsl:value-of select="retrieveAETs"/>
      </td>
				<td width="1%">
				  <a href="javascript:alert('Edit Study not yet implemented')">Edit</a>
      </td>
				<td width="1%">
					<input type="checkbox" name="stickyStudy" value="{pk}">
						<xsl:if test="/model/stickyStudies/item = pk">
							<xsl:attribute name="checked"/>
						</xsl:if>
					</input>
			</td>
		</tr>
		<xsl:apply-templates select="series/item">
			<xsl:sort data-type="number" order="ascending" select="seriesNumber"/>
		</xsl:apply-templates>
	</xsl:template>

	<xsl:template match="item[@type='org.dcm4chex.archive.ejb.interfaces.SeriesDTO']">
		<tr bgcolor="#eeeeee">		  
			<xsl:variable name="rowspan" select="1+count(descendant::item)"/>
		  <td width="1%" rowspan="{$rowspan}">
				<xsl:choose>
					<xsl:when test="$rowspan=1">
					  <a title="Show Instances" href="expandSeries.m?patPk={../../../../pk}&amp;studyPk={../../pk}&amp;seriesPk={pk}">
						<img src="images/icons/collapsed.gif" width="16" height="16" border="0" alt="+"/></a>				
					</xsl:when>
					<xsl:otherwise>
					  <a title="Hide Instances" href="collapseSeries.m?patPk={../../../../pk}&amp;studyPk={../../pk}&amp;seriesPk={pk}">
						<img src="images/icons/expanded.gif" width="16" height="16" border="0" alt="-"/></a>				
					</xsl:otherwise>
				</xsl:choose>
			</td>
      <td title="Series Number">
				<xsl:value-of select="seriesNumber"/>
			</td>
      <td width="1%" title="Series Date">
				<xsl:value-of select="seriesDateTime"/>
			</td>
      <td colspan="1" title="Series Description">
        <xsl:value-of select="seriesDescription"/>
      </td>
      <td width="1%" title="Body Part">
					<xsl:value-of select="bodyPartExamined"/>
					&nbsp;
					<xsl:value-of select="laterality"/>
      </td>
			<td width="1%" title="Modality">
        <xsl:value-of select="modality"/>
      </td>
			<td width="1%" title="Number of Instances">
        <xsl:value-of select="numberOfInstances"/>
      </td>
		  <td width="1%" title="Retrieve AETs">
        <xsl:value-of select="retrieveAETs"/>
      </td>
			<td width="1%">
				  <a href="javascript:alert('Edit Series not yet implemented')">Edit</a>
      </td>
				<td width="1%">
					<input type="checkbox" name="stickySeries" value="{pk}">
						<xsl:if test="/model/stickySeries/item = pk">
							<xsl:attribute name="checked"/>
						</xsl:if>
					</input>
			</td>
		</tr>
		<xsl:apply-templates select="instances/item">
			<xsl:sort data-type="number" order="ascending" select="instanceNumber"/>
		</xsl:apply-templates>
	</xsl:template>

	<xsl:template match="item[@type='org.dcm4chex.archive.ejb.interfaces.ImageDTO']">
		<tr bgcolor="#ffffff">		  
			<td title="Instance Number">
        <xsl:value-of select="instanceNumber"/>
      </td>
			<td width="1%" title="Content Datetime">
        <xsl:value-of select="contentDateTime"/>
			</td>
      <td title="ImageType">
        <xsl:value-of select="imageType"/>
			</td>
      <td width="1%" colspan="2" title="Pixel Matrix">
        <xsl:value-of select="photometricInterpretation"/>
				&nbsp;
        <xsl:value-of select="rows"/>x<xsl:value-of select="columns"/>x<xsl:value-of select="numberOfFrames"/>
				&nbsp;
        <xsl:value-of select="bitsAllocated"/>bits
      </td>
			<td width="1%" title="Number of Files">
        <xsl:value-of select="numberOfFiles"/>
			</td>
		  <td width="1%" title="Retrieve AETs">
        <xsl:value-of select="retrieveAETs"/>
      </td>
			<td width="1%">
				  <a href="javascript:alert('Edit Image not implemented')">Edit</a>
      </td>
				<td width="1%">
					<input type="checkbox" name="stickyInst" value="{pk}">
						<xsl:if test="/model/stickyInstances/item = pk">
							<xsl:attribute name="checked"/>
						</xsl:if>
					</input>
				</td>
		</tr>
	</xsl:template>

	<xsl:template match="item[@type='org.dcm4chex.archive.ejb.interfaces.PresentationStateDTO']">
		<tr bgcolor="#ffffff">		  
			<td title="Instance Number">
        <xsl:value-of select="instanceNumber"/>
      </td>
			<td width="1%" title="Creation Datetime">
        <xsl:value-of select="presentationCreationDateTime"/>
			</td>
      <td title="Presentation Description">
        <xsl:value-of select="presentationDescription"/>
			</td>
			<td width="1%" title="Presentation Label">
        <xsl:value-of select="presentationLabel"/>
			</td>
			<td width="1%" title="Number of Referenced Images">
        -&gt;<xsl:value-of select="numberOfReferencedImages"/>
			</td>
			<td width="1%" title="Number of Files">
        <xsl:value-of select="numberOfFiles"/>
			</td>
		  <td width="1%" title="Retrieve AETs">
        <xsl:value-of select="retrieveAETs"/>
      </td>
			<td width="1%">
				  <a href="javascript:alert('Edit Presentation State not implemented')">Edit</a>
       </td>
				<td width="1%">
					<input type="checkbox" name="stickyInst" value="{pk}">
						<xsl:if test="/model/stickyInstances/item = pk">
							<xsl:attribute name="checked"/>
						</xsl:if>
					</input>
				</td>
		</tr>
	</xsl:template>

	<xsl:template match="item[@type='org.dcm4chex.archive.ejb.interfaces.StructuredReportDTO']">
		<tr bgcolor="#ffffff">		  
			<td title="Instance Number">
        <xsl:value-of select="instanceNumber"/>
      </td>
			<td width="1%" title="Content Datetime">
        <xsl:value-of select="contentDateTime"/>
			</td>
      <td title="Document Title">
        <xsl:value-of select="documentTitle"/>
			</td>
      <td width="1%" colspan="2" title="Document Status">
        <xsl:value-of select="completionFlag"/>/<xsl:value-of select="verificationFlag"/>
      </td>
			<td width="1%" title="Number of Files">
        <xsl:value-of select="numberOfFiles"/>
			</td>
		  <td width="1%" title="Retrieve AETs">
        <xsl:value-of select="retrieveAETs"/>
      </td>
			<td width="1%">
				  <a href="javascript:alert('Edit Structured Report not implemented')">Edit</a>
      </td>
				<td width="1%">
					<input type="checkbox" name="stickyInst" value="{pk}">
						<xsl:if test="/model/stickyInstances/item = pk">
							<xsl:attribute name="checked"/>
						</xsl:if>
					</input>
				</td>
		</tr>
	</xsl:template>


</xsl:stylesheet>
