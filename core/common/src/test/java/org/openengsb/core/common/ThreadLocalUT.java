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

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.openengsb.core.common.context.ContextHolder;
import org.openengsb.core.common.util.ThreadLocalUtil;

public class ThreadLocalUT {

    private String[] arr1s = new String[1000];
    private String[] arr2s = new String[1000];

    @Test
    public void threadPoolPerformance() throws Exception {
        generateStrings();
        Runnable command = new Runnable() {
            @Override
            public void run() {
                ContextHolder.get().setCurrentContextId("0");
                for (int i = 0; i < 1000; i++) {
                    StringUtils.getLevenshteinDistance(arr1s[i], arr2s[i]);
                }
            }
        };

        Runnable command2 = new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 1000; i++) {
                    StringUtils.getLevenshteinDistance(arr1s[i], arr2s[i]);
                }
            }
        };

        ExecutorService pool = Executors.newFixedThreadPool(10);
        long start = executeTasks(command, pool);
        System.out.println(System.currentTimeMillis() - start);
        pool = ThreadLocalUtil.contextAwareExecutor(pool);
        start = executeTasks(command2, pool);
        System.out.println(System.currentTimeMillis() - start);
    }

    private void generateStrings() {
        for (int i = 0; i < 1000; i++) {
            arr1s[i] = UUID.randomUUID().toString();
            arr2s[i] = UUID.randomUUID().toString();
        }
    }

    private long executeTasks(Runnable command, ExecutorService pool) throws InterruptedException, ExecutionException {
        Collection<Future<?>> futures = new HashSet<Future<?>>();
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            futures.add(pool.submit(command));
        }
        for (Future<?> f : futures) {
            f.get();
        }
        return start;
    }
}
