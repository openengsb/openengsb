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

package org.openengsb.core.common.util;

import org.openengsb.core.common.OpenEngSBService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

public final class OsgiServiceUtils {

    public static Object getService(BundleContext bundleContext, String className, String filter) {
        ServiceReference[] allServiceReferences;
        try {
            allServiceReferences =
                bundleContext.getServiceReferences(OpenEngSBService.class.getName(), filter);
        } catch (InvalidSyntaxException e1) {
            throw new RuntimeException(e1);
        }
        if (allServiceReferences == null) {
            throw new IllegalArgumentException("service with filter " + filter + " not found");
        }
        if (allServiceReferences.length != 1) {
            throw new IllegalStateException("mutliple services matching " + filter);
        }
        return bundleContext.getService(allServiceReferences[0]);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getService(BundleContext bundleContext, Class<? extends T> clazz, String filter) {
        return (T) getService(bundleContext, clazz.getName(), filter);
    }

    private OsgiServiceUtils() {
    }
}
