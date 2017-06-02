<!-- Parameters rows, cols, nest where nest is the child nesting to use -->
$if(layout.emptyCriteria)$$if(layout.isntServer)$<span>$i18n.EnterSearch$</span>$endif$
$elseif(layout.hasError)$<span style="color: red;">$layout.hasError$</span>
$elseif(layout.noResults)$<span>$i18n.NoResults$</span>
$elseif(layout.search.patient)$
<div id="$layout.id$" class="studyTable">
$if(layout.search.tooManyResults)$
<span class="warning">Too many results found.</span>
$endif$
$html/nestedTable(rows=layout.search.patient,cols=layout.columns,nested="study",id=layout.id)$
</div>
$endif$
