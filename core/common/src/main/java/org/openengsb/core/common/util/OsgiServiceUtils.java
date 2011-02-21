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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.util.tracker.ServiceTracker;

public final class OsgiServiceUtils {

    private static final long DEFAULT_TIMEOUT = 30000L;

    /**
     * retrieves the highest ranked service exporting the given interface.
     *
     * @throws OsgiServiceNotAvailableException when the service is not available after 30 seconds
     */
    public static <T> T getService(BundleContext bundleContext, Class<T> clazz)
        throws OsgiServiceNotAvailableException {
        return getService(bundleContext, clazz, DEFAULT_TIMEOUT);
    }

    /**
     * retrieves the highest ranked service exporting the given interface.
     *
     * @throws OsgiServiceNotAvailableException when the service is not available after the given timeout
     */
    @SuppressWarnings("unchecked")
    public static <T> T getService(BundleContext bundleContext, Class<T> clazz, long timeout)
        throws OsgiServiceNotAvailableException {
        ServiceTracker tracker = new ServiceTracker(bundleContext, clazz.getName(), null);
        Object result = getServiceFromTracker(tracker, timeout);
        if (result == null) {
            throw new OsgiServiceNotAvailableException(String.format("no service of type %s available at the time",
                clazz.getName()));
        }
        return (T) result;
    }

    public static <T> T getService(BundleContext bundleContext, Filter filter)
        throws OsgiServiceNotAvailableException {
        return getService(bundleContext, filter, DEFAULT_TIMEOUT);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getService(BundleContext bundleContext, Filter filter, long timeout)
        throws OsgiServiceNotAvailableException {
        ServiceTracker t = new ServiceTracker(bundleContext, filter, null);
        Object result = getServiceFromTracker(t, timeout);
        if (result == null) {
            throw new OsgiServiceNotAvailableException(String.format(
                "no service matching filter \"%s\" available at the time", filter.toString()));
        }
        return (T) result;
    }

    /**
     * retrieve the highest ranked service that matches the given filter
     *
     * @throws OsgiServiceNotAvailableException when the service is not available after 30 seconds
     */
    public static <T> T getService(BundleContext bundleContext, String filterString)
        throws OsgiServiceNotAvailableException {
        return getService(bundleContext, filterString, DEFAULT_TIMEOUT);
    }

    /**
     * retrieve the highest ranked service that matches the given filter
     *
     * @throws OsgiServiceNotAvailableException when the service is not available after the given timeout
     */
    public static <T> T getService(BundleContext bundleContext, String filterString, long timeout)
        throws OsgiServiceNotAvailableException {
        Filter filter;
        try {
            filter = bundleContext.createFilter(filterString);
        } catch (InvalidSyntaxException e1) {
            throw new IllegalArgumentException(e1);
        }
        return getService(bundleContext, filter, timeout);
    }

    /**
     * retrieves the highest ranked service that exports the given interface and the has the given instanceid
     *
     * @throws OsgiServiceNotAvailableException when the service is not available after 30 seconds
     */
    public static <T> T getServiceWithId(BundleContext bundleContext, Class<? extends T> clazz, String id)
        throws OsgiServiceNotAvailableException {
        return getServiceWithId(bundleContext, clazz, id, DEFAULT_TIMEOUT);
    }

    /**
     * retrieves the highest ranked service that exports the given interface and the has the given instanceid
     *
     * @throws OsgiServiceNotAvailableException when the service is not available after the given timeout
     */
    @SuppressWarnings("unchecked")
    public static <T> T getServiceWithId(BundleContext bundleContext, Class<? extends T> clazz, String id,
            long timeout) throws OsgiServiceNotAvailableException {
        return (T) getServiceWithId(bundleContext, clazz.getName(), id, timeout);
    }

    /**
     * retrieves the highest ranked service that exports the given interface and the has the given instanceid
     *
     * @throws OsgiServiceNotAvailableException when the service is not available after 30 seconds
     */
    public static Object getServiceWithId(BundleContext bundleContext, String className, String id)
        throws OsgiServiceNotAvailableException {
        return getServiceWithId(bundleContext, className, id, DEFAULT_TIMEOUT);
    }

    /**
     * retrieves the highest ranked service that exports the given interface and the has the given instanceid
     *
     * @throws OsgiServiceNotAvailableException when the service is not available after the given timeout
     */
    public static Object getServiceWithId(BundleContext bundleContext, String className, String id, long timeout)
        throws OsgiServiceNotAvailableException {
        Filter filter;
        try {
            filter = makeFilter(className, String.format("(id=%s)", id));
        } catch (InvalidSyntaxException e) {
            throw new IllegalArgumentException(e);
        }
        return getService(bundleContext, filter, timeout);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getOsgiServiceProxy(final BundleContext bundleContext, final Filter filter,
            Class<T> targetClass) {
        return (T) Proxy.newProxyInstance(targetClass.getClassLoader(), new Class<?>[]{ targetClass },
            new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    Object service = OsgiServiceUtils.getService(bundleContext, filter);
                    return method.invoke(service, args);
                }
            });
    }

    public static Filter makeFilterForClass(Class<?> clazz) {
        return makeFilterForClass(clazz.getName());
    }

    public static Filter makeFilterForClass(String className) {
        try {
            return FrameworkUtil.createFilter(String.format("(%s=%s)", Constants.OBJECTCLASS, className));
        } catch (InvalidSyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static Filter makeFilter(Class<?> clazz, String otherFilter) throws InvalidSyntaxException {
        return makeFilter(clazz.getName(), otherFilter);
    }

    public static Filter makeFilter(String className, String otherFilter) throws InvalidSyntaxException {
        return FrameworkUtil.createFilter("(&" + makeFilterForClass(className) + otherFilter + ")");
    }

    private static Object getServiceFromTracker(ServiceTracker tracker, long timeout)
        throws OsgiServiceNotAvailableException {
        tracker.open();
        try {
            return tracker.waitForService(timeout);
        } catch (InterruptedException e) {
            throw new OsgiServiceNotAvailableException(e);
        } finally {
            tracker.close();
        }
    }

    private OsgiServiceUtils() {
    }

}
