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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import org.openengsb.core.common.context.ContextHolder;

public class ThreadLocalUtil {

    @SuppressWarnings("unchecked")
    private static Object transform(Object arg) {
        if (arg instanceof Runnable && arg.getClass() != ContextAwareRunnable.class) {
            return new ContextAwareRunnable((Runnable) arg);
        } else if (arg instanceof Callable<?>) {
            return new ContextAwareCallable((Callable<Object>) arg);
        }
        return arg;
    }

    public static void transformSingleInvocation(Object[] args) {
        args[0] = transform(args[0]);
    }

    @SuppressWarnings("unchecked")
    public static void transformBatchInvocation(Object[] args) {
        Collection<?> tasks = (Collection<?>) args[0];
        Collection<Object> result;
        try {
            result = tasks.getClass().newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        for (Object o : tasks) {
            result.add(transform(o));
        }
        args[0] = result;
    }

    static class ContextAwareRunnable implements Runnable {
        private Runnable original;
        private String context = ContextHolder.get().getCurrentContextId();

        public ContextAwareRunnable(Runnable original) {
            this.original = original;
        }

        @Override
        public void run() {
            ContextHolder.get().setCurrentContextId(context);
            original.run();
        }
    }

    static class ContextAwareCallable implements Callable<Object> {
        private Callable<Object> original;
        private String context = ContextHolder.get().getCurrentContextId();

        public ContextAwareCallable(Callable<Object> original) {
            this.original = original;
        }

        @Override
        public Object call() throws Exception {
            ContextHolder.get().setCurrentContextId(context);
            return original.call();
        }
    }

    public static ExecutorService contextAwareExecutor(final ExecutorService original) {
        return (ExecutorService) Proxy.newProxyInstance(ThreadLocalUtil.class.getClassLoader(),
            new Class<?>[] { ExecutorService.class },
            new InvocationHandler() {
                private ExecutorService originalExecutor = original;

                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    String methodName = method.getName();
                    if ("submit".equals(methodName)) {
                        transformSingleInvocation(args);
                    } else if ("invokeAll".equals(methodName) || "invokeAny".equals(methodName)) {
                        transformBatchInvocation(args);
                    }
                    return method.invoke(originalExecutor, args);
                }

            });
    }
}
