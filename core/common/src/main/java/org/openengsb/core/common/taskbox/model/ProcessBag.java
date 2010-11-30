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
 * TODO: document what a ProcessBag is...
 */
public class ProcessBag {
    
    private String processId;
    private String context;
    private String user;
    
    private HashMap<String,Object> properties;
    
    public ProcessBag(String processId, String context, String user) {
        this.processId = processId;
        this.context = context;
        this.user = user;
        
        properties = new HashMap<String,Object>();
        addProperty("process ID", processId);
        addProperty("context", context);
        addProperty("user", user);
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
        if(properties.containsKey(key))
            return false;
        else
            properties.put(key, value);
        return true;
    }
    
    public Object addOrReplaceProperty(String key, Object value) {
        /**
         * @return: The previous value to that key is returned,
         * or null if there was no previous value to that key
         */
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

    /*
    public String[] getPropertyKeys() {
        return properties.keySet().toArray(new String[properties.keySet().size()]);
    }
    */
    
    public List<String> getPropertyKeyList() {
        return new ArrayList<String>(properties.keySet());
    }
    
    public Integer getNumberOfProperties() {
        return properties.size();
    }
}
