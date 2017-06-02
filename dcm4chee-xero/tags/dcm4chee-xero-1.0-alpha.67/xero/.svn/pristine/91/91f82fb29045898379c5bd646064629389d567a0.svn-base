<head>
  <meta http-equiv="Content-Type" content="text/xhtml"/>
  <meta content="initial-scale=1.0" name="viewport"/>

  $! TODO - combine the stylesheets dynamically !$
  <link type="text/css" rel="stylesheet" href="style/general.css?v=$version$"> </link>
  <link type="text/css" rel="stylesheet" href="theme.css?v=$version$&amp;theme=$i18n.theme$"> </link>
  $if(i18n.cssTheme)$
  <link type="text/css" rel="stylesheet" href="$i18n.cssTheme$?v=$version$"> </link>
  $endif$
  $if(IS_IE)$
    <style> v\:* { behavior: url(#default#VML); display: inline-block; overflow: hidden; position: absolute; }
    #SearchLayoutTable { width: 98%; margin-right: 16px; }
    $if(IS_IE6)$
    div.hoverMenu li {	filter:alpha(opacity=100);  }
    
    #StudyMenuS0Link, #StudyMenuS1Link {
       width:expression(Math.min(parseInt(this.offsetWidth), 180 ) + "px");
       overflow: hidden;
    }
    
    #SearchLayout { height: 95%; overflow: scroll;  }
    $else$
    #SearchLayout { overflow: scroll; bottom: 8px;  }
    $endif$
      </style>
  $elseif(IS_FIREFOX)$
  	<style>
  		table { height: 100%; }
  		tbody { overflow: auto; height: 95%; }
  		.studyTable { overflow: hidden; }
  	</style>
  $endif$
  <title>$i18n.Title$</title>
  
  $firebugInclude()$
  
  <link rel="icon" href="favicon.ico?v=$version$" type="image/x-icon" />
  <link rel="shortcut icon" href="favicon.ico?v=$version$" type="image/x-icon" /> 
</head>