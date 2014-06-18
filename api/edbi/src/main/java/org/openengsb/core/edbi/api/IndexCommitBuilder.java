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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.openengsb.core.api.model.OpenEngSBModel;
import org.openengsb.core.ekb.api.EKBCommit;

/**
 * Fluent interface for creating Index commits.
 */
public class IndexCommitBuilder {

    /**
     * The commit being built
     */
    private IndexCommit commit;

    public IndexCommitBuilder() {
        commit = new IndexCommit();

        commit.setTimestamp(new Date());
        commit.setCommitId(UUID.randomUUID());

        commit.setModelClasses(new HashSet<Class<?>>());
        commit.setInserts(new HashMap<Class<?>, List<OpenEngSBModel>>());
        commit.setUpdates(new HashMap<Class<?>, List<OpenEngSBModel>>());
        commit.setDeletes(new HashMap<Class<?>, List<OpenEngSBModel>>());
    }

    /**
     * Sets the user of the commit.
     * 
     * @param user the value to set
     * @return this for chaining
     */
    public IndexCommitBuilder user(String user) {
        commit.setUser(user);
        return this;
    }

    /**
     * Sets the commit id of the commit.
     * 
     * @param revisionId the value to set
     * @return this for chaining
     */
    public IndexCommitBuilder commit(UUID revisionId) {
        commit.setCommitId(revisionId);
        return this;
    }

    /**
     * Sets the context id of the commit.
     * 
     * @param contextId the value to set
     * @return this for chaining
     */
    public IndexCommitBuilder context(String contextId) {
        commit.setContextId(contextId);
        return this;
    }

    /**
     * Sets the domain id of the commit.
     * 
     * @param domainId the value to set
     * @return this for chaining
     */
    public IndexCommitBuilder domain(String domainId) {
        commit.setDomainId(domainId);
        return this;
    }

    /**
     * Sets the connector id of the commit.
     * 
     * @param connectorId the value to set
     * @return this for chaining
     */
    public IndexCommitBuilder connector(String connectorId) {
        commit.setConnectorId(connectorId);
        return this;
    }

    /**
     * Sets the instance id of the commit.
     * 
     * @param instanceId the value to set
     * @return this for chaining
     */
    public IndexCommitBuilder instance(String instanceId) {
        commit.setInstanceId(instanceId);
        return this;
    }

    /**
     * Sets the commit timestamp.
     * 
     * @param timestamp the value to set
     * @return this for chaining
     */
    public IndexCommitBuilder timestamp(Date timestamp) {
        commit.setTimestamp(timestamp);
        return this;
    }

    /**
     * Sets the parent revision id of the commit.
     * 
     * @param parentId the revision id of the parent
     * @return this for chaining
     */
    public IndexCommitBuilder parent(UUID parentId) {
        commit.setParentCommitId(parentId);
        return this;
    }

    /**
     * Sets the parent revision id of the commit to the id of the given commit.
     * 
     * @param parent the parent commit
     * @return this for chaining
     */
    public IndexCommitBuilder parent(IndexCommit parent) {
        return parent(parent.getCommitId());
    }

    /**
     * Sets the parent revision id of the commit to the id of the given commit.
     * 
     * @param parent the parent commit
     * @return this for chaining
     */
    public IndexCommitBuilder parent(EKBCommit parent) {
        return parent(parent.getRevisionNumber());
    }

    /**
     * Marks the given object for insertion in the commit.
     * 
     * @param object a OpenEngSBModel instance
     * @return this for chaining
     */
    public IndexCommitBuilder insert(Object object) {
        updateModelClassSet(object);
        getInsertList(object.getClass()).add((OpenEngSBModel) object);
        return this;
    }

    /**
     * Marks the given object for updating in the commit.
     * 
     * @param object a OpenEngSBModel instance
     * @return this for chaining
     */
    public IndexCommitBuilder update(Object object) {
        updateModelClassSet(object);
        getUpdateList(object.getClass()).add((OpenEngSBModel) object);
        return this;
    }

    /**
     * Marks the given object for deletion in the commit.
     * 
     * @param object a OpenEngSBModel instance
     * @return this for chaining
     */
    public IndexCommitBuilder delete(Object object) {
        updateModelClassSet(object);
        getDeleteList(object.getClass()).add((OpenEngSBModel) object);
        return this;
    }

    /**
     * Returns the IndexCommit being built.
     * 
     * @return an IndexCommit instance
     */
    public IndexCommit get() {
        return commit;
    }

    protected void updateModelClassSet(Object object) {
        commit.getModelClasses().add(object.getClass());
    }

    protected List<OpenEngSBModel> getInsertList(Class<?> clazz) {
        return lazyGet(commit.getInserts(), clazz);
    }

    protected List<OpenEngSBModel> getUpdateList(Class<?> clazz) {
        return lazyGet(commit.getUpdates(), clazz);
    }

    protected List<OpenEngSBModel> getDeleteList(Class<?> clazz) {
        return lazyGet(commit.getDeletes(), clazz);
    }

    protected List<OpenEngSBModel> lazyGet(Map<Class<?>, List<OpenEngSBModel>> map, Class<?> clazz) {
        List<OpenEngSBModel> list = map.get(clazz);

        if (list == null) {
            list = new ArrayList<>();
            map.put(clazz, list);
        }

        return list;
    }

    /**
     * Create a new IndexCommitBuilder. Use the fluent interface to set the commits values and then call {@link #get()}
     * to retrieve the instance.
     * 
     * @return a new IndexCommitBuilder instance.
     */
    public static IndexCommitBuilder create() {
        return new IndexCommitBuilder();
    }

}
