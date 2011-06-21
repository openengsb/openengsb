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

package org.openengsb.core.services.internal;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.api.Constants;
import org.openengsb.core.api.OsgiUtilsService;
import org.openengsb.core.api.context.ContextCurrentService;
import org.openengsb.core.api.model.ContextConfiguration;
import org.openengsb.core.api.persistence.ConfigPersistenceService;
import org.openengsb.core.api.persistence.PersistenceException;
import org.openengsb.core.common.OpenEngSBCoreServices;
import org.openengsb.core.common.util.DefaultOsgiUtilsService;
import org.openengsb.core.test.AbstractOsgiMockServiceTest;
import org.openengsb.core.test.DummyPersistenceManager;
import org.osgi.framework.BundleContext;

public class ContextServiceTest extends AbstractOsgiMockServiceTest {

    private DefaultOsgiUtilsService serviceUtils;
    private ContextCurrentService cs;

    @Before
    public void setup() {
        registerConfigPersistence();
        ContextServiceImpl cs = new ContextServiceImpl();
        this.cs = cs;
    }

    private void registerConfigPersistence() {
        final CorePersistenceServiceBackend<?> persistenceBackend = new CorePersistenceServiceBackend<Object>();
        DummyPersistenceManager persistenceManager = new DummyPersistenceManager();
        persistenceBackend.setPersistenceManager(persistenceManager);
        persistenceBackend.setBundleContext(bundleContext);
        persistenceBackend.init();
        Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put(Constants.CONFIGURATION_ID, ContextConfiguration.TYPE_ID);
        props.put(Constants.BACKEND_ID, "dummy");
        registerService(new DefaultConfigPersistenceService(persistenceBackend), props, ConfigPersistenceService.class);
    }

    private void createTestContextA() {
        cs.createContext("a");
        cs.setThreadLocalContext("a");
    }

    @Test
    public void testGetContext() throws Exception {
        cs.createContext("a");
        cs.createContext("b");
        cs.setThreadLocalContext("a");
        Callable<String> callable = new Callable<String>() {
            @Override
            public String call() throws Exception {
                return cs.getThreadLocalContext();
            }
        };
        ExecutorService pool = Executors.newSingleThreadExecutor();
        Future<String> otherThreadContext = pool.submit(callable);
        assertThat(otherThreadContext.get(), is("a"));
    }

    @Test
    public void getEmptyAvailableContexts() {
        assertThat(cs.getAvailableContexts().size(), is(0));
    }

    @Test
    public void getSingleAvailableContexts() {
        createTestContextA();
        assertThat(cs.getAvailableContexts().size(), is(1));
        assertThat(cs.getAvailableContexts().get(0), is("a"));
    }

    @Test
    public void getAvailableContextsWithCreate() throws PersistenceException {
        createTestContextA();
        cs.createContext("temp");
        assertThat(cs.getAvailableContexts().contains("a"), is(true));
        assertThat(cs.getAvailableContexts().contains("temp"), is(true));
        assertThat(cs.getAvailableContexts().size(), is(2));
    }

    @Test
    public void getCurrentThreadContext() {
        createTestContextA();
        assertThat(cs.getThreadLocalContext(), is("a"));
        cs.createContext("threadLocal");
        cs.setThreadLocalContext("threadLocal");
        assertThat("threadLocal", is(cs.getThreadLocalContext()));
    }

    @Test(timeout = 5000)
    public void contextIsLocalToCurrentThread() throws Exception {
        createTestContextA();
        cs.createContext("threadLocal");
        assertThat(cs.getContext(), notNullValue());
        Thread task1 = new Thread() {
            @Override
            public void run() {
                cs.setThreadLocalContext("threadLocal");
            }
        };
        task1.start();
        task1.join();
        ExecutorService pool = Executors.newSingleThreadExecutor();
        Callable<String> otherTask = new Callable<String>() {
            @Override
            public String call() throws Exception {
                return cs.getThreadLocalContext();
            }
        };

        Future<String> otherThreadContext = pool.submit(otherTask);
        assertThat(otherThreadContext.get(), not("threadLocal"));
    }

    @Test
    public void testChangeCurrentContext() throws Exception {
        createTestContextA();
        cs.createContext("x");
        cs.setThreadLocalContext("x");
        assertEquals("x", cs.getThreadLocalContext());
    }

    @Override
    protected void setBundleContext(BundleContext bundleContext) {
        serviceUtils = new DefaultOsgiUtilsService();
        serviceUtils.setBundleContext(bundleContext);
        OpenEngSBCoreServices.setOsgiServiceUtils(serviceUtils);
        registerService(serviceUtils, new Hashtable<String, Object>(), OsgiUtilsService.class);
    }
}
