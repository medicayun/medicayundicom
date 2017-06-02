<!-- This is the per-study control containing the series thumbnails and controls within this study. -->
<div class="studyTray" $html/style()$>
$if(layout.study)$
	<span class="studyDescription toolItem">$layout.study.StudyDescription;format="xml"$ </span>
	<span class="studyid toolItem">Study ID: $layout.study.StudyID;format="xml"$ </span>
	<span class="accNum toolItem">Acc#: $layout.study.AccessionNumber;format="xml"$ </span>
	$shared.studyTrayItems:{$(it)()$}$

$else$
No Study
$endif$
</div>