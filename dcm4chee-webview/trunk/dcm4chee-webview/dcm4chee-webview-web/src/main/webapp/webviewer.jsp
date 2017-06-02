<%-- Version $Revision:$ $Date:$ --%>
 <%@ page import="java.util.Properties" %>
 <%@ page import="java.util.Iterator" %>
 <%@ page import="java.util.List" %>
 <jsp:useBean id="delegate" class="org.dcm4chex.webview.WebViewDelegate" scope="request"/>
 <%
    delegate.setWebviewServiceName("dcm4chee.archive:service=WebViewService");
 	Properties props = delegate.getLaunchProperties( request.getParameterMap() );
	String mode = (String) props.remove("launchMode");
	String key;
 %>
<html><head><META http-equiv="Content-Type" content="text/html; charset=UTF-8"><%
%><title><%=props.getProperty("title")%></title><%
%></head><%
	if ( "applet".equals( mode ) ) {
%><body onResize="window.location.href = window.location.href;" >
	<script language="JavaScript"><!--
		var W="95%",H="95%";
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

		document.writeln('<APPLET WIDTH ='+W+' HEIGHT = '+H+' >\
		<%      
		for ( Iterator iter = props.keySet().iterator() ; iter.hasNext() ;) {	
			key = (String) iter.next();
			%><PARAM NAME = "<%=key %>" VALUE ="<%=props.getProperty(key) %>" />  \
	<% } 
			%><PARAM NAME="type" VALUE="application/x-java-applet;version=1.4" /> \
			<PARAM NAME="scriptable" VALUE="false" /> \
		</APPLET> ');
	//--></script>
</body>
<% } else if ( "pr_select".equals(mode) ) { 
		String url = request.getRequestURL().append( "?ignorePR=true&amp;" ).append( request.getQueryString() ).toString();
%>
<body>
	<table border="0" cellspacing="0" cellpadding="0" width="100%">
		<tr>
			<td>
				<a title="Without Presentation State" href="<%= url%>">No Presentation State</a>
			</td>
		</tr>
<%
	for ( Iterator iter=props.keySet().iterator() ; iter.hasNext() ; ) {
		key = (String) iter.next();
%>		
		<tr>
			<td>
				<a title="Presentation State" href="webviewer.jsp?prUID=<%=key %>">Presentation State (<%=props.getProperty(key) %>)</a>
			</td>
		</tr>
<% 
	} %>

	</table>
</body>	
<% } else if ( "study_select".equals(mode) ) { 
%>
<body>
	<h1>More than one study found! Please choose one of the list:</h1>
	<table border="0" cellspacing="0" cellpadding="0" width="100%">
<%
	for ( Iterator iter=props.keySet().iterator() ; iter.hasNext() ; ) {
		key = (String) iter.next();
%>		
		<tr>
			<td>
				<a title="Study" href="webviewer.jsp?studyUID=<%=key %>">Study: <%=props.getProperty(key) %></a>
			</td>
		</tr>
<% 
	} %>

	</table>
</body>
<% } else if ( "empty".equals(mode) ) { 
%>
<body>
	<h1>Nothing found !!!</h1>
</body>
<% } else if ( "error".equals(mode) ) { 
%>
<body>
	<h1><%= props.getProperty("SEVERITY") %></h1>
	<h2><%= props.getProperty("MESSAGE") %></h2>
</body>
<% } %>
</html>
