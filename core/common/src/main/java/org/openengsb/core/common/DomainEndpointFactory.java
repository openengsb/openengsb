/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.core.common;

import java.util.ArrayList;
import java.util.List;

import org.openengsb.core.common.context.ContextHolder;
import org.openengsb.core.common.util.OsgiServiceUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

public final class DomainEndpointFactory {

    private static BundleContext bundleContext;

    public static <T extends Domain> T getDomainEndpoint(Class<T> domainType, String location) {
        Filter filter = OsgiServiceUtils.getFilterForLocation(domainType, location);
        return OsgiServiceUtils.getOsgiServiceProxy(filter, domainType);
    }

    public static <T extends Domain> List<T> getDomainEndpoints(Class<T> domainType, String location) {
        return getDomainEndpoints(domainType, location, ContextHolder.get().getCurrentContextId());
    }

    public static <T extends Domain> T getDomainEndpoint(Class<T> domainType, String location, String context) {
        Filter filter = OsgiServiceUtils.getFilterForLocation(domainType, location, context);
        return OsgiServiceUtils.getOsgiServiceProxy(filter, domainType);
    }

    public static <T extends Domain> List<T> getDomainEndpoints(Class<T> domainType, String location, String context) {
        Filter filterForLocation = OsgiServiceUtils.getFilterForLocation(domainType, location);
        System.out.println("Filter: " + filterForLocation);
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

            return result;
        }
        for (ServiceReference ref : allServiceReferences) {
            String refLocation = (String) ref.getProperty("location." + context);
            result.add(getDomainEndpoint(domainType, refLocation));
        }
        return result;
    }

    public static void setBundleContext(BundleContext bundleContext) {
        DomainEndpointFactory.bundleContext = bundleContext;
    }

    private DomainEndpointFactory() {
    }

}
