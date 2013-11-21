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

package org.openengsb.ui.admin.basePage;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.openengsb.core.api.context.ContextHolder;
import org.openengsb.ui.admin.global.footer.footerTemplate.FooterTemplate;
import org.openengsb.ui.admin.global.header.HeaderTemplate;
import org.openengsb.ui.admin.global.menu.MenuTemplate;
import org.openengsb.ui.common.OpenEngSBPage;
import org.openengsb.ui.common.OpenEngSBWebSession;
import org.openengsb.ui.common.resources.css.CommonCssLocator;
import org.openengsb.ui.common.resources.js.CommonJsLocator;

public abstract class BasePage extends OpenEngSBPage {

    private static final long serialVersionUID = 4189867438159317921L;

    private String pageNameKey;

    public BasePage() {
        initCommonContent();
    }

    public BasePage(String pageNameKey) {
        this.pageNameKey = pageNameKey;
        initCommonContent();
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        response.render(CssHeaderItem.forReference(CommonCssLocator.getGridsCss()));
        response.render(CssHeaderItem.forReference(CommonCssLocator.getCommonCss()));
        response.render(CssHeaderItem.forReference(CommonCssLocator.getJqueryUiCss()));
        response.render(JavaScriptHeaderItem.forReference(CommonJsLocator.getJqueryJs()));
        response.render(JavaScriptHeaderItem.forReference(CommonJsLocator.getJqueryUi()));
        response.render(JavaScriptHeaderItem.forReference(CommonJsLocator.getJqueryHelper()));
    }

    private void initCommonContent() {
        initializeHeader();
        initializeMenu();
        initializeFooter();

        if (pageNameKey != null) {
            Label sectionName = new Label("sectionName", new StringResourceModel(pageNameKey, this, null));
            add(sectionName);
        } else {
            add(new EmptyPanel("sectionName"));
        }
    }

    public BasePage(PageParameters parameters) {
        super(parameters);
        initCommonContent();
    }

    public BasePage(PageParameters parameters, String pageNameKey) {
        super(parameters);
        initCommonContent();
        this.pageNameKey = pageNameKey;
    }

    private void initializeFooter() {
        add(new FooterTemplate("footer"));
    }

    private void initializeHeader() {
        add(new HeaderTemplate("header"));
    }

    private void initializeMenu() {
        add(new MenuTemplate("menu", this.getMenuItem()));
    }

    /**
     * @return the class name, which should be the index in navigation bar
     * 
     */
    public String getMenuItem() {
        return this.getClass().getSimpleName();
    }

    /**
     * @return the class name, which should be the index in navigation bar
     * 
     */
    @Override
    public String getHeaderMenuItem() {
        return this.getClass().getSimpleName();
    }

    public String getSessionContextId() {
        OpenEngSBWebSession session = OpenEngSBWebSession.get();
        if (session == null) {
            return "foo";
        }
        String contextId = ContextHolder.get().getCurrentContextId();
        if (contextId == null) {
            ContextHolder.get().setCurrentContextId("foo");
            return contextId;
        }
        return contextId;
    }
}
