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

package org.openengsb.core.edbi.hooks;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.openengsb.core.api.model.OpenEngSBModel;
import org.openengsb.core.edbi.api.IndexEngine;
import org.openengsb.core.ekb.api.EKBCommit;
import org.openengsb.core.ekb.api.hooks.EKBPostCommitHook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * EKBPostCommitHook implementation responsible for extracting model information and calling {@code IndexService} to
 * create indices for model types.
 */
public class IndexStructuringHook implements EKBPostCommitHook {

    private static final Logger LOG = LoggerFactory.getLogger(IndexStructuringHook.class);

    private IndexEngine indexEngine;

    @Override
    public void onPostCommit(EKBCommit commit) {
        for (Class<? extends OpenEngSBModel> type : extractTypes(commit.getInserts())) {
            if (!indexEngine.indexExists(type)) {
                LOG.info("Creating index for {}", type);
                indexEngine.createIndex(type);
            }
        }
    }

    private Set<Class<? extends OpenEngSBModel>> extractTypes(Collection<OpenEngSBModel> models) {
        Set<Class<? extends OpenEngSBModel>> types = new HashSet<>();

        for (OpenEngSBModel model : models) {
            types.add(model.getClass());
        }

        return types;
    }

    public IndexEngine getIndexEngine() {
        return indexEngine;
    }

    public void setIndexEngine(IndexEngine indexEngine) {
        this.indexEngine = indexEngine;
    }

}
