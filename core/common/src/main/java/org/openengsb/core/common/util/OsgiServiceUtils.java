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

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.util.tracker.ServiceTracker;

public final class OsgiServiceUtils {

    private static final long DEFAULT_TIMEOUT = 30000L;

    public static <T> T getService(BundleContext bundleContext, Class<T> clazz) {
        return getService(bundleContext, clazz, DEFAULT_TIMEOUT);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getService(BundleContext bundleContext, Class<T> clazz, long timeout) {
        ServiceTracker t = new ServiceTracker(bundleContext, clazz.getName(), null);
        T result;
        try {
            result = (T) t.waitForService(timeout);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (result == null) {
            throw new RuntimeException("service not available");
        }
        return result;
    }

    public static <T> T getService(BundleContext bundleContext, String filterString) {
        return getService(bundleContext, filterString, DEFAULT_TIMEOUT);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getService(BundleContext bundleContext, String filterString, long timeout) {
        Filter filter;
        try {
            filter = bundleContext.createFilter(filterString);
        } catch (InvalidSyntaxException e1) {
            throw new IllegalArgumentException(e1);
        }
        ServiceTracker t = new ServiceTracker(bundleContext, filter, null);
        t.open();
        Object result;
        try {
            result = t.waitForService(timeout);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (result == null) {
            throw new RuntimeException("service not available");
        }
        return (T) result;
    }

    public static <T> T getServiceWithId(BundleContext bundleContext, Class<? extends T> clazz, String id) {
        return getServiceWithId(bundleContext, clazz, id, DEFAULT_TIMEOUT);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getServiceWithId(BundleContext bundleContext, Class<? extends T> clazz, String id,
            long timeout) {
        return (T) getServiceWithId(bundleContext, clazz.getName(), id, timeout);
    }

    public static Object getServiceWithId(BundleContext bundleContext, String className, String id) {
        return getServiceWithId(bundleContext, className, id, DEFAULT_TIMEOUT);
    }

    public static Object getServiceWithId(BundleContext bundleContext, String className, String id, long timeout) {
        String filter = String.format("(&(%s=%s)(id=%s))", Constants.OBJECTCLASS, className, id);
        filter = String.format("(id=%s)", id);
        return getService(bundleContext, filter, timeout);
    }

    private OsgiServiceUtils() {
    }

}
