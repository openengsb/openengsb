/**

   Copyright 2010 OpenEngSB Division, Vienna University of Technology

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.namespace.QName;

import org.drools.RuleBase;
import org.drools.StatefulSession;
import org.openengsb.contextcommon.ContextHelper;
import org.openengsb.contextcommon.ContextHelperImpl;
import org.openengsb.core.EventHelper;
import org.openengsb.core.EventHelperImpl;
import org.openengsb.core.MessageProperties;
import org.openengsb.core.MethodCallHelper;
import org.openengsb.drools.helper.DomainConfigurationImpl;
import org.openengsb.drools.helper.DroolsHelperImpl;

public class DroolsSession {

    private RuleBase ruleBase;

    private ContextHelper contextHelper;

    private DomainConfigurationImpl domainConfiguration;

    private DroolsHelper droolsHelper;

    private EventHelper eventHelper;

    private DroolsEndpoint endpoint;

    private MessageProperties msgProperties;

    public DroolsSession(MessageProperties msgProperties, DroolsEndpoint endpoint) {
        this.msgProperties = msgProperties;
        this.endpoint = endpoint;
        this.ruleBase = endpoint.getRuleBase();

        this.contextHelper = new ContextHelperImpl(endpoint, msgProperties);
        this.domainConfiguration = new DomainConfigurationImpl(contextHelper);
        this.droolsHelper = new DroolsHelperImpl(msgProperties, endpoint);
        this.eventHelper = new EventHelperImpl(endpoint, msgProperties);
    }

    /**
     * inserts objects into the kb.
     * 
     * @param objects the objects to insert.
     */
    public StatefulSession createSession(Collection<Object> objects) {
        Map<String, Object> globals = new HashMap<String, Object>();
        globals.put("ctx", contextHelper);
        globals.put("config", domainConfiguration);
        globals.put("droolsHelper", droolsHelper);
        globals.put("eventHelper", eventHelper);

        for (Entry<String, Class<? extends Domain>> e : DomainRegistry.domains.entrySet()) {
            Object proxy = createProxy(e.getValue());
            domainConfiguration.addDomain((Domain) proxy, e.getKey());
            globals.put(e.getKey(), proxy);
        }

        StatefulSession session = ruleBase.newStatefulSession();
        populateWorkingMemory(session, objects, globals);
        return session;
    }

    private void populateWorkingMemory(StatefulSession memory, Collection<Object> objects, Map<String, Object> globals) {
        for (Entry<String, Object> global : globals.entrySet()) {
            memory.setGlobal(global.getKey(), global.getValue());
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

    private class GuvnorProxyInvocationHandler implements InvocationHandler {

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            QName service = domainConfiguration.getFullServiceName((Domain) proxy);
            return MethodCallHelper.sendMethodCall(endpoint, service, method, args, msgProperties);
        }

    }

}
