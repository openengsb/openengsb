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

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.concurrent.SubjectAwareExecutorService;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.openengsb.core.api.security.Credentials;
import org.openengsb.core.common.util.ContextAwareCallable;
import org.openengsb.core.common.util.ContextAwareRunnable;
import org.openengsb.core.common.util.ThreadLocalUtil;
import org.openengsb.core.security.internal.RootSecurityHolder;

/**
 * provides util-methods for security purposes
 */
public final class SecurityContext {

    public static void login(String username, Credentials credentials) {
        OpenEngSBAuthenticationToken token = new OpenEngSBAuthenticationToken(username, credentials);
        SecurityUtils.getSubject().login(token);
    }

    public static void logout() {
        Subject subject = ThreadContext.getSubject();
        if (subject == null) {
            return;
        }
        subject.logout();
    }

    public static Object getAuthenticatedPrincipal() {
        Subject subject = ThreadContext.getSubject();
        if (subject == null) {
            return null;
        }
        return subject.getPrincipal();
    }

    @SuppressWarnings("unchecked")
    public static List<Object> getAllAuthenticatedPrincipals() {
        return ThreadContext.getSubject().getPrincipals().asList();
    }

    /**
     * Executes the given task with root-permissions. Use with care.
     * 
     * @throws ExecutionException if an exception occurs during the execution of the task
     */
    public static <ReturnType> ReturnType executeWithSystemPermissions(Callable<ReturnType> task)
        throws ExecutionException {
        ContextAwareCallable<ReturnType> contextAwareCallable = new ContextAwareCallable<ReturnType>(task);
        return RootSecurityHolder.getRootSubject().execute(contextAwareCallable);
    }

    public static ExecutorService getSecurityContextAwareExecutor(ExecutorService original) {
        SubjectAwareExecutorService subjectAwareExecutor = new SubjectAwareExecutorService(original);
        return ThreadLocalUtil.contextAwareExecutor(subjectAwareExecutor);
    }

    /**
     * Executes the given task with root-permissions. Use with care.
     */
    public static void executeWithSystemPermissions(Runnable task) {
        ContextAwareRunnable contextAwaretask = new ContextAwareRunnable(task);
        RootSecurityHolder.getRootSubject().execute(contextAwaretask);
    }

    private SecurityContext() {
    }
}
