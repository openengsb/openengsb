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

package org.openengsb.core.common.internal;

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
import org.openengsb.core.common.context.Context;
import org.openengsb.core.common.context.ContextConnectorService;
import org.openengsb.core.common.context.ContextCurrentService;
import org.openengsb.core.common.context.ContextStorageBean;
import org.openengsb.core.common.persistence.PersistenceException;
import org.openengsb.core.common.persistence.PersistenceManager;
import org.openengsb.core.common.persistence.PersistenceService;
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
        cs.setThreadLocalContext("a");
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
        assertThat(cs.getThreadLocalContext(), is("a"));
        cs.createContext("threadLocal");
        cs.setThreadLocalContext("threadLocal");
        assertThat("threadLocal", is(cs.getThreadLocalContext()));
    }

    @Test(timeout = 5000)
    public void contextIsLocalToCurrentThread() throws Exception {
        addTestData();
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
        cs.setThreadLocalContext("x");
        assertEquals("x", cs.getThreadLocalContext());
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
    public void createAndRemoveContext_shouldBeAbleToRecreate() throws Exception {
        addTestData();
        cs.createContext("foobar");
        cs.deleteContext("foobar");
        cs.createContext("foobar");
    }

    @Test
    public void getDefaultConnector() throws Exception {
        addTestData();
        ((ContextConnectorService) cs).registerDefaultConnector("mydomain", "myservice");
        String defaultConnectorServiceId = ((ContextConnectorService) cs).getDefaultConnectorServiceId("mydomain");
        assertThat(defaultConnectorServiceId, is("myservice"));
    }
}
