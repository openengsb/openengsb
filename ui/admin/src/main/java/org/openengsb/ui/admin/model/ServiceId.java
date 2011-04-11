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

package org.openengsb.ui.admin.model;

import java.io.Serializable;

@SuppressWarnings("serial")
public class ServiceId implements Serializable {
    private String serviceClass;
    private String serviceId;
    private String domainName;

    public ServiceId(String serviceClass, String serviceId) {
        this.serviceClass = serviceClass;
        this.serviceId = serviceId;
    }

    public ServiceId(String serviceClass, String serviceId, String domainName) {
        this.serviceClass = serviceClass;
        this.serviceId = serviceId;
        this.domainName = domainName;
    }

    public ServiceId() {
    }

    public String getServiceClass() {
        return this.serviceClass;
    }

    public void setServiceClass(String serviceClass) {
        this.serviceClass = serviceClass;
    }

    public String getServiceId() {
        return this.serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getDomainName() {
        return this.domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", serviceId, serviceClass);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (this.serviceClass == null ? 0 : this.serviceClass.hashCode());
        result = prime * result + (this.serviceId == null ? 0 : this.serviceId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ServiceId)) {
            return false;
        }
        ServiceId other = (ServiceId) obj;
        return other.serviceClass.equals(serviceClass) && other.serviceId.equals(serviceId);
    }

}
