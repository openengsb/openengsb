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
package org.openengsb.core.security;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import org.apache.shiro.concurrent.SubjectAwareExecutorService;
import org.openengsb.core.common.util.ContextAwareCallable;
import org.openengsb.core.common.util.ContextAwareRunnable;
import org.openengsb.core.common.util.ThreadLocalUtil;
import org.openengsb.core.security.internal.RootSubjectHolder;

/**
 * provides util-methods for security purposes
 */
public final class SecurityContext {

    /**
     * Executes the given task with root-permissions. Use with care.
     * 
     * @throws ExecutionException if an exception occurs during the execution of the task
     */
    public static <ReturnType> ReturnType executeWithSystemPermissions(Callable<ReturnType> task)
        throws ExecutionException {
        ContextAwareCallable<ReturnType> contextAwareCallable = new ContextAwareCallable<ReturnType>(task);
        return RootSubjectHolder.getRootSubject().execute(contextAwareCallable);
    }

    /**
     * wraps an existing ExecutorService to handle context- and security-related threadlocal variables
     */
    public static void executeWithSystemPermissions(Runnable task) {
        ContextAwareRunnable contextAwaretask = new ContextAwareRunnable(task);
        RootSubjectHolder.getRootSubject().execute(contextAwaretask);
    }

    /**
     * Wrap the given executor so that it takes authentication-information and context are inherited to tasks when they
     * are submitted.
     */
    public static ExecutorService getSecurityContextAwareExecutor(ExecutorService original) {
        SubjectAwareExecutorService subjectAwareExecutor = new SubjectAwareExecutorService(original);
        return ThreadLocalUtil.contextAwareExecutor(subjectAwareExecutor);
    }

    private SecurityContext() {
    }
}
