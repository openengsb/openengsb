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

package org.openengsb.edb.core.repository.jgit;

import org.openengsb.edb.core.repository.Repository;
import org.openengsb.edb.core.repository.RepositoryFactory;
import org.openengsb.edb.core.repository.RepositoryManagementException;
import org.openengsb.edb.core.repository.RepositoryStateException;

/**
 * Implementation of the {@link RepositoryFactory} to create repositories may
 * be defined by other params. The default repository created is a
 * {@link GitRepository}.
 */
public class GitRepositoryFactory implements RepositoryFactory {

    private String defaultRepositoryId;

    @Override
    public Repository loadRepository(final String repositoryId) throws RepositoryManagementException,
            RepositoryStateException {
        return new GitRepository().create(repositoryId);
    }

    @Override
    public Repository loadDefaultRepository() throws RepositoryManagementException, RepositoryStateException {
        return loadRepository(this.defaultRepositoryId);
    }

    public void setDefaultRepositoryId(final String defaultRepositoryId) {
        this.defaultRepositoryId = defaultRepositoryId;
    }

}
