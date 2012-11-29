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

import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openengsb.core.api.Connector;
import org.openengsb.core.api.ConnectorInstanceFactory;
import org.openengsb.core.api.Domain;
import org.openengsb.core.api.DomainProvider;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ComputationException;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Abstract baseclass for {@link ConnectorInstanceFactory}s that are used for {@link VirtualConnector}s. When such a
 * factory creates a connector,an {@link java.lang.reflect.InvocationHandler} in the form of a {@link VirtualConnector}
 * is created and used in a proxy that is then returned as the resulting connector instance (which is registered as a
 * service).
 */
public abstract class VirtualConnectorFactory<VirtualType extends VirtualConnector>
        implements ConnectorInstanceFactory {

    protected DomainProvider domainProvider;
    protected Map<Domain, VirtualType> handlers = new HashMap<Domain, VirtualType>();

    protected VirtualConnectorFactory(DomainProvider domainProvider) {
        this.domainProvider = domainProvider;
    }

    @Override
    public Connector createNewInstance(String id) {
        VirtualType handler = createNewHandler(id);
        Set<Class<?>> interfaces = Collections.emptySet();
        Connector newProxyInstance = createProxy(handler, interfaces);
        handlers.put(newProxyInstance, handler);
        return newProxyInstance;
    }

    private Connector createProxy(VirtualType handler, Collection<Class<?>> interfaces) {
        HashSet<Class<?>> classes = Sets.newHashSet(interfaces);
        classes.add(domainProvider.getDomainInterface());
        classes.add(Connector.class);
        Class<?>[] classesAsArray = classes.toArray(new Class<?>[classes.size()]);
        return (Connector) Proxy.newProxyInstance(this.getClass().getClassLoader(), classesAsArray, handler);
    }

    /**
     * creates a new {@link VirtualConnector} used as {@link java.lang.reflect.InvocationHandler} for the proxy.
     */
    protected abstract VirtualType createNewHandler(String id);

    @Override
    public Connector applyAttributes(Connector instance, Map<String, String> attributes) {
        VirtualType handler = handlers.get(instance);
        Collection<String> mixins = Maps.filterKeys(attributes, new Predicate<String>() {
            @Override
            public boolean apply(String s) {
                return s.startsWith("mixin.");
            }
        }).values();
        Collection<Class<?>> mixinClasses = Collections2.transform(mixins, new Function<String, Class<?>>() {
            @Override
            public Class<?> apply(String input) {
                try {
                    return getClass().getClassLoader().loadClass(input);
                } catch (ClassNotFoundException e) {
                    throw new ComputationException(e);
                }
            }
        });
        instance = createProxy(handler, mixinClasses);
        updateHandlerAttributes(handler, attributes);
        return instance;
    }

    protected abstract VirtualType updateHandlerAttributes(VirtualType handler, Map<String, String> attributes);

}
