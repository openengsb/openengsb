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

import org.openengsb.connector.wicketacl.WicketPermission;
import org.openengsb.core.api.Constants;
import org.openengsb.core.api.model.annotation.Model;
import org.openengsb.core.api.model.annotation.OpenEngSBModelId;
import org.openengsb.labs.delegation.service.Provide;

@Provide(context = { Constants.DELEGATION_CONTEXT_MODELS })
@Model
public class Permission {

    @OpenEngSBModelId
    private String componentName;

    private String action;

    public Permission() {
    }

    public Permission(String componentName, String action) {
        this.componentName = componentName;
        this.action = action;
    }

    public Permission(String componentName) {
        this.componentName = componentName;
    }

    public Permission(WicketPermission permission) {
        componentName = permission.getComponentName();
        action = permission.getAction();
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public WicketPermission toWicketPermission() {
        return new WicketPermission(componentName, action);
    }

    @Override
    public String toString() {
        return String.format("Permission: %s:%s", componentName, action);
    }

    @Override
    public int hashCode() {
        return Objects.hash(componentName, action);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Permission)) {
            return false;
        }
        final Permission other = (Permission) obj;
        return Objects.equals(componentName, other.componentName) && Objects.equals(action, other.action);
    }
}
