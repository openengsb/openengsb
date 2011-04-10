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

package org.openengsb.core.api;

import java.util.List;

import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

public interface OsgiUtilsService {

    List<ServiceReference> listServiceReferences(Class<?> clazz);

    <T> List<T> listServices(Class<T> clazz);

    /**
     * retrieves the highest ranked service exporting the given interface.
     *
     * @throws OsgiServiceNotAvailableException when the service is not available after 30 seconds
     */
    <T> T getService(Class<T> clazz) throws OsgiServiceNotAvailableException;

    /**
     * retrieves the highest ranked service exporting the given interface.
     *
     * @throws OsgiServiceNotAvailableException when the service is not available after the given timeout
     */
    <T> T getService(Class<T> clazz, long timeout) throws OsgiServiceNotAvailableException;

    /**
     * retrieve the highest ranked service that matches the given filter
     *
     * @throws OsgiServiceNotAvailableException when the service is not available after 30 seconds
     */
    Object getService(Filter filter) throws OsgiServiceNotAvailableException;

    /**
     * retrieve the highest ranked service that matches the given filter
     *
     * @throws OsgiServiceNotAvailableException when the service is not available after the given timeout
     */
    Object getService(Filter filter, long timeout) throws OsgiServiceNotAvailableException;

    /**
     * retrieve the highest ranked service that matches the given filter
     *
     * @throws OsgiServiceNotAvailableException when the service is not available after 30 seconds
     */
    Object getService(String filterString) throws OsgiServiceNotAvailableException;

    /**
     * retrieve the highest ranked service that matches the given filter
     *
     * @throws OsgiServiceNotAvailableException when the service is not available after the given timeout
     */
    Object getService(String filterString, long timeout) throws OsgiServiceNotAvailableException;

    /**
     * retrieves the highest ranked service that exports the given interface and the has the given instanceid
     *
     * @throws OsgiServiceNotAvailableException when the service is not available after 30 seconds
     */
    <T> T getServiceWithId(Class<? extends T> clazz, String id) throws OsgiServiceNotAvailableException;

    /**
     * retrieves the highest ranked service that exports the given interface and the has the given instanceid
     *
     * @throws OsgiServiceNotAvailableException when the service is not available after the given timeout
     */
    <T> T getServiceWithId(Class<? extends T> clazz, String id, long timeout) throws OsgiServiceNotAvailableException;

    /**
     * retrieves the highest ranked service that exports the given interface and the has the given instanceid
     *
     * @throws OsgiServiceNotAvailableException when the service is not available after 30 seconds
     */
    Object getServiceWithId(String className, String id) throws OsgiServiceNotAvailableException;

    /**
     * retrieves the highest ranked service that exports the given interface and the has the given instanceid
     *
     * @throws OsgiServiceNotAvailableException when the service is not available after the given timeout
     */
    Object getServiceWithId(String className, String id, long timeout) throws OsgiServiceNotAvailableException;

    /**
     * returns a proxy that looks up an OSGi-service with the given Filter as soon as a method is called. Note that the
     * returned proxy may throw an {@link OsgiServiceNotAvailableException} if the service is not found within 30
     * seconds
     */
    <T> T getOsgiServiceProxy(final Filter filter, Class<T> targetClass);

    /**
     * returns a proxy that looks up an OSGi-service with the given Filter as soon as a method is called. Note that the
     * returned proxy may throw an {@link OsgiServiceNotAvailableException} if the service is not found within 30
     * seconds
     */
    <T> T getOsgiServiceProxy(final String filter, Class<T> targetClass);

    /**
     * returns a proxy that looks up an OSGi-service according to the targetClass as soon as a method is called. Note
     * that the returned proxy may throw an {@link OsgiServiceNotAvailableException} if the service is not found within
     * 30 seconds
     */
    <T> T getOsgiServiceProxy(Class<T> targetClass);

    /**
     * returns a proxy that looks up an OSGi-service with the given Filter as soon as a method is called. Note that the
     * returned proxy may throw an {@link OsgiServiceNotAvailableException} if the service is not found within the given
     * timeout (in milliseconds)
     */
    <T> T getOsgiServiceProxy(final Filter filter, Class<T> targetClass, long timeout);

    /**
     * returns a proxy that looks up an OSGi-service with the given Filter as soon as a method is called. Note that the
     * returned proxy may throw an {@link OsgiServiceNotAvailableException} if the service is not found within the given
     * timeout (in milliseconds)
     */
    <T> T getOsgiServiceProxy(final String filter, Class<T> targetClass, long timeout);

    /**
     * returns a proxy that looks up an OSGi-service according to the targetClass as soon as a method is called. Note
     * that the returned proxy may throw an {@link OsgiServiceNotAvailableException} if the service is not found within
     * the given timeout (in milliseconds)
     */
    <T> T getOsgiServiceProxy(Class<T> targetClass, long timeout);

    /**
     * creates a filter that matches all services exporting the class as interface
     */
    Filter makeFilterForClass(Class<?> clazz);

    /**
     * creates a filter that matches all services exporting the class as interface
     */
    Filter makeFilterForClass(String className);

    /**
     *
     * creates a filter that matches all services exporting the class as interface and applies to the other Filter
     */
    Filter makeFilter(Class<?> clazz, String otherFilter) throws InvalidSyntaxException;

    /**
     *
     * creates a filter that matches all services exporting the class as interface and applies to the other Filter
     */
    Filter makeFilter(String className, String otherFilter) throws InvalidSyntaxException;

    /**
     * retrieves a service that has the given location in the given context. If there is no service at this location (in
     * this context), the service at the same location in the root-context is returned
     *
     * @throws OsgiServiceNotAvailableException when the service is not available after 30 seconds
     */
    <T> T getServiceForLocation(Class<T> clazz, String location, String context)
        throws OsgiServiceNotAvailableException;

    /**
     * returns a filter that matches services with the given class and location in both the given context and the
     * root-context
     */
    Filter getFilterForLocation(Class<?> clazz, String location, String context);

    /**
     * returns a filter that matches services with the given class and location in both the current context and the
     * root-context
     */
    Filter getFilterForLocation(Class<?> clazz, String location);

    /**
     * returns a filter that matches services with the given location in both the given context and the root-context
     */
    Filter getFilterForLocation(String location, String context);

    /**
     * returns a filter that matches services with the given location in both the current context and the root-context
     */
    Filter getFilterForLocation(String location);

    /**
     * retrieves a service that has the given location in the given context. If there is no service at this location (in
     * this context), the service at the same location in the root-context is returned
     *
     * @throws OsgiServiceNotAvailableException when the service is not available after 30 secondss
     */
    Object getServiceForLocation(String location, String context) throws OsgiServiceNotAvailableException;

    /**
     * retrieves a service that has the given location in the current context. If there is no service at this location
     * (in this context), the service at the same location in the root-context is returned
     *
     * @throws OsgiServiceNotAvailableException when the service is not available after 30 seconds
     */
    Object getServiceForLocation(String location) throws OsgiServiceNotAvailableException;

    /**
     * retrieves a service that has the given location in the current context. If there is no service at this location
     * (in this context), the service at the same location in the root-context is returned
     *
     * @throws OsgiServiceNotAvailableException when the service is not available after 30 seconds
     */
    <T> T getServiceForLocation(Class<T> clazz, String location) throws OsgiServiceNotAvailableException;

}
