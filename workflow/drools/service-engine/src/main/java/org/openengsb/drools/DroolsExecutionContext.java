/**

   Copyright 2010 OpenEngSB Division, Vienna University of Technology

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE\-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
   
 */
package org.openengsb.drools;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.Map.Entry;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.drools.StatefulSession;
import org.drools.WorkingMemory;
import org.drools.event.ActivationCreatedEvent;
import org.drools.event.DefaultAgendaEventListener;
import org.openengsb.contextcommon.ContextHelper;
import org.openengsb.contextcommon.ContextHelperImpl;
import org.openengsb.core.MessageProperties;
import org.openengsb.core.MethodCallHelper;
import org.openengsb.drools.helper.DomainConfigurationImpl;
import org.openengsb.drools.helper.DroolsHelperImpl;

/**
 * Represents the execution context of the Drools rules.
 */
public class DroolsExecutionContext extends DefaultAgendaEventListener {

    /**
     * the logger.
     */
    private static Log log = LogFactory.getLog(DroolsExecutionContext.class);

    /**
     * Memory of the session handling the event.
     */
    private final StatefulSession memory;

    private MessageProperties msgProperties;

    private DroolsEndpoint endpoint;

    private ContextHelper contextHelper;

    private DomainConfigurationImpl domainConfiguration;

    private DroolsHelper droolsHelper;

    /**
     * Start a new execution context for the specified exchange.
     * 
     * This will create and fill {@link WorkingMemory} and register listeners on
     * it to keep track of things.
     * 
     * @param endpoint endpoint the context belongs to
     * @param objects objects to insert into the working memory
     * @param contextId
     */
    public DroolsExecutionContext(DroolsEndpoint endpoint, Collection<Object> objects, MessageProperties msgProperties) {
        this.msgProperties = msgProperties;
        this.endpoint = endpoint;
        this.memory = endpoint.getRuleBase().newStatefulSession();
        this.memory.addEventListener(this);
        this.contextHelper = new ContextHelperImpl(endpoint, msgProperties);
        this.domainConfiguration = new DomainConfigurationImpl(contextHelper);
        this.droolsHelper = new DroolsHelperImpl(this, memory);
        populateWorkingMemory(objects);
    }

    /**
     * inserts objects into the kb.
     * 
     * @param objects the objects to insert.
     */
    private void populateWorkingMemory(Collection<Object> objects) {
        memory.setGlobal("ctx", contextHelper);
        memory.setGlobal("config", domainConfiguration);
        memory.setGlobal("droolsHelper", droolsHelper);

        for (Entry<String, Class<? extends Domain>> e : DomainRegistry.domains.entrySet()) {
            Object proxy = createProxy(e.getValue());
            domainConfiguration.addDomain((Domain) proxy, e.getKey());
            memory.setGlobal(e.getKey(), proxy);
        }

        if (objects != null) {
            for (Object o : objects) {
                memory.insert(o);
            }
        }
    }

    private Object createProxy(Class<? extends Domain> value) {
        return Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] { value },
                new GuvnorProxyInvocationHandler());
    }

    /**
     * Start the execution context. This will fire all rules in the rule base.
     */
    public void start() {
        memory.fireAllRules();
    }

    /**
     * Stop the context, disposing of all event listeners and working memory
     * contents.
     */
    public void stop() {
        memory.removeEventListener(this);
        memory.dispose();
    }

    @Override
    public void activationCreated(ActivationCreatedEvent event, WorkingMemory workingMemory) {
        log.debug("Event fired rule: " + event.getActivation().getRule().getName());
    }

    public void changeMessageProperties(MessageProperties msgProperties) {
        this.msgProperties = msgProperties;
        this.contextHelper = new ContextHelperImpl(endpoint, msgProperties);
        this.domainConfiguration.setContextHelper(contextHelper);
        memory.setGlobal("ctx", contextHelper);
    }

    private class GuvnorProxyInvocationHandler implements InvocationHandler {

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            QName service = domainConfiguration.getFullServiceName((Domain) proxy);
            return MethodCallHelper.sendMethodCall(endpoint, service, method, args, msgProperties);
        }

    }

    public MessageProperties getMessageProperties() {
        return this.msgProperties;
    }

}
