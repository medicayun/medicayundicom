$stringtemplate()$

$if(templatesName)$var $templatesName$ = $endif$
new StringTemplate({ 
$templates:{template|$\n$'$template.name$':$template(template=template)$}; separator=",  "$
  })$if(templatesName)$;$endif$
