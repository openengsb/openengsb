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
package org.openengsb.core.api;

import java.util.Collection;
import java.util.Map;

import org.openengsb.core.api.security.model.SecurityAttributeEntry;

public class GenericControlledObject {

    private Collection<SecurityAttributeEntry> securityAttributes;

    private String action;

    private Map<String, Object> metaData;

    public GenericControlledObject() {
    }

    public GenericControlledObject(Collection<SecurityAttributeEntry> securityAttributes) {
        this.securityAttributes = securityAttributes;
    }

    public GenericControlledObject(Collection<SecurityAttributeEntry> securityAttributes, String action) {
        this.securityAttributes = securityAttributes;
        this.action = action;
    }

    public GenericControlledObject(Collection<SecurityAttributeEntry> securityAttributes, Map<String, Object> metaData) {
        this.securityAttributes = securityAttributes;
        this.metaData = metaData;
    }

    public GenericControlledObject(Collection<SecurityAttributeEntry> securityAttributes, String action,
            Map<String, Object> metaData) {
        this.securityAttributes = securityAttributes;
        this.action = action;
        this.metaData = metaData;
    }

    public Collection<SecurityAttributeEntry> getSecurityAttributes() {
        return securityAttributes;
    }

    public void setSecurityAttributes(Collection<SecurityAttributeEntry> securityAttributes) {
        this.securityAttributes = securityAttributes;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Map<String, Object> getMetaData() {
        return metaData;
    }

    public void setMetaData(Map<String, Object> metaData) {
        this.metaData = metaData;
    }

}
