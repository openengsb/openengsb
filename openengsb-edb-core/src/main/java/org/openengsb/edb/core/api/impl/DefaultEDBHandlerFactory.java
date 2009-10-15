/**

   Copyright 2009 OpenEngSB Division, Vienna University of Technology

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
   
 */

package org.openengsb.edb.core.api.impl;

import org.openengsb.edb.core.api.EDBHandler;
import org.openengsb.edb.core.api.EDBHandlerFactory;
import org.openengsb.edb.core.repository.RepositoryFactory;
import org.openengsb.edb.core.repository.RepositoryManagementException;
import org.openengsb.edb.core.repository.RepositoryStateException;
import org.openengsb.edb.core.search.IndexFactory;

public class DefaultEDBHandlerFactory implements EDBHandlerFactory {

    private String defaultRepositoryId;

    private IndexFactory indexFactory;
    private RepositoryFactory indexRepoFactory;
    private RepositoryFactory dataRepoFactory;

    public EDBHandler loadDefaultRepository() throws RepositoryManagementException, RepositoryStateException {
        return new DefaultEDBHandler(this.defaultRepositoryId, false, this.dataRepoFactory, this.indexRepoFactory,
                this.indexFactory);
    }

    public EDBHandler loadRepository(String repositoryId) throws RepositoryManagementException,
            RepositoryStateException {
        return new DefaultEDBHandler(repositoryId, false, this.dataRepoFactory, this.indexRepoFactory,
                this.indexFactory);
    }

    public final String getDefaultRepositoryId() {
        return this.defaultRepositoryId;
    }

    public void setDefaultRepositoryId(String defaultRepositoryId) {
        this.defaultRepositoryId = defaultRepositoryId;
    }

    public void setDataRepoFactory(RepositoryFactory dataRepoFactory) {
        this.dataRepoFactory = dataRepoFactory;
    }

    public void setIndexFactory(IndexFactory indexFactory) {
        this.indexFactory = indexFactory;
    }

    public void setIndexRepoFactory(RepositoryFactory indexRepoFactory) {
        this.indexRepoFactory = indexRepoFactory;
    }
}
