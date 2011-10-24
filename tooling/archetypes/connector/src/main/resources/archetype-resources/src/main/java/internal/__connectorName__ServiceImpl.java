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

package ${package}.internal;


import org.openengsb.core.common.AbstractOpenEngSBConnectorService;
import org.openengsb.core.api.AliveState;

import org.openengsb.core.api.Domain;
import org.openengsb.domain.${domainL}.${domainInterface};

public class ${connectorName}ServiceImpl extends AbstractOpenEngSBConnectorService implements ${domainInterface} {

    private String attr;

    public ${connectorName}ServiceImpl() {
    }

    void setAttr(String attr) {
        this.attr = attr;
    }

    public String getAttr() {
        return attr;
    }

    @Override
    public AliveState getAliveState() {
        // TODO Auto-generated method stub
        return AliveState.DISCONNECTED;
    }

    // TODO implement domain methods here
}
