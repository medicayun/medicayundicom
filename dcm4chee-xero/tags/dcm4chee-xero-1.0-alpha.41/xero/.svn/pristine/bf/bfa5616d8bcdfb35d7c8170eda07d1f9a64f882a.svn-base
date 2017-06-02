<div class="tabHeader">$layout.layouts:{
  <div id="$it.tabName$TabHead" class="$if(it.used)$activeTab$else$tab$endif$" onclick="controller.switchTab(this,'$it.tabName$')">$i18n.(it.tabName)$</div>
}$
$if(layout.activeTab.tabMenu)$ $(layout.activeTab.tabMenu.template)(layout=layout.activeTab.tabMenu)$ $endif$
$if(layout.tabMenu.template)$ $(layout.tabMenu.template)(layout=layout.tabMenu)$ $endif$
</div>
<div class="tabBody" style="$layout.style$">
$(layout.activeTab.template)(layout=layout.activeTab)$
</div>