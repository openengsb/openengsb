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

import org.openengsb.domain.authentication.AuthenticationDomain;
import org.openengsb.framework.vfs.webdavprotocol.webdavhandler.WebDavHandler;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthenticationDomainTracker {

    private final Logger log = LoggerFactory.getLogger(AuthenticationDomainTracker.class);
    private BundleContext context;
    private WebDavHandler webDavHandler;
    private ServiceTracker<AuthenticationDomain, AuthenticationDomain> tracker;

    public AuthenticationDomainTracker(BundleContext context, WebDavHandler webDavHandler) {
        this.context = context;
        this.webDavHandler = webDavHandler;
    }

    public void open() {
        tracker = new ServiceTracker
                <AuthenticationDomain, AuthenticationDomain>(context, AuthenticationDomain.class, null) {
            @Override
            public AuthenticationDomain addingService(ServiceReference<AuthenticationDomain> reference) {

                AuthenticationDomain service = context.getService(reference);
                register(service);
                return service;
            }

            @Override
            public void removedService(ServiceReference<AuthenticationDomain> reference, AuthenticationDomain service) {
                unregister(service);
            }
        };
        tracker.open();
    }

    public void register(AuthenticationDomain authenticationDomain) {
        log.debug("add new AuthenticationDomain");
        webDavHandler.registerAuthenticationDomainService(authenticationDomain);
    }

    public void unregister(AuthenticationDomain authenticationDomain) {
        log.debug("remove HttpService");
        webDavHandler.unregisterAuthenticationDomainService();
    }

    public void close() {
        tracker.close();
    }
}
