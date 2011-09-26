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

package org.openengsb.domain.example;

import org.apache.commons.lang.StringUtils;
import org.openengsb.core.api.security.model.Permission;

public class ExamplePermission implements Permission {

    private String serviceId;
    private String context;
    private String allowedPrefix;

    public ExamplePermission(String allowedPrefix) {
        this.allowedPrefix = allowedPrefix;
    }

    public ExamplePermission() {
    }

    @Override
    public String describe() {
        return "Allows to call Example-domain with example messages starting with %s";
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getAllowedPrefix() {
        return allowedPrefix;
    }

    public void setAllowedPrefix(String allowedPrefix) {
        this.allowedPrefix = allowedPrefix;
    }

    @Override
    public String toString() {

        return String.format("serviceId: %s, context: %s, allowedPrefix: \"%s\"",
            StringUtils.defaultString(serviceId, "ALL"),
            StringUtils.defaultString(context, "ALL"),
            allowedPrefix);
    }
}
