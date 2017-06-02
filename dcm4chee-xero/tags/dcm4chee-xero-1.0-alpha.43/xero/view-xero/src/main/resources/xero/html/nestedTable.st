$! Parameters rows, cols, nest where nest is the child nesting to use !$
<table id="$id$Table">
  <thead id="$id$Head">
  <tr>$cols.header:{<th id="$it.id$" $if(layout.action)$class="sortableHeader" onclick="controller.itemAction('$layout.id$', event, $html/layoutInfo(field={columns.header[$i0$]})$)"$endif$>$if(it.i18n)$$i18n.(it.i18n)$$else$$it.name$$endif$$if(layout.sortPatient.img.(it.sort))$<img alt="" src="$layout.sortPatient.img.(it.sort)$?v=$version$" class="sort"/>$endif$</th>}$</tr>
  </thead>
  <tbody id="$id$Body">
   $rows: 
     {rowTop | $html/nestedRow(cssClass="odd")$ },
     {rowTop | $html/nestedRow(cssClass="even")$ }
   $
   <tr style="height:95%;" />
   </tbody>
</table>

