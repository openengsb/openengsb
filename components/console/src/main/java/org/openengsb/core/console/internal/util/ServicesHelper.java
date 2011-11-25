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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.openengsb.core.api.AliveState;
import org.openengsb.core.api.ConnectorManager;
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
    private ConnectorManager serviceManager;


    public ServicesHelper() {

    }

    public ServicesHelper(BundleContext bundleContext) {
        this.osgiUtilsService = new DefaultOsgiUtilsService();
        this.osgiUtilsService.setBundleContext(bundleContext);
        wiringService = osgiUtilsService.getService(org.openengsb.core.api.WiringService.class);
        serviceManager = osgiUtilsService.getService(org.openengsb.core.api.ConnectorManager.class);
    }

    /**
     * this method prints out all available services and their alive state
     */
    public void listRunningServices() {
        List<String> formatedOutput = getRunningServices();
        for (String s : formatedOutput) {
            OutputStreamFormater.printValue(s);
        }
    }

    /**
     * returns all running services
     */
    public List<String> getRunningServices() {
        final Locale defaultLocale = Locale.getDefault();
        List<String> formatedOutput = new ArrayList<String>();
        List<DomainProvider> serviceList = osgiUtilsService.listServices(DomainProvider.class);
        Collections.sort(serviceList, Comparators.forDomainProvider());

        for (final DomainProvider domainProvider : serviceList) {

            Class<? extends Domain> domainInterface = domainProvider.getDomainInterface();
            final List<? extends Domain> domainEndpoints = wiringService.getDomainEndpoints(domainInterface, "*");
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
        return formatedOutput;
    }

    public void setOsgiUtilsService(DefaultOsgiUtilsService osgiUtilsService) {
        this.osgiUtilsService = osgiUtilsService;
    }

    public void setWiringService(WiringService wiringService) {
        this.wiringService = wiringService;
    }
}
