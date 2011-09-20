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

package org.openengsb.core.api.workflow.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlRootElement;

import org.openengsb.core.api.persistence.IgnoreInQueries;
import org.openengsb.core.api.workflow.ProcessBagException;

/**
 * The ProcessBag is a workflow property that contains all neccessary information and workflow metadata.
 *
 * It contains a HashMap so every sub-class can use this field to add custom properties. Each workflow creates its own
 * new ProcessBag when none is passed on workflow start.
 *
 * One of the properties is the workflow ID it belongs to. It is recommended to not change this value!
 */
@SuppressWarnings("serial")
@XmlRootElement
public class ProcessBag implements Serializable {
    private String processId;
    private String context;
    private String user;
    private transient Object processIdLock = new Object();
    private boolean empty = false;

    private Map<String, Object> properties;

    public ProcessBag() {
        properties = new HashMap<String, Object>();
    }

    public ProcessBag(Map<String, Object> properties) {
        this.properties = properties;
    }

    public ProcessBag(ProcessBag bag) {
        properties = bag.properties;
        processId = bag.processId;
        context = bag.context;
        user = bag.user;
    }

    public void setProcessId(String processId) {
        synchronized (processIdLock) {
            this.processId = processId;
            processIdLock.notifyAll();
        }
    }

    public String getProcessId() {
        if (empty) {
            return processId;
        }
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
     * Adds a new property only if it does not exist already
     *
     * @throws ProcessBagException if the key is already present
     */
    public void addProperty(String key, Object value) throws ProcessBagException {
        if (properties.containsKey(key)) {
            throw new ProcessBagException(key + " already used!");
        } else {
            properties.put(key, value);
        }
    }

    /**
     * Adds a property and replaces an potential old one
     */
    public void addOrReplaceProperty(String key, Object value) {
        properties.put(key, value);
    }

    public Object getProperty(String key) {
        if (properties == null) {
            return null;
        }
        return properties.get(key);
    }

    public void removeProperty(String key) {
        properties.remove(key);
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

    protected void setEmpty() {
        empty = true;
    }

    /*
     * need these for jackson
     */
    @IgnoreInQueries
    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

}
