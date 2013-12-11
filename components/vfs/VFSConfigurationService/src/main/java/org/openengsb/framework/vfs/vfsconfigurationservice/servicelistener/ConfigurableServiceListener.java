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

package org.openengsb.framework.vfs.vfsconfigurationservice.servicelistener;

import org.openengsb.framework.vfs.api.configurableservice.ConfigurableService;
import org.openengsb.framework.vfs.vfsconfigurationservice.vfsconfigurationservice.VFSConfigurationService;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author
 * Richard
 */
public class ConfigurableServiceListener {
    private final Logger log = LoggerFactory.getLogger(ConfigurableServiceListener.class);
    private BundleContext context;
    private VFSConfigurationService vfsConfigurationService;
    private ServiceTracker<ConfigurableService, ConfigurableService> tracker;

    public ConfigurableServiceListener(BundleContext bc, VFSConfigurationService vfsConfigurationService) {
        this.context = bc;
        this.vfsConfigurationService = vfsConfigurationService;
    }

    public void open() {
        tracker = new ServiceTracker<ConfigurableService, ConfigurableService>(context,
                ConfigurableService.class, null) {
            @Override
            public ConfigurableService addingService(ServiceReference<ConfigurableService> reference) {

                ConfigurableService service = context.getService(reference);
                register(service);
                context.ungetService(reference);
                return service;
            }

            @Override
            public void removedService(ServiceReference<ConfigurableService> reference, ConfigurableService service) {
                //RepositoryHandler service = service;
                unregister(service);
            }
        };

        tracker.open();
    }

    public void register(ConfigurableService configurableService) {
        log.debug("add new ConfigurableService");
        vfsConfigurationService.setConfigurableService(configurableService);
    }

    public void unregister(ConfigurableService configurableService) {
        log.debug("remove ConfigurableService");
        vfsConfigurationService.setConfigurableServiceLost(configurableService);
    }

    public void close() {
        tracker.close();
    }
}
