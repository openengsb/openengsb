package org.openengsb.ui.common.imprint;

import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.Panel;

public abstract class ImprintPanel extends Panel{

    private static final long serialVersionUID = 3426187409658223545L;
		
	public ImprintPanel(String id) {
		super(id);
		initContent();
	}

	private void initContent() {
		String openEngSBUrl="http://www.openengsb.org";
		String openEngSBMail="info@openengsb.org";
		
		ExternalLink websiteLink = new ExternalLink("openEngSBWebsiteLink",openEngSBUrl,openEngSBUrl);
		ExternalLink mailLink = new ExternalLink("openEngSBEmailLink","mailto:"+openEngSBMail,openEngSBMail);
		add(websiteLink);
		add(mailLink);
	}

}
