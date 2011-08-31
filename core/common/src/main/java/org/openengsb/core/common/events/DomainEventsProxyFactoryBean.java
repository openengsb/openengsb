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

package org.openengsb.core.common.events;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import org.openengsb.core.api.DomainEvents;
import org.openengsb.core.api.edb.EngineeringDatabaseService;
import org.openengsb.core.api.workflow.WorkflowService;

public class DomainEventsProxyFactoryBean {

    private Class<? extends DomainEvents> domainEventInterface;

    private WorkflowService workflowService;
    private EngineeringDatabaseService edbService;

    private ForwardHandler makeHandler() {
        ForwardHandler handler = new ForwardHandler();
        handler.setWorkflowService(workflowService);
        handler.setEdbService(edbService);
        return handler;
    }

    public void setWorkflowService(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }
    
    public void setEdbService(EngineeringDatabaseService edbService) {
        this.edbService = edbService;
    }

    public void setDomainEventInterface(Class<? extends DomainEvents> domainEventInterface) {
        this.domainEventInterface = domainEventInterface;
    }

    public DomainEvents getObject() throws Exception {
        ClassLoader classLoader = domainEventInterface.getClassLoader();
        Class<?>[] classes = new Class<?>[]{DomainEvents.class, domainEventInterface};
        InvocationHandler handler = makeHandler();
        return (DomainEvents) Proxy.newProxyInstance(classLoader, classes, handler);
    }

}
