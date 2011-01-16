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

package org.openengsb.core.common;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.aopalliance.aop.Advice;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.framework.ProxyFactory;

public class ProxyFactoryBean {

    private Log log = LogFactory.getLog(ProxyFactoryBean.class);

    private List<String> proxyInterfaces;

    private List<Advice> advices;

    private Object target;

    public Object getObject() {
        Class<?>[] interfaces = retrieveInterfaceClasses();
        ProxyFactory proxyFactory = new ProxyFactory(interfaces);
        for (Advice a : advices) {
            proxyFactory.addAdvice(a);
        }
        proxyFactory.setTarget(target);
        return proxyFactory.getProxy(getClass().getClassLoader());
    }

    public void setProxyInterfaces(List<String> proxyInterfaces) {
        this.proxyInterfaces = proxyInterfaces;
    }

    public void setAdvices(List<Advice> advices) {
        this.advices = advices;
    }

    public void setTarget(Object target) {
        this.target = target;
    }

    private Class<?>[] retrieveInterfaceClasses() {
        Class<?>[] interfaces = new Class<?>[proxyInterfaces.size()];
        Collection<ClassLoader> loaders = new HashSet<ClassLoader>();
        loaders.add(Thread.currentThread().getContextClassLoader());
        loaders.add(getClass().getClassLoader());
        loaders.add(ClassLoader.getSystemClassLoader());

        for (int i = 0; i < proxyInterfaces.size(); i++) {
            Class<?> loadedClass = null;
            int j = 0;
            for (ClassLoader loader : loaders) {
                j++;
                try {
                    loadedClass = loader.loadClass(proxyInterfaces.get(i));
                    log.info("found class using Classloader: " + i + " - " + loader.getClass());
                    break;
                } catch (ClassNotFoundException e) {
                    // ignore throw new RuntimeException(e);
                }
            }
            if (loadedClass == null) {
                throw new IllegalStateException("Could not find class " + proxyInterfaces.get(i));
            }
            interfaces[i] = loadedClass;
        }
        return interfaces;
    }
}
