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

package org.openengsb.connector.wicketacl;

import org.openengsb.core.api.Constants;
import org.openengsb.core.api.security.model.Permission;
import org.openengsb.labs.delegation.service.Provide;

@Provide(Constants.DELEGATION_CONTEXT_PERMISSIONS)
public class WicketPermission implements Permission {

    private String componentName;
    private String action;

    public WicketPermission() {
    }

    public WicketPermission(String componentName, String action) {
        this.componentName = componentName;
        this.action = action;
    }

    public WicketPermission(String componentName) {
        this.componentName = componentName;
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

    @Override
    public String describe() {
        return String.format("Permission to %s all components belonging the ui context of %s", action, componentName);
    }

    @Override
    public String toString() {
        return String.format("P %s:%s", componentName, action);
    }

}
