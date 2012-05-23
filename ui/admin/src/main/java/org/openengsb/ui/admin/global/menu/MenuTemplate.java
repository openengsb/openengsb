/**
 * Licensed to the Austrian Association for Software Tool Integration (AASTI)
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. The AASTI licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.ui.admin.global.menu;

import java.io.Serializable;
import java.util.ArrayList;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.resource.loader.ComponentStringResourceLoader;
import org.openengsb.core.api.security.model.SecurityAttributeEntry;
import org.openengsb.core.common.SecurityAttributeProviderImpl;
import org.openengsb.ui.admin.global.BookmarkablePageLabelLink;
import org.openengsb.ui.admin.index.Index;
import org.openengsb.ui.admin.sendEventPage.SendEventPage;
import org.openengsb.ui.admin.serviceListPage.ServiceListPage;
import org.openengsb.ui.admin.taskOverview.TaskOverview;
import org.openengsb.ui.admin.testClient.TestClient;
import org.openengsb.ui.admin.userService.UserListPage;
import org.openengsb.ui.admin.wiringPage.WiringPage;
import org.ops4j.pax.wicket.api.PaxWicketBean;

@SuppressWarnings("serial")
public class MenuTemplate extends Panel {

    private final ArrayList<MenuItem> menuItems = new ArrayList<MenuItem>();
    private final ArrayList<String> avialableItems = new ArrayList<String>();

    private static String menuIndex;

    @PaxWicketBean(name = "attributeStore")
    private SecurityAttributeProviderImpl attributeStore;

    public MenuTemplate(String id, String menuIndex) {
        super(id);

        MenuTemplate.menuIndex = menuIndex;
        initMainMenuItems();
        initMainMenu();
    }

    private void initMainMenuItems() {
        addMenuItem("Index", Index.class, Index.PAGE_NAME_KEY, Index.PAGE_DESCRIPTION_KEY);
        addMenuItem("UserService", UserListPage.class, UserListPage.PAGE_NAME_KEY, UserListPage.PAGE_DESCRIPTION_KEY,
            "ROLE_ADMIN");
        addMenuItem("TestClient", TestClient.class, TestClient.PAGE_NAME_KEY, TestClient.PAGE_DESCRIPTION_KEY);
        addMenuItem("SendEventPage", SendEventPage.class, SendEventPage.PAGE_NAME_KEY,
            SendEventPage.PAGE_DESCRIPTION_KEY);
        addMenuItem("ServiceListPage", ServiceListPage.class, ServiceListPage.PAGE_NAME_KEY,
            ServiceListPage.PAGE_DESCRIPTION_KEY);
        addMenuItem("TaskOverview", TaskOverview.class, TaskOverview.PAGE_NAME_KEY, TaskOverview.PAGE_DESCRIPTION_KEY);
        addMenuItem("WiringPage", WiringPage.class, WiringPage.PAGE_NAME_KEY, WiringPage.PAGE_DESCRIPTION_KEY,
            "ROLE_ADMIN");
    }

    private void initMainMenu() {

        if (MenuTemplate.getActiveIndex() == null || !avialableItems.contains(MenuTemplate.getActiveIndex())) {
            // update menu item to index, because page index is not found!
            MenuTemplate.menuIndex = "Index";
        }

        // generate main navigation
        ListView<MenuItem> menuItemsList = new ListView<MenuItem>("menuItems", menuItems) {
            @Override
            protected void populateItem(ListItem<MenuItem> item) {
                MenuItem menuItem = item.getModelObject();
                item.add(menuItem.getLink());

                Label itemDescription = new Label("itemDescripton", menuItem.getItemDescription());
                item.add(itemDescription);

                if (menuItem.getIcon() != null) {
                    String backgroundAttribute = "background:url('resources/"
                            + menuItem.getIcon().getExtension() + "') no-repeat scroll left center transparent;";
                    item.add(AttributeModifier.replace("style", backgroundAttribute));
                }

                if (item.getIndex() == menuItems.size() - 1) {
                    item.add(AttributeModifier.replace("class", "lastElement"));
                }

                // set menu item to active
                if (menuItem.getItemName().equals(MenuTemplate.getActiveIndex())) {
                    item.add(AttributeModifier.replace("class", "activeElement"));
                }
            }
        };
        add(menuItemsList);
    }

    /**
     * get the name of the current active menu item
     */
    public static String getActiveIndex() {
        return MenuTemplate.menuIndex;
    }

    /**
     * Adds a new item to main header navigation where the index defines the name of the index, which should be the
     * class name; linkClass defines the class name to be linked to; langKey defines the language key for the text which
     * should be displayed and authority defines who is authorized to see the link
     */
    public void addMenuItem(String index, Class<? extends WebPage> linkClass, String langKey, String langDescKey,
            String... authority) {
        addMenuItem(index, linkClass, langKey, langDescKey, null, authority);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void addMenuItem(String index, Class<? extends WebPage> linkClass, String langKey, String langDescKey,
            ResourceReference icon, String... authority) {

        ComponentStringResourceLoader csrl = new ComponentStringResourceLoader();
        String label =
            csrl.loadStringResource(linkClass, langKey, getSession().getLocale(), getSession().getStyle(), "");
        String description =
            csrl.loadStringResource(linkClass, langDescKey, getSession().getLocale(), getSession().getStyle(), "");
        BookmarkablePageLabelLink pageLabelLink = new BookmarkablePageLabelLink("link", linkClass, label);
        addAuthorizationRoles(pageLabelLink, authority);

        if (icon == null) {
            menuItems.add(new MenuItem(index, pageLabelLink, description));
        } else {
            menuItems.add(new MenuItem(index, pageLabelLink, description, icon));
        }
        avialableItems.add(index);
    }

    private void addAuthorizationRoles(BookmarkablePageLabelLink<?> pageLabelLink, String... authority) {
        if (authority == null) {
            return;
        }
        for (String a : authority) {
            attributeStore.putAttribute(pageLabelLink, new SecurityAttributeEntry(a, "RENDER"));
        }
    }

    private static class MenuItem implements Serializable {
        private final String index;
        private final String itemDescription;
        private final BookmarkablePageLabelLink<? extends WebPage> link;
        private final ResourceReference icon;

        public MenuItem(String index, BookmarkablePageLabelLink<? extends WebPage> link, String itemDescription) {
            this.index = index;
            this.link = link;
            icon = null;
            this.itemDescription = itemDescription;
        }

        public MenuItem(String index, BookmarkablePageLabelLink<? extends WebPage> link, String itemDescription,
                ResourceReference icon) {
            this.index = index;
            this.link = link;
            this.icon = icon;
            this.itemDescription = itemDescription;
        }

        public String getItemName() {
            return index;
        }

        public BookmarkablePageLabelLink<? extends WebPage> getLink() {
            return link;
        }

        public ResourceReference getIcon() {
            return icon;
        }

        public String getItemDescription() {
            return itemDescription;
        }
    }

}
