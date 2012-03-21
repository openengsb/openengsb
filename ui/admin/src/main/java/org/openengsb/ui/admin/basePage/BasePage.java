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

import org.apache.wicket.PageParameters;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.authentication.AuthenticatedWebSession;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.JavascriptPackageResource;
import org.openengsb.core.api.context.ContextHolder;
import org.openengsb.ui.admin.global.footer.footerTemplate.FooterTemplate;
import org.openengsb.ui.admin.global.header.HeaderTemplate;
import org.openengsb.ui.admin.global.menu.MenuTemplate;
import org.openengsb.ui.admin.loginPage.LoginPage;
import org.openengsb.ui.common.FavIconPackageResource;
import org.openengsb.ui.common.OpenEngSBPage;
import org.openengsb.ui.common.OpenEngSBWebSession;
import org.openengsb.ui.common.resources.css.CommonCssLocator;
import org.openengsb.ui.common.resources.images.CommonPictureLocator;
import org.openengsb.ui.common.resources.js.CommonJsLocator;

public abstract class BasePage extends OpenEngSBPage {

	public BasePage() {
		if (!((AuthenticatedWebSession) getSession()).isSignedIn()) {
            throw new RestartResponseAtInterceptPageException(LoginPage.class);
		}
		initHeader();
		initCommonContent();
	}

	private void initHeader() {
		add(CSSPackageResource.getHeaderContribution(CommonCssLocator
				.getGridsCss()));
		add(CSSPackageResource.getHeaderContribution(CommonCssLocator
				.getCommonCss()));
		add(CSSPackageResource.getHeaderContribution(CommonCssLocator
				.getJqueryUiCss()));
		add(JavascriptPackageResource.getHeaderContribution(CommonJsLocator
				.getJqueryJs()));
		add(JavascriptPackageResource.getHeaderContribution(CommonJsLocator
				.getJqueryUi()));
		add(JavascriptPackageResource.getHeaderContribution(CommonJsLocator
				.getJqueryHelper()));
		add(FavIconPackageResource.getHeaderContribution(CommonPictureLocator
				.getFavIcon()));
	}

	private void initCommonContent() {
		initializeHeader();
		initializeMenu();
		initializeFooter();
	}

	public BasePage(PageParameters parameters) {
		super(parameters);
		initCommonContent();
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
