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

import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import org.openengsb.core.api.Event;

@XmlRootElement
public interface RemoteEvent extends Event {
    String getClassName();
    
    void setClassName(String className);
    
    Map<String, String> getNestedEventProperties();
    
    void setNestedEventProperties(Map<String, String> nestedEventProperties);
    
    Map<String, String> getContextValues();
    
    void setContextValues(Map<String, String> contextValues);
    
//    private String className;
//    private Map<String, String> nestedEventProperties = new HashMap<String, String>();
//    private Map<String, String> contextValues = new HashMap<String, String>();
//
//    public RemoteEvent() {
//    }
//
//    public RemoteEvent(String className) {
//        this.className = className;
//    }
//
//    public String getClassName() {
//        return this.className;
//    }
//
//    public void setClassName(String className) {
//        this.className = className;
//    }
//
//    public Map<String, String> getNestedEventProperties() {
//        return this.nestedEventProperties;
//    }
//
//    public void setNestedEventProperties(Map<String, String> nestedEventProperties) {
//        this.nestedEventProperties = nestedEventProperties;
//    }
//
//    public Map<String, String> getContextValues() {
//        return this.contextValues;
//    }
//
//    public void setContextValues(Map<String, String> contextValues) {
//        this.contextValues = contextValues;
//    }

}
