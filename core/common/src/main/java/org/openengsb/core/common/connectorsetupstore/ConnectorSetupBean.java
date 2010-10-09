/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.core.common.connectorsetupstore;

import java.util.HashMap;
import java.util.Map;

public class ConnectorSetupBean {

    private String domain;

    private String id;

    private Map<String, String> properties;

    public ConnectorSetupBean(String domain, String id, Map<String, String> properties) {
        this.domain = domain;
        this.id = id;
        this.properties = properties;
    }

    public String getDomain() {
        return domain;
    }

    public String getId() {
        return id;
    }

    public Map<String, String> getProperties() {
        return new HashMap<String, String>(properties);
    }

}
