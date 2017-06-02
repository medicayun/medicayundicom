$layout.study.series:{$if(it.gsps)$
  <span class="gsps">Presentation</span>
  $series/gspsMarkup(markup=it.gsps)$
  <ul class="gsps">
    <li class="gsps">
		<span class="jslink" onclick="controller.selectGsps('$layout.id$','')">Un-apply</span>
    </li>
    $it.gsps:{
	  <li class="gsps">
		<span class="jslink" onclick="controller.selectGsps('$layout.id$','$it.contentLabel$')">$it.contentLabel; format="xml"$</span>
	  </li>
    }$
  </ul>
$endif$}$
