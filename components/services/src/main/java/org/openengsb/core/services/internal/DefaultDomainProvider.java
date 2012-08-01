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

package org.openengsb.core.services.internal;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.openengsb.core.api.Domain;
import org.openengsb.core.api.DomainEvents;
import org.openengsb.core.api.DomainProvider;
import org.openengsb.core.api.Event;
import org.openengsb.core.api.l10n.BundleStrings;
import org.openengsb.core.api.l10n.LocalizableString;
import org.osgi.framework.Bundle;

/**
 * Base class for {@code DomainProvider} implementations with the following functionality:
 * <ul>
 * <li>extracts domain interface through parameterized type</li>
 * <li>id is class name of domain interface</li>
 * <li>name is looked up through localized {@code BundleStrings.getString("domain.name")}</li>
 * <li>description is looked up through localized {@code BundleStrings.getString("domain.description")}</li>
 * <li>returns an empty event list</li>
 * </ul>
 */
public class DefaultDomainProvider implements DomainProvider {

    private BundleStrings strings;
    private final Class<? extends Domain> domainInterface;
    private final Class<? extends DomainEvents> domainEventInterface;
    protected String id;

    public DefaultDomainProvider(String id, Bundle bundle, Class<? extends Domain> domainInterface,
            Class<? extends DomainEvents> domainEventInterface) {
        this.id = id;
        strings = new BundleStrings(bundle);
        this.domainInterface = domainInterface;
        this.domainEventInterface = domainEventInterface;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public LocalizableString getName() {
        return strings.getString("domain.name");
    }

    @Override
    public LocalizableString getDescription() {
        return strings.getString("domain.description");
    }

    @Override
    public Class<? extends DomainEvents> getDomainEventInterface() {
        return domainEventInterface;
    }

    @Override
    public List<Class<? extends Event>> getEvents() {
        List<Class<? extends Event>> events = new ArrayList<Class<? extends Event>>();
        for (Method m : domainEventInterface.getDeclaredMethods()) {
            if (!isEventMethod(m)) {
                continue;
            }
            events.add(getEventParameter(m));
        }
        return events;
    }

    @SuppressWarnings("unchecked")
    private Class<? extends Event> getEventParameter(Method m) {
        Class<?> firstParam = m.getParameterTypes()[0];
        return (Class<? extends Event>) firstParam;
    }

    private boolean isEventMethod(Method m) {
        if (m.getParameterTypes().length == 0) {
            return false;
        }
        return Event.class.isAssignableFrom(m.getParameterTypes()[0]);
    }

    @Override
    public Class<? extends Domain> getDomainInterface() {
        return domainInterface;
    }
}
