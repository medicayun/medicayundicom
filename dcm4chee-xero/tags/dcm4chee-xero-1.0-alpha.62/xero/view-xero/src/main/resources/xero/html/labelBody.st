<a id='$layout.id$Link' tabindex="-1" $if(layout.title)$title="$layout.title;format="xml"$"$endif$ class="jslink" $if(layout.action)$onmouseup="return controller.itemAction('$layout.id$',event);" href="#" onmousedown="return cancelEvent(event);"
$elseif(layout.href)$ href="$layout.href;format="xml"$?v=$version$" $if(layout.target)$target="$layout.target$"$endif$
$elseif(layout.area)$onmousedown="return controller.mouseMenu('$layout.id$',event);"$else$ onclick="return cancelEvent(event);" href="#"$endif$>
$if(layout.i18n)$
$if(i18n.(layout.icon))$
<img id='$layout.id$Img' class="$if(layout.isActive)$active $endif$icon" src="$i18n.(layout.icon)$?v=$version$" alt="$i18n.(layout.i18n);format="xml"$" />
$if(layout.showLabel)$ $i18n.(layout.i18n)$$endif$
$elseif(i18n.(layout.i18n))$
$i18n.(layout.i18n)$
$else$
$layout.i18n$
$endif$
$elseif(layout.icon)$
<img id='$layout.id$Img' class="icon" src="$layout.icon$?v=$version$" alt="$layout.label;format="xml"$" />
$if(layout.showLabel)$ $layout.label;format="xml"$$endif$
$elseif(layout.label)$
$layout.label;format="xml"$
$if(layout.layouts)$<img src="$i18n.MenuIcon$" />$endif$
$else$
Unknown label
$endif$
$if(layout.layouts)$
$if(IS_IE6)$<table cellspacing="0" cellpadding="0" border="0"><tr><td>$else$</a>$endif$
<div class="autoWidthMenu">
<ul id="sub$layout.id$" $if(layout.displayClass)$class="$layout.displayClass$"$endif$>
$layout.layouts:{layout|$if(layout.used)$$(layout.template)()$$endif$}$
</ul>
</div>
$if(IS_IE6)$</td></tr></table></a>$endif$
$else$
</a>
$endif$
