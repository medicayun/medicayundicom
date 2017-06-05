<?xml version="1.0" encoding="UTF-8"?>
<!--
 $Id: folder.xsl 1485 2005-02-03 14:45:10Z javawilli $
 -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="html" indent="yes" encoding="ISO-8859-1"/>
<xsl:variable name="page_title">Patient List</xsl:variable>
<xsl:include href="page.xsl"/>

<xsl:template match="model">
	<form action="foldersubmit.m" method="get" name="myForm">
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
						<input type="image" value="Search" name="filter" src="images/search.gif" border="0"
						 	title="New Search"/>
					</td>
					<td width="40" bgcolor="eeeeee">
						<input type="image" value="Prev" name="prev" src="images/prev.gif" alt="prev" border="0"
						 	title="Previous Search Results">
							<xsl:if test="offset = 0">
                  <xsl:attribute name="disabled">disabled</xsl:attribute>
							</xsl:if>
						</input>
					</td>
					<td width="40" bgcolor="eeeeee">
						<input type="image" value="Next" name="next" src="images/next.gif" alt="next" border="0"
						 	title="Next Search Results">
							<xsl:if test="offset + limit &gt;= total">
								<xsl:attribute name="disabled">disabled</xsl:attribute>
							</xsl:if>
						</input>
					</td>
					<td width="40" bgcolor="eeeeee">
						<a href="patientEdit.m?pk=-1">
							<img src="images/addpat.gif" alt="Add Patient" border="0" title="Add new Patient"/>		
						</a>
					</td>
					<td width="40" bgcolor="eeeeee">
						<input type="image" value="Merge" name="merge" src="images/merge.gif" alt="merge" border="0"
							title="Merge selected Patients" onclick="return validateChecks(this.form.stickyPat, 'Patient', 2)">
							<xsl:if test="total &lt;= 0">
								<xsl:attribute name="disabled">disabled</xsl:attribute>
							</xsl:if>
						</input>
					</td>
					<td width="40" bgcolor="eeeeee">
						<input type="image" value="Move" name="move" src="images/move.gif" alt="move" border="0"
							title="Move selected Entities">
							<xsl:if test="total &lt;= 0">
								<xsl:attribute name="disabled">disabled</xsl:attribute>
							</xsl:if>
						</input>
					</td>
					<td width="40" bgcolor="eeeeee">
						<input type="image" value="Del" name="del" src="images/loeschen.gif" alt="delete" border="0"
							title="Delete selected Entities"
							onclick="return confirm('Delete selected Entities?')">
							<xsl:if test="total &lt;= 0">
								<xsl:attribute name="disabled">disabled</xsl:attribute>
							</xsl:if>
						</input>
					</td>
					<td width="40" bgcolor="eeeeee">
						<input type="image" value="Send" name="send" src="images/send.gif" alt="send" border="0"
							title="Send selected Entities to specified Destination"
							onclick="return confirm('Send selected entities to ' + 
document.myForm.destination.options[document.myForm.destination.selectedIndex ].text + '?')">
							<xsl:if test="total &lt;= 0">
								<xsl:attribute name="disabled">disabled</xsl:attribute>
							</xsl:if>
						</input>
					</td>
					<td width="50" bgcolor="eeeeee">
						<select size="1" name="destination" title="Send Destination">
							<xsl:for-each select="aets/item">
								<xsl:sort data-type="text" order="ascending" select="title"/>
								<option>
									<xsl:if test="/model/destination = title">
										<xsl:attribute name="selected"/>
									</xsl:if>
									<xsl:value-of select="title"/>
								</option>
							</xsl:for-each>						
						</select>
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
			<col width="22%"/>
			<col width="10%"/>
			<col width="12%"/>
			<col width="47%"/>
			<col width="4%"/>
		</colgroup>
		<tr bgcolor="eeeeee">
			<td bgcolor="cccccc">
				<font size="1">
					Patient</font>
			</td>
			<td>
				<font size="1" color="ff0000">
					Name:</font>
			</td>
    	<td>
				<font size="1" color="ff0000">
					Patient ID:</font>
    	</td>
			<td>
				<font size="1" color="ff0000">
					Birthdate:</font>
			</td>
    	<td>
				<font size="1" color="ff0000">
					Sex:</font>
    	</td>
			<td bgcolor="cccccc">
			</td>
			</tr>
	</table>	
	
	<table border="0" cellspacing="0" cellpadding="0" width="100%">
		<colgroup>
			<col width="5%"/>
			<col width="11%"/>
			<col width="12%"/>
			<col width="10%"/>
			<col width="26%"/>
			<col width="9%"/>
			<col width="16%"/>
			<col width="3%"/>
			<col width="4%"/>
			<col width="4%"/>
		</colgroup>
		<tr bgcolor="eeeeee">
			<td bgcolor="ccccff">
				<font size="1">
					Study</font>
			</td>
			<td>
				<font size="1" color="ff0000">
					Date/Time:</font>
			</td>
			<td>
				<font size="1" color="ff0000">
					Study ID:</font>
			</td>
			<td>
				<font size="1" color="ff0000">
					Mods:</font>
			</td>
			<td>
				<font size="1" color="ff0000">
					Study Description:</font>
			</td>
			<td>
				<font size="1" color="ff0000">
					Acc.Nr.:</font>
			</td>
			<td>
				<font size="1" color="ff0000">
					Ref. Physican:</font>
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
			<col width="12%"/>
			<col width="12%"/>
			<col width="10%"/>
			<col width="35%"/>
			<col width="18%"/>
			<col width="4%"/>
			<col width="4%"/>
			<col width="4%"/>
		</colgroup>
		<tr bgcolor="eeeeee">
			<td bgcolor="ccffcc">
				<font size="1">
					Serie</font>
			</td>
			<td>
				<font size="1" color="ff0000">
					Date/Time:</font>
			</td>
			<td>
				<font size="1" color="ff0000">
					Series No:</font>
			</td>
			<td>
				<font size="1" color="ff0000">
					Mod:</font>			
			</td>
			<td>
				<font size="1" color="ff0000">
					Series Description/Body Part:</font>
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


