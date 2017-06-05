<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:internal="urn:my-internal-data">

   <xsl:output method="html" indent="yes" encoding="ISO-8859-1" />

   <xsl:variable name="page_title">Series Edit</xsl:variable>
   <xsl:include href  = "page.xsl" />

   <xsl:template match="model">
      <form action="seriesUpdate.m" method="post">
         <input name="patPk" type="hidden" value="{patPk}" />
         <input name="studyPk" type="hidden" value="{studyPk}" />
         <input name="seriesPk" type="hidden" value="{seriesPk}" />
				 <xsl:apply-templates select="series"/>
      </form>
   </xsl:template>
	 
   <xsl:template match="series">

		   <table border="1" cellspacing="0" cellpadding="0" width="100%"><tr><td>
           <table border="0">
            <tr>
               <td class="label" bgcolor="#eeeeee">Series Instance UID:</td>
               <td>
								 <xsl:value-of select="seriesIUID"/>
							 </td>
            </tr>
            <tr>
               <td class="label" bgcolor="#eeeeee">Series Number:</td>
               <td>							 
                  <input size="16" name="seriesNumber" type="text" value="{seriesNumber}" />
							 </td>
            </tr>
            <tr>
               <td class="label" bgcolor="#eeeeee">Modality:</td>
               <td>							 
                  <input size="16" name="modality" type="text" value="{modality}" />
							 </td>
            </tr>
            <tr>
               <td class="label" bgcolor="#eeeeee">Body Part Examined:</td>
               <td>							 
                  <input size="16" name="bodyPartExamined" type="text" value="{bodyPartExamined}" />
							 </td>
            </tr>
            <tr>
               <td class="label" bgcolor="#eeeeee">Laterality:</td>
               <td>							 
                  <input size="16" name="laterality" type="text" value="{laterality}" />
							 </td>
            </tr>
						<tr>
               <td class="label" bgcolor="#eeeeee">Series Description:</td>
               <td>							 
                  <input size="64" name="seriesDescription" type="text" value="{seriesDescription}" />
							 </td>
            </tr>
            <tr>
               <td class="label" bgcolor="#eeeeee">Date/Time [yyyy/mm/dd hh:mm:ss]:</td>
               <td>							 
                  <input size="20" name="seriesDateTime" type="text" value="{seriesDateTime}" />
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

