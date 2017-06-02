<div class="$layout.displayClass$ layout" $html/style()$ $if(layout.onmousewheel)$ onmousewheel="return $layout.onmousewheel$('$layout.id$',window.event,this)"$endif$>
$if(layout.north)$$(layout.north.template)(layout=layout.north)$$endif$
$if(layout.west)$$(layout.west.template)(layout=layout.west)$$endif$
$if(layout.east)$$(layout.east.template)(layout=layout.east)$$endif$
$layout.layouts:{layout|$if(layout.used)$$(layout.template)()$$endif$}$
$if(layout.south)$$(layout.south.template)(layout=layout.south)$$endif$
</div>