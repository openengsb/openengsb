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

package org.openengsb.core.security.model;

import java.lang.reflect.Method;

import javax.persistence.Entity;

import org.openengsb.core.api.OpenEngSBService;

@Entity
public class ServicePermission extends AbstractPermission {

    private String instanceId;

    public ServicePermission() {
    }

    public ServicePermission(String service) {
        instanceId = service;
    }

    public ServicePermission(String instanceId, String contextId) {
        super(contextId);
        this.instanceId = instanceId;
    }

    @Override
    public boolean internalPermits(Object service, Method operation, Object[] args) {
        if (!(service instanceof OpenEngSBService)) {
            return false;
        }
        return ((OpenEngSBService) service).getInstanceId().equals(instanceId);
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((instanceId == null) ? 0 : instanceId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ServicePermission other = (ServicePermission) obj;
        if (instanceId == null) {
            if (other.instanceId != null) {
                return false;
            }
        } else if (!instanceId.equals(other.instanceId)) {
            return false;
        }
        return true;
    }

}
