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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;

import org.junit.Test;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;

public class BundleContextMockTest extends AbstractOsgiMockServiceTest {

    @Test
    public void testRegisterAndRetrieveService_shouldWork() throws Exception {
        mockService(Collection.class, "foo");
        // bundleContext.registerService(Collection.class.getName(), new HashSet<Object>(),
        // new Hashtable<String, Object>());
        ServiceReference<?>[] serviceReferences2 = bundleContext.getServiceReferences(Collection.class.getName(), null);
        assertThat(serviceReferences2, not(nullValue()));
        assertThat(serviceReferences2.length, is(1));
    }

    @Test
    public void testCreateServiceTrackerAndCreateService_shouldBeInTracker() throws Exception {
        ServiceTracker serviceTracker = new ServiceTracker(bundleContext, Collection.class.getName(), null);
        serviceTracker.open();
        mockService(Collection.class, "foo");
        assertNotNull(serviceTracker.getService());
    }

    @Test
    public void testCreateServiceTrackerAndUnregisterService_shouldNotBeInTracker() throws Exception {
        ServiceRegistration<?> serviceRegistration =
            bundleContext.registerService(Collection.class.getName(), new HashSet<Object>(),
                new Hashtable<String, Object>());
        ServiceTracker serviceTracker = new ServiceTracker(bundleContext, Collection.class.getName(), null);
        serviceTracker.open();
        serviceRegistration.unregister();
        assertNull(serviceTracker.getService());
    }
}
