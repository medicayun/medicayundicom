<?xml version="1.0" encoding="UTF-8"?>
<!--
 $Id: trash.xsl 2985 2006-11-28 20:33:28Z fangatagfa $
 -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:param name="trash.remove" select="'false'" />

<xsl:output method="html" indent="yes" encoding="UTF-8"/>
<xsl:variable name="page_title">Patient List</xsl:variable>
<xsl:include href="page.xsl"/>

<xsl:template match="model">
	<form action="trashfolder.m" method="post" name="myForm" accept-charset="UTF-8">
		<table class="folder_header" border="0" cellspacing="0" cellpadding="0" width="100%">
			<td class="folder_header" valign="top">
				<table class="folder_header" border="0" height="30" cellspacing="0" cellpadding="0" width="100%">
					<td class="folder_header" width="5">
						<input type="checkbox" name="showWithoutStudies" value="true" title="Show patients without studies">
							<xsl:if test="/model/showWithoutStudies = 'true'">
								<xsl:attribute name="checked"/>
							</xsl:if>
						</input>
					</td>
					<td class="folder_header" width="5" title="Show patients without studies">w/o studies</td>
					<td class="folder_header" align="center">
					<xsl:choose>
						<xsl:when test="total &lt; 1">
							No matching studies found!
						</xsl:when>
						<xsl:otherwise>
							Displaying studies
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
							</b> matching studies.
						</xsl:otherwise>
					</xsl:choose>
 					</td>

					<td class="folder_header" width="150">
					</td>
					<td class="folder_header" width="40">
						<input type="image" value="Search" name="filter" src="images/search.gif" border="0"
						 	title="New Search"/>
					</td>
					<td class="folder_header" width="40">
						<input type="image" value="Prev" name="prev" src="images/prev.gif" alt="prev" border="0"
						 	title="Previous Search Results">
							<xsl:if test="offset = 0">
                  <xsl:attribute name="disabled">disabled</xsl:attribute>
							</xsl:if>
						</input>
					</td>
					<td class="folder_header" width="40">
						<input type="image" value="Next" name="next" src="images/next.gif" alt="next" border="0"
						 	title="Next Search Results">
							<xsl:if test="offset + limit &gt;= total">
								<xsl:attribute name="disabled">disabled</xsl:attribute>
							</xsl:if>
						</input>
					</td>
					<td class="folder_header" width="40">
						&#160;
					</td>
					<td class="folder_header" width="40">
						<input type="image" value="Undel" name="undel" src="images/undel.gif" alt="undelete" border="0"
							title="Undelete selected Entities"
							onclick="return confirm('Undelete selected Entities?')">
							<xsl:if test="total &lt;= 0">
								<xsl:attribute name="disabled">disabled</xsl:attribute>
							</xsl:if>
						</input>
					</td>
					<xsl:if test="$trash.remove='true'">	
						<td class="folder_header" width="40">
							<input type="image" value="Del" name="del" src="images/loeschen.gif" alt="delete" border="0"
								title="Delete selected Entities"
								onclick="return confirm('Delete selected Entities?')">
								<xsl:if test="total &lt;= 0">
									<xsl:attribute name="disabled">disabled</xsl:attribute>
								</xsl:if>
							</input>
						</td>
					</xsl:if>
				</table>
				<table class="folder_search" border="0" width="100%" cellpadding="0" cellspacing="0">
				  <tr>
					<td class="folder_search" >Patient Name:
					</td>
					<td>
						<input size="10" name="patientName" type="text" value="{patientName}"/>
      				</td>
					<td class="folder_search" >Patient ID:
					</td>
					<td>
						<input size="10" name="patientID" type="text" value="{patientID}"/>
					</td>
		      		<td class="label">Study IUID:</td>
		      		<td>
		        		<input size="45" name="studyUID" type="text" value="{studyUID}"/>
		      		</td>
		      		<td class="label">Accession No.:
							</td>
		      		<td>
		        		<input size="10" name="accessionNumber" type="text" value="{accessionNumber}"/>
		      		</td>
		      	  </tr>
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
<table class="folder_overview" border="0" cellpadding="0" cellspacing="0" width="100%">
	<table class="folder_overview" border="0" cellpadding="0" cellspacing="0" width="100%">
		<colgroup>
			<col width="5%"/>
			<col width="22%"/>
			<col width="10%"/>
			<col width="12%"/>
			<col width="47%"/>
			<col width="4%"/>
		</colgroup>
		<tr>
			<td class="patient_mark" >
				<font size="1">
					Patient</font>
			</td>
			<td>
				<font size="1">
					Name:</font>
			</td>
    	<td>
				<font size="1">
					Patient ID:</font>
    	</td>
			<td>
				<font size="1">
					Birthdate:</font>
			</td>
    	<td>
				<font size="1">
					Sex:</font>
    	</td>
			<td>
			</td>
			</tr>
	</table>	
	
	<table class="folder_overview" border="0" cellspacing="0" cellpadding="0" width="100%">
		<colgroup>
			<col width="5%"/><!-- margin -->
			<col width="11%"/><!-- Date/time -->
			<col width="12%"/><!-- StudyID -->
			<col width="10%"/><!-- Modalities -->
			<col width="26%"/><!-- Study Instance UID -->
			<col width="9%"/><!-- Acc No --><!-- 73 -->
    		<col width="13%"/><!-- Ref. Physician -->
		    <col width="4%"/><!-- Study Status ID -->
			<col width="2%"/><!-- No. of Series -->
		    <col width="2%"/><!-- No. of Instances -->
			<col width="2%"/><!-- add -->
			<col width="2%"/><!-- edit -->
			<col width="2%"/><!-- sticky -->
		</colgroup>
		<tr>
			<td class="study_mark" >
				<font size="1">
					Study</font>
			</td>
			<td>
				<font size="1">
					Date/Time:</font>
			</td>
			<td>
				<font size="1">
					Study ID (@Media):</font>
			</td>
			<td>
				<font size="1">
					Mods:</font>
			</td>
			<td>
				<font size="1">
				Study IUID:
				</font>
			</td>
			<td>
				<font size="1">
					Acc.No.:</font>
			</td>
			<td>
				<font size="1">
					Ref. Physician:</font>
			</td>
			<td>
				<font size="1">
					Status:</font>
			</td>
			<td>
				<font size="1">
					NoS:</font>
			</td>
			<td>
				<font size="1">
					NoI:</font>
			</td>
			<td>&#160;</td>
			<td>&#160;</td>
			<td>&#160;</td>
		</tr>
	</table>
	
	<table class="folder_overview" border="0" cellspacing="0" cellpadding="0" width="100%">
		<colgroup>
			<col width="5%"/><!-- margin -->
			<col width="12%"/><!-- date/time -->
			<col width="12%"/><!-- Series No -->
			<col width="10%"/><!-- Modality -->
			<col width="35%"/><!-- Series Instance UID. -->
			<col width="10%"/><!-- Vendor/Model -->
			
    		<col width="8%"/><!-- PPS Status -->
			<col width="2%"/><!-- spacer -->
			<col width="2%"/><!-- edit -->
			<col width="2%"/><!-- sticky -->
		</colgroup>
		<tr>
			<td class="series_mark" >
				<font size="1">
					Series</font>
			</td>
			<td>
				<font size="1">
					Date/Time:</font>
			</td>
			<td>
				<font size="1">
					Series No (@media):</font>
			</td>
			<td>
				<font size="1">
					Mod:</font>			
			</td>
			<td>
				<font size="1">
				Series Instance UID:
				</font>
			</td>
			<td>
				<font size="1">
					Vendor/Model:</font>
			</td>
			<td>
				<font size="1">
					PPS Status:</font>
			</td>
			<td>
				<font size="1">
					NoI:</font>
			</td>
			<td>&#160;</td>
			<td>&#160;</td>
			<td>&#160;</td>
		</tr>
	</table>
