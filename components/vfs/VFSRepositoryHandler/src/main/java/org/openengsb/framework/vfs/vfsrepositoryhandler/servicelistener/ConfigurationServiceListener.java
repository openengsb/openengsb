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

package org.openengsb.framework.vfs.vfsrepositoryhandler.servicelistener;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import org.openengsb.framework.vfs.api.configurationservice.ConfigurationService;
import org.openengsb.framework.vfs.vfsrepositoryhandler.VFSRepositoryHandler;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Richard
 */
public class ConfigurationServiceListener {

    private final Logger log = LoggerFactory.getLogger(ConfigurationServiceListener.class);
    private BundleContext context;
    private VFSRepositoryHandler vfsRepositoryHandler;
    private ServiceTracker<ConfigurationService, ConfigurationService> tracker;

    public ConfigurationServiceListener(BundleContext context, VFSRepositoryHandler vfsRepositoryHandler) {
        this.context = context;
        this.vfsRepositoryHandler = vfsRepositoryHandler;
    }

    public void open() {
        tracker = new ServiceTracker<ConfigurationService, ConfigurationService>(
                context, ConfigurationService.class, null) {
            @Override
            public ConfigurationService addingService(ServiceReference<ConfigurationService> reference) {
                ConfigurationService service = context.getService(reference);
                register(service);
                context.ungetService(reference);
                return service;
            }

            @Override
            public void removedService(ServiceReference<ConfigurationService> reference, ConfigurationService service) {
                //RepositoryHandler service = service;
                unregister(service);
            }
        };

        tracker.open();
    }

    public void register(ConfigurationService configurationService) {
        log.debug("add new ConfigurationService");
        vfsRepositoryHandler.registerConfigurationService(configurationService);
    }

    public void unregister(ConfigurationService configurationService) {
        log.debug("remove ConfigurationService");
        vfsRepositoryHandler.deregisterConfigurationService();
    }

    public void close() {
        tracker.close();
    }
}
