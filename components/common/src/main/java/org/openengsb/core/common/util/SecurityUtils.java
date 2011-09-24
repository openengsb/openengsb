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
package org.openengsb.core.common.util;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.openengsb.core.api.security.BundleAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private static ExecutorService executor = Executors.newCachedThreadPool();

    static class RootCallable<V> extends ContextAwareCallable<V> {
        public RootCallable(Callable<V> original) {
            super(original);
        }

        @Override
        public V call() throws Exception {
            SecurityContextHolder.getContext().setAuthentication(new BundleAuthenticationToken("", ""));
            try {
                return super.call();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                throw e;
            } finally {
                SecurityContextHolder.clearContext();
            }
        }
    }

    static class RootRunnable extends ContextAwareRunnable {

        public RootRunnable(Runnable original) {
            super(original);
        }

        @Override
        public void run() {
            SecurityContextHolder.getContext().setAuthentication(new BundleAuthenticationToken("", ""));
            try {
                super.run();
            } finally {
                SecurityContextHolder.getContext().setAuthentication(null);
            }
        }

    }

    public static <V> V executeWithSystemPermissions(Callable<V> task) throws ExecutionException {
        Future<V> future = executor.submit(new RootCallable<V>(task));
        try {
            return future.get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void executeWithSystemPermissions(Runnable task) {
        executor.execute(new RootRunnable(task));
    }
    
    private SecurityUtils() {
    }
}