</table>
</xsl:template>


<xsl:template match="item[@type='org.dcm4chex.archive.web.maverick.model.PatientModel']">
	<tr>
      <table class="patient_line" width="100%" cellpadding="0" cellspacing="0" border="0">
		<colgroup>
			<col width="1%"/>
			<col width="26%"/>
			<col width="10%"/>
			<col width="12%"/>
		    <col width="45%"/>
		    <col width="2%"/>
		    <col width="2%"/>
			<col width="2%"/>
		</colgroup>
		<xsl:variable name="rowspan" select="1+count(descendant::item)"/>
			<td class="patient_mark" align="right" rowspan="{$rowspan}">
				<xsl:choose>
					<xsl:when test="$rowspan=1">
						<a title="Show Studies" href="expandTrashPatient.m?patPk={pk}&amp;expand=true">
							<img src="images/plus.gif" border="0" alt="+"/>
              			</a>				
					</xsl:when>
					<xsl:otherwise>
						<a title="Hide Studies" href="expandTrashPatient.m?patPk={pk}&amp;expand=false">							
							<img src="images/minus.gif" border="0" alt="-"/>
              			</a>				
					</xsl:otherwise>
				</xsl:choose>
			</td>
			<td title="Patient Name">
				<strong><xsl:value-of select="patientName"/>&#160;</strong>
  			</td>
			<td title="Patient ID">
				<strong><xsl:value-of select="patientID"/>&#160;</strong>
			</td>
			<td title="Birth Date">
				<strong><xsl:value-of select="patientBirthDate"/>&#160;</strong>
			</td>
			<td title="Patient Sex">
				<strong><xsl:value-of select="patientSex"/>&#160;</strong>
			</td>
            <td>&#160;</td>
			<td class="patient_mark" align="right">
				<a href="trashfolder.m?undel=patient&amp;patPk={pk}"
					onclick="return confirm('Undelete this patient ?')">
					<img src="images/undel.gif" alt="Undelete Patient" border="0" title="Undelete Patient"/>		
				</a>
			</td>
			<td class="patient_mark" align="right">
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
		<table class="study_line" width="100%" cellpadding="0" cellspacing="0" border="0">
			<xsl:variable name="rowspan" select="1+count(descendant::item)"/>
			<colgroup>
				<col width="2%"/><!-- margin -->
				<col width="14%"/><!-- Date/time -->
				<col width="12%"/><!-- StudyID -->
				<col width="10%"/><!-- Modalities -->
				<col width="26%"/><!-- Study Instance UID -->
				<col width="9%"/><!-- Acc No -->
	    		<col width="13%"/><!-- Ref. Physician -->
			    <col width="4%"/><!-- Study Status ID -->
				<col width="2%"/><!-- No. of Series -->
			    <col width="2%"/><!-- No. of Instances -->
				<col width="2%"/><!-- add -->
				<col width="2%"/><!-- edit -->
				<col width="2%"/><!-- sticky -->
			</colgroup>
			<td class="study_mark" align="right" rowspan="{$rowspan}">
				<xsl:choose>
					<xsl:when test="$rowspan=1">
						<a title="Show Series" href="expandTrashStudy.m?patPk={../../pk}&amp;studyPk={pk}&amp;expand=true">
						    <img src="images/plus.gif" border="0" alt="+"/>
	                                                                                    </a>				
					</xsl:when>
					<xsl:otherwise>
						<a title="Hide Series" href="expandTrashStudy.m?patPk={../../pk}&amp;studyPk={pk}&amp;expand=false">							
						    <img src="images/minus.gif" border="0" alt="-"/>
	                                                                                    </a>				
					</xsl:otherwise>
				</xsl:choose>
			</td>
	  		<td title="Study Date">
				<xsl:value-of select="studyDateTime"/>&#160;
			</td>
			<td title="Study ID (@Media)" >
				<xsl:value-of select="studyID"/>
				<xsl:if test="filesetId != '_NA_'"> @<xsl:value-of select="filesetId"/> </xsl:if>
				&#160;
			</td>
		 	<td title="Modalities">
				<xsl:value-of select="modalitiesInStudy"/>&#160;
			</td>
	  		<td title="Study IUID">
				<xsl:value-of select="studyIUID"/>&#160;
			</td>
			<td title="Accession Number">
				&#160;<xsl:value-of select="accessionNumber"/>&#160;
			</td>
	  		<td title="Referring Physican">
				<xsl:value-of select="referringPhysician"/>&#160;
			</td>
	  		<td title="Study Status ID" align="center">
	  			<xsl:choose>
	  				<xsl:when test="studyStatusImage!=''">
		      			<img src="{studyStatusImage}" border="0" alt="{studyStatusId}"/>
		      		</xsl:when>
	  				<xsl:when test="studyStatusId!=''">
	  					<xsl:value-of select="studyStatusId"/>
	  				</xsl:when>
	  				<xsl:otherwise>&#160;</xsl:otherwise>
	      		</xsl:choose>
			</td>
	  		<td title="Number of Series" align="center">
				<xsl:value-of select="numberOfSeries"/>&#160;
			</td>
	  		<td title="Number of Instances" align="center">
				<xsl:value-of select="numberOfInstances"/>&#160;
			</td>
            <td>&#160;</td>
		    <td class="study_mark" align="right">
				<a href="trashfolder.m?undel=study&amp;studyPk={pk}"
					onclick="return confirm('Undelete this study ?')">
					<img src="images/undel.gif" alt="Undelete Study" border="0" title="Undelete Study"/>		
				</a>
		    </td>
			<td class="study_mark" align="right">
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
		<table class="series_line" width="100%" cellpadding="0" cellspacing="0" border="0" >	  
			<colgroup>
				<col width="3%"/><!-- left margin -->
				<col width="14%"/><!-- Date/Time -->
				<col width="12%"/><!-- Series No -->
				<col width="10%"/><!-- Modality -->
				<col width="35%"/><!-- Series Instance UID. -->
				<col width="10%"/><!-- Vendor/Model -->
	    		<col width="8%"/><!-- PPS Status -->
				<col width="2%"/><!-- spacer -->
				<col width="2%"/><!-- edit -->
				<col width="2%"/><!-- sticky -->
			</colgroup>
			<xsl:variable name="rowspan" select="1+count(descendant::item)"/>
			<td class="series_mark" align="right" rowspan="{$rowspan}">
				<xsl:choose>
					<xsl:when test="$rowspan=1">
		  				<a title="Show Instances" href="expandTrashSeries.m?patPk={../../../../pk}&amp;studyPk={../../pk}&amp;seriesPk={pk}&amp;expand=true">
							<img src="images/plus.gif" border="0" alt="+"/>
              			</a>				
					</xsl:when>
					<xsl:otherwise>
			  			<a title="Hide Instances" href="expandTrashSeries.m?patPk={../../../../pk}&amp;studyPk={../../pk}&amp;seriesPk={pk}&amp;expand=false">
							<img src="images/minus.gif" border="0" alt="-"/>
	              		</a>				
					</xsl:otherwise>
				</xsl:choose>
			</td>
			<td title="Series Date">
				<xsl:value-of select="seriesDateTime"/>&#160;
			</td>
			<td title="Series Number (@media)">
				<xsl:value-of select="seriesNumber"/>
				<xsl:if test="filesetId != '_NA_'"> @<xsl:value-of select="filesetId"/> </xsl:if>
				&#160;
			</td>
		    <td title="Modality">
				<xsl:value-of select="modality"/>&#160;
			</td>
      		<td title="Series Instance UID">
						<xsl:value-of select="seriesIUID"/>&#160;
	      	</td>
			<td title="Modality Vendors / Modelname">
    			<xsl:value-of select="manufacturer"/>
					\ <xsl:value-of select="manufacturerModelName"/>&#160;
      		</td>
			<td>
				<xsl:choose>
					<xsl:when test="PPSStatus='DISCONTINUED' or incorrectWLEntry='true'">
						<xsl:attribute name="style">color: red</xsl:attribute>
						<xsl:attribute name="title">PPS Status:<xsl:value-of select="DRCodeMeaning"/></xsl:attribute>
						<xsl:text>DISCONTINUED</xsl:text>
					</xsl:when>
					<xsl:when test="PPSStatus!=''">
						<xsl:attribute name="style">color: black</xsl:attribute>
						<xsl:attribute name="title">PPS Status</xsl:attribute>
						<xsl:value-of select="PPSStatus"/>	
					</xsl:when>
				</xsl:choose>
	    		&#160;
    	  	</td>
			<td title="Number of Instances" align="center">
				<xsl:value-of select="numberOfInstances"/>
			</td>
            <td>&#160;</td>
            <td class="series_mark" align="right">
				<a href="trashfolder.m?undel=series&amp;seriesPk={pk}"
					onclick="return confirm('Undelete this series ?')">
					<img src="images/undel.gif" alt="Undelete Series" border="0" title="Undelete Series"/>		
				</a>
	        </td>
            <td class="series_mark" align="right">
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
<table class="instance_line" width="100%" cellpadding="0" cellspacing="0" border="0">
		<colgroup>
			<col width="4%"/>
			<col width="12%"/>
			<col width="3%"/>
			<col width="6%"/>
			<col width="25%"/>
			<col width="5%"/>
			<col width="10%"/>
			<col width="31%"/>
			<col width="2%"/>
			<col width="2%"/>
		</colgroup>
		<xsl:variable name="rowspan" select="1+count(descendant::item)"/>
		  <td align="right" rowspan="{$rowspan}">
			<xsl:choose>
				<xsl:when test="$rowspan=1">
	  				<a title="Show files" href="expandTrashInstance.m?expand=true&amp;patPk={../../../../../../pk}&amp;studyPk={../../../../pk}&amp;seriesPk={../../pk}&amp;instancePk={pk}">
						<img src="images/plus.gif" border="0" alt="+"/>
  		            </a>				
				</xsl:when>
				<xsl:otherwise>
  			        <a title="Hide Instances" href="expandTrashInstance.m?expand=false&amp;patPk={../../../../../../pk}&amp;studyPk={../../../../pk}&amp;seriesPk={../../pk}&amp;instancePk={pk}">
						<img src="images/minus.gif" border="0" alt="-"/>
                    </a>				
				</xsl:otherwise>
			</xsl:choose>
                                </td>

		<td title="Content Datetime" >
			<xsl:value-of select="contentDateTime"/>&#160;
		</td>
		<td title="Instance Number" >
	   		<xsl:value-of select="instanceNumber"/>&#160;
    </td>
    <td title="ImageType" >
			<xsl:value-of select="imageType"/>&#160;
		</td>
    <td title="Pixel Matrix" >
	    	<xsl:value-of select="photometricInterpretation"/>
				??
    		<xsl:value-of select="rows"/>x<xsl:value-of select="columns"/>x<xsl:value-of select="numberOfFrames"/>
				??
    		<xsl:value-of select="bitsAllocated"/>bits&#160;
    </td>
		<td title="Number of Files" >
			<xsl:value-of select="numberOfFiles"/>&#160;
		</td>
	  	<td title="Retrieve AETs" >
			<xsl:value-of select="retrieveAETs"/>&#160;
    	</td>
	  	<td title="SopIUID" >
			<xsl:value-of select="sopIUID"/>&#160;
    	</td>
        <td class="instance_mark" align="right">
			<a href="trashfolder.m?undel=instance&amp;instancePk={pk}"
				onclick="return confirm('Undelete this instance ?')">
				<img src="images/undel.gif" alt="Undelete Instance" border="0" title="Undelete Instance"/>		
			</a>
        </td>
		
		<td class="instance_mark" align="right">
			<input type="checkbox" name="stickyInst" value="{pk}">
				<xsl:if test="/model/stickyInstances/item = pk">
					<xsl:attribute name="checked"/>
				</xsl:if>
			</input>
		</td>
      </table>
	</tr>
		<xsl:apply-templates select="files/item">
			<xsl:sort data-type="number" order="descending" select="pk"/>
		</xsl:apply-templates>
