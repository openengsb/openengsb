package org.openengsb.core.common.events;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import org.openengsb.core.api.DomainEvents;
import org.openengsb.core.api.edb.EnterpriseDatabaseService;

public class EDBEventsProxyFactoryBean {
    
    private Class<? extends DomainEvents> domainEventInterface;
    
    private EnterpriseDatabaseService edbService;
    
    public void setDomainEventInterface(Class<? extends DomainEvents> domainEventInterface) {
        this.domainEventInterface = domainEventInterface;
    }
    
    public void setEdbService(EnterpriseDatabaseService edbService) {
        this.edbService = edbService;
    }
    
    public DomainEvents getObject() throws Exception {
        ClassLoader classLoader = domainEventInterface.getClassLoader();
        Class<?>[] classes = new Class<?>[]{DomainEvents.class, domainEventInterface};
        InvocationHandler handler = makeHandler();
        return (DomainEvents) Proxy.newProxyInstance(classLoader, classes, handler);
    }
    
    private EDBForwardHandler makeHandler() {
        EDBForwardHandler handler = new EDBForwardHandler();
        handler.setEnterpriseDatabaseService(edbService);
        return handler;
    }

}
