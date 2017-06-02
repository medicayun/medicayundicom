/** The menu controller handles displaying/managing of menus.  The behaviour is:
 * 1. Mouse-down on a menu to display it
 * 2. Menu-item uses mouse-up to activate menu item, hiding overall menu.
 * 3. Mouse-up outside menu area causes menu to be hidden
 * Some specific areas may still use hover menus (eg display area controls)
 */

/** Causes the action to be executed */
XeroController.prototype.itemAction = function(id,event,arg) {
	var lay = this.findLayout(id);
	this.hideMenu();
	return this[lay.action](lay,event,arg!=null ? arg : lay.arg);
};

/** Cause the menu to be displayed. */
XeroController.prototype.mouseMenu = function(id,event) {
	var lay = this.findLayout(id);
	var clickedMenu = document.getElementById("sub"+lay.id);
	if( this.hideMenu()==clickedMenu ) return;
    this.model.clickedMenu = clickedMenu;
    if(! this.model.clickedMenu) {
    	console.warn("Clicked on menu",id,"but no sub-menu: sub"+lay.id);
    	return;
    };
   	this.model.clickedMenu.style.display = "block";
};

/** Hide any visible menus - returns true if a menu was hidden (ie don't perform any action) */
XeroController.prototype.hideMenu = function() {
	if( ! this.model.clickedMenu ) return false;
	var ret = this.model.clickedMenu;
	ret.style.display = "";
	this.model.clickedMenu = null;
	return ret;
};
