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
package org.openengsb.framework.edbi.hook;

import org.openengsb.core.api.context.ContextHolder;
import org.openengsb.core.api.security.AuthenticationContext;
import org.openengsb.core.edbi.api.IndexCommit;
import org.openengsb.core.edbi.api.IndexEngine;
import org.openengsb.core.ekb.api.EKBCommit;
import org.openengsb.core.ekb.api.hooks.EKBPostCommitHook;
import org.openengsb.framework.edbi.hook.internal.CommitConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * EKBPostCommitHook implementation responsible for extracting model information and calling {@code IndexService} to
 * create indices for model types.
 */
public class IndexHook implements EKBPostCommitHook {

    private static final Logger LOG = LoggerFactory.getLogger(IndexHook.class);

    private IndexEngine indexEngine;
    private AuthenticationContext authenticationContext;

    @Override
    public void onPostCommit(EKBCommit ekbCommit) {
        LOG.info("Caught onPostCommit event for EKBCommit {}", ekbCommit.getRevisionNumber());

        CommitConverter commitConverter = new CommitConverter(getAuthenticationContext(), getContextHolder());

        IndexCommit commit = commitConverter.convert(ekbCommit);

        indexEngine.commit(commit);
    }

    public ContextHolder getContextHolder() {
        return ContextHolder.get();
    }

    public AuthenticationContext getAuthenticationContext() {
        return authenticationContext;
    }

    public void setAuthenticationContext(AuthenticationContext authenticationContext) {
        this.authenticationContext = authenticationContext;
    }

    public IndexEngine getIndexEngine() {
        return indexEngine;
    }

    public void setIndexEngine(IndexEngine indexEngine) {
        this.indexEngine = indexEngine;
    }
}
