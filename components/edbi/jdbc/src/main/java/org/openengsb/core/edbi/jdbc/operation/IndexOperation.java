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
package org.openengsb.core.edbi.jdbc.operation;

import java.util.List;

import org.openengsb.core.api.model.OpenEngSBModel;
import org.openengsb.core.edbi.api.IndexCommit;
import org.openengsb.core.edbi.jdbc.JdbcIndex;

/**
 * A unit-of-work object that encapsulates the necessary data of an operation on an index.
 */
public abstract class IndexOperation implements Operation {

    private IndexCommit commit;
    private JdbcIndex<?> index;

    private List<OpenEngSBModel> models;

    public IndexOperation(IndexCommit commit, JdbcIndex<?> index, List<OpenEngSBModel> models) {
        this.commit = commit;
        this.index = index;
        this.models = models;
    }

    /**
     * Returns the commit that issued this operation. Used to provide meta-data.
     * 
     * @return a IndexCommit
     */
    public IndexCommit getCommit() {
        return commit;
    }

    /**
     * The Index on which the operation is executed on.
     * 
     * @return an Index
     */
    public JdbcIndex<?> getIndex() {
        return index;
    }

    /**
     * A list of OpenEngSBModel entries this operation is executed on.
     * 
     * @return the models to insert
     */
    public List<OpenEngSBModel> getModels() {
        return models;
    }
}