</xsl:template>

<xsl:template match="item[@type='org.dcm4chex.archive.web.maverick.model.PresentationStateModel']">
	<tr>
<table class="instance_line" width="100%" cellpadding="0" cellspacing="0" border="0">	
		<colgroup>
			<col width="4%"/>
			<col width="15%"/>
			<col width="6%"/>
			<col width="20%"/>
			<col width="5%"/>
			<col width="5%"/>
			<col width="5%"/>
			<col width="12%"/>
			<col width="24%"/>
			<col width="2%"/>
			<col width="2%"/>
		</colgroup>
		<xsl:variable name="rowspan" select="1+count(descendant::item)"/>
		  <td align="right" rowspan="{$rowspan}">
			<xsl:choose>
				<xsl:when test="$rowspan=1">
	  				<a title="Show files" href="expandTrashInstance.m?expand=true&amp;patPk={../../../../../../pk}&amp;studyPk={../../../../pk}&amp;seriesPk={../../pk}&amp;instancePk={pk}">
						<img src="images/plus.gif" border="0" alt="+"/>
              		                                            </a>				
				</xsl:when>
				<xsl:otherwise>
	  			        <a title="Hide Instances" href="expandTrashInstance.m?expand=false&amp;patPk={../../../../../../pk}&amp;studyPk={../../../../pk}&amp;seriesPk={../../pk}&amp;instancePk={pk}">
					<img src="images/minus.gif" border="0" alt="-"/>
                                                                              </a>				
				</xsl:otherwise>
			</xsl:choose>
                                </td>
		<td title="Creation Datetime" >
      		<xsl:value-of select="presentationCreationDateTime"/>&#160;
		</td>
		<td title="Instance Number" >
			<xsl:value-of select="instanceNumber"/>&#160;
    	</td>
    	<td title="Presentation Description" >
      		<xsl:value-of select="presentationDescription"/>&#160;
		</td>
		<td title="Presentation Label" >
    		<xsl:value-of select="presentationLabel"/>&#160;
		</td>
		<td title="Number of Referenced Images" >
      		-&gt;<xsl:value-of select="numberOfReferencedImages"/>&#160;
		</td>
		<td title="Number of Files" >
      		<xsl:value-of select="numberOfFiles"/>&#160;
		</td>
		<td title="Retrieve AETs" >
      		<xsl:value-of select="retrieveAETs"/>&#160;
    	</td>
	  	<td title="SopIUID" >
			<xsl:value-of select="sopIUID"/>&#160;
    	</td>
        <td class="instance_mark" align="right">
			<a href="trashfolder.m?undel=instance&amp;patPk={../../../../../../pk}&amp;studyPk={../../../../pk}&amp;seriesPk={../../pk}&amp;instancePk={pk}"
				onclick="return confirm('Undelete this instance ?')">
				<img src="images/undel.gif" alt="Undelete Instance" border="0" title="Undelete Instance"/>		
			</a>
        </td>
		<td class="instance_mark" align="right">
			<input type="checkbox" name="stickyInst" value="{pk}">
				<xsl:if test="/model/stickyInstances/item = pk">
					<xsl:attribute name="checked"/>
				</xsl:if>
			</input>
		</td>
      </table>
	</tr>
		<xsl:apply-templates select="files/item">
			<xsl:sort data-type="number" order="descending" select="pk"/>
		</xsl:apply-templates>
