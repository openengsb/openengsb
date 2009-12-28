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

package org.openengsb.edb.core.repository;

/**
 * Factory to create or load repositories; or delete them.
 */
public interface RepositoryFactory {

    /**
     * Creates a repository in the given path, if it doesn't exists, else simply
     * opens it.
     */
    Repository loadRepository(final String repositoryId) throws RepositoryManagementException,
            RepositoryStateException;

    /**
     * Loads the default repository.
     */
    Repository loadDefaultRepository() throws RepositoryManagementException, RepositoryStateException;

}
