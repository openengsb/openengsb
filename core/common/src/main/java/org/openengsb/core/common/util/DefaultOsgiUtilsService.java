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

package org.openengsb.core.common.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openengsb.core.api.OsgiServiceNotAvailableException;
import org.openengsb.core.api.OsgiUtilsService;
import org.openengsb.core.api.context.ContextHolder;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

public class DefaultOsgiUtilsService implements OsgiUtilsService {

    private final class ServiceTrackerInvocationHandler implements InvocationHandler {
        private ServiceTracker tracker;

        private ServiceTrackerInvocationHandler(ServiceTracker tracker) {
            this.tracker = tracker;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Object service = tracker.getService();
            try {
                return method.invoke(service, args);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        }
    }

    private static final Log LOGGER = LogFactory.getLog(DefaultOsgiUtilsService.class);
    private static final long DEFAULT_TIMEOUT = 30000L;

    private BundleContext bundleContext;

    @Override
    public <T> T getService(Class<T> clazz) throws OsgiServiceNotAvailableException {
        return getService(clazz, DEFAULT_TIMEOUT);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getService(Class<T> clazz, long timeout) throws OsgiServiceNotAvailableException {
        ServiceTracker tracker = new ServiceTracker(bundleContext, clazz.getName(), null);
        Object result = getServiceFromTracker(tracker, timeout);
        if (result == null) {
            throw new OsgiServiceNotAvailableException(String.format("no service of type %s available at the time",
                clazz.getName()));
        }
        return (T) result;
    }

    @Override
    public Object getService(Filter filter)
        throws OsgiServiceNotAvailableException {
        return getService(filter, DEFAULT_TIMEOUT);
    }

    @Override
    public Object getService(Filter filter, long timeout) throws OsgiServiceNotAvailableException {
        ServiceTracker t = new ServiceTracker(bundleContext, filter, null);
        LOGGER.debug("getting service for filter " + filter + " from tracker");
        Object result = getServiceFromTracker(t, timeout);
        if (result == null) {
            throw new OsgiServiceNotAvailableException(String.format(
                "no service matching filter \"%s\" available at the time", filter.toString()));
        }
        return result;
    }

    @Override
    public Object getService(String filterString) throws OsgiServiceNotAvailableException {
        return getService(filterString, DEFAULT_TIMEOUT);
    }

    @Override
    public Object getService(String filterString, long timeout) throws OsgiServiceNotAvailableException {
        Filter filter;
        try {
            filter = FrameworkUtil.createFilter(filterString);
        } catch (InvalidSyntaxException e1) {
            throw new IllegalArgumentException(e1);
        }
        return getService(filter, timeout);
    }

    @Override
    public <T> T getServiceWithId(Class<? extends T> clazz, String id) throws OsgiServiceNotAvailableException {
        return getServiceWithId(clazz, id, DEFAULT_TIMEOUT);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getServiceWithId(Class<? extends T> clazz, String id, long timeout)
        throws OsgiServiceNotAvailableException {
        return (T) getServiceWithId(clazz.getName(), id, timeout);
    }

    @Override
    public Object getServiceWithId(String className, String id) throws OsgiServiceNotAvailableException {
        return getServiceWithId(className, id, DEFAULT_TIMEOUT);
    }

    @Override
    public Object getServiceWithId(String className, String id, long timeout) throws OsgiServiceNotAvailableException {
        Filter filter;
        try {
            filter = makeFilter(className, String.format("(id=%s)", id));
        } catch (InvalidSyntaxException e) {
            throw new IllegalArgumentException(e);
        }
        return getService(filter, timeout);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getOsgiServiceProxy(final Filter filter, Class<T> targetClass, final long timeout) {
        return (T) Proxy.newProxyInstance(targetClass.getClassLoader(), new Class<?>[] { targetClass },
            new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    LOGGER.info("dynamically resolving service for filter : " + filter);
                    Object service = getService(filter, timeout);
                    return method.invoke(service, args);
                }
            });
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getOsgiServiceProxy(final String filter, Class<T> targetClass, final long timeout) {
        return (T) Proxy.newProxyInstance(targetClass.getClassLoader(), new Class<?>[] { targetClass },
            new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    LOGGER.info("dynamically resolving service for filter : " + filter);
                    Object service = getService(filter, timeout);
                    return method.invoke(service, args);
                }
            });
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getOsgiServiceProxy(final Class<T> targetClass, final long timeout) {

        return (T) Proxy.newProxyInstance(targetClass.getClassLoader(), new Class<?>[] { targetClass },
            new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    LOGGER.info("dynamically resolving service for class : " + targetClass.toString());
                    Object service = getService(targetClass, timeout);
                    try {
                        return method.invoke(service, args);
                    } catch (InvocationTargetException e) {
                        throw e.getCause();
                    }
                }
            });
    }

