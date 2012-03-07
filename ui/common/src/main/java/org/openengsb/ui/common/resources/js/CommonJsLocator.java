package org.openengsb.ui.common.resources.js;

import org.apache.wicket.ResourceReference;

public final class CommonJsLocator {

	public static ResourceReference getJqueryJs() {
		return new ResourceReference(CommonJsLocator.class,"jquery-1.7.1.min.js");
	}
	
	public static ResourceReference getJqueryUi() {
		return new ResourceReference(CommonJsLocator.class,"jquery-ui-1.8.17.custom.min.js");
	}
	
	public static ResourceReference getJqueryHelper() {
		return new ResourceReference(CommonJsLocator.class,"jqueryHelper.js");
	}
	
}
