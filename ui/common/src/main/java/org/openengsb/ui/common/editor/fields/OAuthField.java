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

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.link.PopupSettings;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.handler.RedirectRequestHandler;
import org.apache.wicket.validation.IValidator;
import org.openengsb.core.api.descriptor.AttributeDefinition;
import org.openengsb.core.api.oauth.OAuthData;
import org.openengsb.ui.common.editor.ModelFacade;
import org.openengsb.ui.common.model.OAuthPageFactory;
import org.openengsb.ui.common.model.OAuthPageModel;

/**
 * This is quite a special field which provides an easy and direct way for components to register oauth components such
 * as facebook or twitter.
 */
@SuppressWarnings("serial")
public class OAuthField extends AbstractField<String> {

    public OAuthField(String id, IModel<String> model, AttributeDefinition attribute,
            IValidator<String> fieldValidationValidator) {
        super(id, model, attribute, fieldValidationValidator);
    }

    @Override
    protected ModelFacade<String> createFormComponent(AttributeDefinition attribute, final IModel<String> model) {
        PopupSettings popupSettings =
            new PopupSettings("popuppagemap").setHeight(300).setWidth(600).setLeft(50).setTop(50);
        Link<OAuthData> pageLink =
            new Link<OAuthData>("popupLink", new OAuthPageModel(
                new Model<OAuthData>(attribute.getOAuthConfiguration()))) {
                @Override
                public void onClick() {
                    OAuthData oauth = getModelObject();
                    String redirectURL = buildRedirectURL(getRequest());
                    oauth.setRedirectURL(redirectURL);
                    String link = oauth.generateFirstCallLink();
                    OAuthPageFactory.putOAuthObject(getSession().getId(), oauth);
                    getRequestCycle().scheduleRequestHandlerAfterCurrent(new RedirectRequestHandler(link));
                }
            };
        pageLink.setPopupSettings(popupSettings);
        final TextField<String> tokenResult = new TextField<String>("field", model);
        tokenResult.setRequired(true);
        tokenResult.setOutputMarkupId(true);
        List<Component> list = new ArrayList<Component>();
        list.add(pageLink);
        ModelFacade<String> container = new ModelFacade<String>();
        container.setHelpComponents(list);
        container.setMainComponent(tokenResult);
        return container;
    }

    private String buildRedirectURL(Request request) {
        if (request instanceof WebRequest) {
            HttpServletRequest hsr = ((ServletWebRequest) request).getContainerRequest();
            String currentURL = hsr.getRequestURL().toString();
            currentURL += "oauth";
            return currentURL;
        }
        return null;
    }
}
