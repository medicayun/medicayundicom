<div class="menu" $html/style()$>
	<ul>
	$if(layout.patient)$
$layout.layouts:{layout|$if(layout.used)$$(layout.template)()$$endif$}$

	<li>$layout.patient.patientNameF;format="xml"$</li>
	<li>| $layout.patient.PatientID;format="xml"$</li>
	<li> $i18n.("SEX_"+layout.patient.PatientSex);format="xml"$</li>
	<li> $layout.patient.birthDateF;format="xml"$</li>

	$else$
	$i18n.NoPatient;format="xml"$
	$endif$	
	</ul>
</div>