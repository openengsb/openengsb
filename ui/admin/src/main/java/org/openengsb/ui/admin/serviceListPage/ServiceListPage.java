/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.ui.admin.serviceListPage;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.extensions.ajax.markup.html.AjaxLazyLoadPanel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.openengsb.core.common.ServiceManager;
import org.openengsb.core.common.service.DomainService;
import org.openengsb.ui.admin.basePage.BasePage;
import org.openengsb.ui.admin.serviceListPanel.ServiceListPanel;
import org.osgi.framework.BundleContext;

@AuthorizeInstantiation("ROLE_USER")
public class ServiceListPage extends BasePage {

    @SpringBean
    private DomainService services;

    @SpringBean
    private BundleContext bundleContext;

    @SpringBean(name = "services")
    private List<ServiceManager> serviceManager;

    public ServiceListPage() {
        initContent();
    }

    public ServiceListPage(PageParameters parameters) {
        super(parameters);
        initContent();
    }

    @SuppressWarnings("serial")
    private void initContent() {
        add(new AjaxLazyLoadPanel("lazy") {
            @Override
            public Component getLazyLoadComponent(String markupId) {
                return new ServiceListPanel(markupId, bundleContext, services, serviceManager);
            }
        });
    }
}
