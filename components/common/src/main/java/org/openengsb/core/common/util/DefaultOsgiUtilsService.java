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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;

public class DefaultOsgiUtilsService implements OsgiUtilsService {

    /**
     * serves as common invocation handler for proxies that resolve osgi-services dynamically. The proxy tries to
     * resolve the service for the given timeout. A timeout of 0 means that the proxy will wait for the service
     * indefinitely {@link ServiceTracker#waitForService(long)} A timeout < 0 means that the service tracker will not
     * wait for the service at all. If the service is not available immediately an
     * {@link OsgiServiceNotAvailableException} is thrown.
     *
     */
    private final class ServiceTrackerInvocationHandler implements InvocationHandler {
        private ServiceTracker tracker;
        private Long timeout = -1L;
        private final String info;

        protected ServiceTrackerInvocationHandler(Filter filter, long timeout) {
            this(filter);
            this.timeout = timeout;
        }

        protected ServiceTrackerInvocationHandler(Filter filter) {
            tracker = new ServiceTracker(bundleContext, filter, null);
            info = filter.toString();
        }

        protected ServiceTrackerInvocationHandler(String className, long timeout) {
            this(className);
            this.timeout = timeout;
        }

        protected ServiceTrackerInvocationHandler(String className) {
            tracker = new ServiceTracker(bundleContext, className, null);
            info = "Class: " + className;
        }

