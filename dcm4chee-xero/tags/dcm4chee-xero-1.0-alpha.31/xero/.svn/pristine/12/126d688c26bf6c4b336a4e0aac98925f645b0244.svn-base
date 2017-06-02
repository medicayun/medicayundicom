$! Contains image navigation operations !$
<div id="imageNav$layout.id$" class="imageNav menu">
$if(layout.used)$
<ul>
<li><img id="prev$layout.id$" src="$if(layout.isFirst)$$i18n.PrevDisabledIcon$$else$$i18n.PrevIcon$$endif$?v=$version$" onclick="controller.navImg('$layout.id$',event,-1)"/></li>
<li><img id="beforeLine$layout.id$" src="icon/slider.gif"  style="width:$layout.beforeLine$em; height:1em; vertical-align: middle;"/></li>
<li>$layout.x$</li>
<li><img id="afterLine$layout.id$" src="icon/slider.gif" style="width:$layout.afterLine$em; height:1em;vertical-align: middle;" /></li>
<li>
<img id="next$layout.id$" src="$if(layout.isLast)$$i18n.NextDisabledIcon$$else$$i18n.NextIcon$$endif$?v=$version$" onclick="controller.navImg('$layout.id$',event,1)"/>
</li>
<li>$i18n.Of$ $layout.n$</li></ul>
$endif$
</div>
