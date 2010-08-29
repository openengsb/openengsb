package org.openengsb.ui.web.footer;

import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.openengsb.ui.web.BasePage;
import org.openengsb.ui.web.Index;

public class ImprintPage extends BasePage {

	private static final long serialVersionUID = -6735963232227976804L;

    public ImprintPage() {

       this.add(new BookmarkablePageLink<Index>("index", Index.class));
    }

     @Override
	public String getHeaderMenuItem() {
		return "index";
	}


}