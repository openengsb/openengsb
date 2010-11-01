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

package org.openengsb.core.taskbox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openengsb.core.persistence.PersistenceManager;
import org.openengsb.core.persistence.PersistenceService;
import org.openengsb.core.persistence.PersistenceException;
import org.openengsb.core.taskbox.model.Ticket;
import org.openengsb.core.workflow.WorkflowException;
import org.openengsb.core.workflow.WorkflowService;
import org.osgi.framework.BundleContext;
import org.springframework.osgi.context.BundleContextAware;
import java.util.UUID;

public class TaskboxServiceImpl implements TaskboxService, BundleContextAware {
    private Log log = LogFactory.getLog(getClass());

    private WorkflowService workflowService;

    private String message;

    private PersistenceService persistence;

    private PersistenceManager persistenceManager;

    private BundleContext bundleContext;
    
    public void init() {
    	this.persistence = persistenceManager.getPersistenceForBundle(bundleContext.getBundle());
    }

    @Override
    public String createTicket(String type) {
    	UUID uuid = UUID.randomUUID();
    	Ticket ticket = new Ticket("ID-" + uuid.toString(),type);
    	
    	try {
        	this.persistence.create(ticket);
    	} catch(PersistenceException e) {}
    	
    	return ticket.getID();
    }
    
    @Override
    public Ticket getTicket(String ID) throws TaskboxException {
    	List<Ticket> tickets = persistence.query(new Ticket(ID));
        if (tickets.isEmpty()) {
            throw new TaskboxException("Ticket <" + ID + "> couldn't be found.");
        }
        
        return tickets.get(0);    	
    }
    
    @Override
    public String getWorkflowMessage() throws TaskboxException {
        if (message == null) {
            throw new TaskboxException();
        }
        
        return message;
        
        /*List<Ticket> tickets = persistence.query(new Ticket("Ticket-<" + message + ">", ""));
        if (tickets.isEmpty()) {
            throw new TaskboxException("Ticket: Ticket-<" + message + "> doesn't exist.");
        }
        
        return tickets.get(0).getID();*/
    }

    @Override
    public void setWorkflowMessage(String message) {
        this.message = message;
        
        Ticket ticket = new Ticket("Ticket-<" + message + ">", "");
        
        try {
        	this.persistence.create(ticket);
        } catch(PersistenceException e) {}
    }

    @Override
    public void startWorkflow(String ID) throws TaskboxException {
        try {
        	Ticket ticket = this.getTicket(ID);
        	Map<String, Object> parameterMap = 
                new HashMap<String, Object>();
            parameterMap.put("ticket", ticket);
            
            workflowService.startFlow("tasktest", parameterMap);
            log.trace("Started workflow 'tasktest'");
        } catch (WorkflowException e) {
            throw new TaskboxException(e);
        }
    }

    public void setWorkflowService(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }
    
    public void setPersistenceManager(PersistenceManager persistenceManager) {
        this.persistenceManager = persistenceManager;
    }

    @Override
    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }
}
