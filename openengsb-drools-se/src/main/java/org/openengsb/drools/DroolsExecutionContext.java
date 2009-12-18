/**

   Copyright 2009 OpenEngSB Division, Vienna University of Technology

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
import java.util.UUID;
import java.util.Map.Entry;

import javax.jbi.messaging.InOut;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.apache.servicemix.jbi.messaging.InOutImpl;
import org.drools.StatefulSession;
import org.drools.WorkingMemory;
import org.drools.event.ActivationCreatedEvent;
import org.drools.event.DefaultAgendaEventListener;
import org.openengsb.contextcommon.ContextHelper;
import org.openengsb.contextcommon.ContextHelperImpl;
import org.openengsb.core.methodcalltransformation.MethodCall;
import org.openengsb.core.methodcalltransformation.ReturnValue;
import org.openengsb.core.methodcalltransformation.Transformer;

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

    private final String contextId;

    private DroolsEndpoint endpoint;

    private ContextHelper contextHelper;

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
    public DroolsExecutionContext(DroolsEndpoint endpoint, Collection<Object> objects, String contextId) {
        this.contextId = contextId == null ? "42" : contextId; // XXX
        this.endpoint = endpoint;
        this.memory = endpoint.getRuleBase().newStatefulSession();
        this.memory.addEventListener(this);
        this.contextHelper = new ContextHelperImpl(endpoint, contextId);

        populateWorkingMemory(objects);
    }

    /**
     * inserts objects into the kb.
     * 
     * @param objects the objects to insert.
     */
    private void populateWorkingMemory(Collection<Object> objects) {
        memory.setGlobal("ctx", contextHelper);

        for (Entry<String, Class<?>> e : DomainRegistry.domains.entrySet()) {
            Object proxy = createProxy(e.getKey(), e.getValue());
            memory.setGlobal(e.getKey(), proxy);
        }

        if (objects != null) {
            for (Object o : objects) {
                memory.insert(o);
            }
        }
    }

    private Object createProxy(String name, Class<?> value) {
        return Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] { value },
                new GuvnorProxyInvocationHandler(name));
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
        /*
         * TODO add auditing features, for tracking fired rules.
         * event.getActivation().getRule();
         */
        log.debug("Event fired rule: " + event.getActivation().getRule().getName());
    }

    private class GuvnorProxyInvocationHandler implements InvocationHandler {

        private String name;

        public GuvnorProxyInvocationHandler(String name) {
            this.name = name;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            try {
                String namespaceURI = contextHelper.getValue(name + "/namespace");
                String serviceName = contextHelper.getValue(name + "/servicename");

                InOut inout = new InOutImpl(UUID.randomUUID().toString());
                inout.setService(new QName(namespaceURI, serviceName));
                inout.setOperation(new QName("methodcall"));

                NormalizedMessage msg = inout.createMessage();
                inout.setInMessage(msg);

                msg.setProperty("contentType", "methodcall");
                msg.setProperty("contextId", contextId);

                MethodCall call = new MethodCall(method, args);

                String xml = Transformer.toXml(call);

                msg.setContent(new StringSource(xml));

                endpoint.sendSync(inout);

                NormalizedMessage outMessage = inout.getOutMessage();
                String outXml = new SourceTransformer().toString(outMessage.getContent());

                ReturnValue returnValue = Transformer.toReturnValue(outXml);

                return returnValue.getValue();
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
