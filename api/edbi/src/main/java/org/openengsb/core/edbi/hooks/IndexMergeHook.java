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

import org.openengsb.core.edb.api.EDBCommit;
import org.openengsb.core.edb.api.hooks.EDBPostCommitHook;
import org.openengsb.core.edbi.api.IndexEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of an EDBPostCommitHook that merges data from EDBCommits into the EDBI.
 */
public class IndexMergeHook implements EDBPostCommitHook {

    private static final Logger LOG = LoggerFactory.getLogger(IndexMergeHook.class);

    private IndexEngine indexEngine;

    @Override
    public void onPostCommit(EDBCommit commit) {
        LOG.info("Merging EDBCommit " + commit.getRevisionNumber());

        indexEngine.merge(commit);
    }

    public IndexEngine getIndexEngine() {
        return indexEngine;
    }

    public void setIndexEngine(IndexEngine indexEngine) {
        this.indexEngine = indexEngine;
    }

}
