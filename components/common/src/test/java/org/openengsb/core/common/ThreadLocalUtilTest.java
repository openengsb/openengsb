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

package org.openengsb.core.common;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.api.context.ContextHolder;
import org.openengsb.core.common.util.ThreadLocalUtil;

public class ThreadLocalUtilTest {

    private ExecutorService pool;
    private AtomicReference<String> referenceContext = new AtomicReference<String>();

    @Before
    public void setup() throws Exception {
        ContextHolder.get().setCurrentContextId("0");
        referenceContext.set(ContextHolder.get().getCurrentContextId());
        /*
         * using a single thread ensures that the same thread is used for all tasks
         */
        ExecutorService pool = Executors.newSingleThreadExecutor();
        this.pool = ThreadLocalUtil.contextAwareExecutor(pool);
    }

    @Test
    public void testExecute2CallablesInSingleThread_shouldReturnCorrectContext() throws Exception {
        Callable<Boolean> command = new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return ContextHolder.get().getCurrentContextId() == referenceContext.get();
            }
        };

        assertThat(pool.submit(command).get(), is(true));

        ContextHolder.get().setCurrentContextId("1");
        referenceContext.set("1");

        assertThat(pool.submit(command).get(), is(true));
    }

    @Test
    public void testExecute2RunnablesWithDifferentContexts_shouldBeExecutedInCorrectContext() throws Exception {
        /*
         * use this variable to indicate successful executions, because Runnables do not have return values
         */
        final AtomicBoolean success = new AtomicBoolean(false);
        Runnable command = new Runnable() {
            @Override
            public void run() {
                if (ContextHolder.get().getCurrentContextId() == referenceContext.get()) {
                    success.set(true);
                } else {
                    success.set(false);
                }
            }
        };
        pool.submit(command).get();

        ContextHolder.get().setCurrentContextId("1");
        referenceContext.set("1");

        /* this command is run in the same thread, but should run in the current context */
        pool.submit(command).get();
        assertThat(success.get(), is(true));
    }

    @Test
    public void testRunTasksWithInvokeAll_shouldBeExecutedInCorrectContext() throws Exception {
        final AtomicInteger successfulExecutions = new AtomicInteger(0);
        Runnable command = new Runnable() {
            @Override
            public void run() {
                if (ContextHolder.get().getCurrentContextId() == referenceContext.get()) {
                    successfulExecutions.incrementAndGet();
                }
            }
        };
        pool.submit(command).get();

        ContextHolder.get().setCurrentContextId("1");
        referenceContext.set("1");

        Collection<Callable<Object>> commands = new ArrayList<Callable<Object>>();
        commands.add(Executors.callable(command));
        commands.add(Executors.callable(command));
        List<Future<Object>> invokeAll = pool.invokeAll(commands);
        for (Future<Object> f : invokeAll) {
            f.get();
        }

        assertThat(successfulExecutions.get(), is(3));
    }

    @Test
    public void testCallableException() throws Exception {
        Callable<Boolean> command = new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                throw new RuntimeException("test");
            }
        };
        try {
            pool.submit(command).get();
        } catch (ExecutionException e) {
            assertThat(e.getCause().getMessage(), is("test"));
        }
    }
}
