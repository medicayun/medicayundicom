<div class="image" $html/style()$ xmlns="http://www.w3.org/1999/xhtml">
 $if(layout.errorMessage)$
 <span class="errorMessage">$layout.errorMessage;format="xml"$</span>
 $elseif(HAS_VML)$
   <v:group id="vml$layout.id$" class="imageDisplay" onmousedown="return controller.mouseClickDrag('$layout.id$',event);"
    ondblclick="return controller.doubleClick('$layout.id$');"   	 
$if(layout.sameSize)$
	style="overflow: visible; left: $layout.negHalfRealWidth$; margin-left: 50%; margin-top: 50%; top: $layout.negHalfRealHeight$; width: $layout.realWidth$; height: $layout.realHeight$;"
$else$
     style="overflow: visible; left: $layout.translateXPercent$%; top: $layout.translateYPercent$%; width: expression(Math.min(document.getElementById('$layout.id$').clientWidth,document.getElementById('$layout.id$').clientHeight*$layout.coordWidth$/$layout.coordHeight$));height: expression(Math.min(document.getElementById('$layout.id$').clientWidth*$layout.coordHeight$/$layout.coordWidth$,document.getElementById('$layout.id$').clientHeight));"
$endif$
     coordorigin="$layout.coordX$,$layout.coordY$" coordsize="$layout.coordWidth$,$layout.coordHeight$">
$if(layout.flipRotate)$
   <v:group style="top: -1; left: -1; width: 2; height: 2; overflow: visible;$if(layout.rotation)$rotation: $layout.rotation$;$endif$ $if(layout.flip)$flip:x;$endif$" coordorigin="-1,-1" coordsize="2,2">
$endif$
$if(layout.aspectPercent)$
   <v:group style="overflow: visible; width: 100; height: $layout.aspectPercent$;" coordsize="100,100">
$endif$
$layout.imageUrls:{
    <v:shape id="$it.imageId$Shape" style="width: $it.cols$px; height:$it.rows$px; left: $it.left$px;top:$it.top$px" filled="f" stroked="f">
        <v:imagedata id="$it.imageId$" src="$it.imageUrl;format="xml"$"/>
    </v:shape>
}$
		
		$image/imageRelativeVml()$
$if(layout.aspectPercent)$
    <!-- aspect percent end -->
	</v:group>
$endif$
$if(layout.flipRotate)$
    <!-- flip rotate end -->
	</v:group>
$endif$
   <!-- image group end -->
   </v:group>

   <v:group class="imageDisplayRel" style="width: 100%; height: 100%; top: 0px; left: 0px;" coordsize="1000,1000" coordorigin="0,0">
	   $image/displayRelativeVml()$
   </v:group>
$else$
$if(layout.image)$
<svg id="svg$layout.id$" class="imageDisplay" xmlns="http://www.w3.org/2000/svg" onclick="return false;" onmousedown="return controller.mouseClickDrag('$layout.id$',evt);">

<svg id="gim$layout.id$" 
	class="imageGroup"
$if(layout.sameSize)$
	preserveAspectRatio="xMidYMid slice"
	x="$layout.negHalfRealWidth$" y="$layout.negHalfRealHeight$"
	style="width:100%; overflow: visible;">
	<svg x="50%" y="50%" width="$layout.realWidth$" height="$layout.realHeight$"
$else$
	preserveAspectRatio="xMidYMid meet" 
$endif$
	viewBox="$layout.coordX$ $layout.coordY$ $layout.coordWidth$ $layout.coordHeight$"
>
<g id="tr$layout.id$" transform="$if(layout.flip)$scale(-1,1)$endif$ $if(layout.rotation)$rotate($layout.rotation$,0,0)$endif$ $if(layout.aspect)$ scale(1,$layout.aspect$)$endif$">
$layout.imageUrls:{
<image id="$it.imageId$" preserveAspectRatio="none" xlink:href="$it.imageUrl;format="xml"$" width="$it.cols$" height="$it.rows$" transform="translate($it.left$,$it.top$)" onerror="controller.imageError('$layout.id$')"/>
}$


$image/imageRelativeSvg()$
</g>
</svg>
$if(layout.sameSize)$</svg>$endif$
<svg class="imageDisplayRel" style="width: 100%; height: 100%;" preserveAspectRatio="none" viewBox="0,0,1000,1000">
$image/displayRelativeSvg()$
</svg>
</svg>
$else$
<span style="position:relative;top:40px; left: 40px;">Image Loading</span>
$endif$
$endif$

$html/callNamed(name="imageMenu")$

$html/callNamed(name="rightImageMenu")$

$html/callNamed(name="bottomImageMenu")$

</div>
