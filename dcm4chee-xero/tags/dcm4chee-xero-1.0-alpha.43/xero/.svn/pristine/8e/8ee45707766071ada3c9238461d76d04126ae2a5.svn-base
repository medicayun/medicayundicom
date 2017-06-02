<div class="menu" style="position: absolute; top: 27px; right: 5px;">
<ul class="rightmenu">

$if(layout.parent.image.keyObjects)$

    <li><a href="#" onclick="return cancelEvent();"><img class="icon" style="background:none;" src="$i18n.KeyObjectsIcon$?v=$version$" />
    $if(IS_IE)$<table cellspacing="0" cellpadding="0" border="0"><tr><td>$else$</a>$endif$
	<ul>
	$layout.parent.image.keyObjects:{
		<li><a class="koTitle">$it.contentLabel$</a></li>
		$it.keyText:{<li class="koText">$it;format="xml"$</li>}$
	}$
	</ul>
	$if(IS_IE)$</td></tr></table></a>$endif$
	</li>
$endif$

	<li><span id="wlIndicator$layout.id$" style="display:none;"> </span></li>

</ul></div>
