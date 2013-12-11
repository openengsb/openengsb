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

package org.openengsb.framework.vfs.webdavprotocol.servicelistener;

import org.openengsb.framework.vfs.api.repositoryhandler.RepositoryHandler;
import org.openengsb.framework.vfs.webdavprotocol.webdavhandler.WebDavHandler;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RepositoryHandlerListener {

    private final Logger log = LoggerFactory.getLogger(RepositoryHandlerListener.class);
    private BundleContext context;
    private WebDavHandler webDavHandler;
    private ServiceTracker<RepositoryHandler, RepositoryHandler> tracker;

    public RepositoryHandlerListener(BundleContext context, WebDavHandler webDavHandler) {
        this.context = context;
        this.webDavHandler = webDavHandler;
    }

    public void open() {
        tracker = new ServiceTracker<RepositoryHandler, RepositoryHandler>(context, RepositoryHandler.class, null) {
            @Override
            public RepositoryHandler addingService(ServiceReference<RepositoryHandler> reference) {

                RepositoryHandler service = context.getService(reference);
                register(service);
                context.ungetService(reference);
                return service;
            }

            @Override
            public void removedService(ServiceReference<RepositoryHandler> reference, RepositoryHandler service) {
                //RepositoryHandler service = service;
                unregister(service);
            }
        };

        tracker.open();
    }

    public void register(RepositoryHandler repositoryHandler) {
        log.debug("add new RepositoryHandler");
        webDavHandler.registerRepositoryHandler(repositoryHandler);
    }

    public void unregister(RepositoryHandler repositoryHandler) {
        log.debug("remove RepositoryHandler");
        webDavHandler.unregisterRepositoryHandler();
    }

    public void close() {
        tracker.close();
    }
}