        protected ServiceTrackerInvocationHandler(Class<?> targetClass, long timeout) {
            this(targetClass.getName(), timeout);
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Object service = getService();
            if (service == null) {
                throw new OsgiServiceNotAvailableException("could not resolve service with tracker: " + info);
            }
            try {
                return method.invoke(service, args);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        }

        private synchronized Object getService() throws InterruptedException {
            tracker.open();
            try {
                if (timeout < 0) {
                    return tracker.getService();
                } else {
                    return tracker.waitForService(timeout);
                }
            } finally {
                tracker.close();
            }
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultOsgiUtilsService.class);
    private static final long DEFAULT_TIMEOUT = 30000L;

    private BundleContext bundleContext;

    public DefaultOsgiUtilsService() {
    }

    public DefaultOsgiUtilsService(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    @Override
    public <T> T getService(Class<T> clazz) throws OsgiServiceNotAvailableException {
        return getService(clazz, DEFAULT_TIMEOUT);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getService(Class<T> clazz, long timeout) throws OsgiServiceNotAvailableException {
        ServiceTracker tracker = new ServiceTracker(bundleContext, clazz.getName(), null);
        Object result = waitForServiceFromTracker(tracker, timeout);
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
        LOGGER.debug("getting service for filter {} from tracker", filter);
        Object result = waitForServiceFromTracker(t, timeout);
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
        return getService(createFilter(filterString), timeout);
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
        Filter filter = makeFilter(className, String.format("(%s=%s)", Constants.SERVICE_PID, id));
        return getService(filter, timeout);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getOsgiServiceProxy(final Filter filter, Class<T> targetClass, final long timeout) {
        return (T) Proxy.newProxyInstance(targetClass.getClassLoader(), new Class<?>[]{ targetClass },
            new ServiceTrackerInvocationHandler(filter, timeout));
    }

    @Override
    public <T> T getOsgiServiceProxy(final String filter, Class<T> targetClass, long timeout) {
        return getOsgiServiceProxy(createFilter(filter), targetClass, timeout);
    }

    /**
     * creates a Filter, but wraps the {@link InvalidSyntaxException} into an {@link IllegalArgumentException}
     */
    public static Filter createFilter(String filterString) {
        try {
            return FrameworkUtil.createFilter(filterString);
        } catch (InvalidSyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getOsgiServiceProxy(Class<T> targetClass, long timeout) {
        return (T) Proxy.newProxyInstance(targetClass.getClassLoader(), new Class<?>[]{ targetClass },
            new ServiceTrackerInvocationHandler(targetClass, timeout));
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
    public Filter makeFilter(Class<?> clazz, String otherFilter) throws IllegalArgumentException {
        return makeFilter(clazz.getName(), otherFilter);
    }

    @Override
    public Filter makeFilter(String className, String otherFilter) throws IllegalArgumentException {
        if (otherFilter == null) {
            return makeFilterForClass(className);
        }
        try {
            return FrameworkUtil.createFilter("(&" + makeFilterForClass(className) + otherFilter + ")");
        } catch (InvalidSyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getServiceForLocation(Class<T> clazz, String location, String context)
        throws OsgiServiceNotAvailableException, IllegalArgumentException {
        Filter compiled = getFilterForLocation(clazz, location, context);
        return (T) getService(compiled);
    }

    @Override
    public Filter getFilterForLocation(Class<?> clazz, String location, String context)
        throws IllegalArgumentException {
        String filter = makeLocationFilterString(location, context);
        return makeFilter(clazz, filter);
    }

    @Override
    public Filter getFilterForLocation(Class<?> clazz, String location) throws IllegalArgumentException {
        return getFilterForLocation(clazz, location, ContextHolder.get().getCurrentContextId());
    }

    @Override
    public Filter getFilterForLocation(String location, String context) throws IllegalArgumentException {
        String filter = makeLocationFilterString(location, context);
        try {
            return FrameworkUtil.createFilter(filter);
        } catch (InvalidSyntaxException e) {
            throw new IllegalArgumentException("location is invalid: " + location, e);
        }
    }

    @Override
    public Filter getFilterForLocation(String location) throws IllegalArgumentException {
        return getFilterForLocation(location, ContextHolder.get().getCurrentContextId());
    }

    private String makeLocationFilterString(String location, String context) throws IllegalArgumentException {
        return String.format("(|(location.%s=%s)(location.root=%s))", context, location, location);
    }

    @Override
    public Object getServiceForLocation(String location, String context) throws OsgiServiceNotAvailableException,
        IllegalArgumentException {
        return getService(getFilterForLocation(location, context));
    }

    @Override
    public Object getServiceForLocation(String location) throws OsgiServiceNotAvailableException,
        IllegalArgumentException {
        LOGGER.debug("retrieve service for location: {}", location);
        return getService(getFilterForLocation(location));
    }

    @Override
    public <T> T getServiceForLocation(Class<T> clazz, String location) throws OsgiServiceNotAvailableException,
        IllegalArgumentException {
        return getServiceForLocation(clazz, location, ContextHolder.get().getCurrentContextId());
    }

    /**
     * tries to retrieve the service from the given service-tracker for the amount of milliseconds provided by the given
     * timeout.
     *
     * @throws OsgiServiceNotAvailableException if the service could not be found within the given timeout
     */
    private static Object waitForServiceFromTracker(ServiceTracker tracker, long timeout)
        throws OsgiServiceNotAvailableException {
        synchronized (tracker) {
            tracker.open();
            try {
                return tracker.waitForService(timeout);
            } catch (InterruptedException e) {
                throw new OsgiServiceNotAvailableException(e);
            } finally {
                tracker.close();
            }
        }
    }

    @Override
    public List<ServiceReference> listServiceReferences(Class<?> clazz) {
        return listServiceReferences(clazz, null);
    }

    @Override
    public List<ServiceReference> listServiceReferences(String filter) {
        return listServiceReferences(null, filter);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ServiceReference> listServiceReferences(Class<?> clazz, String filter) {
        List<ServiceReference> result = new ArrayList<ServiceReference>();
        String className = clazz == null ? null : clazz.getName();
        try {
            ServiceReference[] serviceReferences = bundleContext.getServiceReferences(className, filter);
            if (serviceReferences == null) {
                return result;
            }
            CollectionUtils.addAll(result, serviceReferences);
            Collections.sort(result);
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
        tracker.close();
        return result;
    }

    @Override
    public <T> List<T> listServices(Class<T> clazz, String filterString) throws IllegalArgumentException {
        Filter filter = makeFilter(clazz, filterString);
        ServiceTracker tracker = new ServiceTracker(bundleContext, filter, null);
        return getListFromTracker(tracker);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getService(final Class<T> clazz, final ServiceReference reference)
        throws OsgiServiceNotAvailableException {
        return (T) getService(reference);
    }

    @Override
    public Object getService(ServiceReference reference) throws OsgiServiceNotAvailableException {
        Object service = bundleContext.getService(reference);
        if (service == null) {
            throw new OsgiServiceNotAvailableException("service retrieved from the bundlecontext was null");
        }
        return service;
    }

    @Override
    public <T> Iterator<T> getServiceIterator(Iterable<ServiceReference> references, Class<T> serviceClass) {
        return Iterators.transform(references.iterator(), new Function<ServiceReference, T>() {
            @SuppressWarnings("unchecked")
            @Override
            public T apply(ServiceReference input) {
                return (T) bundleContext.getService(input);
            }
        });
    }

    @Override
    public Iterator<Object> getServiceIterator(Iterable<ServiceReference> references) {
        return getServiceIterator(references, Object.class);
    }

    /**
     * Any bundle-context is fine here, since it does not matter from which bundlecontext services are retrieved.
     */
    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

}
