<v:line from="0,0" to="1000,0" strokecolor="#202020" strokeweight="2"/>
<v:line from="0,0" to="0,1000"  strokecolor="#202020" strokeweight="2"/>
<v:line from="1000,0" to="1000,1000" strokecolor="#202020" strokeweight="2"/>
<v:line from="0,1000" to="1000,1000" strokecolor="#202020" strokeweight="2" />

$layout.image.use:{
$if(it.isDISPLAYREL)$
$it._parent._parent._parent.svg.(it.href).text:{
<v:line type="#$it.id$"  from="$it.xc$,$it.yc$" to="$it.xc2$,$it.yc2$" filled='t' stroked='t' fillcolor='white' strokecolor='white'>
     <v:path textpathok="True"/>
     <v:textpath on="True" string="$it.TEXT;format="xml"$" style="font-size: $it.iefontsize$;"/>
</v:line>
}$
$it._parent._parent._parent.svg.(it.href).path:{
<v:shape type="#$it.id$" style="width:100; height:100;" coordsize="100 100"/>
}$
$endif$
}$
