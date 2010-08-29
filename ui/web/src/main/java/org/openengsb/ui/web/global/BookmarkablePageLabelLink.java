package org.openengsb.ui.web.global;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;

public class BookmarkablePageLabelLink extends BookmarkablePageLink {

	private static final long serialVersionUID = -7780506697716089759L;

	private String label;


	/**
	 * @param id
	 * @param pageClass
	 * @param parameters
	 */
	public BookmarkablePageLabelLink(String id, Class<?> pageClass, PageParameters parameters, String label) {
		super(id, pageClass, parameters);
		this.label = label;
	}

	/**
	 * @param id
	 * @param pageClass
	 */
	public BookmarkablePageLabelLink(String id, Class<?> pageClass, String label) {
		super(id, pageClass);
		this.label = label;
	}

	/**
	 * (non-Javadoc)
	 * @see org.apache.wicket.markup.html.link.AbstractLink#onComponentTagBody(org.apache.wicket.markup.MarkupStream, org.apache.wicket.markup.ComponentTag)
	 */
	@Override
	protected void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag) {
		replaceComponentTagBody(markupStream, openTag, label);
	}
}