<xsl:template match="item[@type='org.dcm4chex.archive.web.maverick.model.PatientModel']">
	<tr>
      <table width="100%" callpadding="1" callspacing="1" border="0">
		<colgroup>
			<col width="1%"/>
			<col width="26%"/>
			<col width="10%"/>
			<col width="12%"/>
			<col width="47%"/>
			<col width="2%"/>
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
			<td align="right" bgcolor="ccccff">
				<a href="studyEdit.m?patPk={pk}&amp;studyPk=-1">
					<img src="images/add.gif" alt="Add Study" border="0" title="Add new Study"/>		
				</a>
			</td>
			<td align="right" bgcolor="cccccc">
				<a href="patientEdit.m?pk={pk}">
					<img src="images/edit.gif" alt="Edit Patient" border="0" title="Edit Patient Attributes"/>		
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

<xsl:template match="item[@type='org.dcm4chex.archive.web.maverick.model.StudyModel']">
<tr>
	<table width="100%" callpadding="0" callspacing="0" border="0">
		<xsl:variable name="rowspan" select="1+count(descendant::item)"/>
		<colgroup>
			<col width="2%"/>
			<col width="14%"/>
			<col width="12%"/>
			<col width="10%"/>
			<col width="26%"/>
			<col width="9%"/>
			<col width="17%"/>
			<col width="2%"/>
			<col width="4%"/>
			<col width="2%"/>
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
			<td title="Accession Number">
				<xsl:value-of select="accessionNumber"/>
			</td>
      <td title="Referring Physican">
				<xsl:value-of select="referringPhysician"/>
			</td>
      <td title="Number of Series">
				<xsl:value-of select="numberOfSeries"/>
			</td>
      <td title="Number of Instances">
				<xsl:value-of select="numberOfInstances"/>
			</td>
			<td align="right" bgcolor="ccffcc">
			<a href="seriesEdit.m?patPk={../../pk}&amp;studyPk={pk}&amp;seriesPk=-1">
				<img src="images/add.gif" alt="Add Series" border="0" title="Add new series"/>		
			</a>
			</td>
			<td align="right" bgcolor="ccccff">
				<a href="studyEdit.m?patPk={../../pk}&amp;studyPk={pk}">
					<img src="images/edit.gif" alt="Edit Study" border="0" title="Edit Study Attributes"/>		
				</a>
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

<xsl:template match="item[@type='org.dcm4chex.archive.web.maverick.model.SeriesModel']">
	<tr>
<table width="100%" callpadding="0" callspacing="0" border="0">	  
		<colgroup>
			<col width="3%"/>
			<col width="14%"/>
			<col width="12%"/>
			<col width="10%"/>
			<col width="35%"/>
			<col width="18%"/>
			<col width="4%"/>
			<col width="2%"/>
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
				<a href="seriesEdit.m?patPk={../../../../pk}&amp;studyPk={../../pk}&amp;seriesPk={pk}">
					<img src="images/edit.gif" alt="Edit Series" border="0" title="Edit Series Attributes"/>		
				</a>
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

<xsl:template match="item[@type='org.dcm4chex.archive.web.maverick.model.ImageModel']">
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
				??
    		<xsl:value-of select="rows"/>x<xsl:value-of select="columns"/>x<xsl:value-of select="numberOfFrames"/>
				??
    		<xsl:value-of select="bitsAllocated"/>bits
    </td>
		<td title="Number of Files">
			<xsl:value-of select="numberOfFiles"/>
		</td>
	  <td title="Retrieve AETs">
			<xsl:value-of select="retrieveAETs"/>
    </td>
		<td align="right" bgcolor="ffffcc">
			<xsl:choose>
				<xsl:when test="availability='ONLINE'" >			
					<a href="{/model/wadoBaseURL}wado?requestType=WADO&amp;studyUID={../../../../studyIUID}&amp;seriesUID={../../seriesIUID}&amp;objectUID={sopIUID}" target="imageview" >
						<img src="images/image.gif" alt="View image" border="0" title="View image"/>		
					</a>
				</xsl:when>
				<xsl:otherwise>
					<img src="images/invalid.gif" alt="Image not online" border="0" title="Image not online"/>		
				</xsl:otherwise>
			</xsl:choose>				
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

<xsl:template match="item[@type='org.dcm4chex.archive.web.maverick.model.PresentationStateModel']">
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

<xsl:template match="item[@type='org.dcm4chex.archive.web.maverick.model.StructuredReportModel']">
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
			<xsl:choose>
				<xsl:when test="availability='ONLINE'" >			
					<a href="{/model/wadoBaseURL}IHERetrieveDocument?requestType=DOCUMENT&amp;documentUID={sopIUID}&amp;preferredContentType=application/pdf" target="SRview" >
						<img src="images/sr.gif" alt="View Report" border="0" title="View Report"/>		
					</a>
				</xsl:when>
				<xsl:otherwise>
					<img src="images/invalid.gif" alt="Report not online" border="0" title="Report not online"/>		
				</xsl:otherwise>
			</xsl:choose>				
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
