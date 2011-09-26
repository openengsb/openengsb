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

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.lang.ClassUtils;
import org.openengsb.core.api.security.SecurityAttributeProvider;
import org.openengsb.core.api.security.annotation.SecurityAttribute;
import org.openengsb.core.api.security.annotation.SecurityAttributes;
import org.openengsb.core.api.security.model.SecurityAttributeEntry;
import org.springframework.security.core.context.SecurityContextHolder;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

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

    public static Collection<SecurityAttributeEntry> getSecurityAttributesFromClass(
            Class<?> componentClass) {
        SecurityAttribute annotation = componentClass.getAnnotation(SecurityAttribute.class);
        if (annotation != null) {
            return Arrays.asList(convertAnnotationToEntry(annotation));
        }
        SecurityAttributes annotation2 = componentClass.getAnnotation(SecurityAttributes.class);
        if (annotation2 != null) {
            Collection<SecurityAttributeEntry> result = Lists.newArrayList();
            for (SecurityAttribute a : annotation2.value()) {
                result.add(convertAnnotationToEntry(a));
            }
            return result;
        }
        return null;
    }

    public static Collection<SecurityAttributeEntry> getSecurityAttributesForMethod(Method method) {
        Collection<SecurityAttributeEntry> result = Sets.newHashSet();
        @SuppressWarnings("unchecked")
        List<Class<?>> allInterfaces = ClassUtils.getAllInterfaces(method.getDeclaringClass());
        allInterfaces.add(method.getDeclaringClass());
        for (Class<?> interfaze : allInterfaces) {
            Method method2;
            try {
                method2 = interfaze.getMethod(method.getName(), method.getParameterTypes());

            } catch (NoSuchMethodException e) {
                continue;
            }
            for (SecurityAttribute a : findAllSecurityAttributeAnnotations(method2)) {
                result.add(new SecurityAttributeEntry(a.key(), a.value()));
            }
        }
        return result;
    }

    public static Collection<String> getServiceInstanceIds(Collection<SecurityAttributeProvider> providers,
            Object service) {
        Collection<String> result = new ArrayList<String>();
        for (SecurityAttributeProvider p : providers) {
            Collection<SecurityAttributeEntry> attribute = p.getAttribute(service);
            for (SecurityAttributeEntry entry : attribute) {
                if ("name".equals(entry.getKey())) {
                    result.add(entry.getValue());
                }
            }
        }
        return result;
    }

    private static SecurityAttributeEntry convertAnnotationToEntry(SecurityAttribute annotation) {
        return new SecurityAttributeEntry(annotation.key(), annotation.value());
    }

    public static void executeWithSystemPermissions(Runnable task) {
        executor.execute(new RootRunnable(task));
    }

    public static SecurityAttribute[] findAllSecurityAttributeAnnotations(AnnotatedElement element) {
        SecurityAttribute annotation = element.getAnnotation(SecurityAttribute.class);
        if (annotation != null) {
            return new SecurityAttribute[]{ annotation };
        }
        SecurityAttributes annotations = element.getAnnotation(SecurityAttributes.class);
        if (annotations == null) {
            return new SecurityAttribute[0];
        }
        return annotations.value();
    }

    private SecurityUtils() {
    }
}
