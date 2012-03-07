package org.openengsb.ui.common.resources.css;

import org.apache.wicket.ResourceReference;

public final class CommonCssLocator {

	public static ResourceReference getGridsCss() {
		return new ResourceReference(CommonCssLocator.class,"grids.css");
	}
	
	public static ResourceReference getCommonCss() {
		return new ResourceReference(CommonCssLocator.class,"jquery.css");
	}
	
	public static ResourceReference getJqueryUiCss() {
		return new ResourceReference(CommonCssLocator.class,"style.css");
	}
}
