/**

   Copyright 2010 OpenEngSB Division, Vienna University of Technology

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package org.openengsb.ui.web.global.header;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.openengsb.ui.web.Index;
import org.openengsb.ui.web.TestClient;
import org.openengsb.ui.web.global.BookmarkablePageLabelLink;

import java.io.Serializable;
import java.util.ArrayList;

public class HeaderTemplate extends Panel {

	private static final long serialVersionUID = 1262492232985739307L;

	private final ArrayList<HeaderMenuItem> menuItems = new ArrayList<HeaderMenuItem>();
	private final ArrayList<String> avialableItems = new ArrayList<String>();

	private static String menuIndex;

	public HeaderTemplate(String id, String menuIndex) {
		super(id);

		HeaderTemplate.menuIndex = menuIndex;

		this.addHeaderMenuItem("index", Index.class, "index.title");
		this.addHeaderMenuItem("testClient", TestClient.class, "testclient");
        this.add(new BookmarkablePageLink<Index>("indexLogo1", Index.class));
		this.add(new BookmarkablePageLink<Index>("indexLogo2", Index.class));
//        this.addHeaderMenuItem("sendEvent", SendEventPage.class, "sendevent.title");


		if (HeaderTemplate.getActiveIndex() == null || !this.avialableItems.contains(HeaderTemplate.getActiveIndex())) {
			// update menu item to index, because page index is not found!
			HeaderTemplate.menuIndex = "index";
		}

		// generate main navigation
		ListView headerMenuItems = new ListView("headerMenuItems", this.menuItems) {
			private static final long serialVersionUID = -2458903054129857522L;

			protected void populateItem(ListItem item) {
				HeaderMenuItem menuItem = (HeaderMenuItem) item.getModelObject();
				item.add(menuItem.getLink());

				// set menu item to active
				if (menuItem.getItemName().equals(HeaderTemplate.getActiveIndex())) {
					item.add(new AttributeModifier("class", true, new AbstractReadOnlyModel() {
						private static final long serialVersionUID = 1L;

						@Override
						public String getObject() {
							return "active";
						}
					}));
				}
			}
		};

		this.add(headerMenuItems);
	}

	/**
	 * @returns the name of the current active menu item
	 */
	public static String getActiveIndex() {
		return HeaderTemplate.menuIndex;
	}

	/**
	 * adds new item to main header navigation
	 *
	 * @param index
	 * @param linkClass
     * @param langKey
	 */
	@SuppressWarnings("unchecked")
	public void addHeaderMenuItem(String index, Class linkClass, String langKey) {
		this.menuItems.add(new HeaderMenuItem(index, new BookmarkablePageLabelLink("link", linkClass,  this
				.getLocalizer().getString(langKey, this))));
		this.avialableItems.add(index);
	}

	/**
	 * single header menu item
	 *
	 */
	private class HeaderMenuItem implements Serializable {
		private static final long serialVersionUID = -3893570203135664161L;

		private final String index;
		private final BookmarkablePageLabelLink link;

		public HeaderMenuItem(String index, BookmarkablePageLabelLink link) {
			this.index = index;
			this.link = link;
		}

		public String getItemName() {
			return this.index;
		}

		public BookmarkablePageLabelLink getLink() {
			return this.link;
		}
	}
}