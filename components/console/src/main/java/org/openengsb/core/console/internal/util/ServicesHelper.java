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

package org.openengsb.core.console.internal.util;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.openengsb.core.api.AliveState;
import org.openengsb.core.api.ConnectorProvider;
import org.openengsb.core.api.Constants;
import org.openengsb.core.api.Domain;
import org.openengsb.core.api.DomainProvider;
import org.openengsb.core.api.WiringService;
import org.openengsb.core.common.util.Comparators;
import org.openengsb.core.common.util.DefaultOsgiUtilsService;
import org.openengsb.core.common.util.OutputStreamFormater;
import org.openengsb.core.common.util.SecurityUtils;
import org.osgi.framework.BundleContext;

/**
 *
 */
public class ServicesHelper {


    private DefaultOsgiUtilsService osgiUtilsService;
    private WiringService wiringService;


    public ServicesHelper(BundleContext bundleContext) {
        this.osgiUtilsService = new DefaultOsgiUtilsService();
        this.osgiUtilsService.setBundleContext(bundleContext);
        wiringService = osgiUtilsService.getService(org.openengsb.core.api.WiringService.class);
    }

    /**
     * this method prints out services which can be created
     */
    public void listCreatableServices() {
        List<DomainProvider> serviceList = osgiUtilsService.listServices(DomainProvider.class);
        Collections.sort(serviceList, Comparators.forDomainProvider());

        OutputStreamFormater.printValue("Services");
        Locale defaultLocale = Locale.getDefault();

        for (DomainProvider dp : serviceList) {
            OutputStreamFormater
                    .printValue(dp.getName().getString(defaultLocale), dp.getDescription().getString(defaultLocale));
            printConnectorProvidersByDomain(dp.getId());
        }
    }

    /**
     * prints out all available connectors for a given domain provider
     */
    private void printConnectorProvidersByDomain(String domainType) {
        Locale defaultLocale = Locale.getDefault();
        List<ConnectorProvider> connectorProviders = osgiUtilsService.listServices(
                ConnectorProvider.class, String.format("(%s=%s)", Constants.DOMAIN_KEY, domainType));
        if (connectorProviders == null || connectorProviders.size() == 0) {
            OutputStreamFormater.printValue(String.format("No connectors found for domain: %s", domainType));
        } else {
            for (ConnectorProvider connectorProvider : connectorProviders) {
                String serviceId = connectorProvider.getId();
                String serviceName = connectorProvider.getDescriptor().getName().getString(defaultLocale);
                String serviceDescription = connectorProvider.getDescriptor().getDescription().getString(defaultLocale);
                if (serviceId != null && serviceName != null && serviceDescription != null) {
                    OutputStreamFormater.printValuesWithPrefix(serviceId, serviceName, serviceDescription);
                }
            }
        }
    }

    public void createService(InputStream keyboard) {
        //OutputStreamFormater.printValue("Enter Service ID");
        //TODO: not yet implemented
    }

    /**
     * this method prints out all available services and their alive state
     */
    public void listService() {
        Locale defaultLocale = Locale.getDefault();

        List<DomainProvider> serviceList = osgiUtilsService.listServices(DomainProvider.class);
        Collections.sort(serviceList, Comparators.forDomainProvider());

        for (DomainProvider domainProvider : serviceList) {
            OutputStreamFormater
                    .printValue(domainProvider.getName().getString(defaultLocale),
                            domainProvider.getDescription().getString(defaultLocale));

            Class<? extends Domain> domainInterface = domainProvider.getDomainInterface();
            final List<? extends Domain> domainEndpoints = wiringService.getDomainEndpoints(domainInterface, "*");
            try {
                SecurityUtils.executeWithSystemPermissions(new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        for (Domain serviceReference : domainEndpoints) {
                            String id = serviceReference.getInstanceId();
                            AliveState aliveState = serviceReference.getAliveState();
                            if (id != null) {
                                OutputStreamFormater.printTabbedValues(9, id, aliveState.toString());
                            }
                        }
                        return null;
                    }
                });
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }
}
