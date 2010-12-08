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

package org.openengsb.core.common.workflow.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.openengsb.core.common.workflow.ProcessBagException;

/**
 * The ProcessBag is a workflow property and contains all neccessary information and workflow metadata, i.e. processId
 * or context. It contains a HashMap so every sub-class can use this field to add custom properties. Each workflow
 * creates its own new ProcessBag when none is passed on workflow start.
 */
public class ProcessBag {
    private String processId;
    private String context;
    private String user;
    private Object processIdLock = new Object();

    private Map<String, Object> properties;

    public ProcessBag() {
        properties = new HashMap<String, Object>();
    }

    public ProcessBag(Map<String, Object> properties) {
        this.properties = properties;
    }

    public ProcessBag(ProcessBag bag) {
        this.properties = bag.properties;
        this.processId = bag.processId;
        this.context = bag.context;
        this.user = bag.user;
    }

    public ProcessBag(String processId, String context, String user) {
        this();
        this.processId = processId;
        this.context = context;
        this.user = user;
    }

    public void setProcessId(String processId) {
        synchronized (processIdLock) {
            this.processId = processId;
            processIdLock.notifyAll();
        }
    }

    public String readProcessId() {
        synchronized (processIdLock) {
            while (processId == null) {
                try {
                    processIdLock.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            return processId;
        }
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getContext() {
        return context;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getUser() {
        return user;
    }
    
    /**
     * Adds a new property if, but only if it does not exist already
     * 
     * @throws ProcessBagException
     */
    public void addProperty(String key, Object value) throws ProcessBagException {
        if (properties.containsKey(key)) {
            throw new ProcessBagException(key + " already used in the processbag!");
        } else {
            properties.put(key, value);
        }
    }

    /**
     * This method adds a property and replaces an potential old one with the same key
     * 
     * @return: The previous value to that key is returned, or null if there was no previous value to that key
     */
    public Object addOrReplaceProperty(String key, Object value) {
        return properties.put(key, value);
    }

    public Object getProperty(String key) {
        return properties.get(key);
    }

    public Object removeProperty(String key) {
        return properties.remove(key);
    }

    public boolean containsProperty(String key) {
        return properties.containsKey(key);
    }

    public Class<?> getPropertyClass(String key) {
        return properties.get(key).getClass();
    }

    public Set<String> propertyKeySet() {
        return properties.keySet();
    }

    public int propertyCount() {
        return properties.size();
    }

    public void removeAllProperties() {
        properties.clear();
    }
}
