<?xml version="1.0" encoding="UTF-8"?>
<!--
 $Id: ko_view.xsl 2237 2006-01-25 16:28:35Z javawilli $
 -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="html" indent="no" encoding="ISO-8859-1"/>
   
<xsl:template match="/model">
<html>
<head>
   <title><xsl:value-of select="patient/patientName" /> (<xsl:value-of select="patient/patientSex" />)</title>
</head>
<body>
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
    		var _info = navigator.userAgent; 
    		var _ns = false; 
    		var _ns6 = false;
    		var _ie = (_info.indexOf("MSIE") &gt; 0 &amp;&amp; _info.indexOf("Win") &gt; 0 &amp;&amp; _info.indexOf("Windows 3.1") &lt; 0);
	//--&gt;</xsl:text></script>
    	<comment>
        	<script LANGUAGE="JavaScript1.1"><xsl:text>&lt;!--
        		var _ns = (navigator.appName.indexOf("Netscape") &gt;= 0 &amp;&amp; ((_info.indexOf("Win") &gt; 0 &amp;&amp; _info.indexOf("Win16") &lt; 0 &amp;&amp; java.lang.System.getProperty("os.version").indexOf("3.5") &lt; 0) || (_info.indexOf("Sun") &gt; 0) || (_info.indexOf("Linux") &gt; 0) || (_info.indexOf("AIX") &gt; 0) || (_info.indexOf("OS/2") &gt; 0) || (_info.indexOf("IRIX") &gt; 0)));
        		var _ns6 = ((_ns == true) &amp;&amp; (_info.indexOf("Mozilla/5") &gt;= 0));
		//--&gt;</xsl:text></script>
    	</comment>

	<script LANGUAGE="JavaScript"><xsl:text>&lt;!--
    		if (_ie == true) 
			document.writeln('&lt;object classid="clsid:8AD9C840-044E-11D1-B3E9-00805F499D93" WIDTH = '+W+' HEIGHT = '+H+'  codebase="http://java.sun.com/products/plugin/autodl/jinstall-1_4-windows-i586.cab#Version=1,4,0,0" &gt; \
			&lt;noembed&gt; &lt;XMP&gt;\
			&lt;APPLET  CODE = "com.tiani.jvision.applet.DisplayApplet.class" ARCHIVE = "../WebViewer/jvapplet.jar" WIDTH = "600" HEIGHT = "600" &gt; &lt;/XMP&gt;\
		                       &lt;PARAM NAME = "CODE" VALUE = "com.tiani.jvision.applet.DisplayApplet.class" /&gt; \
		    	        &lt;PARAM NAME = "ARCHIVE" VALUE = "../WebViewer/jvapplet.jar" /&gt; \
			        &lt;PARAM NAME = "DB_FIRST_NAME" VALUE="</xsl:text><xsl:call-template name="firstName" /><xsl:text>" /&gt; \
			        &lt;PARAM NAME ="DB_LAST_NAME" VALUE ="</xsl:text><xsl:call-template name="lastName" /><xsl:text>"/&gt; \
			        &lt;PARAM NAME ="DB_SEX" VALUE ="</xsl:text><xsl:value-of select="patient/patientSex" /><xsl:text>" /&gt; \
			        &lt;PARAM NAME ="DB_BIRTH_DATE" VALUE ="</xsl:text><xsl:call-template name="birthDate" /><xsl:text>" /&gt; \
			        &lt;PARAM NAME = "KEYNOTE" VALUE="</xsl:text><xsl:value-of select="sopIUID" /><xsl:text>" /&gt; \
			        &lt;PARAM NAME = "IMAGE_SERVLET" VALUE ="/WebViewer/servlet/GetImageServlet?Q=ORIG&amp;IMGUID=" /&gt;  \
   			        &lt;PARAM NAME="type" VALUE="application/x-java-applet;version=1.4" /&gt; \
   			        &lt;PARAM NAME="scriptable" VALUE="false" /&gt; \
			&lt;/APPLET&gt; \
			&lt;/noembed&gt; \
			&lt;/object&gt;');
    		else if (_ns == true &amp;&amp; _ns6 == false) 
			document.writeln('&lt;embed \
	    			type="application/x-java-applet;version=1.4" \
            			CODE = "com.tiani.jvision.applet.DisplayApplet.class" \
            			ARCHIVE = "../WebViewer/jvapplet.jar" \
            			WIDTH = '+W+' \
            			HEIGHT ='+H+' \
    		                        DB_FIRST_NAME="</xsl:text><xsl:call-template name="firstName" /><xsl:text>" \
    		                        DB_LAST_NAME="</xsl:text><xsl:call-template name="lastName" /><xsl:text>" \
    		                        DB_SEX="</xsl:text><xsl:value-of select="patient/patientSex" /><xsl:text>" \
    		                        DB_BIRTH_DATE="</xsl:text><xsl:call-template name="birthDate" /><xsl:text>" \
    		                        KEYNOTE="</xsl:text><xsl:value-of select="sopIUID" /><xsl:text>" \
    		                        IMAGE_SERVLET="/WebViewer/servlet/GetImageServlet?Q=ORIG&amp;IMGUID="  \
	    			scriptable=false \
	    			pluginspage="http://java.sun.com/products/plugin/index.html#download"&gt;');
		else
			document.writeln('&lt;APPLET  CODE = "com.tiani.jvision.applet.DisplayApplet.class" ARCHIVE = "../WebViewer/jvapplet.jar" WIDTH ='+W+' HEIGHT = '+H+' &gt;\
		                       &lt;PARAM NAME = "CODE" VALUE = "com.tiani.jvision.applet.DisplayApplet.class" /&gt; \
		    	        &lt;PARAM NAME = "ARCHIVE" VALUE = "../WebViewer/jvapplet.jar" /&gt; \
			        &lt;PARAM NAME = "DB_FIRST_NAME" VALUE="</xsl:text><xsl:call-template name="firstName" /><xsl:text>" /&gt; \
			        &lt;PARAM NAME ="DB_LAST_NAME" VALUE ="</xsl:text><xsl:call-template name="lastName" /><xsl:text>"/&gt; \
			        &lt;PARAM NAME ="DB_SEX" VALUE ="</xsl:text><xsl:value-of select="patient/patientSex" /><xsl:text>" /&gt; \
			        &lt;PARAM NAME ="DB_BIRTH_DATE" VALUE ="</xsl:text><xsl:call-template name="birthDate" /><xsl:text>" /&gt; \
			        &lt;PARAM NAME = "KEYNOTE="</xsl:text><xsl:value-of select="sopIUID" /><xsl:text>" /&gt; \
			        &lt;PARAM NAME = "IMAGE_SERVLET" VALUE ="/WebViewer/servlet/GetImageServlet?Q=ORIG&amp;IMGUID=" /&gt;  \
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

