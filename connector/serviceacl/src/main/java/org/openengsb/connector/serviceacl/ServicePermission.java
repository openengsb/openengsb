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
package org.openengsb.connector.serviceacl;

import org.openengsb.core.api.Constants;
import org.openengsb.core.api.security.model.Permission;
import org.openengsb.labs.delegation.service.Provide;

@Provide(Constants.DELEGATION_CONTEXT_PERMISSIONS)
public class ServicePermission implements Permission {

    private String type;
    private String instance;
    private String operation;
    private String context;

    public ServicePermission() {
    }

    public ServicePermission(String type) {
        this.type = type;
    }

    public ServicePermission(String type, String operation) {
        this.type = type;
        this.operation = operation;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    @Override
    public String describe() {
        return null;
    }
}
