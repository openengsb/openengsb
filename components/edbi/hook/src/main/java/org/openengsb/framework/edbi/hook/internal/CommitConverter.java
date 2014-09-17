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
package org.openengsb.framework.edbi.hook.internal;

import static org.openengsb.core.util.CollectionUtilsExtended.group;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.openengsb.core.api.context.ContextHolder;
import org.openengsb.core.api.model.OpenEngSBModel;
import org.openengsb.core.api.security.AuthenticationContext;
import org.openengsb.core.edbi.api.IndexCommit;
import org.openengsb.core.ekb.api.EKBCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;

/**
 * Converts EKBCommits into IndexCommits.
 */
public class CommitConverter {

    private static final Logger LOG = LoggerFactory.getLogger(CommitConverter.class);

    private AuthenticationContext authenticationContext;
    private ContextHolder contextHolder;

    public CommitConverter(AuthenticationContext authenticationContext, ContextHolder contextHolder) {
        this.authenticationContext = authenticationContext;
        this.contextHolder = contextHolder;
    }

    /**
     * Convert the given EKBCommit to an IndexCommit.
     * 
     * @param ekbCommit the commit to convert
     * @return a new IndexCommit instance representing the given EKBCommit
     */
    public IndexCommit convert(EKBCommit ekbCommit) {
        IndexCommit commit = new IndexCommit();

        commit.setCommitId(ekbCommit.getRevisionNumber());
        commit.setParentCommitId(ekbCommit.getParentRevisionNumber());
        commit.setConnectorId(ekbCommit.getConnectorId());
        commit.setDomainId(ekbCommit.getDomainId());
        commit.setInstanceId(ekbCommit.getInstanceId());
        commit.setTimestamp(new Date());

        commit.setUser(getUser());
        commit.setContextId(getContextId());

        List<OpenEngSBModel> inserts = ekbCommit.getInserts();
        List<OpenEngSBModel> updates = ekbCommit.getUpdates();
        List<OpenEngSBModel> deletes = ekbCommit.getDeletes();

        Set<Class<?>> modelClasses = extractTypes(inserts, updates, deletes);

        commit.setModelClasses(modelClasses);

        commit.setInserts(mapByClass(inserts));
        commit.setUpdates(mapByClass(updates));
        commit.setDeletes(mapByClass(deletes));

        return commit;
    }

    /**
     * Groups all models in the given collection by their class.
     * 
     * @param models the models
     * @return a map containing all given models grouped by their type
     */
    protected Map<Class<?>, List<OpenEngSBModel>> mapByClass(Collection<OpenEngSBModel> models) {
        return group(models, new Function<OpenEngSBModel, Class<?>>() {
            @Override
            public Class<?> apply(OpenEngSBModel input) {
                return input.getClass();
            }
        });
    }

    protected Set<Class<?>> extractTypes(Collection<OpenEngSBModel> inserts, Collection<OpenEngSBModel> updates,
            Collection<OpenEngSBModel> deletes) {
        Set<Class<?>> types = new HashSet<>();

        extractTypesInto(inserts, types);
        extractTypesInto(updates, types);
        extractTypesInto(deletes, types);

        return types;
    }

    private void extractTypesInto(Collection<OpenEngSBModel> models, Set<Class<?>> types) {
        for (OpenEngSBModel model : models) {
            types.add(model.getClass());
        }
    }

    protected String getUser() {
        if (authenticationContext == null) {
            LOG.warn("AuthenticationContext not available, unable to retrieve current user");
            return "";
        }

        Object principal = authenticationContext.getAuthenticatedPrincipal();

        if (principal == null) {
            LOG.warn("AuthenticationContext did not return an authenticated principal");
            return "";
        } else if (principal instanceof String) {
            return (String) principal;
        } else {
            throw new UnsupportedOperationException("Can not handle principles of type " + principal.getClass());
        }
    }

    protected String getContextId() {
        if (contextHolder == null) {
            LOG.warn("ContextHolder not available, unable to retrieve current contextId");
            return "";
        }

        String contextId = contextHolder.getCurrentContextId();

        if (StringUtils.isEmpty(contextId)) {
            LOG.warn("ContextHolder returned empty contextId");
            return "";
        }

        return contextId;
    }

}
