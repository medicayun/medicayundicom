/** Defines the Find page layout information */
function FindLayout(suffix) {
	this.baseConstructor("Find");
	var queryBox = new Layout("QueryLayout","query/query");
	queryBox.query = new UrlFactory(STUDY_QUERY+"&maxResults=500",queryBox.id);
	 
	var searchLayout = new Layout("SearchLayout","query/studyTable");
	searchLayout.columns = XeroStudyColumns;
	searchLayout.action = "sortQuery";
	searchLayout.query = queryBox.query;
	searchLayout.sortPatient = new SortPatient(searchLayout);
	searchLayout.viewRenderedListeners = ["fixScrollPosition"];
	this.add(queryBox);
	this.add(searchLayout);
	
	var findMenu = new FindMenuLayout("");
	this.add(findMenu,TabsLayout.TabMenu);
};

FindLayout.prototype = new Layout();

/** The menu for the find display tab */
function FindMenuLayout(suffix) {
	this.baseConstructor("FindMenu");
$layout/addChildren(children=findMenuLayout.children)$
};

FindMenuLayout.prototype = new Layout("FindMenuPrototype", "html/menu");
FindMenuLayout.prototype.ulClass="click";

function ReportSelect(suffix) {
	this.baseConstructor("ReportSelect"+suffix);
	this.add(new ReportSelectItem("ShowReportAndImages"));
	this.add(new ReportSelectItem("ShowReport"));
	this.add(new ReportSelectItem("ShowImages"));
	var active = this.layouts[0];
	this.copyFrom(active);
	active.isActive = true;
};

ReportSelect.prototype = new Layout("ReportSelect","html/toolItem");
ReportSelect.prototype.area="Menu";

function ReportSelectItem(i18n) {
	this.baseConstructor(i18n);
	this.i18n = i18n;
	this.icon = i18n+"Icon";
};
ReportSelectItem.prototype = new Layout("ReportSelectItem", "html/toolItem");
ReportSelectItem.prototype.action = "rotateButton";
ReportSelectItem.prototype.showLabel = true;