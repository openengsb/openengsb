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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class ThreadLocalUT {

    private ThreadLocal<String> local = new InheritableThreadLocal<String>();
    private ExecutorService pool = Executors.newSingleThreadExecutor();

    @Before
    public void setUp() throws Exception {
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
    }

    @Test
    public void testPools() throws Exception {
        local.set("a");
        Authentication authentication = mock(Authentication.class);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        Callable<String> localCall = new Callable<String>() {
            @Override
            public String call() throws Exception {
                System.out.println(Thread.currentThread().getId());
                return local.get();
            }
        };
        Callable<Authentication> contextCall = new Callable<Authentication>() {
            @Override
            public Authentication call() throws Exception {
                System.out.println(Thread.currentThread().getId());
                return SecurityContextHolder.getContext().getAuthentication();
            }
        };
        Future<String> local1 = pool.submit(localCall);
        Future<Authentication> auth1 = pool.submit(contextCall);

        assertThat(local1.get(), is(local.get()));
        assertThat(auth1.get(), is(SecurityContextHolder.getContext().getAuthentication()));

        local.set("a");
        authentication = mock(Authentication.class);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        Future<String> local2 = pool.submit(localCall);
        Future<Authentication> auth2 = pool.submit(contextCall);

        assertThat(local2.get(), is(local.get()));
        assertThat(auth2.get(), is(SecurityContextHolder.getContext().getAuthentication()));
    }

}