</xsl:template>

<xsl:template match="item[@type='org.dcm4chex.archive.web.maverick.model.StructuredReportModel']">
	<tr>
<table class="instance_line" width="100%" cellpadding="1" cellspacing="0" border="0">		 
		<colgroup>
			<col width="4%"/>
			<col width="15%"/>
			<col width="6%"/>
			<col width="15%"/>
			<col width="15%"/>
			<col width="5%"/>
			<col width="13%"/>
			<col width="23"/>
			<col width="2%"/>
			<col width="2%"/>
		</colgroup>
 
		<xsl:variable name="rowspan" select="1+count(descendant::item)"/>
		  <td align="right" rowspan="{$rowspan}">
			<xsl:choose>
				<xsl:when test="$rowspan=1">
	  				<a title="Show files" href="expandTrashInstance.m?expand=true&amp;patPk={../../../../../../pk}&amp;studyPk={../../../../pk}&amp;seriesPk={../../pk}&amp;instancePk={pk}">
						<img src="images/plus.gif" border="0" alt="+"/>
              		                                            </a>				
				</xsl:when>
				<xsl:otherwise>
	  			        <a title="Hide Instances" href="expandTrashInstance.m?expand=false&amp;patPk={../../../../../../pk}&amp;studyPk={../../../../pk}&amp;seriesPk={../../pk}&amp;instancePk={pk}">
					<img src="images/minus.gif" border="0" alt="-"/>
                                                                              </a>				
				</xsl:otherwise>
			</xsl:choose>
                                </td>
		<td title="Content Datetime" >
    	                    <xsl:value-of select="contentDateTime"/>&#160;
		</td>
		<td title="Instance Number" >
  		    <xsl:value-of select="instanceNumber"/>&#160;
    	</td>
    	<td title="Document Title" >
  			<xsl:value-of select="documentTitle"/>&#160;
		</td>
		<td title="Document Status" >
      		<xsl:value-of select="completionFlag"/>/<xsl:value-of select="verificationFlag"/>&#160;
    	</td>
		<td title="Number of Files" >
			<xsl:value-of select="numberOfFiles"/>&#160;
		</td>
	  	<td title="Retrieve AETs" >
			<xsl:value-of select="retrieveAETs"/>&#160;
    	</td>
	  	<td title="SopIUID" >
			<xsl:value-of select="sopIUID"/>&#160;
    	</td>
        <td class="instance_mark" align="right">
			<a href="trashfolder.m?undel=instance&amp;patPk={../../../../../../pk}&amp;studyPk={../../../../pk}&amp;seriesPk={../../pk}&amp;instancePk={pk}"
				onclick="return confirm('Undelete this instance ?')">
				<img src="images/undel.gif" alt="Undelete Instance" border="0" title="Undelete Instance"/>		
			</a>
        </td>
		<td class="instance_mark" align="right">
			<input type="checkbox" name="stickyInst" value="{pk}">
				<xsl:if test="/model/stickyInstances/item = pk">
					<xsl:attribute name="checked"/>
				</xsl:if>
			</input>
		</td>
