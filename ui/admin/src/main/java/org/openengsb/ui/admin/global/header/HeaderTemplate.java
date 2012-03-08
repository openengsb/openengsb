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

package org.openengsb.ui.admin.global.header;

import java.util.List;
import java.util.Locale;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.authentication.AuthenticatedWebSession;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.protocol.http.WebSession;
import org.openengsb.core.api.context.ContextHolder;
import org.openengsb.core.common.SecurityAttributeProviderImpl;
import org.openengsb.ui.admin.index.Index;
import org.openengsb.ui.admin.loginPage.LoginPage;
import org.openengsb.ui.admin.model.OpenEngSBFallbackVersion;
import org.openengsb.ui.api.OpenEngSBVersionService;
import org.openengsb.ui.common.OpenEngSBWebSession;
import org.openengsb.ui.common.resources.images.CommonPictureLocator;
import org.ops4j.pax.wicket.api.PaxWicketBean;

@SuppressWarnings("serial")
public class HeaderTemplate extends Panel {
    
	
	
    @PaxWicketBean(name = "openengsbVersion")
    private OpenEngSBFallbackVersion openengsbVersion;
    @PaxWicketBean(name = "openengsbVersionService")
    private List<OpenEngSBVersionService> openengsbVersionService;
    
	
    public HeaderTemplate(String id) {
        super(id);
        
        baseInitialization();
        initializeTopMenu();
    }

    private void baseInitialization() {
        
    	BookmarkablePageLink<Index> homelink = new BookmarkablePageLink<Index>("logo", Index.class);
    	homelink.add(new Image("topImage", CommonPictureLocator.getGreyscaleLogo()));
    	add(homelink);

        if (openengsbVersion == null) {
            openengsbVersion = new OpenEngSBFallbackVersion();
        }
        if (openengsbVersionService == null || openengsbVersionService.size() == 0) {
            add(new Label("version", openengsbVersion.getVersionNumber()));
        } else {
            add(new Label("version", openengsbVersionService.get(0).getOpenEngSBVersion()));
        }
    }

    
    private void initializeTopMenu() {
    	Link<Object> link = new Link<Object>("logout") {
    		@Override
            public void onClick() {
                boolean signedIn = ((OpenEngSBWebSession) WebSession.get()).isSignedIn();
                if (signedIn) {
                    ((AuthenticatedWebSession) getSession()).signOut();
                }
                setResponsePage(signedIn ? Index.class : LoginPage.class);
            }
        };
        add(link);
        
        final Label projectLabel = new Label("currentProject",ContextHolder.get().getCurrentContextId());
        add(projectLabel);
        final Label languageLabel = new Label("currentLanguage",getSession().getLocale().getDisplayLanguage());
        languageLabel.setOutputMarkupId(true);
        add(languageLabel);
        
    	add(new AjaxFallbackLink<Object>("lang.en") {
            @Override
			public void onClick(AjaxRequestTarget target) {
				getSession().setLocale(Locale.ENGLISH);
				languageLabel.setDefaultModelObject(getSession().getLocale().getDisplayLanguage());
				target.addComponent(getPage());
			}
        });
    	
    	add(new AjaxFallbackLink<Object>("lang.de") {
            @Override
			public void onClick(AjaxRequestTarget target) {
				getSession().setLocale(Locale.GERMAN);
				languageLabel.setDefaultModelObject(getSession().getLocale().getDisplayLanguage());
				target.addComponent(getPage());
			}
        });
    }
}
