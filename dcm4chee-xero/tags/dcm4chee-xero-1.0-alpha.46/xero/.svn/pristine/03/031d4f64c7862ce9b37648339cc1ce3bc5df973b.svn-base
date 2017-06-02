<div style="float: left;"><table>

<tr><td>$i18n.PatientName;format="xml"$</td><td><input id="patientNameInput" type="text" name="PatientName" value="$query.PatientName$" onkeypress="controller.keyPress('find',event)"/></td></tr>
<tr><td>$i18n.PatientID;format="xml"$</td><td><input id="patientIdInput" type="text" name="PatientID" value="$query.PatientID$" onkeypress="controller.keyPress('find',event)"/></td></tr>

$if(i18n.queryAE)$
<td>$i18n.AE;format="xml"$</td><td>
  <select id="aeSelect" name="ae" onkeypress="controller.keyPress('find',event)">
	$if(query.ae)$<option selected="true" value="$query.ae;format="xml"$">$query.ae;format="xml"$</option>$endif$
	<option value="local"> </option>
  	<option value="PACS_AGFA">IDC Pacs Agfa</option>
  	<option value="PACS_TELEMIS">Pacs Telemis</option>
  	<option value="PACS_CERNER">Pacs Cerner</option>
  	<option value="DCM4CHEE">Central Archive</option>
  	<option value="PACS_J4C_DATACEN">J4C DataCenter</option>
  </select>
</td>
$else$
<input id="aeInput" type="hidden" name="ae" value="$query.ae$" onkeypress="controller.keyPress('find',event)"/>
$endif$

</table></div>

<div style="float: left;"><table>

<tr><td>$i18n.AccessionNumber;format="xml"$</td><td><input id="accessionNumberInput" type="text" name="AccessionNumber" value="$query.AccessionNumber$" onkeypress="controller.keyPress('find',event)"/></td></tr>

<tr><td>$i18n.StudyDateTimeF;format="xml"$</td><td>
  <select id="studyDateTimeSelect" name="StudyDateTime" onkeypress="controller.keyPress('find',event)">
	$if(query.StudyDateTime)$<option selected="true" value="$query.StudyDateTime;format="xml"$">$if(i18n.(query.StudyDateTime))$ $i18n.(query.StudyDateTime)$ $else$ $query.StudyDateTime;format="xml"$ $endif$</option>$endif$
	<option value=""> </option>
  	<option value="Today">$i18n.Today$</option>
  	<option value="SinceYesterday">$i18n.SinceYesterday$</option>
  	<option value="Yesterday">$i18n.Yesterday$</option>
  	<option value="Last7days">$i18n.Last7days$</option>
  	<option value="Last4weeks">$i18n.Last4weeks$</option>
  	<option value="ThisMonth">$i18n.ThisMonth$</option>
  	<option value="LastMonth">$i18n.LastMonth$</option>
  	<option value="ThisYear">$i18n.ThisYear$</option>
  	<option value="LastYear">$i18n.LastYear$</option>
  	<option value="SinceLastYear">$i18n.SinceLastYear$</option>
  	<option value="Last10Years">$i18n.Last10Years$</option>
  </select>
</td></tr>

<tr><td>$i18n.Modality;format="xml"$</td><td>
  <select id="modalitiesInStudySelect" name="ModalitiesInStudy" onkeypress="controller.keyPress('find',event)">
	$if(query.ModalitiesInStudy)$<option selected="true" value="$query.ModalitiesInStudy;format="xml"$">$if(i18n.(query.ModalitiesInStudy))$$i18n.(query.ModalitiesInStudy)$$else$$query.ModalitiesInStudy$$endif$</option>$endif$
	<option value=""> </option>
	<option value="CR">CR</option>
	<option value="DX">DX</option>
	<option value="CT">CT</option>
	<option value="MG">MG</option>
	<option value="MR">MR</option>
	<option value="US">US</option>
	<option value="NM">NM</option>
	<option value="XC">XC</option>
	<option value="XA">XA</option>
	<option value="PX">PX</option>
	
	<option value="IO">$i18n.IO$</option>
	<option value="KO">$i18n.KO$</option>
	<option value="SR">$i18n.SR$</option>
	<option value="OT">$i18n.OT$</option>
	<option value="PR">$i18n.PR$</option>
	<option value="AU">$i18n.AU$</option>
	<option value="ECG">$i18n.ECG$</option>
  </select></td>
</tr>

</table>
</div>

$if( i18n.queryAdvanced )$
<div style="float: left;"><table>
</table>
</div>
$endif$

