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

import org.openengsb.framework.vfs.api.remoteservice.RemoteService;
import org.openengsb.framework.vfs.vfsconfigurationservice.vfsconfigurationservice.VFSConfigurationService;


import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoteServiceListener {
    private final Logger log = LoggerFactory.getLogger(RemoteServiceListener.class);
    private BundleContext context;
    private VFSConfigurationService vfsConfigurationService;
    private ServiceTracker<RemoteService, RemoteService> tracker;

    public RemoteServiceListener(BundleContext context, VFSConfigurationService vfsConfigurationService) {
        this.context = context;
        this.vfsConfigurationService = vfsConfigurationService;
    }

    public void open() {
        tracker = new ServiceTracker<RemoteService, RemoteService>(context, RemoteService.class, null) {
            @Override
            public RemoteService addingService(ServiceReference<RemoteService> reference) {
                RemoteService service = context.getService(reference);
                register(service);
                context.ungetService(reference);
                return service;
            }

            @Override
            public void removedService(ServiceReference<RemoteService> reference, RemoteService service) {
                //RepositoryHandler service = service;
                unregister(service);
            }
        };

        tracker.open();
    }

    public void register(RemoteService remoteService) {
        log.debug("add new RemoteService");
        vfsConfigurationService.registerRemoteService(remoteService);
    }

    public void unregister(RemoteService remoteService) {
        log.debug("remove RemoteService");
        vfsConfigurationService.unregisterRemoteService(remoteService);
    }

    public void close() {
        tracker.close();
    }
}
