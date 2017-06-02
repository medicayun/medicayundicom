<!-- Defines the series tray items - currently just creates an unordered list of
  class seriesTray, with list items each containing a link to a given series. -->
<li><a class="thumbnail toolItem">Series</a>
<ul class="thumbnail">
$layout.study.series:{$if(it.image)$
	<li class="thumbnail">
		<span>$it.SeriesDescription; format="xml"$ | $it.Viewable$</span>
		<img onclick="controller.navToSer('$layout.id$', '$it.seriesUID$')" height="256" src="/wado2/wado?objectUID=$it.image.("0").objectUID$&amp;rows=256&amp;v=$version$" />
	</li>
$endif$
}$
</ul>
</li>