<xsl:stylesheet version="1.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="html" indent="yes" encoding="ISO-8859-1"/>
<xsl:variable name="page_title">Modality Worklist Console</xsl:variable>
<xsl:include href="../page.xsl"/>
<xsl:template match="model">
<!-- Filter -->
	<form action="mwl_console.m" method="get" name="myForm">
		<table border="0" cellspacing="0" cellpadding="0" width="100%" bgcolor="eeeeee">
			<td valign="top">
				<table border="0" height="30" cellspacing="0" cellpadding="0" width="100%">
					<td bgcolor="eeeeee" align="center">
						<xsl:if test="total &gt; 0">
							<b>Modality Worklist:</b> Displaying procedure step 
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
							</b>matching procedure steps.
						</xsl:if>
						<xsl:if test="total = 0">
							<b>Modality Worklist:</b> No matching procedure steps found!
						</xsl:if>
 					</td>

					<td width="150" bgcolor="eeeeee">
					</td>
					<td width="40" bgcolor="eeeeee">
						<input type="image" value="Search" name="filter" src="images/search.gif" border="0"
						 	title="New Search"/>
					</td>
					<td width="40" bgcolor="eeeeee">
						<xsl:if test="offset &gt; 0">
							<a href="mwl_console.m?nav=prev">
								<img src="images/prev.gif" alt="prev" border="0" title="Previous Search Results"/>		
							</a>
						</xsl:if>
					</td>
					<td width="40" bgcolor="eeeeee">
						<xsl:if test="offset + limit &lt; total">
							<a href="mwl_console.m?nav=next">
								<img src="images/next.gif" alt="next" border="0" title="Next Search Results"/>		
							</a>
						</xsl:if>
					</td>
				</table>
				<table border="0" cellpadding="0" cellspacing="0" bgcolor="eeeeee">
					<tr>
						<td bgcolor="eeeeee">&#160;&#160;</td>
						<td bgcolor="eeeeee" class="label">Patient:</td>
						<td bgcolor="eeeeee">
							<input size="10" name="patientName" type="text" value="{filter/patientName}"
								title="Patient name"/>
						</td>
						<td bgcolor="eeeeee">&#160;&#160;</td>
						<td bgcolor="eeeeee" colspan="2">Date: </td>
						<td bgcolor="eeeeee">
							<input size="15" name="startDate" type="text" value="{filter/startDate}"
								title="Query Start date"/>
						</td>
						<td bgcolor="eeeeee">to: </td>
						<td bgcolor="eeeeee">
							<input size="15" name="endDate" type="text" value="{filter/endDate}"
								title="Query End date"/>
						</td>
						<td bgcolor="eeeeee">&#160;&#160;</td>
						<td bgcolor="eeeeee" >Modality: </td>
						<td bgcolor="eeeeee">
							<input size="10" name="modality" type="text" value="{filter/modality}"
								title="Modality"/>
						</td>
						<td bgcolor="eeeeee">&#160;&#160;</td>
						<td bgcolor="eeeeee" nowrap="nowrap" >Station AET: </td>
						<td bgcolor="eeeeee">
							<input size="10" name="stationAET" type="text" value="{filter/stationAET}"
								title="Station AET"/>
						</td>
						<td bgcolor="eeeeee">&#160;&#160;</td>
						<td bgcolor="eeeeee" nowrap="nowrap" >Accession No.: </td>
						<td bgcolor="eeeeee">
							<input size="10" name="accessionNumber" type="text" value="{filter/accessionNumber}"
								title="Accession number"/>
						</td>
						<td width="100%" bgcolor="eeeeee">&#160;</td>
						
					</tr>
				</table>
			</td>
		</table>
		<xsl:call-template name="tableheader"/>
		<xsl:apply-templates select="mwlEntries/item">
			<xsl:sort data-type="number" order="ascending" select="spsID"/>
		</xsl:apply-templates>

</form>
</xsl:template>

<xsl:template name="tableheader">
		
<!-- Header of working list entries -->
<table width="100%" border="0" cellspacing="0" cellpadding="0">

	<table border="0" cellpadding="0" cellspacing="0" width="100%">
		<colgroup>
			<col width="10%"/>
			<col width="10%"/>
			<col width="9%"/>
			<col width="27%"/>
			<col width="11%"/>
			<col width="14%"/>
			<col width="15%"/>
			<col width="12%"/>
		</colgroup>
		<tr >
			<td bgcolor="eeeeee" style="height:7px" colspan="8"></td> <!-- spacer -->
		</tr>
		<tr>
			<th title="Scheduled Procedure Step ID" align="left">ID</th>
			<th title="Requested Procedure Step ID" align="left">Req. Proc. ID</th>
			<th title="Accession Number" align="left">Acc. No.</th>
			<th title="Requested Procedure Description" align="left">Proc. Desc.</th>
			<th title="Scheduled Step Start Date" align="left">Start Date</th>
			<th title="Sched. Station: (&lt;Name&gt;-&lt;AET&gt;[&lt;Mod.&gt;]" align="left">Station</th>
			<th title="Patient: Name and ID" align="left">Patient</th>
			<xsl:choose>
				<xsl:when test="local = 'true'">
					<th nowrap="nowrap">Function</th>	
				</xsl:when>
				<xsl:otherwise>
					<th >&#160;&#160;</th>
				</xsl:otherwise>
			</xsl:choose>
		</tr>
	</table>
	
	<table border="0" cellpadding="0" cellspacing="0" width="100%">
		<colgroup>
			<col width="1%"/>
			<col width="15%"/>
			<col width="16%"/>
			<col width="27%"/>
			<col width="15%"/>
			<col width="8%"/>
			<col width="10%"/>
			<col width="4%"/>
			<col width="12%"/>
		</colgroup>
		<tr>
			<th align="left">&#160;&#160;</th> <!-- intend -->
			<th title="Study Instance UID" align="left">StudyIUID</th>
			<th title="Filler and Placer Order Number" align="left">Filler/Placer Order</th>
			<th title="Scheduled Procedure Step Description (protocol)" align="left">SPS Desc.</th>
			<th title="Name of the patient's referring physician" align="left">Ref. Physician</th>
			<th title="Admission No.: Identification number of the visit" align="left">Adm. ID</th>
			<th title="Patients Birthdate" align="left">Birthdate</th>
			<th title="Sex of the patient" align="left">Sex</th>
			<th align="left">&#160;&#160;</th> <!-- function -->
		</tr>
		<tr >
			<td bgcolor="eeeeee" style="height:5px" colspan="9"></td> <!-- spacer -->
		</tr>
	</table>

