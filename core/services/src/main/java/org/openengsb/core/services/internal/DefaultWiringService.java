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

package org.openengsb.core.services.internal;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openengsb.core.api.Domain;
import org.openengsb.core.api.OsgiServiceNotAvailableException;
import org.openengsb.core.api.OsgiUtilsService;
import org.openengsb.core.api.WiringService;
import org.openengsb.core.api.context.ContextHolder;
import org.openengsb.core.common.OpenEngSBCoreServices;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

/**
 * Default Wiring Implementation which can be overwritten by another service implementation easily if required.
 */
public class DefaultWiringService implements WiringService {

    private static final long DEFAULT_TIMEOUT = 5000L;
    private static final Log LOGGER = LogFactory.getLog(DefaultWiringService.class);

    private BundleContext bundleContext;

    @Override
    public <T extends Domain> T getDomainEndpoint(Class<T> domainType, String location) {
        Filter filter = getServiceUtils().getFilterForLocation(domainType, location);
        return getServiceUtils().getOsgiServiceProxy(filter, domainType);
    }

    @Override
    public <T extends Domain> List<T> getDomainEndpoints(Class<T> domainType, String location) {
        return getDomainEndpoints(domainType, location, ContextHolder.get().getCurrentContextId());
    }

    @Override
    public <T extends Domain> T getDomainEndpoint(Class<T> domainType, String location, String context) {
        Filter filter = getServiceUtils().getFilterForLocation(domainType, location, context);
        return getServiceUtils().getOsgiServiceProxy(filter, domainType);
    }

    @Override
    public <T extends Domain> List<T> getDomainEndpoints(Class<T> domainType, String location, String context) {
        Filter filterForLocation = getServiceUtils().getFilterForLocation(domainType, location);
        ServiceReference[] allServiceReferences;
        try {
            allServiceReferences =
                bundleContext.getAllServiceReferences(domainType.getName(), filterForLocation.toString());
        } catch (InvalidSyntaxException e) {
            // this can never happen, because the filter has been compiled before
            throw new RuntimeException(e);
        }
        List<T> result = new ArrayList<T>();
        if (allServiceReferences == null) {
            LOGGER.info("no references found for filter: " + filterForLocation.toString());
            return result;
        }
        LOGGER.debug(String.format("found %s references for %s", allServiceReferences.length, filterForLocation));
        for (ServiceReference ref : allServiceReferences) {
            Object serviceId = ref.getProperty(Constants.SERVICE_ID);
            String filterString = String.format("(%s=%s)", Constants.SERVICE_ID, serviceId);
            try {
                T osgiServiceProxy =
                    getServiceUtils().getOsgiServiceProxy(FrameworkUtil.createFilter(filterString), domainType);
                result.add(osgiServiceProxy);
            } catch (InvalidSyntaxException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    @Override
    public boolean isConnectorCurrentlyPresent(Class<? extends Domain> domainType) {
        Domain service;
        try {
            service = getServiceUtils().getService(domainType, DEFAULT_TIMEOUT);
        } catch (OsgiServiceNotAvailableException e) {
            return false;
        }
        return service != null;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    private OsgiUtilsService getServiceUtils() {
        return OpenEngSBCoreServices.getServiceUtilsService();
    }
}

