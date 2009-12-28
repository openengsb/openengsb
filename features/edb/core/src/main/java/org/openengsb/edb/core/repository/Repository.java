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

import java.io.File;
import java.io.IOException;

/**
 * Internal wrapper interface for the real implementation used to store and
 * version the data behind the interface. This could be a GIT repository, a SVN
 * repository... Repositories could be created via the RepositoryFactory class.
 */
public interface Repository {

    /**
     * Method creates a repository at the actual user-path with the repositoryId
     * as sub-path. The path of the repository could be seen as ./repositoryId
     * in more detail. If any exception in the state of the repository exists an
     * {@link RepositoryStateException} is thrown. This could happen if jgit
     * throws any internal {@link IOException}. A
     * {@link RepositoryManagementException}should be thrown in each other case
     * (e.g. if null is entered for the {@link #create(String)} method. This
     * method should return the pointer to an existing repository or create a
     * new one if the repository does not exist at the moment.
     */
    Repository create(final String repositoryId) throws RepositoryStateException, RepositoryManagementException;

    /**
     * Method returning a class to finally prepare and do a commit.
     */
    Commit prepareCommit();

    /**
     * Method returning a class to finally prepare and do a reset.
     */
    Reset prepareReset();

    /**
     * Be careful in using this method; it will delete the entire repository
     * recursively.
     */
    void removeRepository();

    /**
     * Returns the base path of an repository.
     */
    File getRepositoryBase();

    /**
     * Returns the current head revision identifier.
     * 
     * @throws RepositoryStateException in case the repository is in illegal
     *         state
     * @return head id
     */
    String getHeadRevision() throws RepositoryStateException;
}