    @Override
    public <T> T getOsgiServiceProxy(Class<T> targetClass) {
        return getOsgiServiceProxy(targetClass, DEFAULT_TIMEOUT);
    }

    @Override
    public <T> T getOsgiServiceProxy(Filter filter, Class<T> targetClass) {
        return getOsgiServiceProxy(filter, targetClass, DEFAULT_TIMEOUT);
    }

    @Override
    public <T> T getOsgiServiceProxy(String filter, Class<T> targetClass) {
        return getOsgiServiceProxy(filter, targetClass, DEFAULT_TIMEOUT);
    }

    @Override
    public Filter makeFilterForClass(Class<?> clazz) {
        return makeFilterForClass(clazz.getName());
    }

    @Override
    public Filter makeFilterForClass(String className) {
        try {
            return FrameworkUtil.createFilter(String.format("(%s=%s)", Constants.OBJECTCLASS, className));
        } catch (InvalidSyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public Filter makeFilter(Class<?> clazz, String otherFilter) throws InvalidSyntaxException {
        return makeFilter(clazz.getName(), otherFilter);
    }

    @Override
    public Filter makeFilter(String className, String otherFilter) throws InvalidSyntaxException {
        if (otherFilter == null) {
            return makeFilterForClass(className);
        }
        return FrameworkUtil.createFilter("(&" + makeFilterForClass(className) + otherFilter + ")");
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getServiceForLocation(Class<T> clazz, String location, String context)
        throws OsgiServiceNotAvailableException {
        Filter compiled = getFilterForLocation(clazz, location, context);
        return (T) getService(compiled);
    }

    @Override
    public Filter getFilterForLocation(Class<?> clazz, String location, String context) {
        String filter = makeLocationFilterString(location, context);
        try {
            return makeFilter(clazz, filter);
        } catch (InvalidSyntaxException e) {
            throw new IllegalArgumentException("location is invalid", e);
        }
    }

    @Override
    public Filter getFilterForLocation(Class<?> clazz, String location) {
        return getFilterForLocation(clazz, location, ContextHolder.get().getCurrentContextId());
    }

    @Override
    public Filter getFilterForLocation(String location, String context) {
        String filter = makeLocationFilterString(location, context);
        try {
            return FrameworkUtil.createFilter(filter);
        } catch (InvalidSyntaxException e) {
            throw new IllegalArgumentException("location is invalid", e);
        }
    }

    @Override
    public Filter getFilterForLocation(String location) {
        return getFilterForLocation(location, ContextHolder.get().getCurrentContextId());
    }

    private String makeLocationFilterString(String location, String context) {
        return String.format("(|(location.%s=%s)(location.root=%s))", context, location, location);
    }

    @Override
    public Object getServiceForLocation(String location, String context) throws OsgiServiceNotAvailableException {
        return getService(getFilterForLocation(location, context));
    }

    @Override
    public Object getServiceForLocation(String location) throws OsgiServiceNotAvailableException {
        LOGGER.debug("retrieve service for location: " + location);
        return getService(getFilterForLocation(location));
    }

    @Override
    public <T> T getServiceForLocation(Class<T> clazz, String location) throws OsgiServiceNotAvailableException {
        return getServiceForLocation(clazz, location, ContextHolder.get().getCurrentContextId());
    }

    private Object getServiceFromTracker(ServiceTracker tracker, long timeout)
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

    @Override
    public List<ServiceReference> listServiceReferences(Class<?> clazz) {
        List<ServiceReference> result = new ArrayList<ServiceReference>();
        try {
            CollectionUtils.addAll(result, bundleContext.getServiceReferences(clazz.getName(), null));
        } catch (InvalidSyntaxException e) {
            throw new IllegalArgumentException(e);
        }
        return result;
    }

    @Override
    public <T> List<T> listServices(Class<T> clazz) {
        ServiceTracker tracker = new ServiceTracker(bundleContext, clazz.getName(), null);
        return getListFromTracker(tracker);
    }

    private <T> List<T> getListFromTracker(ServiceTracker tracker) {
        tracker.open();
        Object[] services = tracker.getServices();
        List<T> result = new ArrayList<T>();
        if (services != null) {
            CollectionUtils.addAll(result, services);
        }
        return result;
    }

    @Override
    public <T> List<T> listServices(Class<T> clazz, String filterString) throws InvalidSyntaxException {
        Filter filter = makeFilter(clazz, filterString);
        ServiceTracker tracker = new ServiceTracker(bundleContext, filter, null);
        return getListFromTracker(tracker);

    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getService(final Class<T> clazz, final ServiceReference reference) {
        ServiceTracker tracker = new ServiceTracker(bundleContext, reference, null);
        tracker.open();
        Object newProxyInstance = Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[] { clazz },
            new ServiceTrackerInvocationHandler(tracker));
        return (T) newProxyInstance;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;

    }

}
