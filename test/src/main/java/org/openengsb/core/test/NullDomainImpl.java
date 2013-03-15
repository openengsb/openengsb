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

package org.openengsb.core.test;

import org.openengsb.core.api.AliveState;
import org.openengsb.core.api.Connector;

public class NullDomainImpl implements NullDomain, Connector {

    private AliveState state = AliveState.OFFLINE;

    @Override
    public AliveState getAliveState() {
        return state;
    }

    @Override
    public void nullMethod() {
    }

    @Override
    public Object nullMethod(Object o) {
        return o;
    }

    @Override
    public Object nullMethod(Object o, String b) {
        return o;
    }

    protected String instanceId;

    public NullDomainImpl() {
    }

    public NullDomainImpl(String instanceId) {
        this.instanceId = instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    @Override
    public String getInstanceId() {
        return instanceId;
    }

    public void setAliveState(AliveState state) {
        this.state = state;
    }

    @Override
    public void setDomainId(String domainId) {
    }

    @Override
    public String getDomainId() {
        return null;
    }

    @Override
    public void setConnectorId(String connectorId) {
    }

    @Override
    public String getConnectorId() {
        return null;
    }

    @Override
    public void commitModel(DummyModel model) {
    }

}
