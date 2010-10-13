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

package org.openengsb.ui.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.openengsb.core.common.Domain;
import org.openengsb.core.common.ServiceManager;
import org.openengsb.core.common.descriptor.ServiceDescriptor;
import org.openengsb.core.common.l10n.LocalizableString;
import org.openengsb.core.common.l10n.PassThroughLocalizableString;
import org.openengsb.core.common.service.DomainService;
import org.openengsb.core.common.util.AliveState;
import org.openengsb.ui.web.model.LocalizableStringModel;
import org.osgi.framework.ServiceReference;

public class ServiceListPage extends BasePage {

    private static Log log = LogFactory.getLog(ServiceListPage.class);

    @SpringBean
    private DomainService services;

    @SpringBean(name = "managedServiceInstances")
    private List<ServiceReference> managedServiceInstances;

    @SpringBean(name = "services")
    private List<ServiceManager> serviceManager;

    private final Map<AliveState, List<ServiceReference>> domainServiceMap;

    public ServiceListPage() {
        domainServiceMap = new HashMap<AliveState, List<ServiceReference>>();
        domainServiceMap.put(AliveState.CONNECTING, new ArrayList<ServiceReference>());
        domainServiceMap.put(AliveState.DISCONNECTED, new ArrayList<ServiceReference>());
        domainServiceMap.put(AliveState.ONLINE, new ArrayList<ServiceReference>());
        domainServiceMap.put(AliveState.OFFLINE, new ArrayList<ServiceReference>());

        log.debug("service list initialized");

        IModel<List<ServiceReference>> connectingServicesLoadableModel =
            createLoadableServiceReferenceModel(AliveState.CONNECTING);
        IModel<List<ServiceReference>> onlineServicesLoadableModel =
            createLoadableServiceReferenceModel(AliveState.ONLINE);
        IModel<List<ServiceReference>> offlineServicesLoadableModel =
            createLoadableServiceReferenceModel(AliveState.OFFLINE);
        IModel<List<ServiceReference>> disconnectedServicesLoadableModel =
            createLoadableServiceReferenceModel(AliveState.DISCONNECTED);

        WebMarkupContainer connectingServicePanel = new WebMarkupContainer("connectingServicePanel");
        connectingServicePanel.setOutputMarkupId(true);
        WebMarkupContainer onlineServicePanel = new WebMarkupContainer("onlineServicePanel");
        onlineServicePanel.setOutputMarkupId(true);
        WebMarkupContainer offlineServicePanel = new WebMarkupContainer("offlineServicePanel");
        offlineServicePanel.setOutputMarkupId(true);
        WebMarkupContainer disconnectedServicePanel = new WebMarkupContainer("disconnectedServicePanel");
        disconnectedServicePanel.setOutputMarkupId(true);

        Label noConServices =
            new Label("noConServices", new StringResourceModel("noServicesAvailable", this, null).getString());
        Label noOnServices =
            new Label("noOnServices", new StringResourceModel("noServicesAvailable", this, null).getString());
        Label noOffServices =
            new Label("noOffServices", new StringResourceModel("noServicesAvailable", this, null).getString());
        Label noDiscServices =
            new Label("noDisServices", new StringResourceModel("noServicesAvailable", this, null).getString());

        connectingServicePanel.add(createServiceListView(connectingServicesLoadableModel, "connectingServices",
            noConServices, connectingServicePanel));
        onlineServicePanel.add(createServiceListView(onlineServicesLoadableModel, "onlineServices", noOnServices,
            onlineServicePanel));
        offlineServicePanel.add(createServiceListView(offlineServicesLoadableModel, "offlineServices", noOffServices,
            offlineServicePanel));
        disconnectedServicePanel.add(createServiceListView(disconnectedServicesLoadableModel, "disconnectedServices",
            noDiscServices, disconnectedServicePanel));

        noConServices.setVisible(false);
        noOnServices.setVisible(false);
        noOffServices.setVisible(false);
        noDiscServices.setVisible(false);

        noConServices.setOutputMarkupId(true);
        noOnServices.setOutputMarkupId(true);
        noOffServices.setOutputMarkupId(true);
        noDiscServices.setOutputMarkupId(true);

        connectingServicePanel.add(noConServices);
        onlineServicePanel.add(noOnServices);
        offlineServicePanel.add(noOffServices);
        disconnectedServicePanel.add(noDiscServices);

        add(connectingServicePanel);
        add(onlineServicePanel);
        add(offlineServicePanel);
        add(disconnectedServicePanel);

        if (connectingServicesLoadableModel.getObject().isEmpty()) {
            noConServices.setVisible(true);
        }
        if (onlineServicesLoadableModel.getObject().isEmpty()) {
            noOnServices.setVisible(true);
        }
        if (offlineServicesLoadableModel.getObject().isEmpty()) {
            noOffServices.setVisible(true);
        }
        if (disconnectedServicesLoadableModel.getObject().isEmpty()) {
            noDiscServices.setVisible(true);
        }
    }

