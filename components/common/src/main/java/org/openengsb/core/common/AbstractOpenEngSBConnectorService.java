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

package org.openengsb.core.common;

import java.util.List;

import org.openengsb.core.api.Connector;
import org.openengsb.core.api.ekb.EKBCommit;
import org.openengsb.core.api.model.OpenEngSBModel;

/**
 * Base class for implementations of connector services. It can also prepare an EKBCommit for you.
 */
public abstract class AbstractOpenEngSBConnectorService extends AbstractOpenEngSBService implements Connector {
    protected String domainId;
    protected String connectorId;

    public AbstractOpenEngSBConnectorService() {
        super();
    }

    public AbstractOpenEngSBConnectorService(String instanceId) {
        super(instanceId);
    }

    @Override
    public void setDomainId(String domainId) {
        this.domainId = domainId;
    }

    @Override
    public String getDomainId() {
        return domainId;
    }

    @Override
    public void setConnectorId(String connectorId) {
        this.connectorId = connectorId;
    }

    @Override
    public String getConnectorId() {
        return connectorId;
    }

    /**
     * Generates an EKBCommit with the informations about the domain inserted. Also attaches the given models
     * to the commit.
     */
    public EKBCommit createEKBCommit(List<OpenEngSBModel> inserts, List<OpenEngSBModel> updates,
            List<OpenEngSBModel> deletes) {
        EKBCommit commit = createEKBCommit();
        commit.addInserts(inserts).addUpdates(updates).addDeletes(deletes);
        return commit;
    }

    /**
     * Generates an EKBCommit with the informations about the domain inserted.
     */
    public EKBCommit createEKBCommit() {
        EKBCommit commit = new EKBCommit().setDomainId(domainId).setConnectorId(connectorId);
        commit.setInstanceId(instanceId);
        return commit;
    }
}
