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

package org.openengsb.ui.admin.loginPage;

import java.util.Locale;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.openengsb.ui.admin.model.UsernamePassword;
import org.openengsb.ui.common.OpenEngSBPage;
import org.openengsb.ui.common.imprint.ImprintPanel;
import org.openengsb.ui.common.resources.css.CommonCssLocator;
import org.openengsb.ui.common.resources.images.CommonPictureLocator;
import org.openengsb.ui.common.resources.js.CommonJsLocator;
import org.ops4j.pax.wicket.api.PaxWicketMountPoint;

@PaxWicketMountPoint(mountPoint = "login")
public class LoginPage extends OpenEngSBPage {

    private static final long serialVersionUID = 4704550987311760491L;

    private UsernamePassword user = new UsernamePassword();

    public final class DefaultImprintPanel extends ImprintPanel {

        private static final long serialVersionUID = 1L;

        public DefaultImprintPanel(String id) {
            super(id);
        }
    }

    public LoginPage() {
        initContent();
    }

    public LoginPage(PageParameters parameters) {
        super(parameters);
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        response.renderCSSReference(CommonCssLocator.getGridsCss());
        response.renderCSSReference(CommonCssLocator.getLoginPageCss());
        response.renderCSSReference(CommonCssLocator.getJqueryUiCss());
        response.renderJavaScriptReference(CommonJsLocator.getJqueryJs());
        response.renderJavaScriptReference(CommonJsLocator.getJqueryUi());
        response.renderJavaScriptReference(CommonJsLocator.getJqueryHelper());

        // Javascript code to set the focus on the unsername input field. Only necessary for loginpage,
        // therefore injected directly
        response.renderJavaScript(""
                + "$(function() {"
                + "  $(\"#username\").focus();"
                + "});"
                + "", "setFocusOnload");
    }

    private void initContent() {
        @SuppressWarnings("serial")
        Form<UsernamePassword> loginForm = new Form<UsernamePassword>("loginForm") {
            @Override
            protected void onSubmit() {
                AuthenticatedWebSession session = AuthenticatedWebSession.get();
                if (session.signIn(user.getUsername(), user.getPassword())) {
                    setDefaultResponsePageIfNecessary();
                } else {
                    error(new StringResourceModel("error", this, null).getString());
                }
            }

            private void setDefaultResponsePageIfNecessary() {
                if (!continueToOriginalDestination()) {
                    setResponsePage(getApplication().getHomePage());
                }
            }
        };
        loginForm.setModel(new CompoundPropertyModel<UsernamePassword>(user));
        add(loginForm);
        loginForm.add(new RequiredTextField<String>("username"));
        loginForm.add(new PasswordTextField("password"));

        FeedbackPanel feedbackPanel = new FeedbackPanel("feedback");
        feedbackPanel.setOutputMarkupId(true);
        add(feedbackPanel);

        add(new Image("topImage", CommonPictureLocator.getGreyscaleLogoBig()));

        DefaultImprintPanel imprintDialogue = new DefaultImprintPanel("imprintDialogue");
        imprintDialogue.setOutputMarkupId(true);
        add(imprintDialogue);

        ExternalLink addUserLink = new ExternalLink("imprint", "#");
        addUserLink.add(AttributeModifier.replace("onClick",
            "showModalButtonCloseDialogue('" + imprintDialogue.getMarkupId() + "'"
                    + ",'" + getLocalizer().getString("imprint", this) + "',false,false,550,450)"));
        loginForm.add(addUserLink);

        add(new Link<Object>("lang.en") {

            private static final long serialVersionUID = -2740581767694866689L;

            @Override
            public void onClick() {
                getSession().setLocale(Locale.ENGLISH);
                setResponsePage(this.getPage());
            }
        });

        add(new Link<Object>("lang.de") {

            private static final long serialVersionUID = -6858440905643185661L;

            @Override
            public void onClick() {
                getSession().setLocale(Locale.GERMAN);
                setResponsePage(this.getPage());
            }
        });
    }
}