</table>
	</tr>
		<xsl:apply-templates select="files/item">
			<xsl:sort data-type="number" order="descending" select="pk"/>
		</xsl:apply-templates>
</xsl:template>

<xsl:template match="item[@type='org.dcm4chex.archive.web.maverick.model.WaveformModel']">
	<tr>
<table class="instance_line" width="100%" cellpadding="0" cellspacing="0" border="0">
		<colgroup>
			<col width="4%"/>
			<col width="10%"/>
			<col width="3%"/>
			<col width="21%"/>
			<col width="25%"/>
			<col width="10%"/>
			<col width="23%"/>
			<col width="2%"/>
			<col width="2%"/>
		</colgroup>
		<xsl:variable name="rowspan" select="1+count(descendant::item)"/>
		  <td align="right" rowspan="{$rowspan}">
			<xsl:choose>
				<xsl:when test="$rowspan=1">
	  				<a title="Show files" href="expandTrashInstance.m?expand=true&amp;patPk={../../../../../../pk}&amp;studyPk={../../../../pk}&amp;seriesPk={../../pk}&amp;instancePk={pk}">
						<img src="images/plus.gif" border="0" alt="+"/>
              		                                            </a>				
				</xsl:when>
				<xsl:otherwise>
	  			        <a title="Hide Instances" href="expandTrashInstance.m?expand=false&amp;patPk={../../../../../../pk}&amp;studyPk={../../../../pk}&amp;seriesPk={../../pk}&amp;instancePk={pk}">
					<img src="images/minus.gif" border="0" alt="-"/>
                                                                              </a>				
				</xsl:otherwise>
			</xsl:choose>
                                </td>
		<td title="Content Datetime" >
			<xsl:value-of select="contentDateTime"/>&#160;
		</td>
		<td title="Instance Number" >
	   		<xsl:value-of select="instanceNumber"/>&#160;
	    </td>
	    <td title="WaveformType" >
			<xsl:value-of select="waveformType"/>&#160;
		</td>
	    <td title="dummy" >&#160;&#160;</td>
 	  	<td title="Retrieve AETs" >
			<xsl:value-of select="retrieveAETs"/>&#160;
    	</td>
	  	<td title="SopIUID" >
			<xsl:value-of select="sopIUID"/>&#160;
    	</td>
        <td class="instance_mark" align="right">
			<a href="trashfolder.m?undel=instance&amp;patPk={../../../../../../pk}&amp;studyPk={../../../../pk}&amp;seriesPk={../../pk}&amp;instancePk={pk}"
				onclick="return confirm('Undelete this instance ?')">
				<img src="images/undel.gif" alt="Undelete Instance" border="0" title="Undelete Instance"/>		
			</a>
        </td>
		<td class="instance_mark" align="right" >
			<input type="checkbox" name="stickyInst" value="{pk}">
				<xsl:if test="/model/stickyInstances/item = pk">
					<xsl:attribute name="checked"/>
				</xsl:if>
			</input>
		</td>
      </table>
	</tr>
		<xsl:apply-templates select="files/item">
			<xsl:sort data-type="number" order="descending" select="pk"/>
		</xsl:apply-templates>
