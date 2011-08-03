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

package org.openengsb.ui.common.editor.fields;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.link.PopupSettings;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.util.time.Duration;
import org.apache.wicket.validation.IValidator;
import org.openengsb.core.api.OAuthData;
import org.openengsb.core.api.descriptor.AttributeDefinition;
import org.openengsb.ui.common.OAuthPage;
import org.openengsb.ui.common.editor.ModelFascade;
import org.openengsb.ui.common.model.OAuthPageFactory;
import org.openengsb.ui.common.model.OAuthPageModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is quite a special field which provides an easy and direct way for components to register oauth components such
 * as facebook or twitter.
 */
@SuppressWarnings("serial")
public class OAuthField extends AbstractField<String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(OAuthField.class);

    public String getResult()
    {
        return result;
    }

    public void setResult(String result)
    {
        this.result = result;
    }

    private String result;
    BookmarkablePageLink<OAuthData> oldPageLink;
    Link<OAuthData> pageLink;

    public OAuthField(String id, IModel<String> model, AttributeDefinition attribute,
            IValidator<String> fieldValidationValidator) {
        super(id, model, attribute, fieldValidationValidator);
    }

    @Override
    protected ModelFascade<String> createFormComponent(AttributeDefinition attribute, final IModel<String> model) {
        ModelFascade<String> container = new ModelFascade<String>();
        PopupSettings popupSettings =
            new PopupSettings("popuppagemap").setHeight(300).setWidth(600).setLeft(50).setTop(50);
        pageLink =
            new Link<OAuthData>("popupLink1", new OAuthPageModel(
                new Model<OAuthData>(attribute.getOAuthConfiguration()))) {
                @Override
                public void onClick() {
                    try {
                        OAuthData tmp = getModelObject();
                        pageLink.setResponsePage(new OAuthPage(tmp));
                    } catch (Exception e) {
                        LOGGER.error("Cant forward to OAuthPage", e);
                        error(new StringResourceModel("oauth.forward.error", model));
                    }
                }
            };
        pageLink.setPopupSettings(popupSettings);
        final TextField<String> tokenResult = new TextField<String>("field", model);
        tokenResult.setRequired(true);
        tokenResult.setOutputMarkupId(true);
        tokenResult.add(new AbstractAjaxTimerBehavior(Duration.milliseconds(1000)) {

            @Override
            protected void onTimer(AjaxRequestTarget target) {
                OAuthData tmp = OAuthPageFactory.getOAuthObject(getSession().getId());
                if (tmp != null) {
                    if (tmp.getOutParameter() != null && !tmp.getOutParameter().equals("")) {
                        tokenResult.setModelValue(new String[]{ tmp.getOutParameter() });
                        OAuthPageFactory.removeOAuthObject(getSession().getId());
                        target.addComponent(tokenResult);
                        stop();
                    }
                }

            }

        });
        List<Component> list = new ArrayList<Component>();
        list.add(pageLink);
        container.setHelpComponents(list);
        container.setMainComponent(tokenResult);
        return container;
    }
}
