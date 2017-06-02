$if( row.isNearline )$<img src="$i18n.NearlineIcon$" title="$i18n.NearlineTip$"/>
$elseif( row.isOffline )$<img src="$i18n.OfflineIcon$" title="$i18n.OfflineTip$"/>
$endif$ 
$if(rowTop.manyStudy)$<input  id="check-$row.studyUID$" tabindex="-1" type="checkbox" $if(row.isSelected)$checked="checked"$endif$ onclick="controller.manyStudy('$layout.id$','$rowTop.PatientIdentifier$','$row.studyUID$')"/>$endif$<a id="$row.studyUID$" class="jslink" onclick="controller.displayStudy('$layout.id$','$rowTop.PatientIdentifier;format="xml"$','$row.studyUID$')">$row.StudyDescription;format="xml"$</a>