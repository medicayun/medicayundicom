<div class="iReport" $html/style()$>

$if(layout.closeReportAreaUsed)$<div class="closeArea" style="right: 22px;">
<img src="$i18n.CloseIcon$" onclick="controller.closeArea('$layout.id$', event, 'Report');" />
</div>$endif$

<iframe class="iReport" src="$WADO_QUERY;format="xml"$&amp;template=$i18n.ReportTemplate$&amp;$layout.src$">
Contains the report iframe information.
</iframe>
</div>