    @SuppressWarnings("serial")
    private ListView<ServiceReference> createServiceListView(final IModel<List<ServiceReference>> serviceLoadableModel,
            final String id, final Label noServicesLabel, final WebMarkupContainer serviceWebMarkupContainer) {
        return new ListView<ServiceReference>(id, serviceLoadableModel) {

            @Override
            protected void populateItem(final ListItem<ServiceReference> item) {
                final ServiceReference serv = item.getModelObject();
                final ServiceManager sm = getServiceManager((String) serv.getProperty("managerId"));
                final String id = (String) serv.getProperty("id");
                LocalizableString description = new PassThroughLocalizableString("");
                if (sm != null) {
                    ServiceDescriptor desc = sm.getDescriptor();
                    description = desc.getDescription();
                }
                item.add(new Label("service.name", id));
                item.add(new Label("service.description", new LocalizableStringModel(this, description)));
                item.add(new AjaxLink<ServiceManager>("updateService",
                        createLoadableDetachableServiceManagerModel(sm)) {
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        setResponsePage(new ConnectorEditorPage(getModelObject(), id));
                    }
                });
                item.add(new AjaxLink<ServiceManager>("deleteService",
                        createLoadableDetachableServiceManagerModel(sm)) {

                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        getList().remove(item.getModelObject());
                        sm.delete(id);
                        noServicesLabel.setVisible(getList().size() <= 0);
                        target.addComponent(noServicesLabel);
                        target.addComponent(serviceWebMarkupContainer);
                    }
                });
            }
        };
    }

    @SuppressWarnings("serial")
    private LoadableDetachableModel<ServiceManager> createLoadableDetachableServiceManagerModel(
            final ServiceManager sm) {
        return new LoadableDetachableModel<ServiceManager>() {
            @Override
            protected ServiceManager load() {
                return sm;
            }
        };
    }

    private ServiceManager getServiceManager(String managerId) {
        for (ServiceManager sm : serviceManager) {
            if (managerId.equals(sm.getDescriptor().getId())) {
                return sm;
            }
        }
        return null;
    }

    @SuppressWarnings("serial")
    private LoadableDetachableModel<List<ServiceReference>> createLoadableServiceReferenceModel(
            final AliveState state) {
        return new LoadableDetachableModel<List<ServiceReference>>() {
            @Override
            protected List<ServiceReference> load() {
                updateDomainServiceMap();
                List<ServiceReference> managers = new ArrayList<ServiceReference>(services.domains().size());
                managers.addAll(domainServiceMap.get(state));
                return managers;
            }
        };
    }

    private void updateDomainServiceMap() {
        for (ServiceReference serviceReference : managedServiceInstances) {
            if (!"domain".equals(serviceReference.getProperty("openengsb.service.type"))) {
                Domain domainService = (Domain) services.getService(serviceReference);

                List<ServiceReference> serviceReferenceList = domainServiceMap.get(domainService.getAliveState());
                if (!serviceReferenceList.contains(serviceReference)) {
                    serviceReferenceList.add(serviceReference);
                }
            }
        }
    }

}
