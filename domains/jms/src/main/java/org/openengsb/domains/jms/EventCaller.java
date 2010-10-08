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

package org.openengsb.domains.jms;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.openengsb.core.common.DomainEvents;
import org.openengsb.core.common.DomainProvider;
import org.openengsb.core.common.Event;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class EventCaller implements DomainEvents {

    private DomainEvents domainEventsHandler;

    public EventCaller(BundleContext bundleContext, DomainProvider provider) {
        ServiceReference serviceReference =
            bundleContext.getServiceReference(provider.getDomainEventInterface().getName());
        Object service = bundleContext.getService(serviceReference);
        if (service instanceof DomainEvents) {
            this.domainEventsHandler = (DomainEvents) service;
        }
    }

    public void raiseEvent(Event event) {
        try {
            Method method = domainEventsHandler.getClass().getMethod("raiseEvent", event.getClass());
            method.invoke(domainEventsHandler, event);
        } catch (SecurityException e) {
            throw new OpenENGSbProxyException(e);
        } catch (NoSuchMethodException e) {
            throw new OpenENGSbProxyException(e);
        } catch (IllegalArgumentException e) {
            throw new OpenENGSbProxyException(e);
        } catch (IllegalAccessException e) {
            throw new OpenENGSbProxyException(e);
        } catch (InvocationTargetException e) {
            throw new OpenENGSbProxyException(e);
        }
    }
}
