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

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class HttpServiceTracker extends ServiceTracker {

    private final Logger logger = LoggerFactory.getLogger(HttpServiceTracker.class);

    public HttpServiceTracker(BundleContext context) {
        super(context, HttpService.class.getName(), null);
    }

    @Override
    public Object addingService(ServiceReference reference) {
        logger.info("HttpServiceTracker found new HttpService");
        HttpService http = (HttpService) super.addingService(reference);
        return http;
    }

    @Override
    public void removedService(ServiceReference reference, Object service) {
        logger.info("HttpServiceTracker unregistered HttpService");
        super.removedService(reference, service);
    }

    @Override
    public void modifiedService(ServiceReference reference, Object service) {
        logger.info("HttpServiceTracker modified HttpService");
        super.modifiedService(reference, service);
    }
}
