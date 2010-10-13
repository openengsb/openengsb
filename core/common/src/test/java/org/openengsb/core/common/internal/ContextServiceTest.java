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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.concurrent.CountDownLatch;

import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.common.context.Context;
import org.openengsb.core.common.context.ContextCurrentService;

public class ContextServiceTest {

    private ContextCurrentService cs;

    @Before
    public void setup() {
        ContextServiceImpl cs = new ContextServiceImpl();
        this.cs = cs;
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
    public void getEmptyAvailableContexts() {
        ContextServiceImpl contextService = new ContextServiceImpl();
        assertThat(contextService.getAvailableContexts().size(), is(0));
    }

    @Test
    public void getSingleAvailableContexts() {
        assertThat(cs.getAvailableContexts().size(), is(1));
        assertThat(cs.getAvailableContexts().get(0), is("a"));
    }

    @Test
    public void getAvailableContextsWithCreate() {
        cs.createContext("temp");
        assertThat(cs.getAvailableContexts().contains("a"), is(true));
        assertThat(cs.getAvailableContexts().contains("temp"), is(true));
        assertThat(cs.getAvailableContexts().size(), is(2));
    }

    @Test
    public void getCurrentThreadContext() {
        assertThat(cs.getThreadLocalContext(), is("a"));
        cs.createContext("threadLocal");
        cs.setThreadLocalContext("threadLocal");
        assertThat("threadLocal", is(cs.getThreadLocalContext()));
    }

    @Test(timeout = 5000)
    public void contextIsLocalToCurrentThread() throws InterruptedException {
        cs.createContext("threadLocal");
        cs.setThreadLocalContext("threadLocal");
        assertThat(cs.getContext(), notNullValue());
        final CountDownLatch latch = new CountDownLatch(1);
        Thread th = new Thread(new Runnable() {
            @Override
            public void run() {
                if (cs.getContext() == null) {
                    latch.countDown();
                }
            }
        });
        th.start();
        latch.await();
    }

    @Test
    public void pathIsRootSlash_shouldReturnRootContext() {
        assertThat(cs.getContext("/").get("a"), is("a"));
    }

    @Test
    public void pathWithRootSlash_shouldResolveFromRoot() {
        assertThat(cs.getContext("/child").get("b"), is("b"));
    }

    @Test
    public void pathWithoutRootSlash_shouldResolveFromRoot() {
        assertThat(cs.getContext("child").get("b"), is("b"));
        assertThat(cs.getContext("child/").get("b"), is("b"));
    }

    @Test
    public void pathWithMultipleLevels_shouldResolveToLevel() {
        assertThat(cs.getContext("/child/child2").get("c"), is("c"));
    }

    @Test
    public void putValueWithDeepPath_shouldSetRightChildValue() {
        cs.putValue("/child/child2/new", "new");
        assertThat(cs.getContext().getChild("child").getChild("child2").get("new"), is("new"));
    }

    @Test
    public void getValueWithDeepPath_shouldReturnRightChildValue() {
        assertThat(cs.getValue("/child/child2/c"), is("c"));
    }

    @Test
    public void getValueWithNonExistingPath_shouldReturnNull() {
        assertThat(cs.getValue("/non-existing/path"), nullValue());
    }

    @Test
    public void putValueWithNonExistingInnerPath_shouldCreatePathAndPutValue() {
        cs.putValue("/non-existing/path/and/key", "a");
        assertThat(cs.getValue("/non-existing/path/and/key"), is("a"));
    }

    @Test
    public void testGetCurrentContext() throws Exception {
        assertEquals("a", cs.getCurrentContextId());
    }

    @Test
    public void testChangeCurrentContext() throws Exception {
        cs.createContext("x");
        cs.setThreadLocalContext("x");
        assertEquals("x", cs.getCurrentContextId());
    }
}
