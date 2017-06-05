<xsl:stylesheet version="1.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output method="html">
</xsl:output>
<xsl:variable name="page_title">Modality Performed Procedure Step - MPPS Console</xsl:variable>
<xsl:include href="../page.xsl"/>
<xsl:template match="model">
<!-- Filter -->
	<form action="mpps_console.m" method="get" name="myForm">
		<table border="0" cellspacing="0" cellpadding="0" width="100%" bgcolor="eeeeee">
			<td valign="top">
				<table border="0" height="30" cellspacing="0" cellpadding="0" width="100%">
					<td bgcolor="eeeeee" align="center">
						<xsl:if test="total &gt; 0">
							<b>MPPS Worklist:</b> Displaying procedure step 
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
							<b>MPPS Worklist:</b> No matching procedure steps found!
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
							<a href="mpps_console.m?nav=prev">
								<img src="images/prev.gif" alt="prev" border="0" title="Previous Search Results"/>		
							</a>
						</xsl:if>
					</td>
					<td width="40" bgcolor="eeeeee">
						<xsl:if test="offset + limit &lt; total">
							<a href="mpps_console.m?nav=next">
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
							<input size="10" name="patientName" type="text" value="{filter/patientName}"/>
						</td>
						<td bgcolor="eeeeee">&#160;&#160;</td>
						<td bgcolor="eeeeee" colspan="2">Date: </td>
						<td bgcolor="eeeeee">
							<input size="12" name="startDate" type="text" value="{filter/startDate}"/>
						</td>
						<td bgcolor="eeeeee">to: </td>
						<td bgcolor="eeeeee">
							<input size="12" name="endDate" type="text" value="{filter/endDate}"/>
						</td>
						<td bgcolor="eeeeee">&#160;&#160;</td>
						<td bgcolor="eeeeee" >Modality: </td>
						<td bgcolor="eeeeee">
							<input size="4" name="modality" type="text" value="{filter/modality}"/>
						</td>
						<td bgcolor="eeeeee">&#160;&#160;</td>
						<td bgcolor="eeeeee" nowrap="nowrap" >Station AET: </td>
						<td bgcolor="eeeeee">
							<input size="8" name="stationAET" type="text" value="{filter/stationAET}"/>
						</td>
						<td bgcolor="eeeeee">&#160;&#160;</td>
						<td bgcolor="eeeeee" nowrap="nowrap" >Acc. No.: (</td>
						<td bgcolor="eeeeee">
							<input type="checkbox" name="emptyAccNo" value="true">
								<xsl:if test="/model/filter/emptyAccNo = 'true'">
									<xsl:attribute name="checked"/>
								</xsl:if>
							</input>
						</td>
						<td bgcolor="eeeeee" nowrap="nowrap" >unscheduled) </td>
						<td bgcolor="eeeeee">
							<input size="10" name="accessionNumber" type="text" value="{filter/accessionNumber}"/>
						</td>
						<td bgcolor="eeeeee" nowrap="nowrap" >Status: </td>
						<td bgcolor="eeeeee">
							<select name="status" >
								<option value="">
									<xsl:if test="filter/status=''"><xsl:attribute name="selected" /></xsl:if>
									[ANY]
								</option>
								<option value="0">
									<xsl:if test="filter/status=0"><xsl:attribute name="selected" /></xsl:if>
									IN PROGRESS
								</option>
								<option value="1">
									<xsl:if test="filter/status=1"><xsl:attribute name="selected" /></xsl:if>
									COMPLETED
								</option>
								<option value="2">
									<xsl:if test="filter/status=2"><xsl:attribute name="selected" /></xsl:if>
									DISCONTINUED
								</option>
							</select>
					 						</td>
						<td width="100%" bgcolor="eeeeee">&#160;</td>
						
					</tr>
				</table>
			</td>
		</table>
		<xsl:call-template name="tableheader"/>
		<xsl:apply-templates select="mppsEntries/item">
		</xsl:apply-templates>

</form>
</xsl:template>

<xsl:template name="tableheader">
		
