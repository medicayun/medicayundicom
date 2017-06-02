$! Child list creates a list of child items from a list contained in a layout object !$
$layout.items:{
<li class="$if(layout.selected.(it.(layout.itemId)))$active$endif$ $layout.extraClass$"><a id="$layout.id$.$it.(layout.itemId);format="xml"$" href="#" onclick="return controller.itemAction('$layout.id$',event,'$it.(layout.itemId);format="xml"$');">
	$if(layout.listTemplate)$$(layout.listTemplate)()$
	$else$$it.(layout.childItem);format="xml"$
	$endif$
</a>
</li>
}$