</xsl:template>

<xsl:template match="item[@type='org.dcm4chex.archive.ejb.interfaces.FileDTO']">
<xsl:variable name="line_name">
	<xsl:choose>
		<xsl:when test="fileStatus &lt; 0">error_line</xsl:when>
		<xsl:otherwise>file_line</xsl:otherwise>
	</xsl:choose>
</xsl:variable>
	<tr>
<table class="{$line_name}" width="100%" cellpadding="0" cellspacing="0" border="0">
		<colgroup>
			<col width="5%"/>
			<col width="10%"/>
			<col width="10%"/>
			<col width="10%"/>
			<col width="10%"/>
			<col width="35%"/>
			<col width="20%"/>
		</colgroup>
                             <td>&#160;</td>
		<td title="fileTSUID">
			<xsl:value-of select="fileTsuid"/>&#160;
		</td>
		<td title="retrieveAET">
	   		<xsl:value-of select="retrieveAET"/>&#160;
                            </td>
                            <td title="Status">
                                        <xsl:choose>
                                            <xsl:when test="fileStatus=0">OK</xsl:when>
                                            <xsl:when test="fileStatus=1">to archive</xsl:when>
                                            <xsl:when test="fileStatus=2">archived</xsl:when>
                                            <xsl:when test="fileStatus=-1">compress failed</xsl:when>
                                            <xsl:when test="fileStatus=-2">verify compress failed</xsl:when>
                                            <xsl:when test="fileStatus=-3">MD5 check failed</xsl:when>
                                            <xsl:when test="fileStatus=-3">HSM query failed</xsl:when>
                                            <xsl:otherwise>unknown(<xsl:value-of select="fileStatus"/>)</xsl:otherwise>
                                        </xsl:choose>&#160;
                            </td>
                            <td title="Size">
	    	               <xsl:value-of select="fileSize"/> bytes&#160;
                            </td>
		<td title="Path">
			<xsl:value-of select="directoryPath"/>/<xsl:value-of select="filePath"/>&#160;
		</td>
	  	<td title="MD5">
			<xsl:value-of select="md5String"/>
    	                </td>
      </table>
	</tr>
</xsl:template>
    
</xsl:stylesheet>
