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

/**
 * creates context-aware Threadpools
 */
public final class ThreadLocalUtil {

    private static final ClassLoader CLASS_LOADER = ThreadLocalUtil.class.getClassLoader();
    private static final Class<?>[] INTERFACES = new Class<?>[] { ExecutorService.class };

    static final class ExecutorServiceHandler implements InvocationHandler {
        private final ExecutorService original;

        public ExecutorServiceHandler(ExecutorService original) {
            this.original = original;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String methodName = method.getName();
            if ("submit".equals(methodName)) {
                transformSingleInvocation(args);
            } else if ("invokeAll".equals(methodName) || "invokeAny".equals(methodName)) {
                transformBatchInvocation(args);
            }
            return method.invoke(original, args);
        }
    }

    @SuppressWarnings("unchecked")
    private static Object transform(Object arg) {
        if (arg instanceof Runnable && arg.getClass() != ContextAwareRunnable.class) {
            return new ContextAwareRunnable((Runnable) arg);
        } else if (arg instanceof Callable<?> && arg.getClass() != ContextAwareCallable.class) {
            return new ContextAwareCallable<Object>((Callable<Object>) arg);
        }
        return arg;
    }

    private static void transformSingleInvocation(Object[] args) {
        args[0] = transform(args[0]);
    }

    @SuppressWarnings("unchecked")
    private static void transformBatchInvocation(Object[] args) {
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

    /**
     * creates a context-aware proxy of the given ExecutorService. All calls to the resulting proxy are directly
     * forwarded to the original after wrapping all {@link Runnable} and {@link Callable} in their context-aware
     * counter-parts.
     *
     * All submitted tasks to the resulting ExecutorService are guaranteed to run in the same context as the method on
     * the ExecutorService was called. When doing this:
     *
     * <pre>
     * ExecutorService pool = ThreadlocalUtil.contextAwareExecutor(Executors.newCachedThreadPool());
     * ContextHolder.get().setCurrentContextId(&quot;42&quot;);
     * pool.submit(command);
     * </pre>
     *
     * You can assume that the command is executed in a thread where the contextid is set to "42".
     *
     */
    public static ExecutorService contextAwareExecutor(ExecutorService original) {
        return (ExecutorService) Proxy.newProxyInstance(CLASS_LOADER, INTERFACES, new ExecutorServiceHandler(original));
    }

    private ThreadLocalUtil() {
    }
}