<!-- Header of working list entries -->
<table width="100%" border="0" cellspacing="0" cellpadding="0">

	<table border="0" cellpadding="0" cellspacing="0" width="100%">
		<colgroup>
			<col width="8%"/>
			<col width="9%"/>
			<col width="15%"/>
			<col width="20%"/>
			<col width="11%"/>
			<col width="12%"/>
			<col width="3%"/>
			<col width="10%"/>
			<col width="12%"/>
		</colgroup>
		<tr >
			<td bgcolor="eeeeee" style="height:7px" colspan="9"></td> <!-- spacer -->
		</tr>
		<tr>
			<th title="Accession Number" align="left">Acc. No.</th>
			<th title="Procedure Step Status" align="left">Status</th>
			<th title="Study Instance UID" align="left">StudyIUID</th>
			<th title="Performed Procedure Description" align="left">Proc. Desc.</th>
			<th title="Performed Step Start Date" align="left">Start Date</th>
			<th title="Perf. Station: (&lt;Name&gt;-&lt;AET&gt;[&lt;Mod.&gt;]" align="left">Station</th>
			<th title="Number of Instances: " align="left">NoI</th>
			<th title="PatientName: " align="left">Patient</th>
			<xsl:choose>
				<xsl:when test="unscheduled = 'true'">
					<th nowrap="nowrap">Function</th>	
				</xsl:when>
				<xsl:otherwise>
					<th >&#160;&#160;</th>
				</xsl:otherwise>
			</xsl:choose>
		</tr>
	</table>
</table>

</xsl:template>

<!-- List of working list entries ( scheduled procedur steps ) -->

<xsl:template match="item[@type='org.dcm4chex.archive.web.maverick.mpps.model.MPPSEntry']">

<table width="100%" border="0" cellspacing="0" cellpadding="0">
	
	<table border="0" cellpadding="0" cellspacing="0" width="100%">
		<colgroup>
			<col width="8%"/>
			<col width="9%"/>
			<col width="15%"/>
			<col width="20%"/>
			<col width="11%"/>
			<col width="12%"/>
			<col width="3%"/>
			<col width="10%"/>
			<col width="12%"/>
		</colgroup>
		<tr>
	        <td align="left" title="Accession No." >
				<a href="foldersubmit.m?destination=LOCAL&amp;accessionNumber={accNumbers}&amp;patientName=&amp;patientID=&amp;studyID=&amp;studyDateRange=&amp;modality=&amp;filter.x=5&amp;filter.y=12"><!-- TODO if more than one accNumber -->
					<xsl:value-of select="accNumbers"/>
				</a>
		 	</td>
	        <td align="left" title="PPSStatus" >
				<xsl:value-of select="PPSStatus"/>
		 	</td>
	        <td title="StudyIUID">
				<a href="foldersubmit.m?destination=LOCAL&amp;studyUID={studyUIDs}&amp;accessionNumber=&amp;patientName=&amp;patientID=&amp;studyDateRange=&amp;modality=&amp;filter.x=5&amp;filter.y=12"><!-- TODO if more than one studyIUID -->
					<xsl:value-of select="studyUIDs"/>
				</a>
		 	</td>
	        <td align="left" title="PPS Desc.">
				<xsl:value-of select="PPSDescription"/>
		 	</td>
	        <td align="left" title="Start Date" >
				<xsl:value-of select="ppsStartDateTime"/>
	        </td>
	        <td align="left" title="Station" >
				<xsl:if test="string-length(stationName) > 0">
					<xsl:value-of select="stationName"/> -
				</xsl:if>
				<xsl:value-of select="stationAET"/>[<xsl:value-of select="modality"/>]
	        </td>
	        <td align="left" title="Number of Instances" >
				<xsl:value-of select="numberOfInstances"/>
	        </td>
	        <td align="left" title="Patient" >
				<a href="foldersubmit.m?destination=LOCAL&amp;patientID={patientID}&amp;accessionNumber=&amp;patientName=&amp;studyID=&amp;studyDateRange=&amp;modality=&amp;filter.x=5&amp;filter.y=12">
					<xsl:value-of select="patientName"/>
				</a>
			</td>
			<xsl:choose> <!-- TODO -->
				<xsl:when test="/model/unscheduled = 'true'">
					<td title="Function" align="center" valign="bottom">
						&#160;&#160;
					</td>	
				</xsl:when>
				<xsl:otherwise>
					<td>&#160;&#160;</td>
				</xsl:otherwise>
			</xsl:choose>
		</tr>
	</table>

</table>
</xsl:template>
	   
</xsl:stylesheet>

