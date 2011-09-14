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
package org.openengsb.ui.common.model;

import org.openengsb.core.security.model.GenericPermission;

public class UiPermission extends GenericPermission {
    private String securityAttribute;

    public UiPermission() {
    }

    public UiPermission(String componentSecurityContext) {
        this.securityAttribute = componentSecurityContext;
    }

    public String getSecurityAttribute() {
        return securityAttribute;
    }

    public void setSecurityAttribute(String securityAttribute) {
        this.securityAttribute = securityAttribute;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((securityAttribute == null) ? 0 : securityAttribute.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        UiPermission other = (UiPermission) obj;
        if (securityAttribute == null) {
            if (other.securityAttribute != null)
                return false;
        } else if (!securityAttribute.equals(other.securityAttribute))
            return false;
        return true;
    }

}
