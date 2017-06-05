<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="xml"
		doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"
		doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"
        indent = "yes"/>
	<!-- root document -->
	<xsl:template match="/">
		<html>
			<head>
				<title>Audit Repository Log</title>
				<link rel="stylesheet" type="text/css" href="arr-style.css"/>
			</head>
			<body>
				<xsl:apply-templates select="query"/>
			</body>
		</html>
	</xsl:template>
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
    <h1>Audit Record Repository List View</h1><hr/>
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
      <b>Results: </b>
      from <xsl:value-of select="@start + 1"/> to <xsl:value-of select="@start + $nrec"/> (of <xsl:value-of select="@tot-nrecords"/>)
      <xsl:if test="@pagesize = -1"> (all)</xsl:if>
    </xsl:if>
    <!-- main form -->
    <table>
      <tr>
        <td>
          <xsl:if test="((@start &gt; 0) or ($eof = 'false')) and (@pagesize != 0)">
            <!-- pager -->
            <table class="pager">
              <tr>
                <td>
                  <xsl:if test="@start &gt; 0">
                    <xsl:variable name="startentry">
                      <xsl:choose>
                        <xsl:when test="@start - @pagesize &lt; 0"><xsl:value-of select="number(0)"/></xsl:when>
                        <xsl:otherwise><xsl:value-of select="@start - @pagesize"/></xsl:otherwise>
                      </xsl:choose>
                    </xsl:variable>
                    <a href="/dcm4jboss-arr/?type={@type}&amp;host={@host}&amp;from={@from}&amp;to={@to}&amp;aet={@aet}&amp;username={@username}&amp;patientname={@patientname}&amp;patientid={@patientid}&amp;start={$startentry}&amp;pagesize={@pagesize}{$sortbystr}">&lt;&lt; Prev</a>
                  </xsl:if>
                </td>
                <td>
                  <xsl:if test="$eof = 'false'">
                    <a href="/dcm4jboss-arr/?type={@type}&amp;host={@host}&amp;from={@from}&amp;to={@to}&amp;aet={@aet}&amp;username={@username}&amp;patientname={@patientname}&amp;patientid={@patientid}&amp;start={@start + @pagesize}&amp;pagesize={@pagesize}{$sortbystr}">Next &gt;&gt;</a>
                  </xsl:if>
                </td>
              </tr>
            </table>
          </xsl:if>
        </td>
      </tr>
      <tr>
        <td>
          <!-- query -->
          <table class="query">
            <form name="frmArrQuery" method="get" action="/dcm4jboss-arr">
              <xsl:variable name="pagesize_form">
                <xsl:choose>
                  <xsl:when test="@pagesize &lt;= 0"><xsl:value-of select="number(30)"/></xsl:when> <!-- DEFAULT PAGE SIZE -->
                  <xsl:otherwise><xsl:value-of select="@pagesize"/></xsl:otherwise>
                </xsl:choose>
              </xsl:variable>
              <tr>
                <td><input class="query-submit" type="submit" value="Query"/></td>
                <td><input name="update" type="checkbox" value="1">Update view</input></td>
                <!--<td colspan="1">
                  <button class="query-submit" name="reset" onClick="frmArrQuery.reset();">Clear</button>
                </td>-->
              </tr>
              <tr><td>Results to return:</td><td><input class="text" name="pagesize" type="text" size="5" maxlength="5" value="{$pagesize_form}"/></td></tr>
              <tr><td>Patient Name:</td><td><input class="text" name="patientname" type="text" value="{@patientname}"/></td></tr>
              <tr><td>Patient ID:</td><td><input class="text" name="patientid" type="text" value="{@patientid}"/></td></tr>
              <tr><td>AE Title:</td><td><input class="text" name="aet" type="text" value="{@aet}"/></td></tr>
              <tr><td>Username:</td><td><input class="text" name="username" type="text" value="{@username}"/></td></tr>
              <tr><td>Host:</td><td><input class="text" name="host" type="text" value="{@host}"/></td></tr>
              <tr><td>TimeStamp</td></tr>
              <tr><td>>from:</td><td><input class="text" name="from" type="text" value="{@from}"/></td></tr>
              <tr><td>>to:</td><td><input class="text" name="to" type="text" svalue="{@to}"/></td></tr>
              <tr><td>Type</td></tr>
              <tr><td colspan="2"><input type="checkbox" name="type" value="ActorConfig">ActorConfig</input></td></tr>
              <tr><td colspan="2"><input type="checkbox" name="type" value="ActorStartStop">ActorStartStop</input></td></tr>
              <tr><td colspan="2"><input type="checkbox" name="type" value="AuditLogUsed">AuditLogUsed</input></td></tr>
              <tr><td colspan="2"><input type="checkbox" name="type" value="BeginStoringInstances">BeginStoringInstances</input></td></tr>
              <tr><td colspan="2"><input type="checkbox" name="type" value="DICOMInstancesDeleted">DICOMInstancesDeleted</input></td></tr>
              <tr><td colspan="2"><input type="checkbox" name="type" value="DICOMInstancesUsed">DICOMInstancesUsed</input></td></tr>
              <tr><td colspan="2"><input type="checkbox" name="type" value="DicomQuery">DicomQuery</input></td></tr>
              <tr><td colspan="2"><input type="checkbox" name="type" value="Export">Export</input></td></tr>
              <tr><td colspan="2"><input type="checkbox" name="type" value="Import">Import</input></td></tr>
              <tr><td colspan="2"><input type="checkbox" name="type" value="InstancesSent">InstancesSent</input></td></tr>
              <tr><td colspan="2"><input type="checkbox" name="type" value="InstancesStored">InstancesStored</input></td></tr>
              <tr><td colspan="2"><input type="checkbox" name="type" value="NetworkEntry">NetworkEntry</input></td></tr>
              <tr><td colspan="2"><input type="checkbox" name="type" value="OrderRecord">OrderRecord</input></td></tr>
              <tr><td colspan="2"><input type="checkbox" name="type" value="PatientRecord">PatientRecord</input></td></tr>
              <tr><td colspan="2"><input type="checkbox" name="type" value="ProcedureRecord">ProcedureRecord</input></td></tr>
              <tr><td colspan="2"><input type="checkbox" name="type" value="SecurityAlert">SecurityAlert</input></td></tr>
              <tr><td colspan="2"><input type="checkbox" name="type" value="StudyDeleted">StudyDeleted</input></td></tr>
              <tr><td colspan="2"><input type="checkbox" name="type" value="UserAuthenticated">UserAuthenticated</input></td></tr>
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
          </table>
        </td>
        <td>
          <xsl:if test="$nrec != 0">
            <!-- audit record list -->
 	          <table class="c1">
	            <tr class="head">
	              <td><a href="/dcm4jboss-arr?{$lastquery}&amp;orderby=type&amp;orderdir={$vsortdir-type}"><div class="width: 100%">Audit Event</div></a></td>
	              <td><a href="/dcm4jboss-arr?{$lastquery}&amp;orderby=host&amp;orderdir={$vsortdir-host}"><div class="width: 100%">Host</div></a></td>
	              <td><a href="/dcm4jboss-arr?{$lastquery}&amp;orderby=timestamp&amp;orderdir={$vsortdir-timestamp}"><div class="width: 100%">Timestamp (local)</div></a></td>
	              <td>Information</td>
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
	                <td><xsl:value-of select="@host"/></td>
	                <td><xsl:value-of select="@timestamp"/></td>
	                <td><xsl:call-template name="Info"/></td>
	                <td><a href="arr-view.do?pk={@pk}&amp;viewxml=1">(xml)</a></td>
	              </tr>
	            </xsl:for-each>
	          </table>
          </xsl:if>
        </td>
      </tr>
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
		<xsl:if test="$type = 'Import'"><span class="event">Import</span></xsl:if>
		<xsl:if test="$type = 'InstancesStored'"><span class="event">InstancesStored</span></xsl:if>
		<xsl:if test="$type = 'ProcedureRecord'"><span class="event">ProcedureRecord</span></xsl:if>
		<xsl:if test="$type = 'ActorStartStop'"><span class="event">ActorStartStop</span></xsl:if>
		<xsl:if test="$type = 'ActorConfig'"><span class="event">ActorConfig</span></xsl:if>
		<xsl:if test="$type = 'Export'"><span class="event">Export</span></xsl:if>
		<xsl:if test="$type = 'DICOMInstancesDeleted'"><span class="event-important">DICOMInstancesDeleted</span></xsl:if>
		<xsl:if test="$type = 'PatientRecord'"><span class="event">PatientRecord</span></xsl:if>
		<xsl:if test="$type = 'OrderRecord'"><span class="event">OrderRecord</span></xsl:if>
		<xsl:if test="$type = 'BeginStoringInstances'"><span class="event">BeginStoringInstances</span></xsl:if>
		<xsl:if test="$type = 'InstancesSent'"><span class="event">InstancesSent</span></xsl:if>
		<xsl:if test="$type = 'DICOMInstancesUsed'"><span class="event">DICOMInstancesUsed</span></xsl:if>
		<xsl:if test="$type = 'StudyDeleted'"><span class="event-important">StudyDeleted</span></xsl:if>
		<xsl:if test="$type = 'DicomQuery'"><span class="event">DicomQuery</span></xsl:if>
		<xsl:if test="$type = 'SecurityAlert'"><span class="event-alert">SecurityAlert</span></xsl:if>
		<xsl:if test="$type = 'UserAuthenticated'"><span class="event">UserAuthenticated</span></xsl:if>
		<xsl:if test="$type = 'AuditLogUsed'"><span class="event">AuditLogUsed</span></xsl:if>
		<xsl:if test="$type = 'NetworkEntry'"><span class="event">NetworkEntry</span></xsl:if>
	</xsl:template>
</xsl:stylesheet>

