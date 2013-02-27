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

import java.util.Iterator;
import java.util.List;

import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;

public interface OsgiUtilsService {

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
     *
     * @throws IllegalArgumentException if the given filter could not be compiled
     */
    <T> T getOsgiServiceProxy(final String filter, Class<T> targetClass, long timeout) throws IllegalArgumentException;

    /**
     * returns a proxy that looks up an OSGi-service according to the targetClass as soon as a method is called. Note
     * that the returned proxy may throw an {@link OsgiServiceNotAvailableException} if the service is not found within
     * the given timeout (in milliseconds)
     */
    <T> T getOsgiServiceProxy(Class<T> targetClass, long timeout);

    /**
     * retrieves a service that has the given location in the given context. If there is no service at this location (in
     * this context), the service at the same location in the root-context is returned
     *
     * @throws OsgiServiceNotAvailableException when the service is not available after 30 seconds
     * @throws IllegalArgumentException if the location contains special characters that prevent the filter from
     *         compiling
     */
    <T> T getServiceForLocation(Class<T> clazz, String location, String context)
        throws OsgiServiceNotAvailableException, IllegalArgumentException;

    /**
     * retrieves a service that has the given location in the given context. If there is no service at this location (in
     * this context), the service at the same location in the root-context is returned
     *
     * @throws OsgiServiceNotAvailableException when the service is not available after 30 seconds
     * @throws IllegalArgumentException if the location contains special characters that prevent the filter from
     *         compiling
     */
    Object getServiceForLocation(String location, String context) throws OsgiServiceNotAvailableException,
        IllegalArgumentException;

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

    /**
     * list all service-references that are exported with the given interface.
     *
     * NOTE that the returned references may become invalid at any time.
     */
    <T> List<ServiceReference<T>> listServiceReferences(Class<T> clazz);

    /**
     * list all service-references that are exported with the given interface.
     *
     * NOTE that the returned references may become invalid at any time.
     */
    List<ServiceReference<?>> listServiceReferences(String filter);

    /**
     * list all service-references that are exported with the given interface.
     *
     * NOTE that the returned references may become invalid at any time.
     */
    <T> List<ServiceReference<T>> listServiceReferences(Class<T> clazz, String filter);

    /**
     * returns a list of all serivce-objects of services exported with the given interface.
     *
     * NOTE that the returned references may become invalid at any time.
     */
    <T> List<T> listServices(Class<T> clazz);

    /**
     * returns a list of all serivce-objects of services exported with the given interface matching the given filter.
     *
     * NOTE that the returned references may become invalid at any time.
     *
     * @throws IllegalArgumentException if the given filter can not be compiled
     */
    <T> List<T> listServices(Class<T> clazz, String filter) throws IllegalArgumentException;

    /**
     * provides an {@link Iterator} where each service is resolved from the {@link ServiceReference} when needed
     */
    <T> Iterator<T> getServiceIterator(Iterable<ServiceReference<T>> references);

    /**
     * provides an {@link Iterator} where each service is resolved from the {@link ServiceReference} when needed
     */
    <T> Iterator<T> getServiceIterator(Iterable<ServiceReference> references, Class<T> serviceClass);

}
