$! Call with name set to the child layout to call if available !$
$if(layout.(name))$
$if(layout.(name).used)$
$(layout.(name).template)(layout=layout.(name))$
$endif$
$endif$