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
package org.openengsb.core.test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.lang.ArrayUtils;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

public final class ServiceReferenceList<ServiceType> implements List<ServiceReference<ServiceType>> {
    private ServiceTracker tracker;

    public ServiceReferenceList(ServiceTracker tracker) {
        this.tracker = tracker;
        tracker.open();
    }

    @Override
    public int size() {
        return tracker.size();
    }

    @Override
    public boolean isEmpty() {
        return tracker.size() == 0;
    }

    @Override
    public boolean contains(Object o) {
        return ArrayUtils.contains(tracker.getServices(), o);
    }

    @Override
    public Iterator<ServiceReference<ServiceType>> iterator() {
        return asList().iterator();
    }

    @Override
    public Object[] toArray() {
        if (tracker.getServices() == null) {
            return new Object[0];
        }
        return tracker.getServices();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return asList().toArray(a);
    }

    @Override
    public boolean add(ServiceReference<ServiceType> e) {
        throw new UnsupportedOperationException("modifying the service-list is not allowed");
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("modifying the service-list is not allowed");
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return asList().containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends ServiceReference<ServiceType>> c) {
        throw new UnsupportedOperationException("modifying the service-list is not allowed");
    }

    @Override
    public boolean addAll(int index, Collection<? extends ServiceReference<ServiceType>> c) {
        throw new UnsupportedOperationException("modifying the service-list is not allowed");
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("modifying the service-list is not allowed");
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("modifying the service-list is not allowed");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("modifying the service-list is not allowed");
    }

    @SuppressWarnings("unchecked")
    @Override
    public ServiceReference<ServiceType> get(int index) {
        return tracker.getServiceReferences()[index];
    }

    @Override
    public ServiceReference<ServiceType> set(int index, ServiceReference<ServiceType> element) {
        throw new UnsupportedOperationException("modifying the service-list is not allowed");
    }

    @Override
    public void add(int index, ServiceReference<ServiceType> element) {
        throw new UnsupportedOperationException("modifying the service-list is not allowed");
    }

    @Override
    public ServiceReference<ServiceType> remove(int index) {
        throw new UnsupportedOperationException("modifying the service-list is not allowed");
    }

    @Override
    public int indexOf(Object o) {
        return ArrayUtils.indexOf(tracker.getServices(), o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return ArrayUtils.lastIndexOf(tracker.getServices(), o);
    }

    @Override
    public ListIterator<ServiceReference<ServiceType>> listIterator() {
        return asList().listIterator();
    }

    @Override
    public ListIterator<ServiceReference<ServiceType>> listIterator(int index) {
        return asList().listIterator(index);
    }

    @Override
    public List<ServiceReference<ServiceType>> subList(int fromIndex, int toIndex) {
        return asList().subList(fromIndex, toIndex);
    }

    private List<ServiceReference<ServiceType>> asList() {
        @SuppressWarnings("unchecked")
        ServiceReference<ServiceType>[] serviceReferences = tracker.getServiceReferences();
        if (serviceReferences == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(serviceReferences);
    }
}
