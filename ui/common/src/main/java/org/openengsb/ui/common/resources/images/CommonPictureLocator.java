package org.openengsb.ui.common.resources.images;

import org.apache.wicket.ResourceReference;

public final class CommonPictureLocator {

	public static ResourceReference getGreyscaleLogo() {
		return new ResourceReference(CommonPictureLocator.class,"openengsb_medium_greyscale.png");
	}
	
	public static ResourceReference getFavIcon() {
		return new ResourceReference(CommonPictureLocator.class,"openengsb.ico");
	}

	
}
