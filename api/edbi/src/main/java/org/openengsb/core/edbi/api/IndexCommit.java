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
package org.openengsb.core.edbi.api;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.openengsb.core.api.model.OpenEngSBModel;

/**
 * A commit.
 */
public class IndexCommit {

    private UUID commitId;
    private UUID parentCommitId;
    private Date timestamp;

    private String user;
    private String contextId;

    private String connectorId;
    private String domainId;
    private String instanceId;

    private Set<Class<?>> modelClasses;

    private Map<Class<?>, List<OpenEngSBModel>> inserts;
    private Map<Class<?>, List<OpenEngSBModel>> updates;
    private Map<Class<?>, List<OpenEngSBModel>> deletes;

    public UUID getCommitId() {
        return commitId;
    }

    public void setCommitId(UUID commitId) {
        this.commitId = commitId;
    }

    public UUID getParentCommitId() {
        return parentCommitId;
    }

    public void setParentCommitId(UUID parentCommitId) {
        this.parentCommitId = parentCommitId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getContextId() {
        return contextId;
    }

    public void setContextId(String contextId) {
        this.contextId = contextId;
    }

    public String getConnectorId() {
        return connectorId;
    }

    public void setConnectorId(String connectorId) {
        this.connectorId = connectorId;
    }

    public String getDomainId() {
        return domainId;
    }

    public void setDomainId(String domainId) {
        this.domainId = domainId;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public Set<Class<?>> getModelClasses() {
        return modelClasses;
    }

    public void setModelClasses(Set<Class<?>> modelClasses) {
        this.modelClasses = modelClasses;
    }

    public Map<Class<?>, List<OpenEngSBModel>> getInserts() {
        return inserts;
    }

    public void setInserts(Map<Class<?>, List<OpenEngSBModel>> inserts) {
        this.inserts = inserts;
    }

    public Map<Class<?>, List<OpenEngSBModel>> getUpdates() {
        return updates;
    }

    public void setUpdates(Map<Class<?>, List<OpenEngSBModel>> updates) {
        this.updates = updates;
    }

    public Map<Class<?>, List<OpenEngSBModel>> getDeletes() {
        return deletes;
    }

    public void setDeletes(Map<Class<?>, List<OpenEngSBModel>> deletes) {
        this.deletes = deletes;
    }
}
