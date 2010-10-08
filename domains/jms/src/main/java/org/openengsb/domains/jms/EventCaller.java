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
