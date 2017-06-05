<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:internal="urn:my-internal-data">

<xsl:output method="html">
</xsl:output>
<xsl:variable name="page_title">User Admin Console</xsl:variable>
<xsl:include href="../page.xsl"/>
<xsl:template match="model">

		<table width="70%" border="0" bordercolor="#ffffff" cellspacing="5" cellpadding="0">
		<tr>	<center>
			<td>
				<tr>
					<td width="25%"><h2>User ID</h2></td>
					<td colspan="4" width="65%"><h2>Roles</h2></td>
					<td colspan="2" width="10%" align="center"><a href="user_new.m?newUser=new"><img src="images/addpat.gif" alt="add new user" border="0"/></a></td>
				</tr>
					<xsl:apply-templates select="userList/item">
						<xsl:sort data-type="text" order="ascending" select="userID"/>
					</xsl:apply-templates>
			</td>	</center>
		</tr>
		</table>
		<DL>
		<DT>WebUser:</DT>
		<DD>A user in this role is allowed to use this web interface.</DD>
		<DT>WebAdmin:</DT>
		<DD>A user in this role has admin rights in the web interface.</DD>
		<DD>The user must be also in role WebUser!</DD>
		<DT>Admin:</DT>
		<DD>This role is used to allow configuration access via JMX console.</DD>
		<DT>AuditRep:</DT>
		<DD>Members of this role are allowed to access the Audit Repository.</DD>
		</DL>


</xsl:template>

	<xsl:template match="item[@type='org.dcm4chex.archive.web.maverick.admin.DCMUser']">
		<tr>
	        <td title="User ID" valign="top" >
				<xsl:value-of select="userID"/>
			</td>
																	
	        <td title="WebUser">
				WebUser<input type="checkbox" name="webUser" value="true" disabled="true">
					<xsl:if test="webUser = 'true'">
						<xsl:attribute name="checked"/>
					</xsl:if>
				</input>
	        </td>
																	
	        <td title="WebAdmin">
				WebAdmin<input type="checkbox" name="webAdmin" value="true" disabled="true">
					<xsl:if test="webAdmin = 'true'">
						<xsl:attribute name="checked"/>
					</xsl:if>
				</input>
	        </td>
																	
	        <td title="Admin">
				Admin<input type="checkbox" name="jBossAdmin" value="true" disabled="true">
					<xsl:if test="JBossAdmin = 'true'">
						<xsl:attribute name="checked"/>
					</xsl:if>
				</input>
	        </td>
	        <td title="AuditRep">
				AuditRep<input type="checkbox" name="arrUser" value="true" disabled="true">
					<xsl:if test="arrUser = 'true'">
						<xsl:attribute name="checked"/>
					</xsl:if>
				</input>
	        </td>
			<td align="center" valign="top" >
				<a href="user_edit.m?userID={userID}">
					<img src="images/edit.gif" alt="edit" border="0"/>		
				</a>
	        </td>
			<td align="left" valign="top" >
					<a href="user_delete.m?userID={userID}" onclick="return confirm('Are you sure you want to delete?')">
					<img src="images/delete.gif" alt="delete" border="0"/>							
					</a>					
			</td>
		</tr>
	</xsl:template>
 
</xsl:stylesheet>


