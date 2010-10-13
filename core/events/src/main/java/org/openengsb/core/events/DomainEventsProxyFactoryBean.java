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

package org.openengsb.core.events;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import org.openengsb.core.common.DomainEvents;
import org.openengsb.core.workflow.WorkflowService;
import org.springframework.beans.factory.FactoryBean;

public class DomainEventsProxyFactoryBean implements FactoryBean<DomainEvents> {

    private Class<? extends DomainEvents> domainEventInterface;

    private WorkflowService workflowService;

    private ForwardHandler makeHandler() {
        ForwardHandler handler = new ForwardHandler();
        handler.setWorkflowService(workflowService);
        return handler;
    }

    public void setWorkflowService(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    public void setDomainEventInterface(Class<? extends DomainEvents> domainEventInterface) {
        this.domainEventInterface = domainEventInterface;
    }

    @Override
    public DomainEvents getObject() throws Exception {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Class<?>[] classes = new Class<?>[]{ DomainEvents.class, domainEventInterface };
        InvocationHandler handler = makeHandler();
        return (DomainEvents) Proxy.newProxyInstance(classLoader, classes, handler);
    }

    @Override
    public Class<? extends DomainEvents> getObjectType() {
        return domainEventInterface;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }
}
