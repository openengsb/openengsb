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

package org.openengsb.core.common.taskbox.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A ProcessBag can be used to dynamically save many different properties as
 * key-value pairs (key is a String, value is any object)
 */
public class ProcessBag {

    private String processId;
    private String context;
    private String user;

    private HashMap<String, Object> properties;

    /**
     * Constructor
     */
    public ProcessBag() {
        properties = new HashMap<String, Object>();
    }

    /**
     * Constructor
     * 
     * @param processId
     * @param context
     * @param user
     */
    public ProcessBag(String processId, String context, String user) {
        this();
        this.processId = processId;
        this.context = context;
        this.user = user;        
    }

    /**
     * this method sets all attributes of this ProcessBag to null
     */
    public void setNull() {
        this.processId = null;
        this.context = null;
        this.user = null;
        this.properties = null;
    }

    /**
     * static method to get a totally empty ProcessBag for testing reasons
     * 
     * @return an empty ProcessBag
     */
    public static ProcessBag returnNullProcessBag() {
        ProcessBag pb = new ProcessBag();
        pb.setNull();
        return pb;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getProcessId() {
        return processId;
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

    public boolean addProperty(String key, Object value) {
        if (properties.containsKey(key))
            return false;
        else
            properties.put(key, value);
        return true;
    }

    /**
     * This method adds a property and replaces an potential old one with the
     * same key
     * 
     * @return: The previous value to that key is returned, or null if there was
     *          no previous value to that key
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

    public boolean containsPropertyKey(String key) {
        return properties.containsKey(key);
    }

    public Class<?> getPropertyClass(String key) {
        return properties.get(key).getClass();
    }

    public List<String> getPropertyKeyList() {
        return new ArrayList<String>(properties.keySet());
    }

    public Integer getNumberOfProperties() {
        return properties.size();
    }

    public void removeAllProperties() {
        properties.clear();
    }
}
