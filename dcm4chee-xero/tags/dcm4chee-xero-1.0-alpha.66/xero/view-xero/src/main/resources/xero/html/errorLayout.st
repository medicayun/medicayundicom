$if(layout.errorMessage)$
<span id="$layout.id$" class="errorMessage">$layout.errorMessage$</span>
$else$
$html/layout()$
$endif$