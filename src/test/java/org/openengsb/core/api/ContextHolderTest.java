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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import org.junit.Test;
import org.openengsb.core.api.context.ContextHolder;

public class ContextHolderTest {

    @Test
    public void testContextHolderHasInitialContext_contextShouldBeNotNull() throws Exception {
        ContextHolder context = ContextHolder.get();
        assertThat(context, notNullValue());
    }

    @Test
    public void testGetContext_shouldReturnCurrentContext() throws Exception {
        ContextHolder context = ContextHolder.get();
        context.setCurrentContextId("foo");
        String contextId = context.getCurrentContextId();
        assertThat(contextId, is("foo"));
    }

    @Test
    public void testContextIsNotModifiedByChildThread_contextShouldBeUnchanged() throws Exception {
        ContextHolder context = ContextHolder.get();
        context.setCurrentContextId("foo");
        Callable<String> task = new Callable<String>() {
            @Override
            public String call() throws Exception {
                ContextHolder.get().setCurrentContextId("bar");
                return ContextHolder.get().getCurrentContextId();
            }
        };
        String result = Executors.newSingleThreadExecutor().submit(task).get();
        assertThat(result, is("bar"));
        assertThat(context.getCurrentContextId(), is("foo"));
    }

    @Test
    public void testContextIsAccessibleFromChildThread_shouldGetUnchangedContext() throws Exception {
        ContextHolder context = ContextHolder.get();
        context.setCurrentContextId("foo");
        Callable<String> task = new Callable<String>() {
            @Override
            public String call() throws Exception {
                return ContextHolder.get().getCurrentContextId();
            }
        };
        String result = Executors.newSingleThreadExecutor().submit(task).get();
        assertThat(result, is("foo"));
    }

}
