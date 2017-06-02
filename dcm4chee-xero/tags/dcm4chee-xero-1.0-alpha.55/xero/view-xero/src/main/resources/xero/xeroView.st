$! Defines the view components for the combined query/display view. !$
$if(templates)$
$jstemplate(templatesName="xeroView")$
$else$
alert("WARNING - no templates found.");
$endif$
