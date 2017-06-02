<div id="$layout.id$">
$if(layout.template)$
$(layout.template)()$
$elseif(layout.template)$

<!-- List templates here that are required but are listed indirectly - temporary until restart. -->

$else$
No layout found.
$endif$
</div>