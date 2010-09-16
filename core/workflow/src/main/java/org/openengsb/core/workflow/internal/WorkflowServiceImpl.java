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

package org.openengsb.core.workflow.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.drools.RuleBase;
import org.drools.StatefulSession;
import org.drools.event.AgendaEventListener;
import org.openengsb.core.common.Domain;
import org.openengsb.core.common.Event;
import org.openengsb.core.common.context.ContextCurrentService;
import org.openengsb.core.workflow.RuleBaseException;
import org.openengsb.core.workflow.RuleManager;
import org.openengsb.core.workflow.WorkflowException;
import org.openengsb.core.workflow.WorkflowService;
import org.openengsb.core.workflow.model.RuleBaseElementId;
import org.openengsb.core.workflow.model.RuleBaseElementType;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.springframework.osgi.context.BundleContextAware;

public class WorkflowServiceImpl implements WorkflowService, BundleContextAware, ServiceListener {

    private RuleManager rulemanager;

    private Collection<AgendaEventListener> listeners = new ArrayList<AgendaEventListener>();

    private ContextCurrentService currentContextService;
    private Map<String, Domain> domainServices = new HashMap<String, Domain>();

    private BundleContext bundleContext;

    private long timeout = 10000;

    private boolean findGlobal(String name) {
        ServiceReference[] allServiceReferences;
        try {
            allServiceReferences = bundleContext.getAllServiceReferences(Domain.class.getName(),
                    String.format("(&(openengsb.service.type=domain)(id=domains.%s))", name));
        } catch (InvalidSyntaxException e) {
            throw new IllegalStateException(e);
        }
        if (allServiceReferences == null) {
            return false;
        }
        if (allServiceReferences.length != 1) {
            throw new IllegalStateException(String.format("found more than one match for \"%s\".", name));
        }
        ServiceReference ref = allServiceReferences[0];
        Domain service = (Domain) bundleContext.getService(ref);
        domainServices.put(name, service);
        return true;
    }

    @Override
    public void processEvent(Event event) throws WorkflowException {
        try {
            currentContextService.setThreadLocalContext(event.getContextId());
            StatefulSession session = createSession();
            populateGlobals(session);
            session.insert(event);
            session.fireAllRules();
            session.dispose();
        } catch (RuleBaseException e) {
            throw new WorkflowException(e);
        }
    }

    private Collection<String> findMissingGlobals() {
        Collection<String> globalsToProcess = new ArrayList<String>();
        for (RuleBaseElementId id : rulemanager.list(RuleBaseElementType.Global)) {
            globalsToProcess.add(id.getName());
        }
        globalsToProcess.removeAll(domainServices.keySet());

        for (Iterator<String> iterator = globalsToProcess.iterator(); iterator.hasNext();) {
            String g = iterator.next();
            if (findGlobal(g)) {
                iterator.remove();
            }
        }
        return globalsToProcess;
    }

    private void populateGlobals(StatefulSession session) throws WorkflowException {
        Collection<String> missingGlobals = findMissingGlobals();
        if (!missingGlobals.isEmpty()) {
            try {
                synchronized (domainServices) {
                    domainServices.wait(timeout);
                }
            } catch (InterruptedException e) {
                throw new WorkflowException(e);
            }
            for (Iterator<String> iterator = missingGlobals.iterator(); iterator.hasNext();) {
                if (domainServices.get(iterator.next()) != null) {
                    iterator.remove();
                }
            }
            if (!missingGlobals.isEmpty()) {
                throw new WorkflowException("there are unassigned globals, maybe some service is missing "
                        + missingGlobals);
            }
        }
        for (Entry<String, Domain> entry : domainServices.entrySet()) {
            session.setGlobal(entry.getKey(), entry.getValue());
        }
    }

    public void setRulemanager(RuleManager rulemanager) {
        this.rulemanager = rulemanager;
    }

    protected StatefulSession createSession() throws RuleBaseException {
        RuleBase rb = rulemanager.getRulebase();
        StatefulSession session = rb.newStatefulSession();
        for (AgendaEventListener l : listeners) {
            session.addEventListener(l);
        }
        return session;
    }

    public void registerRuleListener(AgendaEventListener listener) {
        listeners.add(listener);
    }

    public void setCurrentContextService(ContextCurrentService currentContextService) {
        this.currentContextService = currentContextService;
    }

    public void setDomainServices(Map<String, Domain> domainServices) {
        this.domainServices = domainServices;
    }

    @Override
    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    @Override
    public void serviceChanged(ServiceEvent event) {
        if (event.getType() == ServiceEvent.REGISTERED) {
            ServiceReference serviceReference = event.getServiceReference();
            if (serviceReference.getProperty("openengsb.service.type").equals("domain")) {
                String id = (String) serviceReference.getProperty("id");
                String name = id.replaceFirst("domains.", "");
                Domain service = (Domain) bundleContext.getService(serviceReference);
                synchronized (domainServices) {
                    domainServices.put(name, service);
                    domainServices.notify();
                }
            }
        }

    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }
}
