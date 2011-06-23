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
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.api.context.Context;
import org.openengsb.core.api.context.ContextConnectorService;
import org.openengsb.core.api.context.ContextCurrentService;
import org.openengsb.core.api.context.ContextHolder;
import org.openengsb.core.api.context.ContextStorageBean;
import org.openengsb.core.api.persistence.PersistenceException;
import org.openengsb.core.api.persistence.PersistenceManager;
import org.openengsb.core.api.persistence.PersistenceService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

public class ContextServiceTest {

    private ContextCurrentService cs;

    private PersistenceService persistence;

    @Before
    public void setup() {
        ContextServiceImpl cs = new ContextServiceImpl();
        cs.setBundleContext(mock(BundleContext.class));
        persistence = mock(PersistenceService.class);
        PersistenceManager persistenceManager = mock(PersistenceManager.class);
        when(persistenceManager.getPersistenceForBundle(any(Bundle.class))).thenReturn(persistence);
        cs.setPersistenceManager(persistenceManager);
        cs.init();
        this.cs = cs;
    }

    private void addTestData() {
        List<ContextStorageBean> result = new ArrayList<ContextStorageBean>();
        result.add(new ContextStorageBean(null));
        when(persistence.query(any(ContextStorageBean.class))).thenReturn(result);
        cs.createContext("a");
        ContextHolder.get().setCurrentContextId("a");
        Context c = cs.getContext();
        c.put("a", "a");
        Context child = c.createChild("child");
        child.put("b", "b");
        Context child2 = child.createChild("child2");
        child2.put("c", "c");
    }

    @Test
    public void testGetContext() throws Exception {
        cs.createContext("a");
        cs.createContext("b");
        ContextHolder.get().setCurrentContextId("a");
        Callable<String> callable = new Callable<String>() {
            @Override
            public String call() throws Exception {
                return ContextHolder.get().getCurrentContextId();
            }
        };
        ExecutorService pool = Executors.newSingleThreadExecutor();
        Future<String> otherThreadContext = pool.submit(callable);
        assertThat(otherThreadContext.get(), is("a"));
    }

    @Test
    public void testContextIsCreated_shouldWork() throws PersistenceException {
        verify(persistence).create(any(ContextStorageBean.class));
    }

    @Test
    public void getEmptyAvailableContexts() {
        assertThat(cs.getAvailableContexts().size(), is(0));
    }

    @Test
    public void getSingleAvailableContexts() {
        addTestData();
        assertThat(cs.getAvailableContexts().size(), is(1));
        assertThat(cs.getAvailableContexts().get(0), is("a"));
    }

    @Test
    public void getAvailableContextsWithCreate() throws PersistenceException {
        addTestData();
        cs.createContext("temp");
        verify(persistence, atLeast(1)).update(any(ContextStorageBean.class), any(ContextStorageBean.class));
        assertThat(cs.getAvailableContexts().contains("a"), is(true));
        assertThat(cs.getAvailableContexts().contains("temp"), is(true));
        assertThat(cs.getAvailableContexts().size(), is(2));
    }

    @Test
    public void getCurrentThreadContext() {
        addTestData();
        assertThat(ContextHolder.get().getCurrentContextId(), is("a"));
        cs.createContext("threadLocal");
        ContextHolder.get().setCurrentContextId("threadLocal");
        assertThat("threadLocal", is(ContextHolder.get().getCurrentContextId()));
    }

    @Test(timeout = 5000)
    public void contextIsLocalToCurrentThread() throws Exception {
        addTestData();
        cs.createContext("threadLocal");
        assertThat(cs.getContext(), notNullValue());
        Thread task1 = new Thread() {
            @Override
            public void run() {
                ContextHolder.get().setCurrentContextId("threadLocal");
            }
        };
        task1.start();
        task1.join();
        ExecutorService pool = Executors.newSingleThreadExecutor();
        Callable<String> otherTask = new Callable<String>() {
            @Override
            public String call() throws Exception {
                return ContextHolder.get().getCurrentContextId();
            }
        };

        Future<String> otherThreadContext = pool.submit(otherTask);
        assertThat(otherThreadContext.get(), not("threadLocal"));
    }

    @Test
    public void pathIsRootSlash_shouldReturnRootContext() {
        addTestData();
        assertThat(cs.getContext("/").get("a"), is("a"));
    }

    @Test
    public void pathWithRootSlash_shouldResolveFromRoot() {
        addTestData();
        assertThat(cs.getContext("/child").get("b"), is("b"));
    }

    @Test
    public void pathWithoutRootSlash_shouldResolveFromRoot() {
        addTestData();
        assertThat(cs.getContext("child").get("b"), is("b"));
        assertThat(cs.getContext("child/").get("b"), is("b"));
    }

    @Test
    public void pathWithMultipleLevels_shouldResolveToLevel() {
        addTestData();
        assertThat(cs.getContext("/child/child2").get("c"), is("c"));
    }

    @Test
    public void putValueWithDeepPath_shouldSetRightChildValue() {
        addTestData();
        cs.putValue("/child/child2/new", "new");
        assertThat(cs.getContext().getChild("child").getChild("child2").get("new"), is("new"));
    }

    @Test
    public void getValueWithDeepPath_shouldReturnRightChildValue() {
        addTestData();
        assertThat(cs.getValue("/child/child2/c"), is("c"));
    }

    @Test
    public void getValueWithNonExistingPath_shouldReturnNull() {
        addTestData();
        assertThat(cs.getValue("/non-existing/path"), nullValue());
    }

    @Test
    public void putValueWithNonExistingInnerPath_shouldCreatePathAndPutValue() throws PersistenceException {
        addTestData();
        cs.putValue("/non-existing/path/and/key", "a");
        verify(persistence, atLeast(1)).update(any(ContextStorageBean.class), any(ContextStorageBean.class));
        assertThat(cs.getValue("/non-existing/path/and/key"), is("a"));
    }

    @Test
    public void testChangeCurrentContext() throws Exception {
        addTestData();
        cs.createContext("x");
        ContextHolder.get().setCurrentContextId("x");
        assertEquals("x", ContextHolder.get().getCurrentContextId());
    }

    @Test
    public void testRegisterDefaultConnector() throws Exception {
        addTestData();
        ((ContextConnectorService) cs).registerDefaultConnector("mydomain", "myservice");
        /*
         * Forwardhandler expects the value at this path
         */
        String value = cs.getValue("/domain/mydomain/defaultConnector/id");
        assertThat(value, is("myservice"));
    }

    @Test
    public void getDefaultConnector() throws Exception {
        addTestData();
        ((ContextConnectorService) cs).registerDefaultConnector("mydomain", "myservice");
        String defaultConnectorServiceId = ((ContextConnectorService) cs).getDefaultConnectorServiceId("mydomain");
        assertThat(defaultConnectorServiceId, is("myservice"));
    }
}
