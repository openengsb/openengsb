/**

   Copyright 2010 OpenEngSB Division, Vienna University of Technology

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

package org.openengsb.edb.core.api;

import org.openengsb.edb.core.repository.RepositoryFactory;
import org.openengsb.edb.core.search.IndexFactory;

/**
 * Factory to create or load repositories; or delete them.
 */
public interface EDBHandlerFactory {

    /**
     * Creates a handler accessing an EDB in the given path, if it (the EDB)
     * doesn't exists, else simply opens it.
     */
    EDBHandler loadRepository(String repositoryId);

    /**
     * Loads a handler for an EDB in 'the' default path.
     */
    EDBHandler loadDefaultRepository();

    /**
     * Set repo id used in {@link #loadDefaultRepository()}
     */
    void setDefaultRepositoryId(String defaultRepositoryId);

    /**
     * Set factory used to provide handler to manage versioning of the search
     * index
     */
    void setIndexRepoFactory(RepositoryFactory indexRepoFactory);

    /**
     * Set factory used to provide handler to store, update and delete content
     */
    void setDataRepoFactory(RepositoryFactory dataRepoFactory);

    /**
     * Set factory used to provide handler to index and search content
     */
    void setIndexFactory(IndexFactory indexFactory);
}
