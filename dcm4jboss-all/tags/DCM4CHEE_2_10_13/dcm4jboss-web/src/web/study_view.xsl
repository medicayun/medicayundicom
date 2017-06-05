<?xml version="1.0" encoding="UTF-8"?>
<!--
 $Id: study_view.xsl 3130 2007-02-19 11:04:15Z javawilli $
 -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="html" indent="no" encoding="UTF-8"/>
   
<xsl:template match="/model">
<html>
<head>
	<title><xsl:value-of select="patient/patientName" /> (<xsl:value-of select="patient/patientSex" />)</title>
	<script language = "JavaScript" src= "dcm4che.js"/>
</head>
<body onLoad="checkPopup('{popupMsg}')" >
	<script language="JavaScript"><xsl:text>&lt;!--
        	var W=600,H=600;
                if (parseInt(navigator.appVersion)>3) 
		{
			if (navigator.appName=="Netscape") 
			{
				W = window.innerWidth-20;
				H = window.innerHeight-35;
			}
			if (navigator.appName.indexOf("Microsoft")!=-1) 
			{
				W = document.body.offsetWidth-30;
				H = document.body.offsetHeight-35;
			}
			else
			{
				W = window.innerWidth-15;
				H = window.innerHeight-20;
			}
		}
	//--&gt;</xsl:text></script>

	<script LANGUAGE="JavaScript"><xsl:text>&lt;!--
			document.writeln('&lt;APPLET  CODE = "com.tiani.jvision.applet.DisplayApplet.class" ARCHIVE = "../WebViewer/jvapplet.jar" WIDTH ='+W+' HEIGHT = '+H+' &gt;\
		                       &lt;PARAM NAME = "CODE" VALUE = "com.tiani.jvision.applet.DisplayApplet.class" /&gt; \
		    	        &lt;PARAM NAME = "ARCHIVE" VALUE = "../WebViewer/jvapplet.jar" /&gt; \
			        &lt;PARAM NAME = "DB_FIRST_NAME" VALUE="</xsl:text><xsl:call-template name="firstName" /><xsl:text>" /&gt; \
			        &lt;PARAM NAME ="DB_LAST_NAME" VALUE ="</xsl:text><xsl:call-template name="lastName" /><xsl:text>"/&gt; \
			        &lt;PARAM NAME ="DB_SEX" VALUE ="</xsl:text><xsl:value-of select="patient/patientSex" /><xsl:text>" /&gt; \
			        &lt;PARAM NAME ="DB_BIRTH_DATE" VALUE ="</xsl:text><xsl:call-template name="birthDate" /><xsl:text>" /&gt; \
			        &lt;PARAM NAME = "SELECT_SEQUENCE" VALUE="</xsl:text><xsl:value-of select="selectedSeries" /><xsl:text>" /&gt; \
			        </xsl:text>    <xsl:apply-templates select="studyContainer/series/item"/>
    		                        <xsl:text>&lt;PARAM NAME = "IMAGE_SERVLET" VALUE ="/WebViewer/servlet/GetImageServlet?IMGUID=" /&gt;  \
    			        &lt;PARAM NAME="type" VALUE="application/x-java-applet;version=1.4" /&gt; \
    			        &lt;PARAM NAME="scriptable" VALUE="false" /&gt; \
			&lt;/APPLET&gt; ');
    		                            
	//--&gt;</xsl:text></script>
	

 
	<!--"END_CONVERTED_APPLET"-->

</body>
</html>
	    
</xsl:template>
    
<xsl:template name="firstName">
    <xsl:variable name="pat"><xsl:value-of select="patient/patientName"/></xsl:variable>
    <xsl:choose>
        <xsl:when test="contains($pat,'^')">
             <xsl:value-of select="substring-after($pat,'^')"/>
        </xsl:when>
    </xsl:choose>
</xsl:template>

<xsl:template name="lastName">
    <xsl:variable name="pat"><xsl:value-of select="patient/patientName"/></xsl:variable>
    <xsl:choose>
        <xsl:when test="contains($pat,'^')">
             <xsl:value-of select="substring-before($pat,'^')"/>
        </xsl:when>
        <xsl:otherwise>
            <xsl:value-of select="$pat"/>    
        </xsl:otherwise>
    </xsl:choose>
</xsl:template>
    
<xsl:template match="item[@type='org.dcm4chex.archive.web.maverick.StudyViewCtrl$SeriesContainer']">
    <xsl:if test="modality != 'SR' and modality != 'KO' ">
        <xsl:text>&lt;PARAM NAME="SEQUENCE</xsl:text><xsl:value-of select="position()"/><xsl:text>" VALUE="Seq. Nr. </xsl:text><xsl:value-of select="seriesNumber"/><xsl:apply-templates select="instanceUIDs/item"></xsl:apply-templates><xsl:text>"&gt; \
                                    </xsl:text>
        </xsl:if>
</xsl:template>
    
<xsl:template name="ieSeries" >
     <xsl:if test="modality != 'SR' and modality != 'KO' ">
            <xsl:text>SEQUENCE</xsl:text><xsl:value-of select="position()"/><xsl:text>="Seq. Nr. </xsl:text><xsl:value-of select="seriesNumber"/>
                <xsl:apply-templates select="instanceUIDs/item"></xsl:apply-templates>
        <xsl:text>" \
                                                                                            </xsl:text>
        </xsl:if>
</xsl:template>
    
<xsl:template match="item[@type='java.lang.String']">
	<xsl:text>;</xsl:text><xsl:value-of select="." />
</xsl:template>

<xsl:template name="birthDate">
    <xsl:variable name="bd"><xsl:value-of select="patient/patientBirthDate"/></xsl:variable>
    <xsl:choose>
        <xsl:when test="contains($bd,'/')">
             <xsl:value-of select="substring-before($bd,'/')"/>
             <xsl:variable name="bd1"><xsl:value-of select="substring-after($bd,'/')"/></xsl:variable>
             <xsl:value-of select="substring-before($bd1,'/')"/>
             <xsl:value-of select="substring-after($bd1,'/')"/>
        </xsl:when>
        <xsl:otherwise>
            <xsl:value-of select="$bd"/>    
        </xsl:otherwise>
    </xsl:choose>
</xsl:template>

</xsl:stylesheet>

