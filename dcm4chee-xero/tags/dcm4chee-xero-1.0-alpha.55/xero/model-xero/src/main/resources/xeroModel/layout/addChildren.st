/** Children added from static metadata */
var child;
$children:{child|
	$if(child.template)$
	child = new Layout("$child.name$"+suffix,"$child.template$");
	$elseif(child.layout)$
	child = new $child.layout$(suffix$if(child.i18n)$,"$child.i18n;format="xml"$"$endif$);
	$elseif(child.i18n)$
	child = new LabelLayout("$child.i18n$"+suffix,"$child.i18n$");
	$elseif(child.label)$
	child = new LabelLayout("$child.label;format="js"$"+suffix,"$child.label;format="js"$","");
	$elseif(child.codeTemplate)$
	child = $(child.codeTemplate)()$;
	$else$
	/* Unknown child type $child$ */
	child = null;
	$endif$
	if( child ) {
		$if(child.label)$child.label="$child.label$";$endif$
		$if(child.icon)$child.icon="$child.icon$";$endif$
		$if(child.mode)$child.mode="$child.mode$";$endif$
		$if(child.action)$child.action="$child.action$";$endif$
		$if(child.href)$child.href="$child.href;format="js"$";$endif$
		$if(child.showLabel)$child.showLabel=true;$endif$
		$if(child.isActive)$child.isActive=$child.isActive$;$endif$
		$if(child.target)$child.target="$child.target;format="js"$";$endif$
		$if(child.arg)$child.arg="$child.arg;format="js"$";$endif$
		$if(child.tooltip)$child.tooltip="$child.tooltip;format="js"$";$endif$
		this.add(child $if(child.name)$,"$child.name$"$endif$);
	};
}$