</table>

</xsl:template>

<!-- List of working list entries ( scheduled procedur steps ) -->

<xsl:template match="item[@type='org.dcm4chex.archive.web.maverick.mwl.model.MWLEntry']">

<table width="100%" border="0" cellspacing="0" cellpadding="0">
	
	<table border="0" cellpadding="0" cellspacing="0" width="100%">
		<colgroup>
			<col width="10%"/>
			<col width="10%"/>
			<col width="9%"/>
			<col width="27%"/>
			<col width="11%"/>
			<col width="14%"/>
			<col width="15%"/>
			<col width="12%"/>
		</colgroup>
		<tr>
	        <td align="left" title="SPS ID" >
				<xsl:value-of select="spsID"/>
	        </td>
	        <td align="left" title="Req. Procedure ID" >
				<xsl:value-of select="reqProcedureID"/>
		 	</td>
	        <td align="left" title="Accession No." >
				<xsl:value-of select="accessionNumber"/>
		 	</td>
	        <td align="left" title="Proc. Desc.">
				<xsl:value-of select="reqProcedureDescription"/>
		 	</td>
	        <td align="left" title="Start Date" >
				<xsl:value-of select="spsStartDateTime"/>
	        </td>
	        <td align="left" title="Station" >
				<xsl:if test="string-length(stationName) > 0">
					<xsl:value-of select="stationName"/> -
				</xsl:if>
				<xsl:value-of select="stationAET"/>[<xsl:value-of select="modality"/>]
	        </td>
	        <td align="left" title="Patient" >
				<a href="foldersubmit.m?destination=LOCAL&amp;patientID={patientID}&amp;accessionNumber=&amp;patientName=&amp;studyID=&amp;studyDateRange=&amp;modality=&amp;filter.x=5&amp;filter.y=12">
					<xsl:value-of select="patientName"/> [<xsl:value-of select="patientID"/>]
				</a>
			</td>
			<xsl:choose>
				<xsl:when test="/model/local = 'true'">
					<td title="Function" align="center" valign="bottom">
						<a href="mwl_console.m?action=delete&amp;spsid={spsID}">
							<xsl:attribute name="onclick">return confirm('Delete worklist entry <xsl:value-of select="spsID"/> ?')</xsl:attribute>
							<img src="images/delete.gif" alt="delete" border="0" title="Delete this worklist entry!"/>		
						</a>
					</td>	
				</xsl:when>
				<xsl:otherwise>
					<td>&#160;&#160;</td>
				</xsl:otherwise>
			</xsl:choose>
		</tr>
	</table>

	<table border="0" cellpadding="0" cellspacing="0" width="100%">
		<colgroup>
			<col width="1%"/>
			<col width="15%"/>
			<col width="16%"/>
			<col width="27%"/>
			<col width="15%"/>
			<col width="8%"/>
			<col width="10%"/>
			<col width="4%"/>
			<col width="12%"/>
		</colgroup>
		<tr>
			<td>&#160;&#160;</td><!-- intend -->
	        <td title="StudyIUID">
				<xsl:value-of select="studyUID"/>
		 	</td>
	        <td title="Filler/Placer Order">
				<xsl:value-of select="fillerOrderNumber"/>/<xsl:value-of select="placerOrderNumber"/>
		 	</td>
	        <td title="SPS Desc.">
				<xsl:value-of select="SPSDescription"/>
		 	</td>
	        <td title="Ref. Physician">
				<xsl:value-of select="referringPhysicianName"/>
		 	</td>
	        <td title="Admission ID">
				<xsl:value-of select="admissionID"/>
		 	</td>
	        <td title="Birthday">
				<xsl:value-of select="patientBirthDate"/>
		 	</td>
	        <td title="Birthday">
				<xsl:value-of select="patientSex"/>
		 	</td>
			<td>&#160;&#160;</td><!-- function -->
		</tr>
		<tr >
			<td bgcolor="eeeeee" style="height:5px" colspan="8"></td> <!-- spacer -->
		</tr>
	</table>

</table>
</xsl:template>
	   
</xsl:stylesheet>

