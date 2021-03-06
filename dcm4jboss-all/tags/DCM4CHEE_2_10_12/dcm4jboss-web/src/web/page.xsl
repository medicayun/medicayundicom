<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<!--
	 	Enable/disable the folders to match project requirements
	 	TODO: Remove project specific hardcoded values
	 -->
	<xsl:param name="folder" select="'true'" />
	<xsl:param name="trash" select="'true'" />
	<xsl:param name="ae_mgr" select="'true'" />
	<xsl:param name="offline_storage" select="'false'" />
	<xsl:param name="mwl_console" select="'false'" />
	<xsl:param name="mpps_console" select="'false'" />
	<xsl:param name="gpwl_console" select="'false'" />
	<xsl:param name="gppps_console" select="'false'" />
	<xsl:param name="user_admin" select="'true'" />
	<xsl:param name="audit_repository" select="'true'" />

	<xsl:template match="/">
		<html>
		<head>
			<meta http-equiv="content-type" content="text/html; charset=UTF-8" />
			<title><xsl:copy-of select="$page_title" /></title>
			<script language = "JavaScript" src= "dcm4che.js"/>
			<link rel="stylesheet" href="stylesheet.css" type="text/css"/>
		</head>
		<body onLoad="checkError('{model/errorCode}');checkPopup('{model/popupMsg}')" bgcolor="#FFFFFF" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0" link="#FF0000" alink="#FF0000" vlink="#FF0000">
		<table class="dcm4chee_header" width="100%" cellspacing="0">
	  		<tr valign="middle" style="center">
	    		<td class="logo" width="50" align="left"><img class="logo" src="images/logo.gif" border="0"/></td>
	    		<xsl:if test="$folder='true'">
		    		<td width="100" align="center">
		    			<xsl:choose>
			    			<xsl:when test="model/modelName = 'FOLDER'">
								<xsl:attribute name="class">tab-selected</xsl:attribute>
							</xsl:when>
							<xsl:otherwise>
								<xsl:attribute name="class">tab</xsl:attribute>
							</xsl:otherwise>
		    			</xsl:choose>
		    			<a class="tab" href="foldersubmit.m?filter=true">Folder</a>
		    		</td>
		      	</xsl:if>
	    		<xsl:if test="$trash='true'">
	    			<td width="100" align="center">
		    			<xsl:choose>
			    			<xsl:when test="model/modelName = 'TRASH'">
								<xsl:attribute name="class">tab-selected</xsl:attribute>
							</xsl:when>
							<xsl:otherwise>
								<xsl:attribute name="class">tab</xsl:attribute>
							</xsl:otherwise>
		    			</xsl:choose>
	    				<a class="tab" href="trashfolder.m?filter=true">Trash</a>
	    			</td>
		      	</xsl:if>
	    		<xsl:if test="$ae_mgr='true'">
		    		<td width="120" align="center">
		    			<xsl:choose>
			    			<xsl:when test="model/modelName = 'AEMgr'">
								<xsl:attribute name="class">tab-selected</xsl:attribute>
							</xsl:when>
							<xsl:otherwise>
								<xsl:attribute name="class">tab</xsl:attribute>
							</xsl:otherwise>
		    			</xsl:choose>
		    			<a class="tab" href="ae.m">AE Management</a>
		    		</td>
		      	</xsl:if>
	    		<xsl:if test="$offline_storage='true'">
		      		<td width="120" align="center">
		      			<xsl:choose>
		    				<xsl:when test="model/modelName = 'MCM'">
								<xsl:attribute name="class">tab-selected</xsl:attribute>
							</xsl:when>
							<xsl:otherwise>
								<xsl:attribute name="class">tab</xsl:attribute>
							</xsl:otherwise>
		    			</xsl:choose>
		      			<a class="tab" href="mcm_console.m">Offline Storage</a>
		      		</td>
		      	</xsl:if>
	    		<xsl:if test="$mwl_console='true'">
		      		<td width="120" align="center">
		      			<xsl:choose>
		    				<xsl:when test="model/modelName = 'MWL'">
								<xsl:attribute name="class">tab-selected</xsl:attribute>
							</xsl:when>
							<xsl:otherwise>
								<xsl:attribute name="class">tab</xsl:attribute>
							</xsl:otherwise>
		    			</xsl:choose>
		      			<a class="tab" href="mwl_console.m?filter.x=1">Worklist Console</a>
		      		</td>
		      	</xsl:if>
	    		<xsl:if test="$mpps_console='true'">
		      		<td width="120" align="center">
		      			<xsl:choose>
			    			<xsl:when test="model/modelName = 'MPPS'">
								<xsl:attribute name="class">tab-selected</xsl:attribute>
							</xsl:when>
							<xsl:otherwise>
								<xsl:attribute name="class">tab</xsl:attribute>
							</xsl:otherwise>
		    			</xsl:choose>
		      			<a class="tab" href="mpps_console.m?filter.x=1">MPPS Console</a>
		      		</td>
		      	</xsl:if>
	    		<xsl:if test="$gpwl_console='true'">
		      		<td width="120" align="center">
		      			<xsl:choose>
		    				<xsl:when test="model/modelName = 'GPWL'">
								<xsl:attribute name="class">tab-selected</xsl:attribute>
							</xsl:when>
							<xsl:otherwise>
								<xsl:attribute name="class">tab</xsl:attribute>
							</xsl:otherwise>
		    			</xsl:choose>
		      			<a class="tab" href="gpwl_console.m?filter.x=1">GP Worklist Console</a>
		      		</td>
		      	</xsl:if>
	    		<xsl:if test="$gppps_console='true'">
		      		<td width="120" align="center">
		      			<xsl:choose>
		    				<xsl:when test="model/modelName = 'GPPPS'">
								<xsl:attribute name="class">tab-selected</xsl:attribute>
							</xsl:when>
							<xsl:otherwise>
								<xsl:attribute name="class">tab</xsl:attribute>
							</xsl:otherwise>
		    			</xsl:choose>
		      			<a class="tab" href="gppps_console.m?filter.x=1">GPPPS Console</a>
		      		</td>
		      	</xsl:if>
	      		<td width="120" align="center">
	      			<xsl:choose>
		    			<xsl:when test="model/modelName = 'UserAdmin'">
							<xsl:attribute name="class">tab-selected</xsl:attribute>
						</xsl:when>
						<xsl:otherwise>
							<xsl:attribute name="class">tab</xsl:attribute>
						</xsl:otherwise>
    				</xsl:choose>
      				<a class="tab" href="useradmin_console.m">User Admin</a>
	      		</td>
	    		<xsl:if test="$audit_repository='true'">
			      	<td class="tab" width="120" align="center"><a class="tab" href="../dcm4chee-arr">Audit Repository</a></td>
		      	</xsl:if>
		      	<td class="tab" width="120" align="center"><a class="tab" href="foldersubmit.m?logout=">Logout</a></td>
				<td width="40%"></td>
			  	<xsl:apply-templates select="model"/>
		     </tr>
	     </table>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>
	
