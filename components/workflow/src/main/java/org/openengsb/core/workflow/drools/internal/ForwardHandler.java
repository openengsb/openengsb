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

package org.openengsb.core.workflow.drools.internal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.openengsb.core.api.DomainEvents;
import org.openengsb.core.api.Event;
import org.openengsb.core.api.model.ModelDescription;
import org.openengsb.core.common.AbstractOpenEngSBInvocationHandler;
import org.openengsb.core.ekb.api.TransformationEngine;
import org.openengsb.core.workflow.api.WorkflowException;
import org.openengsb.core.workflow.api.WorkflowService;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ForwardHandler extends AbstractOpenEngSBInvocationHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ForwardHandler.class);
    private WorkflowService workflowService;
    private TransformationEngine transformer;

    public ForwardHandler() {
        super(true);
    }

    public ForwardHandler(WorkflowService workflowService, TransformationEngine transformer) {
        super(true);
        this.workflowService = workflowService;
        this.transformer = transformer;
    }

    private ModelDescription getModelDescription(Class<?> clazz){
        Bundle origin = FrameworkUtil.getBundle(clazz);
        Version version = origin.getVersion();
        return new ModelDescription(clazz, version);
    }

    @Override
    public Object handleInvoke(Object proxy, Method method, Object[] args) throws IllegalAccessException,
            InvocationTargetException {
        checkMethod(method);
        Object source = args[0];
        ModelDescription sourceModel = getModelDescription(source.getClass());
        Class<? extends DomainEvents> target = findDomainEventsInterface(proxy);
        ModelDescription targetModel = getModelDescription(target);
        Event e = (Event) transformer.performTransformation(sourceModel, targetModel, source);
        forwardEvent(e);
        return null;
    }

    private Class<? extends DomainEvents> findDomainEventsInterface(Object proxy) {
        for (Class<?> i : proxy.getClass().getInterfaces()) {
            if (DomainEvents.class.isAssignableFrom(i) && !DomainEvents.class.equals(i)) {
                return (Class<? extends DomainEvents>) i;
            }
        }
        throw new IllegalStateException("proxy was not registered with a domain-events-interface "
                + Arrays.toString(proxy.getClass().getInterfaces()));
    }

    private void forwardEvent(Event event) throws InvocationTargetException {
        LOGGER.info("Forwarding event to workflow service");
        try {
            workflowService.processEvent(event);
        } catch (WorkflowException e) {
            throw new InvocationTargetException(e);
        }
    }

    private void checkMethod(Method method) {
        if (method.getParameterTypes().length != 1) {
            throw new EventProxyException(
                    "Event proxy can only handle methods named raiseEvent where the first parameter is of type Event, "
                            + "but encountered invocation of method raiseEvent without parameter. Method: " + method);
        } else if (!Event.class.isAssignableFrom(method.getParameterTypes()[0])) {
            throw new EventProxyException(
                    "Event proxy can only handle methods named raiseEvent where the first parameter is of type Event, "
                            + "but encountered invocation of method raiseEvent where first parameter is no Event. Method: "
                            + method);
        }
    }

    public void setWorkflowService(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }
}
