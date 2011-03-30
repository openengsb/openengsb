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

package org.openengsb.core.api.persistence;

import org.apache.commons.lang.ObjectUtils;

public class ConnectorDomainPair {
    private String domain;

    private String connector;

    public ConnectorDomainPair(String domain, String connector) {
        this.domain = domain;
        this.connector = connector;
    }

    public String getDomain() {
        return domain;
    }

    public String getConnector() {
        return connector;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ConnectorDomainPair)) {
            return false;
        }
        ConnectorDomainPair other = (ConnectorDomainPair) obj;
        return ObjectUtils.equals(domain, other.domain) && ObjectUtils.equals(connector, other.connector);
    }

    @Override
    public int hashCode() {
        return 17 + ObjectUtils.hashCode(domain) * 13 + ObjectUtils.hashCode(connector);
    }

}
