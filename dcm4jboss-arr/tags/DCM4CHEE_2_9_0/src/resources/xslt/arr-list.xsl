<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="html"
	doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"
  doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"
  indent = "yes" encoding="UTF-8" />
<xsl:variable name="page_title">Audit Repository Log</xsl:variable>
<xsl:include href="page.xsl"/>
<!-- root document -->
	
<!-- query -->
	<xsl:template match="query">
    <xsl:variable name="nrec"><xsl:value-of select="@nrecords"/></xsl:variable> <!-- number of records actually returned -->
    <xsl:variable name="eof"><xsl:value-of select="@eof"/></xsl:variable> <!-- if you have reached end of records to fetch -->
    <xsl:variable name="lastquery">start=<xsl:value-of select="@start"/>&amp;pagesize=<xsl:value-of select="@pagesize"/>&amp;from=<xsl:value-of select="@from"/>&amp;to=<xsl:value-of select="@to"/>&amp;host=<xsl:value-of select="@host"/>&amp;type=<xsl:value-of select="@type"/>&amp;aet=<xsl:value-of select="@aet"/>&amp;username=<xsl:value-of select="@username"/>&amp;patientname=<xsl:value-of select="@patientname"/>&amp;patientid=<xsl:value-of select="@patientid"/></xsl:variable> <!-- last query -->
    <xsl:variable name="vsortdir-type">
    	<xsl:choose>
	    	<xsl:when test="@sortdir-type != 'DESC'">DESC</xsl:when>
	    	<xsl:otherwise>ASC</xsl:otherwise>
	    </xsl:choose>
   	</xsl:variable>
    <xsl:variable name="vsortdir-host">
    	<xsl:choose>
	    	<xsl:when test="@sortdir-host != 'DESC'">DESC</xsl:when>
	    	<xsl:otherwise>ASC</xsl:otherwise>
	    </xsl:choose>
	</xsl:variable>
    <xsl:variable name="vsortdir-timestamp">
    	<xsl:choose>
	    	<xsl:when test="@sortdir-timestamp != 'DESC'">DESC</xsl:when>
	    	<xsl:otherwise>ASC</xsl:otherwise>
	    </xsl:choose>
	</xsl:variable>
    <xsl:variable name="sortbystr">
    	<xsl:if test="@sortdir-type != ''">&amp;orderby=type&amp;orderdir=<xsl:value-of select="@sortdir-type"/></xsl:if>
    	<xsl:if test="@sortdir-host != ''">&amp;orderby=host&amp;orderdir=<xsl:value-of select="@sortdir-host"/></xsl:if>
    	<xsl:if test="@sortdir-timestamp != ''">&amp;orderby=timestamp&amp;orderdir=<xsl:value-of select="@sortdir-timestamp"/></xsl:if>
	</xsl:variable>
    <!-- title -->
		<!-- <h1>Audit Record Repository List View</h1><hr/>
 -->
    <!-- display chosen options in the case of a search -->
		<xsl:if test="(@type != '') or (@host != '') or (@from != '') or (@to != '')">
			<i>Searched
            <xsl:if test="@type != ''"> for <xsl:value-of select="@type"/> events</xsl:if>
            <xsl:if test="@host != ''"> from host: <xsl:value-of select="@host"/></xsl:if>
            <xsl:if test="(@from != '') or (@to != '')"> from: <xsl:value-of select="@from"/> to: <xsl:value-of select="@to"/></xsl:if>
            <br/></i>
		</xsl:if>
    <!-- display the position in the set of records -->
    <xsl:if test="$nrec != 0">
		<table width="100%" cellspacing="0" cellpadding="0" border="0"> 
			<td width="400" bgcolor="#eeeeee" valign="middle">
			<h1>Audit Record Repository List View</h1>
			</td>
      <td width="300" bgcolor="#eeeeee">
			<b>Results: </b>
      from <xsl:value-of select="@start + 1"/> to <xsl:value-of select="@start + $nrec"/> (of <xsl:value-of select="@tot-nrecords"/>)
      <xsl:if test="@pagesize = -1"> (all)</xsl:if>
			</td>
			<td></td>
		</table>
    </xsl:if>
    <!-- main form -->
    <table border="0" cellspacing="0" cellpadding="0" width="100%">
