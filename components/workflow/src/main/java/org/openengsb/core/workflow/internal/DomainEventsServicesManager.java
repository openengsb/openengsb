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
package org.openengsb.core.workflow.internal;

import java.lang.reflect.Proxy;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;

import org.openengsb.core.api.Constants;
import org.openengsb.core.api.DomainEvents;
import org.openengsb.core.api.workflow.WorkflowService;
import org.openengsb.core.common.events.ForwardHandler;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

public class DomainEventsServicesManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(DomainEventsServicesManager.class);

    private Map<Bundle, ServiceRegistration> handledDomains = Maps.newHashMap();
    private BundleContext bundleContext;
    private WorkflowService workflowService;

    private BundleTracker bundleTracker;

    public void start() {
        bundleTracker = new BundleTracker(bundleContext, Bundle.ACTIVE, new BundleTrackerCustomizer() {
            @Override
            public void removedBundle(Bundle bundle, BundleEvent event, Object object) {
            }

            @Override
            public void modifiedBundle(Bundle bundle, BundleEvent event, Object object) {
            }

            @Override
            public Object addingBundle(Bundle bundle, BundleEvent event) {
                startDomainServices(bundle);
                return bundle;
            }
        });
        bundleTracker.open();
    }

    public void stop() {
        bundleTracker.close();
    }

    private synchronized void startDomainServices(Bundle bundle) {
        Dictionary<String, String> headers = bundle.getHeaders();
        String interfacename = headers.get(Constants.DOMAIN_EVENTS_INTERFACE_HEADER);
        if (interfacename == null) {
            return;
        }
        String domainname = headers.get(Constants.DOMAIN_NAME_HEADER);
        if (domainname == null) {
            return;
        }
        try {
            doRegisterService(bundle, interfacename, domainname);
        } catch (ClassNotFoundException e) {
            LOGGER.error("unable to register DomainEventsService for bundle {}", bundle, e);
        }
    }

    private void doRegisterService(Bundle bundle, String interfacename, String domainname)
        throws ClassNotFoundException {
        Object proxy = createProxy(bundle, interfacename);
        Hashtable<String, Object> props = new Hashtable<String, Object>();
        props.put(org.osgi.framework.Constants.SERVICE_PID, String.format("domain.%s.events", domainname));
        props.put("openengsb.service.type", "domain-events");
        ServiceRegistration<?> registerService =
            bundle.getBundleContext().registerService(interfacename, proxy, props);
        handledDomains.put(bundle, registerService);
    }

    private Object createProxy(Bundle bundle, String interfacename) throws ClassNotFoundException {
        Class<?> interfaceClass = bundle.loadClass(interfacename);
        ClassLoader classLoader = interfaceClass.getClassLoader();
        Class<?>[] classes = new Class<?>[]{ DomainEvents.class, interfaceClass };
        ForwardHandler forwardHandler = new ForwardHandler(workflowService);
        return Proxy.newProxyInstance(classLoader, classes, forwardHandler);
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public void setWorkflowService(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

}
