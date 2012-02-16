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

import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.authentication.AuthenticatedWebSession;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.openengsb.ui.admin.global.footer.imprintPage.ImprintPage;
import org.openengsb.ui.admin.global.header.HeaderTemplate;
import org.openengsb.ui.admin.model.UsernamePassword;
import org.openengsb.ui.common.OpenEngSBPage;
import org.openengsb.ui.common.imprint.ImprintPanel;
import org.ops4j.pax.wicket.api.PaxWicketMountPoint;

@PaxWicketMountPoint(mountPoint = "login")
public class LoginPage extends OpenEngSBPage{
    private UsernamePassword user = new UsernamePassword();

    public final class ip extends ImprintPanel {

		private static final long serialVersionUID = 1L;

		public ip(String id) {
			super(id);
		}
    	
    }
    
    public LoginPage() {
        initContent();
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
        add(new Image("topImage", new ResourceReference(HeaderTemplate.class, "openengsb_medium_greyscale.png")));
        
        ip imprintDialogue = new ip("imprintDialogue");
        imprintDialogue.setOutputMarkupId(true);
        add(imprintDialogue);
        
        ExternalLink addUserLink = new ExternalLink("imprint","#");
        addUserLink.add(new SimpleAttributeModifier("onClick","showModalButtonCloseDialogue('"+imprintDialogue.getMarkupId()+"','Imprint',false,false,550,450)"));
        add(addUserLink);
    }

    public LoginPage(PageParameters parameters) {
        super(parameters);
    }

}
