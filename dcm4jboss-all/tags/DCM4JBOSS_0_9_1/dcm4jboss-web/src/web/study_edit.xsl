<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:internal="urn:my-internal-data">

   <xsl:output method="html" indent="yes" encoding="ISO-8859-1" />

   <xsl:variable name="page_title">Study Edit</xsl:variable>
   <xsl:include href  = "page.xsl" />

   <xsl:template match="model">
      <form action="studyUpdate.m" method="post">
         <input name="patPk" type="hidden" value="{patPk}" />
         <input name="studyPk" type="hidden" value="{studyPk}" />
				 <xsl:apply-templates select="study"/>
      </form>
   </xsl:template>
	 
   <xsl:template match="study">

		   <table border="1" cellspacing="0" cellpadding="0" width="100%"><tr><td>
           <table border="0">
            <tr>
               <td class="label" bgcolor="#eeeeee">Study Instance UID:</td>
               <td>
								 <xsl:value-of select="studyIUID"/>
							 </td>
            </tr>
            <tr>
               <td class="label" bgcolor="#eeeeee">Study ID:</td>
               <td>							 
                  <input size="16" name="studyID" type="text" value="{studyID}" />
							 </td>
            </tr>
            <tr>
               <td class="label" bgcolor="#eeeeee">Accession Number:</td>
               <td>							 
                  <input size="16" name="accessionNumber" type="text" value="{accessionNumber}" />
							 </td>
            </tr>
            <tr>
            <tr>
               <td class="label" bgcolor="#eeeeee">Referring Physician:</td>
               <td>							 
                  <input size="64" name="referringPhysician" type="text" value="{referringPhysician}" />
							 </td>
            </tr>
               <td class="label" bgcolor="#eeeeee">Study Description:</td>
               <td>							 
                  <input size="64" name="studyDescription" type="text" value="{studyDescription}" />
							 </td>
            </tr>
            <tr>
               <td class="label" bgcolor="#eeeeee">Date/Time [yyyy/mm/dd hh:mm]:</td>
               <td>							 
                  <input size="16" name="studyDateTime" type="text" value="{studyDateTime}" />
							 </td>
            </tr>
            <tr>
               <td class="label" bgcolor="#eeeeee">Modalities in Study:</td>
               <td>							 
								 <xsl:value-of select="modalitiesInStudy"/>
							 </td>
            </tr>
            <tr>
               <td class="label" bgcolor="#eeeeee">Number of Series:</td>
               <td>							 
								 <xsl:value-of select="numberOfSeries"/>
							 </td>
            </tr>
            <tr>
               <td class="label" bgcolor="#eeeeee">Number of Instances:</td>
               <td>							 
								 <xsl:value-of select="numberOfInstances"/>
							 </td>
            </tr>
						<tr>
               <td class="label" bgcolor="#eeeeee">Availability:</td>
               <td>
								 <xsl:value-of select="availability"/>
               </td>
            </tr>
            <tr>
               <td class="label" bgcolor="#eeeeee">Retrieve AETs:</td>
               <td>							 
								 <xsl:value-of select="retrieveAETs"/>
							 </td>
            </tr>
            <tr>
               <td align="left">
									<input type="submit" name="submit" value="Update" />
                  <input type="submit" name="cancel" value="Cancel" />
               </td>
            </tr>
         </table>
         </td></tr></table>
   </xsl:template>
</xsl:stylesheet>