<!--      <tr>
        <td>
          <xsl:if test="((@start &gt; 0) or ($eof = 'false')) and (@pagesize != 0)">
             pager 
          </xsl:if>
        </td>
      </tr>
 -->

        <td valign="top">
          <!-- query -->
          <table border="0" cellspacing="0" cellpadding="0">
					<tr>
                
		            <form name="frmArrQuery" method="get" action="/dcm4chee-arr">
								<xsl:variable name="pagesize_form">
                	<xsl:choose>
                  	<xsl:when test="@pagesize &lt;= 0"><xsl:value-of select="number(30)"/></xsl:when> <!-- DEFAULT PAGE SIZE -->
                  	<xsl:otherwise><xsl:value-of select="@pagesize"/></xsl:otherwise>
                	</xsl:choose>
              	</xsl:variable>
								<!--<td colspan="1">
<button class="query-submit" name="reset" onClick="frmArrQuery.reset();">Clear</button>
</td>-->
								<td bgcolor="#eeeeee" height="25" valign="top">
                   	<xsl:if test="@start &gt; 0">
                    <xsl:variable name="startentry">
                      <xsl:choose>
                        <xsl:when test="@start - @pagesize &lt; 0"><xsl:value-of select="number(0)"/>
												</xsl:when>
                        <xsl:otherwise><xsl:value-of select="@start - @pagesize"/></xsl:otherwise>
                      </xsl:choose>
                    </xsl:variable>
                    <a href="/dcm4chee-arr/?type={@type}&amp;host={@host}&amp;from={@from}&amp;to={@to}&amp;aet={@aet}&amp;username={@username}&amp;patientname={@patientname}&amp;patientid={@patientid}&amp;start={$startentry}&amp;pagesize={@pagesize}{$sortbystr}"><img src="images/preview.gif" border="0" /> </a>
                  	</xsl:if>
 
									</td>	
									<td bgcolor="#eeeeee" height="25" valign="top">
                  <xsl:if test="$eof = 'false'">
  
                    <a href="/dcm4chee-arr/?type={@type}&amp;host={@host}&amp;from={@from}&amp;to={@to}&amp;aet={@aet}&amp;username={@username}&amp;patientname={@patientname}&amp;patientid={@patientid}&amp;start={@start + @pagesize}&amp;pagesize={@pagesize}{$sortbystr}"><img src="images/next.gif" border="0"/> </a>
                  </xsl:if>
 
								</td>
								<td bgcolor="#eeeeee" height="25" valign="top">
								<input type="image" value="Query" name="query" src="images/query.gif" border="0" alt="Query"/>
                <input name="update" type="checkbox" value="1">Update view</input>
								<!--		<td><input class="query-submit" type="submit" value="Query"/></td>
   -->
								</td>
              <tr><td colspan="2" height="25"> Results to return:</td><td><input class="text" name="pagesize" type="text" size="5" maxlength="5" value="{$pagesize_form}"/></td></tr>
              <tr><td colspan="2" height="25"> Patient Name:</td><td><input class="text" name="patientname" type="text" value="{@patientname}"/></td></tr>
              <tr><td colspan="2" height="25"> Patient ID:</td><td><input class="text" name="patientid" type="text" value="{@patientid}"/></td></tr>
              <tr><td colspan="2" height="25"> AE Title:</td><td><input class="text" name="aet" type="text" value="{@aet}"/></td></tr>
              <tr><td colspan="2" height="25"> Username:</td><td><input class="text" name="username" type="text" value="{@username}"/></td></tr>
              <tr><td colspan="2" height="25"> Host:</td><td><input class="text" name="host" type="text" value="{@host}"/></td></tr>
              <tr><td colspan="2" height="20"><b> TimeStamp</b></td></tr>
              <tr><td colspan="2" height="25"> from:</td><td><input class="text" name="from" type="text" value="{@from}"/></td></tr>
              <tr><td colspan="2" height="25"> to:</td><td><input class="text" name="to" type="text" svalue="{@to}"/></td></tr>
              <tr><td colspan="3" height="20" bgcolor="#eeeeee"><b> Type</b></td></tr>
							<tr><td></td></tr>
							<tr><td colspan="3"><input type="checkbox" name="type" value="ActorConfig">ActorConfig</input></td></tr>
              <tr><td colspan="3"><input type="checkbox" name="type" value="ActorStartStop">ActorStartStop</input></td></tr>
              <tr><td colspan="3"><input type="checkbox" name="type" value="AuditLogUsed">AuditLogUsed</input></td></tr>
              <tr><td colspan="3"><input type="checkbox" name="type" value="BeginStoringInstances">BeginStoringInstances</input></td></tr>
              <tr><td colspan="3"><input type="checkbox" name="type" value="DICOMInstancesDeleted">DICOMInstancesDeleted</input></td></tr>
              <tr><td colspan="3"><input type="checkbox" name="type" value="DICOMInstancesUsed">DICOMInstancesUsed</input></td></tr>
              <tr><td colspan="3"><input type="checkbox" name="type" value="DicomQuery">DicomQuery</input></td></tr>
              <tr><td colspan="3"><input type="checkbox" name="type" value="Export">Export</input></td></tr>
              <tr><td colspan="3"><input type="checkbox" name="type" value="Import">Import</input></td></tr>
              <tr><td colspan="3"><input type="checkbox" name="type" value="InstancesSent">InstancesSent</input></td></tr>
              <tr><td colspan="3"><input type="checkbox" name="type" value="InstancesStored">InstancesStored</input></td></tr>
              <tr><td colspan="3"><input type="checkbox" name="type" value="NetworkEntry">NetworkEntry</input></td></tr>
              <tr><td colspan="3"><input type="checkbox" name="type" value="OrderRecord">OrderRecord</input></td></tr>
              <tr><td colspan="3"><input type="checkbox" name="type" value="PatientRecord">PatientRecord</input></td></tr>
              <tr><td colspan="3"><input type="checkbox" name="type" value="ProcedureRecord">ProcedureRecord</input></td></tr>
              <tr><td colspan="3"><input type="checkbox" name="type" value="SecurityAlert">SecurityAlert</input></td></tr>
              <tr><td colspan="3"><input type="checkbox" name="type" value="StudyDeleted">StudyDeleted</input></td></tr>
              <tr><td colspan="3"><input type="checkbox" name="type" value="UserAuthenticated">UserAuthenticated</input></td></tr>
              <tr><td colspan="3" height="20" bgcolor="#eeeeee"></td></tr>
							
	          <!-- old drop-down box for event selection
		          <select name="type">
		            <option value="" selected="selected">[Any]</option>
		            <option value="Import">Import</option>
		            <option value="InstancesStored">InstancesStored</option>
		            <option value="ProcedureRecord">ProcedureRecord</option>
		            <option value="ActorStartStop">ActorStartStop</option>
		            <option value="ActorConfig">ActorConfig</option>
		            <option value="Export">Export</option>
		            <option value="DICOMInstancesDeleted">DICOMInstancesDeleted</option>
		            <option value="PatientRecord">PatientRecord</option>
		            <option value="OrderRecord">OrderRecord</option>
		            <option value="BeginStoringInstances">BeginStoringInstances</option>
		            <option value="InstancesSent">InstancesSent</option>
		            <option value="DICOMInstancesUsed">DICOMInstancesUsed</option>
		            <option value="StudyDeleted">StudyDeleted</option>
		            <option value="DicomQuery">DicomQuery</option>
		            <option value="SecurityAlert">SecurityAlert</option>
		            <option value="UserAuthenticated">UserAuthenticated</option>
		            <option value="AuditLogUsed">AuditLogUsed</option>
		            <option value="NetworkEntry">NetworkEntry</option>
		          </select>
	          -->
            </form>
						</tr>
          </table>
        </td>
        <td width="1%" bgcolor="#eeeeee">
				</td>
        <td width="100%" valign="top">
          <xsl:if test="$nrec != 0">
            <!-- audit record list -->
 	          <table width="100%" border="0" cellspacing="0" cellpadding="0">
	            <tr bgcolor="#eeeeee" height="25" align="top">
								<td><a href="/dcm4chee-arr?{$lastquery}&amp;orderby=type&amp;orderdir={$vsortdir-type}"><div class="width: 100%">Audit Event</div></a></td>
								<td width="10" bgcolor="#eeeeee"></td>
	              <td><a href="/dcm4chee-arr?{$lastquery}&amp;orderby=host&amp;orderdir={$vsortdir-host}"><div class="width: 100%">Host</div></a></td>
								<td width="10" bgcolor="#eeeeee"></td>
	              <td><a href="/dcm4chee-arr?{$lastquery}&amp;orderby=timestamp&amp;orderdir={$vsortdir-timestamp}"><div class="width: 100%">Timestamp (local)</div></a></td>
								<td width="5" bgcolor="#eeeeee"></td>								
	              <td>Information</td>
								<td width="10" bgcolor="#eeeeee"></td>								
	              <td><!-- for view xml --></td>
	            </tr>
	            <xsl:for-each select="record">
	              <xsl:variable name="pos" select="(position() mod 2)"/>
	              <tr class="row{$pos}">
	                <td><a href="arr-view.do?pk={@pk}">
	                  <xsl:call-template name="Event">
	                    <xsl:with-param name="type" select="@type"/>
	                  </xsl:call-template></a>
	                </td>
								<td width="10"></td>										
	                <td><xsl:value-of select="@host"/></td>
								<td width="10"></td>										
	                <td><xsl:value-of select="@timestamp"/></td>
								<td width="10"></td>										
	                <td><xsl:call-template name="Info"/></td>
								<td width="10"></td>										
	                <td><a href="arr-view.do?pk={@pk}&amp;viewxml=1">(xml)</a></td>
	              </tr>
	            </xsl:for-each>
	          </table>
          </xsl:if>
        </td>

    </table>
	</xsl:template>
	<!-- record (not used)-->
	<xsl:template match="record">
	</xsl:template>
	<!--(Import | InstancesStored | ProcedureRecord | ActorStartStop | ActorConfig | Export | DICOMInstancesDeleted | PatientRecord |
	OrderRecord | BeginStoringInstances | InstancesSent | DICOMInstancesUsed | StudyDeleted | DicomQuery | SecurityAlert |
	UserAuthenticated | AuditLogUsed | NetworkEntry)-->
	<!-- Display any extra information in a record  -->
	<xsl:template name="Info">
		<xsl:if test="@aet != ''"><b> AET:</b> <xsl:value-of select="@aet"/> </xsl:if>
		<xsl:if test="@username != ''"><b> Username:</b> <xsl:value-of select="@username"/> </xsl:if>
		<xsl:if test="@patientname != ''"><b> Patient Name:</b> <xsl:value-of select="@patientname"/> </xsl:if>
		<xsl:if test="@patientid != ''"><b> Patient ID:</b> <xsl:value-of select="@patientid"/> </xsl:if>
	</xsl:template>
	<!-- Display event -->
	<xsl:template name="Event">
		<xsl:param name="type"/>
		<xsl:if test="$type = 'Import'"><span>Import</span></xsl:if>
		<xsl:if test="$type = 'InstancesStored'"><span>InstancesStored</span></xsl:if>
		<xsl:if test="$type = 'ProcedureRecord'"><span>ProcedureRecord</span></xsl:if>
		<xsl:if test="$type = 'ActorStartStop'"><span>ActorStartStop</span></xsl:if>
		<xsl:if test="$type = 'ActorConfig'"><span>ActorConfig</span></xsl:if>
		<xsl:if test="$type = 'Export'"><span>Export</span></xsl:if>
		<xsl:if test="$type = 'DICOMInstancesDeleted'"><span>DICOMInstancesDeleted</span></xsl:if>
		<xsl:if test="$type = 'PatientRecord'"><span>PatientRecord</span></xsl:if>
		<xsl:if test="$type = 'OrderRecord'"><span>OrderRecord</span></xsl:if>
		<xsl:if test="$type = 'BeginStoringInstances'"><span>BeginStoringInstances</span></xsl:if>
		<xsl:if test="$type = 'InstancesSent'"><span>InstancesSent</span></xsl:if>
		<xsl:if test="$type = 'DICOMInstancesUsed'"><span>DICOMInstancesUsed</span></xsl:if>
		<xsl:if test="$type = 'StudyDeleted'"><span>StudyDeleted</span></xsl:if>
		<xsl:if test="$type = 'DicomQuery'"><span>DicomQuery</span></xsl:if>
		<xsl:if test="$type = 'SecurityAlert'"><span>SecurityAlert</span></xsl:if>
		<xsl:if test="$type = 'UserAuthenticated'"><span>UserAuthenticated</span></xsl:if>
		<xsl:if test="$type = 'AuditLogUsed'"><span>AuditLogUsed</span></xsl:if>
		<xsl:if test="$type = 'NetworkEntry'"><span>NetworkEntry</span></xsl:if>
	</xsl:template>
</xsl:stylesheet>

