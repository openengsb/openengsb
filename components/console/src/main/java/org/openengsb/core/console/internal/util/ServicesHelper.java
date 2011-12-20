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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.apache.felix.service.command.CommandProcessor;
import org.apache.felix.service.command.CommandSession;
import org.openengsb.core.api.AliveState;
import org.openengsb.core.api.ConnectorManager;
import org.openengsb.core.api.Domain;
import org.openengsb.core.api.DomainProvider;
import org.openengsb.core.api.WiringService;
import org.openengsb.core.api.model.ConnectorId;
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
    private ConnectorManager serviceManager;
    private InputStream keyboard;
    private CommandSession commandSession;


    public ServicesHelper() {

    }

    public ServicesHelper(BundleContext bundleContext) {
        this.osgiUtilsService = new DefaultOsgiUtilsService();
        this.osgiUtilsService.setBundleContext(bundleContext);
        wiringService = osgiUtilsService.getService(org.openengsb.core.api.WiringService.class);
        serviceManager = osgiUtilsService.getService(org.openengsb.core.api.ConnectorManager.class);
        CommandProcessor commandProcessor = osgiUtilsService
            .getService(org.apache.felix.service.command.CommandProcessor.class);
        commandSession = commandProcessor.createSession(System.in, System.err, System.out);
        keyboard = commandSession.getKeyboard();
    }

    /**
     * this method prints out all available services and their alive state
     */
    public void listRunningServices() {
        final Locale defaultLocale = Locale.getDefault();
        List<String> formatedOutput = new ArrayList<String>();
        Map<DomainProvider, List<? extends Domain>> domainsAndEndpoints = getDomainsAndEndpoints();
        Set<DomainProvider> domainProviders = domainsAndEndpoints.keySet();
        for (final DomainProvider domainProvider : domainProviders) {
            final List<? extends Domain> domainEndpoints = domainsAndEndpoints.get(domainProvider);
            try {
                formatedOutput.addAll(SecurityUtils.executeWithSystemPermissions(new Callable<List<String>>() {
                    @Override
                    public List<String> call() throws Exception {
                        List<String> formatedOutput = new ArrayList<String>();
                        formatedOutput.add(OutputStreamFormater
                            .formatValues(domainProvider.getName().getString(defaultLocale),
                                domainProvider.getDescription().getString(defaultLocale)));
                        for (Domain serviceReference : domainEndpoints) {
                            String id = serviceReference.getInstanceId();
                            AliveState aliveState = serviceReference.getAliveState();
                            if (id != null) {
                                formatedOutput.add(OutputStreamFormater.formatValues(9, id, aliveState.toString()));
                            }
                        }
                        return formatedOutput;
                    }
                }));
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        for (String s : formatedOutput) {
            OutputStreamFormater.printValue(s);
        }
    }


    /**
     * returns all running services
     */
    public List<Domain> getRunningServices() {
        Map<DomainProvider, List<? extends Domain>> domainsAndEndpoints = getDomainsAndEndpoints();
        List<Domain> endpoints = new ArrayList<Domain>();
        Set<DomainProvider> domainProviders = domainsAndEndpoints.keySet();
        for (DomainProvider provider : domainProviders) {
            endpoints.addAll(domainsAndEndpoints.get(provider));
        }
        return endpoints;
    }

    public Map<DomainProvider, List<? extends Domain>> getDomainsAndEndpoints() {
        Map<DomainProvider, List<? extends Domain>> domainsAndEndpoints
            = new HashMap<DomainProvider, List<? extends Domain>>();
        List<DomainProvider> serviceList = osgiUtilsService.listServices(DomainProvider.class);
        Collections.sort(serviceList, Comparators.forDomainProvider());

        for (final DomainProvider domainProvider : serviceList) {
            Class<? extends Domain> domainInterface = domainProvider.getDomainInterface();
            List<? extends Domain> domainEndpoints = wiringService.getDomainEndpoints(domainInterface, "*");
            domainsAndEndpoints.put(domainProvider, domainEndpoints);
        }
        return domainsAndEndpoints;
    }

    /**
     * delete a service identified by its id
     */
    public void deleteService(final String id, boolean force) {
        if (!force) {
            OutputStreamFormater
                .printValue(String.format("Do you really want to delete the connector: %s (Y/n): ", id));
        }
        try {
            int input = force ? 'Y' : keyboard.read();
            if ('Y' == (char) input) {
                SecurityUtils.executeWithSystemPermissions(new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        ConnectorId fullId = ConnectorId.fromFullId(id);
                        serviceManager.delete(fullId);
                        return null;
                    }
                });
                OutputStreamFormater.printValue(String.format("Service: %s successfully deleted", id));
            }
        } catch (ExecutionException e) {
            System.err.println("Could not delete service");
        } catch (IOException e) {
            System.err.println("Unexpected Error");
            e.printStackTrace();
        }
    }

    /**
     * returns a list of all ids
     */
    public List<String> getRunningServiceIds() {
        final List<Domain> runningServices = getRunningServices();
        List<String> result = new ArrayList<String>();

        try {
            result = SecurityUtils.executeWithSystemPermissions(new Callable<List<String>>() {
                @Override
                public List<String> call() throws Exception {
                    List<String> ids = new ArrayList<String>();
                    for (Domain d : runningServices) {
                        String id = d.getInstanceId();
                        if (id != null) {
                            ids.add(id);
                        }
                    }
                    return ids;
                }
            });
        } catch (ExecutionException e) {
            //ignore
        }
        return result;
    }

    public void setOsgiUtilsService(DefaultOsgiUtilsService osgiUtilsService) {
        this.osgiUtilsService = osgiUtilsService;
    }

    public void setWiringService(WiringService wiringService) {
        this.wiringService = wiringService;
    }

    public InputStream getKeyboard() {
        return keyboard;
    }

    public CommandSession getCommandSession() {
        return commandSession;
    }
}
