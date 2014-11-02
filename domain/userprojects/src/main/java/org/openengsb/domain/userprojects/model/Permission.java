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

package org.openengsb.domain.userprojects.model;

import java.util.Objects;

import org.openengsb.core.api.Constants;
import org.openengsb.core.api.model.annotation.Model;
import org.openengsb.core.api.model.annotation.OpenEngSBModelId;
import org.openengsb.labs.delegation.service.Provide;

/**
 * Note: To make sure that this object can be managed properly by the EDB it is recommended to call the generateUuid
 * method after setting all object attributes.
 */
@Provide(context = { Constants.DELEGATION_CONTEXT_MODELS })
@Model
public class Permission {

    @OpenEngSBModelId
    private String uuid;

    private String component;

    public Permission() {
    }

    public Permission(String component) {
        this.component = component;
    }

    public Permission(String component, String owner) {
        this.component = component;
        generateUuid(owner);
    }

    public String getUuid() {
        return uuid;
    }

    public boolean generateUuid(String owner) {
        if (component == null) {
            return false;
        } else {
            uuid = "Perm+" + owner + "+" + component;
            return true;
        }

    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    @Override
    public String toString() {
        return String.format("Permission: %s:%d", component, uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(component, uuid);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Permission)) {
            return false;
        }
        final Permission other = (Permission) obj;
        return Objects.equals(component, other.component) && Objects.equals(uuid, other.uuid);
    }
}
