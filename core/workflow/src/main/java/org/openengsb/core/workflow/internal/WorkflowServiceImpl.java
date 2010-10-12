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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.drools.KnowledgeBase;
import org.drools.event.process.DefaultProcessEventListener;
import org.drools.event.process.ProcessCompletedEvent;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.ProcessInstance;
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

    private Log log = LogFactory.getLog(WorkflowServiceImpl.class);

    private RuleManager rulemanager;
    private ContextCurrentService currentContextService;
    private BundleContext bundleContext;

    private Map<String, Domain> domainServices = new HashMap<String, Domain>();

    private Map<String, StatefulKnowledgeSession> sessions = new HashMap<String, StatefulKnowledgeSession>();

    private long timeout = 10000;

    @Override
    public void processEvent(Event event) throws WorkflowException {
        StatefulKnowledgeSession session = getSessionForCurrentContext();
        session.insert(event);
        for (ProcessInstance p : session.getProcessInstances()) {
            p.signalEvent(event.getType(), event);
        }
        session.fireAllRules();
    }

    @Override
    public long startFlow(String processId) throws WorkflowException {
        StatefulKnowledgeSession session = getSessionForCurrentContext();
        ProcessInstance processInstance = session.startProcess(processId);
        return processInstance.getId();
    }

    public void waitForFlowToFinish(long id) throws InterruptedException, WorkflowException {
        StatefulKnowledgeSession session = getSessionForCurrentContext();
        synchronized (session) {
            while (session.getProcessInstance(id) != null) {
                session.wait(5000);
            }
        }
    }

    private StatefulKnowledgeSession getSessionForCurrentContext() throws WorkflowException {
        String currentContextId = currentContextService.getCurrentContextId();
        if (currentContextId == null) {
            throw new IllegalStateException("contextID must not be null");
        }
        if (sessions.containsKey(currentContextId)) {
            return sessions.get(currentContextId);
        }
        StatefulKnowledgeSession session;
        try {
            session = createSession();
        } catch (RuleBaseException e) {
            throw new WorkflowException(e);
        }
        sessions.put(currentContextId, session);
        return session;
    }

    private Collection<String> findMissingGlobals() {
        Collection<String> globalsToProcess = new ArrayList<String>();
        for (RuleBaseElementId id : rulemanager.list(RuleBaseElementType.Global)) {
            globalsToProcess.add(id.getName());
        }
        globalsToProcess.remove("event");
        globalsToProcess.removeAll(domainServices.keySet());

        return discoverNewGlobalValues(globalsToProcess);
    }

    private Collection<String> discoverNewGlobalValues(Collection<String> globalsToProcess) {
        for (Iterator<String> iterator = globalsToProcess.iterator(); iterator.hasNext();) {
            String g = iterator.next();
            if (findGlobal(g)) {
                iterator.remove();
            }
        }
        return globalsToProcess;
    }

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

    protected StatefulKnowledgeSession createSession() throws RuleBaseException, WorkflowException {
        KnowledgeBase rb = rulemanager.getRulebase();
        log.debug("retrieved rulebase: " + rb + "from source " + rulemanager);
        final StatefulKnowledgeSession session = rb.newStatefulKnowledgeSession();
        log.debug("session started");
        populateGlobals(session);
        log.debug("globals have been set");
        session.addEventListener(new DefaultProcessEventListener() {
            @Override
            public void afterProcessCompleted(ProcessCompletedEvent event) {
                synchronized (session) {
                    session.notifyAll();
                }
            }
        });
        return session;
    }

    private void populateGlobals(StatefulKnowledgeSession session) throws WorkflowException {
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

    public void setRulemanager(RuleManager rulemanager) {
        this.rulemanager = rulemanager;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }
}
