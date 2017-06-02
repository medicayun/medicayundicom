<div id="$layout.id$">
<li id="prev$layout.id$" class="label toolItem"><img id="imgPrev$layout.id$" src="$if(layout.isFirst)$$i18n.PrevDisabledIcon$$else$$i18n.PrevIcon$$endif$?v=$version$" onclick="controller.navSer('$layout.id$',event,-1)"/></li>
<li id="next$layout.id$" class="label toolItem"><img id="imgNext$layout.id$" src="$if(layout.isLast)$$i18n.NextDisabledIcon$$else$$i18n.NextIcon$$endif$?v=$version$" onclick="controller.navSer('$layout.id$',event,1)"/></li>
<li id="x$layout.id$" class="label toolItem">$layout.x$</li>
<li id="n$layout.id$" class="label toolItem">$i18n.Of$ $layout.n$</li>
</div>