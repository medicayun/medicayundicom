var isServer = false;
$sharedStatic()$
/** Utilities contains methods that are used client side only */
$utilities()$
$model()$
$xeroView()$
$controller(IS_SERVER=false)$

/** Configure the model with the default i18n values */
$modelI18n()$

// Define short versions of the MVC components.
var ua = navigator.userAgent;
var model = new XeroModel(_SARISSA_IS_IE);
var controller = new XeroController(model, ua, xeroView);
