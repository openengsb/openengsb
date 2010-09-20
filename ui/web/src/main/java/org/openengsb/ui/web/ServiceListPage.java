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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.openengsb.core.common.Domain;
import org.openengsb.core.common.util.AliveEnum;
import org.openengsb.ui.web.model.ServiceId;
import org.openengsb.ui.web.service.DomainService;
import org.osgi.framework.ServiceReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServiceListPage extends BasePage {

    private static Log log = LogFactory.getLog(TestClient.class);

    @SpringBean
    private DomainService services;

    @SpringBean(name = "managedServiceInstances")
    private List<ServiceReference> managedServiceInstances;

    private Map<AliveEnum, List<ServiceReference>> domainServiceMap;

    public ServiceListPage() {
        domainServiceMap = new HashMap<AliveEnum, List<ServiceReference>>();
        domainServiceMap.put(AliveEnum.CONNECTING, new ArrayList<ServiceReference>());
        domainServiceMap.put(AliveEnum.DISCONNECTED, new ArrayList<ServiceReference>());
        domainServiceMap.put(AliveEnum.ONLINE, new ArrayList<ServiceReference>());
        domainServiceMap.put(AliveEnum.OFFLINE, new ArrayList<ServiceReference>());

        log.debug("service list initialized");

        IModel<List<ServiceReference>> connectingServicesLoadableModel = createLoadableServiceReferenceModel(
            AliveEnum.CONNECTING);
        IModel<List<ServiceReference>> onlineServicesLoadableModel = createLoadableServiceReferenceModel(
            AliveEnum.ONLINE);
        IModel<List<ServiceReference>> offlineServicesLoadableModel = createLoadableServiceReferenceModel(
            AliveEnum.OFFLINE);
        IModel<List<ServiceReference>> disconnectedServicesLoadableModel = createLoadableServiceReferenceModel(
            AliveEnum.DISCONNECTED);


        add(createServiceListView(connectingServicesLoadableModel, "connectingServices"));
        add(createServiceListView(connectingServicesLoadableModel, "onlineServices"));
        add(createServiceListView(connectingServicesLoadableModel, "offlineServices"));
        add(createServiceListView(connectingServicesLoadableModel, "disconnectedServices"));

    }

    private ListView<ServiceReference> createServiceListView(
        final IModel<List<ServiceReference>> connectingServicesLoadableModel, String id) {
        return new ListView<ServiceReference>(id, connectingServicesLoadableModel) {

            @Override
            protected void populateItem(ListItem<ServiceReference> item) {
                ServiceReference serv = item.getModelObject();
                item.add(new Label("service.name", (String) serv.getProperty("id")));
                item.add(new Label("service.description", "bla"));
            }
        };
    }

    private LoadableDetachableModel<List<ServiceReference>> createLoadableServiceReferenceModel(final AliveEnum state) {
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
                Domain domainService = (Domain) (services.getService(serviceReference));
                domainServiceMap.get(domainService.getAliveState()).add(serviceReference);
            }
        }
    }


    private Domain getDomainService(ServiceId service) {
        Object serviceObject = services.getService(service.getServiceClass(), service.getServiceId());
        return (Domain) serviceObject;
    }
}
