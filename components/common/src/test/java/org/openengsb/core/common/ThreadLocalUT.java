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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Test;
import org.openengsb.core.api.context.ContextHolder;
import org.openengsb.core.common.util.ThreadLocalUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadLocalUT {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadLocalUT.class);

    /**
     * shows the impact of the proxying and argument-manipulation on performance.
     */
    @Test
    public void testThreadPoolPerformance_printsPerformanceValues() throws Exception {
        Runnable command = new Runnable() {
            @Override
            public void run() {
                ContextHolder.get().setCurrentContextId("0");
            }
        };

        Runnable command2 = new Runnable() {
            @Override
            public void run() {
            }
        };

        ExecutorService regularPool = Executors.newFixedThreadPool(10);
        long start = executeTasks(command, regularPool);
        LOGGER.info("" + (System.currentTimeMillis() - start));
        ExecutorService contextAwareExecutor = ThreadLocalUtil.contextAwareExecutor(regularPool);
        start = executeTasks(command2, contextAwareExecutor);
        LOGGER.info("" + (System.currentTimeMillis() - start));
    }

    private long executeTasks(Runnable command, ExecutorService pool) throws Exception {
        Collection<Future<?>> futures = new HashSet<Future<?>>();
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            futures.add(pool.submit(command));
        }
        for (Future<?> f : futures) {
            f.get();
        }
        return start;
    }
}
