package org.openengsb.ui.web.footer;

import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;


public class FooterTemplate extends Panel {

	private static final long serialVersionUID = -5103712644096223402L;

	public FooterTemplate(String id) {
		super(id);

        add(new BookmarkablePageLink<ImprintPage>("imprint", ImprintPage.class));
	}

}